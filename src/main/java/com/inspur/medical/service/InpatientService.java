package com.inspur.medical.service;

import com.inspur.medical.dto.InpatientDTO;
import com.inspur.medical.dto.InpatientQueryDTO;
import com.inspur.medical.dto.InpatientRechargeDTO;
import com.inspur.medical.dto.InpatientRechargeResponse;
import com.inspur.medical.dto.InpatientResponse;
import com.inspur.medical.entity.InpatientBalance;
import com.inspur.medical.entity.InpatientRechargeRecord;
import com.inspur.medical.entity.InpatientRecord;
import com.inspur.medical.entity.Patient;
import com.inspur.medical.repository.InpatientBalanceRepository;
import com.inspur.medical.repository.InpatientRechargeRecordRepository;
import com.inspur.medical.repository.InpatientRecordRepository;
import com.inspur.medical.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class InpatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private InpatientRecordRepository inpatientRecordRepository;

    @Autowired
    private InpatientBalanceRepository inpatientBalanceRepository;

    @Autowired
    private InpatientRechargeRecordRepository inpatientRechargeRecordRepository;

    public InpatientResponse searchInpatient(InpatientQueryDTO queryDTO) {
        if (queryDTO == null || isAllBlank(queryDTO.getPatientID(), queryDTO.getMedicalNo(), queryDTO.getCardNo())) {
            return InpatientResponse.error("PatientID\u3001MedicalNo\u3001CardNo \u4e0d\u80fd\u540c\u65f6\u4e3a\u7a7a");
        }

        List<InpatientRecord> records = inpatientRecordRepository.search(
                queryDTO.getPatientID(),
                queryDTO.getMedicalNo(),
                queryDTO.getCardNo(),
                queryDTO.getCardType()
        );

        if (records.isEmpty()) {
            return InpatientResponse.success(Collections.<InpatientDTO>emptyList());
        }

        Map<String, Patient> patientMap = patientRepository.findByPatientIDIn(
                records.stream()
                        .map(InpatientRecord::getPatientId)
                        .distinct()
                        .collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(Patient::getPatientID, Function.identity()));

        Map<String, InpatientBalance> balanceMap = inpatientBalanceRepository.findByMedicalNoIn(
                records.stream()
                        .map(InpatientRecord::getMedicalNo)
                        .collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(InpatientBalance::getMedicalNo, Function.identity()));

        List<InpatientDTO> results = new ArrayList<>();
        for (InpatientRecord record : records) {
            Patient patient = patientMap.get(record.getPatientId());
            if (patient == null) {
                continue;
            }

            InpatientBalance balance = balanceMap.get(record.getMedicalNo());
            results.add(new InpatientDTO(
                    patient.getPatientID(),
                    patient.getPatientName(),
                    patient.getSex(),
                    patient.getDob(),
                    record.getMedicalNo(),
                    record.getCardType(),
                    record.getCardNo(),
                    record.getAdmInfoList(),
                    record.getAdmId(),
                    record.getAdmDate(),
                    record.getAdmDept(),
                    record.getBillNo(),
                    record.getAdmReason(),
                    record.getNationalCode(),
                    balance != null ? balance.getTotalAmount() : "0.00",
                    balance != null ? balance.getDepositAmount() : "0.00",
                    balance != null ? balance.getDepositBalance() : "0.00"
            ));
        }

        return InpatientResponse.success(results);
    }

    @Transactional
    public InpatientRechargeResponse rechargeInpatient(InpatientRechargeDTO rechargeDTO) {
        String missingField = validateRechargeRequest(rechargeDTO);
        if (missingField != null) {
            return InpatientRechargeResponse.error("Missing required field: " + missingField);
        }

        BigDecimal payAmt;
        try {
            payAmt = parsePositiveAmount(rechargeDTO.getPayAmt());
        } catch (IllegalArgumentException ex) {
            return InpatientRechargeResponse.error(ex.getMessage());
        }

        String platformNo = normalize(rechargeDTO.getPlatformNo());
        InpatientRechargeRecord existingRecord = inpatientRechargeRecordRepository.findByPlatformNo(platformNo).orElse(null);
        if (existingRecord != null) {
            if (!isSameRechargeRequest(existingRecord, rechargeDTO, payAmt)) {
                return InpatientRechargeResponse.error("PlatformNo already exists with different request data");
            }
            return InpatientRechargeResponse.success(
                    existingRecord.getSerialNo(),
                    existingRecord.getAfterDepositAmount(),
                    existingRecord.getAfterDepositBalance()
            );
        }

        String patientId = normalize(rechargeDTO.getPatientID());
        String medicalNo = normalize(rechargeDTO.getMedicalNo());
        InpatientRecord inpatientRecord = inpatientRecordRepository
                .findByPatientIdAndMedicalNo(patientId, medicalNo)
                .orElse(null);
        if (inpatientRecord == null) {
            return InpatientRechargeResponse.error("Inpatient record not found");
        }

        if (!equalsNormalized(inpatientRecord.getCardNo(), rechargeDTO.getCardNo())
                || !equalsNormalized(inpatientRecord.getCardType(), rechargeDTO.getCardType())) {
            return InpatientRechargeResponse.error("CardNo or CardType does not match inpatient record");
        }

        InpatientBalance inpatientBalance = inpatientBalanceRepository
                .findByPatientIdAndMedicalNo(patientId, medicalNo)
                .orElse(null);
        if (inpatientBalance == null) {
            return InpatientRechargeResponse.error("Inpatient balance not found");
        }

        BigDecimal beforeDepositAmount = parseStoredAmount(inpatientBalance.getDepositAmount());
        BigDecimal beforeDepositBalance = parseStoredAmount(inpatientBalance.getDepositBalance());
        BigDecimal afterDepositAmount = beforeDepositAmount.add(payAmt);
        BigDecimal afterDepositBalance = beforeDepositBalance.add(payAmt);

        InpatientRechargeRecord rechargeRecord = new InpatientRechargeRecord();
        rechargeRecord.setSerialNo(generateRechargeSerialNo());
        rechargeRecord.setPatientId(patientId);
        rechargeRecord.setMedicalNo(medicalNo);
        rechargeRecord.setCardNo(normalize(rechargeDTO.getCardNo()));
        rechargeRecord.setCardType(normalize(rechargeDTO.getCardType()));
        rechargeRecord.setPayDetails(normalize(rechargeDTO.getPayDetails()));
        rechargeRecord.setPayModeCode(normalize(rechargeDTO.getPayModeCode()));
        rechargeRecord.setPayAccountNo(normalize(rechargeDTO.getPayAccountNo()));
        rechargeRecord.setPayAmt(formatAmount(payAmt));
        rechargeRecord.setPlatformNo(platformNo);
        rechargeRecord.setOutPayNo(normalize(rechargeDTO.getOutPayNo()));
        rechargeRecord.setPayChannel(normalize(rechargeDTO.getPayChannel()));
        rechargeRecord.setPosPayStr(normalize(rechargeDTO.getPosPayStr()));
        rechargeRecord.setPayDate(normalize(rechargeDTO.getPayDate()));
        rechargeRecord.setPayTime(normalize(rechargeDTO.getPayTime()));
        rechargeRecord.setBeforeDepositAmount(formatAmount(beforeDepositAmount));
        rechargeRecord.setBeforeDepositBalance(formatAmount(beforeDepositBalance));
        rechargeRecord.setAfterDepositAmount(formatAmount(afterDepositAmount));
        rechargeRecord.setAfterDepositBalance(formatAmount(afterDepositBalance));
        rechargeRecord.setStatus("SUCCESS");
        rechargeRecord.setResultContent("Recharge succeeded");
        inpatientRechargeRecordRepository.save(rechargeRecord);

        inpatientBalance.setDepositAmount(formatAmount(afterDepositAmount));
        inpatientBalance.setDepositBalance(formatAmount(afterDepositBalance));
        inpatientBalanceRepository.save(inpatientBalance);

        return InpatientRechargeResponse.success(
                rechargeRecord.getSerialNo(),
                rechargeRecord.getAfterDepositAmount(),
                rechargeRecord.getAfterDepositBalance()
        );
    }

    private String validateRechargeRequest(InpatientRechargeDTO rechargeDTO) {
        if (rechargeDTO == null) {
            return "requestBody";
        }
        if (isBlank(rechargeDTO.getPatientID())) {
            return "PatientID";
        }
        if (isBlank(rechargeDTO.getMedicalNo())) {
            return "MedicalNo";
        }
        if (isBlank(rechargeDTO.getCardNo())) {
            return "CardNo";
        }
        if (isBlank(rechargeDTO.getCardType())) {
            return "CardType";
        }
        if (isBlank(rechargeDTO.getPayAmt())) {
            return "PayAmt";
        }
        if (isBlank(rechargeDTO.getPlatformNo())) {
            return "PlatformNo";
        }
        if (isBlank(rechargeDTO.getPayModeCode())) {
            return "PayModeCode";
        }
        if (isBlank(rechargeDTO.getPayDate())) {
            return "PayDate";
        }
        if (isBlank(rechargeDTO.getPayTime())) {
            return "PayTime";
        }
        return null;
    }

    private BigDecimal parsePositiveAmount(String value) {
        try {
            BigDecimal amount = new BigDecimal(value.trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("PayAmt must be greater than 0");
            }
            return amount.setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("PayAmt is invalid");
        }
    }

    private BigDecimal parseStoredAmount(String value) {
        if (isBlank(value)) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        try {
            return new BigDecimal(value.trim()).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
    }

    private String formatAmount(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String generateRechargeSerialNo() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "IPR" + System.currentTimeMillis() + suffix;
    }

    private boolean isSameRechargeRequest(InpatientRechargeRecord record, InpatientRechargeDTO rechargeDTO, BigDecimal payAmt) {
        return equalsNormalized(record.getPatientId(), rechargeDTO.getPatientID())
                && equalsNormalized(record.getMedicalNo(), rechargeDTO.getMedicalNo())
                && equalsNormalized(record.getCardNo(), rechargeDTO.getCardNo())
                && equalsNormalized(record.getCardType(), rechargeDTO.getCardType())
                && equalsNormalized(record.getPayDetails(), rechargeDTO.getPayDetails())
                && equalsNormalized(record.getPayModeCode(), rechargeDTO.getPayModeCode())
                && equalsNormalized(record.getPayAccountNo(), rechargeDTO.getPayAccountNo())
                && equalsNormalized(record.getPayAmt(), formatAmount(payAmt))
                && equalsNormalized(record.getPlatformNo(), rechargeDTO.getPlatformNo())
                && equalsNormalized(record.getOutPayNo(), rechargeDTO.getOutPayNo())
                && equalsNormalized(record.getPayChannel(), rechargeDTO.getPayChannel())
                && equalsNormalized(record.getPosPayStr(), rechargeDTO.getPosPayStr())
                && equalsNormalized(record.getPayDate(), rechargeDTO.getPayDate())
                && equalsNormalized(record.getPayTime(), rechargeDTO.getPayTime());
    }

    private boolean isAllBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return false;
            }
        }
        return true;
    }

    private boolean isBlank(String value) {
        return normalize(value) == null;
    }

    private boolean equalsNormalized(String left, String right) {
        return Objects.equals(normalize(left), normalize(right));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
