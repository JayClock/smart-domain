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
from typing import Dict

MANAGED_START = "<!-- smart-domain-style:start -->"
MANAGED_END = "<!-- smart-domain-style:end -->"
MANIFEST_INSTALLER = "smart-domain-style"


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


def install(target: Path, force: bool = False) -> Dict[str, str]:
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
    for required in [source_skill / "SKILL.md", source_agents, source_readme]:
        if not required.is_file():
            raise InstallError(f"installation source is missing: {required}")

    style_dir = target / ".smart-domain"
    manifest_path = style_dir / "manifest.json"
    manifest = load_manifest(manifest_path)
    managed_install = manifest.get("installedBy") == MANIFEST_INSTALLER
    destination_skill = target / ".agents/skills/smart-domain-backend"
    destination_readme = style_dir / "README.md"

    conflicts = [path for path in [destination_skill, destination_readme] if path.exists()]
    if conflicts and not managed_install and not force:
        joined = ", ".join(str(path) for path in conflicts)
        raise InstallError(f"refusing to overwrite unmanaged paths: {joined}; rerun with --force")

    runtime_version = read_runtime_version(root)
    pattern_version = read_pattern_version(root)
    revision = source_revision(root)
    variables = {
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
    installed_manifest = {
        "schemaVersion": 1,
        "installedBy": MANIFEST_INSTALLER,
        "patternVersion": pattern_version,
        "runtimeVersion": runtime_version,
        "sourceRevision": revision,
        "skill": ".agents/skills/smart-domain-backend",
    }
    manifest_path.write_text(
        json.dumps(installed_manifest, indent=2, sort_keys=True) + "\n",
        encoding="utf-8",
    )
    return {key: str(value) for key, value in installed_manifest.items()}


def parse_args(argv: list[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Install Smart Domain Pattern Contract, coding skill, and AGENTS instructions."
    )
    parser.add_argument("target", type=Path, help="consumer repository directory")
    parser.add_argument(
        "--force",
        action="store_true",
        help="replace an existing unmanaged Smart Domain skill/style directory",
    )
    return parser.parse_args(argv)


def main(argv: list[str] | None = None) -> int:
    args = parse_args(sys.argv[1:] if argv is None else argv)
    try:
        result = install(args.target, force=args.force)
    except InstallError as error:
        print(f"error: {error}", file=sys.stderr)
        return 2
    print(
        "Installed Smart Domain Pattern v{patternVersion} (runtime {runtimeVersion}) into {target}.".format(
            target=args.target.resolve(), **result
        )
    )
    print("Next: read .smart-domain/README.md and ask the agent to use smart-domain-backend.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
