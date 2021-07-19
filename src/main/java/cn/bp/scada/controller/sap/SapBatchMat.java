package cn.bp.scada.controller.sap;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import cn.bp.scada.mapper.scada.CiPLANTeTMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoTable;

import cn.bp.scada.mapper.scada.BatchMat;
import cn.bp.scada.sap.SapConnUtils;
import cn.bp.scada.sap.bean.BatchMatInfo;
import cn.bp.scada.common.utils.PrimaryHelper;

@RestController
public class SapBatchMat extends JdbcDaoSupport{
	@Resource
    public void setJb(JdbcTemplate jb) {
        super.setJdbcTemplate(jb);
    }
	@Resource
	private BatchMat batchMat;
	@Resource
	private JdbcTemplate template;
	@Autowired
	private SapConnUtils sapUtils;
	@Autowired
	private PrimaryHelper ph;
	@Resource
	private CiPLANTeTMapper ciPLANTeTMapper;
	private Logger LOG = LoggerFactory.getLogger(this.getClass());
	/**
	 * 批次物料信息接口
	 * @return
	 */
	@Scheduled(cron="0 0 4,10,18,23 * * ? ")  //定时任务(按左到右的顺序依次为：秒，分，时，天，月，星期)，每天4,10,18,23点触发
	//@Scheduled(fixedDelay = 10000) //定时任务，间隔执行，每7分钟
	public String batchMats(){
		LOG.info("进入批次物料信息接口,当前时间为:"+ph.getDateTime());
		try {
		JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象
		JCoFunction function = sapUtils.getFunction("ZRFC_MM_BATCHMATERIAL");//获取到函数

		//入参
				JCoParameterList parameterList = function.getImportParameterList();
			//设置入参
				String sqlMax = "select max(ERSDA) cdte  from BATCHMAT";
				Map<String, Object> queryForMap = template.queryForMap(sqlMax);
				Object object = queryForMap.get("cdte");
				String st ="";
				if(object == null) {
					st="20191201";
				}else{
					LOG.info("上次同步的最大批次物料日期为："+object.toString());
					Calendar calendar = Calendar.getInstance();

					Date dat = new Date();
					SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd");

					calendar.setTime(dat);
					calendar.add(calendar.DATE, -1);
					String format = sim.format(calendar.getTime());

					st  = getDateStr(format);

				}
				LOG.info("批次物料起始日期："+st);

				parameterList.setValue("BEGINDATE", st); //创建日期,开始
				parameterList.setValue("ENDDATE", "20990401"); //创建日期,结束
			function.execute(jCoDestination);

			JCoTable  table = function.getTableParameterList().getTable("IT_INFO");//获取表
			final List<BatchMatInfo> list = new ArrayList<BatchMatInfo>();
			System.out.println(table.getNumRows()+"条批次物料信息");
			if(table.getNumRows()>0) {
				for (int i = 0; i < table.getNumRows(); i++) {
					BatchMatInfo batchMatInfo = new BatchMatInfo();
					table.setRow(i);
						batchMatInfo.setMatnr(table.getString("MATNR"));
						batchMatInfo.setCharg(table.getString("CHARG"));
						batchMatInfo.setMaktx(table.getString("MAKTX"));
						batchMatInfo.setErsda(table.getString("ERSDA"));
						batchMatInfo.setLifnr(table.getString("LIFNR"));
						batchMatInfo.setName1(table.getString("NAME1"));
					list.add(batchMatInfo);
				}
				String sql= "insert into BATCHMAT(matnr,charg, maktx, ersda,lifnr,name1,IN_DATE) values(?,?,?,?,?,?,sysdate)";
				//批量插入到批次物料表
				insertBatch(sql,list);
			}
		}catch (JCoException e){
			System.out.println(e.toString());
			}
		ciPLANTeTMapper.updateEt(0);
		String bgSql = "UPDATE IPLANT1.C_IPLANT_E2_T SET STOP_SUM=0,ET_STA='1'";
		template.update(bgSql);
		 String dateDev = ph.getDateDev();
		 String devdates = dateDev+" 8:30:00";
		String sqls = "UPDATE IPLANT1.C_IPLANT_E2_T SET DEV_START=?,PRO_START='',PRO_END='',CT='' ";
		template.update(sqls,devdates);
		String beSql = "UPDATE IPLANT1.C_IPLANT_E2_T SET BG_START='',BG_END='',BG_MSG='' ";
		template.update(beSql);
		String deSqlBG = "DELETE FROM SCADA_BG_J WHERE  BG_END IS NULL";
		template.update(deSqlBG);
		return "ok";
	}
public String  getDateStr(String st)  {

		SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd");
		Date parse = null;
		try {
			parse = sim.parse(st);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		SimpleDateFormat sims = new SimpleDateFormat("yyyyMMdd");
		String format = sims.format(parse);

		return format;
	}

public void insertBatch(String sql,final List<BatchMatInfo> listBom){
	this.getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter(){

		{
		}

		@Override
		public void setValues(PreparedStatement ps, int i) throws SQLException {
			// TODO Auto-generated method stub

			ps.setString(1, listBom.get(i).getMatnr() );
			ps.setString(2, listBom.get(i).getCharg() );
			ps.setString(3, listBom.get(i).getMaktx());
			ps.setString(4, listBom.get(i).getErsda());
			ps.setString(5, listBom.get(i).getLifnr());
			ps.setString(6, listBom.get(i).getName1());

			if(i%5000==0 && i!=0){
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
}
