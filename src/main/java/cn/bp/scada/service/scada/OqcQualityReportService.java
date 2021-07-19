package cn.bp.scada.service.scada;

import org.apache.ibatis.annotations.Param;

import cn.bp.scada.modle.mes.OqcQualityReport;

public interface OqcQualityReportService {

	 public int insertOqcQualityReport(@Param(value="oqcQualityReport") OqcQualityReport oqcQualityReport, String userId) throws Exception;

}
