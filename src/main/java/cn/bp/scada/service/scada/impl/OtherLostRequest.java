package cn.bp.scada.service.scada.impl;

import cn.bp.scada.common.utils.PrimaryHelper;
import cn.bp.scada.common.utils.agv.OeeUtils;
import cn.bp.scada.common.utils.data.DateUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class OtherLostRequest {
    @Autowired
    private OeeUtils lostTime;
    @Autowired
    private DateUtils dateUtils;
    @Autowired
    private PrimaryHelper ph;

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Async
    public String requestLost(String lineName) {
        String factory = "邢村工厂";
        JSONArray array = new JSONArray();
        JSONObject str = new JSONObject();
        str.put("taskCode", "record"); //记录状态
        str.put("factory", factory); //工厂名称
        str.put("lineName", lineName); //线体名称
        str.put("class", ""); //班组名称
        str.put("currentTime", dateUtils.getTime()); //上传工站时间
        array.put(str);
        JSONObject js = new JSONObject();
        js.put("reqcode", "T"+ph.getIdFive());//唯一任务号，必填
        js.put("data", str);
        LOG.info("进入损失采集接口："+js.getString("reqcode"));
        String result = lostTime.doPostToOee("http://10.63.11.148:8090/api/otherRequest/autoProductionLine//lineName/stopInfoUpdate", js);
        LOG.info("任务号："+js.getString("reqcode")+"   返回结果："+result);
        return result;
/*“data”:{
“taskCode”:” record”, //record:正常统计；else:不进行统计“factory”:”xcgck1”,   //工厂名称(英文名称)
“class”:”D”,   //班组名称（D:白班/N:夜班）
 “lineName”:”cell1”,   //线体名称
“currentTime”:”101”,  // 上传当前工站时间(格式:yyyy-MM-dd HH:mm:ss)*/
    }

}
