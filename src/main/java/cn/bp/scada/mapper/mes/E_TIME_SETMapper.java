package cn.bp.scada.mapper.mes;

import cn.bp.scada.modle.mes.E_TIME_SET;

import java.util.List;

public interface E_TIME_SETMapper {
    int deleteByPrimaryKey(Short id);

    int insert(E_TIME_SET record);

    E_TIME_SET selectByPrimaryKey(Short id);

    List<E_TIME_SET> selectAll();

    E_TIME_SET selectNewTime();

    int updateByPrimaryKey(E_TIME_SET record);

}