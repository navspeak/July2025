package com.navn.securitydemo.dto;

public record DTO(String key, String value) {
    public static DTO of(String key, String value) {
        return new DTO(key, value);
    }
}
