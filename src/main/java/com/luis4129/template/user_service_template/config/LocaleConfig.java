package com.luis4129.template.user_service_template.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

/**
 * This is a stateless REST API, so locale is negotiated per-request via the
 * standard {@code Accept-Language} header rather than a session/cookie - e.g.
 * {@code Accept-Language: pt-BR}. Requests with no header, or a header outside
 * the supported set, fall back to {@link #DEFAULT_LOCALE}.
 */
@Configuration
public class LocaleConfig {

    public static final Locale DEFAULT_LOCALE = Locale.of("en", "US");
    public static final Locale PT_BR = Locale.of("pt", "BR");

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(DEFAULT_LOCALE);
        resolver.setSupportedLocales(List.of(DEFAULT_LOCALE, PT_BR));
        return resolver;
    }
}
