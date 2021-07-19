package cn.bp.scada.modle.mes;

import org.springframework.stereotype.Repository;

@Repository
public class    E_CLASS_SELECT {
    private Short id;

    private Short connectTimeSetId;

    private String classType;

    private String machineLineType;

    private Short selectStatus;

    private String machineid;

    public E_CLASS_SELECT(Short id, Short connectTimeSetId, String classType, String machineLineType, Short selectStatus, String machineid) {
        this.id = id;
        this.connectTimeSetId = connectTimeSetId;
        this.classType = classType;
        this.machineLineType = machineLineType;
        this.selectStatus = selectStatus;
        this.machineid = machineid;
    }

    public E_CLASS_SELECT() {
        super();
    }

    public Short getId() {
        return id;
    }

    public void setId(Short id) {
        this.id = id;
    }

    public Short getConnectTimeSetId() {
        return connectTimeSetId;
    }

    public void setConnectTimeSetId(Short connectTimeSetId) {
        this.connectTimeSetId = connectTimeSetId;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType == null ? null : classType.trim();
    }

    public String getMachineLineType() {
        return machineLineType;
    }

    public void setMachineLineType(String machineLineType) {
        this.machineLineType = machineLineType == null ? null : machineLineType.trim();
    }

    public Short getSelectStatus() {
        return selectStatus;
    }

    public void setSelectStatus(Short selectStatus) {
        this.selectStatus = selectStatus;
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
        sb.append(", connectTimeSetId=").append(connectTimeSetId);
        sb.append(", classType=").append(classType);
        sb.append(", machineLineType=").append(machineLineType);
        sb.append(", selectStatus=").append(selectStatus);
        sb.append(", machineid=").append(machineid);
        sb.append("]");
        return sb.toString();
    }
}