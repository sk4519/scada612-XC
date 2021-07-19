package cn.bp.scada.controller.mes;

import cn.bp.scada.common.utils.data.DateUtils;
import cn.bp.scada.mapper.mes.E_SCHEDUL_SETMapper;
import cn.bp.scada.mapper.mes.E_STOP_REASONMapper;
import cn.bp.scada.modle.mes.E_SCHEDUL_SET;
import cn.bp.scada.service.scada.OEEService;
import io.swagger.annotations.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前端访问数据模板格式
 */

@Api(value = "OEE控制层", tags = {"OEE设置数值与显示"})
@RestController
public class OEEController {
    @Autowired
    private OEEService oeeService;
    @Autowired
    private E_STOP_REASONMapper e_stop_reasonMapper;
    @Autowired
    private E_SCHEDUL_SETMapper e_schedul_setMapper;
    @Autowired
    private DateUtils dateUtils;

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Scheduled(cron = "0 0 9 * * ?") //定时任务，每天9：00定时执行
    public void updateSchedul() {
        e_schedul_setMapper.insertDayTime();
        e_schedul_setMapper.insertNightTime();
        //更新维护状态
        e_schedul_setMapper.updateSSMS();//上班时间
        e_stop_reasonMapper.updateSRMS();//停线原因
      /*  e_schedul_set.setTime(dateUtils.getDate());
        Date nowTime = dateUtils.getNowDate();
        *//*   白班*//*
        e_schedul_set.setWorkStartTime(dateUtils.dateFormat1(nowTime, 0, -30));
        e_schedul_set.setWorkEndTime(dateUtils.dateFormat1(nowTime, 11, 30));
        e_schedul_set.setChangtime(nowTime);
        e_schedul_set.setWorkTime("12");
        e_schedul_set.setStatus("生产");
        e_schedul_set.setClassName("白班");
        //主板预组装A
        e_schedul_set.setLineName("主板预组装A");
        e_schedul_setMapper.insertAll(e_schedul_set);
        //主板预组装B
        e_schedul_set.setLineName("主板预组装B");
        e_schedul_setMapper.insertAll(e_schedul_set);
        //后测
        e_schedul_set.setLineName("后测");
        e_schedul_setMapper.insertAll(e_schedul_set);
        //包装
        e_schedul_set.setLineName("包装");
        e_schedul_setMapper.insertAll(e_schedul_set);
        //机器组装A
        e_schedul_set.setLineName("机器组装A");
        e_schedul_setMapper.insertAll(e_schedul_set);
        //机器组装B
        e_schedul_set.setLineName("机器组装B");
        e_schedul_setMapper.insertAll(e_schedul_set);
        *//*   夜班*//*
        e_schedul_set.setWorkStartTime(null);
        e_schedul_set.setWorkEndTime(null);
        e_schedul_set.setChangtime(nowTime);
        e_schedul_set.setWorkTime("");
        e_schedul_set.setStatus("不生产");
        e_schedul_set.setClassName("夜班");
        //主板预组装A
        e_schedul_set.setLineName("主板预组装A");
        e_schedul_setMapper.insertAll(e_schedul_set);
        //主板预组装B
        e_schedul_set.setLineName("主板预组装B");
        e_schedul_setMapper.insertAll(e_schedul_set);
        //后测
        e_schedul_set.setLineName("后测");
        e_schedul_setMapper.insertAll(e_schedul_set);
        //包装
        e_schedul_set.setLineName("包装");
        e_schedul_setMapper.insertAll(e_schedul_set);
        //机器组装A
        e_schedul_set.setLineName("机器组装A");
        e_schedul_setMapper.insertAll(e_schedul_set);
        //机器组装B
        e_schedul_set.setLineName("机器组装B");
        e_schedul_setMapper.insertAll(e_schedul_set);*/
    }

    /**
     * 設定OEE時間段值接口
     * 格式：
     * {"REQ":[{"REQ_DATA":
     * {"RECORDTIME":3,"DAYMOR1":"08:35:00","DAYMOR2":"11:35:00",
     * "DAYAFTER1":"13:35:00","DAYAFTER2":"17:30:00","DAYNIG1":"18:15:00",
     * "DAYNIG2":"20:30:00","NIGMOR1":"21:00:00","NIGMOR2":"23:30:00","NIGAFTER1":"00:30:00",
     * "NIGAFTER2":"05:30:00","NIGNIG1":"06:15:00","NIGNIG2":"08:30:00",
     * "MBAAD":"1","MBAAN":"1","MBABD":"1","MBABN":"1",
     * "TESTD":"1","TESTN":"1","PACKD":"1","PACKN":"1"}}]}
     */
    @PostMapping("/setoeetime")
    @ApiOperation(value = "OEE设置时间段和开班情况", notes = "更新时间段和开班情况")
    @ApiImplicitParam(name = "reqStr", value = "json字符串，需要的字段：CRATE", required = false, dataType = "String")
    @ApiResponses({@ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "设定失败")})
    public String setOEEClass(String reqStr) {
        JSONObject json = new JSONObject(reqStr);
        LOG.info("OEE时间段设定值为：" + json);
        String out = oeeService.OEETimeAClassSet(json);
        return out;
    }


    /**
     * 更新OEE時間线体开启情况
     * 格式：
     * {"REQ":[{"REQ_DATA":
     * {"RECORDTIME":3,"DAYMOR1":"08:35:00","DAYMOR2":"11:35:00",
     * "DAYAFTER1":"13:35:00","DAYAFTER2":"17:30:00","DAYNIG1":"18:15:00",
     * "DAYNIG2":"20:30:00","NIGMOR1":"21:00:00","NIGMOR2":"23:30:00","NIGAFTER1":"00:30:00",
     * "NIGAFTER2":"05:30:00","NIGNIG1":"06:15:00","NIGNIG2":"08:30:00"}}]}
     */
    @PostMapping("/updateoeeclass")
    @ApiOperation(value = "OEE设置时间段和开班情况", notes = "更新时间段和开班情况")
    @ApiImplicitParam(name = "reqStr", value = "json字符串，需要的字段：CRATE", required = false, dataType = "String")
    @ApiResponses({@ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "设定失败")})
    public void setOEETime() {


    }
}
