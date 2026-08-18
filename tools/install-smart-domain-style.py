#!/usr/bin/env python3
"""Install the portable Smart Domain style kit into another repository."""

from __future__ import annotations

import argparse
import json
import re
import shutil
import subprocess
import sys
from pathlib import Path
from typing import Dict, List, Optional

MANAGED_START = "<!-- smart-domain-style:start -->"
MANAGED_END = "<!-- smart-domain-style:end -->"
MANIFEST_INSTALLER = "smart-domain-style"
JAVA_PACKAGE_PATTERN = re.compile(r"^[A-Za-z_]\w*(?:\.[A-Za-z_]\w*)*$")


class InstallError(RuntimeError):
    """Raised when installation would be unsafe or the source kit is invalid."""


def repository_root() -> Path:
    return Path(__file__).resolve().parents[1]


def read_runtime_version(root: Path) -> str:
    properties = (root / "gradle.properties").read_text(encoding="utf-8")
    match = re.search(r"^smartDomainVersion=(.+)$", properties, re.MULTILINE)
    if not match:
        raise InstallError("gradle.properties does not define smartDomainVersion")
    return match.group(1).strip()


def read_pattern_version(root: Path) -> str:
    contract = (root / "docs/pattern-contract.md").read_text(encoding="utf-8")
    match = re.search(r"^Pattern version:\s*(.+?)\s*$", contract, re.MULTILINE)
    if not match:
        raise InstallError("pattern-contract.md does not define Pattern version")
    return match.group(1).strip()


def source_revision(root: Path) -> str:
    result = subprocess.run(
        ["git", "-C", str(root), "rev-parse", "--short=12", "HEAD"],
        check=False,
        capture_output=True,
        text=True,
    )
    if result.returncode != 0:
        return "unversioned"
    revision = result.stdout.strip()
    dirty = subprocess.run(
        ["git", "-C", str(root), "status", "--porcelain"],
        check=False,
        capture_output=True,
        text=True,
    )
    return f"{revision}-dirty" if dirty.stdout.strip() else revision


def load_manifest(path: Path) -> Dict[str, object]:
    if not path.exists():
        return {}
    try:
        payload = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as error:
        raise InstallError(f"cannot read existing manifest {path}: {error}") from error
    if not isinstance(payload, dict):
        raise InstallError(f"existing manifest must contain a JSON object: {path}")
    return payload


def render_template(path: Path, variables: Dict[str, str]) -> str:
    rendered = path.read_text(encoding="utf-8")
    for name, value in variables.items():
        rendered = rendered.replace("{{" + name + "}}", value)
    unresolved = re.findall(r"\{\{[A-Z_]+\}\}", rendered)
    if unresolved:
        raise InstallError(f"unresolved template variables in {path}: {unresolved}")
    return rendered.rstrip() + "\n"


def update_agents(existing: str, managed_block: str) -> str:
    starts = existing.count(MANAGED_START)
    ends = existing.count(MANAGED_END)
    if starts != ends or starts > 1:
        raise InstallError("AGENTS.md contains malformed Smart Domain managed markers")

    block = managed_block.strip()
    if starts == 1:
        start = existing.index(MANAGED_START)
        end = existing.index(MANAGED_END, start) + len(MANAGED_END)
        parts = [existing[:start].strip(), block, existing[end:].strip()]
        return "\n\n".join(part for part in parts if part) + "\n"

    if not existing.strip():
        return block + "\n"
    return existing.rstrip() + "\n\n" + block + "\n"


def validate_base_package(base_package: object) -> str:
    if not isinstance(base_package, str) or not JAVA_PACKAGE_PATTERN.fullmatch(base_package):
        raise InstallError("a valid --base-package is required for the first installation")
    return base_package


def validate_source_roots(source_roots: object) -> List[str]:
    if not isinstance(source_roots, list) or not source_roots:
        raise InstallError("at least one relative --source-root is required")
    validated: List[str] = []
    for source_root in source_roots:
        if not isinstance(source_root, str) or not source_root.strip():
            raise InstallError("source roots must be non-empty relative paths")
        candidate = Path(source_root)
        if candidate.is_absolute() or ".." in candidate.parts:
            raise InstallError(f"source root must stay inside the consumer repository: {source_root}")
        validated.append(candidate.as_posix().rstrip("/") or ".")
    return list(dict.fromkeys(validated))


def checker_config(base_package: str, source_roots: List[str]) -> Dict[str, object]:
    return {
        "schemaVersion": 1,
        "sourceRoots": source_roots,
        "basePackages": [base_package],
        "domainPackages": [f"{base_package}.domain"],
        "apiPackages": [f"{base_package}.api"],
        "persistencePackages": [
            f"{base_package}.persistent",
            f"{base_package}.persistence",
        ],
        "ignoredPathParts": ["build", "target", "generated", "node_modules", ".gradle"],
        "allowedServiceTypes": [],
    }


def install(
    target: Path,
    base_package: Optional[str] = None,
    source_roots: Optional[List[str]] = None,
    force: bool = False,
) -> Dict[str, object]:
    root = repository_root()
    target = target.expanduser().resolve()
    if target == root:
        raise InstallError("target must be a consumer repository, not the Smart Domain source")
    target.mkdir(parents=True, exist_ok=True)
    if not target.is_dir():
        raise InstallError(f"target is not a directory: {target}")

    source_skill = root / ".agents/skills/smart-domain-backend"
    source_agents = root / "templates/consumer/AGENTS.smart-domain.md"
    source_readme = root / "templates/consumer/README.smart-domain.md"
    source_checker = root / "tools/smart-domain-check.py"
    for required in [source_skill / "SKILL.md", source_agents, source_readme, source_checker]:
        if not required.is_file():
            raise InstallError(f"installation source is missing: {required}")

    style_dir = target / ".smart-domain"
    manifest_path = style_dir / "manifest.json"
    manifest = load_manifest(manifest_path)
    managed_install = manifest.get("installedBy") == MANIFEST_INSTALLER
    destination_skill = target / ".agents/skills/smart-domain-backend"
    destination_readme = style_dir / "README.md"
    destination_checker = style_dir / "check.py"
    destination_config = style_dir / "config.json"

    configuration_override = base_package is not None or source_roots is not None
    existing_checker_config: Dict[str, object] = {}
    if managed_install and destination_config.exists() and not configuration_override:
        try:
            loaded_config = json.loads(destination_config.read_text(encoding="utf-8"))
        except (OSError, json.JSONDecodeError) as error:
            raise InstallError(f"cannot read existing checker config {destination_config}: {error}") from error
        if isinstance(loaded_config, dict):
            existing_checker_config = loaded_config
    configured_bases = existing_checker_config.get("basePackages")
    configured_base = configured_bases[0] if isinstance(configured_bases, list) and configured_bases else None
    effective_base_package = validate_base_package(
        base_package or configured_base or manifest.get("basePackage")
    )
    effective_source_roots = validate_source_roots(
        source_roots
        if source_roots is not None
        else existing_checker_config.get("sourceRoots", manifest.get("sourceRoots", ["src/main/java"]))
    )
    managed_paths = [
        destination_skill,
        destination_readme,
        destination_checker,
        destination_config,
    ]
    conflicts = [path for path in managed_paths if path.exists()]
    if conflicts and not managed_install and not force:
        joined = ", ".join(str(path) for path in conflicts)
        raise InstallError(f"refusing to overwrite unmanaged paths: {joined}; rerun with --force")

    runtime_version = read_runtime_version(root)
    pattern_version = read_pattern_version(root)
    revision = source_revision(root)
    variables = {
        "BASE_PACKAGE": effective_base_package,
        "RUNTIME_VERSION": runtime_version,
        "PATTERN_VERSION": pattern_version,
        "SOURCE_REVISION": revision,
    }

    if destination_skill.exists():
        shutil.rmtree(destination_skill)
    destination_skill.parent.mkdir(parents=True, exist_ok=True)
    shutil.copytree(source_skill, destination_skill)

    agents_path = target / "AGENTS.md"
    existing_agents = agents_path.read_text(encoding="utf-8") if agents_path.exists() else ""
    managed_block = render_template(source_agents, variables)
    agents_path.write_text(update_agents(existing_agents, managed_block), encoding="utf-8")

    style_dir.mkdir(parents=True, exist_ok=True)
    destination_readme.write_text(render_template(source_readme, variables), encoding="utf-8")
    shutil.copy2(source_checker, destination_checker)
    if not destination_config.exists() or not managed_install or configuration_override or force:
        destination_config.write_text(
            json.dumps(checker_config(effective_base_package, effective_source_roots), indent=2) + "\n",
            encoding="utf-8",
        )

    installed_manifest: Dict[str, object] = {
        "schemaVersion": 1,
        "installedBy": MANIFEST_INSTALLER,
        "patternVersion": pattern_version,
        "runtimeVersion": runtime_version,
        "sourceRevision": revision,
        "basePackage": effective_base_package,
        "sourceRoots": effective_source_roots,
        "skill": ".agents/skills/smart-domain-backend",
        "checker": ".smart-domain/check.py",
    }
    manifest_path.write_text(
        json.dumps(installed_manifest, indent=2, sort_keys=True) + "\n",
        encoding="utf-8",
    )
    return installed_manifest


def parse_args(argv: list[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Install Smart Domain Pattern Contract, coding skill, and AGENTS instructions."
    )
    parser.add_argument("target", type=Path, help="consumer repository directory")
    parser.add_argument(
        "--base-package",
        help="Java base package; required on first install, reused from the manifest on updates",
    )
    parser.add_argument(
        "--source-root",
        action="append",
        dest="source_roots",
        help="Java source root relative to the target; repeat for multiple modules",
    )
    parser.add_argument(
        "--force",
        action="store_true",
        help="replace an existing unmanaged Smart Domain skill/style directory",
    )
    return parser.parse_args(argv)


def main(argv: list[str] | None = None) -> int:
    args = parse_args(sys.argv[1:] if argv is None else argv)
    try:
        result = install(
            args.target,
            base_package=args.base_package,
            source_roots=args.source_roots,
            force=args.force,
        )
    except InstallError as error:
        print(f"error: {error}", file=sys.stderr)
        return 2
    print(
        "Installed Smart Domain Pattern v{patternVersion} (runtime {runtimeVersion}) into {target}.".format(
            target=args.target.resolve(), **result
        )
    )
    print("Next: run python3 .smart-domain/check.py and ask the agent to use smart-domain-backend.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
