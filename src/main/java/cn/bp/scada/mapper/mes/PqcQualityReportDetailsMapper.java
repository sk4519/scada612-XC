package cn.bp.scada.mapper.mes;

import cn.bp.scada.modle.mes.PqcQualityReportDetails;
import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface PqcQualityReportDetailsMapper {
    int insert(List<PqcQualityReportDetails> record);

    List<PqcQualityReportDetails> selectAll();
    
    int updateBatchPQC(List<PqcQualityReportDetails> record );
    
    /*int batchUpdateSplitSinglePickCurrency(@Param(value = "list") List<MultiOrderCurrency> list) throws Exception;*/
    
}