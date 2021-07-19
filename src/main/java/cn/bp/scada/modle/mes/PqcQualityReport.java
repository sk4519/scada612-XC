package cn.bp.scada.modle.mes;


import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class PqcQualityReport implements Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2157398711269852666L;

	private Integer id;

    private String snNo1;

    private String isSecrecy;

    private String orderNo1;

    private String prodModel;

    private Integer poQty;

    private Date schedulingDt;

    private String instructNo;

    private String lineCd;

    private String crtId;

    private Date crtDt;

    private String uptId;

    private Date uptDt;

    private String faultDescription1;

    private String remarks;

    private String testResults1;

    private String snNo2;

    private String orderNo2;

    private String testResults2;

    private String faultDescription2;

    private String button;
    
    private String nextOne;
    
    public String getNextOne() {
		return nextOne;
	}

	public void setNextOne(String nextOne) {
		this.nextOne = nextOne;
	}

	private  List<PqcQualityReportDetails> reportDetailsList;
    
    public PqcQualityReport(Integer id, String snNo1, String isSecrecy, String orderNo1, String prodModel, Integer poQty, Date schedulingDt, String instructNo, String lineCd, String crtId, Date crtDt, String uptId, Date uptDt, String faultDescription1, String remarks, String testResults1, String snNo2, String orderNo2, String testResults2, String faultDescription2, String button) {
        this.id = id;
        this.snNo1 = snNo1;
        this.isSecrecy = isSecrecy;
        this.orderNo1 = orderNo1;
        this.prodModel = prodModel;
        this.poQty = poQty;
        this.schedulingDt = schedulingDt;
        this.instructNo = instructNo;
        this.lineCd = lineCd;
        this.crtId = crtId;
        this.crtDt = crtDt;
        this.uptId = uptId;
        this.uptDt = uptDt;
        this.faultDescription1 = faultDescription1;
        this.remarks = remarks;
        this.testResults1 = testResults1;
        this.snNo2 = snNo2;
        this.orderNo2 = orderNo2;
        this.testResults2 = testResults2;
        this.faultDescription2 = faultDescription2;
        this.button = button;
    }

    public PqcQualityReport() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSnNo1() {
        return snNo1;
    }

    public void setSnNo1(String snNo1) {
        this.snNo1 = snNo1 == null ? null : snNo1.trim();
    }

    public String getIsSecrecy() {
        return isSecrecy;
    }

    public void setIsSecrecy(String isSecrecy) {
        this.isSecrecy = isSecrecy == null ? null : isSecrecy.trim();
    }

    public String getOrderNo1() {
        return orderNo1;
    }

    public void setOrderNo1(String orderNo1) {
        this.orderNo1 = orderNo1 == null ? null : orderNo1.trim();
    }

    public String getProdModel() {
        return prodModel;
    }

    public void setProdModel(String prodModel) {
        this.prodModel = prodModel == null ? null : prodModel.trim();
    }

    public Integer getPoQty() {
        return poQty;
    }

    public void setPoQty(Integer poQty) {
        this.poQty = poQty;
    }

    public Date getSchedulingDt() {
        return schedulingDt;
    }

    public void setSchedulingDt(Date schedulingDt) {
        this.schedulingDt = schedulingDt;
    }

    public String getInstructNo() {
        return instructNo;
    }

    public void setInstructNo(String instructNo) {
        this.instructNo = instructNo == null ? null : instructNo.trim();
    }

    public String getLineCd() {
        return lineCd;
    }

    public void setLineCd(String lineCd) {
        this.lineCd = lineCd == null ? null : lineCd.trim();
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

    public String getFaultDescription1() {
        return faultDescription1;
    }

    public void setFaultDescription1(String faultDescription1) {
        this.faultDescription1 = faultDescription1 == null ? null : faultDescription1.trim();
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks == null ? null : remarks.trim();
    }

    public String getTestResults1() {
        return testResults1;
    }

    public void setTestResults1(String testResults1) {
        this.testResults1 = testResults1 == null ? null : testResults1.trim();
    }

    public String getSnNo2() {
        return snNo2;
    }

    public void setSnNo2(String snNo2) {
        this.snNo2 = snNo2 == null ? null : snNo2.trim();
    }

    public String getOrderNo2() {
        return orderNo2;
    }

    public void setOrderNo2(String orderNo2) {
        this.orderNo2 = orderNo2 == null ? null : orderNo2.trim();
    }

    public String getTestResults2() {
        return testResults2;
    }

    public void setTestResults2(String testResults2) {
        this.testResults2 = testResults2 == null ? null : testResults2.trim();
    }

    public String getFaultDescription2() {
        return faultDescription2;
    }

    public void setFaultDescription2(String faultDescription2) {
        this.faultDescription2 = faultDescription2 == null ? null : faultDescription2.trim();
    }

    public String getButton() {
        return button;
    }

    public void setButton(String button) {
        this.button = button == null ? null : button.trim();
    }
    

    public List<PqcQualityReportDetails> getReportDetailsList() {
		return reportDetailsList;
	}

	public void setReportDetailsList(List<PqcQualityReportDetails> reportDetailsList) {
		this.reportDetailsList = reportDetailsList;
	}

	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", snNo1=").append(snNo1);
        sb.append(", isSecrecy=").append(isSecrecy);
        sb.append(", orderNo1=").append(orderNo1);
        sb.append(", prodModel=").append(prodModel);
        sb.append(", poQty=").append(poQty);
        sb.append(", schedulingDt=").append(schedulingDt);
        sb.append(", instructNo=").append(instructNo);
        sb.append(", lineCd=").append(lineCd);
        sb.append(", crtId=").append(crtId);
        sb.append(", crtDt=").append(crtDt);
        sb.append(", uptId=").append(uptId);
        sb.append(", uptDt=").append(uptDt);
        sb.append(", faultDescription1=").append(faultDescription1);
        sb.append(", remarks=").append(remarks);
        sb.append(", testResults1=").append(testResults1);
        sb.append(", snNo2=").append(snNo2);
        sb.append(", orderNo2=").append(orderNo2);
        sb.append(", testResults2=").append(testResults2);
        sb.append(", faultDescription2=").append(faultDescription2);
        sb.append(", button=").append(button);
        sb.append("]");
        return sb.toString();
    }
}