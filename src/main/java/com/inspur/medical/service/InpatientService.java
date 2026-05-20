package com.inspur.medical.service;

import com.inspur.medical.dto.InpatientDTO;
import com.inspur.medical.dto.InpatientQueryDTO;
import com.inspur.medical.dto.InpatientResponse;
import com.inspur.medical.entity.InpatientBalance;
import com.inspur.medical.entity.InpatientRecord;
import com.inspur.medical.entity.Patient;
import com.inspur.medical.repository.InpatientBalanceRepository;
import com.inspur.medical.repository.InpatientRecordRepository;
import com.inspur.medical.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    public InpatientResponse searchInpatient(InpatientQueryDTO queryDTO) {
        if (queryDTO == null || isAllBlank(queryDTO.getPatientID(), queryDTO.getMedicalNo(), queryDTO.getCardNo())) {
            return InpatientResponse.error("PatientID、MedicalNo、CardNo 不能同时为空");
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

    private boolean isAllBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
