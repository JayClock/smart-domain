#!/usr/bin/env python3
"""Dependency-free architecture checks for Smart Domain consumer repositories."""

from __future__ import annotations

import argparse
import json
import re
import sys
from dataclasses import asdict, dataclass
from pathlib import Path
from typing import Iterable, List, Sequence

DEFAULT_IGNORED_PARTS = {"build", "target", "generated", "node_modules", ".gradle"}
TYPE_PATTERN = re.compile(r"\b(?:class|interface|record|enum)\s+(\w+)")
ENTITY_PATTERN = re.compile(
    r"\b(?:class|record)\s+(\w+)[^{;]*?\bimplements\b[^{;]*?\bEntity\s*<",
    re.DOTALL,
)
RAW_COLLECTION_FIELD_PATTERN = re.compile(
    r"^\s*(?:private|protected|public)\s+(?:final\s+)?(?:java\.util\.)?"
    r"(?:List|Collection|Set)\s*<\s*(?:\?\s+extends\s+)?([\w.]+)",
    re.MULTILINE,
)
WIDE_INTERFACE_PATTERN = re.compile(
    r"\binterface\s+(\w+)\s+extends\s+[^\{]*(?:HasMany|HasOne)\b",
    re.MULTILINE,
)
COMMENT_OR_LITERAL_PATTERN = re.compile(
    r"//[^\n]*|/\*.*?\*/|\"(?:\\.|[^\"\\])*\"|'(?:\\.|[^'\\])*'",
    re.DOTALL,
)


class ConfigurationError(RuntimeError):
    """Raised for an invalid checker configuration."""


@dataclass(frozen=True)
class Finding:
    severity: str
    rule: str
    path: str
    line: int
    message: str


@dataclass(frozen=True)
class JavaSource:
    path: Path
    relative_path: str
    text: str
    code: str
    package: str
    imports: Sequence[str]

    def line_at(self, offset: int) -> int:
        return self.text.count("\n", 0, offset) + 1


def _string_list(config: dict, key: str, required: bool = True) -> List[str]:
    value = config.get(key)
    if value is None and not required:
        return []
    if not isinstance(value, list) or not all(isinstance(item, str) and item for item in value):
        raise ConfigurationError(f"{key} must be a non-empty string array")
    if required and not value:
        raise ConfigurationError(f"{key} must not be empty")
    return value


def load_config(path: Path) -> dict:
    try:
        config = json.loads(path.read_text(encoding="utf-8"))
    except OSError as error:
        raise ConfigurationError(f"cannot read config {path}: {error}") from error
    except json.JSONDecodeError as error:
        raise ConfigurationError(f"invalid JSON in {path}: {error}") from error
    if not isinstance(config, dict):
        raise ConfigurationError("config must contain a JSON object")
    if config.get("schemaVersion") != 1:
        raise ConfigurationError("config schemaVersion must be 1")
    for key in [
        "sourceRoots",
        "basePackages",
        "domainPackages",
        "apiPackages",
        "persistencePackages",
    ]:
        _string_list(config, key)
    _string_list(config, "ignoredPathParts", required=False)
    return config


def package_matches(package: str, prefixes: Iterable[str]) -> bool:
    return any(package == prefix or package.startswith(prefix + ".") for prefix in prefixes)


def import_matches(import_name: str, prefixes: Iterable[str]) -> bool:
    return any(import_name == prefix or import_name.startswith(prefix + ".") for prefix in prefixes)


def mask_comments_and_literals(text: str) -> str:
    def mask(match: re.Match[str]) -> str:
        return "".join("\n" if character == "\n" else " " for character in match.group(0))

    return COMMENT_OR_LITERAL_PATTERN.sub(mask, text)


def parse_source(path: Path, repository: Path) -> JavaSource:
    text = path.read_text(encoding="utf-8")
    code = mask_comments_and_literals(text)
    package_match = re.search(r"^\s*package\s+([\w.]+)\s*;", code, re.MULTILINE)
    imports = re.findall(r"^\s*import\s+(?:static\s+)?([\w.*]+)\s*;", code, re.MULTILINE)
    return JavaSource(
        path=path,
        relative_path=path.relative_to(repository).as_posix(),
        text=text,
        code=code,
        package=package_match.group(1) if package_match else "",
        imports=imports,
    )


def collect_sources(repository: Path, config: dict) -> tuple[List[JavaSource], List[Finding]]:
    ignored = set(_string_list(config, "ignoredPathParts", required=False)) | DEFAULT_IGNORED_PARTS
    sources: List[JavaSource] = []
    findings: List[Finding] = []
    for root_name in _string_list(config, "sourceRoots"):
        source_root = (repository / root_name).resolve()
        try:
            source_root.relative_to(repository)
        except ValueError as error:
            raise ConfigurationError(f"source root escapes repository: {root_name}") from error
        if not source_root.exists():
            findings.append(
                Finding("warning", "SD000", root_name, 1, "configured source root does not exist")
            )
            continue
        for path in sorted(source_root.rglob("*.java")):
            relative_parts = path.relative_to(repository).parts
            if any(part in ignored for part in relative_parts):
                continue
            sources.append(parse_source(path, repository))
    if not sources:
        findings.append(Finding("warning", "SD000", ".", 1, "no Java sources were found"))
    return sources, findings


def check_orchestration_types(source: JavaSource, config: dict) -> List[Finding]:
    if not package_matches(source.package, _string_list(config, "basePackages")):
        return []
    segments = set(source.package.split("."))
    classified = package_matches(
        source.package,
        _string_list(config, "domainPackages") + _string_list(config, "apiPackages"),
    )
    forbidden_package = bool(segments & {"application", "usecase", "usecases"})
    findings: List[Finding] = []
    if forbidden_package:
        findings.append(
            Finding(
                "error",
                "SD001",
                source.relative_path,
                1,
                f"forbidden orchestration package: {source.package}",
            )
        )
    if classified or forbidden_package:
        allowed_services = set(_string_list(config, "allowedServiceTypes", required=False))
        for match in TYPE_PATTERN.finditer(source.code):
            name = match.group(1)
            qualified_name = f"{source.package}.{name}"
            if name.endswith("Service") and qualified_name in allowed_services:
                continue
            if name.endswith(
                ("Service", "Facade", "UseCase", "UseCaseHandler", "CommandHandler", "Mediator")
            ):
                findings.append(
                    Finding(
                        "error",
                        "SD001",
                        source.relative_path,
                        source.line_at(match.start()),
                        f"business orchestration type is forbidden: {name}",
                    )
                )
    return findings


def check_layer_imports(source: JavaSource, config: dict) -> List[Finding]:
    domain_packages = _string_list(config, "domainPackages")
    api_packages = _string_list(config, "apiPackages")
    persistence_packages = _string_list(config, "persistencePackages")
    findings: List[Finding] = []

    if package_matches(source.package, domain_packages):
        forbidden = [
            "org.springframework",
            "org.mybatis",
            "jakarta.ws.rs",
            "jakarta.persistence",
            "com.fasterxml.jackson",
            "java.sql",
            "io.github.jayclock.smartdomain.mybatis",
        ] + api_packages + persistence_packages
        for imported in source.imports:
            if import_matches(imported, forbidden):
                findings.append(
                    Finding(
                        "error",
                        "SD002",
                        source.relative_path,
                        1,
                        f"domain imports framework or outer-layer type: {imported}",
                    )
                )

    if package_matches(source.package, api_packages):
        forbidden = persistence_packages + [
            "org.mybatis",
            "io.github.jayclock.smartdomain.mybatis",
        ]
        for imported in source.imports:
            simple_name = imported.rsplit(".", 1)[-1]
            if import_matches(imported, forbidden) or simple_name.endswith("Mapper"):
                findings.append(
                    Finding(
                        "error",
                        "SD003",
                        source.relative_path,
                        1,
                        f"API imports persistence implementation: {imported}",
                    )
                )

    if package_matches(source.package, persistence_packages):
        for imported in source.imports:
            if import_matches(imported, api_packages):
                findings.append(
                    Finding(
                        "error",
                        "SD004",
                        source.relative_path,
                        1,
                        f"persistence imports API type: {imported}",
                    )
                )
    return findings


def check_association_shape(source: JavaSource, entity_names: set[str], config: dict) -> List[Finding]:
    if not package_matches(source.package, _string_list(config, "domainPackages")):
        return []
    findings: List[Finding] = []
    for match in RAW_COLLECTION_FIELD_PATTERN.finditer(source.code):
        target = match.group(1).rsplit(".", 1)[-1]
        if target in entity_names:
            findings.append(
                Finding(
                    "error",
                    "SD005",
                    source.relative_path,
                    source.line_at(match.start()),
                    f"raw entity collection must be an association object: {target}",
                )
            )

    for interface_match in WIDE_INTERFACE_PATTERN.finditer(source.code):
        wide_name = interface_match.group(1)
        accessor = re.compile(rf"\bpublic\s+{re.escape(wide_name)}\s+\w+\s*\(")
        for match in accessor.finditer(source.code):
            findings.append(
                Finding(
                    "error",
                    "SD006",
                    source.relative_path,
                    source.line_at(match.start()),
                    f"public accessor leaks wide association interface: {wide_name}",
                )
            )
    return findings


def run_checks(config_path: Path) -> List[Finding]:
    config_path = config_path.expanduser().resolve()
    config = load_config(config_path)
    repository = config_path.parent.parent
    sources, findings = collect_sources(repository, config)
    entity_names = {
        match.group(1)
        for source in sources
        if package_matches(source.package, _string_list(config, "domainPackages"))
        for match in ENTITY_PATTERN.finditer(source.code)
    }
    for source in sources:
        findings.extend(check_orchestration_types(source, config))
        findings.extend(check_layer_imports(source, config))
        findings.extend(check_association_shape(source, entity_names, config))
    return sorted(findings, key=lambda item: (item.path, item.line, item.rule, item.message))


def format_text(findings: Sequence[Finding]) -> str:
    if not findings:
        return "Smart Domain check passed."
    lines = [
        f"{finding.severity.upper()} {finding.rule} {finding.path}:{finding.line} {finding.message}"
        for finding in findings
    ]
    errors = sum(finding.severity == "error" for finding in findings)
    warnings = sum(finding.severity == "warning" for finding in findings)
    lines.append(f"Smart Domain check: {errors} error(s), {warnings} warning(s).")
    return "\n".join(lines)


def parse_args(argv: Sequence[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Check a Java backend against Smart Domain Pattern v1.")
    parser.add_argument(
        "--config",
        type=Path,
        default=Path(__file__).resolve().with_name("config.json"),
        help="checker config (default: config.json beside this script)",
    )
    parser.add_argument("--format", choices=["text", "json"], default="text")
    parser.add_argument(
        "--strict-warnings", action="store_true", help="return failure when warnings are present"
    )
    return parser.parse_args(argv)


def main(argv: Sequence[str] | None = None) -> int:
    args = parse_args(sys.argv[1:] if argv is None else argv)
    try:
        findings = run_checks(args.config)
    except ConfigurationError as error:
        print(f"configuration error: {error}", file=sys.stderr)
        return 2
    if args.format == "json":
        print(json.dumps([asdict(finding) for finding in findings], indent=2))
    else:
        print(format_text(findings))
    has_errors = any(finding.severity == "error" for finding in findings)
    has_strict_warnings = args.strict_warnings and bool(findings)
    return 1 if has_errors or has_strict_warnings else 0


if __name__ == "__main__":
    raise SystemExit(main())
