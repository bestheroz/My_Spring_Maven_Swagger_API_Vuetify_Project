package com.github.bestheroz.sample.api.table.samplemembermst;

import org.joda.time.DateTime;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TestTableSampleMemberMstVO implements Serializable {
    private String memberId;
    private String memberPw;
    private String memberNm;
    private String memberTyp;
    private Integer loginFailCnt;
    private Boolean closeTf;
    private String token;
    private DateTime expireDt;
    private String regMemberId;
    private DateTime regDt;
    private String updMemberId;
    private DateTime updDt;

    public String getMemberId() {
        return this.memberId;
    }

    public void setMemberId(final String memberId) {
        this.memberId = memberId;
    }

    public String getMemberPw() {
        return this.memberPw;
    }

    public void setMemberPw(final String memberPw) {
        this.memberPw = memberPw;
    }

    public String getMemberNm() {
        return this.memberNm;
    }

    public void setMemberNm(final String memberNm) {
        this.memberNm = memberNm;
    }

    public String getMemberTyp() {
        return this.memberTyp;
    }

    public void setMemberTyp(final String memberTyp) {
        this.memberTyp = memberTyp;
    }

    public Integer getLoginFailCnt() {
        return this.loginFailCnt;
    }

    public void setLoginFailCnt(final Integer loginFailCnt) {
        this.loginFailCnt = loginFailCnt;
    }

    public Boolean getCloseTf() {
        return this.closeTf;
    }

    public void setCloseTf(final Boolean closeTf) {
        this.closeTf = closeTf;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public DateTime getExpireDt() {
        return this.expireDt;
    }

    public void setExpireDt(final DateTime expireDt) {
        this.expireDt = expireDt;
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
