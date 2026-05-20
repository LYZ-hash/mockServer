package com.inspur.medical.repository;

import com.inspur.medical.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByPatientID(String patientID);
    List<Patient> findByPatientIDIn(Collection<String> patientIDs);
    List<Patient> findByPatientNameContaining(String patientName);
    Optional<Patient> findByIdNo(String idNo);
    Optional<Patient> findByMobile(String mobile);
    Optional<Patient> findByPatientCard(String patientCard);
}
