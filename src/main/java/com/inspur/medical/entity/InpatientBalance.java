package com.inspur.medical.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "inpatient_balance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InpatientBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false, length = 50)
    private String patientId;

    @Column(name = "medical_no", nullable = false, length = 50)
    private String medicalNo;

    @Column(name = "total_amount", length = 20)
    private String totalAmount;

    @Column(name = "deposit_amount", length = 20)
    private String depositAmount;

    @Column(name = "deposit_balance", length = 20)
    private String depositBalance;
}
