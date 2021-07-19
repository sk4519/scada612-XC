
package cn.bp.scada.service.scada.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import cn.bp.scada.mapper.mes.OqcQualityReportDetailsMapper;
import cn.bp.scada.mapper.mes.OqcQualityReportMapper;
import cn.bp.scada.modle.mes.OqcQualityReport;
import cn.bp.scada.modle.mes.OqcQualityReportDetails;
import cn.bp.scada.service.scada.OqcQualityReportService;

@Service
public class OqcQualityReportServiceImpl implements OqcQualityReportService {
    @Autowired
    private OqcQualityReportMapper oqcQualityReportMapper;
    @Autowired
    private OqcQualityReportDetailsMapper oqcQualityReportDetailsMapper;

    private Logger LOG = LoggerFactory.getLogger(this.getClass());
    @Override
    @Transactional
    public int insertOqcQualityReport(OqcQualityReport oqcQualityReport,String userId) throws Exception {
    	int res= 0;

    	//oqc主表
    	try {
    		if (oqcQualityReport.getId()==null){
			    	OqcQualityReport oqc = new OqcQualityReport();
			    	oqc.setSnNo(oqcQualityReport.getSnNo());
			    	oqc.setIsSecrecy(oqcQualityReport.getIsSecrecy());
			    	oqc.setClassify(oqcQualityReport.getClassify());
			    	oqc.setAttribut(oqcQualityReport.getAttribut());
			    	oqc.setFaultDescription(oqcQualityReport.getFaultDescription());
			    	oqc.setRepairMethod(oqcQualityReport.getRepairMethod());
			    	oqc.setRemarks(oqcQualityReport.getRemarks());
			    	oqc.setCrtId(userId);
			    	oqcQualityReportMapper.insert(oqc);
			    	Integer rids = oqc.getId();
			    	//子表
			    	List<OqcQualityReportDetails> oqcDetailsList = new ArrayList<OqcQualityReportDetails>();
			    	for(OqcQualityReportDetails details: oqcQualityReport.getReportDetailsList()){
			    		OqcQualityReportDetails oqcDetails = new OqcQualityReportDetails();
			    		oqcDetails.setOqcQualityReportId(rids);
				    	oqcDetails.setInspTypeCode(details.getInspTypeCode());
				    	oqcDetails.setInspTypeName(details.getInspTypeName());
				    	oqcDetails.setInspTypeSort(details.getInspTypeSort());
				    	oqcDetails.setInspDescCode(details.getInspDescCode());
				    	oqcDetails.setInspDescName(details.getInspDescName());
				    	oqcDetails.setInspDescSort(details.getInspDescSort());
				    	oqcDetails.setInspResult(details.getInspResult());
				    	oqcDetails.setRemarks(details.getRemarks());
				    	oqcDetails.setCrtId(userId);
				    	oqcDetailsList.add(oqcDetails);
			    	}
			    	  res = oqcQualityReportDetailsMapper.insert(oqcDetailsList);
    		}else{
				    	OqcQualityReport oqc = new OqcQualityReport();
				    	oqc.setId(oqcQualityReport.getId());
				    	oqc.setSnNo(oqcQualityReport.getSnNo());
				    	oqc.setIsSecrecy(oqcQualityReport.getIsSecrecy());
				    	oqc.setClassify(oqcQualityReport.getClassify());
				    	oqc.setAttribut(oqcQualityReport.getAttribut());
				    	oqc.setFaultDescription(oqcQualityReport.getFaultDescription());
				    	oqc.setRepairMethod(oqcQualityReport.getRepairMethod());
				    	oqc.setRemarks(oqcQualityReport.getRemarks());
				    	oqc.setUptId(userId);
				    	res = oqcQualityReportMapper.updateByPrimaryKey(oqc);
    		}

	    	if(res > 0){
	    		LOG.info("OQC检查报告操作成功！");
	    	} else {
				LOG.info("OQC检查报告操作失败！");
			}
        } catch (Exception e) {
            LOG.info("OQC检查报告保存失败！");
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly(); //手动回滚事务
        }
    	return res;

    }


}

