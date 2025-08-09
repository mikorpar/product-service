package com.mkorpar.productservice.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.server.servlet.OAuth2AuthorizationServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@Profile("!noauth & !external-auth-server")
public class AuthorizationServerConfig {

    private static final String JTW_SIGNATURE_RSA_KEY_ID = "jtw-signature-rsa-key";
    private final OAuth2AuthorizationServerPropertiesMapper propertiesMapper;

    @Value("${jwt.signature.keystore}")
    private String jwtSignatureKeyStore;
    @Value("${jwt.signature.keystore.type}")
    private String jwtSignatureKeyStoreType;
    @Value("${jwt.signature.keystore.key.entry.name}")
    private String jwtSignatureKeyEntryName;
    @Value("${jwt.signature.keystore.key.entry.pwd}")
    private String jwtSignatureKeyEntryPwd;

    AuthorizationServerConfig(OAuth2AuthorizationServerProperties properties) {
        this.propertiesMapper = new OAuth2AuthorizationServerPropertiesMapper(properties);
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        JdbcRegisteredClientRepository registeredClientRepository = new JdbcRegisteredClientRepository(jdbcTemplate);
        propertiesMapper.asRegisteredClients().forEach(registeredClientRepository::save);
        return registeredClientRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                OAuth2AuthorizationServerConfigurer.authorizationServer();
        return http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, Customizer.withDefaults())
                .authorizeHttpRequests((authorize) ->
                        authorize.anyRequest().authenticated()
                ).build();
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return propertiesMapper.asAuthorizationServerSettings();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() throws IOException, KeyStoreException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException {
        RSAKey rsaKey = getRsaKey();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    private RSAKey getRsaKey() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        InputStream keyStoreInputStream = new ClassPathResource(jwtSignatureKeyStore).getInputStream();
        KeyStore keyStore = KeyStore.getInstance(jwtSignatureKeyStoreType);
        keyStore.load(keyStoreInputStream, jwtSignatureKeyEntryPwd.toCharArray());

        Key privateKey = keyStore.getKey(jwtSignatureKeyEntryName, jwtSignatureKeyEntryPwd.toCharArray());
        X509Certificate cert = (X509Certificate) keyStore.getCertificate(jwtSignatureKeyEntryName);

        return new RSAKey.Builder((RSAPublicKey) cert.getPublicKey())
                .privateKey((RSAPrivateKey) privateKey)
                .keyID(JTW_SIGNATURE_RSA_KEY_ID)
                .build();
    }

}
