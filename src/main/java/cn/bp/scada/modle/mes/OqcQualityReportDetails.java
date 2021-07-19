package cn.bp.scada.modle.mes;

import java.util.Date;

public class OqcQualityReportDetails {
    private Integer id;

    private Integer oqcQualityReportId;

    private String inspTypeCode;

    private String inspTypeName;

    private Integer inspTypeSort;

    private String inspDescCode;

    private String inspDescName;

    private Integer inspDescSort;

    private String inspResult;

    private String remarks;

    private String crtId;

    private Date crtDt;

    private String uptId;

    private Date uptDt;

    public OqcQualityReportDetails(Integer id, Integer oqcQualityReportId, String inspTypeCode, String inspTypeName, Integer inspTypeSort, String inspDescCode, String inspDescName, Integer inspDescSort, String inspResult, String remarks, String crtId, Date crtDt, String uptId, Date uptDt) {
        this.id = id;
        this.oqcQualityReportId = oqcQualityReportId;
        this.inspTypeCode = inspTypeCode;
        this.inspTypeName = inspTypeName;
        this.inspTypeSort = inspTypeSort;
        this.inspDescCode = inspDescCode;
        this.inspDescName = inspDescName;
        this.inspDescSort = inspDescSort;
        this.inspResult = inspResult;
        this.remarks = remarks;
        this.crtId = crtId;
        this.crtDt = crtDt;
        this.uptId = uptId;
        this.uptDt = uptDt;
    }

    public OqcQualityReportDetails() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOqcQualityReportId() {
        return oqcQualityReportId;
    }

    public void setOqcQualityReportId(Integer oqcQualityReportId) {
        this.oqcQualityReportId = oqcQualityReportId;
    }

    public String getInspTypeCode() {
        return inspTypeCode;
    }

    public void setInspTypeCode(String inspTypeCode) {
        this.inspTypeCode = inspTypeCode == null ? null : inspTypeCode.trim();
    }

    public String getInspTypeName() {
        return inspTypeName;
    }

    public void setInspTypeName(String inspTypeName) {
        this.inspTypeName = inspTypeName == null ? null : inspTypeName.trim();
    }

    public Integer getInspTypeSort() {
        return inspTypeSort;
    }

    public void setInspTypeSort(Integer inspTypeSort) {
        this.inspTypeSort = inspTypeSort;
    }

    public String getInspDescCode() {
        return inspDescCode;
    }

    public void setInspDescCode(String inspDescCode) {
        this.inspDescCode = inspDescCode == null ? null : inspDescCode.trim();
    }

    public String getInspDescName() {
        return inspDescName;
    }

    public void setInspDescName(String inspDescName) {
        this.inspDescName = inspDescName == null ? null : inspDescName.trim();
    }

    public Integer getInspDescSort() {
        return inspDescSort;
    }

    public void setInspDescSort(Integer inspDescSort) {
        this.inspDescSort = inspDescSort;
    }

    public String getInspResult() {
        return inspResult;
    }

    public void setInspResult(String inspResult) {
        this.inspResult = inspResult == null ? null : inspResult.trim();
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks == null ? null : remarks.trim();
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", oqcQualityReportId=").append(oqcQualityReportId);
        sb.append(", inspTypeCode=").append(inspTypeCode);
        sb.append(", inspTypeName=").append(inspTypeName);
        sb.append(", inspTypeSort=").append(inspTypeSort);
        sb.append(", inspDescCode=").append(inspDescCode);
        sb.append(", inspDescName=").append(inspDescName);
        sb.append(", inspDescSort=").append(inspDescSort);
        sb.append(", inspResult=").append(inspResult);
        sb.append(", remarks=").append(remarks);
        sb.append(", crtId=").append(crtId);
        sb.append(", crtDt=").append(crtDt);
        sb.append(", uptId=").append(uptId);
        sb.append(", uptDt=").append(uptDt);
        sb.append("]");
        return sb.toString();
    }
}