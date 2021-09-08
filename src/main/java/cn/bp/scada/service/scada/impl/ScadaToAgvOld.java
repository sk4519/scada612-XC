package cn.bp.scada.service.scada.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import cn.bp.scada.common.constant.DeviceCode;
import cn.bp.scada.common.utils.redis.RedisUtils;
import cn.bp.scada.mapper.scada.CiPLANTeTMapper;
import cn.bp.scada.mapper.scada.Pack;
import cn.bp.scada.modle.scada.ScadaResponShow;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Service;
import cn.bp.scada.controller.scada.ScadaAndAgvMt;
import cn.bp.scada.controller.scada.ScadaToAgvBan;
import cn.bp.scada.modle.scada.ScadaQuery;
import cn.bp.scada.modle.scada.ScadaRespon;
import cn.bp.scada.common.utils.agv.AgvUtils;
import cn.bp.scada.common.utils.JsonHelper;
import cn.bp.scada.common.utils.MesApiUtils;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 老化区搬运任务 & 组装线回收空框任务
 *
 * @author Administrator
 */
@Service
public class ScadaToAgvOld extends JdbcDaoSupport {
    @Resource
    public void setJb(JdbcTemplate jb) {
        super.setJdbcTemplate(jb);
    }

    @Resource
    private AgvUtils agv;

    @Resource
    private JsonHelper jsons;
    @Resource
    private JdbcTemplate template;
    @Resource
    private ScadaToAgvBan agvBan;
    @Resource
    private ScadaAndAgvMt agvMt;
    @Resource
    private MesApiUtils mesApiUtils;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private Pack pack;
    @Resource
    private CiPLANTeTMapper ciPLANTeTMapper;

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    private int count = 0;
    private boolean flag = false;

    /**
     * 产品到达上老化架吸掉移载区，上位机通知SCADA上老化架
     *
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public ScadaRespon sendAgvOld(Object msg) throws JSONException, IOException {

        //1.到达上老化架吸掉移载区，上位机通知SCADA上老化架
        ScadaRespon result = new ScadaRespon();

        JSONObject deviceRequest = new JSONObject(msg.toString());
        LOG.info("产品上老化架，收到的上位机信息：" + deviceRequest);
        int i = ciPLANTeTMapper.selectMO();
        int res = ciPLANTeTMapper.updateEt(i);
        JSONObject reqDataDetail = new JSONObject();
        JSONObject responseData = null;
        String line = null;
        try {
            if (deviceRequest.getString("device_sn").equals("ECDLC027")) {
                line = "LCLINE001";
            } else {
                line = "LCLINE002";
            }
            String staSql = "SELECT COUNT(1) STACOUNT FROM OLDTEST_STATION WHERE LINE_CD= ? AND SHELF_ID IS NOT NULL";
            Map<String, Object> map1 = template.queryForMap(staSql, line);
            //判断必须存在一个老化架才进行之后的逻辑，否则返回此地方无老化架，防止解绑
            if (Integer.parseInt(map1.get("STACOUNT").toString()) > 0) {


                reqDataDetail.put("IN_VEHICLES", deviceRequest.getString("con_sn"));
                reqDataDetail.put("IN_ET_CD", deviceRequest.getString("device_sn"));
                reqDataDetail.put("IN_TUS", "2"); //代表要查两个货架满


                if (redisUtils.get("iscg").toString().equals("OK")) { //是参观模式
                    reqDataDetail.put("IFS", "STAS422");
                    responseData = mesApiUtils.doPost(reqDataDetail);
                    LOG.info("参观模式：Oracle返回扫码信息："+responseData.toString()+"   载具SN为："+deviceRequest.getString("con_sn"));
                } else {
                    reqDataDetail.put("IFS", "STAS42");
                    responseData = mesApiUtils.doPost(reqDataDetail);
                    LOG.info("生产模式：Oracle返回扫码信息："+responseData.toString()+"   载具SN为："+deviceRequest.getString("con_sn"));
                }


                if (responseData.getString("CODE").equals("0")) {
                    result.setResult_flag("OK");
                    result.setResult_message(responseData.getString("MSG"));
                    switch (responseData.getString("STATION")) {
                        case "C0101":
                            result.setFrame_stion("B"); //老化架位置
                            break;
                        case "C0103":
                            result.setFrame_stion("B"); //老化架位置
                            break;
                        default:
                            result.setFrame_stion("A"); //老化架位置
                            break;
                    }

                    result.setMaterial_stion(responseData.getString("LOCATION")); //产品所在位置

                } else if (responseData.getString("CODE").equals("1")) {
                    result.setResult_flag("1");//执行方法异常
                    result.setResult_message(responseData.getString("MSG"));
                } else if (responseData.getString("CODE").equals("2")) {
                    result.setResult_flag("2");
                    result.setResult_message("NO");
                } else if (responseData.getString("CODE").equals("4")) {
                    result.setResult_flag("4");
                    result.setResult_message("NO");
                } else if (responseData.getString("CODE").equals("5")) {
                    result.setResult_flag("5");
                    result.setResult_message("NO");
                } else {

                    while (true) {
                        LOG.info(responseData.getString("MSG") + ",等待AGV补充货架，while循环,设备编码：" + deviceRequest.getString("device_sn"));

                        try {
                            Thread.sleep(15000);

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        String lineSql = "SELECT LINE_CD  FROM IPLANT1.C_IPLANT_E2_T WHERE ET_CD= '" + deviceRequest.getString("device_sn") + "' ";
                        Map<String, Object> map = this.getJdbcTemplate().queryForMap(lineSql);

                        String oldSql = "SELECT COUNT(1) cou FROM MES1.OLDTEST_STATION WHERE SHELF_ID IS NOT NULL AND LINE_CD = '" + map.get("LINE_CD").toString() + "' ";
                        Map<String, Object> map2 = this.getJdbcTemplate().queryForMap(oldSql);

                        if (Integer.parseInt(map2.get("cou").toString()) > 0) {

                            break;
                        }
                    }

                    //reqDataDetail.put("IFS", "STAS42");
                    reqDataDetail.put("IN_VEHICLES", deviceRequest.getString("con_sn"));
                    reqDataDetail.put("IN_ET_CD", deviceRequest.getString("device_sn"));
                    reqDataDetail.put("IN_TUS", "2"); //代表查一次


                    JSONObject responseDataTwo = mesApiUtils.doPost(reqDataDetail);
                    switch (responseDataTwo.getString("STATION")) {
                        case "C0101":
                            result.setFrame_stion("B"); //老化架位置
                            break;
                        case "C0103":
                            result.setFrame_stion("B"); //老化架位置
                            break;
                        default:
                            result.setFrame_stion("A"); //老化架位置
                            break;
                    }
                    result.setResult_flag("OK");
                    result.setResult_message(responseDataTwo.getString("MSG"));
                    result.setMaterial_stion(responseDataTwo.getString("LOCATION")); //产品所在位置
                    flag = false;

                }

            } else {
                result.setResult_flag("OK");
                result.setResult_message("无老化架，等待补充！");
            }
        } catch (Exception e) {
            result.setResult_flag("9");
            result.setResult_message("FAIL");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 前测切换模式
     *
     * @param msg
     * @return
     */
    public ScadaResponShow xidiaoCg(Object msg) {
        ScadaResponShow sh = new ScadaResponShow();
        JSONObject json = new JSONObject(msg.toString());
        try {
            redisUtils.set("iscg", json.getString("work_sn"));
            if (json.getString("work_sn").equals("OK")) {
                LOG.info("收到前测切换参观模式");
                sh.setResult_message("收到前测切换参观模式");
            } else {
                LOG.info("收到前测切换生产模式");
                sh.setResult_message("收到前测切换生产模式");
            }
            sh.setResult_flag("OK");

        } catch (Exception e) {
            sh.setResult_flag("NG");
            e.printStackTrace();
        }
        return sh;
    }


    /**
     * 上位机通知 回收空框，7.创建回收空料箱任务
     *
     * @param msg
     * @return
     * @throws JSONException
     */
    public ScadaQuery sendAgvSc(Object msg) throws JSONException {
        //上位机传过来回收空框的设备点位，scada根据设备点位查出对应的AGV回收空框点位，传给agv
        ScadaQuery sq = new ScadaQuery();
        JSONObject json = new JSONObject(msg.toString());
        String matPoint = json.getString("mat_point"); //获取设备点位
        //从数据库根据设备点位查出AGV点位
        String sql = "select AGV_POINT,LINE_CD from ET_IP_AGV where MAT_POINT='" + matPoint + "' ";
        Map<String, Object> map = template.queryForMap(sql);
        Object agvPoint = map.get("AGV_POINT");
        String line = map.get("LINE_CD").toString(); //线别

        JSONObject js = new JSONObject();
        js.put("srcWbCode", agvPoint); //空料箱回收上箱点编码(起点)

        count++;
        String hSql = "";
        if (count % 2 == 0) {
            hSql = "select MAT_POINT,AGV_POINT from (select MAT_POINT,AGV_POINT from ET_IP_AGV s where s.line_cd='" + line + "' and s.status=1 order by AGV_POINT asc) a where rownum=1";
        } else {
            hSql = "select MAT_POINT,AGV_POINT from (select MAT_POINT,AGV_POINT from ET_IP_AGV s where s.line_cd='" + line + "' and s.status=1 order by AGV_POINT desc) a where rownum=1";
        }

        Map<String, Object> emptyK = new HashMap<>();
        try {
            emptyK = template.queryForMap(hSql);
        } catch (Exception e) {
            LOG.info(line + "滚筒线回收空箱的点位状态都为0,请重新放置空框，点位为：" + matPoint);
            emptyK.put("AGV_POINT", "CG0401");
            emptyK.put("MAT_POINT", "B");
        }


        if (agvPoint.toString().equals("CG0104")) { //固定回到主板接空框机
            String zSql = "select status from ET_IP_AGV where agv_point ='CG0502'"; //查询一线主板回空框机可不可用
            Map<String, Object> map2 = template.queryForMap(zSql);
            if (map2.get("status").toString().equals("3")) {
                LOG.info("一线主板回空框任务已创建");
                js.put("targetEmptyRecyleWb", "CG0502");//空框回收下箱点(给我们送空箱的点)*/
                js.put("isSta", "0");
                hSql = "update ET_IP_AGV set STATUS= 0 where MAT_POINT='ECD102' ";
                template.update(hSql);
                sq = agvMt.createEmpty(js);
            } else {
                LOG.info("一线主板回空框机没有叫空框，不可用,此次呼叫储存，待设备呼叫空框时接走");
                //把此次呼叫空框存起来
                String saveSql = "UPDATE ET_IP_AGV SET STATUS = 11 WHERE ET_IP='191' ";
                template.update(saveSql);
            }


        } else if (agvPoint.toString().equals("CG0112")) { //固定回到一线机箱接空框机
            String zSql = "select status from ET_IP_AGV where agv_point ='CG0102'"; //查询一线机箱回空框机可不可用
            Map<String, Object> map2 = template.queryForMap(zSql);
            //	if(map2.get("status").toString().equals("4")) {
            LOG.info("一线机箱回空框任务已创建");
            js.put("targetEmptyRecyleWb", "CG0102");//空框回收下箱点(给我们送空箱的点)*/
            js.put("isSta", "0");
            sq = agvMt.createEmpty(js);
            //	}else {
            //LOG.info("一线机箱回空框没有呼叫空框");
            //	}

        } else if (agvPoint.toString().equals("CG0204")) { // 二线的主板回空框
            String zSql = "select status from ET_IP_AGV where agv_point ='CG0601'"; //查询二线主板回空框机可不可用
            Map<String, Object> map2 = template.queryForMap(zSql);
            if (map2.get("status").toString().equals("3")) {
                js.put("targetEmptyRecyleWb", "CG0601");//主板空框回收下箱点(给我们送空箱的点)*/
                js.put("isSta", "0");
                hSql = "update ET_IP_AGV set STATUS= 0 where MAT_POINT='ECD201' ";
                template.update(hSql);
                sq = agvMt.createEmpty(js);
            } else {
                LOG.info("二线主板回空框机没有叫空框，此次呼叫储存，待设备呼叫空框时接走");
                //把此次呼叫空框存起来
                String saveSql = "UPDATE ET_IP_AGV SET STATUS = 12 WHERE ET_IP='192' ";
                template.update(saveSql);
            }

        } else if (agvPoint.toString().equals("CG0212")) { //二线的机箱回空框

            String zSql = "select status from ET_IP_AGV where agv_point ='CG0202'"; //查询二线机箱回空框机可不可用
            Map<String, Object> map2 = template.queryForMap(zSql);
            //	if(map2.get("status").toString().equals("4")) {
            LOG.info("二线机箱回空框任务已创建");
            js.put("targetEmptyRecyleWb", "CG0202");//空框回收下箱点(给我们送空箱的点)*/
            js.put("isSta", "0");
            sq = agvMt.createEmpty(js);
            //	}else {
            //LOG.info("二线机箱回空框没有呼叫空框");
            //	}

        } else { //否则就回到滚筒线接空框
            if (emptyK.size() > 0) {
                if (matPoint.equals("ECD106") || matPoint.equals("ECD107")) {
                    js.put("targetEmptyRecyleWb", "CG0301");
                } else if (matPoint.equals("ECD206") || matPoint.equals("ECD207")) {
                    js.put("targetEmptyRecyleWb", "CG0401");
                } else {
                    js.put("targetEmptyRecyleWb", emptyK.get("AGV_POINT").toString());//空框回收下箱点(给我们送空箱的点)*/
                }
                js.put("gunPoint", emptyK.get("MAT_POINT").toString());
                js.put("isSta", "1");
                sq = agvMt.createEmpty(js);
            } else {
                LOG.info("滚筒线回空框点位不可用,线别：" + line);
            }
        }
        sq.setResult_flag("OK");
        return sq;
    }

    /**
     * 滚筒线收到空框告诉SCADA
     *
     * @param msg
     * @return
     * @throws JSONException
     */
    public ScadaQuery sendAgvScIn(Object msg) throws JSONException {

        JSONObject json = new JSONObject(msg.toString());
        ScadaQuery sq = new ScadaQuery();

        sq.setResult_flag("OK");
        LOG.info("滚筒线收到了空框,点位为：");
        String sqls = "update ET_IP_AGV set STATUS =0 where MAT_POINT='" + json.getString("mat_point") + "' ";
        template.update(sqls);
        return sq;
    }

    /**
     * 光驱或硬盘叫料，修改状态
     *
     * @param msg
     * @return
     * @throws JSONException
     */
    public ScadaQuery sdCome(Object msg) throws JSONException {

        JSONObject json = new JSONObject(msg.toString());
        ScadaQuery sq = new ScadaQuery();
        String sql = "select CALL_MATER_SEQ.NEXTVAL tt from dual";
        try {


            Map<String, Object> queryForMap = template.queryForMap(sql);
            Object object = queryForMap.get("tt");//获取序列自增id
            //根据设备编号获取其他信息
            sql = "SELECT T1.MAT_TYPE ITEM_GRP_CD,T1.ROUT_CD STATION_CD,T2.WP_CD WP_CD FROM IPLANT1.C_IPLANT_E2_T T1 LEFT JOIN MES1.MES_STATION T2 " +
                    "ON T1.ROUT_CD = T2.STATION_CD WHERE T1.ET_CD=?";
            Map<String, Object> map = template.queryForMap(sql, json.getString("mat_point"));
            switch (json.getString("mat_point")) {
                case "ECD106":
                    LOG.info("一线光驱叫料,点位为：" + json.getString("mat_point"));
                    break;
                case "ECD107":
                    LOG.info("一线硬盘叫料,点位为：" + json.getString("mat_point"));
                    break;
                case "ECD206":
                    LOG.info("二线光驱叫料,点位为：" + json.getString("mat_point"));
                    break;
                case "ECD207":
                    LOG.info("二线硬盘叫料,点位为：" + json.getString("mat_point"));
                    break;
                default:
                    LOG.info("其他设备叫料,设备号为：" + json.getString("mat_point"));
                    break;
            }
            sq.setFlow_code(json.getString("mat_point"));
            sq.setResult_flag("OK");
            sql = "INSERT INTO MES1.CALL_MATERIAL(ID,ITEM_GRP_CD,CRT_ID,CRT_DT,WP_CD,ET_CD,STATION_CD,STATUS) values (?,?,'admin',sysdate,?,?,?,0)";
            template.update(sql, object, map.get("ITEM_GRP_CD"), map.get("WP_CD"), json.getString("mat_point"), map.get("STATION_CD"));
        } catch (Exception e) {
            sq.setResult_flag("NG");
            LOG.error("设备叫料异常");
            e.printStackTrace();
        }
        return sq;
    }

    /**
     * 上位机通知scada产品是否成功放老化架上 ，如果是尾数或装满  agv可以来搬运老化架
     *
     * @param msg
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public ScadaRespon createAgvBan(Object msg) throws JSONException, IOException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        ScadaRespon sr = new ScadaRespon();
        JSONObject jsn = new JSONObject();
        try {
            String isputOK = deviceRequest.get("old_status").toString(); //产品是否成功放到老化架  0：成功  1：失败


            JSONObject reqDataDetail = new JSONObject();
            reqDataDetail.put("IFS", "STAS442");

            if (isputOK.equals("0")) {
                //调用mes方法，老化架产品位置++
                LOG.info("产品成功放到老化架上");
                reqDataDetail.put("IN_OLD_STATUS", "0"); //产品是否放置成功

                sr.setResult_flag("OK");
                sr.setResult_message("YES");
            } else {
                //失败，不调用mes方法，产品位置不变
                LOG.info("产品没有成功放到老化架上");
                reqDataDetail.put("IN_OLD_STATUS", "1"); //产品是否放置成功
                sr.setResult_flag("OK");
                sr.setResult_message("YES");
            }

            String oldLocation = ""; //接收老化架位置
		/*	String device = deviceRequest.getString("device_sn");
			if (device.equals("ECDLC027")) {
				switch (deviceRequest.getString("frame_stion")) {
					case "A":
						oldLocation = "C0104"; //老化架位置
						break;
					default:
						oldLocation = "C0103"; //老化架位置
						break;
				}
			} else {
				switch (deviceRequest.getString("frame_stion")) {
					case "A":
						oldLocation = "C0102"; //老化架位置
						break;
					case "B":
						oldLocation = "C0101"; //老化架位置
						break;
					default:
						break;
				}
			}*/
            switch (deviceRequest.getString("frame_stion")) {
                case "A":
                    oldLocation = "C0102"; //老化架位置
                    break;
                case "B":
                    oldLocation = "C0101"; //老化架位置
                    break;
                case "C":
                    oldLocation = "C0104"; //老化架位置
                    break;
                default:
                    oldLocation = "C0103"; //老化架位置
                    break;
            }

            reqDataDetail.put("IN_FRAME_STION", oldLocation); //老化架位置
            reqDataDetail.put("IN_MATERIAL_STION", deviceRequest.getString("material_stion")); //老化架产品所放位置
            reqDataDetail.put("IN_ET_CD", deviceRequest.getString("device_sn")); //设备编码
            JSONObject responseData = mesApiUtils.doPost(reqDataDetail);

            if (responseData.getString("STATUS").equals("1")) {

                //创建产线到人工插线区搬运任务
                jsn.put("taskType", "plToWokpw");
                jsn.put("srcWb", oldLocation);
                LOG.info("通知AGV来产线搬运老化架，位置信息：" + jsn);
                agvBan.agvHandlingTask(jsn);
            }

        } catch (Exception e) {
            sr.setResult_flag("OK");
            sr.setResult_message("YES");
            e.printStackTrace();
        }
        return sr;
    }
}


/**
 *
 */