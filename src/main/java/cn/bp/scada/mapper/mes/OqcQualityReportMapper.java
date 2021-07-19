package cn.bp.scada.mapper.mes;

import cn.bp.scada.modle.mes.OqcQualityReport;
import java.util.List;

public interface OqcQualityReportMapper {
    int deleteByPrimaryKey(Short id);

    int insert(OqcQualityReport record);

    OqcQualityReport selectByPrimaryKey(Short id);

    List<OqcQualityReport> selectAll();

    int updateByPrimaryKey(OqcQualityReport record);
}