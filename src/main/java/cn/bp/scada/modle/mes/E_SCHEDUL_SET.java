package cn.bp.scada.modle.mes;

import org.springframework.stereotype.Repository;

import java.util.Date;
@Repository
public class E_SCHEDUL_SET {
    private Short id;

    private String time;

    private String lineName;

    private String className;

    private String status;

    private Date workStartTime;

    private Date workEndTime;

    private String workTime;

    private String notes;

    private Date changtime;

    public E_SCHEDUL_SET(Short id, String time, String lineName, String className, String status, Date workStartTime, Date workEndTime, String workTime, String notes, Date changtime) {
        this.id = id;
        this.time = time;
        this.lineName = lineName;
        this.className = className;
        this.status = status;
        this.workStartTime = workStartTime;
        this.workEndTime = workEndTime;
        this.workTime = workTime;
        this.notes = notes;
        this.changtime = changtime;
    }

    public E_SCHEDUL_SET() {
        super();
    }

    public Short getId() {
        return id;
    }

    public void setId(Short id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time == null ? null : time.trim();
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName == null ? null : lineName.trim();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className == null ? null : className.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public Date getWorkStartTime() {
        return workStartTime;
    }

    public void setWorkStartTime(Date workStartTime) {
        this.workStartTime = workStartTime;
    }

    public Date getWorkEndTime() {
        return workEndTime;
    }

    public void setWorkEndTime(Date workEndTime) {
        this.workEndTime = workEndTime;
    }

    public String getWorkTime() {
        return workTime;
    }

    public void setWorkTime(String workTime) {
        this.workTime = workTime == null ? null : workTime.trim();
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes == null ? null : notes.trim();
    }

    public Date getChangtime() {
        return changtime;
    }

    public void setChangtime(Date changtime) {
        this.changtime = changtime;
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
        sb.append(", className=").append(className);
        sb.append(", status=").append(status);
        sb.append(", workStartTime=").append(workStartTime);
        sb.append(", workEndTime=").append(workEndTime);
        sb.append(", workTime=").append(workTime);
        sb.append(", notes=").append(notes);
        sb.append(", changtime=").append(changtime);
        sb.append("]");
        return sb.toString();
    }
}