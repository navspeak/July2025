package com.example.hello.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

@Entity
@Table(name = "AUDIT_MAPPING")
@IdClass(AuditMappingId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditMapping implements Persistable<AuditMappingId> {

    @Id
    @Column(name = "AUDIT_ID")
    private Long auditId;

    @Id
    @Column(name = "TID", nullable = false)
    private Long tid;

    @Column(name = "CREATED_TIMESTAMP", nullable = false)
    private LocalDateTime createdTimestamp;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public AuditMappingId getId() {
        return new AuditMappingId(auditId, tid);
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}