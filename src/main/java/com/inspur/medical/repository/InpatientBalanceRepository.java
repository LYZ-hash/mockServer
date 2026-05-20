package com.inspur.medical.repository;

import com.inspur.medical.entity.InpatientBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface InpatientBalanceRepository extends JpaRepository<InpatientBalance, Long> {
    List<InpatientBalance> findByMedicalNoIn(Collection<String> medicalNos);
}
