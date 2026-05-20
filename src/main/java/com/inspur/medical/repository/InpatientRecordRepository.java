package com.inspur.medical.repository;

import com.inspur.medical.entity.InpatientRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InpatientRecordRepository extends JpaRepository<InpatientRecord, Long> {

    @Query("SELECT r FROM InpatientRecord r WHERE " +
           "(:patientId IS NULL OR :patientId = '' OR r.patientId = :patientId) AND " +
           "(:medicalNo IS NULL OR :medicalNo = '' OR r.medicalNo = :medicalNo) AND " +
           "(:cardNo IS NULL OR :cardNo = '' OR r.cardNo = :cardNo) AND " +
           "(:cardType IS NULL OR :cardType = '' OR r.cardType = :cardType) " +
           "ORDER BY r.admDate DESC, r.admId DESC")
    List<InpatientRecord> search(@Param("patientId") String patientId,
                                 @Param("medicalNo") String medicalNo,
                                 @Param("cardNo") String cardNo,
                                 @Param("cardType") String cardType);
}
