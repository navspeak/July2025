package com.example.jpa.entity;

import java.io.Serializable;

public record AuditMappingId(Long auditId, Long tid) implements Serializable {}