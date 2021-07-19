package cn.bp.scada.controller.scada;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import cn.bp.scada.common.utils.redis.RedisUtils;
import cn.bp.scada.modle.scada.ScadaRequest;
import cn.bp.scada.modle.scada.ScadaRespon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.util.JSONPObject;

import cn.bp.scada.modle.scada.ScadaQuery;
import cn.bp.scada.common.utils.agv.AgvUtils;
import cn.bp.scada.common.utils.dbhelper.DBHelperLocation;
import cn.bp.scada.common.utils.JsonHelper;
import cn.bp.scada.common.utils.MesApiUtils;
import cn.bp.scada.common.utils.PrimaryHelper;
import cn.bp.scada.controller.sap.SapDeleProdcMsg;

/**
 * 工产线、测试区、叉车AGV搬运任务
 *
 * @author Administrator
 */
@RestController
@RequestMapping("/api/wisdom/agvHandlingTask")
public class ScadaToAgvBan extends JdbcDaoSupport {
    @Resource
    public void setJb(JdbcTemplate jb) {
        super.setJdbcTemplate(jb);
    }

    @Resource
    private PrimaryHelper ph;
    @Resource
    private JsonHelper agvJson;
    @Resource
    private AgvUtils agv;
    @Resource
    private MesApiUtils mesApiUtils;
    @Resource
    private JsonHelper jsons;
    @Resource
    private ScadaAndAgvMt smt;
    @Resource
    private SapDeleProdcMsg smg;
    @Resource
    private JdbcTemplate template;
    @Resource
    private DBHelperLocation dbLocation;
    @Autowired
    private RedisUtils redisUtil;

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    final static String ET_IP = "192.168.10.253";


    @RequestMapping("/t")
    public void test(HttpServletRequest request) throws JSONException {
        LOG.info("AGV异常查找收到数据request："+request);
        JSONObject js = new JSONObject();

        js = agvJson.getJSONParam(request);
        JSONObject jsonMo = js.getJSONObject("data");

        jsonMo.put("taskType", jsonMo.getString("taskType"));
        jsonMo.put("srcWb", jsonMo.getString("srcWb"));
        jsonMo.put("podCode", jsonMo.getString("podCode"));
        agvHandlingTask(jsonMo);
    }

    /**
     * 1.1.	创建搬运任务,提 供方：agv
     *
     * @param
     * @return
     */

    public ScadaQuery agvHandlingTask(JSONObject json) {
        LOG.info("AGV异常查找收到数据request："+json);
        ScadaQuery sr = new ScadaQuery();
        int flag = 1;
        try {
            json.get("reject");
        } catch (Exception e) {
            flag = 2;
        }

        try {
            JSONArray array = new JSONArray();
            JSONObject str = new JSONObject();
            str.put("taskCode", ph.getId()); //唯一任务号，必填
            str.put("taskType", json.getString("taskType")); //任务类型
            str.put("srcWb", json.getString("srcWb")); //搬运任务起点
            if (flag == 1) {
                str.put("destWb", json.getString("destWb")); //搬运任务终点
            }
            str.put("taskPri", "urgent"); //优先级—紧急（urgent）、普通（normal）必填项

            if (json.getString("taskType").equals("plToWokpw")) {
                LOG.info("创建产线到插线区搬运任务，发送给AGV的数据 ：" + str);
            } else if (json.getString("taskType").equals("quaHaulback")) {
                LOG.info("老化创建检验区回空搬运任务，发送给AGV的数据 ：" + str);
            } else if (json.getString("taskType").equals("agingToQuaInsp")) {

                str.put("podCode", json.getString("podCode"));//货架号
                LOG.info("老化区去检验区，发送给AGV的数据 ：" + str + " ,起点为：" + str.getString("srcWb") + " ,货架号为：" + str.getString("podCode"));

            } else {
                LOG.info("不良品搬运，发送给AGV的数据 ：" + str);
            }
            array.put(str);
            JSONObject js = new JSONObject();
            js.put("reqcode", ph.getIdFive());
            js.put("data", array);

            String doPost = agv.doPostToAgv("http://10.50.4.11:8888/api/wisdom/pToPHandlingTask", js);
            JSONObject agvJsonResult = new JSONObject(doPost);
            LOG.info("创建搬运任务，agv返回：" + agvJsonResult.toString());

            if (json.getString("taskType").equals("agingToQuaInsp") && agvJsonResult.getString("code").equals("0")) {
                //如果是老化区到检验区，并且AGV返回成功，则修改状态
                //修改载具绑定表的IS_HAND为1，已发送过 ；；、
                String sql = "UPDATE MES1.OLD_SHELF_BIND SET IS_HAND =1 WHERE BOXNO='" + json.getString("podCode") + "' ";
                this.getJdbcTemplate().update(sql);
            }

            sr.setCode(agvJsonResult.getString("code"));
            sr.setMessage(agvJsonResult.getString("message"));
            sr.setReqcode("11");
        } catch (JSONException e) {
            LOG.info("创建AGV搬运，异常");
            sr.setCode("-1");
            sr.setMessage("异常");
            sr.setReqcode(ph.getIdFive());
           LOG.info(e.toString(),e);
        }
        return sr;
    }

    /**
     * 2.AGV离开起点,提供方：scada
     *
     * @param
     * @return
     * @throws JSONException
     */
    @RequestMapping("/leaveSrcWb")
    public String taskUpda(HttpServletRequest request) throws JSONException {
        String result = "";
        JSONObject jsonResult = new JSONObject();
        try {
            JSONObject js = agvJson.getJSONParam(request);

            JSONObject jsn = js.getJSONObject("data");
            	String podCode = jsn.getString("podCode"); //货架号
            ScadaRequest sr = new ScadaRequest();
            switch (jsn.getString("srcWb").charAt(0)) {
                case 'C':
                    if(jsn.getString("srcWb").charAt(1)!='H') {
                        LOG.info("AGV离开产线搬运区起点,接收的agv数据：" + jsn.toString());
                        //发送离开信息给上位机
                        sendAgvComeOrGo(jsn.getString("srcWb"), "2");
                    }
                    break;
                case 'A':
                    LOG.info("AGV离开老化区，接收的agv数据：" + jsn.toString());

                    //置空老化架在老化室的位置
                    String hSql = "UPDATE MES1.OLD_SHELF_BIND SET OLD_STATION_ID = '' WHERE OLD_STATION_ID='" + jsn.getString("srcWb") + "'";
                    this.getJdbcTemplate().update(hSql);

                    //修改老化位置对应表状态为 1
                    String levelSql = "UPDATE AGV_OLD_LOCATION SET STATUS = 1 WHERE SHEFT_LOCATION = ?";
                    this.getJdbcTemplate().update(levelSql, jsn.getString("srcWb"));

                    sr.setOp_flag("A04");
                    sr.setMo_num("0");
                    sr.setMat_point(jsn.getString("srcWb"));
                    sr.setEt_ip(ET_IP); //灭灯

                    smt.toDevice(sr);

					oldStatus(jsn.getString("srcWb"),3);
                    break;

                case 'D':
                    LOG.info("AGV离开检验区起点,接收的agv数据：" + js.toString());
                    String insertSql = "INSERT INTO LOCATION_INFO_HIS(SN,LOCATION,ADDTIME) "
                            + "SELECT SN,LOCATION,NOW() FROM LOCATION_INFO WHERE "
                            + "REVERSE(substring(REVERSE(LOCATION),1,6))=" + podCode + "";
                    dbLocation.excuteUpdate(insertSql, null);

                    String deleTySql = "DELETE FROM LOCATION_INFO WHERE REVERSE(substring(REVERSE(LOCATION),1,6))=" + podCode + " ";
                    dbLocation.excuteUpdate(deleTySql, null);

                    cleanPod(podCode);
                    break;
                default:
                   LOG.info("PDA离开其他区，过滤");
                    break;
            }

            //收到agv的数据，如果进行处理，最后返回结果给agv

            jsonResult.put("code", "0");
            jsonResult.put("message", "成功");
            jsonResult.put("reqcode", ph.getIdFive());
            result = jsonResult.toString();
        } catch (Exception e) {
            jsonResult.put("code", "0");
            jsonResult.put("message", "成功");
            jsonResult.put("reqcode", ph.getIdFive());
            result = jsonResult.toString();
           LOG.info(e.toString(),e);
        }
        return result;
    }

    /**
     * 3.AGV到达终点,提供方：scada
     *
     * @param
     * @return
     * @throws IOException
     * @throws JSONException
     */
    @RequestMapping("/arriveDestWb")
    public String arriveDestWb(HttpServletRequest request) throws Exception {
        String result = "";
        JSONObject jsonResult = new JSONObject();
        try {
            JSONObject js = new JSONObject();
            js = agvJson.getJSONParam(request);
            LOG.info("收到AGV到达终点接口,数据："+js);
            JSONObject jsn = js.getJSONObject("data");

            String oldLocation = jsn.getString("destWb"); //老化架位置
            String shelf = jsn.getString("podCode"); //货架号
            String  type = jsn.getString("type");//PDA还是自动 0：PDA 1:系统

            switch (jsn.getString("destWb").charAt(0)) {
                case 'C':
                    if(jsn.getString("destWb").charAt(1)!='H') {
                        //发送信息给上位机
                        sendAgvComeOrGo(jsn.getString("destWb"), "1");
                        cleanPod(shelf);
                        //空货架补充产线
                        LOG.info("AGV到达空货架补充产线，接收的agv数据，到达点为:" + oldLocation + ",货架号：" + shelf);
                        String sql = "UPDATE MES1.OLDTEST_STATION SET SHELF_ID=" + shelf + ",SHELF_QTY=0 WHERE STATION_ID='" + oldLocation + "' ";
                        template.update(sql);

                        String reSql = " SELECT LINE_CD  FROM MES1.OLDTEST_STATION WHERE STATION_ID = '" + oldLocation + "' ";
                        Map<String, Object> map = this.getJdbcTemplate().queryForMap(reSql);

                        String countSql = "SELECT count(1) cou  FROM MES1.OLDTEST_STATION WHERE LINE_CD = '" + map.get("LINE_CD").toString() + "' "
                                + "AND SHELF_ID IS NOT NULL";
                        Map<String, Object> forMap = this.getJdbcTemplate().queryForMap(countSql);

                        if (Integer.parseInt(forMap.get("cou").toString()) <= 1) {
                            String sqls = "UPDATE MES1.OLDTEST_STATION SET IS_PUT = 'N' WHERE STATION_ID='" + oldLocation + "'";
                            this.getJdbcTemplate().update(sqls);
                            //一个老化架都没有的情况，到达后触发上位机扫码
                            touchMaster(oldLocation);

                        }

                    } else {
                        LOG.info("AGV补充到缓存区");
                    }

                    break;
                case 'A': //  1）	iwcs AGV通知SCADA老化柜到位，SCADA下发命令工控机亮起黄灯；
                    LOG.info("AGV到位老化区，接收的agv数据：" + jsn.toString());
                    //更新载具绑定表，老化架在老化室的位置信息
                    String PDASql = "UPDATE MES1.OLD_SHELF_BIND SET OLD_STATION_ID = '" + jsn.getString("destWb") + "' WHERE  BOXNO = '" + shelf + "' ";
                    this.getJdbcTemplate().update(PDASql);

                    String upSql ="UPDATE MES_TURNOVER_TOOL_CREATE SET SHELF_STA=0 WHERE  SN= ? ";
                    this.getJdbcTemplate().update(upSql,shelf);
                    ScadaRequest sr = new ScadaRequest();
                    sr.setOp_flag("A01");
                    sr.setMo_num("1");
                    sr.setMat_point(jsn.getString("destWb"));
                    sr.setEt_ip(ET_IP); //老化区有100多个点位对应1台工控机，不想维护100条记录，hardcode;

                    JSONObject device = smt.toDevice(sr);
                    if (device.getString("result_flag").equals("NG")) {
                        LOG.info("老化区到位通知上位机亮灯失败，点位：" + jsn.getString("destWb"));
                    }
                    //修改老化位置对应表状态为 0
					oldStatus(jsn.getString("destWb"),0);
                    break;

                case 'D' :
                    if(type .equals("1")) {
                        LOG.info("AGV到达检验区，接收的AGV数据：" + jsn.toString());
                        //根据AGV发送的老化架编号查出到位老化架上的产品位置
                        //要修改此老化架的四个状态，老化完成状态SHELF_STA、AGV是否拔线状态PULL_LINE、是否发送过老化完成给AGV状态OLD_COMPLETE，是否发送过搬运老化到后测is_hand状态
                        //天眼的locationinfo表要查出此老化架，同时记录表新增此删除的老化架

                        //AGV到位
                        String pdCode = jsn.get("podCode").toString(); //老化架编号
                        String destWb = jsn.get("destWb").toString(); //到达检验区位置，E或者F
                        String locations = "";
                        if (destWb.equals("D0101")) {
                            //对应PLC的点位
                            locations = "E";
                        } else {
                            locations = "F";
                        }


                        //根据架子编号查出产品RN，老化架位置
                        String afterSql1 = "select convert("
                                + "SUBSTRING_INDEX( substr( SUBSTRING_INDEX(LOCATION,'-',2),6,6),'-',-1  ),UNSIGNED INTEGER ) RN, "
                                + " REVERSE(substring(REVERSE(LOCATION),1,6))  podcode from "
                                + "LOCATION_INFO where REVERSE(substring(REVERSE(LOCATION),1,6))=" + pdCode + " ORDER BY "
                                + "RN";
                        List<Map<String, Object>> afterLocationList1 = dbLocation.executeQueryTest(afterSql1, null, null);
                        StringBuffer buRN1 = new StringBuffer();
                        if (afterLocationList1.size() > 0) {
                            for (int i = 0; i < afterLocationList1.size(); i++) {
                                if (afterLocationList1.size() == 1) {
                                    buRN1.append("" + afterLocationList1.get(i).get("RN") + "");
                                } else {
                                    if (i == afterLocationList1.size() - 1) {
                                        buRN1.append("" + afterLocationList1.get(i).get("RN") + "");
                                    } else {
                                        buRN1.append("" + afterLocationList1.get(i).get("RN") + "" + ",");
                                    }
                                }
                            }
                            ScadaRequest srx = new ScadaRequest();
                            srx.setOp_flag("S011");
                            srx.setFrame_stion(locations);
                            srx.setMaterial_stion(buRN1.toString()); //1到12产品位置
                            srx.setResult_message(pdCode); //老化架 编号
                            srx.setFlow_code("After");
                            srx.setEt_ip("192.168.10.49");

                            JSONObject deviceRespon = smt.toDevice(srx);
                            LOG.info("后测下老化架，上位机返回：" + deviceRespon);

                        } else {
                            LOG.info("此货架在天眼不存在，货架编号：" + pdCode);
                        }
                    } else {
                        LOG.info("PDA通知到达检验区，不告诉上位机");
                    }

                    break;
                default:

                    LOG.info("PDA发送其他区，过滤");
            }

            //如果处理接收的agv数据，最后返回结果给agv

            jsonResult.put("code", "0");
            jsonResult.put("message", "成功");
            jsonResult.put("reqcode", ph.getIdFive());
            result = jsonResult.toString();
        } catch (Exception e) {
            jsonResult.put("code", "0");
            jsonResult.put("message", "成功");
            jsonResult.put("reqcode", ph.getIdFive());
            result = jsonResult.toString();
           LOG.info(e.toString(),e);
        }
        return result;
    }

    /**
     * 4.PDA通知 老化区人工已插线,提供方：scada
     *
     * @param
     * @return
     * @throws IOException
     * @throws JSONException
     */
    @RequestMapping("/notifyPodInfo")
    public String notifyPodInfo(HttpServletRequest request) throws IOException, JSONException {

        String result = "";
        try {

            JSONObject js = new JSONObject();
            js = agvJson.getJSONParam(request);
            JSONObject jsn = js.getJSONObject("data");
            String podCode = jsn.getString("podCode"); //货架号

            //代表人工已插线
            LOG.info("PDA通知 老化区人工已插线，接收的agv数据：" + jsn);
            //判断人工输入的老化架位置是否正确，不正确返回失败，让PDA重新发送
            String sql = "SELECT COUNT(1) CO FROM AGV_OLD_LOCATION WHERE SHEFT_LOCATION ='" + jsn.getString("destWb") + "'";
            String sql2 = "SELECT COUNT(1) CO FROM MES1.MES_TURNOVER_TOOL_CREATE WHERE SN ='" + podCode + "'";
            Map<String, Object> map = this.getJdbcTemplate().queryForMap(sql);
            Map<String, Object> map2 = this.getJdbcTemplate().queryForMap(sql2);

            if (Integer.parseInt(map.get("CO").toString()) > 0 && Integer.parseInt(map2.get("CO").toString()) > 0) {

                //修改周转工具列表的老化架编号为1 老化中
                String updaSql = "UPDATE MES1.MES_TURNOVER_TOOL_CREATE SET SHELF_STA = 1 WHERE SN = '" + podCode + "' ";
                this.getJdbcTemplate().update(updaSql);


                //更新载具绑定表，老化架在老化室的位置信息
                String PDASql = "UPDATE MES1.OLD_SHELF_BIND SET OLD_STATION_ID = '" + jsn.getString("destWb") + "' WHERE  BOXNO = '" + podCode + "' ";
                this.getJdbcTemplate().update(PDASql);

                //将老化架编号给上位机，设备控制三色灯为老化中
                //  2）	iwcs PDA通知老化架人工已插线，SCADA下发命令工控机亮起绿灯；
                ScadaRequest sr = new ScadaRequest();
                sr.setOp_flag("A02");
                sr.setMo_num("2");
                sr.setMat_point(jsn.getString("destWb"));
                sr.setEt_ip(ET_IP); //老化区有100多个点位对应1台工控机，不想维护100条记录，hardcode;

                JSONObject device = smt.toDevice(sr);
                if (device.getString("result_flag").equals("OK")) {
                    LOG.info("PDA通知人工插线，上位机返回了OK");
                }

                //修改老化位置对应表状态为 0
                oldStatus(jsn.getString("destWb"),1);
				//先把记录添加到历史表，再删
				String insertSql = "INSERT INTO LOCATION_INFO_HIS(SN,LOCATION,ADDTIME) "
						+ "SELECT SN,LOCATION,NOW() FROM LOCATION_INFO WHERE "
						+ "REVERSE(substring(REVERSE(LOCATION),1,6))=" + podCode + "";
				dbLocation.excuteUpdate(insertSql, null);

				String deleTySql = "DELETE FROM LOCATION_INFO WHERE REVERSE(substring(REVERSE(LOCATION),1,6))=" + podCode + " ";
				dbLocation.excuteUpdate(deleTySql, null);

                String destWb = jsn.getString("destWb") + "-"; //老化架位置


                //根据老化架编号从绑定表查出SN和序列
                String bindSqn = "select rn, min(productsn) sn, count(*),CREATIME "
                        + "as ct from OLD_SHELF_BIND where boxno = '" + podCode + "' AND PRODUCTSN IS NOT NULL and bind_sta = 0 group by rn,CREATIME "
                        + "having count(*) >= 1 order by CREATIME desc";

                List<Map<String, Object>> bindList = this.getJdbcTemplate().queryForList(bindSqn);
                List<Object> paramsList = new ArrayList<>();
                //插入到天眼系统老化架位置信息
                String locaSql = "INSERT INTO LOCATION_INFO(SN,LOCATION,ADDTIME) VALUES(?,?,now())";
                String existsSql = "SELECT 1 FROM LOCATION_INFO WHERE SN=? LIMIT 1";
                for (int i = 0; i < bindList.size(); i++) {
                    paramsList.add(bindList.get(i).get("sn"));
					 List<Map<String, Object>> maps = dbLocation.executeQueryTest(existsSql, paramsList, null);
					if (maps.size()<1) {
                        paramsList.add(destWb + bindList.get(i).get("rn") + "-" + podCode);
                        dbLocation.excuteUpdate(locaSql, paramsList);
                        paramsList.clear();
                        if (i >= 11) {
                            break;
                        }
                    }
                }


                JSONObject jsonResult = new JSONObject();
                jsonResult.put("returnCode", 200);
                jsonResult.put("returnMsg", "成功");
                JSONObject jsonReturn = new JSONObject();
                jsonResult.put("returnData", jsonReturn);
                jsonResult.put("reqcode", ph.getIdFive());
                result = jsonResult.toString();

            } else {
                LOG.info("人工输入的老化位置或者货架不存在，请人员确认");
                //人工输入老化架有误
                JSONObject jsonResult = new JSONObject();
                jsonResult.put("returnCode", 400);
                jsonResult.put("returnMsg", "人工输入的老化位置或货架编号错误，请重新输入");
            }

        } catch (JSONException e) {
           LOG.info(e.toString(),e);
            JSONObject jsonResult = new JSONObject();
            jsonResult.put("returnCode", 400);
            jsonResult.put("returnMsg", "失败，请重新输入");
        } catch (Exception e) {
           LOG.info(e.toString(),e);
        }
        return result;
    }

    /**
     * 老化完成时候把对应的货架传给AGV
     *
     * @param json
     * @return
     * @throws JSONException
     */
    public String modifyPodStatus(JSONObject json) throws JSONException {
        String time = ph.getDateTime();
        json.put("modifyDate", time);

        JSONObject js = new JSONObject();
        js.put("reqcode", ph.getIdFive());
        js.put("data", json);
        String doPost = agv.doPostToAgv("http://10.50.4.11:8888/api/wisdom/notify/modifyPodStatus", js);
        JSONObject agvJsonResult = new JSONObject(doPost);
        LOG.info("老化完成货架传给AGV，货架编号为：" + json.getString("podCode") + " ,agv返回：" + agvJsonResult.toString());
        //  3）	iwcs PDA修改状态老化架老化结束，SCADA下发命令工控机黄灯闪烁；
        ScadaRequest sr = new ScadaRequest();
        sr.setOp_flag("A03");
        sr.setMo_num("3");
        sr.setMat_point(json.getString("destWb"));
        sr.setEt_ip(ET_IP); //老化区有100多个点位对应1台工控机，不想维护100条记录，hardcode;

        JSONObject device = smt.toDevice(sr);

        if (agvJsonResult.getString("code").equals("0") && device.get("result_flag").equals("OK")) {
            //修改老化架状态为1，已发送过老化完成给AGV
            String sql = "update MES_TURNOVER_TOOL_CREATE set OLD_COMPLETE = 1 where sn = '" + json.getString("podCode") + "' ";
            this.getJdbcTemplate().update(sql);
        }
        //修改老化位置对应表状态为 2
        oldStatus(json.getString("destWb"),2);
        return agvJsonResult.toString();
    }

    /**
     * PDA通知老化区人工已拔线
     *
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping("/notifyPullOutLine")
    public String PullOutLine(HttpServletRequest request) throws IOException {

        String result = "";
        try {
            JSONObject js = new JSONObject();
            js = agvJson.getJSONParam(request);
            JSONObject jsn = js.getJSONObject("data");
            String oldLocation = jsn.getString("destWb"); //点位
            String shelf = jsn.getString("podCode"); //货架号
            LOG.info("AGV通知人工已拔线，点位：" + oldLocation + ",货架号：" + shelf);
            //判断人工输入的老化架位置是否正确，不正确返回失败，让PDA重新发送
            String sql1 = "SELECT COUNT(1) CO FROM AGV_OLD_LOCATION WHERE SHEFT_LOCATION ='" + jsn.getString("destWb") + "'";
            String sql2 = "SELECT COUNT(1) CO FROM MES1.MES_TURNOVER_TOOL_CREATE WHERE SN ='" + shelf + "'";
            Map<String, Object> map = this.getJdbcTemplate().queryForMap(sql1);
            Map<String, Object> map2 = this.getJdbcTemplate().queryForMap(sql2);
            if (Integer.parseInt(map.get("CO").toString()) > 0 && Integer.parseInt(map2.get("CO").toString()) > 0) {

                //修改老化架状态为1，已拔线
                String sql = "UPDATE MES_TURNOVER_TOOL_CREATE SET PULL_LINE = 1 WHERE SN = '" + shelf + "' ";
                this.getJdbcTemplate().update(sql);

                JSONObject jsonResult = new JSONObject();
                jsonResult.put("returnCode", 200);
                jsonResult.put("returnMsg", "成功");
                JSONObject jsonReturn = new JSONObject();
                jsonResult.put("returnData", jsonReturn);
                jsonResult.put("reqcode", "T6000001970");
                result = jsonResult.toString();
            } else {
                LOG.info("PDA通知拔线，人工输入的老化位置或者货架不存在，请再次输入");
                //人工输入老化架有误
                JSONObject jsonResult = new JSONObject();
                jsonResult.put("returnCode", 400);
                jsonResult.put("returnMsg", "人工输入的老化位置或货架编号错误，请重新输入");
            }

        } catch (Exception e) {
           LOG.info(e.toString(),e);
        }
        return result;
    }

    /**
     * PDA通知产线搬运任务到达或离开
     *
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/notifyAGVArrive")
    public String notifyAGVArrivePDA(HttpServletRequest request) throws Exception {

        String result = "";
        try {
            JSONObject js = new JSONObject();
            js = agvJson.getJSONParam(request);
            JSONObject jsn = js.getJSONObject("data");
            String oldLocation = jsn.getString("destWb"); //PDA到达产线或离开产线的点位
            String shelf = jsn.getString("podCode"); //货架号

            if (jsn.getString("notifyType").equals("leave")) { //PDA离开
                //发送离开信息给上位机
                sendAgvComeOrGo(oldLocation, "2");

                LOG.info("PDA通知老化架离开产线，接收的agv数据PDA接口，离开起点为:" + oldLocation + ",货架号：" + shelf);
                String sql = "UPDATE MES1.OLDTEST_STATION SET SHELF_ID='' ,SHELF_QTY=0,IS_PUT='Y' WHERE STATION_ID=?";
                this.getJdbcTemplate().update(sql, oldLocation);

                String reSql = " SELECT LINE_CD  FROM MES1.OLDTEST_STATION WHERE STATION_ID = '" + oldLocation + "' ";
                Map<String, Object> map = this.getJdbcTemplate().queryForMap(reSql);

                String countSql = "SELECT count(1) cou  FROM MES1.OLDTEST_STATION WHERE LINE_CD = '" + map.get("LINE_CD").toString() + "' "
                        + "AND SHELF_ID IS NOT NULL";
                Map<String, Object> forMap = this.getJdbcTemplate().queryForMap(countSql);

                if (Integer.parseInt(forMap.get("cou").toString()) > 0) {
                    countSql = "SELECT station_id,is_put  FROM MES1.OLDTEST_STATION WHERE LINE_CD = '" + map.get("LINE_CD").toString() + "' "
                            + "AND SHELF_ID IS NOT NULL";
                    Map<String, Object> uMap = this.getJdbcTemplate().queryForMap(countSql);
                    if (uMap.get("is_put").toString().equals("Y")) {
                        sql = "UPDATE MES1.OLDTEST_STATION SET IS_PUT='N' WHERE STATION_ID=?";
                        this.getJdbcTemplate().update(sql, uMap.get("station_id").toString());
                    }
                }

            } else {
                switch (jsn.getString("destWb").charAt(0)) {
                    case 'C':
                        sendAgvComeOrGo(oldLocation, "1");
                        cleanPod(shelf);
                        //空货架补充产线
                        LOG.info("PDA通知AGV空货架补充产线，到达点为:" + oldLocation + ",货架号：" + shelf);
                        String sql = "UPDATE MES1.OLDTEST_STATION SET SHELF_ID=" + shelf + ",SHELF_QTY=0 WHERE STATION_ID='" + oldLocation + "' ";
                        template.update(sql);

                        String reSql = " SELECT LINE_CD  FROM MES1.OLDTEST_STATION WHERE STATION_ID = '" + oldLocation + "' ";
                        Map<String, Object> map = this.getJdbcTemplate().queryForMap(reSql);

                        String countSql = "SELECT count(1) cou  FROM MES1.OLDTEST_STATION WHERE LINE_CD = '" + map.get("LINE_CD").toString() + "' "
                                + "AND SHELF_ID IS NOT NULL";
                        Map<String, Object> forMap = this.getJdbcTemplate().queryForMap(countSql);

                        if (Integer.parseInt(forMap.get("cou").toString()) <= 1) {
                            String sqls = "UPDATE MES1.OLDTEST_STATION SET IS_PUT = 'N' WHERE STATION_ID='" + oldLocation + "'";
                            this.getJdbcTemplate().update(sqls);
                            //一个老化架都没有的情况，到达后触发上位机扫码
                            touchMaster(oldLocation);
                        }
                        break;
                    case 'A': //  1）	iwcs AGV通知SCADA老化柜到位，SCADA下发命令工控机亮起黄灯；
                        LOG.info("PDA通知AGV到位老化区，接收的agv数据：" + jsn.toString());
                        ScadaRequest sr = new ScadaRequest();
                        sr.setOp_flag("A01");
                        sr.setMo_num("1");
                        sr.setMat_point(jsn.getString("destWb"));
                        sr.setEt_ip(ET_IP); //老化区有100多个点位对应1台工控机，不想维护100条记录，hardcode;

                        JSONObject device = smt.toDevice(sr);
                        if (device.getString("result_flag").equals("NG")) {
                            LOG.info("PDA通知老化区到位通知上位机亮灯失败，点位：" + jsn.getString("destWb"));
                        }
                        //修改老化位置对应表状态为 0
						oldStatus(jsn.getString("destWb"),0);
                        break;
                    default:
                        LOG.info("PDA通知AGV到达检验区，接收的AGV数据：" + jsn.toString());


                        //AGV到位
                        String pdCode = jsn.get("podCode").toString(); //老化架编号
                        String destWb = jsn.get("destWb").toString(); //到达检验区位置，E或者F
                        String locations = "";
                        if (destWb.equals("D0101")) {
                            //对应PLC的点位
                            locations = "E";
                        } else {
                            locations = "F";
                        }


                        //根据架子编号查出产品RN，老化架位置
                        String afterSql1 = "select convert("
                                + "SUBSTRING_INDEX( substr( SUBSTRING_INDEX(LOCATION,'-',2),6,6),'-',-1  ),UNSIGNED INTEGER ) RN, "
                                + " REVERSE(substring(REVERSE(LOCATION),1,6))  podcode from "
                                + "LOCATION_INFO where REVERSE(substring(REVERSE(LOCATION),1,6))=" + pdCode + " ORDER BY "
                                + "RN";
                        List<Map<String, Object>> afterLocationList1 = dbLocation.executeQueryTest(afterSql1, null, null);
                        StringBuffer buRN1 = new StringBuffer();
                        if (afterLocationList1.size() > 0) {
                            for (int i = 0; i < afterLocationList1.size(); i++) {
                                if (afterLocationList1.size() == 1) {
                                    buRN1.append("" + afterLocationList1.get(i).get("RN") + "");
                                } else {
                                    if (i == afterLocationList1.size() - 1) {
                                        buRN1.append("" + afterLocationList1.get(i).get("RN") + "");
                                    } else {
                                        buRN1.append("" + afterLocationList1.get(i).get("RN") + "" + ",");
                                    }
                                }
                            }
                            ScadaRequest srx = new ScadaRequest();
                            srx.setOp_flag("S011");
                            srx.setFrame_stion(locations);
                            srx.setMaterial_stion(buRN1.toString()); //1到12产品位置
                            srx.setResult_message(pdCode); //产品的具体位置，1,2
                            srx.setFlow_code("After");
                            srx.setEt_ip("192.168.10.49");

                            JSONObject deviceRespon = smt.toDevice(srx);
                            LOG.info("PDA通知后测下老化架，上位机返回：" + deviceRespon);

                        }
                        break;
                }


            }

            JSONObject jsonResult = new JSONObject();
            jsonResult.put("returnCode", 200);
            jsonResult.put("returnMsg", "成功");
            JSONObject jsonReturn = new JSONObject();
            jsonResult.put("returnData", jsonReturn);
            jsonResult.put("reqcode", ph.getIdFive());
            result = jsonResult.toString();
        } catch (Exception e) {
           LOG.info(e.toString(),e);
            JSONObject jsonResult = new JSONObject();
            jsonResult.put("returnCode", 400);
            jsonResult.put("returnMsg", "失败");

        }
        return result;
    }

    /**
     * 触发搬走按钮，安规搬运货架~
     *
     * @param req
     * @param data
     * @return
     * @throws JSONException
     */
    @RequestMapping("/callAgvAng")
    public JSONPObject yieldWo(HttpServletRequest req, String data) throws JSONException {

        String name = req.getParameter("callbackparam");//获取到jsonp的函数名
        JSONObject str = new JSONObject();
        ScadaQuery reQue = new ScadaQuery();
        try {

            String startPoint = ""; //任务起点
            String overPoint = ""; //任务终点

            String rejectStartPoint = ""; //从不良维修点搬回：起点
            String rejectOverPoint = ""; //从不良维修点搬回：终点
            switch (data) {
                case "1A": // 一线的A位置（老化架）
                    LOG.info("安规搬运老化架按钮为： 1A， 从安规的A位置搬走老化架到维修区的第二个位置（F0102），再从维修区的第一个位置搬空货架到安规的B位置");
                    startPoint = "E0102";
                    overPoint = "F0102";

                    rejectStartPoint = "F0101";
                    rejectOverPoint = "E0101";
                    break;

                case "1B":
                    LOG.info("安规搬运老化架按钮为： 1B， 从安规的B位置搬走老化架到维修区的第一个位置（F0101）,再从维修区的第二个位置搬空货架到安规的A位置");
                    startPoint = "E0101";
                    overPoint = "F0101";

                    rejectStartPoint = "F0102";
                    rejectOverPoint = "E0102";
                    break;

                case "2A":
                    LOG.info("安规搬运老化架按钮为： 2线A, 从安规的A位置搬走老化架到维修区的第四个位置（F0104）,再从维修区的第三个位置（F0103）搬空货架到安规的B位置");
                    startPoint = "E0103";
                    overPoint = "F0104";

                    rejectStartPoint = "F0103";
                    rejectOverPoint = "E0104";
                    break;

                default: //2B
                    LOG.info("安规搬运老化架按钮为： 2线B， 从安规的B位置搬走老化架到维修区的第三个位置（F0103）,再从维修区的第四个位置（F0104)搬空架到安规的A位置");
                    startPoint = "E0104";
                    overPoint = "F0103";

                    rejectStartPoint = "F0104";
                    rejectOverPoint = "E0103";
                    break;
            }
            str.put("reject", "1"); //标识是不是搬运不良品
            str.put("taskType", "rejectPtop"); //任务类型
            str.put("taskCode", ph.getIdFive()); //唯一任务号，必填
            str.put("srcWb", rejectStartPoint); //搬运任务起点
            str.put("destWb", rejectOverPoint); //搬运任务终点
            reQue = agvHandlingTask(str); //先创建不良到安规搬运任务
            if (reQue.getCode().equals("0")) {
                str.put("taskCode", ph.getId()); //唯一任务号，必填
                str.put("srcWb", startPoint); //搬运任务起点
                str.put("destWb", overPoint); //搬运任务终点
                str.put("taskPri", "urgent"); //优先级—紧急（urgent）、普通（normal）必填项

                reQue = agvHandlingTask(str); //创建安规到不良区搬运任务
            } else {
                reQue.setMessage("维修区点位:" + rejectStartPoint + "," + reQue.getMessage() + ",请放置一个空货架");
            }


        } catch (Exception e) {
            reQue.setMessage("失败");
           LOG.info(e.toString(),e);
        }
        return new JSONPObject(name, reQue.getMessage());
    }

    /**
     * 上位机告诉SCADA产品是否成功吸走
     *
     * @param msg
     * @return
     * @throws JSONException
     */
    public ScadaRespon afterXidiao(Object msg) throws JSONException {
        ScadaRespon srx = new ScadaRespon();
        try {

            JSONObject json = new JSONObject(msg.toString());
            LOG.info("status为，0：成功，1，失败  " + "status= " + json.getString("status"));
            String podCode = json.getString("result_message");//老化架编号
            String met = json.getString("frame_stion"); //老化架位置，E,F

            //PLC把整个老化架已推完，呼叫AGV搬运空货架

            switch (met) {
                case "E":
                    met = "D0101";
                    break;

                default:
                    met = "D0102";
                    break;
            }


            //同时删除天眼对应的产品SN数据
            //先把记录添加到历史表，再删
           /* String insertSql = "INSERT INTO LOCATION_INFO_HIS(SN,LOCATION,ADDTIME) "
                    + "SELECT SN,LOCATION,NOW() FROM LOCATION_INFO WHERE "
                    + "REVERSE(substring(REVERSE(LOCATION),1,6))=" + podCode + "";
            dbLocation.excuteUpdate(insertSql, null);

            String deleTySql = "DELETE FROM LOCATION_INFO WHERE REVERSE(substring(REVERSE(LOCATION),1,6))=" + podCode + " ";
            dbLocation.excuteUpdate(deleTySql, null);

            cleanPod(podCode);*/
            //呼叫AGV
            JSONObject str = new JSONObject();
            str.put("taskCode", ph.getId()); //唯一任务号，必填
            str.put("taskType", "quaHaulback"); //任务类型
            str.put("srcWb", met); //搬运任务起点
            str.put("taskPri", "urgent"); //优先级—紧急（urgent）、普通（normal）必填项

            ScadaQuery reQue = agvHandlingTask(str); //检验区回空老化架
            LOG.info("AGV返回检验区回空：" + reQue.getMessage());


            srx.setFlow_code("After");
            srx.setResult_flag("OK");

        } catch (Exception e) {
           LOG.info(e.toString(),e);
        }
        return srx;
    }


    //前测吸吊移栽通知上位机老化架到达或者离开
    public void sendAgvComeOrGo(String location, String status) {
        ScadaRequest result = new ScadaRequest();
        result.setOp_flag("S022");
        try {

            switch (location) {
                case "C0101":
                    result.setFrame_stion("B"); //老化架位置
                    result.setEt_ip("192.168.10.250");
                    break;
                case "C0102":
                    result.setFrame_stion("A"); //老化架位置
                    result.setEt_ip("192.168.10.250");
                    break;
                case "C0103":
                    result.setFrame_stion("B"); //老化架位置
                    result.setEt_ip("192.168.10.214");
                    break;
                default:
                    result.setFrame_stion("A"); //老化架位置
                    result.setEt_ip("192.168.10.214");
                    break;
            }
            result.setStatus(status);
            smt.toDevice(result);
        } catch (Exception e) {
           LOG.info(e.toString(),e);
            LOG.error("前测通知老化架到位失败");
        }
    }

    /**
     * 清除老化架
     *
     * @param podCode
     */
    public void cleanPod(String podCode) {
        try {
            //删除绑定表对应的老化架信息
            String hSql = "DELETE FROM MES1.OLD_SHELF_BIND  WHERE BOXNO='" + podCode + "'";
            //初始化周转工具表老化架状态SHELF_STA为0--未老化，PULL_LINE为0--未拔线，OLD_COMPLETE为0--未发送过老化完成给AGV
            String Msql = "UPDATE MES1.MES_TURNOVER_TOOL_CREATE SET SHELF_STA=0 , PULL_LINE=0 , "
                    + "OLD_COMPLETE = 0 WHERE SN ='" + podCode + "'";
            this.getJdbcTemplate().update(hSql);
            this.getJdbcTemplate().update(Msql);
            String deleTySql = "DELETE FROM LOCATION_INFO WHERE REVERSE(substring(REVERSE(LOCATION),1,6))=" + podCode + " ";
            dbLocation.excuteUpdate(deleTySql, null);
        }catch (Exception e) {
            LOG.info(e.toString(),e);
        }

    }

	/**
	 * AGV到达或离开，更改位置状态
	 * @param location
	 */
	public void oldStatus(String location,int lin) {
    	String sql = "UPDATE AGV_OLD_LOCATION SET STATUS=? WHERE SHEFT_LOCATION=?";
    	template.update(sql,lin,location);
	}

    /**
     * 产线老化架到达触发上位机扫码
     * @param oldLocation
     */
	public void touchMaster(String oldLocation)
 {
     String masterIp = redisUtil.hget("matPoint", oldLocation).toString(); //上位机IP
     ScadaRequest result = new ScadaRequest();
     result.setOp_flag("C09");
     result.setEt_ip(masterIp);
     smt.toDevice(result);
 }

}
