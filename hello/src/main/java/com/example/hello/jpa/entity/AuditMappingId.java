package com.example.hello.jpa.entity;

import java.io.Serializable;

public record AuditMappingId(Long auditId, Long tid) implements Serializable {}