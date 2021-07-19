package cn.bp.scada.mapper.mes;

import cn.bp.scada.modle.mes.OqcQualityReportDetails;
import java.util.List;

public interface OqcQualityReportDetailsMapper {
    int insert(List<OqcQualityReportDetails> record);

    List<OqcQualityReportDetails> selectAll();
}