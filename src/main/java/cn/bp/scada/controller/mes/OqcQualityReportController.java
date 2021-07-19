package cn.bp.scada.controller.mes;


import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;

import cn.bp.scada.modle.mes.OqcQualityReport;
import cn.bp.scada.modle.mes.PqcQualityReport;
import cn.bp.scada.service.scada.OqcQualityReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(value = "OQC控制层",tags = {"新增OQC质检报告"})
@RestController
public class OqcQualityReportController {

    @Autowired
    private OqcQualityReportService oqcQualityReportService;
    
    
    
    @PostMapping("/saveOqcQualityReport")
    @ApiOperation(value="添加OQC检查报告", notes="增加OQC检查报告" )
	@ApiImplicitParams({
        @ApiImplicitParam(name = "userId", value = "userId", required = true, paramType = "query",dataType = "String"),
        @ApiImplicitParam(name = "oqcQualityReport", value = "OQC检查报告录入信息", required = true, dataType = "OqcQualityReport")
	})
    @ResponseBody
	public Map<String, Object> saveOqcQualityReport(String userId,  String oqcQualityReport) throws Exception {
    	   Map<String, Object> map = new HashMap<>();
    	   OqcQualityReport oqcQualityReportJS = JSONArray.parseObject(oqcQualityReport, OqcQualityReport.class); 
    	try{
    	    int result = oqcQualityReportService.insertOqcQualityReport(oqcQualityReportJS, userId);
    	    if (result > 0 ) {
    	    	map.put("massg", "新增成功");
    	    	map.put("code", "1");
    	    }else{
    	    	map.put("massg", "新增失败");
    	    	map.put("code", "0");
    	    }
    	    
    	}catch(Exception e){
    		e.printStackTrace();
    	}
		return map;

    }
    

}
