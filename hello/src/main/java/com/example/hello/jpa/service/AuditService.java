package com.example.hello.jpa.service;

import com.example.hello.jpa.entity.Audit;
import com.example.hello.jpa.entity.AuditMapping;
import com.example.hello.jpa.repository.AuditMappingRepository;
import com.example.hello.jpa.repository.AuditRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.LongStream;

@Service
@Slf4j
public class AuditService {

    private final AuditRepository auditRepository;
    private final AuditMappingRepository auditMappingRepository;
    private final EntityManager entityManager;

    public AuditService(AuditRepository auditRepository,
                        AuditMappingRepository auditMappingRepository, EntityManager entityManager) {
        this.auditRepository = auditRepository;
        this.auditMappingRepository = auditMappingRepository;
        this.entityManager = entityManager;
    }


    @Transactional
    public Long createAuditWithMappings(LocalDateTime start,
                                        LocalDateTime end,
                                        List<Long> transactionIds) {
        // Step 1: Save audit
        Audit entity = new Audit();
        entity.setEnd(LocalDateTime.now());
        entity.setStart(LocalDateTime.now());
        Audit audit = auditRepository.saveAndFlush(entity);
        Long auditId = audit.getId();

        log.debug("Created audit with ID: {}", auditId);
        auditMappingRepository.batchInsert(auditId, transactionIds);

        // Step 2: Create mappings
//        List<AuditMapping> mappings = transactionIds.stream()
//                .distinct()
//                .map(txId -> AuditMapping.builder()
//                        .auditId(auditId)
//                        .tid(txId)
//                        .createdTimestamp(LocalDateTime.now())
//                        .isNew(true)  // Explicitly mark as new
//                        .build())
//                .toList();
//
//        // Step 3: Save all - LET HIBERNATE BATCH!
//        // DON'T call flush() here!
//        auditMappingRepository.saveAll(mappings);
//
//        log.info("Created audit {} with {} mappings", auditId, mappings.size());
//        List<Long> txIds = LongStream.range(1000, 1100).boxed().toList();
//        Session session = entityManager.unwrap(Session.class);
//        try(var statelessSession = session)
//        LocalDateTime timestamp = LocalDateTime.now();
//        int batchSize = 50;
//        for (int i = 0; i < txIds.size(); i++) {
//            AuditMapping mapping = new AuditMapping();
//            mapping.setAuditId(auditId);
//            mapping.setTid(txIds.get(i));
//            mapping.setCreatedTimestamp(LocalDateTime.now());
//            entityManager.persist(mapping);
//
//            if ((i + 1) % batchSize == 0) {
//                log.debug("Flushing batch at {} entities", i + 1);
//                entityManager.flush();
//                entityManager.clear();
//            }
//        }
//
//        entityManager.flush();
//        entityManager.clear();


        return auditId;
    }
}
