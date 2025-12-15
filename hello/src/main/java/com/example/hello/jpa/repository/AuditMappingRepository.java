package com.example.jpa.repository;

import com.example.jpa.entity.Audit;
import com.example.jpa.entity.AuditMapping;
import com.example.jpa.entity.AuditMappingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditMappingRepository extends JpaRepository<AuditMapping, AuditMappingId> {

}