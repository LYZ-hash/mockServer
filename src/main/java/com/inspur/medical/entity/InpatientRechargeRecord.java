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
@Table(name = "inpatient_recharge_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InpatientRechargeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serial_no", nullable = false, length = 50)
    private String serialNo;

    @Column(name = "patient_id", nullable = false, length = 50)
    private String patientId;

    @Column(name = "medical_no", nullable = false, length = 50)
    private String medicalNo;

    @Column(name = "card_no", nullable = false, length = 50)
    private String cardNo;

    @Column(name = "card_type", nullable = false, length = 20)
    private String cardType;

    @Column(name = "pay_details", length = 1000)
    private String payDetails;

    @Column(name = "pay_mode_code", nullable = false, length = 50)
    private String payModeCode;

    @Column(name = "pay_account_no", length = 100)
    private String payAccountNo;

    @Column(name = "pay_amt", nullable = false, length = 20)
    private String payAmt;

    @Column(name = "platform_no", nullable = false, length = 100)
    private String platformNo;

    @Column(name = "out_pay_no", length = 100)
    private String outPayNo;

    @Column(name = "pay_channel", length = 50)
    private String payChannel;

    @Column(name = "pos_pay_str", length = 1000)
    private String posPayStr;

    @Column(name = "pay_date", nullable = false, length = 20)
    private String payDate;

    @Column(name = "pay_time", nullable = false, length = 20)
    private String payTime;

    @Column(name = "before_deposit_amount", nullable = false, length = 20)
    private String beforeDepositAmount;

    @Column(name = "before_deposit_balance", nullable = false, length = 20)
    private String beforeDepositBalance;

    @Column(name = "after_deposit_amount", nullable = false, length = 20)
    private String afterDepositAmount;

    @Column(name = "after_deposit_balance", nullable = false, length = 20)
    private String afterDepositBalance;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "result_content", length = 200)
    private String resultContent;
}
