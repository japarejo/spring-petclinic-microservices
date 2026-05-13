package org.springframework.samples.aclmicroservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.aclmicroservice.model.ClinicalRecord;

public interface ClinicalRecordRepository extends JpaRepository<ClinicalRecord, Long> {
}
