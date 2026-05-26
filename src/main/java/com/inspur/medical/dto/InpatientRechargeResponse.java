package com.inspur.medical.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InpatientRechargeResponse {

    @JsonProperty("ResultCode")
    private String resultCode;

    @JsonProperty("ResultContent")
    private String resultContent;

    @JsonProperty("SerialNo")
    private String serialNo;

    @JsonProperty("DepositAmount")
    private String depositAmount;

    @JsonProperty("DepositBalance")
    private String depositBalance;

    public static InpatientRechargeResponse success(String serialNo, String depositAmount, String depositBalance) {
        return new InpatientRechargeResponse("1", "Recharge succeeded", serialNo, depositAmount, depositBalance);
    }

    public static InpatientRechargeResponse error(String message) {
        return new InpatientRechargeResponse("0", message, null, null, null);
    }
}
