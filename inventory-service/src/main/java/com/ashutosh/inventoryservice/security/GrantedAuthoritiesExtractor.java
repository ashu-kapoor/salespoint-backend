package com.ashutosh.inventoryservice.security;

import com.nimbusds.jose.shaded.gson.internal.LinkedTreeMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

@Slf4j
class GrantedAuthoritiesExtractor implements Converter<Jwt, Collection<GrantedAuthority>> {

  @SuppressWarnings("unchecked")
  public Collection<GrantedAuthority> convert(Jwt jwt) {
    try {
      LinkedTreeMap<String, ArrayList<String>> authorities =
          (LinkedTreeMap<String, ArrayList<String>>) jwt.getClaims().get("realm_access");

      ArrayList<String> role = authorities.get("roles");

      return role.stream()
          .map(SimpleGrantedAuthority::new)
          .peek(z -> log.info("Extracted authority for user {}", z.getAuthority()))
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("Exception occurred parsing the token {}", e.getMessage());
    }
    return List.of(new SimpleGrantedAuthority("NO_AUTH"));
  }
}
