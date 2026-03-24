package reengineering.ddd.demo.ecommerce.mybatis.config;

import io.github.jayclock.smartdomain.boot.EnableSmartDomainMybatis;
import org.springframework.context.annotation.Configuration;
import reengineering.ddd.demo.ecommerce.model.Listing;

@Configuration
@EnableSmartDomainMybatis(
    associationBasePackages = "reengineering.ddd.demo.ecommerce.mybatis",
    leafEntityTypes = {Listing.class})
public class EcommerceDemoSmartDomainMybatisConfiguration {}
