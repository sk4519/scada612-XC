package cn.bp.scada.service.scada.impl;

import cn.bp.scada.common.DynamicTask;
import cn.bp.scada.common.utils.data.DateUtils;
import cn.bp.scada.mapper.mes.E_CLASS_SELECTMapper;
import cn.bp.scada.mapper.mes.E_TIME_SETMapper;
import cn.bp.scada.modle.mes.E_CLASS_SELECT;
import cn.bp.scada.modle.mes.E_TIME_SET;
import cn.bp.scada.service.scada.OEEService;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 前端访问数据模板格式
 * {"REQ":[{"REQ_DATA":
 * {"RECORDTIME":3,"DAYMOR1":"08:35:00","DAYMOR2":"11:35:00",
 * "DAYAFTER1":"13:35:00","DAYAFTER2":"17:30:00","DAYNIG1":"18:15:00",
 * "DAYNIG2":"20:30:00","NIGMOR1":"21:00:00","NIGMOR2":"23:30:00","NIGAFTER1":"00:30:00",
 * "NIGAFTER2":"05:30:00","NIGNIG1":"06:15:00","NIGNIG2":"08:30:00"}}]}
 */
@Service
public class OEEServicempl implements OEEService {
    @Autowired
    private E_TIME_SET e_time_set;
    @Autowired
    private E_TIME_SETMapper e_time_setMapper;
    @Autowired
    private DateUtils dateUtils;
    @Autowired
    private DynamicTask dynamicTask;
    @Autowired
    private E_CLASS_SELECTMapper e_class_selectMapper;
    @Autowired
    private E_CLASS_SELECT e_class_select;


    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Override
    public String OEETimeSet(JSONObject json) {

        JSONObject js = new JSONObject();
        JSONObject reqDataDetail1 = new JSONObject();

        try {
            JSONObject jsonMes = json.getJSONArray("REQ").getJSONObject(0).getJSONObject("REQ_DATA");
            System.out.println("OEE时间设定收到的数据:" + json.toString());

            /*获取Json数据具体内容插入数据库*/
            e_time_set.setRecordTime(dateUtils.getNowDate());
            e_time_set.setStopRecordTimeSet((short) jsonMes.getInt("RECORDTIME"));
            long j = (short) jsonMes.getInt("RECORDTIME");
            //白班数据
            e_time_set.setDayMorning1(jsonMes.getString("DAYMOR1"));
            e_time_set.setDayMorning2(jsonMes.getString("DAYMOR2"));
            e_time_set.setDayAfternoon1(jsonMes.getString("DAYAFTER1"));
            e_time_set.setDayAfternoon2(jsonMes.getString("DAYAFTER2"));
            e_time_set.setDayNight1(jsonMes.getString("DAYNIG1"));
            e_time_set.setDayNight2(jsonMes.getString("DAYNIG2"));
            //夜班数据
            e_time_set.setNightMorning1(jsonMes.getString("NIGMOR1"));
            e_time_set.setNightMorning2(jsonMes.getString("NIGMOR2"));
            e_time_set.setNightAfternoon1(jsonMes.getString("NIGAFTER1"));
            e_time_set.setNightAfternoon2(jsonMes.getString("NIGAFTER2"));
            e_time_set.setNightNight1(jsonMes.getString("NIGNIG1"));
            e_time_set.setNightNight2(jsonMes.getString("NIGNIG2"));
            //插入数据
            e_time_setMapper.insert(e_time_set);
            e_class_selectMapper.insertallline();//插入帶入的數據

            //更新主板预组装A 白班 状态
            e_class_select.setClassType("白班");
            e_class_select.setMachineid("ECDLC005");
            e_class_select.setSelectStatus(Short.valueOf(jsonMes.getString("MBAAD")));
            e_class_selectMapper.updateStatus(e_class_select);

            //更新主板预组装A 夜班 状态
            e_class_select.setClassType("夜班");
            e_class_select.setMachineid("ECDLC005");
            e_class_select.setSelectStatus(Short.valueOf(jsonMes.getString("MBAAN")));
            e_class_selectMapper.updateStatus(e_class_select);

            //更新主板预组装B 白班 状态
            e_class_select.setClassType("白班");
            e_class_select.setMachineid("ECDLC205");
            e_class_select.setSelectStatus(Short.valueOf(jsonMes.getString("MBABD")));
            e_class_selectMapper.updateStatus(e_class_select);

            //更新主板预组装B 夜班 状态
            e_class_select.setClassType("夜班");
            e_class_select.setMachineid("ECDLC205");
            e_class_select.setSelectStatus(Short.valueOf(jsonMes.getString("MBABN")));
            e_class_selectMapper.updateStatus(e_class_select);

            //更新后测 白班 状态
            e_class_select.setClassType("白班");
            e_class_select.setMachineid("ECDLC034");
            e_class_select.setSelectStatus(Short.valueOf(jsonMes.getString("TESTD")));
            e_class_selectMapper.updateStatus(e_class_select);

            //更新后测 夜班 状态
            e_class_select.setClassType("夜班");
            e_class_select.setMachineid("ECDLC034");
            e_class_select.setSelectStatus(Short.valueOf(jsonMes.getString("TESTN")));
            e_class_selectMapper.updateStatus(e_class_select);

            //更新包装 白班 状态
            e_class_select.setClassType("白班");
            e_class_select.setMachineid("ECDLC026");
            e_class_select.setSelectStatus(Short.valueOf(jsonMes.getString("PACKD")));
            e_class_selectMapper.updateStatus(e_class_select);

            //更新包装 夜班 状态
            e_class_select.setClassType("夜班");
            e_class_select.setMachineid("ECDLC026");
            e_class_select.setSelectStatus(Short.valueOf(jsonMes.getString("PACKN")));
            e_class_selectMapper.updateStatus(e_class_select);

            js.put("CODE", "0");
            js.put("MSG", "接口正常，插入信息成功");
            LOG.info("插入e_time_set成功，插入e_class_select成功");


            //重启当时任务
            dynamicTask.stopCron();
            dynamicTask.startCron();
            LOG.info("重新启动定时任务");
        } catch (JSONException e) {
            e.printStackTrace();
            LOG.info("更新e_time_set數據失敗");
        }
        return js.toString();
    }
    @Override
    public String OEETimeAClassSet(JSONObject json){
        JSONObject js = new JSONObject();
        JSONObject reqDataDetail1 = new JSONObject();
        JSONObject jsonMes = json.getJSONArray("REQ").getJSONObject(0).getJSONObject("REQ_DATA");
        System.out.println("OEE时间设定收到的数据:" + json.toString());
        //重启当时任务
        dynamicTask.stopCron();
        dynamicTask.startCron();
        LOG.info("重新启动定时任务");
        return js.toString();
    }
}