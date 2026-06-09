package com.tashfi.InventoryManagementSystem.core.config;

import com.tashfi.InventoryManagementSystem.core.enums.CreatedBy;
import com.tashfi.InventoryManagementSystem.core.enums.Gender;
import com.tashfi.InventoryManagementSystem.core.enums.ProductStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.PostgresDialect;

import java.util.List;

@Configuration
public class R2dbcConfig {

    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions() {
        return R2dbcCustomConversions.of(PostgresDialect.INSTANCE, List.of(
                new StringToGenderConverter(),
                new GenderToStringConverter(),
                new StringToCreatedByConverter(),
                new CreatedByToStringConverter(),
                new StringToProductStatusConverter(),
                new ProductStatusToStringConverter()
        ));
    }

    @ReadingConverter
    static class StringToGenderConverter implements Converter<String, Gender> {
        @Override
        public Gender convert(String source) {
            return Gender.valueOf(source.toUpperCase());
        }
    }

    @WritingConverter
    static class GenderToStringConverter implements Converter<Gender, String> {
        @Override
        public String convert(Gender source) {
            return source.name();
        }
    }

    @ReadingConverter
    static class StringToCreatedByConverter implements Converter<String, CreatedBy> {
        @Override
        public CreatedBy convert(String source) {
            return CreatedBy.valueOf(source.toUpperCase());
        }
    }

    @WritingConverter
    static class CreatedByToStringConverter implements Converter<CreatedBy, String> {
        @Override
        public String convert(CreatedBy source) {
            return source.name();
        }
    }

    @ReadingConverter
    static class StringToProductStatusConverter implements Converter<String, ProductStatus> {
        @Override
        public ProductStatus convert(String source) {
            return ProductStatus.valueOf(source.toUpperCase());
        }
    }

    @WritingConverter
    static class ProductStatusToStringConverter implements Converter<ProductStatus, String> {
        @Override
        public String convert(ProductStatus source) {
            return source.name();
        }
    }
}