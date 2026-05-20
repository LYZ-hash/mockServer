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
@Table(name = "inpatient_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InpatientRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false, length = 50)
    private String patientId;

    @Column(name = "medical_no", nullable = false, length = 50)
    private String medicalNo;

    @Column(name = "card_type", length = 20)
    private String cardType;

    @Column(name = "card_no", length = 50)
    private String cardNo;

    @Column(name = "adm_info_list", length = 500)
    private String admInfoList;

    @Column(name = "adm_id", nullable = false, length = 50)
    private String admId;

    @Column(name = "adm_date", length = 20)
    private String admDate;

    @Column(name = "adm_dept", length = 100)
    private String admDept;

    @Column(name = "bill_no", length = 50)
    private String billNo;

    @Column(name = "adm_reason", length = 100)
    private String admReason;

    @Column(name = "national_code", length = 20)
    private String nationalCode;
}
