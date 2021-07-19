package cn.bp.scada.modle.mes;

import org.springframework.stereotype.Repository;

import java.util.Date;
@Repository
public class E_STOP_REASON {
    private Short id;

    private Date time;

    private String lineName;

    private String calssName;

    private Date stopStartTime;

    private Date stopEndTime;

    private Short stopTime;

    private String stopReason1;

    private String stopReason2;

    private String notes;

    private Short recordStatus;

    private String machineid;

    public E_STOP_REASON(Short id, Date time, String lineName, String calssName, Date stopStartTime, Date stopEndTime, Short stopTime, String stopReason1, String stopReason2, String notes, Short recordStatus, String machineid) {
        this.id = id;
        this.time = time;
        this.lineName = lineName;
        this.calssName = calssName;
        this.stopStartTime = stopStartTime;
        this.stopEndTime = stopEndTime;
        this.stopTime = stopTime;
        this.stopReason1 = stopReason1;
        this.stopReason2 = stopReason2;
        this.notes = notes;
        this.recordStatus = recordStatus;
        this.machineid = machineid;
    }

    public E_STOP_REASON() {
        super();
    }

    public Short getId() {
        return id;
    }

    public void setId(Short id) {
        this.id = id;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName == null ? null : lineName.trim();
    }

    public String getCalssName() {
        return calssName;
    }

    public void setCalssName(String calssName) {
        this.calssName = calssName == null ? null : calssName.trim();
    }

    public Date getStopStartTime() {
        return stopStartTime;
    }

    public void setStopStartTime(Date stopStartTime) {
        this.stopStartTime = stopStartTime;
    }

    public Date getStopEndTime() {
        return stopEndTime;
    }

    public void setStopEndTime(Date stopEndTime) {
        this.stopEndTime = stopEndTime;
    }

    public Short getStopTime() {
        return stopTime;
    }

    public void setStopTime(Short stopTime) {
        this.stopTime = stopTime;
    }

    public String getStopReason1() {
        return stopReason1;
    }

    public void setStopReason1(String stopReason1) {
        this.stopReason1 = stopReason1 == null ? null : stopReason1.trim();
    }

    public String getStopReason2() {
        return stopReason2;
    }

    public void setStopReason2(String stopReason2) {
        this.stopReason2 = stopReason2 == null ? null : stopReason2.trim();
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes == null ? null : notes.trim();
    }

    public Short getRecordStatus() {
        return recordStatus;
    }

    public void setRecordStatus(Short recordStatus) {
        this.recordStatus = recordStatus;
    }

    public String getMachineid() {
        return machineid;
    }

    public void setMachineid(String machineid) {
        this.machineid = machineid == null ? null : machineid.trim();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", time=").append(time);
        sb.append(", lineName=").append(lineName);
        sb.append(", calssName=").append(calssName);
        sb.append(", stopStartTime=").append(stopStartTime);
        sb.append(", stopEndTime=").append(stopEndTime);
        sb.append(", stopTime=").append(stopTime);
        sb.append(", stopReason1=").append(stopReason1);
        sb.append(", stopReason2=").append(stopReason2);
        sb.append(", notes=").append(notes);
        sb.append(", recordStatus=").append(recordStatus);
        sb.append(", machineid=").append(machineid);
        sb.append("]");
        return sb.toString();
    }
}