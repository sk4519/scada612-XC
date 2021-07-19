package cn.bp.scada.modle.scada;

import java.math.BigDecimal;
import java.util.Date;

public class CiPLANTeT {
    private String etCd;

    private String etNm;

    private String etUt;

    private String etMt;

    private Date etMd;

    private String etQt;

    private Date etPt;

    private String etPw;

    private String dictIt;

    private String plCd;

    private String lcCd;

    private String etTt;

    private String etSt;

    private String etLt;

    private String etPl;

    private String crtId;

    private Date crtDt;

    private String crtIp;

    private String uptId;

    private Date uptDt;

    private String uptIp;

    private String etRes;

    private String rqSn;

    private String lineCd;

    private String routCd;

    private Short etQty;

    private BigDecimal etRt;

    private String matType;

    private String etSta;

    private String isRead;

    private String devStart;

    private String devEnd;

    private String bgStart;

    private String bgEnd;

    private String bgMsg;

    private String proStart;

    private String proEnd;

    private String ct;

    private Short stdTm;

    public CiPLANTeT(String etCd, String etNm, String etUt, String etMt, Date etMd, String etQt, Date etPt, String etPw, String dictIt, String plCd, String lcCd, String etTt, String etSt, String etLt, String etPl, String crtId, Date crtDt, String crtIp, String uptId, Date uptDt, String uptIp, String etRes, String rqSn, String lineCd, String routCd, Short etQty, BigDecimal etRt, String matType, String etSta, String isRead, String devStart, String devEnd, String bgStart, String bgEnd, String bgMsg, String proStart, String proEnd, String ct, Short stdTm) {
        this.etCd = etCd;
        this.etNm = etNm;
        this.etUt = etUt;
        this.etMt = etMt;
        this.etMd = etMd;
        this.etQt = etQt;
        this.etPt = etPt;
        this.etPw = etPw;
        this.dictIt = dictIt;
        this.plCd = plCd;
        this.lcCd = lcCd;
        this.etTt = etTt;
        this.etSt = etSt;
        this.etLt = etLt;
        this.etPl = etPl;
        this.crtId = crtId;
        this.crtDt = crtDt;
        this.crtIp = crtIp;
        this.uptId = uptId;
        this.uptDt = uptDt;
        this.uptIp = uptIp;
        this.etRes = etRes;
        this.rqSn = rqSn;
        this.lineCd = lineCd;
        this.routCd = routCd;
        this.etQty = etQty;
        this.etRt = etRt;
        this.matType = matType;
        this.etSta = etSta;
        this.isRead = isRead;
        this.devStart = devStart;
        this.devEnd = devEnd;
        this.bgStart = bgStart;
        this.bgEnd = bgEnd;
        this.bgMsg = bgMsg;
        this.proStart = proStart;
        this.proEnd = proEnd;
        this.ct = ct;
        this.stdTm = stdTm;
    }

    public CiPLANTeT() {
        super();
    }

    public String getEtCd() {
        return etCd;
    }

    public void setEtCd(String etCd) {
        this.etCd = etCd == null ? null : etCd.trim();
    }

    public String getEtNm() {
        return etNm;
    }

    public void setEtNm(String etNm) {
        this.etNm = etNm == null ? null : etNm.trim();
    }

    public String getEtUt() {
        return etUt;
    }

    public void setEtUt(String etUt) {
        this.etUt = etUt == null ? null : etUt.trim();
    }

    public String getEtMt() {
        return etMt;
    }

    public void setEtMt(String etMt) {
        this.etMt = etMt == null ? null : etMt.trim();
    }

    public Date getEtMd() {
        return etMd;
    }

    public void setEtMd(Date etMd) {
        this.etMd = etMd;
    }

    public String getEtQt() {
        return etQt;
    }

    public void setEtQt(String etQt) {
        this.etQt = etQt == null ? null : etQt.trim();
    }

    public Date getEtPt() {
        return etPt;
    }

    public void setEtPt(Date etPt) {
        this.etPt = etPt;
    }

    public String getEtPw() {
        return etPw;
    }

    public void setEtPw(String etPw) {
        this.etPw = etPw == null ? null : etPw.trim();
    }

    public String getDictIt() {
        return dictIt;
    }

    public void setDictIt(String dictIt) {
        this.dictIt = dictIt == null ? null : dictIt.trim();
    }

    public String getPlCd() {
        return plCd;
    }

    public void setPlCd(String plCd) {
        this.plCd = plCd == null ? null : plCd.trim();
    }

    public String getLcCd() {
        return lcCd;
    }

    public void setLcCd(String lcCd) {
        this.lcCd = lcCd == null ? null : lcCd.trim();
    }

    public String getEtTt() {
        return etTt;
    }

    public void setEtTt(String etTt) {
        this.etTt = etTt == null ? null : etTt.trim();
    }

    public String getEtSt() {
        return etSt;
    }

    public void setEtSt(String etSt) {
        this.etSt = etSt == null ? null : etSt.trim();
    }

    public String getEtLt() {
        return etLt;
    }

    public void setEtLt(String etLt) {
        this.etLt = etLt == null ? null : etLt.trim();
    }

    public String getEtPl() {
        return etPl;
    }

    public void setEtPl(String etPl) {
        this.etPl = etPl == null ? null : etPl.trim();
    }

    public String getCrtId() {
        return crtId;
    }

    public void setCrtId(String crtId) {
        this.crtId = crtId == null ? null : crtId.trim();
    }

    public Date getCrtDt() {
        return crtDt;
    }

    public void setCrtDt(Date crtDt) {
        this.crtDt = crtDt;
    }

    public String getCrtIp() {
        return crtIp;
    }

    public void setCrtIp(String crtIp) {
        this.crtIp = crtIp == null ? null : crtIp.trim();
    }

    public String getUptId() {
        return uptId;
    }

    public void setUptId(String uptId) {
        this.uptId = uptId == null ? null : uptId.trim();
    }

    public Date getUptDt() {
        return uptDt;
    }

    public void setUptDt(Date uptDt) {
        this.uptDt = uptDt;
    }

    public String getUptIp() {
        return uptIp;
    }

    public void setUptIp(String uptIp) {
        this.uptIp = uptIp == null ? null : uptIp.trim();
    }

    public String getEtRes() {
        return etRes;
    }

    public void setEtRes(String etRes) {
        this.etRes = etRes == null ? null : etRes.trim();
    }

    public String getRqSn() {
        return rqSn;
    }

    public void setRqSn(String rqSn) {
        this.rqSn = rqSn == null ? null : rqSn.trim();
    }

    public String getLineCd() {
        return lineCd;
    }

    public void setLineCd(String lineCd) {
        this.lineCd = lineCd == null ? null : lineCd.trim();
    }

    public String getRoutCd() {
        return routCd;
    }

    public void setRoutCd(String routCd) {
        this.routCd = routCd == null ? null : routCd.trim();
    }

    public Short getEtQty() {
        return etQty;
    }

    public void setEtQty(Short etQty) {
        this.etQty = etQty;
    }

    public BigDecimal getEtRt() {
        return etRt;
    }

    public void setEtRt(BigDecimal etRt) {
        this.etRt = etRt;
    }

    public String getMatType() {
        return matType;
    }

    public void setMatType(String matType) {
        this.matType = matType == null ? null : matType.trim();
    }

    public String getEtSta() {
        return etSta;
    }

    public void setEtSta(String etSta) {
        this.etSta = etSta == null ? null : etSta.trim();
    }

    public String getIsRead() {
        return isRead;
    }

    public void setIsRead(String isRead) {
        this.isRead = isRead == null ? null : isRead.trim();
    }

    public String getDevStart() {
        return devStart;
    }

    public void setDevStart(String devStart) {
        this.devStart = devStart == null ? null : devStart.trim();
    }

    public String getDevEnd() {
        return devEnd;
    }

    public void setDevEnd(String devEnd) {
        this.devEnd = devEnd == null ? null : devEnd.trim();
    }

    public String getBgStart() {
        return bgStart;
    }

    public void setBgStart(String bgStart) {
        this.bgStart = bgStart == null ? null : bgStart.trim();
    }

    public String getBgEnd() {
        return bgEnd;
    }

    public void setBgEnd(String bgEnd) {
        this.bgEnd = bgEnd == null ? null : bgEnd.trim();
    }

    public String getBgMsg() {
        return bgMsg;
    }

    public void setBgMsg(String bgMsg) {
        this.bgMsg = bgMsg == null ? null : bgMsg.trim();
    }

    public String getProStart() {
        return proStart;
    }

    public void setProStart(String proStart) {
        this.proStart = proStart == null ? null : proStart.trim();
    }

    public String getProEnd() {
        return proEnd;
    }

    public void setProEnd(String proEnd) {
        this.proEnd = proEnd == null ? null : proEnd.trim();
    }

    public String getCt() {
        return ct;
    }

    public void setCt(String ct) {
        this.ct = ct == null ? null : ct.trim();
    }

    public Short getStdTm() {
        return stdTm;
    }

    public void setStdTm(Short stdTm) {
        this.stdTm = stdTm;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", etCd=").append(etCd);
        sb.append(", etNm=").append(etNm);
        sb.append(", etUt=").append(etUt);
        sb.append(", etMt=").append(etMt);
        sb.append(", etMd=").append(etMd);
        sb.append(", etQt=").append(etQt);
        sb.append(", etPt=").append(etPt);
        sb.append(", etPw=").append(etPw);
        sb.append(", dictIt=").append(dictIt);
        sb.append(", plCd=").append(plCd);
        sb.append(", lcCd=").append(lcCd);
        sb.append(", etTt=").append(etTt);
        sb.append(", etSt=").append(etSt);
        sb.append(", etLt=").append(etLt);
        sb.append(", etPl=").append(etPl);
        sb.append(", crtId=").append(crtId);
        sb.append(", crtDt=").append(crtDt);
        sb.append(", crtIp=").append(crtIp);
        sb.append(", uptId=").append(uptId);
        sb.append(", uptDt=").append(uptDt);
        sb.append(", uptIp=").append(uptIp);
        sb.append(", etRes=").append(etRes);
        sb.append(", rqSn=").append(rqSn);
        sb.append(", lineCd=").append(lineCd);
        sb.append(", routCd=").append(routCd);
        sb.append(", etQty=").append(etQty);
        sb.append(", etRt=").append(etRt);
        sb.append(", matType=").append(matType);
        sb.append(", etSta=").append(etSta);
        sb.append(", isRead=").append(isRead);
        sb.append(", devStart=").append(devStart);
        sb.append(", devEnd=").append(devEnd);
        sb.append(", bgStart=").append(bgStart);
        sb.append(", bgEnd=").append(bgEnd);
        sb.append(", bgMsg=").append(bgMsg);
        sb.append(", proStart=").append(proStart);
        sb.append(", proEnd=").append(proEnd);
        sb.append(", ct=").append(ct);
        sb.append(", stdTm=").append(stdTm);
        sb.append("]");
        return sb.toString();
    }
}