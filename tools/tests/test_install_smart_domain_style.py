from __future__ import annotations

import importlib.util
import json
import tempfile
import unittest
from pathlib import Path

SCRIPT = Path(__file__).resolve().parents[1] / "install-smart-domain-style.py"
SPEC = importlib.util.spec_from_file_location("install_smart_domain_style", SCRIPT)
assert SPEC and SPEC.loader
INSTALLER = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(INSTALLER)


class InstallSmartDomainStyleTest(unittest.TestCase):
    def setUp(self) -> None:
        self.temporary = tempfile.TemporaryDirectory()
        self.target = Path(self.temporary.name) / "consumer"
        self.target.mkdir()

    def tearDown(self) -> None:
        self.temporary.cleanup()

    def test_installs_portable_skill_agents_contract_and_manifest(self) -> None:
        result = INSTALLER.install(self.target)

        skill = self.target / ".agents/skills/smart-domain-backend"
        self.assertTrue((skill / "SKILL.md").is_file())
        self.assertTrue((skill / "evals/evals.json").is_file())
        self.assertTrue((skill / "references/pattern-contract.md").is_file())
        self.assertTrue((skill / "references/accounting-reference.md").is_file())

        agents = (self.target / "AGENTS.md").read_text(encoding="utf-8")
        self.assertEqual(1, agents.count(INSTALLER.MANAGED_START))
        self.assertEqual(1, agents.count(INSTALLER.MANAGED_END))
        self.assertIn("HTTP resource", agents)
        self.assertIn("association matrix", agents)

        manifest = json.loads(
            (self.target / ".smart-domain/manifest.json").read_text(encoding="utf-8")
        )
        self.assertEqual("smart-domain-style", manifest["installedBy"])
        self.assertEqual(result["patternVersion"], manifest["patternVersion"])
        self.assertEqual(result["runtimeVersion"], manifest["runtimeVersion"])
        readme = (self.target / ".smart-domain/README.md").read_text(encoding="utf-8")
        self.assertNotIn("{{", readme)
        self.assertIn(manifest["runtimeVersion"], readme)

    def test_reinstall_is_idempotent_and_preserves_project_instructions(self) -> None:
        agents_path = self.target / "AGENTS.md"
        agents_path.write_text("# Project rules\n\nKeep this.\n", encoding="utf-8")

        INSTALLER.install(self.target)
        agents_path.write_text(
            agents_path.read_text(encoding="utf-8") + "\n## Local rule\n\nKeep this too.\n",
            encoding="utf-8",
        )
        INSTALLER.install(self.target)

        agents = agents_path.read_text(encoding="utf-8")
        self.assertEqual(1, agents.count(INSTALLER.MANAGED_START))
        self.assertIn("# Project rules", agents)
        self.assertIn("Keep this.", agents)
        self.assertIn("## Local rule", agents)
        self.assertIn("Keep this too.", agents)

    def test_refuses_to_overwrite_unmanaged_skill_without_force(self) -> None:
        skill = self.target / ".agents/skills/smart-domain-backend"
        skill.mkdir(parents=True)
        (skill / "SKILL.md").write_text("custom\n", encoding="utf-8")

        with self.assertRaisesRegex(INSTALLER.InstallError, "unmanaged paths"):
            INSTALLER.install(self.target)

        INSTALLER.install(self.target, force=True)
        self.assertNotEqual("custom\n", (skill / "SKILL.md").read_text(encoding="utf-8"))

    def test_rejects_malformed_agents_markers(self) -> None:
        (self.target / "AGENTS.md").write_text(
            INSTALLER.MANAGED_START + "\nmissing end\n", encoding="utf-8"
        )

        with self.assertRaisesRegex(INSTALLER.InstallError, "malformed"):
            INSTALLER.install(self.target)

    def test_rejects_installing_into_source_repository(self) -> None:
        with self.assertRaisesRegex(INSTALLER.InstallError, "consumer repository"):
            INSTALLER.install(INSTALLER.repository_root())


if __name__ == "__main__":
    unittest.main()
