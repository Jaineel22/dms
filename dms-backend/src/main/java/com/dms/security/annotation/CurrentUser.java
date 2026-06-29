package com.dms.security.annotation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds the currently authenticated {@link org.springframework.security.core.userdetails.UserDetails}
 * into a controller method parameter, removing the need to write
 * {@code @AuthenticationPrincipal UserDetails userDetails} everywhere.
 *
 * <p>Usage:</p>
 * <pre>
 *   public ResponseEntity&lt;?&gt; me(@CurrentUser UserDetails userDetails) { ... }
 * </pre>
 */
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
}