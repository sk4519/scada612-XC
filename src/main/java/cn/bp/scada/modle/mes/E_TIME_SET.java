package cn.bp.scada.modle.mes;

import org.springframework.stereotype.Repository;

import java.util.Date;
@Repository
public class E_TIME_SET {
    private Short id;

    private Short stopRecordTimeSet;

    private Date recordTime;

    private String dayMorning1;

    private String dayMorning2;

    private String dayAfternoon1;

    private String dayAfternoon2;

    private String dayNight1;

    private String dayNight2;

    private String nightMorning1;

    private String nightMorning2;

    private String nightAfternoon1;

    private String nightAfternoon2;

    private String nightNight1;

    private String nightNight2;

    public E_TIME_SET(Short id, Short stopRecordTimeSet, Date recordTime, String dayMorning1, String dayMorning2, String dayAfternoon1, String dayAfternoon2, String dayNight1, String dayNight2, String nightMorning1, String nightMorning2, String nightAfternoon1, String nightAfternoon2, String nightNight1, String nightNight2) {
        this.id = id;
        this.stopRecordTimeSet = stopRecordTimeSet;
        this.recordTime = recordTime;
        this.dayMorning1 = dayMorning1;
        this.dayMorning2 = dayMorning2;
        this.dayAfternoon1 = dayAfternoon1;
        this.dayAfternoon2 = dayAfternoon2;
        this.dayNight1 = dayNight1;
        this.dayNight2 = dayNight2;
        this.nightMorning1 = nightMorning1;
        this.nightMorning2 = nightMorning2;
        this.nightAfternoon1 = nightAfternoon1;
        this.nightAfternoon2 = nightAfternoon2;
        this.nightNight1 = nightNight1;
        this.nightNight2 = nightNight2;
    }

    public E_TIME_SET() {
        super();
    }

    public Short getId() {
        return id;
    }

    public void setId(Short id) {
        this.id = id;
    }

    public Short getStopRecordTimeSet() {
        return stopRecordTimeSet;
    }

    public void setStopRecordTimeSet(Short stopRecordTimeSet) {
        this.stopRecordTimeSet = stopRecordTimeSet;
    }

    public Date getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(Date recordTime) {
        this.recordTime = recordTime;
    }

    public String getDayMorning1() {
        return dayMorning1;
    }

    public void setDayMorning1(String dayMorning1) {
        this.dayMorning1 = dayMorning1 == null ? null : dayMorning1.trim();
    }

    public String getDayMorning2() {
        return dayMorning2;
    }

    public void setDayMorning2(String dayMorning2) {
        this.dayMorning2 = dayMorning2 == null ? null : dayMorning2.trim();
    }

    public String getDayAfternoon1() {
        return dayAfternoon1;
    }

    public void setDayAfternoon1(String dayAfternoon1) {
        this.dayAfternoon1 = dayAfternoon1 == null ? null : dayAfternoon1.trim();
    }

    public String getDayAfternoon2() {
        return dayAfternoon2;
    }

    public void setDayAfternoon2(String dayAfternoon2) {
        this.dayAfternoon2 = dayAfternoon2 == null ? null : dayAfternoon2.trim();
    }

    public String getDayNight1() {
        return dayNight1;
    }

    public void setDayNight1(String dayNight1) {
        this.dayNight1 = dayNight1 == null ? null : dayNight1.trim();
    }

    public String getDayNight2() {
        return dayNight2;
    }

    public void setDayNight2(String dayNight2) {
        this.dayNight2 = dayNight2 == null ? null : dayNight2.trim();
    }

    public String getNightMorning1() {
        return nightMorning1;
    }

    public void setNightMorning1(String nightMorning1) {
        this.nightMorning1 = nightMorning1 == null ? null : nightMorning1.trim();
    }

    public String getNightMorning2() {
        return nightMorning2;
    }

    public void setNightMorning2(String nightMorning2) {
        this.nightMorning2 = nightMorning2 == null ? null : nightMorning2.trim();
    }

    public String getNightAfternoon1() {
        return nightAfternoon1;
    }

    public void setNightAfternoon1(String nightAfternoon1) {
        this.nightAfternoon1 = nightAfternoon1 == null ? null : nightAfternoon1.trim();
    }

    public String getNightAfternoon2() {
        return nightAfternoon2;
    }

    public void setNightAfternoon2(String nightAfternoon2) {
        this.nightAfternoon2 = nightAfternoon2 == null ? null : nightAfternoon2.trim();
    }

    public String getNightNight1() {
        return nightNight1;
    }

    public void setNightNight1(String nightNight1) {
        this.nightNight1 = nightNight1 == null ? null : nightNight1.trim();
    }

    public String getNightNight2() {
        return nightNight2;
    }

    public void setNightNight2(String nightNight2) {
        this.nightNight2 = nightNight2 == null ? null : nightNight2.trim();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", stopRecordTimeSet=").append(stopRecordTimeSet);
        sb.append(", recordTime=").append(recordTime);
        sb.append(", dayMorning1=").append(dayMorning1);
        sb.append(", dayMorning2=").append(dayMorning2);
        sb.append(", dayAfternoon1=").append(dayAfternoon1);
        sb.append(", dayAfternoon2=").append(dayAfternoon2);
        sb.append(", dayNight1=").append(dayNight1);
        sb.append(", dayNight2=").append(dayNight2);
        sb.append(", nightMorning1=").append(nightMorning1);
        sb.append(", nightMorning2=").append(nightMorning2);
        sb.append(", nightAfternoon1=").append(nightAfternoon1);
        sb.append(", nightAfternoon2=").append(nightAfternoon2);
        sb.append(", nightNight1=").append(nightNight1);
        sb.append(", nightNight2=").append(nightNight2);
        sb.append("]");
        return sb.toString();
    }
}