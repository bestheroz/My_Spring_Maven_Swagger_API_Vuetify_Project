package com.github.bestheroz.sample.api.table.samplecodedet;

import org.joda.time.DateTime;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TestTableSampleCodeDetVO implements Serializable {
    private String groupCode;
    private String code;
    private String codeNm;
    private Boolean useTf;
    private Integer displayOrder;
    private String remark1;
    private String regMemberId;
    private DateTime regDt;
    private String updMemberId;
    private DateTime updDt;

    public String getGroupCode() {
        return this.groupCode;
    }

    public void setGroupCode(final String groupCode) {
        this.groupCode = groupCode;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public String getCodeNm() {
        return this.codeNm;
    }

    public void setCodeNm(final String codeNm) {
        this.codeNm = codeNm;
    }

    public Boolean getUseTf() {
        return this.useTf;
    }

    public void setUseTf(final Boolean useTf) {
        this.useTf = useTf;
    }

    public Integer getDisplayOrder() {
        return this.displayOrder;
    }

    public void setDispSeq(final Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getRemark1() {
        return this.remark1;
    }

    public void setRemark1(final String remark1) {
        this.remark1 = remark1;
    }

    public String getRegMemberId() {
        return this.regMemberId;
    }

    public void setRegMemberId(final String regMemberId) {
        this.regMemberId = regMemberId;
    }

    public DateTime getRegDt() {
        return this.regDt;
    }

    public void setRegDt(final DateTime regDt) {
        this.regDt = regDt;
    }

    public String getUpdMemberId() {
        return this.updMemberId;
    }

    public void setUpdMemberId(final String updMemberId) {
        this.updMemberId = updMemberId;
    }

    public DateTime getUpdDt() {
        return this.updDt;
    }

    public void setUpdDt(final DateTime updDt) {
        this.updDt = updDt;
    }

}
