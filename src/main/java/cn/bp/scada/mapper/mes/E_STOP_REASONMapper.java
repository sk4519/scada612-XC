package cn.bp.scada.mapper.mes;

import cn.bp.scada.modle.mes.E_STOP_REASON;

import java.util.Date;
import java.util.List;

public interface E_STOP_REASONMapper {
    int deleteByPrimaryKey(Short id);

    int insert(E_STOP_REASON record);

    short selectMaxID();

    E_STOP_REASON selectByPrimaryKey(Short id);

    List<E_STOP_REASON> selectAll();

    Date selectNewTime1(String machine);

    int updateByPrimaryKey(E_STOP_REASON record);

    int updateNewTime(E_STOP_REASON record);

    int updateStopStartTime(E_STOP_REASON record);

    int deleteMaxRecord(String machineid);

    int insertnew1(E_STOP_REASON record);

    int updateSRMS();//每早上9:00更新八点半之前可维护状态--停线原因维护表
}