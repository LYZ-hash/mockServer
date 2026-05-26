package com.inspur.medical.repository;

import com.inspur.medical.entity.InpatientRechargeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InpatientRechargeRecordRepository extends JpaRepository<InpatientRechargeRecord, Long> {
    Optional<InpatientRechargeRecord> findByPlatformNo(String platformNo);
}
