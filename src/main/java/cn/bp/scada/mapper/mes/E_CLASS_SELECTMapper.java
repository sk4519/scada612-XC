package cn.bp.scada.mapper.mes;

import cn.bp.scada.modle.mes.E_CLASS_SELECT;

import java.util.List;

public interface E_CLASS_SELECTMapper {
    int deleteByPrimaryKey(Short id);

    int insert(E_CLASS_SELECT record);

    E_CLASS_SELECT selectByPrimaryKey(Short id);

    List<E_CLASS_SELECT> selectAll();

    String selectDayClassEnable(String machineID);

    String selectNightClassEnable(String machineID);

    int updateByPrimaryKey(E_CLASS_SELECT record);

    int updateStatus(E_CLASS_SELECT record);

    String selectLineName(String machineID);

    int insertallline();//插入所有數據，更新connectID

}