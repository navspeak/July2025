package com.navn.securitydemo.controller;

import com.navn.securitydemo.dto.DTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@RestController
public class TestController {

    @GetMapping("/")
    List<DTO> home(){
        return List.of(DTO.of("k1", "v1"), DTO.of("k2", "v2"));
    }

    @GetMapping("/secured")
    List<DTO> secured(){
        return List.of(DTO.of("k1", "secured"));
    }

    @PostMapping("/secured")
    DTO foo(DTO dto){
        return dto;
    }

    @GetMapping("/secured/scopes")
    public Collection<? extends GrantedAuthority> scopes(@AuthenticationPrincipal OAuth2User principal) {
        return principal.getAuthorities();
    }

    @GetMapping("/secured/scopes/OAuth2AuthenticationToken")
    public Collection<GrantedAuthority> home(OAuth2AuthenticationToken authentication) {
        return authentication.getAuthorities();}
}
