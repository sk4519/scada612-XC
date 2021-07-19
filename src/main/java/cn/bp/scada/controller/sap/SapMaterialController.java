package cn.bp.scada.controller.sap;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.annotation.Resource;

import cn.bp.scada.common.utils.data.DateUtils;
import cn.bp.scada.mapper.scada.BatchMat;
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
import org.springframework.web.bind.annotation.RestController;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoTable;
import cn.bp.scada.sap.SapConnUtils;
import cn.bp.scada.sap.bean.Material;
import cn.bp.scada.common.utils.MesApiUtils;
import cn.bp.scada.common.utils.PrimaryHelper;

@RestController
public class SapMaterialController extends JdbcDaoSupport {
    @Resource
    public void setJb(JdbcTemplate jb) {
        super.setJdbcTemplate(jb);
    }

    @Resource
    private SapConnUtils sapUtils;
    @Resource
    private MesApiUtils mesApiUtils;
    @Resource
    private PrimaryHelper ph;
    @Autowired
    private BatchMat mat;

    JSONObject json = new JSONObject();
    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    /**
     * 物料主数据
     *
     * @param
     * @param
     * @return
     * @throws JSONException
     */
    @Scheduled(fixedDelay = 300000) //定时任务，间隔执行，每5分钟
    @Async
    public String getSapMatFunction() throws JSONException {

        String result = "导入失败,物料已存在";
        LOG.info("进入物料主数据");

        try {
            Date dat = new Date();
            SimpleDateFormat sim = new SimpleDateFormat("yyyyMMdd");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dat);
            calendar.add(calendar.DATE, -15);
            String format = sim.format(calendar.getTime());

            JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象
            JCoFunction function = sapUtils.getFunction("ZMM_MATERAIL_DETAIL");//获取到函数
            //入参
            JCoParameterList parameterList = function.getImportParameterList();
            //设置入参
            parameterList.setValue("B_WERKS", "8100");//起始工厂
            parameterList.setValue("E_WERKS", "8100");//截止工厂
            parameterList.setValue("B_MATNR", "");//起始物料编号
            parameterList.setValue("E_MATNR", "");//结束物料编号
            parameterList.setValue("B_ERSDA", format);//起始创建日期
            parameterList.setValue("E_ERSDA", ph.getDates());//结束创建日期
            parameterList.setValue("B_LAEDA", "");//起始最后一程修改日期
            parameterList.setValue("E_LAEDA", "");//结束最后一程修改日期
            //执行函数
            function.execute(jCoDestination);
            parameterList.clear();

            String sql = "insert into material_main(fct_code,mt_matkl,MT_WGBEZ,IT_TP,MT_TNR,MT_DS,MT_MST,MT_MST_DS,MEINS,IN_DT,MT_CD)"
                    + " values(?,?,?,?,?,?,?,?,?,sysdate,?)";//插入数据到中间表
            //如果入参是内表形式，就以如下代码传入sap系统
            JCoTable table = function.getTableParameterList().getTable("OUTPUT");//获取表

            final List<Material> list = new ArrayList<Material>();

            System.out.println("+++++++");
            for (int i = 0; i < table.getNumRows(); i++) {
                Material m = new Material();
                table.setRow(i);
                m.setFct_code(table.getString("WERKS"));//工厂
                m.setMt_matkl(table.getString("MATKL"));//物料组
                m.setMT_WGBEZ(table.getString("WGBEZ"));//物料组描述
                m.setIT_TP(table.getString("MTART"));//物料类型
                m.setMT_TNR(table.getString("MATNR"));//物料编号
                m.setMT_DS(table.getString("MAKTX"));//物料描述
                m.setMT_MST(table.getString("MMSTA"));//物料状态 MT_MST_DS
                m.setMT_MST_DS(table.getString("MTSTB"));//物料状态描述
                m.setMEINS(table.getString("MEINS"));//基本计量单位
                list.add(m);

            }
            System.out.println(list.size() + "条物料主数据: --");
            insertMtMain(sql, list);//批量插入物料主表（中间表）


            //将中间表的数据插入到MES物料表
            String addSql = " INSERT INTO MES1.R_MES_ITEM_T (FCT_CD,ITEM_CD,ITEM_NM,ITEM_TYPE,USE_YN,ITEM_GRP_CD,ITEM_GRP_NM,MO,crt_id,crt_dt)"
                    + " select m.fct_code,m.mt_tnr,m.mt_ds,m.it_tp,'Y',m.mt_matkl,m.mt_wgbez,'trt','admin',sysdate from"
                    + "(SELECT fct_code,MT_TNR,MT_DS,it_tp,mt_matkl,mt_wgbez FROM MES1.material_main GROUP "
                    + "BY fct_code,MT_TNR,MT_DS,it_tp,mt_matkl,mt_wgbez) "
                    + "m  WHERE not EXISTS(SELECT ITEM_CD FROM MES1.R_MES_ITEM_T T2 WHERE m.MT_TNR=T2.ITEM_CD)";

            saveOrUpdate(addSql);
            result = "共" + list.size() + "条物料主数据";

            //}
            //清空中间表
            String deleSql = "delete from material_main";
            saveOrUpdate(deleSql);

            try {
                json.put("IFS", "W0000056"); //物料组
                mesApiUtils.doPost(json);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //定时查出有异常的设备
			 List<Map<String,Object>> lis = mat.queryDeviceBg();
            //每五分钟比较异常时间，进行批量修改

			for (Map m :lis) {
				String bgDt = m.get("BG_START").toString();
				long[] datePoor = DateUtils.getDistanceTimes(bgDt, ph.getDateTimes()); //比较两个时间相差多少分
				mat.upDeviceBg(ph.getDateTimes(),datePoor[2],m.get("ET_CD").toString());
			}

		} catch (Exception e) {
            System.out.println(e.toString());
            result = "SAP连接超时";
        }
        return "";
    }

    public void insertMtMain(String sql, final List<Material> list) {
        this.getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {
            String squSql = "select SAP_MTSQU.NEXTVAL tt from dual";

            {
            }

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                // TODO Auto-generated method stub

                Map<String, Object> queryForMap = getJdbcTemplate().queryForMap(squSql);
                Object object = queryForMap.get("tt");//获取序列自增id
                ps.setString(1, list.get(i).getFct_code());
                ps.setString(2, list.get(i).getMt_matkl());
                ps.setString(3, list.get(i).getMT_WGBEZ());
                ps.setString(4, list.get(i).getIT_TP());
                ps.setString(5, list.get(i).getMT_TNR());
                ps.setString(6, list.get(i).getMT_DS());
                ps.setString(7, list.get(i).getMT_MST());
                ps.setString(8, list.get(i).getMT_MST_DS());
                ps.setString(9, list.get(i).getMEINS());
                ps.setObject(10, object);

                if (i % 5000 == 0 && i != 0) {
                    ps.executeBatch();
                }
            }

            @Override
            public int getBatchSize() {
                System.out.println(list.size());
                return list.size();
            }
        });
    }

    public void saveOrUpdate(String sql) {//新增中间表数据到MES物料表或者删除中间表数据
        this.getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {

            {
            }

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                // TODO Auto-generated method stub
                if (i % 5000 == 0) {
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
