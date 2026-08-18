from __future__ import annotations

import importlib.util
import json
import sys
import tempfile
import unittest
from pathlib import Path

SCRIPT = Path(__file__).resolve().parents[1] / "smart-domain-check.py"
SPEC = importlib.util.spec_from_file_location("smart_domain_check", SCRIPT)
assert SPEC and SPEC.loader
CHECKER = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = CHECKER
SPEC.loader.exec_module(CHECKER)


class SmartDomainCheckTest(unittest.TestCase):
    def setUp(self) -> None:
        self.temporary = tempfile.TemporaryDirectory()
        self.repository = Path(self.temporary.name) / "consumer"
        self.repository.mkdir()
        style = self.repository / ".smart-domain"
        style.mkdir()
        self.config = style / "config.json"
        self.config.write_text(
            json.dumps(
                {
                    "schemaVersion": 1,
                    "sourceRoots": ["src/main/java"],
                    "basePackages": ["com.acme.shop"],
                    "domainPackages": ["com.acme.shop.domain"],
                    "apiPackages": ["com.acme.shop.api"],
                    "persistencePackages": [
                        "com.acme.shop.persistent",
                        "com.acme.shop.persistence",
                    ],
                    "ignoredPathParts": ["build", "target", "generated"],
                }
            ),
            encoding="utf-8",
        )

    def tearDown(self) -> None:
        self.temporary.cleanup()

    def write_java(self, relative: str, source: str) -> None:
        path = self.repository / "src/main/java" / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(source.strip() + "\n", encoding="utf-8")

    def findings(self) -> list:
        return CHECKER.run_checks(self.config)

    def test_accepts_connected_no_service_shape(self) -> None:
        self.write_java(
            "com/acme/shop/domain/Order.java",
            """
            package com.acme.shop.domain;
            import io.github.jayclock.smartdomain.core.Entity;
            import io.github.jayclock.smartdomain.core.HasMany;
            // class PricingService must not be interpreted as source.
            public final class Order implements Entity<String, OrderDescription> {
              private Lines lines;
              public HasMany<String, Line> lines() { return lines; }
              public interface Lines extends HasMany<String, Line> { Line add(String sku); }
            }
            """,
        )
        self.write_java(
            "com/acme/shop/domain/Line.java",
            """
            package com.acme.shop.domain;
            import io.github.jayclock.smartdomain.core.Entity;
            public final class Line implements Entity<String, LineDescription> {}
            """,
        )
        self.write_java(
            "com/acme/shop/persistent/OrderLines.java",
            """
            package com.acme.shop.persistent;
            import com.acme.shop.domain.Order;
            public final class OrderLines implements Order.Lines {}
            """,
        )
        self.write_java(
            "com/acme/shop/api/OrderResource.java",
            """
            package com.acme.shop.api;
            import com.acme.shop.domain.Orders;
            public final class OrderResource { private final Orders orders; }
            """,
        )

        self.assertEqual([], self.findings())

    def test_rejects_application_packages_and_business_middlemen(self) -> None:
        self.write_java(
            "com/acme/shop/application/CheckoutUseCase.java",
            """
            package com.acme.shop.application;
            public final class CheckoutUseCase {}
            """,
        )
        self.write_java(
            "com/acme/shop/domain/PricingService.java",
            """
            package com.acme.shop.domain;
            public final class PricingService {}
            """,
        )
        self.write_java(
            "com/acme/shop/infrastructure/EmailService.java",
            """
            package com.acme.shop.infrastructure;
            public final class EmailService {}
            """,
        )

        findings = self.findings()
        self.assertGreaterEqual(sum(item.rule == "SD001" for item in findings), 3)
        self.assertFalse(any("EmailService" in item.message for item in findings))

    def test_allows_explicit_external_service_contract(self) -> None:
        config = json.loads(self.config.read_text(encoding="utf-8"))
        config["allowedServiceTypes"] = ["com.acme.shop.domain.PaymentService"]
        self.config.write_text(json.dumps(config), encoding="utf-8")
        self.write_java(
            "com/acme/shop/domain/PaymentService.java",
            """
            package com.acme.shop.domain;
            public interface PaymentService {}
            """,
        )

        self.assertFalse(any(item.rule == "SD001" for item in self.findings()))

    def test_rejects_inverted_layer_imports(self) -> None:
        self.write_java(
            "com/acme/shop/domain/Order.java",
            """
            package com.acme.shop.domain;
            import org.springframework.stereotype.Component;
            import com.acme.shop.persistent.OrderMapper;
            public final class Order {}
            """,
        )
        self.write_java(
            "com/acme/shop/api/OrderResource.java",
            """
            package com.acme.shop.api;
            import com.acme.shop.persistent.OrderMapper;
            public final class OrderResource {}
            """,
        )
        self.write_java(
            "com/acme/shop/persistent/OrderRows.java",
            """
            package com.acme.shop.persistent;
            import com.acme.shop.api.OrderResource;
            public final class OrderRows {}
            """,
        )

        rules = {item.rule for item in self.findings()}
        self.assertTrue({"SD002", "SD003", "SD004"}.issubset(rules))

    def test_rejects_raw_entity_collections(self) -> None:
        self.write_java(
            "com/acme/shop/domain/Order.java",
            """
            package com.acme.shop.domain;
            import java.util.List;
            import io.github.jayclock.smartdomain.core.Entity;
            public final class Order implements Entity<String, OrderDescription> {
              private List<Line> lines;
            }
            """,
        )
        self.write_java(
            "com/acme/shop/domain/Line.java",
            """
            package com.acme.shop.domain;
            import io.github.jayclock.smartdomain.core.Entity;
            public final class Line implements Entity<String, LineDescription> {}
            """,
        )

        self.assertIn("SD005", {item.rule for item in self.findings()})

    def test_rejects_public_wide_association_accessors(self) -> None:
        self.write_java(
            "com/acme/shop/domain/Order.java",
            """
            package com.acme.shop.domain;
            import io.github.jayclock.smartdomain.core.HasMany;
            public final class Order {
              private Lines lines;
              public Lines lines() { return lines; }
              public interface Lines extends HasMany<String, Line> { Line add(String sku); }
            }
            """,
        )

        self.assertIn("SD006", {item.rule for item in self.findings()})

    def test_missing_source_root_is_a_warning(self) -> None:
        findings = self.findings()
        self.assertTrue(findings)
        self.assertTrue(all(item.severity == "warning" for item in findings))
        self.assertEqual({"SD000"}, {item.rule for item in findings})


if __name__ == "__main__":
    unittest.main()
