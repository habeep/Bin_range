package com.mastercard.evaluation.bin.range.models;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class BinRangeInfo {
    private UUID ref;
    private BigDecimal start;
    private BigDecimal end;
    private String bankName;
    private String currencyCode;

    public UUID getRef() {
        return ref;
    }

    public void setRef(UUID ref) {
        this.ref = ref;
    }

    public BigDecimal getStart() {
        return start;
    }

    public void setStart(BigDecimal start) {
        this.start = start;
    }

    public BigDecimal getEnd() {
        return end;
    }

    public void setEnd(BigDecimal end) {
        this.end = end;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BinRangeInfo that = (BinRangeInfo) o;
        return Objects.equals(ref, that.ref) &&
                Objects.equals(start, that.start) &&
                Objects.equals(end, that.end) &&
                Objects.equals(bankName, that.bankName) &&
                Objects.equals(currencyCode, that.currencyCode);
    }

    @Override
    public int hashCode() {

        return Objects.hash(ref, start, end, bankName, currencyCode);
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BinRangeInfo [ref=" + ref + ", start=" + start + ", end=" + end + ", bankName=" + bankName
				+ ", currencyCode=" + currencyCode + "]";
	}
    
    
}
