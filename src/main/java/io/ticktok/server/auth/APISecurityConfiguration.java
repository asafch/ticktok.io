package io.ticktok.server.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
@Order(1)
public class APISecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final String authToken;

    public APISecurityConfiguration(@Value("${http.auth-token}") String authToken) {
        this.authToken = authToken;
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.
                antMatcher("/api/**").
                csrf().disable().
                sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).
                and().addFilter(new APIAuthFilter(authToken)).authorizeRequests().anyRequest().authenticated();
    }
}
