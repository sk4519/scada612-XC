package cn.bp.scada.mapper.scada;

import cn.bp.scada.modle.scada.CiPLANTeT;
import java.util.List;
import java.util.Map;

public interface CiPLANTeTMapper {
    int deleteByPrimaryKey(String etCd);

    int insert(CiPLANTeT record);

    CiPLANTeT selectByPrimaryKey(String etCd);

    List<CiPLANTeT> selectAll();

    int updateByPrimaryKey(Map<String,Object> record);

    int selectMO();

    int updateEt(int etSt);
}