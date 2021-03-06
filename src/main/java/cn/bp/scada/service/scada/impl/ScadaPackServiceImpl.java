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
        map.put("sort", "192.168.10.234"); //???????????????IP
        map.put("pack", "192.168.10.98");    //?????????IP
    }

    @Override
    public String labelPrintNh(JSONObject json) {
        JSONObject js = new JSONObject();
        try {
            JSONObject jsonMes = json.getJSONArray("REQ").getJSONObject(0).getJSONObject("REQ_DATA");
            String sn = jsonMes.getString("CRATE");

            System.out.println("??????SN:" + sn);
            //???sap????????????
            Map<String, Object> nhPrint = sapLable.testSap(sn, "");
            if (nhPrint.get("erp").equals("0")) {

                js.put("CODE", "0");
                js.put("MSG", "??????");
            } else {
                js.put("CODE", "1");
                js.put("MSG", "ERP??????????????????");
            }

        } catch (Exception e) {
            js.put("CODE", "2");
            js.put("MSG", "?????????????????????");
            e.printStackTrace();
        }

        System.out.println("???????????????????????????json:" + js.toString());
        return js.toString();
    }

    @Override
    public ScadaRespon packLine(JSONObject deviceRequest) {
        ScadaRespon result = new ScadaRespon();
        try {

            String line = deviceRequest.getString("device_sn"); //??????
            String status = deviceRequest.getString("status"); //??????
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
            System.out.println("???????????????" + status);

           /* ScadaRequest sr = new ScadaRequest();
            sr.setOp_flag("B104");
            sr.setStatus(status);
            sr.setBox_sn(sn);
            sr.setEt_ip(map.get("pack").toString());
            JSONObject device = smt.toDevice(sr);
            if (device.getString("result_flag").equals("OK")) {
                js.put("CODE", "0");
                js.put("MSG", "??????"+sn);
            } else {
                js.put("CODE", "1");
                js.put("MSG", "?????????????????????");
            }
*/
            js.put("CODE", "0");
            js.put("MSG", "??????" + sn);
        } catch (Exception e) {
            js.put("CODE", "2");
            js.put("MSG", "??????");
            e.printStackTrace();
        }
        log.info("?????????json:" + js.toString());
        return js.toString();
    }

    /**
     * ????????????--??????????????????
     */
    @Override
    public String sapLabelPrintOut1(JSONObject json) {
        JSONObject js = new JSONObject();
        JSONObject reqDataDetail1 = new JSONObject();
        reqDataDetail1.put("IFS", "STA9001");
        try {
            JSONObject jsonMes = json.getJSONArray("REQ").getJSONObject(0).getJSONObject("REQ_DATA");
            String crate = jsonMes.getString("CRATE");//??????SN
            String work = jsonMes.getString("WORK");//
            String user = jsonMes.getString("USER");//??????ID
            String process = jsonMes.getString("PROCESS");//
            String keypn = jsonMes.getString("KEYPN");//??????QN

            //????????????SN????????????
            String orderSql = "SELECT AUFNR FROM (SELECT AUFNR FROM PRODUCT_SN WHERE SERNR='" + crate + "' ORDER BY CRT_DT DESC) WHERE ROWNUM=1";
            Map<String, Object> orderMap = new HashMap<>();
            if (work.equals("G00101")) {
                orderMap = template.queryForMap(orderSql);
                redisUtil.set("G00101", orderMap.get("AUFNR").toString());
                log.info("G00101????????????" + redisUtil.get("G00101"));
            } else {
                orderMap = template.queryForMap(orderSql);
                redisUtil.set("G00101", orderMap.get("AUFNR").toString());
                log.info("G00101????????????" + redisUtil.get("G00101"));
            }
            //
            Map<String, Object> map = new HashMap<>();
            map.put("crate", crate);
            map.put("work", work);
            map.put("user", user);
            map.put("process", process);
            map.put("keypn", keypn);
            map.put("boxSn", crate); //??????SN
            //???sap????????????
            Map<String, Object> testSap1 = sapLable.testSap(crate, redisUtil.get("G00101").toString());
            testSap1.put("O_NO", Integer.parseInt(redisUtil.get("G00101").toString()) + "");
            testSap1.put("JX_SN", crate);
            //??????????????????
            String url = "//192.168.10.159/FilePrint//";//?????????????????????ip??????
            String label = printLabel.masterLabelPack(crate, url, testSap1);
            if (!label.equals("0")) {
                js.put("CODE", "3");
                js.put("MSG", "?????????????????????????????????????????????");
            } else {
                //????????????
               /* Map<String, Object> stringObjectMap = insertCatch(testSap1);
                pack.insertPack(stringObjectMap);*/
                //????????????????????????--??????
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
                    js.put("MSG", "??????????????????????????????");
                } else {
                    js.put("CODE", "0");
                    js.put("MSG", "??????");
                }
                //???????????????????????????
                System.out.println("?????????json:" + js.toString());
            }
        } catch (Exception e) {
            js.put("CODE", "3");
            js.put("MSG", "???????????????????????????");
            log.error(e.toString(), e);
        } finally {

        }
        return js.toString();
    }

    @Override
    //  @Transactional(isolation = Isolation.DEFAULT) //????????????????????????
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

            //????????????SN????????????
            String orderSql = "SELECT AUFNR FROM (SELECT AUFNR FROM PRODUCT_SN WHERE SERNR='" + crate + "' ORDER BY CRT_DT DESC) WHERE ROWNUM=1";
            Map<String, Object> orderMap = new HashMap<>();
            if (work.equals("G001")) {
                orderMap = template.queryForMap(orderSql);
                redisUtil.set("G001", orderMap.get("AUFNR").toString());
                log.info("G001????????????" + redisUtil.get("G001"));
            } else {
                orderMap = template.queryForMap(orderSql);
                redisUtil.set("G002", orderMap.get("AUFNR").toString());
                log.info("G002????????????" + redisUtil.get("G002"));
            }
            //
            Map<String, Object> map = new HashMap<>();
            map.put("crate", crate);
            map.put("work", work);
            map.put("user", user);
            map.put("process", process);
            map.put("keypn", keypn);
            map.put("boxSn", crate); //??????SN
            String upSql = "";


            String seleSql = "SELECT COUNT(1) COU FROM PACKING_MACHINE_LINE WHERE MACHINE_STA=1";
            Map<String, Object> lineMap = template.queryForMap(seleSql);

            seleSql = "SELECT COUNT(1) COU FROM PACKING_MACHINE_CODE WHERE JX_CODE IS NOT NULL";
            if (Integer.parseInt(lineMap.get("COU").toString()) > 1) { //??????????????????
                upSql = "UPDATE PACKING_MACHINE_CODE SET JX_CODE=?, JP_CODE=?,"
                        + " USERID=?, WP_CD=?, STATION_CD=?, BOX_SN=?,USE_YN='Y' WHERE SORT_LINE=?";
                update(upSql, map);


                Map<String, Object> map2 = template.queryForMap(seleSql);
                if (Integer.parseInt(map2.get("COU").toString()) > 1) { //??????????????????????????????
                    //???sap????????????
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
                    //????????????

                    Map<String, Object> stringObjectMap = insertCatch(testSap1);
                    pack.insertPack(stringObjectMap);
                    Map<String, Object> stringObjectMap2 = insertCatch(testSap2);
                    pack.insertPack(stringObjectMap2);

                /*    String epSql = "UPDATE PACKING_MACHINE_CODE SET USE_YN='N' ";
                    template.update(epSql);*/
                    //????????????????????????

                    reqDataDetail1.put("IN_CODE1", "G001");
                    reqDataDetail1.put("IN_CODE2", "G002");
                    JSONObject jsonMesRus = mesApiUtils.doPost(reqDataDetail1);
                    if (jsonMesRus.getString("CODE").equals("1")) {
                        js.put("CODE", "4");
                        js.put("MSG", "??????????????????????????????");
                    } else {
                        js.put("CODE", "0");
                        js.put("MSG", "??????");
                    }

                   /* upSql= "UPDATE PACKING_MACHINE_CODE SET USE_YN='N' ";
                    template.update(upSql);*/

                    //???????????????????????????
                    ScadaRequest sr = new ScadaRequest();
                    sr.setOp_flag("C06");
                    sr.setEt_ip("192.168.10.98");
                    smt.toDevice(sr);

                } else {
                    js.put("CODE", "0");
                    js.put("MSG", "??????");
                }


            } else {
                //????????????
                //??????PACKING_MACHINE_CODE???????????????????????????
                upSql = "UPDATE PACKING_MACHINE_CODE SET JX_CODE=?, JP_CODE=?,"
                        + " USERID=?, WP_CD=?, STATION_CD=?, BOX_SN=? WHERE SORT_LINE=?";
                update(upSql, map);

                Map<String, Object> map2 = template.queryForMap(seleSql);
                if (Integer.parseInt(map2.get("COU").toString()) > 0) {
                    //???sap????????????
                    Map<String, Object> testSap1 = sapLable.testSap(crate, orderMap.get("AUFNR").toString());
                    testSap1.put("O_NO", Integer.parseInt(orderMap.get("AUFNR").toString()) + "");
                    testSap1.put("JX_SN", crate);
                    if (testSap1.get("erp").equals("0")) {
                        log.info("????????????ERP???????????????????????????");
                    } else {
                        log.info("????????????ERP???????????????????????????");
                    }

                    //????????????
                    Map<String, Object> stringObjectMap = insertCatch(testSap1);
                    pack.insertPack(stringObjectMap);

                    //????????????????????????
                    reqDataDetail1.put("IN_CODE1", "G001");
                    JSONObject jsonMesRus = mesApiUtils.doPost(reqDataDetail1);
                    if (jsonMesRus.getString("CODE").equals("1")) {
                        js.put("CODE", "4");
                        js.put("MSG", "??????????????????????????????");
                    } else {
                        js.put("CODE", "0");
                        js.put("MSG", "??????");
                    }

                    //???????????????????????????
                    ScadaRequest sr = new ScadaRequest();
                    sr.setOp_flag("C06");
                    sr.setEt_ip("192.168.10.98");
                    smt.toDevice(sr);


                } else {
                    js.put("CODE", "2");
                    js.put("MSG", "CODE??????????????????");
                }

            }

            System.out.println("?????????json:" + js.toString());
        } catch (Exception e) {
            js.put("CODE", "3");
            js.put("MSG", "???????????????????????????");
            log.error(e.toString(), e);
            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly(); //??????????????????
        } finally {
            lock.unlock(); //?????????
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
            String obj = s.writeValueAsString(forMap); //null??????????????????
            JSONObject json = new JSONObject(obj); //json???????????????json??????

            //????????????SN????????????
            String orderSql = "SELECT AUFNR FROM PRODUCT_SN WHERE SErnr=? ";
            // Map<String, Object> maps = template.queryForMap(orderSql, json.get("JX_SN"));
            log.info("?????????????????????????????????ERP");
            Map<String, Object> maps = sapLable.testSap(json.get("JX_SN").toString(), "");

            result.setResult_flag("OK");
            result.setCpu_nm(json.get("CPU_NM") + ""); //
            result.setYp_nm(json.get("YP_NM") + "");
            result.setPro_nm(json.get("PRO_NM") + ""); //????????????
            result.setJx_sn(json.get("JX_SN") + "");
            result.setO_no(maps.get("O_NO").toString() + "");
            result.setNc_nm(json.get("NC_NM") + "");
            result.setPro_cd(json.get("PRO_CD") + "");  //????????????
            result.setItem_cd(json.get("ITEM_CD") + "");

            result.setCd(json.get("CD") + "");
            result.setOther(json.get("OTHER") + "");
            result.setNvg_code(json.get("NVG_CODE") + "");
            result.setWay(json.get("WAY") + "");
            result.setNet_card(json.get("NET_CARD") + "");
            result.setSale_no(json.get("SALE_NO") + "");
            result.setPro_no(json.get("PRO_NO") + "");
            result.setItem_nm(json.get("ITEM_NM") + "");
            result.setFirst(maps.get("proDt").toString() + "");//??????????????????

            String sql2 = "update TEMP_PRINT set status=1,UP_DT=sysdate where id=(select id from (select id from TEMP_PRINT where status=0 order by ID asc) where rownum=1 ) ";
            template.update(sql2);

        } catch (Exception e) {
            result.setResult_flag("NG");
            result.setResult_message("???????????????????????????");
            log.error(e.toString(), e);
            log.error("???????????????????????????????????????????????????????????????");
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
                js.put("MSG", "??????");
                js.put("WEIGHT1", device.get("weight1")); //??????1
                js.put("WEIGHT2", device.get("weight2")); //??????2
            } else if (device.getString("result_flag").equals("NO")) {

                js.put("CODE", "1");
                js.put("MSG", "???????????????????????????");
            } else {
                js.put("CODE", "2");
                js.put("MSG", "?????????????????????");
            }

            System.out.println("?????????json:" + js.toString());
            String sql = " Delete from MES1.PACKAGE_WEIGHT_TEMP_T";
            template.update(sql);
        } catch (Exception e) {
            js.put("CODE", "3");
            js.put("MSG", "??????");
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
            String work = jsonMes.getString("WORK"); //??????

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
                js.put("MSG", "??????");
            } else {
                js.put("CODE", "1");
                js.put("MSG", "?????????????????????????????????");
            }

            System.out.println("PQC??????????????????????????????json:" + js.toString());
        } catch (Exception e) {
            js.put("CODE", "2");
            js.put("MSG", "??????");
            e.printStackTrace();
        }
        return js.toString();
    }

    @Override
    public String inGoes(String data, String sn) {
        //???????????????????????????
        String querySql1 = "SELECT COUNT(1) FROM PRODSTS_HIS " +
                "WHERE VSN=(SELECT VSN FROM (SELECT VSN FROM PRODSTS_RM WHERE PRODUCTSN=" + sn + " ORDER BY CREATIME  DESC) WHERE ROWNUM=1) AND WORKNO='G00101'";
        Map<String, Object> stringObjectMap1 = template.queryForMap(querySql1);
        if (stringObjectMap1.get("COUNT(1)").toString().equals("1")) {
            return "????????????--????????????????????? OK???";
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
                log.info("??????OQC???????????????" + device.toString() + " --sn??????" + sn);
            } else {
                log.info("?????????OQC" + "--sn??????" + sn);
            }
        } catch (DataAccessException e) {
            log.info("OQC????????????" + "--sn??????" + sn);
            e.printStackTrace();
        } catch (JSONException e) {
            log.info("OQC????????????" + "--sn??????" + sn);
            e.printStackTrace();
        }
        ScadaRequest sr = new ScadaRequest();
        JSONObject json = null;
        if (data.equals("1")) {
            //???????????????????????????
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
        //???????????????????????????
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
     * ????????????????????????
     *
     * @return
     */
    @RequestMapping("/myself/repairSN")
    private void repairSnAtoOrder() {
        String sql = "SELECT SERNR FROM PRODUCT_SN WHERE AUFNR='000060032515'";//???????????????
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
            result = confir.cpletionConfir(data, "50");//????????????
            String sql = "UPDATE PRODUCT_SN SET STATE=1 ,UP_DT=sysdate,DES=? WHERE SERNR=?";
            template.update(sql, result, data);
        } catch (Exception e) {
            log.info(e.toString(), e);
            result = "????????????";
        }
        return result;
    }

    @Override
    public String repairSnAll(String data) {
        int failureNum = 0;
        StringBuilder failureMsg = new StringBuilder();
        String[] strarray = data.split("\\|");
        try {
            JCoDestination jCoDestination = sapUtils.jCoDestination;//??????????????????
            for (int i = 0; i < strarray.length; i++) {
                if (strarray[i].length() == 9) {
                    JCoFunction function = sapUtils.getFunction("ZRFC_AUFNR_BG");//???????????????
                    String result = confir.cpletionConfirAll(strarray[i], "50", function, jCoDestination);//????????????
                    if (result.equals("????????????")||result.length()>6 ) {
                        failureNum++;
                        failureMsg.append("<br/>" + failureNum + "???????????? " + strarray[i] + " ???????????????");
                        String sql = "UPDATE PRODUCT_SN SET STATE=1 ,UP_DT=sysdate,DES=? WHERE SERNR=?";
                        template.update(sql, result, strarray[i]);
                        String insertSql = "INSERT INTO DEMO_YUAN(INFO,SN,RECORD_TIME) VALUES (?,?,sysdate)";
                        template.update(insertSql,result,strarray[i]);
                        // template.update(insertSql, result, workSn);
                        log.info("???????????????????????? SN???"+strarray[i]+"  sap??????????????????"+result);
                    } else {
                        try {
                            String sql = "UPDATE PRODUCT_SN SET STATE=1 ,UP_DT=sysdate,DES=? WHERE SERNR=?";
                            template.update(sql, result, strarray[i]);
                        } catch (DataAccessException e) {
                            e.printStackTrace();
                            failureNum++;
                            failureMsg.append("<br/>" + failureNum + "???????????? " + strarray[i] + " ?????????????????????");
                        }
                    }
                } else {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "???????????? " + strarray[i] + " ??????????????????");
                }
            }
            if (failureNum == 0) {
                failureMsg.append("<br/>" + failureNum + "???????????????????????? " + strarray.length + "??????");
            }
            log.info("???????????????" + failureMsg.toString());
        } catch (Exception e) {
            log.info(e.toString(), e);
        }
        return failureMsg.toString();
    }


    /**
     * ???????????????????????????????????????
     *
     * @param map
     * @return
     * @throws JSONException
     */
    public Map<String, Object> insertCatch(Map<String, Object> map) throws JSONException {
        Map<String, Object> commonMap = new HashMap<>();
        commonMap.put("IFS", "STA102");
        commonMap.put("ITEM_NM", map.get("prodName")); //????????????
        commonMap.put("ITEM_CD", map.get("prodType")); //????????????
        commonMap.put("JX_SN", map.get("JX_SN"));
        commonMap.put("O_NO", map.get("O_NO"));
        commonMap.put("SALE_NO", map.get("SALE_NO")); //???????????????
        commonMap.put("PRO_NO", map.get("PRO_NO"));  //????????????
        commonMap.put("PRO_CD", map.get("PRO_CD"));  //????????????
        commonMap.put("PRO_NM", map.get("PRO_NM"));  //????????????
        commonMap.put("CPU_NM", map.get("CPU"));  // CPU_NM
        commonMap.put("YP_NM", map.get("YP_NM"));   //??????
        commonMap.put("NC_NM", map.get("NC_NM"));   //??????

        commonMap.put("CD", map.get("CD"));   //??????
        commonMap.put("OTHER", map.get("OTHER"));   //??????
        commonMap.put("NVG_CODE", map.get("NVG_CODE"));   //????????????
        commonMap.put("WAY", map.get("WAY"));   //??????
        commonMap.put("NET_CARD", map.get("NET_CARD"));   //??????
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
     * ??????????????????
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
            String sql = "SELECT ID,SN,STATUS FROM DEMO_YUAN WHERE STATUS!=3";//???????????????
            List<Map<String, Object>> snMap = template.queryForList(sql);
            for (int i = 0; snMap.size() > i; i++) {
                sn = snMap.get(i).get("SN").toString();
                ID = snMap.get(i).get("ID").toString();
                STATUS = Integer.parseInt(snMap.get(i).get("STATUS").toString()) + 1;
                result = confir.cpletionConfir(sn, "50");//????????????
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
            result = "????????????";
        }
        return result;
    }
}
