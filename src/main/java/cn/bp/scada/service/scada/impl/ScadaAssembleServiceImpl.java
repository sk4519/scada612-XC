package cn.bp.scada.service.scada.impl;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.annotation.Resource;

import cn.bp.scada.common.constant.Matpoint;
import cn.bp.scada.common.utils.PrimaryHelper;
import cn.bp.scada.common.utils.data.DateUtils;
import cn.bp.scada.common.utils.dbhelper.DBHelper;
import cn.bp.scada.common.utils.redis.RedisUtils;
import cn.bp.scada.controller.sap.SapCpletionConfir;
import cn.bp.scada.controller.scada.ScadaAndAgvMt;
import cn.bp.scada.mapper.scada.CiPLANTeTMapper;
import cn.bp.scada.modle.scada.ScadaRequest;
import cn.bp.scada.modle.scada.ScadaResponShow;
import cn.bp.scada.service.scada.ScadaAssembleService;
import cn.bp.scada.webservice.JsonDealUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import cn.bp.scada.modle.scada.Material;
import cn.bp.scada.modle.scada.ScadaRespon;
import cn.bp.scada.common.utils.MesApiUtils;

/**
 * scada组装服务类
 */
@Service
public class ScadaAssembleServiceImpl implements ScadaAssembleService {
    @Resource
    private MesApiUtils mesApiUtils;
    @Resource
    private JdbcTemplate template;
    @Resource
    private ScadaAndAgvMt sam;
    @Autowired
    private RedisUtils redisUtil;
    @Resource
    private RedisUtils redisUtils;
    @Autowired
    private CiPLANTeTMapper ciPLANTeTMapper;
    @Resource
    private SapCpletionConfir confir;
    @Resource
    private DBHelper db;

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    /**
     * 主板外面第一个扫描枪（底托验证载具 B011
     *
     * @param msg
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public ScadaRespon downCheck(Object msg) throws IOException, JSONException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        LOG.info("进入主板上料外面第一个扫描枪(底托验证)");
        ScadaRespon result = new ScadaRespon();
        try {

            JSONObject reqDataDetail = new JSONObject();
            reqDataDetail.put("IFS", "ACT0051");
            reqDataDetail.put("IN_PSN_NO", deviceRequest.get("con_sn"));
            reqDataDetail.put("IN_ET_CD", deviceRequest.get("device_sn"));

            JSONObject responseData = mesApiUtils.doPost(reqDataDetail);
            List<Material> materialList = new ArrayList<Material>();

            if (responseData.get("STATUS").equals("1")) {
                result.setResult_flag("OK");
                result.setResult_message(responseData.getString("MESSAGE"));
                result.setFlow_code("1");
                try {

                    String[] materialArray = responseData.getString("STRCOUNT").split("\\|");
                    for (int i = 0; i < materialArray.length; i++) {
                        String[] array = materialArray[i].split(",");
                        Material material = new Material();

                        material.setMaterial_code(array[0]);
                        material.setMaterial_qty(array[1]);
                        materialList.add(material);
                    }
                } catch (Exception e) {
                    LOG.info("物料为空");
                }
            } else {
                result.setResult_flag("NG");
                result.setResult_message(responseData.getString("MESSAGE"));
            }
            result.setMaterial(materialList);
        } catch (Exception e) {
            result.setResult_flag("NG");
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 主板&载具验证 B01
     *
     * @param msg
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public ScadaRespon checkData(Object msg) throws IOException, JSONException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        ScadaRespon result = new ScadaRespon();
        try {

            JSONObject reqDataDetail = new JSONObject();
            reqDataDetail.put("IFS", "ACT0052");
            reqDataDetail.put("IN_PSN_NO", deviceRequest.get("con_sn"));
            reqDataDetail.put("IN_ET_CD", deviceRequest.get("device_sn"));
            AccessTo(deviceRequest.get("device_sn").toString(), deviceRequest.get("con_sn").toString(), 1);
            switch (deviceRequest.getString("device_sn")) {
                case "ECDLC202":
                    LOG.info("进入二线主板上料载具验证");
                    break;
                case "ECDLC001":
                    LOG.info("进入一线主板上料载具验证");
                    break;

                default:

                    break;
            }

            JSONObject responseData = mesApiUtils.doPost(reqDataDetail);

            List<Material> materialList = new ArrayList<Material>();


            if (responseData.get("STATUS").equals("1")) {
                result.setResult_flag("OK");
                result.setResult_message(responseData.getString("MESSAGE"));
                result.setFlow_code("1");
                result.setPro_mod(responseData.getString("ITEMCD"));

                try {

                    String[] materialArray = responseData.getString("STRCOUNT").split("\\|");
                    for (int i = 0; i < materialArray.length; i++) {
                        String[] array = materialArray[i].split(",");
                        Material material = new Material();

                        material.setMaterial_code(array[0]);
                        material.setMaterial_qty(array[1]);
                        materialList.add(material);
                    }
                } catch (Exception e) {
                    LOG.info("物料为空");
                }
            } else if (responseData.get("STATUS").equals("0")) {
                result.setResult_flag("NG");
                result.setResult_message(responseData.getString("MESSAGE"));
            } else {
                result.setResult_flag("PASS");
                result.setResult_message(responseData.getString("MESSAGE"));
            }
            result.setMaterial(materialList);
            //result.setPro_mod(responseData.getString("ITEMCD"));

        } catch (Exception e) {
            result.setResult_flag("NG");
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 硬盘&光驱公用载具验证 Y01
     *
     * @param msg
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public ScadaRespon checkHdData(Object msg) throws IOException, JSONException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        ScadaRespon result = new ScadaRespon();
        try {

            JSONObject reqDataDetail = new JSONObject();
            //reqDataDetail.put("IFS", "ACT0057A");
            reqDataDetail.put("IFS", "STA108");
            reqDataDetail.put("IN_PSN_NO", deviceRequest.get("con_sn"));
            reqDataDetail.put("IN_ET_CD", deviceRequest.get("device_sn"));

            AccessTo(deviceRequest.get("device_sn").toString(), deviceRequest.get("con_sn").toString(), 1);

           String sql = "UPDATE IPLANT1.C_IPLANT_E2_T SET PRO_START=sysdate,PRO_END='',CT='' WHERE ET_CD = ? ";

            switch (deviceRequest.getString("device_sn")) {
                case "ECDLC012":
                    LOG.info("进入一线硬盘载具验证");
                    break;
                case "ECDLC211":
                    LOG.info("进入二线硬盘载具验证");
                    break;
                case "ECDLC209":
                    LOG.info("进入二线光驱载具验证");
                    template.update(sql, "ECDLC211");
                    break;
                default:
                    LOG.info("进入一线光驱载具验证");
                    template.update(sql, "ECDLC012");
                    break;
            }

            JSONObject responseData = mesApiUtils.doPost(reqDataDetail);

            List<Material> materialList = new ArrayList<Material>();


            if (responseData.get("STATUS").equals("1")) {
                result.setResult_flag("OK");
                result.setResult_message(responseData.getString("MESSAGE"));
                result.setFlow_code("1");
                result.setStatus(responseData.getString("POSITION"));
                //  result.setMo_num(responseData.getString("YCSN"));//是否要扫原厂SN标识    0是不扫原厂sn，1是要扫原厂sn
                result.setMat_point(deviceRequest.getString("mat_point"));
                result.setPro_mod(responseData.getString("ITEMCD"));
                result.setCd(responseData.getString("CDHD")); //光驱编码
                result.setYp_nm(responseData.getString("HD")); //硬盘编码
            } else if (responseData.get("STATUS").equals("0")) {
                result.setResult_flag("NG");
                result.setResult_message(responseData.getString("MESSAGE"));
                result.setMat_point(deviceRequest.getString("mat_point"));
            } else {
                result.setResult_flag("PASS");
                result.setResult_message(responseData.getString("MESSAGE"));
                result.setMat_point(deviceRequest.getString("mat_point"));
            }
            result.setMaterial(materialList);

        } catch (Exception e) {
            result.setResult_flag("NG");
            result.setMat_point(deviceRequest.getString("mat_point"));
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 主板下料前第一个扫描枪载具验证
     *
     * @param msg
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public ScadaRespon mainDownBef(Object msg) throws IOException, JSONException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        LOG.info("进入主板下线外面第一把扫描枪");
        ScadaRespon result = new ScadaRespon();
        try {

            JSONObject reqDataDetail = new JSONObject();
            reqDataDetail.put("IFS", "ACT0071");
            reqDataDetail.put("IN_PSN_NO", deviceRequest.get("con_sn"));
            reqDataDetail.put("IN_ET_CD", deviceRequest.get("device_sn"));
            reqDataDetail.put("IN_FLAG", deviceRequest.get("status")); //是否维修，状态

            JSONObject responseData = mesApiUtils.doPost(reqDataDetail);


            if (responseData.get("STATUS").equals("1")) { //
                result.setResult_flag("OK");
                result.setResult_message(responseData.getString("MESSAGE"));
            } else {
                result.setResult_flag("NG");
                result.setResult_message(responseData.getString("MESSAGE"));
            }

        } catch (Exception e) {
            result.setResult_flag("NG");
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 散热器安装&主板下线验证载具&铭牌贴附载具验证& B014
     *
     * @param msg
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public ScadaRespon checkZhuTest(Object msg) throws IOException, JSONException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        ScadaRespon result = new ScadaRespon();
        try {

            JSONObject reqDataDetail = new JSONObject();
            if (deviceRequest.get("device_sn").equals("ECDLC204") || deviceRequest.get("device_sn").equals("ECDLC007")) { //散热器安装
                reqDataDetail.put("IFS", "ACT0052");
                LOG.info("进入散热器安装载具验证");
            } else if (deviceRequest.get("device_sn").equals("ECDLC205") || deviceRequest.get("device_sn").equals("ECDLC005")) { //主板下线验证载具
                reqDataDetail.put("IFS", "ACT0054");
                LOG.info("进入主板下线验证载具");
            } else {
                reqDataDetail.put("IFS", "ACT0054");
                LOG.info("进入铭牌贴附载具验证");
            }

            reqDataDetail.put("IN_PSN_NO", deviceRequest.get("con_sn"));
            reqDataDetail.put("IN_ET_CD", deviceRequest.get("device_sn"));

            AccessTo(deviceRequest.get("device_sn").toString(), deviceRequest.get("con_sn").toString(), 1);
            JSONObject responseData = mesApiUtils.doPost(reqDataDetail);

            List<Material> materialList = new ArrayList<Material>();

            if (responseData.get("STATUS").equals("1")) {
                result.setResult_flag("OK");
                result.setResult_message(responseData.getString("MESSAGE"));
                result.setFlow_code("1");
                result.setPro_mod(responseData.getString("ITEMCD"));
                if (reqDataDetail.getString("IFS").equals("ACT0054")) {
                    result.setVreset(responseData.getString("VRESET")); //是否清料标识，1：清，0：不清，默认0
                    result.setItem_cd(responseData.getString("MATNR"));
                    result.setBox_pn(responseData.getString("BOX_PN"));
                }
                result.setZb_code(responseData.getString("ZB_IDNRK"));


                if (deviceRequest.get("device_sn").equals("ECDLC204") || deviceRequest.get("device_sn").equals("ECDLC007")) { //如果是散热器，要上传物料编码
                    String[] materialArray = responseData.getString("STRCOUNT").split("\\|");
                    for (int i = 0; i < materialArray.length; i++) {
                        String[] array = materialArray[i].split(",");
                        Material material = new Material();

                        material.setMaterial_code(array[0]);
                        material.setMaterial_qty(array[1]);
                        materialList.add(material);
                    }
                }
                result.setMaterial(materialList);
            } else {
                result.setResult_flag("NG");
                result.setResult_message(responseData.getString("MESSAGE"));
            }

        } catch (Exception e) {
            result.setResult_flag("NG");
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 验证是否进入机箱组装 B016
     *
     * @param msg
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public ScadaRespon checkDataCrate(Object msg) throws IOException, JSONException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        ScadaRespon result = new ScadaRespon();
        try {

            JSONObject reqDataDetail = new JSONObject();
            reqDataDetail.put("IFS", "ACT0059");
            reqDataDetail.put("IN_PSN_NO", deviceRequest.get("con_sn")); //载具SN
            LOG.info("进入机箱组装验证");

            JSONObject responseData = mesApiUtils.doPost(reqDataDetail);

            if (responseData.get("STATUS").equals("1")) {
                result.setResult_flag("OK");
                result.setResult_message(responseData.getString("MESSAGE"));
                result.setFlow_code("1");

            } else {
                result.setResult_flag("NG");
                result.setResult_message(responseData.getString("MESSAGE"));
            }

        } catch (Exception e) {
            result.setResult_flag("NG");
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 内存条载具验证 B012
     *
     * @param msg
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public ScadaRespon checkMemory(Object msg) throws IOException, JSONException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        ScadaRespon result = new ScadaRespon();
        try {

            JSONObject reqDataDetail = new JSONObject();
            reqDataDetail.put("IFS", "ACT0055");
            reqDataDetail.put("IN_PSN_NO", deviceRequest.get("con_sn")); //载具SN
            reqDataDetail.put("IN_ET_CD", deviceRequest.get("device_sn"));
            LOG.info("进入内存条载具验证");

            AccessTo(deviceRequest.get("device_sn").toString(), deviceRequest.get("con_sn").toString(), 1);
            JSONObject responseData = mesApiUtils.doPost(reqDataDetail);

            List<Material> materialList = new ArrayList<Material>();
            if (responseData.get("STATUS").equals("1")) {

                try {

                    String[] materialArray = responseData.getString("STRCOUNT").split("\\|");
                    for (int i = 0; i < materialArray.length; i++) {
                        String[] array = materialArray[i].split(",");
                        Material material = new Material();

                        material.setMaterial_code(array[0]);
                        material.setMaterial_qty(array[1]);
                        materialList.add(material);
                    }
                } catch (Exception e) {
                    LOG.info("内存条物料为空");
                }
                result.setResult_flag("OK");
                result.setResult_message(responseData.getString("MESSAGE"));
                result.setMaterial(materialList);
                result.setFlow_code("1");
                result.setPro_mod(responseData.getString("ITEMCD"));
                result.setZb_code(responseData.getString("ZB_IDNRK"));

            } else if (responseData.get("STATUS").equals("0")) {
                result.setResult_flag("NG");
                result.setResult_message(responseData.getString("MESSAGE"));
            } else {
                result.setResult_flag("PASS");
                result.setResult_message(responseData.getString("MESSAGE"));
            }

        } catch (Exception e) {
            result.setResult_flag("NG");
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 安规测试载具验证
     *
     * @param msg
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public ScadaRespon checksafetyTest(Object msg) throws IOException, JSONException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        LOG.info("进入安规测试载具验证");
        ScadaRespon result = new ScadaRespon();
        try {

            JSONObject reqDataDetail = new JSONObject();
            reqDataDetail.put("IFS", "ACT0041");
            reqDataDetail.put("IN_PSN_NO", deviceRequest.get("con_sn"));
            reqDataDetail.put("IN_ET_CD", deviceRequest.get("device_sn"));

            AccessTo(deviceRequest.get("device_sn").toString(), deviceRequest.get("con_sn").toString(), 1);
            JSONObject responseData = mesApiUtils.doPost(reqDataDetail);

            if (responseData.get("STATUS").equals("1")) {
                result.setResult_flag("OK");
                result.setResult_message(responseData.getString("MESSAGE"));
                result.setFlow_code("1");
                result.setBox_pn(responseData.getString("BOX_PN"));
            } else if (responseData.get("STATUS").equals("2")) { //安规过站
                result.setResult_flag("PASS");
                result.setResult_message(responseData.getString("MESSAGE"));
            } else {
                result.setResult_flag("NG");
                result.setResult_message(responseData.getString("MESSAGE"));
            }

        } catch (Exception e) {
            result.setResult_flag("NG");
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 人工安规测试（不良）载具验证
     *
     * @param msg
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public ScadaRespon checksafetyTestPerson(Object msg) throws IOException, JSONException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        LOG.info("进入人工安规测试（不良）载具验证");
        ScadaRespon result = new ScadaRespon();
        try {

            JSONObject reqDataDetail = new JSONObject();
            reqDataDetail.put("IFS", "ACT0047");
            reqDataDetail.put("IN_PSN_NO", deviceRequest.get("con_sn"));
            reqDataDetail.put("IN_ROUT_CD", "ASSY0203");

            JSONObject responseData = mesApiUtils.doPost(reqDataDetail);

            if (responseData.get("STATUS").equals("1")) {
                result.setResult_flag("YES");
                result.setCon_sn(deviceRequest.get("con_sn").toString());
                result.setResult_message("YES");
                result.setFlow_code("1");

            } else if (responseData.get("STATUS").equals("2")) {
                result.setCon_sn(deviceRequest.get("con_sn").toString());
                result.setResult_flag("NO");
                result.setResult_message("YES");
            } else {
                result.setCon_sn(deviceRequest.get("con_sn").toString());
                result.setResult_message("YES");
                if (redisUtils.get("iscg").toString().equals("OK")) { //是参观模式
                    result.setResult_flag("NO");
                } else {
                    result.setResult_flag("NG");
                }

            }
            result.setFlow_code(responseData.getString("MESSAGE"));

        } catch (Exception e) {
            result.setResult_flag("NG");
            result.setResult_message("YES");
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 物料保存&物料验证   1.主板上料 11.底托上料    散热器安装
     *
     * @param msg
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public ScadaRespon saveMaterial(Object msg) throws IOException, JSONException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        ScadaRespon result = new ScadaRespon();
        try {

            switch (deviceRequest.get("device_sn").toString()) {
                case "ECDLC202":
                    LOG.info("进入二线主板与底托物料保存与物料验证");
                    break;

                case "ECDLC204":
                    LOG.info("进入二线散热器安装与物料保存");
                    break;
                case "ECDLC007":
                    LOG.info("进入一线散热器安装与物料保存");
                    break;
                case "ECDLC001":
                    LOG.info("进入一线主板与底托物料保存与物料验证");
                    break;

                default:

                    break;
            }
            JSONArray proSnArray = new JSONArray();
            try {
                proSnArray = deviceRequest.getJSONArray("pro_sn");
            } catch (Exception E) {
                LOG.info("--");
            }

            StringBuilder stringBuider = new StringBuilder();
            for (int i = 0; i < proSnArray.length(); i++) {
                if (i == 0) {
                    stringBuider.append(proSnArray.get(i));
                } else {
                    stringBuider.append("," + proSnArray.get(i));
                }
            }
            //进行物料验证
            JSONObject reqDataMat = new JSONObject();
            reqDataMat.put("IFS", "ACT0053");
            reqDataMat.put("IN_PSN_NO", deviceRequest.get("con_sn")); //载具
            reqDataMat.put("IN_MAT_NO", stringBuider.toString().trim()); //物料条码
            reqDataMat.put("IN_ET_CD", deviceRequest.get("device_sn"));//编码
            try {
                reqDataMat.put("IN_COLLET", deviceRequest.get("dt_sn"));//底托SN
            } catch (Exception e) {
                LOG.info("不是底托");
            }

            AccessTo(deviceRequest.get("device_sn").toString(), deviceRequest.get("con_sn").toString(), 2);
            JSONObject responseMat = mesApiUtils.doPost(reqDataMat);

            if (responseMat.get("STATUS").equals("1")) {

                JSONObject reqDataDetail = new JSONObject();
                reqDataDetail.put("IFS", "STA35");
                reqDataDetail.put("IN_VEHICLES", deviceRequest.get("con_sn"));


                reqDataDetail.put("IN_MATERIALS", stringBuider.toString().trim());

                reqDataDetail.put("IN_DEVICE", deviceRequest.get("device_sn"));
                reqDataDetail.put("IN_WORKNO", deviceRequest.get("work_sn"));
                reqDataDetail.put("IN_USERID", deviceRequest.get("device_sn"));
                reqDataDetail.put("IN_STATUS", deviceRequest.get("status"));
                try {
                    reqDataDetail.put("IN_MACSN", deviceRequest.get("mac_sn")); //MAC地址
                    reqDataDetail.put("IN_DT_SN", deviceRequest.get("dt_sn"));//底托SN
                } catch (Exception e) {
                    LOG.info("不是主板都没有MAC地址");
                }

                JSONObject responseData = mesApiUtils.doPost(reqDataDetail);

                List<Material> materialList = new ArrayList<Material>();

                if (responseData.get("CODE").equals("0")) {
                    result.setResult_flag("OK");
                    result.setResult_message(responseData.getString("MSG"));
                } else {
                    result.setResult_flag("NG");
                    result.setResult_message(responseData.getString("MSG"));
                }

                result.setMaterial(materialList);
                result.setFlow_code("0");

            } else { //物料验证失败，返回NG给上位机
                result.setResult_flag("NG");
                result.setResult_message(responseMat.getString("MESSAGE"));
            }

        } catch (Exception e) {
            result.setResult_flag("NG");
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 物料保存&物料验证   内存 B013
     *
     * @param msg
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public ScadaRespon saveMemory(Object msg) throws IOException, JSONException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        ScadaRespon result = new ScadaRespon();
        JSONArray proSnArray = new JSONArray();
        try {

            try {
                proSnArray = deviceRequest.getJSONArray("pro_sn");
            } catch (Exception e) {
                LOG.info("内存接收的物料proSN为null");
            }
            LOG.info("进入内存物料保存与物料验证");
            StringBuilder stringBuider = new StringBuilder();
            for (int i = 0; i < proSnArray.length(); i++) {
                if (i == 0) {
                    stringBuider.append(proSnArray.get(i));
                } else {
                    stringBuider.append("," + proSnArray.get(i));
                }
            }
            //进行物料验证
            JSONObject reqDataMat = new JSONObject();
            reqDataMat.put("IFS", "ACT0056");
            reqDataMat.put("IN_PSN_NO", deviceRequest.get("con_sn")); //载具
            reqDataMat.put("IN_MAT_PNorQN", stringBuider.toString().trim()); //物料条码
            reqDataMat.put("IN_ET_CD", deviceRequest.get("device_sn"));//设备编码
            JSONObject responseMat = mesApiUtils.doPost(reqDataMat);

            AccessTo(deviceRequest.get("device_sn").toString(), deviceRequest.get("con_sn").toString(), 2);
            if (responseMat.get("STATUS").equals("1")) {

                JSONObject reqDataDetail = new JSONObject();
                reqDataDetail.put("IFS", "STA69");
                reqDataDetail.put("IN_VEHICLES", deviceRequest.get("con_sn"));
                reqDataDetail.put("IN_MATSNS", stringBuider.toString().trim());
                reqDataDetail.put("IN_DEVICE", deviceRequest.get("device_sn"));
                reqDataDetail.put("IN_USERID", deviceRequest.get("device_sn")); //默认设备编号
                reqDataDetail.put("IN_STATUS", deviceRequest.get("status"));

                JSONObject responseData = mesApiUtils.doPost(reqDataDetail);

                List<Material> materialList = new ArrayList<Material>();

                if (responseData.get("CODE").equals("0")) {
                    result.setResult_flag("OK");
                    result.setResult_message(responseData.getString("MSG"));
                } else {
                    result.setResult_flag("NG");
                    result.setResult_message(responseData.getString("MSG"));
                }

                result.setMaterial(materialList);
                result.setFlow_code("0");

            } else { //物料验证失败，返回NG给上位机
                result.setResult_flag("NG");
                result.setResult_message(responseMat.getString("MESSAGE"));
            }

        } catch (Exception e) {
            result.setResult_flag("NG");
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 物料验证  硬盘和光驱 B017
     *
     * @param msg
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public ScadaRespon checkHdMaterial(Object msg) throws IOException, JSONException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        ScadaRespon result = new ScadaRespon();
        String proSnArray = "";
        String cd = "";
        try {


            try {
                proSnArray = deviceRequest.getString("pro_sn"); //硬盘
            } catch (Exception e) {
                LOG.info("硬盘物料为NULL");
            }
            try {
                cd = deviceRequest.getString("cd_sn"); //光驱
            } catch (Exception e) {
                LOG.info("光驱物料为NULL");
            }
            switch (deviceRequest.getString("device_sn")) {
                case "ECDLC012":
                    LOG.info("进入一线硬盘物料验证" + ",硬盘物料为：" + proSnArray);
                    result.setFlow_code("HD");
                    break;
                case "ECDLC211":
                    LOG.info("进入二线硬盘物料验证" + ",硬盘物料为：" + proSnArray);
                    result.setFlow_code("HD");
                    break;
                case "ECDLC209":
                    LOG.info("进入二线光驱物料验证" + ",光驱物料为：" + cd);
                    result.setFlow_code("CD");
                    break;
                default:
                    LOG.info("进入一线光驱物料验证" + ",光驱物料为：" + cd);
                    result.setFlow_code("CD");
                    break;
            }
            proSnArray = deviceRequest.getString("pro_sn"); //硬盘
            cd = deviceRequest.getString("cd_sn"); //光驱
            //进行物料验证
            JSONObject reqDataMat = new JSONObject();
            reqDataMat.put("IFS", "STA109");
            reqDataMat.put("IN_PSN_NO", deviceRequest.get("con_sn")); //载具
            reqDataMat.put("IN_YP_SN", proSnArray); //硬盘原厂SN，物料条码(硬盘)
            reqDataMat.put("IN_CD_QN", cd); //物料条码(光驱)
            reqDataMat.put("IN_ET_CD", deviceRequest.get("device_sn"));//设备编码
            reqDataMat.put("IN_YP_QN", deviceRequest.get("mat_qn"));//硬盘浪潮QN
            JSONObject responseMat = mesApiUtils.doPost(reqDataMat);

            if (responseMat.get("STATUS").equals("1")) { //成功
                result.setResult_flag("OK");
                result.setResult_message(responseMat.getString("MESSAGE"));
            } else if (responseMat.get("STATUS").equals("0")) { //物料验证失败，返回NG给上位机
                result.setResult_flag("NG");
                result.setResult_message(responseMat.getString("MESSAGE"));
            } else { //报警
                result.setResult_flag("Err");
                result.setResult_message(responseMat.getString("MESSAGE"));
            }

        } catch (Exception e) {
            result.setResult_flag("NG");
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 光驱硬盘保存
     *
     * @param msg
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public ScadaRespon saveHdMaterials(Object msg) throws IOException, JSONException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        ScadaRespon result = new ScadaRespon();
        String proSnArray = "";
        String cd = "";
        try {


            try {
                proSnArray = deviceRequest.getString("pro_sn"); //硬盘
            } catch (Exception e) {
                LOG.error("硬盘物料为NULL");
            }
            try {
                cd = deviceRequest.getString("cd_sn"); //光驱
            } catch (Exception e) {
                LOG.error("光驱物料为NULL");
            }
            switch (deviceRequest.getString("device_sn")) {
                case "ECDLC012":
                    LOG.info("进入一线硬盘保存" + ",硬盘物料为：" + proSnArray);
                    break;
                case "ECDLC211":
                    LOG.info("进入二线硬盘保存" + ",硬盘物料为：" + proSnArray);
                    break;
                case "ECDLC209":
                    LOG.info("进入二线光驱保存" + ",光驱物料为：" + cd);
                    break;
                default:
                    LOG.info("进入一线光驱保存" + ",光驱物料为：" + cd);
                    break;
            }

            JSONObject reqDataDetail = new JSONObject();
            reqDataDetail.put("IFS", "STA110");
            reqDataDetail.put("IN_VEHICLES", deviceRequest.get("con_sn"));
            reqDataDetail.put("IN_YP_QN", deviceRequest.get("mat_qn")); //硬盘浪潮QN
            reqDataDetail.put("IN_ET_CD", deviceRequest.get("device_sn"));
            reqDataDetail.put("IN_USERID", deviceRequest.get("device_sn"));
            reqDataDetail.put("IN_MAT_STA", deviceRequest.get("status")); //物料扫码状态
            reqDataDetail.put("IN_CD_QN", cd); //光驱
            reqDataDetail.put("IN_YP_SN", proSnArray); //硬盘原厂SN
            reqDataDetail.put("IN_ROSE_STA", deviceRequest.get("screw_status")); //螺丝锁附状态
            JSONArray arr = deviceRequest.getJSONArray("screw_force"); //螺丝扭力与螺丝号
            for (int i = 0; i < arr.length(); i++) {

                reqDataDetail.put("IN_DEFAULT_" + (i + 1), arr.get(i));
            }

            AccessTo(deviceRequest.get("device_sn").toString(), deviceRequest.get("con_sn").toString(), 2);
            JSONObject responseData = mesApiUtils.doPost(reqDataDetail);

            List<Material> materialList = new ArrayList<Material>();

            if (responseData.get("CODE").equals("0")) {
                result.setResult_flag("OK");
                result.setResult_message(responseData.getString("MSG"));
            } else {
                result.setResult_flag("NG");
                result.setResult_message(responseData.getString("MSG"));
            }

            result.setMaterial(materialList);
            result.setFlow_code("0");

        } catch (Exception e) {
            result.setResult_flag("NG");
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 物料保存  硬盘和光驱 B018
     *
     * @param msg
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public ScadaRespon saveHdMaterial(Object msg) throws IOException, JSONException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        ScadaRespon result = new ScadaRespon();
        String proSnArray = "";
        String cd = "";
        try {


            try {
                proSnArray = deviceRequest.getString("pro_sn"); //硬盘
            } catch (Exception e) {
                LOG.error("硬盘物料为NULL");
            }
            try {
                cd = deviceRequest.getString("cd_sn"); //光驱
            } catch (Exception e) {
                LOG.error("光驱物料为NULL");
            }
            switch (deviceRequest.getString("device_sn")) {
                case "ECDLC012":
                    LOG.info("进入一线硬盘保存" + ",硬盘物料为：" + proSnArray);
                    break;
                case "ECDLC211":
                    LOG.info("进入二线硬盘保存" + ",硬盘物料为：" + proSnArray);
                    break;
                case "ECDLC209":
                    LOG.info("进入二线光驱保存" + ",光驱物料为：" + cd);
                    break;
                default:
                    LOG.info("进入一线光驱保存" + ",光驱物料为：" + cd);
                    break;
            }

            JSONObject reqDataDetail = new JSONObject();
            reqDataDetail.put("IFS", "STA110");
            reqDataDetail.put("IN_VEHICLES", deviceRequest.get("con_sn"));
            reqDataDetail.put("IN_YP_QN", deviceRequest.get("mat_qn")); //硬盘浪潮QN
            reqDataDetail.put("IN_ET_CD", deviceRequest.get("device_sn"));
            reqDataDetail.put("IN_USERID", deviceRequest.get("device_sn"));
            reqDataDetail.put("IN_MAT_STA", deviceRequest.get("status")); //物料扫码状态
            reqDataDetail.put("IN_CD_QN", cd); //光驱
            reqDataDetail.put("IN_YP_SN", proSnArray); //硬盘原厂SN
            reqDataDetail.put("IN_ROSE_STA", deviceRequest.get("screw_status")); //螺丝锁附状态
            JSONArray arr = deviceRequest.getJSONArray("screw_force"); //螺丝扭力与螺丝号
            for (int i = 0; i < arr.length(); i++) {

                reqDataDetail.put("IN_DEFAULT_" + (i + 1), arr.get(i));
            }

            AccessTo(deviceRequest.get("device_sn").toString(), deviceRequest.get("con_sn").toString(), 2);
            JSONObject responseData = mesApiUtils.doPost(reqDataDetail);

            List<Material> materialList = new ArrayList<Material>();

            if (responseData.get("CODE").equals("0")) {
                result.setResult_flag("OK");
                result.setResult_message(responseData.getString("MSG"));
            } else {
                result.setResult_flag("NG");
                result.setResult_message(responseData.getString("MSG"));
            }

            result.setMaterial(materialList);
            result.setFlow_code("0");

        } catch (Exception e) {
            result.setResult_flag("NG");
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 不用物料验证（主板下线解绑）
     *
     * @param msg
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public ScadaRespon saveMaterial2(Object msg) throws IOException, JSONException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        ScadaRespon result = new ScadaRespon();
        try {

            JSONObject reqDataDetail = new JSONObject();
            reqDataDetail.put("IFS", "STA35");
            reqDataDetail.put("IN_VEHICLES", deviceRequest.get("con_sn"));
            JSONArray proSnArray = new JSONArray();
            try {
                proSnArray = deviceRequest.getJSONArray("pro_sn");
            } catch (Exception e) {

            }

            StringBuilder stringBuider = new StringBuilder();
            for (int i = 0; i < proSnArray.length(); i++) {
                if (i == 0) {
                    stringBuider.append(proSnArray.get(i));
                } else {
                    stringBuider.append("," + proSnArray.get(i));
                }
            }

            reqDataDetail.put("IN_MATERIALS", stringBuider.toString().trim());

            reqDataDetail.put("IN_DEVICE", deviceRequest.get("device_sn"));
            reqDataDetail.put("IN_WORKNO", deviceRequest.get("work_sn"));
            reqDataDetail.put("IN_USERID", deviceRequest.get("device_sn"));
            reqDataDetail.put("IN_FLAG", "1");

            if (deviceRequest.get("device_sn").equals("ECDLC005")) {
                LOG.info("进入一线主板下线解绑");
            } else {
                LOG.info("进入二线主板下线解绑");
            }

            JSONObject responseData = mesApiUtils.doPost(reqDataDetail);

            List<Material> materialList = new ArrayList<Material>();

            if (responseData.get("CODE").equals("0")) {
                result.setResult_flag("OK");
                result.setResult_message(responseData.getString("MSG"));

            } else {
                result.setResult_flag("NG");
                result.setResult_message(responseData.getString("MSG"));
            }

            result.setMaterial(materialList);
            result.setFlow_code("0");

        } catch (Exception e) {
            result.setResult_flag("NG");
            LOG.error("mes没有返回是否清料标识字段");
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 散热器打螺丝&主板打螺丝 保存过站（没有物料验证）
     *
     * @param msg
     * @return
     * @throws IOException
     * @throws JSONException
     */
    @SuppressWarnings("all")
    public ScadaRespon saveMaterialsalfNo(Object msg) throws IOException, JSONException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        ScadaRespon result = new ScadaRespon();

        try {

            JSONObject reqDataDetail = new JSONObject();
            reqDataDetail.put("IFS", "STA73");
            JSONArray arr = null;
            try {
                System.out.println(deviceRequest.getJSONArray("screw_force").toString());
                arr = deviceRequest.getJSONArray("screw_force"); //螺丝扭力与螺丝号
            } catch (Exception e) {
                LOG.info("螺丝扭力数组为NULL");
            }
            JSONObject jso = new JSONObject();

            reqDataDetail.put("IN_VEHICLES", deviceRequest.get("con_sn"));
            reqDataDetail.put("IN_DEVICE", deviceRequest.get("device_sn"));
            reqDataDetail.put("IN_USERID", deviceRequest.get("device_sn"));
            reqDataDetail.put("IN_STATUS", deviceRequest.get("screw_status"));
            reqDataDetail.put("IN_SCREW", deviceRequest.get("screw_status"));
            for (int i = 0; i < arr.length(); i++) {

                reqDataDetail.put("IN_DEFAULT_" + (i + 1), arr.get(i));
            }

            if (deviceRequest.get("device_sn").equals("ECDLC206")) {
                LOG.info("进入二线散热器打螺丝保存过站");
            } else if (deviceRequest.get("device_sn").equals("ECDLC090")) {
                LOG.info("进入一线散热器打螺丝保存过站");
            } else if (deviceRequest.get("device_sn").equals("ECDLC010")) {
                LOG.info("进入一线主板打螺丝保存过站");
            } else {
                LOG.info("进入二线主板打螺丝保存过站");
            }

            AccessTo(deviceRequest.get("device_sn").toString(), deviceRequest.get("con_sn").toString(), 2);
            JSONObject responseData = mesApiUtils.doPost(reqDataDetail);

            List<Material> materialList = new ArrayList<Material>();

            if (responseData.get("CODE").equals("0")) {
                result.setResult_flag("OK");
                result.setResult_message(responseData.getString("MSG"));
            } else {
                result.setResult_flag("NG");
                result.setResult_message(responseData.getString("MSG"));
            }

            result.setMaterial(materialList);
            result.setFlow_code("0");

        } catch (Exception e) {
            result.setResult_flag("NG");
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 铭牌标签贴附上传结果，所有设备共用（设备装配过程中如果NG） B09
     *
     * @param msg
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public ScadaRespon saveMaterialLabel(Object msg) throws IOException, JSONException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        ScadaRespon result = new ScadaRespon();
        try {

            JSONObject reqDataDetail = new JSONObject();
            reqDataDetail.put("IFS", "STA63");
            reqDataDetail.put("IN_VEHICLES", deviceRequest.get("con_sn"));

            reqDataDetail.put("IN_ET_CD", deviceRequest.get("device_sn"));
            reqDataDetail.put("IN_USERID", deviceRequest.get("device_sn"));
            reqDataDetail.put("IN_STATUS", deviceRequest.get("status"));

            if (deviceRequest.get("device_sn").equals("ECDLC023")) {
                LOG.info("进入一线铭牌标签贴附上传结果");
            } else if (deviceRequest.get("device_sn").equals("ECDLC216")) {
                LOG.info("进入二线铭牌标签贴附上传结果");
            } else {
                LOG.info("其他设备装备过程中NG，设备号为：" + deviceRequest.get("device_sn"));
            }

            AccessTo(deviceRequest.get("device_sn").toString(), deviceRequest.get("con_sn").toString(), 2);
            JSONObject responseData = mesApiUtils.doPost(reqDataDetail);


            List<Material> materialList = new ArrayList<Material>();

            if (responseData.get("CODE").equals("0")) {
                result.setResult_flag("OK");
                result.setResult_message(responseData.getString("MSG"));
            } else {
                result.setResult_flag("NG");
                result.setResult_message(responseData.getString("MSG"));
            }

            result.setMaterial(materialList);
            result.setFlow_code("0");

        } catch (Exception e) {
            result.setResult_flag("NG");
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 安规测试工位(B04) 上传测试结果
     *
     * @param msg
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public ScadaRespon safetyTest(Object msg) throws JSONException, IOException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        ScadaRespon result = new ScadaRespon();
        try {

            JSONObject reqDataDetail = new JSONObject();
            reqDataDetail.put("IFS", "STA38");
            reqDataDetail.put("IN_VEHICLES", deviceRequest.get("con_sn")); //载具SN
            reqDataDetail.put("IN_DEVICE", deviceRequest.get("device_sn")); //设备SN
            reqDataDetail.put("IN_STATUS", deviceRequest.get("ts_result")); //测试状态
            reqDataDetail.put("IN_USERID", deviceRequest.get("device_sn")); //操作人


            reqDataDetail.put("IN_REASON", deviceRequest.get("bg_msg")); //异常描述
            if (deviceRequest.get("device_sn").equals("ECDLC212")) {
                LOG.info("进入二线安规测试工位(B04) 上传测试结果，异常描述为：" + deviceRequest.get("bg_msg"));
            } else {
                LOG.info("进入一线线安规测试工位(B04) 上传测试结果,异常描述为:" + deviceRequest.get("bg_msg"));
            }

            AccessTo(deviceRequest.get("device_sn").toString(), deviceRequest.get("con_sn").toString(), 2);
            JSONObject responseData = mesApiUtils.doPost(reqDataDetail);

            if (responseData.get("CODE").equals("0")) {
                result.setResult_flag("OK");

            } else {
                result.setResult_flag("NG");
            }
            result.setResult_message(responseData.getString("MSG"));
            result.setFlow_code("0");

        } catch (Exception e) {
            result.setResult_flag("NG");
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 前测载具验证  B06
     *
     * @param msg
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public ScadaRespon checkBeforeTest(Object msg) throws IOException, JSONException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        ScadaRespon result = new ScadaRespon();
        try {


            JSONObject reqDataDetail = new JSONObject();
            reqDataDetail.put("IFS", "ACT0045");
            reqDataDetail.put("IN_PSN_NO", deviceRequest.get("con_sn"));
            reqDataDetail.put("IN_ROUT_CD", "ASSY0202");
            JSONObject responseData = mesApiUtils.doPost(reqDataDetail);

            if (responseData.get("STATUS").equals("1")) { //yes
                result.setResult_flag("YES");
                result.setResult_message("YES");
                result.setCon_sn(responseData.getString("PSNNO"));
                result.setMo_num(deviceRequest.getString("mo_num"));
                result.setStatus(responseData.getString("MESSAGE"));
                result.setFirst(responseData.getString("VFIRST"));
            } else if (responseData.get("STATUS").equals("2")) { //已经测试过的：no
                result.setResult_flag("NO");
                result.setResult_message("YES");
                result.setMo_num(deviceRequest.getString("mo_num"));
                result.setCon_sn(deviceRequest.get("con_sn").toString());
                result.setStatus(responseData.getString("MESSAGE"));
                result.setFirst(responseData.getString("VFIRST"));
            } else if (responseData.get("STATUS").equals("3")) { //重复扫码
                result.setResult_flag("NG");
                result.setCon_sn(deviceRequest.get("con_sn").toString());
                result.setResult_message("YES");
                result.setStatus(responseData.getString("MESSAGE"));
            } else {//操作失败
                result.setResult_flag("NO");
                result.setCon_sn(deviceRequest.get("con_sn").toString());
                result.setResult_message("YES");
                result.setStatus(responseData.getString("MESSAGE"));
            }
            result.setFlow_code("1");

        } catch (Exception e) {
            result.setResult_flag("NO");
            result.setResult_message("YES");
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 前测（修改测试状态）B07
     *
     * @param msg
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public ScadaRespon beforeTest(Object msg) throws JSONException, IOException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        ScadaRespon result = new ScadaRespon();
        try {

            JSONObject reqDataDetail = new JSONObject();
            reqDataDetail.put("IFS", "STA52");
            reqDataDetail.put("IN_VEHICLES", deviceRequest.get("con_sn")); //载具SN
            reqDataDetail.put("IN_STATUS", "1"); //测试状态
            reqDataDetail.put("IN_USERID", deviceRequest.get("device_sn")); //操作人

            JSONObject responseData = mesApiUtils.doPost(reqDataDetail);

            if (responseData.get("CODE").equals("0")) {
                result.setResult_flag("OK");

            } else {
                result.setResult_flag("NG");
            }
            result.setResult_message("NO");
            result.setFlow_code("0");

        } catch (Exception e) {
            result.setResult_flag("NG");
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 老化绑定（上吸吊移栽）验证接口
     *
     * @param msg
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public ScadaRespon oldTestCheck(Object msg) throws IOException, JSONException {
        JSONObject deviceRequest = new JSONObject(msg.toString());

        JSONObject reqDataDetail = new JSONObject();
        reqDataDetail.put("IFS", "ACT0042");
        reqDataDetail.put("IN_PSN_NO", deviceRequest.get("con_sn"));
        reqDataDetail.put("IN_ET_CD", deviceRequest.get("device_sn"));

        JSONObject responseData = mesApiUtils.doPost(reqDataDetail);

        ScadaRespon result = new ScadaRespon();

        if (responseData.get("STATUS").equals("1")) {
            result.setResult_flag("OK");
            result.setResult_message(responseData.getString("MESSAGE"));

        } else {
            result.setResult_flag("NG");
            result.setResult_message(responseData.getString("MESSAGE"));
        }

        return result;
    }

    /**
     * 主板打螺丝&散热器打螺丝，Z01
     *
     * @param msg
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public ScadaRespon autoScrew(Object msg) throws JSONException, IOException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        ScadaRespon result = new ScadaRespon();
        try {

            JSONObject reqDataDetail = new JSONObject();
            reqDataDetail.put("IFS", "ACT0046");
            reqDataDetail.put("IN_PSN_NO", deviceRequest.get("con_sn"));
            reqDataDetail.put("IN_ET_CD", deviceRequest.get("device_sn"));

            if (deviceRequest.get("device_sn").equals("ECDLC206")) {
                LOG.info("进入二线散热器打螺丝载具验证");
            } else if (deviceRequest.get("device_sn").equals("ECDLC090")) {
                LOG.info("进入一线散热器打螺丝载具验证");
            } else if (deviceRequest.get("device_sn").equals("ECDLC010")) {
                LOG.info("进入一线主板打螺丝载具验证");
            } else {
                LOG.info("进入二线主板打螺丝载具验证");
            }

            JSONObject responseData = mesApiUtils.doPost(reqDataDetail);

            if (responseData.get("STATUS").equals("1")) {
                result.setResult_flag("OK");
                result.setResult_message(responseData.getString("MESSAGE"));

                List<Material> materialList = new ArrayList<Material>();
                Material material = new Material();
                result.setPro_mod(responseData.getString("ITEMCD"));
                materialList.add(material);

                result.setMaterial(materialList);
                result.setCd(responseData.getString("CDHD"));
                result.setZb_code(responseData.getString("ZB_IDNRK"));
            } else if (responseData.get("STATUS").equals("0")) {
                result.setResult_flag("NG");
                result.setResult_message(responseData.getString("MESSAGE"));
            } else { //2
                result.setResult_flag("PASS");
                result.setResult_message(responseData.getString("MESSAGE"));
            }
            result.setFlow_code("1");
            AccessTo(deviceRequest.get("device_sn").toString(), deviceRequest.get("con_sn").toString(), 1);
        } catch (Exception e) {
            result.setResult_flag("NG");
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 设备异常时，记录信息
     *
     * @param
     * @return
     * @throws JSONException
     */
    public ScadaResponShow devException(Object msg) throws JSONException {
        com.alibaba.fastjson.JSONObject deviceRequest = com.alibaba.fastjson.JSONObject.parseObject(msg.toString());
        LOG.debug("接收B03操作码数据：" + deviceRequest);
        ScadaResponShow result = new ScadaResponShow();
        try {
            ObjectMapper s = new JsonDealUtils();
            String obj = s.writeValueAsString(deviceRequest); //null转为空字符串
            JSONObject json = new JSONObject(obj); //json字符串转为json对象
            //取设备状态字段，更新设备表  dev_st
            String dev_st = json.getString("dev_st");//设备状态
            String device_sn = json.getString("device_sn");//设备编码
            String bg_start = json.getString("bg_start");//异常开始时间
            String bg_end = json.getString("bg_end");//异常结束时间
            String bg_msg = json.getString("bg_msg");//异常描述
            String sql = "SELECT BG_START,ET_CD,ET_NM FROM IPLANT1.C_IPLANT_E2_T WHERE ET_CD=?";
            Map<String, Object> mapde = template.queryForMap(sql, device_sn);
            Object etNm = mapde.get("ET_NM");
            if (!bg_end.equals("")) { //异常结束时间

                Object start = mapde.get("BG_START");
                String deSql = "DELETE FROM SCADA_BG_J WHERE ET_CD=? AND BG_START IS NULL";
                template.update(deSql,device_sn);
                //查询报表异常时间差
                String timeSt = "SELECT BG_START,ID FROM SCADA_BG_J WHERE ET_CD=? AND BG_END IS NULL";
                 List<Map<String, Object>> maps = template.queryForList(timeSt, device_sn);

                if (start != null) {

                    try {
                        long[] datePoor = DateUtils.getDistanceTimes(start.toString(), bg_end);
                        String bgSql = "UPDATE IPLANT1.C_IPLANT_E2_T SET BG_END='',BG_MSG='',BG_START='',STOP_SUM=STOP_SUM+? WHERE ET_CD=?";

                        //插入设备异常详细信息，记录报表
                        String reportSql = "UPDATE  SCADA_BG_J SET BG_END=?,BG_TIME= ? WHERE ID=? AND BG_END IS NULL";
                      if(maps.size() > 0) {
                          for (int i= 0; i<maps.size(); i++) {
                              long[] datePoorbg = DateUtils.getDistanceTimes(maps.get(i).get("BG_START").toString(), bg_end);
                              template.update(reportSql,bg_end,datePoorbg[2],maps.get(i).get("ID"));
                          }
                      }


                        template.update(bgSql, datePoor[2], device_sn);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Map<String, Object> map = new HashMap<>();
                map.put("BG_START", bg_start);
                map.put("BG_END", bg_end);
                map.put("BG_MSG", bg_msg);
                map.put("ET_STA", dev_st);
                map.put("ET_CD", device_sn);
                ciPLANTeTMapper.updateByPrimaryKey(map);

                String squ = "SELECT EQUIP_BG.NEXTVAL sq FROM DUAL";
                Map<String, Object> queryForMap = template.queryForMap(squ);
                Object object = queryForMap.get("sq");//获取序列自增id
                String reportSql = "INSERT INTO SCADA_BG_J(ET_CD, BG_END, BG_TIME, ET_NM, BG_MSG, BG_START,ID) VALUES(?,?,?,?,?,?,?)";
                template.update(reportSql,device_sn,bg_end,"",etNm,bg_msg,bg_start,object);
            }

            String staSql = "UPDATE IPLANT1.C_IPLANT_E2_T SET ET_STA = ?,UPT_DT=sysdate WHERE ET_CD=?";
            template.update(staSql, dev_st, device_sn);
            result.setResult_flag("OK");
            result.setResult_message("收到B03操作码");
        } catch (Exception e) {
            result.setResult_flag("NG");
            LOG.error("BO3操作接口异常");
            LOG.error(e.toString(), e);
        }

        return result;
    }

    /**
     * 后测载具验证  B066 （验证能不能进工作台）
     *
     * @param msg
     * @throws IOException
     * @throws JSONException
     * @returnk
     */
    public ScadaResponShow checkAfterTest(Object msg) throws IOException, JSONException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        ScadaResponShow result = new ScadaResponShow();
        try {


            JSONObject reqDataDetail = new JSONObject();
            reqDataDetail.put("IFS", "ACT0077");
            reqDataDetail.put("IN_PSN_NO", deviceRequest.get("con_sn"));
            reqDataDetail.put("IN_ROUT_CD", "ASSY04");
            JSONObject responseData = mesApiUtils.doPost(reqDataDetail);

            if (responseData.get("STATUS").equals("1")) { //yes
                result.setResult_flag("YES");
                result.setResult_message(responseData.getString("MESSAGE"));
                result.setCon_sn(responseData.getString("PSNNO"));

            } else if (responseData.get("STATUS").equals("2")) { //已经测试过的：no
                result.setResult_flag("NO");
                result.setResult_message(responseData.getString("MESSAGE"));
            } else { //NG
                result.setResult_flag("NG");
                result.setResult_message(responseData.getString("MESSAGE"));
            }
            result.setFlow_code("1");
            result.setMo_num(deviceRequest.getString("mo_num"));
        } catch (Exception e) {
            result.setResult_flag("NG");
            result.setMo_num(deviceRequest.getString("mo_num"));
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 后测 验证良品还是不良品还是未测 B067
     *
     * @param msg
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public ScadaResponShow afterTestGood(Object msg) throws JSONException, IOException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        ScadaResponShow result = new ScadaResponShow();
        try {

            JSONObject reqDataDetail = new JSONObject();
            reqDataDetail.put("IFS", "ACT0076");
            reqDataDetail.put("IN_PSN_NO", deviceRequest.get("con_sn")); //载具SN

            JSONObject responseData = mesApiUtils.doPost(reqDataDetail);

            if (responseData.get("STATUS").equals("1")) { //YES，良品
                result.setResult_flag("YES");

            } else if (responseData.get("STATUS").equals("0")) { //NG,
                result.setResult_flag("NG");
            } else { // NO  不良品或未测
                result.setResult_flag("NO");
            }
            result.setResult_message(responseData.getString("MESSAGE"));
            result.setFlow_code("0");
            result.setMo_num(deviceRequest.getString("mo_num"));
        } catch (Exception e) {
            result.setResult_flag("NG");
            result.setMo_num(deviceRequest.getString("mo_num"));
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 后测 中间扫描枪验证流向（右边维修工位与测试台之间那把扫描枪）B068
     *
     * @param msg
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public ScadaResponShow afterTestCenter(Object msg) throws JSONException, IOException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        ScadaResponShow result = new ScadaResponShow();
        try {

            JSONObject reqDataDetail = new JSONObject();
            reqDataDetail.put("IFS", "ACT0078");
            reqDataDetail.put("IN_PSN_NO", deviceRequest.get("con_sn")); //载具SN
            reqDataDetail.put("IN_FULL_STATUS", deviceRequest.get("full_status")); //有工装板必须传：1，无载板传0

            JSONObject responseData = mesApiUtils.doPost(reqDataDetail);

            if (responseData.get("STATUS").equals("0")) { //NG
                result.setResult_flag("NG");

            } else if (responseData.get("STATUS").equals("1")) { //YES,进入工作台测试
                result.setResult_flag("YES");
            } else if (responseData.get("STATUS").equals("2")) { //NO,产品回流
                result.setResult_flag("NO");
            } else { // PUT,不良品放行流到维修位
                result.setResult_flag("PUT");
            }
            result.setResult_message(responseData.getString("MESSAGE"));
            result.setFlow_code("0");
            result.setMo_num(deviceRequest.getString("mo_num"));
        } catch (Exception e) {
            result.setResult_flag("NG");
            result.setMo_num(deviceRequest.getString("mo_num"));
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 后测 维修工作台验证 B069
     *
     * @param msg
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public ScadaResponShow afterTestMaintain(Object msg) throws JSONException, IOException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        ScadaResponShow result = new ScadaResponShow();
        try {

            JSONObject reqDataDetail = new JSONObject();
            reqDataDetail.put("IFS", "STA84");
            reqDataDetail.put("IN_VEHICLES", deviceRequest.get("con_sn")); //载具SN

            JSONObject responseData = mesApiUtils.doPost(reqDataDetail);

            if (responseData.get("CODE").equals("0")) { //OK
                result.setResult_flag("OK");

            } else { // NG
                result.setResult_flag("NG");
            }
            result.setResult_message(responseData.getString("MSG"));
            result.setFlow_code("0");
            result.setMo_num(deviceRequest.getString("mo_num"));
        } catch (Exception e) {
            result.setResult_flag("NG");
            result.setMo_num(deviceRequest.getString("mo_num"));
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 后测最后一把扫码枪解绑
     *
     * @param msg
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public ScadaResponShow afterTestNoBind(Object msg) throws JSONException, IOException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        ScadaResponShow result = new ScadaResponShow();
        try {

            JSONObject reqDataDetail = new JSONObject();
            reqDataDetail.put("IFS", "ACT0075");
            reqDataDetail.put("IN_PSN_NO", deviceRequest.get("con_sn")); //载具SN

            JSONObject responseData = mesApiUtils.doPost(reqDataDetail);

            if (responseData.get("STATUS").equals("1")) { //OK
                result.setResult_flag("OK");

            } else { // NG
                result.setResult_flag("NG");
            }
            result.setResult_message(responseData.getString("MESSAGE"));
            result.setFlow_code("0");
            result.setMo_num(deviceRequest.getString("mo_num"));
        } catch (Exception e) {
            result.setResult_flag("NG");
            result.setMo_num(deviceRequest.getString("mo_num"));
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 设备可以上空框或者可以送料时，上位机告知 C02
     *
     * @param msg
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public ScadaResponShow isEmptyAndSend(Object msg) throws JSONException, IOException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        ScadaResponShow result = new ScadaResponShow();
        String type = deviceRequest.get("mo_num").toString(); //下层1（对应AGV的2，下料点  ） 上层2 （对应AGV的3 ，回收空框点）
        String message = null;
        String zbSql = null;
        JSONObject js = new JSONObject();
        try {
            switch (deviceRequest.get("mat_point").toString()) {
                case "ECD102": //A线主板下线通知可以回空框
                    if (type.equals("2")) {
                        LOG.info("一线主板下料机通知可以回空框，去修改数据库状态");
                        zbSql = "update ET_IP_AGV set STATUS= 3 where MAT_POINT='" + deviceRequest.getString("mat_point") + "' ";
                        message = "收到一线主板下料机可以回空框的通知";
                        template.update(zbSql);
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
                    }
                    break;

                case "ECD201":  //二线主板下料机通知可以回空框，去修改数据库状态
                    if (type.equals("2")) {
                        LOG.info("二线主板下料机通知可以回空框，去修改数据库状态");
                        zbSql = "update ET_IP_AGV set STATUS= 3 where MAT_POINT='" + deviceRequest.getString("mat_point") + "' ";
                        message = "收到二线主板下料机可以回空框的通知";
                        template.update(zbSql);
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
                    }
                    break;
                default:

            }
            //查询表里此点位是否有未完成滚动的AGV任务号
            String matPoint = deviceRequest.get("mat_point").toString(); // 下料点点位
            String sql = "SELECT TASKNO,AGV_POINT FROM ET_IP_AGV WHERE MAT_POINT='" + matPoint + "' ";
            Map<String, Object> objectMap = template.queryForMap(sql);
            ScadaRequest sq = new ScadaRequest();
            if (objectMap.get("TASKNO") != null) {
                LOG.info("下层有未完成滚动的AGV任务，通知AGV滚动进料，点位：" + matPoint);
                if (Matpoint.CD_TAKE_POINT_LINE1.equals(matPoint) || Matpoint.YP_TAKE_POINT_LINE1.equals(matPoint) ||
                        Matpoint.CD_TAKE_POINT_LINE2.equals(matPoint) || Matpoint.YP_TAKE_POINT_LINE2.equals(matPoint)
                   ) {
                    //通知设备滚动

                    sq.setOp_flag("B28");
                    sq.setMat_point(matPoint);
                    sq.setMo_num("1");
                    sq.setEt_ip(redisUtil.hget("matPoint", matPoint).toString());
                    sam.toDevice(sq);
                } else if( Matpoint.ZB_EMPTY_POINT_LINE1.equals(matPoint) || Matpoint.ZB_EMPTY_POINT_LINE2.equals(matPoint)) {
                    sq.setOp_flag("B23");
                    sq.setMat_point(matPoint);
                    sq.setMo_num("2");
                    sq.setEt_ip(redisUtil.hget("matPoint", matPoint).toString());
                    sam.toDevice(sq);
                }

                JSONObject jsons = new JSONObject();
                jsons.put("taskCode", objectMap.get("TASKNO").toString()); //唯一任务号，必填
                jsons.put("currentWb", objectMap.get("AGV_POINT").toString()); //当前点位，必填
                if(matPoint.equals("ECD102") || matPoint.equals("ECD201")) {
                    jsons.put("nodeType", "3"); //针对主板下线回空框
                } else {
                    jsons.put("nodeType", "2"); //1上料点，2下料点，3回收空框点，4空上箱点(上到AGV车上)
                }


                JSONObject jsonResult = sam.sayUpDown(jsons, 0);
                if (jsonResult.getString("code").equals("0")) {
                    sql = "update ET_IP_AGV set TASKNO= '' where MAT_POINT=? ";
                    template.update(sql, matPoint);
                }
            }
            result.setResult_flag("OK");
            result.setResult_message(message);
        } catch (Exception e) {
            result.setResult_flag("NG");
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 心跳
     *
     * @param msg
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public ScadaResponShow heartBeat(Object msg) throws JSONException, IOException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        ScadaResponShow result = new ScadaResponShow();
        try {
            LOG.debug("心跳接收");
            String deviceSn = deviceRequest.get("device_sn").toString(); // 设备号
            String workSn = deviceRequest.get("work_sn").toString(); //设备名
            redisUtil.hset("state", deviceSn, new Date());

        } catch (Exception e) {
            result.setResult_flag("NG");
            LOG.error(e.toString(), e);
        }
        return result;
    }

    /**
     * 包装判定工单
     *
     * @param msg
     * @return
     * @throws JSONExceptio
     * @throws IOException
     */
    public ScadaResponShow packMoCheck(Object msg) throws JSONException, IOException {
        JSONObject deviceRequest = new JSONObject(msg.toString());
        ScadaResponShow result = new ScadaResponShow();
        try {
            LOG.info("进入包装判定工单");
            String workSn = deviceRequest.get("work_sn").toString(); //外箱SN
            String cpSql = "SELECT STATE FROM (SELECT STATE FROM PRODUCT_SN WHERE SERNR=? ORDER BY CRT_DT DESC) WHERE ROWNUM=1";
             Map<String, Object> map = template.queryForMap(cpSql, workSn);
            String state= map.get("STATE").toString();

            JSONObject reqDataDetail = new JSONObject();
            reqDataDetail.put("IFS", "ACT0083");
            reqDataDetail.put("IN_PSN_NO", workSn); //SN

            JSONObject responseData = mesApiUtils.doPost(reqDataDetail);
            result.setResult_flag("OK");
            result.setResult_message(responseData.getString("MESSAGE"));
            result.setFlow_code(responseData.getString("STATUS"));

            if(state.equals("0")) {
                LOG.info("调用完工确认接口，SN为：" + workSn);
                String sure = confir.cpletionConfir(workSn, "50");//完工确认
                LOG.info("完工确认已返回"+sure);
                String sql = "UPDATE PRODUCT_SN SET STATE=1 ,UP_DT=sysdate,DES=? WHERE SERNR=?";
                template.update(sql, sure, workSn);
                try {
                    if (sure.length() > 6 || sure.equals("报工失败")) {
                        String insertSql = "INSERT INTO DEMO_YUAN(INFO,SN,RECORD_TIME) VALUES (?,?,sysdate)";
                        template.update(insertSql, sure,workSn);
                       // template.update(insertSql, result, workSn);
                        LOG.info("插入到动态报工： SN为"+workSn+"  sap返回结果为："+sure);
                    }
                } catch (DataAccessException e) {
                    LOG.info("插入到动态报工：" +"数据库插入异常");
                    e.printStackTrace();
                }
            }
    } catch (Exception e) {
        result.setResult_flag("NG");
        result.setFlow_code("0");
        result.setResult_message("外箱SN给错，扫码错误");
        LOG.error(e.toString(), e);
    }
        return result;
    }

    /**
     * 入料和出料时间
     *
     * @param proSt
     * @param proEnd
     * @param conSn
     */
    public void AccessTo(String etCd, String conSn, int fl) {
        String sql = "";
        try {
            if (fl == 1) { //是入料，入料要清掉出料时间和生产节拍
                sql = "UPDATE IPLANT1.C_IPLANT_E2_T SET PRO_START=sysdate,PRO_END='',CT='' WHERE ET_CD = ? ";
                template.update(sql, etCd);
            } else { //出料
                sql = "UPDATE IPLANT1.C_IPLANT_E2_T SET PRO_END=sysdate WHERE ET_CD = ? ";
                template.update(sql, etCd);
                //出料完成，计算生产节拍
                String comSql = "SELECT to_char(PRO_START,'yyyy/MM/dd HH24:mi:ss') PRO_START," +
                        "to_char(PRO_END,'yyyy/MM/dd HH24:mi:ss') PRO_END FROM IPLANT1.C_IPLANT_E2_T WHERE ET_CD = ?";
                Map<String, Object> map = template.queryForMap(comSql, etCd);
                long[] distanceTimes = DateUtils.getDistanceTimes(map.get("PRO_START").toString(), map.get("PRO_END").toString());
                long distanceTime = distanceTimes[3];
                String queSql = "UPDATE IPLANT1.C_IPLANT_E2_T SET CT=? WHERE ET_CD = ? ";
                template.update(queSql, distanceTime, etCd);
            }
        } catch (Exception e) {
            LOG.info(e.toString(), e);
        }


    }
}
