package cn.bp.scada.controller.sap;

import cn.bp.scada.mapper.scada.BatchMat;
import cn.bp.scada.sap.SapConnUtils;
import cn.bp.scada.sap.bean.ProductSn;
import cn.bp.scada.common.utils.dbhelper.DBHelper;
import cn.bp.scada.common.utils.PrimaryHelper;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.sap.conn.jco.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


@RestController
public class SapWoSNController extends JdbcDaoSupport {
    @Resource
    public void setJb(JdbcTemplate jb) {
        super.setJdbcTemplate(jb);
    }

    @Autowired
    private SapConnUtils sapUtils;
    @Autowired
    private BatchMat mat;

    @Resource
    private DBHelper db;

    @Resource
    private PrimaryHelper ph;
    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    //非改配订单获取SN接口
    @RequestMapping("/gd")
    public JSONPObject te(HttpServletRequest req, String data) throws JSONException {
        String name = req.getParameter("callbackparam");//获取到jsonp的函数名
        JSONObject json = new JSONObject();
        json.put("IN_O_NO", data);
        String string = null;

        //获取改配订单SN
        if (!data.substring(0, 5).equals("00006")) {//判断是否为改配订单
            string = getSapWoSnFunction(json);
        } else {
            return new JSONPObject(name, "此订单为改配订单，请检查！");
        }
        return new JSONPObject(name, string);
    }

    /**
     * 改配订单BOM获取
     *
     * @return
     */
    @RequestMapping("/sapUpdaOrderBom")
    public JSONPObject updaOrderBom11(HttpServletRequest req, String data) throws Exception {
        System.out.println("进入:" + Thread.currentThread().getName());
        String name = req.getParameter("callbackparam");//获取到jsonp的函数名
        JSONObject json = new JSONObject(data);
        String firstOrder = json.getString("I_AUFNRGPF");
        String endOrder = json.getString("I_AUFNRGPE");
        if (firstOrder.equals("") || endOrder.equals("")) {
            return new JSONPObject(name, "订单号不能为空，请检查！");
        }
        String checkOrder = "SELECT COUNT(1) from R_MES_MO_T WHERE O_NO=? ";
        Map<String, Object> map1 = this.getJdbcTemplate().queryForMap(checkOrder, firstOrder);
        if (Integer.parseInt(map1.get("COUNT(1)").toString()) == 0) {
            return new JSONPObject(name, "原始订单在系统中没有查到，请检查！");
        }
        if (!endOrder.substring(0, 5).equals("00006")) {
            return new JSONPObject(name, "改配订单号填写错误");
        }
        //同步产品sn
        String checkOrder2 = "SELECT COUNT(1) FROM PRODUCT_SN WHERE AUFNR=? ";
        Map<String, Object> map2 = this.getJdbcTemplate().queryForMap(checkOrder2, endOrder);
        if (Integer.parseInt(map2.get("COUNT(1)").toString()) > 0) {
            return new JSONPObject(name, "该订单SN已经存在在系统中，请检查！");
        }

        String string = null;
        JSONObject json1 = new JSONObject();
        json1.put("IN_O_NO", endOrder);
        string = getSapWoSnFunction(json1);
        int strLengh=0;
        if(string.length()>7){
            logger.info("同步SN获取到的返回结果："+strLengh+" 订单号为："+endOrder);
            strLengh=Integer.parseInt(string.substring(14));
        }
        if(strLengh==0){
            return new JSONPObject(name, "该订单SN没有从SAP中查询到/异常！");
        }
        try {
           /* JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象
            JCoFunction function = sapUtils.getFunction("ZRFC_ZPP97");//获取到函数

            //入参
            JCoParameterList parameterList = function.getImportParameterList();
            //设置入参
            parameterList.setValue("P_AUFNR", firstOrder); //订单号 (原订单10)
            parameterList.setValue("P_AUFNR2", endOrder); //订单号(现订单-改配的60)
		*//*		parameterList.setValue("P_SERNR", "3333"); //序列号1
				parameterList.setValue("P_SERNR2", "3333"); //序列号2*//*
            function.execute(jCoDestination);

            JCoTable table = function.getTableParameterList().getTable("LT_RETURN");//获取表
            System.out.println(table.getNumRows());
            String sql = "insert into MO_BOM_TEMP(O_NO,MATNR,MAKTX,MENGE1,MEINS1) values (?,?,?,?,?)";
            if (table.getNumRows() > 0) {
                for (int i = 0; i < table.getNumRows(); i++) {
                    table.setRow(i);
                    String matnr = table.getValue("MATNR").toString();
                    String maktx = table.getValue("MAKTX").toString();
                    String menge1 = table.getValue("MENGE1").toString();
                    String meins1 = table.getValue("MEINS1").toString();
                    try {
                        Map<String, Object> map = this.getJdbcTemplate().queryForMap(sql, endOrder, matnr, maktx, menge1, meins1);
                    } catch (DataAccessException e) {
                    }
                }
            }*/

            /* *
             * *获取入参，整合入参
             * **/
            String sql = "insert into MO_BOM_TEMP(O_NO,MATNR,MAKTX,MENGE1,MEINS1) values (?,?,?,?,?)";
            JCoDestination jCoDestination1 = sapUtils.jCoDestination;//接收连接对象
            JCoFunction function1 = sapUtils.getFunction("ZRFC_ZPP97");//获取到函数
            JCoParameterList parameterList1 = function1.getImportParameterList();
            //设置入参
            parameterList1.setValue("P_AUFNR", endOrder); //订单号 1
            parameterList1.setValue("P_AUFNR2", firstOrder); //订单号2
		     /*   parameterList1.setValue("P_SERNR", "3333"); //序列号1
				parameterList1.setValue("P_SERNR2", "3333"); //序列号2*/
            function1.execute(jCoDestination1);
            JCoTable table1 = function1.getTableParameterList().getTable("LT_RETURN");//获取表
            System.out.println(table1.getNumRows());
            if (table1.getNumRows() > 0) {
                String sql111 = "DELETE FROM MES1.MO_BOM_TEMP WHERE O_NO=?";
                this.getJdbcTemplate().update(sql111,endOrder);
                for (int i = 0; i < table1.getNumRows(); i++) {
                    table1.setRow(i);
                    String matnr1 = table1.getValue("MATNR").toString();
                    String maktx1 = table1.getValue("MAKTX").toString();
                    String menge11 = table1.getValue("MENGE1").toString();
                    String meins11 = table1.getValue("MEINS1").toString();
                    try {
                        Map<String, Object> map = this.getJdbcTemplate().queryForMap(sql, endOrder, matnr1, maktx1, menge11, meins11);
                    } catch (DataAccessException e) {
                    }
                }
            }
        } catch (JCoException e) {
            System.out.println(e.toString());
        }
        //先删除MO_BOM当前订单信息
        try {
        String sql11 = "DELETE FROM MES1.MO_BOM WHERE MO_NO=(SELECT MO_NO FROM MES1.R_MES_MO_T WHERE O_NO=?)";
        this.getJdbcTemplate().update(sql11,endOrder);

        String sql1 = "INSERT INTO  MES1.MO_BOM (MO_NO,  MATNR,  MAKTX, MENGE, IDNRK, IDKTX, IDNGE, FCT_CD) " +
                " SELECT T4.MO_NO, T4.ITEM_CD, T4.ITEM_NM, '1', T3.MATNR, T3.MAKTX, (T3.SUMQTY) AS SINGLEQTY, T4.FCT_CD " +
                "FROM ( SELECT  O_NO,MATNR,MAKTX,SUM(MENGE1) AS SUMQTY FROM (SELECT ? O_NO, T1.IDNRK MATNR," +
                "T1.IDKTX MAKTX,T1.IDNGE MENGE1  FROM MO_BOM T1 LEFT JOIN R_MES_MO_T T2 ON T1.MO_NO=T2.MO_NO " +
                "WHERE  T2.O_NO=? UNION ALL SELECT O_NO, MATNR ,MAKTX,MENGE1 FROM MO_BOM_TEMP " +
                "WHERE O_NO=? AND MATNR!='V01501P000000000') GROUP BY O_NO,MATNR,MAKTX ) T3 LEFT JOIN MES1.R_MES_MO_T T4 ON T3.O_NO = T4.O_NO ";

        this.getJdbcTemplate().update(sql1, endOrder, firstOrder, endOrder);


        //插入到diag信息
        String diagDelete = "delete from ListToMaterial where listId='" + endOrder + "'";
        db.excuteUpdate(diagDelete, null);
        String diagSql = "SELECT T2.O_NO,IDNRK,get_material_partid(IDKTX) AS MATERIAL_TYPE,IDNGE FROM MES1.MO_BOM T1 "
                + "LEFT JOIN MES1.R_MES_MO_T T2 ON T1.MO_NO=T2.MO_NO "
                + "WHERE T2.O_NO='" + endOrder + "'";
        List<Map<String, Object>> queryForList = this.getJdbcTemplate().queryForList(diagSql);
        List<Object> paramsTest = new ArrayList<>();
        String diagSql2 = "insert into ListToMaterial( materialId,type,listId,num )"
                + " values(?,?,?,?)";
        String diagSql1 = "insert into ListToMaterialCopy( materialId,type,listId,num )"
                + " values(?,?,?,?)";
        for (int j = 0; j < queryForList.size(); j++) {
            String idnge = queryForList.get(j).get("IDNGE").toString();
            if (idnge.length() > 5) { idnge = idnge.substring(0, 5); }
            if (Float.parseFloat(idnge) > 0) {
                paramsTest.add(queryForList.get(j).get("IDNRK"));
                paramsTest.add(queryForList.get(j).get("MATERIAL_TYPE"));
                paramsTest.add(Integer.parseInt(queryForList.get(j).get("O_NO").toString()));
                paramsTest.add(idnge);
                db.excuteUpdate(diagSql2, paramsTest);
                db.excuteUpdate(diagSql1, paramsTest);
            }
            paramsTest.clear();
        }
        String checkSql = "SELECT * FROM ListToMaterial WHERE listId='" + Integer.parseInt(queryForList.get(0).get("O_NO").toString()) + "'";
        String checkSql1 = "SELECT * FROM ListToMaterialCopy WHERE listId='" + Integer.parseInt(queryForList.get(0).get("O_NO").toString()) + "'";
        List checkList = db.executeQueryTest(checkSql, null, null);
        List checkList1 = db.executeQueryTest(checkSql1, null, null);
        logger.info("插入到老化的数据为：" + checkList);
        logger.info("插入到老化的数据为：" + checkList1);
        } catch (DataAccessException e) {
            return new JSONPObject(name, "接口异常，请检查！");
        }
        return new JSONPObject(name, string);
    }

    //改配订单获取数据接口
    @RequestMapping("/bomGainGP")
    public JSONPObject bomGainGP(HttpServletRequest req, String data) throws JSONException {
        String name = req.getParameter("callbackparam");//获取到jsonp的函数名
        JSONObject json = new JSONObject();
        json.put("IN_O_NO", data);
        String string = null;
        //判断是否为改配订单
        if (data.substring(0, 5).equals("00006")) {
            string = getSapWoSnFunction(json);
            return new JSONPObject(name, string);
        } else {
            return new JSONPObject(name, "此订单为非改配订单，请检查！");
        }
    }

    @RequestMapping("/gds")
    public String tes(String data) throws JSONException {

        JSONObject json = new JSONObject();
        json.put("IN_O_NO", data);
        String string = getSapWoSnFunction(json);
        return string;
    }

    /**
     * 获取工单SN
     *
     * @return
     * @throws JSONException
     */
    public String getSapWoSnFunction(JSONObject reqDataDetail) throws JSONException {
        String result = "产品SN已存在";
        try {
            JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象
            JCoFunction function = sapUtils.getFunction("ZPP_GET_SERNR");//获取到函数
            //入参
            JCoParameterList parameterList = function.getImportParameterList();
            //设置入参
            parameterList.setValue("I_AUFNR", reqDataDetail.get("IN_O_NO")); //生产单号

            function.execute(jCoDestination);
            Map<String, String> m = new HashMap<>();
            m.put("I_AUFNR", reqDataDetail.get("IN_O_NO").toString());
            //出参
            JCoTable table1 = function.getTableParameterList().getTable("TBL_SERNR");//获取序列号和订单号
            //判断产品sn是否存在
            //List<String> seleExistsSn = mat.seleExistsSn(m);
            String exisSql = "select AUFNR from PRODUCT_SN n where exists (select aufnr from PRODUCT_SN t where t.aufnr=?)";
            List<Map<String, Object>> seleExistsSn = this.getJdbcTemplate().queryForList(exisSql, reqDataDetail.get("IN_O_NO"));
            if (seleExistsSn.size() < 1) {//不存在则增加

                List<ProductSn> list = new ArrayList<ProductSn>();
                String sql = "insert into PRODUCT_SN(AUFNR,SERNR,CRT_ID,CRT_DT,USE_STA) values(?,?,'admin',sysdate,'N')";
                for (int i = 0; i < table1.getNumRows(); i++) {
                    table1.setRow(i);
                    ProductSn pSn = new ProductSn();
                    pSn.setAufnr(table1.getString("AUFNR"));//订单号
                    pSn.setSernr(Integer.parseInt(table1.getString("SERNR")) + "");////序列号(产品sn)
                    list.add(pSn);
                }

                LOG.info(list.size() + " 条产品SN数据,订单号为" + reqDataDetail.get("IN_O_NO").toString());
                boolean flag = false;
                if (list.size() > 0) {
                    insertBatchProdcSn(sql, list);
                    LOG.info("产品SN插入本地数据库成功，时间为：" + ph.getDateTime());

                    String testSql = "insert into ListToSN(listId,snNumber) values(?,?)";
                    flag = db.addNewsPaper(testSql, list);
                    if (flag) {
                        LOG.info("产品SN插入测试LISTtoSN成功,时间为：" + ph.getDateTime());
                    } else {
                        LOG.info("产品SN插入测试LISTtoSN失败,时间为：" + ph.getDateTime());
                    }
                }


                result = "导入产品SN成功,数据条为:" + list.size();
            }
        } catch (JCoException e) {
            System.out.println("产品SN异常");
            System.out.println(e.toString());
        }
        System.out.println(result);
        return result;
    }

    public void insertBatchProdcSn(String sql, final List<ProductSn> list) {
        this.getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {
            {
            }

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                // TODO Auto-generated method stubs
                ps.setString(1, list.get(i).getAufnr());
                ps.setString(2, list.get(i).getSernr());
                if (i % 5000 == 0 && i != 0) {
                    ps.executeBatch();
                }
            }

            @Override
            public int getBatchSize() {
                // TODO Auto-generated method stub
                return list.size();
            }
        });
    }
}

