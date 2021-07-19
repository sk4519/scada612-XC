
package cn.bp.scada.service.scada.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import cn.bp.scada.mapper.mes.PqcQualityReportDetailsMapper;
import cn.bp.scada.mapper.mes.PqcQualityReportMapper;
import cn.bp.scada.modle.mes.PqcQualityReport;
import cn.bp.scada.modle.mes.PqcQualityReportDetails;
import cn.bp.scada.service.scada.PqcQualityReportService;



@Service
public class PqcQualityReportServiceImpl implements PqcQualityReportService {
    @Autowired
    private PqcQualityReportMapper pqcQualityReportMapper;
    @Autowired
    private PqcQualityReportDetailsMapper pqcQualityReportDetailsMapper;

    private Logger LOG = LoggerFactory.getLogger(this.getClass());
    @Override
    @Transactional
    public Map<String, Object> insertPqcQualityReport(PqcQualityReport pqcQualityReport,String userId) throws Exception {
    	int res= 0;
    	String messg ;
    	Map<String, Object> map = new HashMap<>();
    	//pqc主表
    	try {
    		Integer result = pqcQualityReportMapper.selectBysnNo(pqcQualityReport.getSnNo1());
    		if (pqcQualityReport.getId()==null && result == null){
			    	PqcQualityReport pqc = new PqcQualityReport();
			    	pqc.setSnNo1(pqcQualityReport.getSnNo1());
			    	pqc.setIsSecrecy(pqcQualityReport.getIsSecrecy());//订单型号（Y是通用机，N是专用机）
			    	pqc.setOrderNo1(pqcQualityReport.getOrderNo1());
			    	pqc.setProdModel(pqcQualityReport.getProdModel());
			    	pqc.setPoQty(pqcQualityReport.getPoQty());
			    	pqc.setSchedulingDt(pqcQualityReport.getSchedulingDt());
			    	pqc.setInstructNo(pqcQualityReport.getInstructNo());
			    	pqc.setLineCd(pqcQualityReport.getLineCd());
			    	pqc.setFaultDescription1(pqcQualityReport.getFaultDescription1());
			    	pqc.setRemarks(pqcQualityReport.getRemarks());
			    	pqc.setTestResults1(pqcQualityReport.getTestResults1());
			    	//检验第二台
			    	pqc.setSnNo2(pqcQualityReport.getSnNo2());
			    	pqc.setOrderNo2(pqcQualityReport.getOrderNo2());
			    	pqc.setTestResults2(pqcQualityReport.getTestResults2());
			    	pqc.setFaultDescription2(pqcQualityReport.getFaultDescription2());
			    	pqc.setButton(pqcQualityReport.getButton());
			    	pqc.setNextOne(pqcQualityReport.getNextOne());

			    	pqc.setCrtId(userId);
			    	pqcQualityReportMapper.insert(pqc);
			    	Integer rids = pqc.getId();
			    	//子表
			    	List<PqcQualityReportDetails> pqcDetailsList = new ArrayList<PqcQualityReportDetails>();
			    	for(PqcQualityReportDetails details: pqcQualityReport.getReportDetailsList()){
			    		PqcQualityReportDetails pqcDetails = new PqcQualityReportDetails();
			    		pqcDetails.setPqcQualityReportId(rids);
				    	pqcDetails.setInspTypeCode(details.getInspTypeCode());
				    	pqcDetails.setInspTypeName(details.getInspTypeName());
				    	pqcDetails.setInspTypeSort(details.getInspTypeSort());
				    	pqcDetails.setInspDescCode(details.getInspDescCode());
				    	pqcDetails.setInspDescName(details.getInspDescName());
				    	pqcDetails.setInspDescSort(details.getInspDescSort());
				    	pqcDetails.setInspResult(details.getInspResult());
				    	pqcDetails.setRemarks(details.getRemarks());
				    	pqcDetails.setCrtId(userId);
				    	pqcDetailsList.add(pqcDetails);
			    	}
				    	res = pqcQualityReportDetailsMapper.insert(pqcDetailsList);
				    	if(res >= 0){
				    		messg="PQC首件新增成功";
				    	    map.put("code", "1");
				    	    map.put("messg", messg);
				    		LOG.info(messg);
				    	} else {
				    		 messg="PQC首件报告新增失败！";
					    	 map.put("code", "0");
					    	 map.put("messg", messg);
							 LOG.info(messg);
						} 
			    	  
    		}else{
    			        PqcQualityReport pqc = new PqcQualityReport();
				    	pqc.setId(result);
				    	pqc.setSnNo1(pqcQualityReport.getSnNo1());
				    	pqc.setIsSecrecy(pqcQualityReport.getIsSecrecy());//订单型号（Y是通用机，N是专用机）
				    	pqc.setOrderNo1(pqcQualityReport.getOrderNo1());
				    	pqc.setProdModel(pqcQualityReport.getProdModel());
				    	pqc.setPoQty(pqcQualityReport.getPoQty());
				    	pqc.setSchedulingDt(pqcQualityReport.getSchedulingDt());
				    	pqc.setInstructNo(pqcQualityReport.getInstructNo());
				    	pqc.setLineCd(pqcQualityReport.getLineCd());
				    	pqc.setFaultDescription1(pqcQualityReport.getFaultDescription1());
				    	pqc.setRemarks(pqcQualityReport.getRemarks());
				    	pqc.setTestResults1(pqcQualityReport.getTestResults1());
				    	pqc.setUptId(userId);
				    	//检验第二台
				    	pqc.setSnNo2(pqcQualityReport.getSnNo2());

				    	if(pqcQualityReport.getOrderNo2()!=null){
				    		pqc.setOrderNo2(pqcQualityReport.getOrderNo2());
					    }
				    	pqc.setTestResults2(pqcQualityReport.getTestResults2());
				    	
				    	if(pqcQualityReport.getFaultDescription2()!=null){
				    		pqc.setFaultDescription2(pqcQualityReport.getFaultDescription2());
					    }
				    	pqc.setButton(pqcQualityReport.getButton());
				    	pqc.setNextOne(pqcQualityReport.getNextOne());
				    	res = pqcQualityReportMapper.updateByPrimaryKey(pqc);

				    	//子表
				    	List<PqcQualityReportDetails> pqcDetailsList = new ArrayList<PqcQualityReportDetails>();
				    	for(PqcQualityReportDetails details: pqcQualityReport.getReportDetailsList()){
				    		PqcQualityReportDetails pqcDetails = new PqcQualityReportDetails();
				    		pqcDetails.setPqcQualityReportId(result);
//					    	pqcDetails.setInspTypeCode(details.getInspTypeCode());
//					    	pqcDetails.setInspTypeName(details.getInspTypeName());
//					    	pqcDetails.setInspTypeSort(details.getInspTypeSort());
//					    	pqcDetails.setInspDescName(details.getInspDescName());
//					    	pqcDetails.setInspDescSort(details.getInspDescSort());
					    	pqcDetails.setInspDescCode(details.getInspDescCode());
					    	pqcDetails.setInspResult(details.getInspResult());
					    	pqcDetails.setRemarks(details.getRemarks());
					    	pqcDetails.setUptId(userId);
					    	pqcDetailsList.add(pqcDetails);
				    	}
				    	pqcQualityReportDetailsMapper.updateBatchPQC(pqcDetailsList);	  	
				    	if(res >= 0){
				    		messg="PQC首件修改成功";
				    	    map.put("code", "1");
				    	    map.put("messg", messg);
				    		LOG.info(messg);
				    	} else {
				    		 messg="PQC首件修改失败！";
					    	 map.put("code", "0");
					    	 map.put("messg", messg);
							 LOG.info(messg);
						} 

    		}
        } catch (Exception e) {
            LOG.info("PQC首检报告操作失败！");
	    	 map.put("code", "0");
	    	 map.put("messg", "PQC首检报告操作失败！");
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly(); //手动回滚事务
        }
    	return map;

    }


}

