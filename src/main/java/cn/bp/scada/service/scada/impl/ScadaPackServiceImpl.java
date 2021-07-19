package cn.bp.scada.service.scada.impl;

import cn.bp.scada.common.utils.redis.RedisUtils;
import cn.bp.scada.controller.sap.SapCpletionConfir;
import cn.bp.scada.controller.scada.ScadaAndAgvMt;
import cn.bp.scada.controller.sap.SapLabelPrint;
import cn.bp.scada.mapper.scada.Pack;
import cn.bp.scada.modle.scada.ScadaRequest;
import cn.bp.scada.modle.scada.ScadaRespon;
import cn.bp.scada.sap.PrintLabel;
import cn.bp.scada.sap.SapConnUtils;
import cn.bp.scada.service.scada.ScadaPackService;
import cn.bp.scada.common.utils.dbhelper.DBHelper;
import cn.bp.scada.common.utils.MesApiUtils;
import cn.bp.scada.common.utils.PrimaryHelper;
import cn.bp.scada.webservice.JsonDealUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoTable;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class ScadaPackServiceImpl implements ScadaPackService {

    @Resource
    private JdbcTemplate template;
    @Autowired
    private SapLabelPrint sapLable;
    @Autowired
    private SapConnUtils sapUtils;
    @Autowired
    private PrintLabel printLabel;
    @Resource
    private DBHelper db;
    @Resource
    private PrimaryHelper ph;
    @Resource
    private MesApiUtils mesApiUtils;
    @Resource
    private ScadaAndAgvMt smt;
    @Resource
    private Pack pack;
    @Autowired
    private RedisUtils redisUtil;
    @Resource
    private SapCpletionConfir confir;

    private Lock lock = new ReentrantLock();
    private static Map<String, Object> map = new HashMap<String, Object>();

    static {
        map.put("C213", "192.168.10.215");
        map.put("C013", "192.168.10.180");
        map.put("sort", "192.168.10.234"); //分拣的设备IP
        map.put("pack", "192.168.10.98");    //包装线IP
    }

    @Override
    public String labelPrintNh(JSONObject json) {
        JSONObject js = new JSONObject();
        try {
            JSONObject jsonMes = json.getJSONArray("REQ").getJSONObject(0).getJSONObject("REQ_DATA");
            String sn = jsonMes.getString("CRATE");

            System.out.println("机箱SN:" + sn);
            //查sap打印信息
            Map<String, Object> nhPrint = sapLable.testSap(sn, "");
            if (nhPrint.get("erp").equals("0")) {

                js.put("CODE", "0");
                js.put("MSG", "成功");
            } else {
                js.put("CODE", "1");
                js.put("MSG", "ERP获取标签失败");
            }

        } catch (Exception e) {
            js.put("CODE", "2");
            js.put("MSG", "异常，打印失败");
            e.printStackTrace();
        }

        System.out.println("能耗标签打印返回的json:" + js.toString());
        return js.toString();
    }

    @Override
    public ScadaRespon packLine(JSONObject deviceRequest) {
        ScadaRespon result = new ScadaRespon();
        try {

            String line = deviceRequest.getString("device_sn"); //线别
            String status = deviceRequest.getString("status"); //状态
            String sql = "UPDATE PACKING_MACHINE_LINE SET MACHINE_STA=? WHERE SORT_LINE=?";
            template.update(sql, status, line);
            result.setResult_flag("OK");
            sql = "UPDATE PACKING_MACHINE_CODE SET USE_YN='N'";
            template.update(sql);
        } catch (Exception e) {
            result.setResult_flag("NG");
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String weighStatus(JSONObject json) {
        JSONObject js = new JSONObject();
        try {

            JSONObject jsonMes = json.getJSONArray("REQ").getJSONObject(0).getJSONObject("REQ_DATA");
            String status = jsonMes.get("STATUS").toString();
            String sn = jsonMes.get("SN").toString();
            System.out.println("称重状态：" + status);

           /* ScadaRequest sr = new ScadaRequest();
            sr.setOp_flag("B104");
            sr.setStatus(status);
            sr.setBox_sn(sn);
            sr.setEt_ip(map.get("pack").toString());
            JSONObject device = smt.toDevice(sr);
            if (device.getString("result_flag").equals("OK")) {
                js.put("CODE", "0");
                js.put("MSG", "成功"+sn);
            } else {
                js.put("CODE", "1");
                js.put("MSG", "上位机程序异常");
            }
*/
            js.put("CODE", "0");
            js.put("MSG", "成功" + sn);
        } catch (Exception e) {
            js.put("CODE", "2");
            js.put("MSG", "失败");
            e.printStackTrace();
        }
        log.info("返回的json:" + js.toString());
        return js.toString();
    }

    /**
     * 线下包装--打印接口方法
     */
    @Override
    public String sapLabelPrintOut1(JSONObject json) {
        JSONObject js = new JSONObject();
        JSONObject reqDataDetail1 = new JSONObject();
        reqDataDetail1.put("IFS", "STA9001");
        try {
            JSONObject jsonMes = json.getJSONArray("REQ").getJSONObject(0).getJSONObject("REQ_DATA");
            String crate = jsonMes.getString("CRATE");//机器SN
            String work = jsonMes.getString("WORK");//
            String user = jsonMes.getString("USER");//用户ID
            String process = jsonMes.getString("PROCESS");//
            String keypn = jsonMes.getString("KEYPN");//键盘QN

            //根据机箱SN获取订单
            String orderSql = "SELECT AUFNR FROM (SELECT AUFNR FROM PRODUCT_SN WHERE SERNR='" + crate + "' ORDER BY CRT_DT DESC) WHERE ROWNUM=1";
            Map<String, Object> orderMap = new HashMap<>();
            if (work.equals("G00101")) {
                orderMap = template.queryForMap(orderSql);
                redisUtil.set("G00101", orderMap.get("AUFNR").toString());
                log.info("G00101订单号：" + redisUtil.get("G00101"));
            } else {
                orderMap = template.queryForMap(orderSql);
                redisUtil.set("G00101", orderMap.get("AUFNR").toString());
                log.info("G00101订单号：" + redisUtil.get("G00101"));
            }
            //
            Map<String, Object> map = new HashMap<>();
            map.put("crate", crate);
            map.put("work", work);
            map.put("user", user);
            map.put("process", process);
            map.put("keypn", keypn);
            map.put("boxSn", crate); //外箱SN
            //查sap打印信息
            Map<String, Object> testSap1 = sapLable.testSap(crate, redisUtil.get("G00101").toString());
            testSap1.put("O_NO", Integer.parseInt(redisUtil.get("G00101").toString()) + "");
            testSap1.put("JX_SN", crate);
            //调取打印方法
            String url = "//192.168.10.159/FilePrint//";//打印外箱工控机ip地址
            String label = printLabel.masterLabelPack(crate, url, testSap1);
            if (!label.equals("0")) {
                js.put("CODE", "3");
                js.put("MSG", "打印标签方法调取失败，打印失败");
            } else {
                //写缓存表
               /* Map<String, Object> stringObjectMap = insertCatch(testSap1);
                pack.insertPack(stringObjectMap);*/
                //调用保存物料接口--键盘
                reqDataDetail1.put("IN_CODE", "G00101");
                reqDataDetail1.put("JX_CODE", crate);
                reqDataDetail1.put("JP_CODE", keypn);
                reqDataDetail1.put("USERID", user);
                reqDataDetail1.put("WP_CD", process);
                reqDataDetail1.put("STATION_CD", work);
                reqDataDetail1.put("BOX_SN", crate);
                JSONObject jsonMesRus = mesApiUtils.doPost(reqDataDetail1);
                if (jsonMesRus.getString("CODE").equals("1")) {
                    js.put("CODE", "4");
                    js.put("MSG", "物料保存接口返回失败");
                } else {
                    js.put("CODE", "0");
                    js.put("MSG", "成功");
                }
                //告诉上位机可以放行
                System.out.println("返回的json:" + js.toString());
            }
        } catch (Exception e) {
            js.put("CODE", "3");
            js.put("MSG", "接口异常，打印失败");
            log.error(e.toString(), e);
        } finally {

        }
        return js.toString();
    }

    @Override
    //  @Transactional(isolation = Isolation.DEFAULT) //设置事务隔离级别
    public synchronized String sapLabelPrintOut(JSONObject json) {
        JSONObject js = new JSONObject();
        JSONObject reqDataDetail1 = new JSONObject();
        reqDataDetail1.put("IFS", "STA90");
        lock.lock();
        try {

            JSONObject jsonMes = json.getJSONArray("REQ").getJSONObject(0).getJSONObject("REQ_DATA");
            String crate = jsonMes.getString("CRATE");
            String work = jsonMes.getString("WORK");
            String user = jsonMes.getString("USER");
            String process = jsonMes.getString("PROCESS");
            String keypn = jsonMes.getString("KEYPN");

            //根据机箱SN获取订单
            String orderSql = "SELECT AUFNR FROM (SELECT AUFNR FROM PRODUCT_SN WHERE SERNR='" + crate + "' ORDER BY CRT_DT DESC) WHERE ROWNUM=1";
            Map<String, Object> orderMap = new HashMap<>();
            if (work.equals("G001")) {
                orderMap = template.queryForMap(orderSql);
                redisUtil.set("G001", orderMap.get("AUFNR").toString());
                log.info("G001订单号：" + redisUtil.get("G001"));
            } else {
                orderMap = template.queryForMap(orderSql);
                redisUtil.set("G002", orderMap.get("AUFNR").toString());
                log.info("G002订单号：" + redisUtil.get("G002"));
            }
            //
            Map<String, Object> map = new HashMap<>();
            map.put("crate", crate);
            map.put("work", work);
            map.put("user", user);
            map.put("process", process);
            map.put("keypn", keypn);
            map.put("boxSn", crate); //外箱SN
            String upSql = "";


            String seleSql = "SELECT COUNT(1) COU FROM PACKING_MACHINE_LINE WHERE MACHINE_STA=1";
            Map<String, Object> lineMap = template.queryForMap(seleSql);

            seleSql = "SELECT COUNT(1) COU FROM PACKING_MACHINE_CODE WHERE JX_CODE IS NOT NULL";
            if (Integer.parseInt(lineMap.get("COU").toString()) > 1) { //证明开线两条
                upSql = "UPDATE PACKING_MACHINE_CODE SET JX_CODE=?, JP_CODE=?,"
                        + " USERID=?, WP_CD=?, STATION_CD=?, BOX_SN=?,USE_YN='Y' WHERE SORT_LINE=?";
                update(upSql, map);


                Map<String, Object> map2 = template.queryForMap(seleSql);
                if (Integer.parseInt(map2.get("COU").toString()) > 1) { //必须要存在两条数据，
                    //查sap打印信息
                    seleSql = "select JX_CODE from PACKING_MACHINE_CODE order by SORT_LINE";
                    List<Map<String, Object>> list = template.queryForList(seleSql);
                    String jx1 = list.get(0).get("JX_CODE").toString();
                    String jx2 = list.get(1).get("JX_CODE").toString();

                    Map<String, Object> testSap1 = sapLable.testSap(jx1, redisUtil.get("G001").toString());
                    testSap1.put("O_NO", Integer.parseInt(redisUtil.get("G001").toString()) + "");
                    testSap1.put("JX_SN", jx1);

                    Map<String, Object> testSap2 = sapLable.testSap(jx2, redisUtil.get("G002").toString());
                    testSap2.put("O_NO", Integer.parseInt(redisUtil.get("G002").toString()) + "");
                    testSap2.put("JX_SN", jx2);
                    //写缓存表

                    Map<String, Object> stringObjectMap = insertCatch(testSap1);
                    pack.insertPack(stringObjectMap);
                    Map<String, Object> stringObjectMap2 = insertCatch(testSap2);
                    pack.insertPack(stringObjectMap2);

                /*    String epSql = "UPDATE PACKING_MACHINE_CODE SET USE_YN='N' ";
                    template.update(epSql);*/
                    //调用保存物料接口

                    reqDataDetail1.put("IN_CODE1", "G001");
                    reqDataDetail1.put("IN_CODE2", "G002");
                    JSONObject jsonMesRus = mesApiUtils.doPost(reqDataDetail1);
                    if (jsonMesRus.getString("CODE").equals("1")) {
                        js.put("CODE", "4");
                        js.put("MSG", "物料保存接口返回失败");
                    } else {
                        js.put("CODE", "0");
                        js.put("MSG", "成功");
                    }

                   /* upSql= "UPDATE PACKING_MACHINE_CODE SET USE_YN='N' ";
                    template.update(upSql);*/

                    //告诉上位机可以放行
                    ScadaRequest sr = new ScadaRequest();
                    sr.setOp_flag("C06");
                    sr.setEt_ip("192.168.10.98");
                    smt.toDevice(sr);

                } else {
                    js.put("CODE", "0");
                    js.put("MSG", "成功");
                }


            } else {
                //开线一条
                //查询PACKING_MACHINE_CODE这个表是否存在一条
                upSql = "UPDATE PACKING_MACHINE_CODE SET JX_CODE=?, JP_CODE=?,"
                        + " USERID=?, WP_CD=?, STATION_CD=?, BOX_SN=? WHERE SORT_LINE=?";
                update(upSql, map);

                Map<String, Object> map2 = template.queryForMap(seleSql);
                if (Integer.parseInt(map2.get("COU").toString()) > 0) {
                    //查sap打印信息
                    Map<String, Object> testSap1 = sapLable.testSap(crate, orderMap.get("AUFNR").toString());
                    testSap1.put("O_NO", Integer.parseInt(orderMap.get("AUFNR").toString()) + "");
                    testSap1.put("JX_SN", crate);
                    if (testSap1.get("erp").equals("0")) {
                        log.info("包装获取ERP打印信息，返回成功");
                    } else {
                        log.info("包装获取ERP打印信息，返回失败");
                    }

                    //写缓存表
                    Map<String, Object> stringObjectMap = insertCatch(testSap1);
                    pack.insertPack(stringObjectMap);

                    //调用保存物料接口
                    reqDataDetail1.put("IN_CODE1", "G001");
                    JSONObject jsonMesRus = mesApiUtils.doPost(reqDataDetail1);
                    if (jsonMesRus.getString("CODE").equals("1")) {
                        js.put("CODE", "4");
                        js.put("MSG", "物料保存接口返回失败");
                    } else {
                        js.put("CODE", "0");
                        js.put("MSG", "成功");
                    }

                    //告诉上位机可以放行
                    ScadaRequest sr = new ScadaRequest();
                    sr.setOp_flag("C06");
                    sr.setEt_ip("192.168.10.98");
                    smt.toDevice(sr);


                } else {
                    js.put("CODE", "2");
                    js.put("MSG", "CODE表不存在数据");
                }

            }

            System.out.println("返回的json:" + js.toString());
        } catch (Exception e) {
            js.put("CODE", "3");
            js.put("MSG", "接口异常，打印失败");
            log.error(e.toString(), e);
            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly(); //手动回滚事务
        } finally {
            lock.unlock(); //释放锁
        }

        return js.toString();
    }

    @Override
    public ScadaRespon getlable() {
        ScadaRespon result = new ScadaRespon();
        try {

            String sql = "select * from (select * from TEMP_PRINT where status=0 order by ID asc) where rownum=1 ";
            Map<String, Object> forMap = template.queryForMap(sql);

            ObjectMapper s = new JsonDealUtils();
            String obj = s.writeValueAsString(forMap); //null转为空字符串
            JSONObject json = new JSONObject(obj); //json字符串转为json对象

            //根据机箱SN获取订单
            String orderSql = "SELECT AUFNR FROM PRODUCT_SN WHERE SErnr=? ";
            // Map<String, Object> maps = template.queryForMap(orderSql, json.get("JX_SN"));
            log.info("上位机获取标签再查一次ERP");
            Map<String, Object> maps = sapLable.testSap(json.get("JX_SN").toString(), "");

            result.setResult_flag("OK");
            result.setCpu_nm(json.get("CPU_NM") + ""); //
            result.setYp_nm(json.get("YP_NM") + "");
            result.setPro_nm(json.get("PRO_NM") + ""); //商品编码
            result.setJx_sn(json.get("JX_SN") + "");
            result.setO_no(maps.get("O_NO").toString() + "");
            result.setNc_nm(json.get("NC_NM") + "");
            result.setPro_cd(json.get("PRO_CD") + "");  //产品编码
            result.setItem_cd(json.get("ITEM_CD") + "");

            result.setCd(json.get("CD") + "");
            result.setOther(json.get("OTHER") + "");
            result.setNvg_code(json.get("NVG_CODE") + "");
            result.setWay(json.get("WAY") + "");
            result.setNet_card(json.get("NET_CARD") + "");
            result.setSale_no(json.get("SALE_NO") + "");
            result.setPro_no(json.get("PRO_NO") + "");
            result.setItem_nm(json.get("ITEM_NM") + "");
            result.setFirst(maps.get("proDt").toString() + "");//订单下达时间

            String sql2 = "update TEMP_PRINT set status=1,UP_DT=sysdate where id=(select id from (select id from TEMP_PRINT where status=0 order by ID asc) where rownum=1 ) ";
            template.update(sql2);

        } catch (Exception e) {
            result.setResult_flag("NG");
            result.setResult_message("没有标签，获取失败");
            log.error(e.toString(), e);
            log.error("再次警告！没有机箱贴标上位机为什么来取标！");
        }
        return result;
    }

    @Override
    public ScadaRespon labelOver() {
        ScadaRespon result = new ScadaRespon();
        try {

            result.setResult_flag("OK");

        } catch (Exception e) {
            result.setResult_flag("NG");
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String sapGetWeighData() {
        ScadaRequest sr = new ScadaRequest();
        JSONObject js = new JSONObject();
        try {

            sr.setOp_flag("B103");
            sr.setEt_ip(map.get("pack").toString());
            JSONObject device = smt.toDevice(sr);
            if (device.getString("result_flag").equals("OK")) {
                js.put("CODE", "0");
                js.put("MSG", "成功");
                js.put("WEIGHT1", device.get("weight1")); //重量1
                js.put("WEIGHT2", device.get("weight2")); //重量2
            } else if (device.getString("result_flag").equals("NO")) {

                js.put("CODE", "1");
                js.put("MSG", "没有获取到称重数据");
            } else {
                js.put("CODE", "2");
                js.put("MSG", "上位机程序异常");
            }

            System.out.println("返回的json:" + js.toString());
            String sql = " Delete from MES1.PACKAGE_WEIGHT_TEMP_T";
            template.update(sql);
        } catch (Exception e) {
            js.put("CODE", "3");
            js.put("MSG", "失败");
            e.printStackTrace();
        }
        return js.toString();
    }

    @Override
    public String discharged(JSONObject json) {
        JSONObject jsonMes = json.getJSONArray("REQ").getJSONObject(0).getJSONObject("REQ_DATA");
        JSONObject js = new JSONObject();
        try {

            String type = jsonMes.getString("TYPE");
            String work = jsonMes.getString("WORK"); //工位

            ScadaRequest sr = new ScadaRequest();
            sr.setOp_flag("S013");
            sr.setStatus(type);

            if (type.equals("PQC")) {

                sr.setEt_ip(map.get(work).toString());
                sr.setDevice_sn(type);
            } else {
                sr.setEt_ip(map.get(type).toString());
                sr.setWork_sn(work);
            }


            JSONObject device = smt.toDevice(sr);

            if (device.getString("result_flag").equals("OK")) {
                js.put("CODE", "0");
                js.put("MSG", "成功");
            } else {
                js.put("CODE", "1");
                js.put("MSG", "上位机程序异常，无响应");
            }

            System.out.println("PQC与分拣触发放行返回的json:" + js.toString());
        } catch (Exception e) {
            js.put("CODE", "2");
            js.put("MSG", "失败");
            e.printStackTrace();
        }
        return js.toString();
    }

    @Override
    public String inGoes(String data, String sn) {
        //判断是不是线下包装
        String querySql1 = "SELECT COUNT(1) FROM PRODSTS_HIS " +
                "WHERE VSN=(SELECT VSN FROM (SELECT VSN FROM PRODSTS_RM WHERE PRODUCTSN=" + sn + " ORDER BY CREATIME  DESC) WHERE ROWNUM=1) AND WORKNO='G00101'";
        Map<String, Object> stringObjectMap1 = template.queryForMap(querySql1);
        if (stringObjectMap1.get("COUNT(1)").toString().equals("1")) {
            return "线下包装--大小号标签校验 OK！";
        }
        try {
            String querySql = "SELECT COUNT(1) FROM OQCRECORD WHERE SN=" + sn + " AND O_NO=" +
                    "(SELECT AUFNR FROM (SELECT AUFNR from PRODUCT_SN WHERE SERNR=" + sn + " ORDER BY CRT_DT DESC) WHERE ROWNUM=1)";
            Map<String, Object> stringObjectMap = template.queryForMap(querySql);
            if (stringObjectMap.get("COUNT(1)").toString().equals("1")) {
                ScadaRequest sr1 = new ScadaRequest();
                sr1.setOp_flag("B104");
                sr1.setStatus("1");
                sr1.setBox_sn(sn);
                sr1.setEt_ip(map.get("pack").toString());
                JSONObject device = smt.toDevice(sr1);
                log.info("进入OQC挡板抬起：" + device.toString() + " --sn为：" + sn);
            } else {
                log.info("不进入OQC" + "--sn为：" + sn);
            }
        } catch (DataAccessException e) {
            log.info("OQC弹出报错" + "--sn为：" + sn);
            e.printStackTrace();
        } catch (JSONException e) {
            log.info("OQC弹出报错" + "--sn为：" + sn);
            e.printStackTrace();
        }
        ScadaRequest sr = new ScadaRequest();
        JSONObject json = null;
        if (data.equals("1")) {
            //告诉上位机可以放行
            sr.setOp_flag("C07");
            sr.setStatus(data);
            sr.setEt_ip("192.168.10.98");
            json = smt.toDevice(sr);

        }
        return json.getString("result_flag");
    }

    @Override
    public String weighGoes(String data) {
        JSONObject js = new JSONObject();
        //告诉上位机可以放行
        ScadaRequest sr = new ScadaRequest();
        JSONObject json = null;
        if (data.equals("0")) {
            sr.setOp_flag("C08");
            sr.setStatus(data);
            sr.setEt_ip("192.168.10.98");
            json = smt.toDevice(sr);
        }
        if (json.getString("result_flag").equals("OK")) {
            js.put("CODE", "0");
        } else {
            js.put("CODE", "1");
        }

        return js.toString();

    }

    /**
     * 按照订单手动报工
     *
     * @return
     */
    @RequestMapping("/myself/repairSN")
    private void repairSnAtoOrder() {
        String sql = "SELECT SERNR FROM PRODUCT_SN WHERE AUFNR='000060032515'";//修改订单号
        //Map<String, Object> snMap = template.queryForMap(sql);
        List<Map<String, Object>> snMap = template.queryForList(sql);
        for (int i = 0; snMap.size() > i; i++) {
            repairSn(snMap.get(i).get("sernr").toString());
        }
    }

    @Override
    public String repairSn(String data) {
        String result = "";
        try {
            result = confir.cpletionConfir(data, "50");//完工确认
            String sql = "UPDATE PRODUCT_SN SET STATE=1 ,UP_DT=sysdate,DES=? WHERE SERNR=?";
            template.update(sql, result, data);
        } catch (Exception e) {
            log.info(e.toString(), e);
            result = "操作失败";
        }
        return result;
    }

    @Override
    public String repairSnAll(String data) {
        int failureNum = 0;
        StringBuilder failureMsg = new StringBuilder();
        String[] strarray = data.split("\\|");
        try {
            JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象
            JCoFunction function = sapUtils.getFunction("ZRFC_AUFNR_BG");//获取到函数
            for (int i = 0; i < strarray.length; i++) {
                if (strarray[i].length() == 9) {
                    String result = confir.cpletionConfirAll(strarray[i], "50", function, jCoDestination);//完工确认
                    if (result.equals("操作失败")) {
                        failureNum++;
                        failureMsg.append("<br/>" + failureNum + "、序列号 " + strarray[i] + " 报工失败！");
                    } else {
                        try {
                            String sql = "UPDATE PRODUCT_SN SET STATE=1 ,UP_DT=sysdate,DES=? WHERE SERNR=?";
                            template.update(sql, result, data);
                        } catch (DataAccessException e) {
                            e.printStackTrace();
                            failureNum++;
                            failureMsg.append("<br/>" + failureNum + "、序列号 " + strarray[i] + " 报工完成失败！");
                        }
                    }
                } else {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、序列号 " + strarray[i] + " 格式不正确！");
                }
            }
            if (failureNum == 0) {
                failureMsg.append("<br/>" + failureNum + "、全部报工成功： " + strarray.length + "条！");
            }
            log.info("执行结果：" + failureMsg.toString());
        } catch (Exception e) {
            log.info(e.toString(), e);
        }
        return failureMsg.toString();
    }


    /**
     * 存储打印信息，插入到缓存表
     *
     * @param map
     * @return
     * @throws JSONException
     */
    public Map<String, Object> insertCatch(Map<String, Object> map) throws JSONException {
        Map<String, Object> commonMap = new HashMap<>();
        commonMap.put("IFS", "STA102");
        commonMap.put("ITEM_NM", map.get("prodName")); //产品名称
        commonMap.put("ITEM_CD", map.get("prodType")); //产品型号
        commonMap.put("JX_SN", map.get("JX_SN"));
        commonMap.put("O_NO", map.get("O_NO"));
        commonMap.put("SALE_NO", map.get("SALE_NO")); //销售订单号
        commonMap.put("PRO_NO", map.get("PRO_NO"));  //行项目号
        commonMap.put("PRO_CD", map.get("PRO_CD"));  //产品编码
        commonMap.put("PRO_NM", map.get("PRO_NM"));  //商品编码
        commonMap.put("CPU_NM", map.get("CPU"));  // CPU_NM
        commonMap.put("YP_NM", map.get("YP_NM"));   //硬盘
        commonMap.put("NC_NM", map.get("NC_NM"));   //内存

        commonMap.put("CD", map.get("CD"));   //光驱
        commonMap.put("OTHER", map.get("OTHER"));   //其他
        commonMap.put("NVG_CODE", map.get("NVG_CODE"));   //导航编码
        commonMap.put("WAY", map.get("WAY"));   //导轨
        commonMap.put("NET_CARD", map.get("NET_CARD"));   //网卡
        return commonMap;
    }


    public void update(String sql, final Map<String, Object> map) {
        template.batchUpdate(sql, new BatchPreparedStatementSetter() {

            {
            }

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                // TODO Auto-generated method stub

                ps.setString(1, map.get("crate").toString());
                ps.setString(2, map.get("keypn").toString());
                ps.setString(3, map.get("user").toString());
                ps.setString(4, map.get("process").toString());
                ps.setString(5, map.get("work").toString());
                ps.setString(6, map.get("crate").toString());
                ps.setString(7, map.get("work").toString());
                if (i % 5000 == 0 && i != 0) {
                    ps.executeBatch();
                }
            }

            @Override
            public int getBatchSize() {
                System.out.println(map.size());
                return map.size();
            }
        });
    }

    /**
     * 自动循环报工
     *
     * @return
     */
    @Override
    public String repairSnDynamic() {
        String result = "";
        String sn = "";
        String insertSql = "";
        String ID = "0";
        int STATUS = 0;
        try {
            String sql = "SELECT ID,SN,STATUS FROM DEMO_YUAN WHERE STATUS!=3";//修改订单号
            List<Map<String, Object>> snMap = template.queryForList(sql);
            for (int i = 0; snMap.size() > i; i++) {
                sn = snMap.get(i).get("SN").toString();
                ID = snMap.get(i).get("ID").toString();
                STATUS = Integer.parseInt(snMap.get(i).get("STATUS").toString()) + 1;
                result = confir.cpletionConfir(sn, "50");//完工确认
                String sql12 = "UPDATE PRODUCT_SN SET STATE=1 ,UP_DT=sysdate,DES=? WHERE SERNR=?";
                template.update(sql12, result, sn);
                if (result.length() > 6) {
                    insertSql = "UPDATE DEMO_YUAN SET INFO=?,STATUS=? , RECORD_TIME=sysdate WHERE ID=?";
                    template.update(insertSql, result, STATUS, ID);
                } else {
                    insertSql = "UPDATE DEMO_YUAN SET STATUS=3 ,LASTINFO=?,RECORD_TIME=sysdate WHERE ID=?";
                    template.update(insertSql, result, ID);
                }
            }
        } catch (Exception e) {
            log.info(e.toString(), e);
            result = "操作失败";
        }
        return result;
    }
}
