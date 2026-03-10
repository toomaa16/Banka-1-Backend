package com.company.observability.starter.service;

import com.company.observability.starter.domain.UserIdExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

/**
 * Implementacija {@link UserIdExtractor} interfejsa koja pokusava da izdvoji
 * user ID iz Spring Security autentikacionog konteksta.
 * <p>
 * User ID se pokusava pronaci u principal objektu, details objektu ili
 * kroz naziv autentikovanog korisnika.
 */
public class JwtAuthenticationUserIdExtractor implements UserIdExtractor {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationUserIdExtractor.class);

    /**
     * Pokusava da izdvoji user ID iz trenutno autentikovanog korisnika.
     *
     * @return {@link Optional} sa user ID vrednoscu ako je pronadjena,
     *         inace prazan {@link Optional}
     */
    @Override
    public Optional<String> extractUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()){
            return Optional.empty();
        }
        return extractFromPrincipal(authentication.getPrincipal())
                .or(() -> extractFromObject(authentication.getDetails()))
                .or(() -> StringUtils.hasText(authentication.getName()) ? Optional.of(authentication.getName()) : Optional.empty());
    }

    private Optional<String> extractFromPrincipal(Object principal) {
        if (principal == null) {
            return Optional.empty();
        }
        return extractFromObject(principal)
                .or(() -> {
                    if (principal instanceof Principal p && StringUtils.hasText(p.getName())) {
                        return Optional.of(p.getName());
                    }
                    return Optional.empty();
                });
    }

    private Optional<String> extractFromObject(Object source) {
        if (source == null) {
            return Optional.empty();
        }
        return extractClaim(source, "userId")
                .or(() -> extractClaim(source, "user_id"))
                .or(() -> extractClaim(source, "uid"))
                .or(() -> extractClaim(source, "sub"))
                .or(() -> extractClaim(source, "preferred_username"));
    }

    private Optional<String> extractClaim(Object source, String claimName) {
        if (source instanceof Map<?, ?> map) {
            Object value = map.get(claimName);
            if (value instanceof String s && StringUtils.hasText(s)) {
                return Optional.of(s);
            }
        }

        try {
            Method getClaimAsString = source.getClass().getMethod("getClaimAsString", String.class);
            Object value = getClaimAsString.invoke(source, claimName);
            if (value instanceof String s && StringUtils.hasText(s)) {
                return Optional.of(s);
            }
        } catch (Exception e) {
            log.trace("Could not extract claim '{}' via getClaimAsString from {}: {}", claimName, source.getClass().getSimpleName(), e.getMessage());
        }

        try {
            Method getClaims = source.getClass().getMethod("getClaims");
            Object claims = getClaims.invoke(source);
            if (claims instanceof Map<?, ?> map) {
                Object value = map.get(claimName);
                if (value instanceof String s && StringUtils.hasText(s)) {
                    return Optional.of(s);
                }
            }
        } catch (Exception e) {
            log.trace(
                    "Could not extract claim '{}' via getClaims from {}: {}",
                    claimName,
                    source.getClass().getSimpleName(),
                    e.getMessage()
            );
        }

        return Optional.empty();
    }
}
