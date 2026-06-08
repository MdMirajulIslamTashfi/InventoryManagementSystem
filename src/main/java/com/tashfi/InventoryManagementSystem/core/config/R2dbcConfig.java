package com.tashfi.InventoryManagementSystem.core.config;

import com.tashfi.InventoryManagementSystem.core.enums.Gender;
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
                new GenderToStringConverter()
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
}