package cn.bp.scada.service.scada.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;

import cn.bp.scada.common.utils.redis.RedisUtils;
import cn.bp.scada.modle.scada.*;
import cn.bp.scada.service.scada.ScadaToAgvSend;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Service;
import cn.bp.scada.controller.scada.NettyServerController;
import cn.bp.scada.controller.scada.ScadaAndAgvMt;
import cn.bp.scada.common.utils.agv.AgvUtils;
import cn.bp.scada.common.utils.MesApiUtils;

/*
 1.物料料箱到达下料点，上位机调用scada接口，通知scada到达
 2.收到料箱到达信息后，向agv发起取料请求任务
 3.AGV到达供料点后，需要与SCADA系统进行交互，此时，由SCADA系统提供接口供AGV调用
 4.收到agv到达供料点，调用上位机,告知上位机可以滚动
 5.agv收到料后，发送信息给scada, 由scada提供接口给agv调用
 6.scada调用上位机，告知agv收到料
  */
@Service
public class
ScadaToAgvSendImpl extends JdbcDaoSupport implements ScadaToAgvSend {
    @Resource
    public void setJb(JdbcTemplate jb) {
        super.setJdbcTemplate(jb);
    }

    @Resource
    private ScadaAndAgvMt sam;

    @Resource
    private AgvUtils agv;
    @Resource
    private NettyServerController netty;
    private Logger LOG = LoggerFactory.getLogger(this.getClass());
    @Resource
    private JdbcTemplate template;
    @Resource
    private MesApiUtils mesApiUtils;
    @Autowired
    private RedisUtils redisUtil;

    /*物料料箱到达下料点，创建自动产线供料、回收空料箱任务*/
    public ScadaResponShow matBoxDown(Object msg) throws JSONException, IOException {

        ScadaRequest sq = new ScadaRequest();
        ScadaResponShow sr = new ScadaResponShow();
        JSONObject downRequest = new JSONObject(msg.toString());

        String conSn = downRequest.get("con_sn").toString();
        ScadaAndAgvRespon sar = new ScadaAndAgvRespon();
        sar.setCode("0");
        JSONObject creJson = new JSONObject();

        String matPoint = downRequest.get("mat_point").toString(); // 下料点点位
        String moNum = downRequest.get("mo_num").toString(); // 如果是2，则通知可以上空框，如果是3，则告知不可上空框
        if (Integer.parseInt(moNum) == 2) {
            LOG.info("滚筒线发出可以上空框，修改数据库状态,点位为：" + matPoint);

            String sql = "update ET_IP_AGV set STATUS= 1 where MAT_POINT='" + matPoint + "' ";
            template.update(sql);

            //查询表里此点位是否有未完成滚动的AGV任务号
            sql = "SELECT TASKNO,AGV_POINT FROM ET_IP_AGV WHERE MAT_POINT='" + matPoint + "' ";
            Map<String, Object> objectMap = template.queryForMap(sql);
            if (objectMap.get("TASKNO") != null) {
                LOG.info("滚筒线有未完成滚动的AGV任务，先通知设备滚动，再通知AGV滚动回空框，点位：" + matPoint);

				sq.setOp_flag("Z05");
                sq.setMat_point(matPoint.toString());
                sq.setMo_num("2");
                String point = redisUtil.hget("matPoint", matPoint).toString();
                sq.setEt_ip(point);
                sam.toDevice(sq); //发送消息给设备

                JSONObject jsons = new JSONObject();
                jsons.put("taskCode", objectMap.get("TASKNO").toString()); //唯一任务号，必填
                jsons.put("currentWb", objectMap.get("AGV_POINT").toString()); //当前点位，必填
                jsons.put("nodeType", "3"); //1上料点，2下料点，3回收空框点，4空上箱点
                JSONObject jsonResult = sam.sayUpDown(jsons, 0);
                if (jsonResult.getString("code").equals("0")) {
                    sql = "update ET_IP_AGV set TASKNO= '' where MAT_POINT='" + matPoint + "' ";
                    template.update(sql);
                }
            }

            sr.setResult_flag("OK");
            sr.setFlow_code("1");
        } else if (Integer.parseInt(moNum) == 3) {
            LOG.info("滚筒线发出不能上空框，修改数据库状态,点位为：" + matPoint);
            String sql = "update ET_IP_AGV set STATUS= 0 where MAT_POINT='" + matPoint + "' ";
            template.update(sql);
            sr.setResult_flag("OK");
            sr.setFlow_code("1");
        } else { //如果mo_num不是指定的2，就是要接料

            // 写查询sql，查出对应的接料点位后，传给agv
            String sql = "select AGV_POINT from ET_IP_AGV where MAT_POINT='" + matPoint + "' ";

            Map<String, Object> queryForMap = this.getJdbcTemplate().queryForMap(sql);
            Object agvJie = queryForMap.get("AGV_POINT");//获取物料下料点，（agv对应的接料点）

            // 再根据载具SN查到供料点
            JSONObject reqDataDetail = new JSONObject();
            reqDataDetail.put("IFS", "ACT0050");
            reqDataDetail.put("IN_PSN_NO", conSn);//获取物料周转箱SN

            JSONObject responseData = mesApiUtils.doPost(reqDataDetail); //请求mes，mes返回agv点位给SCADA
            String gon = "";


            if (responseData.getString("STATUS").equals("1")) {
                gon = responseData.getString("LOCATIONID");

                sql = "SELECT EMPTY from ET_IP_AGV where AGV_POINT='" + gon + "' ";
                Map<String, Object> map = template.queryForMap(sql);
                gon = map.get("EMPTY").toString();

                //收到料箱到达信息后，向agv发起取料请求任务
                creJson.put("supplyLoadWb", agvJie); //接料点
                creJson.put("supplyUnLoadWb", gon); //供料点
                LOG.info("下料点料箱SN： " + conSn + ", 下料点为：" + matPoint + ", 供料点：" + gon);
                sar = sam.createProLine(creJson);
            } else {
                LOG.info("没有查询到料箱对应的AGV供料点,此接料點需要檢查，點位：" + matPoint);
            }


            //如果agv返回成功，说明已到达，则返回ok信息给上位机
            if (sar.getCode().equals("0")) {
                sr.setResult_flag("OK");
                sr.setFlow_code("1");
                sr.setResult_message("agv任务创建成功");
            } else {
                sr.setResult_flag("NG");
                sr.setFlow_code("1");
                sr.setResult_message("agv任务创建失败");
            }

        }


        return sr;
    }

    /*主板下线请求接料*/
    public ScadaRespon masterDown(Object msg) throws JSONException {
        JSONObject deviceJson = new JSONObject(msg.toString());
        ScadaRespon sr = new ScadaRespon();
        LOG.info("进入主板下线" + deviceJson);
        ScadaAndAgvRespon sar = new ScadaAndAgvRespon();
        sar.setCode("0");

        JSONObject js = new JSONObject();

        String message = "主板agv任务创建成功";
        // 1.叫AGV来主板下线口接料
        JSONObject creJson = new JSONObject();
        String sql = "";
        String moNum = deviceJson.get("mo_num").toString();

        switch (deviceJson.getString("mat_point")) {
            case "ECD101": // 一线主板请求接料
                creJson.put("supplyLoadWb", "CG0501"); //一线接料点，写死
                creJson.put("supplyUnLoadWb", "CG0104"); //一线供料点，写死
                sar = sam.createProLine(creJson); //主板下料机呼叫接料时候才调用AGV
                break;

            case "ECD102": //主板下料机通知可以回空框，去修改数据库状态

                if (moNum.equals("1")) {
                    LOG.info("一线主板下料机通知可以回空框，去修改数据库状态");
                    sql = "update ET_IP_AGV set STATUS= 3 where MAT_POINT='" + deviceJson.getString("mat_point") + "' ";
                    message = "收到一线主板下料机可以回空框的通知";
                    template.update(sql);
                    //查询存储表有没有空框存储
                    String queSql = "SELECT STATUS FROM ET_IP_AGV WHERE ET_IP='191' ";
                    Map<String, Object> map = template.queryForMap(queSql);
                    if (map.get("STATUS").toString().equals("11")) {

                        String deSql = "UPDATE ET_IP_AGV SET STATUS=0 WHERE ET_IP='191'";
                        template.update(deSql);
                        String hSql = "update ET_IP_AGV set STATUS= 0 where MAT_POINT='ECD102' ";
                        template.update(hSql);

                        LOG.info("存储表有空框,一线主板回空框任务已创建");
                        js.put("srcWbCode", "CG0104"); //空料箱回收上箱点编码(起点)
                        js.put("targetEmptyRecyleWb", "CG0502");//空框回收下箱点(给我们送空箱的点)*/
                        js.put("isSta", "0");
                        sam.createEmpty(js);
                    }


                } else {
                    LOG.info("一线主板下料机通知不能回空框，去修改数据库状态");
                    sql = "update ET_IP_AGV set STATUS= 0 where MAT_POINT='" + deviceJson.getString("mat_point") + "' ";
                    message = "收到一线主板下料机不能回空框的通知";
                    template.update(sql);
                }


                break;

            case "ECD201":  //二线主板下料机通知可以回空框，去修改数据库状态
                if (moNum.equals("1")) {
                    LOG.info("二线主板下料机通知可以回空框，去修改数据库状态");
                    sql = "update ET_IP_AGV set STATUS= 3 where MAT_POINT='" + deviceJson.getString("mat_point") + "' ";
                    message = "收到二线主板下料机可以回空框的通知";
                    template.update(sql);
                    //查询存储表有没有空框存储
                    String queSql = "SELECT STATUS FROM ET_IP_AGV WHERE ET_IP='192' ";
                    Map<String, Object> map = template.queryForMap(queSql);
                    if (map.get("STATUS").toString().equals("12")) {

                        String deSql = "UPDATE ET_IP_AGV SET STATUS=0 WHERE ET_IP='192'";
                        template.update(deSql);
                        String hSql = "update ET_IP_AGV set STATUS= 0 where MAT_POINT='ECD201' ";
                        template.update(hSql);

                        LOG.info("存储表有空框,二线主板回空框任务已创建");
                        js.put("srcWbCode", "CG0204"); //空料箱回收上箱点编码(起点)
                        js.put("targetEmptyRecyleWb", "CG0601");//空框回收下箱点(给我们送空箱的点)*/
                        js.put("isSta", "0");
                        sam.createEmpty(js);
                    }


                } else {
                    LOG.info("二线主板下料机通知不能回空框，去修改数据库状态");
                    sql = "update ET_IP_AGV set STATUS= 0 where MAT_POINT='" + deviceJson.getString("mat_point") + "' ";
                    message = "收到二线主板下料机不能回空框的通知";
                    template.update(sql);
                }

                break;

            default: // 二线主板请求接料
                creJson.put("supplyLoadWb", "CG0602"); //二线接料点，写死
                creJson.put("supplyUnLoadWb", "CG0204"); //二线供料点，写死
                sar = sam.createProLine(creJson); //主板下料机呼叫接料时候才调用AGV
                break;
        }
        //如果agv返回成功，说明已到达，则返回ok信息给上位机
        if (sar.getCode().equals("0")) {
            sr.setResult_flag("OK");
            sr.setFlow_code(deviceJson.getString("mat_point"));
            sr.setResult_message(message);

        } else {
            sr.setResult_flag("NG");
            sr.setFlow_code(deviceJson.getString("mat_point"));
            sr.setResult_message("主板agv任务创建失败");
        }

        return sr;
    }


    /*机箱下侧盖请求接料*/
    public ScadaRespon crateDown(Object msg) throws JSONException {
        JSONObject deviceJson = new JSONObject(msg.toString());
        ScadaRespon sr = new ScadaRespon();

        // 1.叫AGV来机箱下料机接料
        JSONObject creJson = new JSONObject();
        ScadaAndAgvRespon sar = new ScadaAndAgvRespon();
        String moNum = deviceJson.get("mo_num").toString();
        String sql = "";

        if (deviceJson.getString("mat_point").equals("ECD103")) { //一线机箱接料和接空框
            switch (moNum) {
                case "1":  //机箱下料
                    LOG.info("一线机箱请求下料");
                    creJson.put("supplyLoadWb", "CG0102"); //接料点，写死
                    creJson.put("supplyUnLoadWb", "CG0112"); //供料点，写死
                    sar = sam.createProLine(creJson);
                    break;

                case "3": //一线机箱通知不能回空框
                    LOG.info("一线机箱下料机通知不能回空框，去修改数据库状态");
                    break;

                default: //机箱回空框一线
                    LOG.info("一线机箱下料机通知可以回空框，数据库状态修改为4");
                    sar.setCode("0");
                    sql = "update ET_IP_AGV set STATUS= 4 where MAT_POINT='" + deviceJson.getString("mat_point") + "' ";
                    template.update(sql);
                    break;
            }


        } else { //二线机箱接料和接空框
            switch (moNum) {
                case "1":  //机箱下料
                    LOG.info("二线机箱请求下料");
                    creJson.put("supplyLoadWb", "CG0202"); //接料点，写死
                    creJson.put("supplyUnLoadWb", "CG0212"); //供料点，写死
                    sar = sam.createProLine(creJson);
                    break;

                case "3": //二线机箱通知不能回空框
                    LOG.info("二线机箱下料机通知不能回空框，去修改数据库状态");
                    break;

                default: //机箱回空框二线
                    LOG.info("二线机箱下料机通知可以回空框，数据库状态改为4");
                    sar.setCode("0");
                    sql = "update ET_IP_AGV set STATUS= 4 where MAT_POINT='" + deviceJson.getString("mat_point") + "' ";
                    template.update(sql);
                    break;
            }
        }
        //如果agv返回成功，说明已到达，则返回ok信息给上位机
        if (sar.getCode().equals("0")) {
            sr.setResult_flag("OK");
            sr.setFlow_code("1");
            sr.setResult_message("agv任务创建成功");

        } else {
            sr.setResult_flag("NG");
            sr.setFlow_code("1");
            sr.setResult_message("agv任务创建失败");
        }

        return sr;
    }

    /**
     * scada发送给设备入参公共方法
     *
     * @param sq
     * @return
     */
    public String commonRequestDevice(ScadaRequest sq) {
        Object obj = null;
        String resultDevice = null;
        ResultMessage resultMessage = new ResultMessage();

        obj = sq;
        resultMessage.setAnswerId("A001");
        resultMessage.setObj(obj);
        resultMessage.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        if (sq.getOp_flag().equals("S011")) {
            resultMessage.setType("ScadaRespon_XIDIAOYIZAI");
        } else if (sq.getOp_flag().equals("S013") && !"PQC".equals(sq.getDevice_sn())) { //一楼分拣触发放行
            resultMessage.setType("ScadaRespon_SortLine");
        } else if ("PQC".equals(sq.getDevice_sn())) {
            resultMessage.setType("ScadaRespon"); //PQC触发放行
        } else if ("B104".equals(sq.getOp_flag()) || "B103".equals(sq.getOp_flag()) || "C06".equals(sq.getOp_flag())
                || "C07".equals(sq.getOp_flag()) || "C08".equals(sq.getOp_flag()) ) {
            resultMessage.setType("ScadaQuery"); //包装
        } else if ("S022".equals(sq.getOp_flag()) || "C09".equals(sq.getOp_flag()) ) {
            resultMessage.setType("ScadaQuery_AgvStatus"); //前测吸吊移栽通知老化架到达或离开
        } else {
            resultMessage.setType("AGV_Scada_Respon");
        }

        try {
            resultDevice = new ObjectMapper().writeValueAsString(resultMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultDevice;
    }
}
