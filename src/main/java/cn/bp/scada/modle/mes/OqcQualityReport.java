package cn.bp.scada.modle.mes;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class OqcQualityReport implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -6684833278967279687L;
    private  List<OqcQualityReportDetails> reportDetailsList;
    
	private Integer id;

    private String snNo;

    private String isSecrecy;

    private String crtId;

    private Date crtDt;

    private String uptId;

    private Date uptDt;

    private String classify;

    private String attribut;

    private String faultDescription;

    private String repairMethod;

    private String remarks;
    


    public OqcQualityReport(Integer id, String snNo, String isSecrecy, String crtId, Date crtDt, String uptId, Date uptDt, String classify, String attribut, String faultDescription, String repairMethod, String remarks) {
        this.id = id;
        this.snNo = snNo;
        this.isSecrecy = isSecrecy;
        this.crtId = crtId;
        this.crtDt = crtDt;
        this.uptId = uptId;
        this.uptDt = uptDt;
        this.classify = classify;
        this.attribut = attribut;
        this.faultDescription = faultDescription;
        this.repairMethod = repairMethod;
        this.remarks = remarks;
    }

    public OqcQualityReport() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSnNo() {
        return snNo;
    }

    public void setSnNo(String snNo) {
        this.snNo = snNo == null ? null : snNo.trim();
    }

    public String getIsSecrecy() {
        return isSecrecy;
    }

    public void setIsSecrecy(String isSecrecy) {
        this.isSecrecy = isSecrecy == null ? null : isSecrecy.trim();
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

    public String getClassify() {
        return classify;
    }

    public void setClassify(String classify) {
        this.classify = classify == null ? null : classify.trim();
    }

    public String getAttribut() {
        return attribut;
    }

    public void setAttribut(String attribut) {
        this.attribut = attribut == null ? null : attribut.trim();
    }

    public String getFaultDescription() {
        return faultDescription;
    }

    public void setFaultDescription(String faultDescription) {
        this.faultDescription = faultDescription == null ? null : faultDescription.trim();
    }

    /*public String getMaintMethod() {
        return maintMethod;
    }

    public void setMaintMethod(String maintMethod) {
        this.maintMethod = maintMethod == null ? null : maintMethod.trim();
    }*/
    
    
    

    public String getRepairMethod() {
		return repairMethod;
	}

	public void setRepairMethod(String repairMethod) {
		this.repairMethod = repairMethod;
	}

	public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks == null ? null : remarks.trim();
    }


	public List<OqcQualityReportDetails> getReportDetailsList() {
		return reportDetailsList;
	}

	public void setReportDetailsList(List<OqcQualityReportDetails> reportDetailsList) {
		this.reportDetailsList = reportDetailsList;
	}

	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", snNo=").append(snNo);
        sb.append(", isSecrecy=").append(isSecrecy);
        sb.append(", crtId=").append(crtId);
        sb.append(", crtDt=").append(crtDt);
        sb.append(", uptId=").append(uptId);
        sb.append(", uptDt=").append(uptDt);
        sb.append(", classify=").append(classify);
        sb.append(", attribut=").append(attribut);
        sb.append(", faultDescription=").append(faultDescription);
        sb.append(", repairMethod=").append(repairMethod);
        sb.append(", remarks=").append(remarks);
        sb.append("]");
        return sb.toString();
    }
}