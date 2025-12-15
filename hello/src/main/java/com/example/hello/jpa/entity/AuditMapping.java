package com.example.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "AUDIT_MAPPING")
@IdClass(AuditMappingId.class)
@Getter
@Setter
public class AuditMapping {

    @Id
    @Column(name = "AUDIT_ID")
    private Long auditId;

    @Id
    @Column(name = "TID")
    private Long tid;

    @Column(name = "CREATED_TIMESTAMP")
    private LocalDateTime createdTimestamp = LocalDateTime.now();

    public AuditMapping() {
    }

    public AuditMapping(Long auditId, Long tid) {
        this.auditId = auditId;
        this.tid = tid;
    }
}

