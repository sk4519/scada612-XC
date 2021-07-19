package cn.bp.scada.controller.scada;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import cn.bp.scada.common.utils.redis.RedisUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.bp.scada.modle.scada.ScadaAndAgvRespon;
import cn.bp.scada.modle.scada.ScadaQuery;
import cn.bp.scada.modle.scada.ScadaRequest;
import cn.bp.scada.service.scada.impl.ScadaToAgvSendImpl;
import cn.bp.scada.common.utils.agv.AgvUtils;
import cn.bp.scada.common.utils.JsonHelper;
import cn.bp.scada.common.utils.MesApiUtils;
import cn.bp.scada.common.utils.PrimaryHelper;

/**
 * scada与agv交互
 *
 * @author Administrator
 */
@Api(value = "AGV控制层", tags = {"与AGV交互接口"})
@RestController
@RequestMapping("/api/wisdom/autoProductionLine")
public class ScadaAndAgvMt extends JdbcDaoSupport {
    @Resource
    public void setJb(JdbcTemplate jb) {
        super.setJdbcTemplate(jb);
    }

    @Resource
    private JsonHelper agvJson;
    @Resource
    private NettyServerController netty;
    @Resource
    private ScadaToAgvSendImpl sp;
    @Resource
    private AgvUtils agv;
    @Resource
    private PrimaryHelper ph;
    @Resource
    private MesApiUtils mesApiUtils;
    @Resource
    private JsonHelper jsons;
    @Resource
    private JdbcTemplate template;
    @Autowired
    private RedisUtils redisUtil;

    public ScadaRequest json;

    public ScadaRequest getJson() {
        return json;
    }

    public void setJson(ScadaRequest json) {
        this.json = json;
    }

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    private int te = 0; //用来判断给AGV返回失败或成功
    private static Map<String, Object> map = new HashMap<String, Object>();

    static {
        map.put("ZB1", "一线主板下料点通知设备滚动");
        map.put("ZB2", "二线主板下料点通知设备滚动");
        map.put("JX1", "一线机箱下料点通知设备滚动");
        map.put("JX2", "二线机箱下料点通知设备滚动");
        map.put("FJ", "滚筒线接料点通知设备滚动，点位：");
        map.put("CD1", "一线通知光驱接料，AGV到位");
        map.put("CD2", "二线通知光驱接料，AGV到位");
        map.put("HD1", "一线通知硬盘接料，AGV到位");
        map.put("HD2", "二线通知硬盘接料，AGV到位");
    }

    /**
     * 1.创建自动产线供料任务,提 供方：agv
     *
     * @param
     * @return
     */
    @RequestMapping("/supplyAndRecyle/supplyAndRecyleCreate")
    public ScadaAndAgvRespon createProLine(JSONObject json) {
        ScadaAndAgvRespon srs = new ScadaAndAgvRespon();
        try {
            JSONArray array = new JSONArray();
            JSONObject str = new JSONObject();
            str.put("taskCode", ph.getId()); //唯一任务号，必填
            str.put("supplyLoadWb", json.getString("supplyLoadWb")); //接料点，必填
            str.put("supplyUnLoadWb", json.getString("supplyUnLoadWb")); //供料点，必填
            str.put("taskPri", "urgent"); //优先级—紧急（urgent）、普通（normal）必填项
            array.put(str);
            JSONObject js = new JSONObject();
            js.put("reqcode", ph.getIdFive());
            js.put("data", array);

            LOG.info("发送给AGV的接料点 ：" + json.getString("supplyLoadWb") + ",供料点：" + json.getString("supplyUnLoadWb"));
            String doPost = agv.doPostToAgv("http://10.50.4.11:8888/api/wisdom/autoProductionLine/supplyAndRecyle/supplyAndRecyleCreate", js);
            JSONObject agvJsonResult = new JSONObject(doPost);

            LOG.info("产线AGV返回数据 ：" + agvJsonResult);
            srs.setCode(agvJsonResult.getString("code"));
            srs.setMessage(agvJsonResult.getString("message"));
            srs.setReqcode(agvJsonResult.getString("reqCode"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return srs;
    }

    /**
     * 2.任务AGV节点变更通知,提供方：scada
     *
     * @param
     * @return
     * @throws JSONException
     */
    @RequestMapping(value = "/supplyAndRecyle/agvProcessNotify", method = RequestMethod.POST)
    @ApiOperation("AGV任务节点变更接口")
    @ApiImplicitParam(name = "request", value = "json数据", required = false)
    public String taskUpda(HttpServletRequest request) {

        JSONObject js = agvJson.getJSONParam(request); //接收agv发送的数据

        JSONObject jsonAgv = new JSONObject();//发送给agv,告诉agv滚动
        JSONObject reAgv = new JSONObject(); //接收AGV滚动接口返回的数据

        JSONObject jsonResult = returnAgv(1); //返回信息给agv,1是成功

        JSONObject jsonClient = new JSONObject(); //接收设备的返回信息

        String commonSql;
        try {
            JSONObject jsn = js.getJSONObject("data");

            ScadaRequest sq = new ScadaRequest();

            //根据AGV点位查出对应的设备点位与上位机IP
            String agvPoint = jsn.getString("currentWb"); //agv点位
            String sql = "select ET_IP,MAT_POINT from ET_IP_AGV where AGV_POINT='" + agvPoint + "' ";
            Map<String, Object> queryForMap = this.getJdbcTemplate().queryForMap(sql);
            Object ET_IP = queryForMap.get("ET_IP"); //设备IP
            Object MAT_POINT = queryForMap.get("MAT_POINT"); //设备点位
            switch (jsn.getString("taskSta")) {

                case "1":  //agv接料点到位
                    LOG.info("agv接料点到位,点位：" + agvPoint);
                    //scada通知agv可以动
                    jsonAgv.put("currentWb", agvPoint); //当前点位
                    jsonAgv.put("nodeType", "1");//1对于scada代表下料点，对于agv就是上料点
                    jsonAgv.put("taskCode", jsn.getString("taskCode")); //任务号

                    int u = 0; //3的时候根据此标识判断是否有空箱，这里接料点到位不判断，写死0
                    reAgv = sayUpDown(jsonAgv, u);
                    LOG.info("AGV接料点到位滚动返回给SCADA的数据：" + reAgv);
                    //如果AGV滚动返回成功，则通知设备滚动


                    if (reAgv.getString("code").equals("0")) {
                        Thread.sleep(1000);
                        switch (MAT_POINT.toString()) {
                            case "ECD101":  // 如果当前设备点位是主板下料点，操作码
                                sq.setOp_flag("B23");
                                LOG.info(map.get("ZB1").toString());
                                sq.setResult_message(map.get("ZB1").toString());
                                break;

                            case "ECD202":  // 如果当前设备点位是主板下料点二线，操作码
                                sq.setOp_flag("B23");
                                LOG.info(map.get("ZB2").toString());
                                sq.setResult_message(map.get("ZB2").toString());
                                break;

                            case "ECD103":  // 如果当前设备点位是机箱下料点，操作码
                                sq.setOp_flag("B26");
                                LOG.info(map.get("JX1").toString());
                                sq.setResult_message(map.get("JX1").toString());
                                break;

                            case "ECD203":  // 如果当前设备点位是机箱下料点二线，操作码
                                sq.setOp_flag("B26");
                                LOG.info(map.get("JX2").toString());
                                sq.setResult_message(map.get("JX2").toString());
                                break;

                            default:
                                sq.setOp_flag("B20"); // 否则就是滚筒线接料点
                                LOG.info(map.get("FJ").toString() + MAT_POINT);
                                sq.setResult_message(map.get("FJ").toString());
                                break;
                        }

                        sq.setMat_point(MAT_POINT.toString());
                        sq.setMo_num("1");
                        sq.setEt_ip(ET_IP.toString());
                        jsonClient = toDevice(sq); //接收到设备返回的信息
                        LOG.info("通知设备滚，设备返回的信息：" + jsonClient);

                    } else {
                        LOG.info("接料点AGV没有返回code是0，失败");
                    }

                    break;

                case "2": //接料点离开
                    LOG.info("收到AGV接料点离开的数据：" + jsn);
                    jsonResult = returnAgv(1);

                    break;

                case "3": //供料点接货到位
                    //第一次供料没有空料箱，第二次开始就要设备传过来一个字段，根据这个字段判断是否有空料箱，
                    //有的话agv就同时要接空箱和接料
                    //通知设备可以滚动s
                    LOG.info("AGV供料点到位，点位：" + agvPoint + ",任务号为：" + jsn.getString("taskCode"));
                    u = 0;
                    //设置AGV的入参，通知AGV滚动
                    jsonAgv.put("currentWb", jsn.getString("currentWb"));
                    jsonAgv.put("nodeType", "2");//2代表下料点，对于agv就是下料
                    jsonAgv.put("taskCode", jsn.getString("taskCode"));

                    //如果是光驱和硬盘供料点到位，就要设备滚动接料
                    if (MAT_POINT.equals("ECD106") || MAT_POINT.equals("ECD107") || MAT_POINT.equals("ECD206") || MAT_POINT.equals("ECD207")) {
                        sq.setOp_flag("B28");
                        sq.setMat_point(MAT_POINT.toString());
                        sq.setMo_num("1");
                        sq.setEt_ip(ET_IP.toString());

                        switch (MAT_POINT.toString()) {
                            case "ECD106": //光驱
                                LOG.info(map.get("CD1").toString());
                                sq.setResult_message(map.get("CD1").toString());
                                break;
                            case "ECD107": //硬盘
                                LOG.info(map.get("HD1").toString());
                                sq.setResult_message(map.get("HD1").toString());
                                break;
                            case "ECD206": //二线光驱
                                LOG.info(map.get("CD2").toString());
                                sq.setResult_message(map.get("CD2").toString());
                                break;
                            default: //二线硬盘
                                LOG.info(map.get("HD2").toString());
                                sq.setResult_message(map.get("HD2").toString());
                                break;
                        }
                        //询问光驱硬盘是否可以上料
                         boolean rollToAgv = isRollToAgv(MAT_POINT.toString(),"1", jsn.getString("taskCode"));
                        if(rollToAgv) {
                        jsonClient = toDevice(sq); //接收到设备返回的信息
                        switch (jsonClient.getString("result_flag")) {
                            case "OK": //上位机返回ok，代表设备已经滚动，scada通知agv可以动
                                reAgv = sayUpDown(jsonAgv, u); //通知AGV滚动
                                jsonResult = returnAgv(1);//返回给agv
                                break;

                            default:
                                LOG.info("上位机没有应答，检查上位机程序，直接通知AGV滚动");
                                jsonResult = returnAgv(1);//返回给agv信息
                                reAgv = sayUpDown(jsonAgv, u); //通知AGV滚动
                                break;
                        }
                        }

                    } else {
                        //不是光驱和硬盘，询问是否可以进料
                        boolean rollToAgv = isRollToAgv(MAT_POINT.toString(), "1", jsn.getString("taskCode"));
                        if (rollToAgv) { //YES，可以滚动
                            reAgv = sayUpDown(jsonAgv, u); //直接通知AGV滚动
                            if (reAgv.getString("code").equals("0")) {
                                LOG.info("AGV已经滚动");
                            } else {
                                LOG.info("AGV没有滚动，点位：" + agvPoint);
                            }
                        }

                    }


                    break;

                case "4": //供料点接货离开
                    LOG.info("收到AGV供料点离开的数据：" + jsn);
                    //scada收到agv离开的信息
                    jsonResult = returnAgv(1);//返回给agv
                    break;

                case "5": //到达空料箱上箱点
                    LOG.info("AGV到达空料箱上箱点（上到AGV车上），点位：" + agvPoint);
                    String emptySql = "";

                    jsonAgv.put("currentWb", jsn.getString("currentWb"));
                    jsonAgv.put("nodeType", "4");//4代表AGV上空箱点
                    jsonAgv.put("taskCode", jsn.getString("taskCode"));

                    int qu = 0; //根据此标识判断是否有空箱,此处写死

                    reAgv = sayUpDown(jsonAgv, qu);
                    LOG.info("AGV到达空料箱上箱点，通知AGV滚动，返回数据：" + reAgv);

                    //通知设备可以滚动s,通过AGV点位查出对应的设备IP
                    sq.setOp_flag("Z03");
                    switch (MAT_POINT.toString()) {

                        case "ECD105":  // 如果当前设备点位是主板接空框点，操作码
                            LOG.info("一线主板接空框点通知设备滚动");

                            break;

                        case "ECD113":  // 如果当前设备点位是机箱接空框点，操作码
                            LOG.info("一线机箱接空框点通知设备滚动");
                            emptySql = "update ET_IP_AGV set STATUS= 0 where MAT_POINT='ECD103' ";
                            template.update(emptySql);
                            break;

                        case "ECD205":  // 如果当前设备点位是主板接空框点，操作码
                            LOG.info("二线主板接空框点通知设备滚动");

                            break;

                        case "ECD213":  // 如果当前设备点位是机箱接空框点，操作码
                            LOG.info("二线机箱接空框点通知设备滚动");
                            emptySql = "update ET_IP_AGV set STATUS= 0 where MAT_POINT='ECD203' ";
                            template.update(emptySql);
                            break;

                        default:
                            LOG.info("其他接空框点通知设备滚动");// 否则就是其他接空框点

                            break;
                    }
                    sq.setEt_ip(ET_IP.toString());
                    sq.setMo_num("2");
                    sq.setMat_point(MAT_POINT.toString());
                    sq.setEt_ip(ET_IP.toString());
                    jsonClient = toDevice(sq); //接收到设备返回的信息
                    try {
                        if (jsonClient.getString("result_flag").equals("OK")) {
                            LOG.info("空料箱上箱点（上到AGV车上），上位机返回OK,返回的AGV点位为：" + agvPoint);
                        }
                    } catch (Exception e) {
                        LOG.info("空料箱上箱点（上到AGV车上），上位机没有返回，点位：" + agvPoint);
                    }
                    jsonResult = returnAgv(1);//返回给agv
                    break;

                case "6": //离开空料箱上箱收点
                    LOG.info("收到AGV空料箱上箱收点离开的数据：" + jsn);
                    //scada收到agv离开的信息
                    jsonResult = returnAgv(1);//返回给agv

                    break;

                case "7": //到达空料箱回收点（回到一楼（滚筒线）点或者回到主板与机箱的接框点）
                    //通知设备可以滚动
                    //根据AGV点位查出对应的设备点位与上位机IP
                    LOG.info("收到AGV到达空料箱回收点（供空框点）,任务号为：" + jsn.getString("taskCode"));
                    //查出此点位的PLC状态，是否可以上空框
                    boolean rollToAgv = isRollToAgv(MAT_POINT.toString(), "1", jsn.getString("taskCode"));
                    //判断如果是主板和机箱回空框，上位机固定返回YES
                    if ( MAT_POINT.toString().equals("ECD103")  || MAT_POINT.toString().equals("ECD203")) {
                        rollToAgv = true;
                    }
                    if (rollToAgv) { //设备正常，可以滚动

                        switch (MAT_POINT.toString()) {
                            case "ECD102":  // 如果当前设备点位是主板回收空框点，操作码
                                sq.setOp_flag("B23");
                                LOG.info("一线主板回收空框点通知设备滚动");
                                commonSql = "update ET_IP_AGV set STATUS= 0 where MAT_POINT='" + MAT_POINT.toString() + "' ";
                                template.update(commonSql);
                                break;

                            case "ECD103":  // 如果当前设备点位是机箱回收空框点，操作码
                                sq.setOp_flag("B26");
                                LOG.info("机箱回收空框点通知设备滚动");
                                break;

                            case "ECD201":  // 如果当前设备点位是主板回收空框点，操作码
                                sq.setOp_flag("B23");
                                LOG.info("二线主板回收空框点通知设备滚动");
                                commonSql = "update ET_IP_AGV set STATUS= 0 where MAT_POINT='" + MAT_POINT.toString() + "' ";
                                template.update(commonSql);
                                break;

                            case "ECD203":  // 如果当前设备点位是机箱回收空框点，操作码
                                sq.setOp_flag("B26");
                                LOG.info("二线机箱回收空框点通知设备滚动");
                                break;

                            default:
                                sq.setOp_flag("Z05"); // 滚筒线回收空框点通知设备滚动
                                LOG.info("滚筒线回收空框点通知设备滚动");
                                String Bsql = "update ET_IP_AGV set STATUS= 0 where MAT_POINT='" + MAT_POINT.toString() + "' ";
                                template.update(Bsql);
                                break;
                        }

                        sq.setMat_point(MAT_POINT.toString());
                        sq.setMo_num("2");
                        sq.setEt_ip(ET_IP.toString());
                        jsonClient = toDevice(sq); //接收到设备返回的信息

                        jsonAgv.put("currentWb", jsn.getString("currentWb"));
                        jsonAgv.put("nodeType", "3");//3代表回收空框点
                        jsonAgv.put("taskCode", jsn.getString("taskCode"));

                        //上位机已经通知设备滚动，此时调用agv，告知滚动
                        int u1 = 0;
                        reAgv = sayUpDown(jsonAgv, u1);
                        LOG.info("回收空框点通知AGV滚动，返回的数据" + reAgv);
                        te = 1;


                        if (jsonClient.getString("result_flag").equals("OK")) {
                            LOG.info("回收空框点（到一楼）或主板机箱，上位机返回OK");
                        } else {
                            LOG.info("回收空框点（到一楼）或主板机箱，上位机无响应");
                        }
                    }

                    jsonResult = returnAgv(1);//返回给agv
                    break;

                case "8": //离开空料箱回收点

                    //scada已经收到agv离开的信息
                    jsonResult = returnAgv(1);//返回给agv
                    LOG.info("SCADA收到AGV离开空料箱回收点，返回给AGV数据：" + jsonResult);
                    break;

                default:
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
        LOG.info("任务节点变更返回给agv数据：" + jsonResult.toString());
        return jsonResult.toString();
    }

    @RequestMapping("/handCallAgv")
    public void sayAgv(String taskCode,String currentWb,String nodeType) throws JSONException {
        LOG.info("手动通知AGV滚动");

        JSONObject jsn = new JSONObject();
        jsn.put("taskCode",taskCode);
        jsn.put("currentWb",currentWb);
        jsn.put("nodeType",nodeType);
        sayUpDown(jsn, 0);
    }


    /**
     * 3.SCADA通知上料点、下料点、回收点滚动开始信息,提 供方：agv
     *
     * @param msg
     * @return
     */
    @RequestMapping("/supplyAndRecyle/startSupllyAndRecyles")
    public JSONObject sayUpDown(JSONObject json, int u) {
        JSONObject agvJsonResult = null;
        String point = "";
        try {
            JSONObject str = new JSONObject();

            str.put("taskCode", json.getString("taskCode")); //唯一任务号，必填
            str.put("currentWb", json.getString("currentWb")); //当前点位，必填
            str.put("nodeType", json.getString("nodeType")); //1上料点，2下料点，3回收空框点，4空上箱点

            JSONObject js = new JSONObject();
            js.put("reqcode", ph.getIdFive()); //请求编号,可自己生成
            js.put("data", str);
            LOG.info("nedetype为："+json.getString("nodeType"));
            switch (json.getString("nodeType")) {

                case "1":
                    point = "上料点";
                    LOG.info("上料点通知AGV滚动,上料点位 ：" + json.getString("currentWb")+"  请求参数为+reqcode:"+js);
                    break;
                case "2":
                    point = "下料点";
                    LOG.info("下料点通知AGV滚动,下料点位 ：" + json.getString("currentWb")+"   请求参数为+reqcode:"+js);
                    break;
                case "3":
                    point = "回收点";
                    LOG.info("回收空框点通知AGV滚动,回收点位 ：" + json.getString("currentWb")+"   请求参数为+reqcode:"+js);
                    break;
                default:
                    point = "上空箱点（上到AGV车上）";
                    LOG.info("上空箱点通知AGV滚动,上空箱（上到AGV车上）点位 ：" + json.getString("currentWb")+"   请求参数为+reqcode:"+js);
                    break;
            }

            String doPost = agv.doPostToAgv("http://10.50.4.11:8888/api/wisdom/autoProductionLine/supplyAndRecyle/startSupllyAndRecyles", js);

            agvJsonResult = new JSONObject(doPost);
            LOG.info(point + ",通知agv滚动接口返回：" + agvJsonResult);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return agvJsonResult;
    }

    /**
     * 4.SCADA上报已下料数量及已接收空框数量,提 供方：agv
     *
     * @param msg
     * @return
     */

    public String sayDownEmpty(JSONObject json, int u) {
        String result = "";
        try {
            JSONObject str = new JSONObject();
            str.put("taskCode", json.getString("taskCode")); //唯一任务号，必填
            str.put("supplyUnLoadNum", "1"); //下料数量，选填
            str.put("supplyUnLoadWb", json.getString("supplyUnLoadWb")); //接料点，必填

            if (u == 1) { //如果供料点有空框，把空框数量也传给AGV
                str.put("emptyRecyleNum", "2"); //空框回收数量，选填项
            }

            JSONObject js = new JSONObject();
            js.put("reqcode", ph.getIdFive()); //请求编号,可自己生成
            js.put("data", str);

            LOG.info("SCADA上报已下料数量及已接收空框数量,发送给AGV的数据 ：" + js);
            String doPost = agv.doPostToAgv("http://27.223.104.6:8888/api/wisdom/autoProductionLine/supplyAndRecyle/supllyAndRecyleResult", js);
            JSONObject agvJsonResult = new JSONObject(doPost);
            LOG.info("SCADA上报已下料数量及已接收空框数量,agv返回：" + agvJsonResult.getString("message"));
            result = agvJsonResult.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 5.iWCS接料点上报已接收供料信息,提供方：scada
     *
     * @param msg
     * @return
     * @throws JSONException
     */
    @RequestMapping("/supplyAndRecyle/supllyUnload/SupllyAndRecyleInfo")
    public String iWCSUp(HttpServletRequest request) {

        JSONObject js = new JSONObject();
        js = agvJson.getJSONParam(request);

        JSONObject jsonAGV = new JSONObject();//发送给agv
        JSONObject jsonResult = returnAgv(1);//返回给agv
        try {
            JSONObject jsn = js.getJSONObject("data");

            String jld = jsn.getString("supplyUnLoadWb"); //接料点
            LOG.info("AGV接料点上报已接收到料箱，接料点为：" + jld);

            String sql = "select ET_IP,MAT_POINT from ET_IP_AGV where AGV_POINT='" + jld + "' ";
            Map<String, Object> queryForMap = this.getJdbcTemplate().queryForMap(sql);
            String MAT_POINT = queryForMap.get("MAT_POINT").toString();
            Object ET_IP = queryForMap.get("ET_IP"); //设备IP

            //通知设备(上位机)可以停止滚动
            ScadaRequest sq = new ScadaRequest();
            switch (MAT_POINT.toString()) {
                case "ECD101":  // 如果当前设备点位是主板下料点，操作码
                    sq.setOp_flag("B24");
                    LOG.info("一线主板下料点通知设备停止滚动");
                    break;

                case "ECD103":  // 如果当前设备点位是机箱下料点，操作码
                    sq.setOp_flag("B27");
                    LOG.info("一线机箱下料点通知设备停止滚动");
                    break;

                case "ECD202":  // 如果当前设备点位是主板下料点，操作码
                    sq.setOp_flag("B24");
                    LOG.info("二线主板下料点通知设备停止滚动");
                    break;

                case "ECD203":  // 如果当前设备点位是机箱下料点，操作码
                    sq.setOp_flag("B27");
                    LOG.info("二线机箱下料点通知设备停止滚动");
                    break;

                default:
                    sq.setOp_flag("B21"); // 否则就是滚筒线接料点
                    LOG.info("滚筒线下料点通知设备停止滚动，点位：" + MAT_POINT);
                    break;
            }
            sq.setMat_point(MAT_POINT);
            sq.setMo_num("1");
            sq.setEt_ip(ET_IP.toString());
            JSONObject jsonClient = toDevice(sq); //接收到设备返回的信息

            try {

                //上位机返回ok，代表设备收到agv的接到料信息，scada通知agv可以离开
                LOG.info("接料点通知设备停止滚动，设备返回的信息" + jsonClient);
                jsonAGV.put("taskCode", jsn.getString("taskCode"));
                jsonAGV.put("currentWb", jsn.getString("supplyUnLoadWb"));
                jsonAGV.put("taskSta", "2"); //接料点离开
                //调用agv是否离开的接口
                sayAgvGo(jsonAGV);
                if (jsonClient.getString("result_flag").equals("OK")) {
                    LOG.info("接料点通知设备停止滚动，设备返回了OK");
                }

            } catch (Exception e) {
                LOG.info("接料点通知设备停止滚动，上位机没有返回OK，点位：" + MAT_POINT);
                e.printStackTrace();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonResult.toString();
    }

    /**
     * u
     * 6.通知AGV是否可以离开,提 供方：agv
     *
     * @param msg
     * @return
     */
    public String sayAgvGo(JSONObject jsonClient) {
        String result = "";
        try {
            JSONObject str = new JSONObject();
            str.put("taskCode", jsonClient.getString("taskCode")); //唯一任务号，必填
            str.put("currentWb", jsonClient.getString("currentWb")); //点位，必填
            str.put("taskSta", jsonClient.getString("taskSta")); //点位离开
            JSONObject js = new JSONObject();
            js.put("reqcode", ph.getIdFive()); //请求编号,可自己生成
            js.put("data", str);

            switch (jsonClient.getString("taskSta")) {
                case "2": //agv接料（取料)离开
                    LOG.info("通知AGV接料离开发送的数据 ：" + js);
                    break;

                case "4": //供料点离开(上料点)
                    LOG.info("通知AGV供料点离开发送的数据 ：" + js);
                    break;

                case "6": //取空料箱点离开(接空箱）
                    LOG.info("通知AGV取空料箱点离开发送的数据 ：" + js);
                    break;

                default: //供空箱（上空箱）
                    LOG.info("通知AGV供空箱离开发送的数据 ：" + js);
                    break;
            }
            String doPost = agv.doPostToAgv("http://10.50.4.11:8888/api/wisdom/autoProductionLine/supplyAndRecyle/supllyUnload/SupllyAndRecyleResult", js);
            JSONObject agvJsonResult = new JSONObject(doPost);
            LOG.info("调用AGV离开接口，返回：" + agvJsonResult);
            result = agvJsonResult.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 7.创建回收空料箱任务,提 供方：agv
     *
     * @param msg
     * @return
     */
    @RequestMapping("/emptyRecyleTask")
    public ScadaQuery createEmpty(JSONObject json) {
        ScadaQuery sr = new ScadaQuery();
        try {
            JSONArray array = new JSONArray();
            JSONObject str = new JSONObject();
            str.put("taskCode", ph.getId()); //唯一任务号，必填
            str.put("srcWbCode", json.getString("srcWbCode")); //空料箱回收上箱点编码
            str.put("targetEmptyRecyleWb", json.getString("targetEmptyRecyleWb")); //空框回收下箱点(给我们送空箱的点)
            str.put("emptyRecyleNum", "1"); //空框回收数量，选填项
            str.put("taskPri", "urgent"); //优先级
            array.put(str);
            JSONObject js = new JSONObject();
            js.put("reqcode", ph.getIdFive()); //请求编号,可自己生成
            js.put("data", array);

            LOG.info("创建回收空箱任务,发给AGV的接空框点 ：" + json.getString("srcWbCode") + "，供空框点：" + json.getString("targetEmptyRecyleWb"));
            String doPost = agv.doPostToAgv("http://10.50.4.11:8888/api/wisdom/autoProductionLine/emptyRecyleTask", js);
            JSONObject agvJsonResult = new JSONObject(doPost);
            LOG.info("创建回收空箱任务，agv返回：" + agvJsonResult);

            sr.setCode(agvJsonResult.getString("code"));
            sr.setMessage(agvJsonResult.getString("message"));
            sr.setReqcode(agvJsonResult.getString("reqCode"));
            sr.setResult_flag("OK");


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sr;
    }

    /**
     * 8.上报回收任务（接空框点位）AGV已回收空料箱结果,提供方：scada
     *
     * @param msg
     * @return
     * @throws JSONException
     */
    @RequestMapping("/emptyRecyleTask/srcWb/recyleResult")
    public String recyle(HttpServletRequest request) {
        String result = "";
        try {

            JSONObject js = new JSONObject();
            js = agvJson.getJSONParam(request);
            JSONObject jsn = js.getJSONObject("data");

            LOG.info("上报回收任务（接空框点位）AGV已回收空料箱结果,接收的agv数据：" + jsn);

            JSONObject jsonAGV = new JSONObject();
            //上位机返回ok，代表设备收到agv的接到料信息，scada通知agv可以离开

            jsonAGV.put("taskCode", jsn.getString("taskCode"));
            jsonAGV.put("currentWb", jsn.getString("srcWbCode"));
            jsonAGV.put("taskSta", "6"); //（接空框点位）AGV已回收空料箱离开
            //调用agv是否离开的接口
            sayAgvGo(jsonAGV);

            JSONObject jsonResult = new JSONObject();
            jsonResult.put("code", "0");
            jsonResult.put("message", "成功");
            jsonResult.put("reqcode", ph.getIdFive());
            result = jsonResult.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 9.iWCS上报回收空料框结果,提供方：scada
     *
     * @param msg
     * @return
     * @throws JSONException
     */
    @RequestMapping("/emptyRecyleTask/recyleWb/recyleResult")
    public String recyleWb(HttpServletRequest request) {
        String result = "";
        try {
            JSONObject js = new JSONObject();
            js = agvJson.getJSONParam(request);

            JSONObject jsn = js.getJSONObject("data");
            LOG.info("9.iWCS上报回收空料框结果,接收的agv数据：" + jsn);

            JSONObject jsonAGV = new JSONObject();
            //上位机返回ok，代表设备收到agv的接到料信息，scada通知agv可以离开

            jsonAGV.put("taskCode", jsn.getString("taskCode"));
            jsonAGV.put("currentWb", jsn.getString("emptyRecyleWb"));
            jsonAGV.put("taskSta", "8"); //供空料箱点离开
            //调用agv是否离开的接口
            sayAgvGo(jsonAGV);

            JSONObject jsonResult = new JSONObject();
            jsonResult.put("code", "0");
            jsonResult.put("message", "成功");
            jsonResult.put("reqcode", ph.getId());
            result = jsonResult.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 10.iWCS上报已下料数量及已接收空框数量,提供方：scada
     *
     * @param msg
     * @return
     * @throws JSONException
     */
    @RequestMapping("/supplyAndRecyle/supllyAndRecyleResult")
    public String supllyAndRecyleResult(HttpServletRequest request) {
        String result = "";
        try {
            JSONObject js = new JSONObject();
            js = agvJson.getJSONParam(request);
            JSONObject jsn = js.getJSONObject("data");

            LOG.info("iWCS上报已下料数量及已接收空框数量,接收的agv数据：" + jsn);
            JSONObject jsonAGV = new JSONObject();
            //scada通知agv可以离开

            jsonAGV.put("taskCode", jsn.getString("taskCode"));
            jsonAGV.put("currentWb", jsn.getString("supplyUnLoadWb"));
            jsonAGV.put("taskSta", "4"); //供料点离开
            //调用agv是否离开的接口
            sayAgvGo(jsonAGV);

            JSONObject jsonResult = new JSONObject();
            jsonResult.put("code", "0");
            jsonResult.put("message", "成功");
            jsonResult.put("reqcode", ph.getIdFive());
            result = jsonResult.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 发送信息给设备
     *
     * @return
     * @throws JSONException
     */
    public JSONObject toDevice(ScadaRequest sq) throws JSONException {
        String resultDevice = "";

        resultDevice = sp.commonRequestDevice(sq);

        //请求信息给上位机
        LOG.info("发送给上位机的信息：" + resultDevice + ",上位机IP为：" + sq.getEt_ip());
        String resultClient = netty.setToClient(sq.getEt_ip(), resultDevice);
        JSONObject jsonClient = new JSONObject();
        try {
            jsonClient = new JSONObject(resultClient);
            if (!sq.getOp_flag().equals("S011")) {
                LOG.info("上位机返回的信息,result_flag：" + jsonClient.getString("result_flag") + ",IP为：" + sq.getEt_ip());
            }

        } catch (Exception e) {
            LOG.info("等待8秒，上位机无应答，IP：" + sq.getEt_ip());
            jsonClient.put("result_flag", "NG");
        }


        return jsonClient;
    }

    /**
     * 返回成功或失败消息给AGV
     *
     * @return res : 1代表成功
     */
    public JSONObject returnAgv(int res) {
        JSONObject jsonResult = new JSONObject();//返回给agv
        try {
            if (res == 1) {
                jsonResult.put("code", "0");
                jsonResult.put("message", "成功");
                jsonResult.put("reqcode", ph.getIdFive());
            } else {
                jsonResult.put("code", "1");
                jsonResult.put("message", "失败");
                jsonResult.put("reqcode", ph.getIdFive());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonResult;

    }

    /**
     * 询问上位机此点位是否可以滚动进料或者进空框
     *
     * @param MAT_POINT 点位
     * @param MO_NUM    上还是下
     * @return true 可以滚 , false  不能滚
     */
    public boolean isRollToAgv(String MAT_POINT, String MO_NUM, String taskCode) {
        ScadaRequest sq = new ScadaRequest();
        sq.setOp_flag("C01");
        sq.setMat_point(MAT_POINT);
        sq.setMo_num(MO_NUM);
        sq.setEt_ip(redisUtil.hget("matPoint", MAT_POINT).toString());
        JSONObject jsonObject = toDevice(sq);//接收到设备返回的信息
        if (jsonObject.getString("result_flag").equals("NG")) {

            LOG.info("上位机挂掉，设备IP为" + redisUtil.hget("matPoint", MAT_POINT));
        }

        if (jsonObject.getString("result_flag").equals("NO") || jsonObject.getString("result_flag").equals("NG")) {  //不能让AGV滚动，此次任务号存起来

            String commonSql = "UPDATE ET_IP_AGV SET TASKNO=? WHERE MAT_POINT=?";
            template.update(commonSql, taskCode, MAT_POINT);
            LOG.info("，任务号存起来，点位：" + MAT_POINT);
        }
        boolean flag  = false;
        if(jsonObject.getString("result_flag").equals("YES") || jsonObject.getString("result_flag").equals("OK")) {
            flag = true;
        }
        LOG.info("询问上位机，上位机返回："+jsonObject.getString("result_flag")+", flag为："+flag+",点位："+MAT_POINT);
        return flag;
    }

}
