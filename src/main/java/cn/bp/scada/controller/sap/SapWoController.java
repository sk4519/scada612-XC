package cn.bp.scada.controller.sap;


import cn.bp.scada.sap.SapConnUtils;
import cn.bp.scada.sap.bean.WorkOrder;
import cn.bp.scada.sap.bean.WorkOrderBom;
import cn.bp.scada.sap.bean.WorkOrderItem;
import cn.bp.scada.common.utils.dbhelper.DBHelper;
import cn.bp.scada.common.utils.MesApiUtils;
import cn.bp.scada.common.utils.PrimaryHelper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.sap.conn.jco.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class SapWoController extends JdbcDaoSupport {
    @Resource
    public void setJb(JdbcTemplate jb) {
        super.setJdbcTemplate(jb);
    }

    @Autowired
    private SapConnUtils sapUtils;
    @Resource
    private MesApiUtils mesApiUtils;
    @Resource
    private SapWoSNController sct;
    @Resource
    private PrimaryHelper ph;
    @Autowired
    private SapMaterialController srt;
    @Autowired
    SapMaintProdcMsg smg;
    @Resource
    private DBHelper db;

    private Logger LOG = LoggerFactory.getLogger(this.getClass());
    public static String regex = "([A-Z][0-9]{4}_[0-9]{2}_[0-9]{2}_[0-9]{2})";
    private Lock lock = new ReentrantLock();
    private int task = 0;
    private String frTime = "";
    private String workNo = "";

    public static String getMatcher(String regex, String source) {
        String result = "";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }

    /**
     * ERP??????
     *
     * @param req
     * @param data ??????????????????????????????????????????
     * @return
     */
    @RequestMapping("/changeErp")
    public JSONPObject changeWo(HttpServletRequest req, String data) {
        String name = req.getParameter("callbackparam");//?????????jsonp????????????
        JSONObject json = new JSONObject(data);
        String sapWoFunction = "";
        try {

            workNo = json.getString("I_AUFNR_FRM");
            String o_no = json.getString("I_AUFNR_FRM");
            String times = json.getString("I_AUFNR_TO");
            if (o_no.equals("") || times.equals("")) {
                return new JSONPObject(name, "?????????????????????");
            }

            //??????????????????????????????
            String queryMo = "SELECT MO_NO,MO_STATE FROM MES1.R_MES_MO_T WHERE  O_NO=?";
            Map<String, Object> moMap = this.getJdbcTemplate().queryForMap(queryMo, o_no);
            Object mo_no = moMap.get("MO_NO");
            String moSta = moMap.get("MO_STATE").toString();
            if (moSta.equals("1")) {
                queryMo = "DELETE FROM MES1.MO_BOM WHERE MO_NO=?";
                this.getJdbcTemplate().update(queryMo, mo_no);
                queryMo = "DELETE FROM WO_T_ITEM WHERE AUFNR=?";
                this.getJdbcTemplate().update(queryMo, o_no);
                queryMo = "DELETE FROM MES1.R_MES_MO_T  WHERE  O_NO=?";
                this.getJdbcTemplate().update(queryMo, o_no);
                queryMo = "DELETE FROM R_MES_WO_T WHERE MO_NO=?";
                this.getJdbcTemplate().update(queryMo, mo_no);
                queryMo = "DELETE FROM PRODUCT_SN WHERE AUFNR=?";
                this.getJdbcTemplate().update(queryMo, o_no);

                frTime = json.getString("I_AUFNR_TO");
                task = 1;
                sapWoFunction = getSapWoFunction();
                task = 0;
            } else {
                sapWoFunction = "??????????????????????????????????????????????????????!";
            }
        } catch (Exception e) {
            sapWoFunction = "???????????????????????????";
        }
        return new JSONPObject(name, sapWoFunction);
    }

    /**
     * ERP??????
     *
     * @param req
     * @param data ??????????????????????????????????????????
     * @return
     */
    @RequestMapping("/changeErpHandle")
    //@Scheduled(fixedDelay = 300000)
    public void changeWoHandle() {
        task = 1;
        //JSONObject json = new JSONObject(data);
        frTime = "20210126";
        workNo = "000010481208";
        String sapWoFunction = "";
        try {

            sapWoFunction = getSapWoFunction();
        } catch (Exception e) {
        }
    }

    /**
     * ????????????&????????????&bom??????
     *
     * @return
     * @throws JSONException
     */
    @Scheduled(fixedDelay = 300000) //?????????????????????????????????5??????
    public String getSapWoFunction() throws JSONException {
        LOG.info("???????????????????????????" + ph.getDateTime());
        String re = "????????????????????????????????????????????????????????????????????????";
        lock.lock();
        try {
            JCoDestination jCoDestination = sapUtils.jCoDestination;//??????????????????

            JCoFunction function = sapUtils.getFunction("ZPP_GET_WO_DETAIL");//???????????????

            //??????
            JCoParameterList parameterList = function.getImportParameterList();
            //????????????
            String sqlMax = "select max(t.o_no) orders from  MES1.R_MES_MO_T t";
            Map<String, Object> queryForMap = this.getJdbcTemplate().queryForMap(sqlMax);
            Object object = queryForMap.get("orders");

            int st = 0;
            String start = "";
            int or = 0;
            String over = "";
            if (object == null) {

            } else {
                try {
                    st = Integer.parseInt(object.toString()) + 1;
                    start = "0000" + st; //???????????????
                    over = "999999999999"; // ???????????????
                } catch (Exception e) {

                }
            }

            String startDown = ph.getDates();
            String overDown = ph.getDates();

            parameterList.setValue("I_WERKS_FRM", "8100");//????????????
            parameterList.setValue("I_WERKS_TO", "8100");//????????????
            if (task == 1) {
                parameterList.setValue("I_PDAT1_FRM", frTime);//??????????????????
                parameterList.setValue("I_AUFNR_FRM", workNo);//???????????????
                parameterList.setValue("I_AUFNR_TO", workNo);//???????????????
            } else {
                parameterList.setValue("I_PDAT1_FRM", startDown);//??????????????????
                parameterList.setValue("I_AUFNR_FRM", "");//???????????????
                parameterList.setValue("I_AUFNR_TO", "");//???????????????
            }

            parameterList.setValue("I_PDAT1_TO", overDown);//??????????????????
            parameterList.setValue("I_STAT", "");//?????????????????????
            function.execute(jCoDestination);
            parameterList.clear();

            //??????
            JCoParameterList list1 = function.getExportParameterList();
            System.out.println("??????:  " + list1.getString(0));
            System.out.println("??????:  " + list1.getString(1));

            final List<WorkOrder> list = new ArrayList<WorkOrder>();
            final List<WorkOrderItem> listItem = new ArrayList<WorkOrderItem>();
            final List<WorkOrderBom> listBom = new ArrayList<WorkOrderBom>();

            //????????????
            String workSql = "insert into WO_T( o_no, fct_code, mt_tnr, mt_ds, m_pt_maktx, m_pt_matnr, pc_md, o_num_sum, sp_code, ftrmi, ftrmi_tm, status, auart,in_dt"
                    + ",V_PT_MATNR,V_SERNR,V_TDLINE,V_MEMO,RKRQ,RKRQ_TM,TECH_INST,PROJ_NAME,EMERGENCY,DISPO,"
                    + "ERNAM,PZMS,WRKST,BISMT,ZXGHSJ,VKBUR,GSTRS,GSUZS,GLTRS,GLUZS,ARBPL,AUFNR_A,CPLB,MO_CD)"
                    + " values(?,?,?,?,?,?,?,?,?,?,?,?,?,sysdate,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            //??????????????????????????????????????????????????????sap??????
            JCoTable table = function.getTableParameterList().getTable("TBL_HEAD");//?????????  TBL_HEAD????????????
            WorkOrder m = null;
            String existsWoSql = "SELECT O_NO FROM MES1.R_MES_MO_T";
            List<Map<String, Object>> queryExistsWo = this.getJdbcTemplate().queryForList(existsWoSql);
            boolean flag = false;
            for (int i = 0; i < table.getNumRows(); i++) {
                table.setRow(i);
                for (int j = 0; j < queryExistsWo.size(); j++) {
                    if (table.getString("AUFNR").equals(queryExistsWo.get(j).get("O_NO"))) {
                        flag = true;
                    }

                }
                if (!flag) { //????????????????????????
                    m = new WorkOrder();

                    m.setO_no(table.getString("AUFNR")); //?????????
                    m.setFct_code(table.getString("WERKS")); //??????
                    m.setMt_tnr(table.getString("PLNBEZ")); //????????????
                    m.setMt_ds(table.getString("MAKTX")); //????????????
                    m.setM_pt_maktx(table.getString("V_PT_MAKTX")); //??????????????????(????????????)
                    m.setM_pt_matnr(table.getString("V_PT_MATNR")); //?????????????????????
                    m.setPc_md(table.getString("V_PT_MAXH")); //????????????
                    m.setO_num_sum((table.getString("GAMNG"))); //??????????????????
                    m.setSp_code(table.getString("V_PRODH")); //????????????
                    m.setAuart(table.getString("AUART")); //????????????
                    m.setFtrmi(table.getString("FTRMI")); //??????????????????
                    m.setFtrmi_tm(table.getString("FTRMI_TM")); //????????????
                    m.setStatus(table.getString("STATUS")); //????????????
                    m.setV_pt_matnr(table.getString("V_PT_MATNR"));
                    m.setV_sernr(table.getString("V_SERNR"));
                    m.setV_tdline(table.getString("V_TDLINE")); //????????????
                    m.setV_memo(table.getString("V_MEMO")); //?????????
                    m.setRkrq(table.getString("RKRQ"));
                    m.setRkrq_tm(table.getString("RKRQ_TM"));
                    m.setTech_inst(table.getString("TECH_INST")); //???????????????
                    m.setProj_name(table.getString("PROJ_NAME"));
                    m.setEmergency(table.getString("EMERGENCY"));
                    m.setDispo(table.getString("DISPO"));
                    m.setErnam(table.getString("ERNAM"));
                    m.setPzms(table.getString("PZMS")); //????????????
                    m.setWrkst(table.getString("WRKST"));
                    m.setBismt(table.getString("BISMT"));
                    m.setZxghsj(table.getString("ZXGHSJ"));
                    m.setVkbur(table.getString("VKBUR"));
                    m.setGstrs(table.getString("GSTRS"));
                    m.setGsuzs(table.getString("GSUZS"));
                    m.setGltrs(table.getString("GLTRS"));
                    m.setGluzs(table.getString("GLUZS"));
                    m.setArbpl(table.getString("ARBPL"));
                    m.setAufnr_a(table.getString("AUFNR_A"));
                    m.setCplb(table.getString("CPLB"));
                    list.add(m);
                }
                flag = false;
            }

            LOG.info(list.size() + " ???????????????: --");
            LOG.info("ERP????????????????????????" + list);
            //??????????????????
            String testsql = "insert into List(listId ,num ,productName , productConfiguration ,other ,command )"
                    + "values(?,?,?,?,?,?)";
            List<Object> params = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                re = "????????????";
                int o_no = Integer.parseInt(list.get(i).getO_no());
                //??????????????????list???????????????
                String deleToList = "delete  from List where listId = " + o_no + " ";
                db.excuteUpdate(deleToList, null);

                //??????????????????listToMaterial???????????????
                String deleToMater = "delete  from ListToMaterial where listId =" + o_no + " ";
                db.excuteUpdate(deleToMater, null);

                String matcher = getMatcher(regex, list.get(i).getV_memo());

                params.add(Integer.parseInt(list.get(i).getO_no())); //?????????
                params.add(list.get(i).getO_num_sum()); //???????????????
                params.add(list.get(i).getM_pt_maktx()); //??????????????????
                params.add(list.get(i).getV_tdline()); //??????
                params.add(list.get(i).getV_memo()); //?????????
                params.add(matcher); //??????
                db.excuteUpdate(testsql, params);

                LOG.info("??????????????????List??????params????????????" + params);
                params.clear();
            }


            //??????bom
            String wkItemsql = "insert into WO_T_ITEM(aufnr,v_sj_matnr, v_bdmng,v_sj_maktx,dumps, in_dt,v_lgort,v_charg,"
                    + "status,proj_info,matkl,wgbez,pn,ITEM_CD)"
                    + "values(?,?,?,?,?,sysdate,?,?,?,?,?,?,?,?)";
            JCoTable tableItem = function.getTableParameterList().getTable("TBL_DETAIL");//?????????  TBL_DETAIL ????????????
            boolean itemFlag = false;
            for (int i = 0; i < tableItem.getNumRows(); i++) {
                tableItem.setRow(i);
                for (int j = 0; j < queryExistsWo.size(); j++) {
                    if (tableItem.getString("AUFNR").equals(queryExistsWo.get(j).get("O_NO"))) {
                        itemFlag = true;
                    }

                }
                if (!itemFlag) {
                    WorkOrderItem item = new WorkOrderItem();

                    item.setAufnr(tableItem.getString("AUFNR"));
                    item.setV_sj_matnr(tableItem.getString("V_SJ_MATNR"));
                    item.setV_bdmng(tableItem.getString("V_BDMNG"));
                    item.setV_sj_maktx(tableItem.getString("V_SJ_MAKTX"));
                    item.setDumps(tableItem.getString("DUMPS"));
                    item.setV_lgort(tableItem.getString("V_LGORT"));
                    item.setV_charg(tableItem.getString("V_CHARG"));
                    item.setStatus(tableItem.getString("STATUS"));
                    item.setProj_info(tableItem.getString("PROJ_INFO"));
                    item.setMatkl(tableItem.getString("MATKL"));
                    item.setWgbez(tableItem.getString("WGBEZ"));
                    item.setPn(tableItem.getString("PN"));
                    listItem.add(item);
                }
                itemFlag = false;
            }

            LOG.info("??? " + listItem.size() + " ?????????bom??????:WO_T_ITEM ??? --");
            LOG.info("ERP??????WO_T_ITEM???????????????" + listItem);
            String bomSql = "insert into wo_t_bom(matnr, maktx, menge, posnr, postp, idnrk, idktx, "
                    + "idnge, meins, notes, pn, in_dt,id)"
                    + "values(?,?,?,?,?,?,?,?,?,?,?,sysdate,?)";
            JCoTable tableBom = function.getTableParameterList().getTable("TBL_DETAIL_OM");//?????????  TBL_DETAIL_OM ??????bom
            for (int i = 0; i < tableBom.getNumRows(); i++) {
                WorkOrderBom bom = new WorkOrderBom();
                tableBom.setRow(i);
                bom.setMatnr(tableBom.getString("MATNR"));
                bom.setMaktx(tableBom.getString("MAKTX"));
                bom.setMenge(tableBom.getString("MENGE"));
                bom.setPosnr(tableBom.getString("POSNR"));
                bom.setPostp(tableBom.getString("POSTP"));
                bom.setIdnrk(tableBom.getString("IDNRK"));
                bom.setIdktx(tableBom.getString("IDKTX"));
                bom.setIdnge(tableBom.getString("IDNGE"));
                bom.setMeins(tableBom.getString("MEINS"));
                bom.setNotes(tableBom.getString("NOTES"));
                bom.setPn(tableBom.getString("PN"));
                listBom.add(bom);
            }
            //System.out.println(listBom.size()+" ???bom????????????: --");


            //????????????bom
			/*String deleDetailSql= "delete from WO_T_ITEM";
			saveOrUpdate(deleDetailSql);*/

            //??????bom?????????
			/*String dele= "delete from WO_T_bom";
			saveOrUpdate(dele);*/

            insertBatchWo(workSql, list);//???????????????????????????(?????????)
            insertBatchWoItem(wkItemsql, listItem);//??????????????????bom???
            //??????????????????????????????MES?????????
            String addSql = "insert into MES1.R_MES_MO_T(FCT_CD,ITEM_CD,ITEM_NM,ITEM_TYPE,plan_strt_dt,PLAN_END_DT,"
                    + "CRT_ID,CRT_DT,MO_NO,MO_STATE,VERSION,ITEM_CD_VERSION,WC_CD,O_NO,PLAN_PO_QTY,USR_DFN_1,USR_DFN_2,USR_DFN_3,USR_DFN_4)"
                    + "select m.fct_code,nvl(tnr,0)	,m.mt_ds,'BQL', da,de,'admin',IN_DT,"
                    + " m.mo_cd,'1','1.0',tnr,'WS04',O_NO,O_NUM_SUM,M_PT_MAKTX,V_MEMO,V_TDLINE,PZMS from (select t.fct_code,nvl(t.mt_tnr,0)"
                    + ",t.mt_ds,'BQL', to_date(t.gstrs||' '||t.GSUZS,'yyyy-mm-dd hh24:mi:ss') da,"
                    + "to_date(t.gltrs||' '||t.GLUZS,'yyyy-mm-dd hh24:mi:ss') de,'admin',IN_DT,"
                    + "t.mo_cd,'1','1.0',mt_tnr tnr,'WS04',O_NO,O_NUM_SUM,M_PT_MAKTX,V_MEMO,V_TDLINE,PZMS from WO_T t  group by "
                    + "fct_code,t.mt_ds,'BQL', to_date(t.gstrs||' '||t.GSUZS,'yyyy-mm-dd hh24:mi:ss'),"
                    + "to_date(t.gltrs||' '||t.GLUZS,'yyyy-mm-dd hh24:mi:ss'),'admin',IN_DT,"
                    + "t.mo_cd,'1','1.0',mt_tnr,'WS04',O_NO,O_NUM_SUM,M_PT_MAKTX,V_MEMO,V_TDLINE,PZMS) m "
                    + "where not EXISTS(select O_NO from  MES1.R_MES_MO_T where m.O_NO=O_NO)";
            saveOrUpdate(addSql);

            //??????sap???????????????
            String deleSql = "delete from WO_T";
            saveOrUpdate(deleSql);
            try {
                /*   if (!table.getString("AUFNR").substring(0, 5).equals("00006")) {*/
                    JSONObject reqDataDetail = new JSONObject();
                    reqDataDetail.put("IFS", "W0000055");
                    String upMoSql = "UPDATE MES1.R_MES_MO_T SET USR_DFN_2=?,USR_DFN_3=?,USR_DFN_4=? WHERE O_NO=?";
                    for (int i = 0; i < list.size(); i++) {
                        reqDataDetail.put("IN_O_NO", list.get(i).getO_no());
                        mesApiUtils.doPost(reqDataDetail); //??????????????????????????????bom???????????????
                        //??????????????????????????????????????????SN??????
                        LOG.info("????????????SN??????????????????" + list.get(i).getO_no());
                        sct.getSapWoSnFunction(reqDataDetail);
                        this.getJdbcTemplate().update(upMoSql, list.get(i).getV_memo(), list.get(i).getV_tdline(), list.get(i).getPzms(), list.get(i).getO_no());
                    }
                    List<Object> paramsTest = new ArrayList<>();
			/*	WorkOrder wd = new WorkOrder();
				wd.setO_no("000010478252");
				list.add(wd);*/

                    //?????????diag??????
                    for (int i = 0; i < list.size(); i++) {
                        String matSql = "SELECT T2.O_NO,IDNRK,get_material_partid(IDKTX) AS MATERIAL_TYPE,IDNGE FROM MES1.MO_BOM T1 "
                                + "LEFT JOIN MES1.R_MES_MO_T T2 ON T1.MO_NO=T2.MO_NO "
                                + "WHERE T2.O_NO='" + list.get(i).getO_no() + "'";
                        List<Map<String, Object>> queryForList = this.getJdbcTemplate().queryForList(matSql);
                        for (int j = 0; j < queryForList.size(); j++) {
                            String sql = "insert into ListToMaterial( materialId,type,listId,num )"
                                    + " values(?,?,?,?)";
                            String sql1 = "insert into ListToMaterialCopy( materialId,type,listId,num )"
                                    + " values(?,?,?,?)";
                            paramsTest.add(queryForList.get(j).get("IDNRK"));
                            paramsTest.add(queryForList.get(j).get("MATERIAL_TYPE"));
                            paramsTest.add(Integer.parseInt(queryForList.get(j).get("O_NO").toString()));
                            paramsTest.add(queryForList.get(j).get("IDNGE"));
                            db.excuteUpdate(sql, paramsTest);
                            db.excuteUpdate(sql1, paramsTest);
                            paramsTest.clear();
                        }
                        String checkSql = "SELECT * FROM ListToMaterial WHERE listId='" + Integer.parseInt(queryForList.get(0).get("O_NO").toString()) + "'";
                        String checkSql1 = "SELECT * FROM ListToMaterialCopy WHERE listId='" + Integer.parseInt(queryForList.get(0).get("O_NO").toString()) + "'";
                        List checkList = db.executeQueryTest(checkSql, null, null);
                        List checkList1 = db.executeQueryTest(checkSql1, null, null);
                        logger.info("??????????????????????????????" + checkList);
                        logger.info("??????????????????????????????" + checkList1);
                    }
               /* }*/
            } catch (IOException e) {
                // TODO Auto-generated catch block
                LOG.info(e.toString(), e);
            }
            String dateTimes = ph.getDateTimes();
            String sqls = "UPDATE IPLANT1.C_IPLANT_E2_T SET DEV_END=?";
            this.getJdbcTemplate().update(sqls, dateTimes);

            //insertBatchWoBom(bomSql,listBom);//????????????bom?????????

        } catch (Exception e) {
            LOG.info(e.toString(), e);
            re = "??????????????????????????????";
        } finally {
            lock.unlock();
        }

        return re;
    }

    public void insertBatchWo(String sql, final List<WorkOrder> list) {
        this.getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {
            String MoSql = "SELECT MES1.MO_RULES_FUN('MO') tt from dual"; //???????????????????????????

            {
            }

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                // TODO Auto-generated method stub
                Map<String, Object> queryForMap = getJdbcTemplate().queryForMap(MoSql);
                Object object = queryForMap.get("tt");//???????????????????????????
                ps.setString(1, list.get(i).getO_no());
                ps.setString(2, list.get(i).getFct_code());
                ps.setString(3, list.get(i).getMt_tnr());
                ps.setString(4, list.get(i).getMt_ds());
                ps.setString(5, list.get(i).getM_pt_maktx());
                ps.setString(6, list.get(i).getM_pt_matnr());
                ps.setString(7, list.get(i).getPc_md());
                ps.setString(8, list.get(i).getO_num_sum());
                ps.setString(9, list.get(i).getSp_code());
                ps.setString(10, list.get(i).getFtrmi());
                ps.setString(11, list.get(i).getFtrmi_tm());
                ps.setString(12, list.get(i).getStatus());
                ps.setString(13, list.get(i).getAuart());
                ps.setString(14, list.get(i).getV_pt_matnr());
                ps.setString(15, list.get(i).getV_sernr());
                ps.setString(16, list.get(i).getV_tdline());
                ps.setString(17, list.get(i).getV_memo());
                ps.setString(18, list.get(i).getRkrq());
                ps.setString(19, list.get(i).getRkrq_tm());
                ps.setString(20, list.get(i).getTech_inst());
                ps.setString(21, list.get(i).getProj_name());
                ps.setString(22, list.get(i).getEmergency());
                ps.setString(23, list.get(i).getDispo());
                ps.setString(24, list.get(i).getErnam());
                ps.setString(25, list.get(i).getPzms());
                ps.setString(26, list.get(i).getWrkst());
                ps.setString(27, list.get(i).getBismt());
                ps.setString(28, list.get(i).getZxghsj());
                ps.setString(29, list.get(i).getVkbur());
                ps.setString(30, list.get(i).getGstrs());
                ps.setString(31, list.get(i).getGsuzs());
                ps.setString(32, list.get(i).getGltrs());
                ps.setString(33, list.get(i).getGluzs());
                ps.setString(34, list.get(i).getArbpl());
                ps.setString(35, list.get(i).getAufnr_a());
                ps.setString(36, list.get(i).getCplb());
                ps.setObject(37, object);
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

    public void insertBatchWoItem(String sql, final List<WorkOrderItem> listItem) {
        this.getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {
            String squSql = "select sap_woItemSqu.NEXTVAL tt from dual";

            {
            }

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                // TODO Auto-generated method stub
                Map<String, Object> queryForMap = getJdbcTemplate().queryForMap(squSql);
                Object object = queryForMap.get("tt");//??????????????????id
                ps.setString(1, listItem.get(i).getAufnr());
                ps.setString(2, listItem.get(i).getV_sj_matnr());
                ps.setString(3, listItem.get(i).getV_bdmng());
                ps.setString(4, listItem.get(i).getV_sj_maktx());
                ps.setString(5, listItem.get(i).getDumps());
                ps.setString(6, listItem.get(i).getV_lgort());
                ps.setString(7, listItem.get(i).getV_charg());
                ps.setString(8, listItem.get(i).getStatus());
                ps.setString(9, listItem.get(i).getProj_info());
                ps.setString(10, listItem.get(i).getMatkl());
                ps.setString(11, listItem.get(i).getWgbez());
                ps.setString(12, listItem.get(i).getPn());
                ps.setObject(13, object);
                if (i % 5000 == 0 && i != 0) {
                    ps.executeBatch();
                }
            }

            @Override
            public int getBatchSize() {
                // TODO Auto-generated method stub
                return listItem.size();
            }
        });
    }

    public void insertBatchWoBom(String sql, final List<WorkOrderBom> listBom) {
        this.getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {
            String squSql = "select sap_woBom.NEXTVAL tt from dual";

            {
            }

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                // TODO Auto-generated method stub

                Map<String, Object> queryForMap = getJdbcTemplate().queryForMap(squSql);
                Object object = queryForMap.get("tt");//??????????????????id
                ps.setString(1, listBom.get(i).getMatnr());
                ps.setString(2, listBom.get(i).getMaktx());
                ps.setString(3, listBom.get(i).getMenge());
                ps.setString(4, listBom.get(i).getPosnr());
                ps.setString(5, listBom.get(i).getPostp());
                ps.setString(6, listBom.get(i).getIdnrk());
                ps.setString(7, listBom.get(i).getIdktx());
                ps.setString(8, listBom.get(i).getIdnge());
                ps.setString(9, listBom.get(i).getMeins());
                ps.setString(10, listBom.get(i).getNotes());
                ps.setString(11, listBom.get(i).getPn());
                ps.setObject(12, object);
                if (i % 5000 == 0 && i != 0) {
                    ps.executeBatch();
                }
            }

            @Override
            public int getBatchSize() {
                // TODO Auto-generated method stub
                return listBom.size();
            }
        });
    }

    public void saveOrUpdate(String sql) {//????????????????????????MES????????????????????????????????????
        this.getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {

            {
            }

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                // TODO Auto-generated method stub
                if (i % 5000 == 0 && i != 0) {
                    ps.executeBatch();
                }
            }

            @Override
            public int getBatchSize() {
                return 1;
            }
        });
    }

}

