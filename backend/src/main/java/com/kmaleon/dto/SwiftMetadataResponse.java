package com.kmaleon.dto;

public class SwiftMetadataResponse {

    private String messageId;
    private String uetr;
    private String settlementDate;
    private String debtorBank;
    private String debtorBic;
    private String debtorAccount;
    private String creditorBank;
    private String creditorBic;
    private String creditorName;
    private String creditorAccount;
    private String remittance;
    private String chargeBearer;

    public SwiftMetadataResponse() {}

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getUetr() { return uetr; }
    public void setUetr(String uetr) { this.uetr = uetr; }

    public String getSettlementDate() { return settlementDate; }
    public void setSettlementDate(String settlementDate) { this.settlementDate = settlementDate; }

    public String getDebtorBank() { return debtorBank; }
    public void setDebtorBank(String debtorBank) { this.debtorBank = debtorBank; }

    public String getDebtorBic() { return debtorBic; }
    public void setDebtorBic(String debtorBic) { this.debtorBic = debtorBic; }

    public String getDebtorAccount() { return debtorAccount; }
    public void setDebtorAccount(String debtorAccount) { this.debtorAccount = debtorAccount; }

    public String getCreditorBank() { return creditorBank; }
    public void setCreditorBank(String creditorBank) { this.creditorBank = creditorBank; }

    public String getCreditorBic() { return creditorBic; }
    public void setCreditorBic(String creditorBic) { this.creditorBic = creditorBic; }

    public String getCreditorName() { return creditorName; }
    public void setCreditorName(String creditorName) { this.creditorName = creditorName; }

    public String getCreditorAccount() { return creditorAccount; }
    public void setCreditorAccount(String creditorAccount) { this.creditorAccount = creditorAccount; }

    public String getRemittance() { return remittance; }
    public void setRemittance(String remittance) { this.remittance = remittance; }

    public String getChargeBearer() { return chargeBearer; }
    public void setChargeBearer(String chargeBearer) { this.chargeBearer = chargeBearer; }
}
