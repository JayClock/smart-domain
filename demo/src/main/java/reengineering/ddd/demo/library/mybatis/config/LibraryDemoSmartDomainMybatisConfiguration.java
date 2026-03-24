package reengineering.ddd.demo.library.mybatis.config;

import io.github.jayclock.smartdomain.boot.EnableSmartDomainMybatis;
import org.springframework.context.annotation.Configuration;
import reengineering.ddd.demo.library.model.Book;

@Configuration
@EnableSmartDomainMybatis(
    associationBasePackages = "reengineering.ddd.demo.library.mybatis",
    leafEntityTypes = {Book.class})
public class LibraryDemoSmartDomainMybatisConfiguration {}
