package cn.bp.scada.mapper.mes;

import cn.bp.scada.modle.mes.PqcQualityReport;
import java.util.List;

public interface PqcQualityReportMapper {
    int deleteByPrimaryKey(Long id);

    int insert(PqcQualityReport record);

    PqcQualityReport selectByPrimaryKey(Long id);

    List<PqcQualityReport> selectAll();

    int updateByPrimaryKey(PqcQualityReport record);
    
    Integer selectBysnNo(String snNo1);
}