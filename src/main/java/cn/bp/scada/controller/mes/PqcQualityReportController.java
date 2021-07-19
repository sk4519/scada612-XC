
package cn.bp.scada.controller.mes;


import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;

import cn.bp.scada.modle.mes.OqcQualityReport;
import cn.bp.scada.modle.mes.PqcQualityReport;
import cn.bp.scada.service.scada.OqcQualityReportService;
import cn.bp.scada.service.scada.PqcQualityReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(value = "OQC控制层",tags = {"新增OQC质检报告"})
@RestController
public class PqcQualityReportController {

    @Autowired
    private PqcQualityReportService pqcQualityReportService;
    
    
    @PostMapping("/savePqcQualityReport")
    @ApiOperation(value="添加PQC检查报告", notes="增加PQC检查报告" )
	@ApiImplicitParams({
        @ApiImplicitParam(name = "userId", value = "admin", required = true, paramType = "query",dataType = "String"),
        @ApiImplicitParam(name = "pqcQualityReport", value = "PQC检查报告录入信息", required = true, dataType = "PqcQualityReport")
	})
    //@RequestBody PqcQualityReport pqcQualityReport
    @ResponseBody
	public Map<String, Object> savePqcQualityReport(String userId, String pqcQualityReport) throws Exception {
    	   Map<String, Object> map = new HashMap<>();
    	   PqcQualityReport pqcQualityReportJS = JSONArray.parseObject(pqcQualityReport, PqcQualityReport.class); 
    	try{
    	    //int result = oqcQualityReportService.insertOqcQualityReport(pqcQualityReportJS, userId);
    		Map<String, Object> map1 = pqcQualityReportService.insertPqcQualityReport(pqcQualityReportJS, userId);
	    	map.put("massg", map1.get("messg"));
	    	map.put("code", map1.get("code"));
    	}catch(Exception e){
    		e.printStackTrace();
    	}
		return map;

    }


}



