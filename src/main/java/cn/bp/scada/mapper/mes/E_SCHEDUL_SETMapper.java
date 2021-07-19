package cn.bp.scada.mapper.mes;

import cn.bp.scada.modle.mes.E_SCHEDUL_SET;

import java.util.Date;
import java.util.List;

public interface E_SCHEDUL_SETMapper {
    int deleteByPrimaryKey(Short id);

    int insert(E_SCHEDUL_SET record);

    E_SCHEDUL_SET selectByPrimaryKey(Short id);

    List<E_SCHEDUL_SET> selectAll();

    int updateByPrimaryKey(E_SCHEDUL_SET record);

    int insertAll(E_SCHEDUL_SET record);

    int insertDayTime();//每早上插入最新时间--白班

    int insertNightTime();//每早上插入最新时间--夜班

    int updateSSMS();//每早上9:00更新八点半之前可维护状态--开班时间表
}