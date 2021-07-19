package cn.bp.scada.service.scada;

import java.util.Map;

import org.apache.ibatis.annotations.Param;

import cn.bp.scada.modle.mes.PqcQualityReport;



public interface PqcQualityReportService {

	 public Map<String, Object> insertPqcQualityReport(@Param(value="pqcQualityReport") PqcQualityReport pqcQualityReport, String userId) throws Exception;

}
