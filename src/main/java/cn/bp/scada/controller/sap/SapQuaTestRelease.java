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

import cn.bp.scada.sap.SapConnUtils;
import cn.bp.scada.sap.bean.QuaTestRa;
import cn.bp.scada.common.utils.PrimaryHelper;

@RestController
public class SapQuaTestRelease extends JdbcDaoSupport{
	@Resource
    public void setJb(JdbcTemplate jb) {
        super.setJdbcTemplate(jb);
    }

	@Autowired
	private SapConnUtils sapUtils;
	@Autowired
	private PrimaryHelper ph;

	private Logger LOG = LoggerFactory.getLogger(this.getClass());
	/**
	 * 质检释放凭证接口
	 * @return
	 * @throws ParseException
	 */
	@Scheduled(cron="0 0 3,5 * * ? ")  //定时任务(按左到右的顺序依次为：秒，分，时，天，月，星期)，每天凌晨3,5点触
	//@Scheduled(fixedDelay = 10000)  //定时任务，间隔执行，每1分钟
	public String QuaTestRelease() throws ParseException{

		LOG.info("进入质检释放凭证,当前时间为:"+ph.getDateTime());
		try {
		JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象
		JCoFunction function = sapUtils.getFunction("ZMM_MATERIAL_UD");//获取到函数
		//入参
		JCoParameterList parameterList = function.getImportParameterList();
	//设置入参
		String sqlMax = "select max(BLDAT) cdte  from QUA_TEST_RA";
		Map<String, Object> queryForMap = this.getJdbcTemplate().queryForMap(sqlMax);
		Object object = queryForMap.get("cdte");
		String st ="";
		if(object == null) {
			st="20190901";
		}else{

			Calendar calendar = Calendar.getInstance();

			Date dat = new Date();
			SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd");

			calendar.setTime(dat);
			calendar.add(calendar.DATE, -1);
			String format = sim.format(calendar.getTime());

			st  =  getDateStr(format);

		}
		LOG.info("质检释放起始日期："+st);
		parameterList.setValue("B_BUDAT", st); //起始收货日期
		parameterList.setValue("E_BUDAT", "20991216"); //结束收货日期
		parameterList.setValue("B_MBLNR", ""); //起始物料凭证编号
		parameterList.setValue("E_MBLNR", ""); //结束物料凭证编号
		parameterList.setValue("B_WERKS", "1100"); //起始工厂
		parameterList.setValue("E_WERKS", "8100"); //结束工厂
		function.execute(jCoDestination);

		//如果入参是内表形式，就以如下代码传入sap系统
		JCoTable  table = function.getTableParameterList().getTable("OUTPUT");//获取表
		final List<QuaTestRa> list= new ArrayList<QuaTestRa>();
		LOG.info(table.getNumRows()+"条质检释放凭证数据: --");
		if(table.getNumRows()>0){
			for (int i = 0; i < table.getNumRows(); i++) {
				table.setRow(i);
				QuaTestRa qua= new QuaTestRa();
				qua.setMblnr(table.getString("MBLNR")); //物料凭证编号
				qua.setZeile(Integer.parseInt(table.getString("ZEILE"))); //物料凭证中的项目
				qua.setMjahr(Integer.parseInt(table.getString("MJAHR"))); //物料凭证年度
				qua.setWerks(table.getString("WERKS")); //工厂
				qua.setEbeln(table.getString("EBELN")); //采购凭证号
				qua.setEbelp(Integer.parseInt(table.getString("EBELP"))); //采购凭证的项目编号
				qua.setCharg(table.getString("CHARG")); //批号
				qua.setMeins(table.getString("MEINS")); //基本计量单位
				qua.setMenge(Double.parseDouble(table.getString("MENGE"))); //数量
				qua.setLifnr(table.getString("LIFNR")); //供应商或债权人的帐号
				qua.setBldat(table.getString("BLDAT")); //凭证中的凭证日期
				qua.setCputm(table.getString("CPUTM")); //输入时间
				qua.setMatnr(table.getString("MATNR")); //物料编号
				qua.setMaktx(table.getString("MAKTX")); //物料描述
				qua.setUsnam(table.getString("USNAM")); //用户名
				qua.setBktxt(table.getString("BKTXT")); //凭证抬头文本
				qua.setBwart(table.getString("BWART")); //移动类型(库存管理)
				qua.setShkzg(table.getString("SHKZG")); //借方/贷方标识
				qua.setInsmk(table.getString("INSMK")); //库存类型
				qua.setName1(table.getString("NAME1")); //名称 1
				list.add(qua);
			}
		} else {
		LOG.info("质检释放没有从ERP获取到数据");
		}

		String sql="insert into QUA_TEST_RA(mblnr, zeile, mjahr, werks, ebeln, ebelp, charg, meins, menge, lifnr, bldat, cputm,"
				+ "matnr, maktx, usnam, bktxt, bwart, shkzg, insmk, name1,id,in_dt)"
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,sysdate)";

		insertQuaTest(sql,list);//批量增加质检释放凭证
		LOG.info("质检释放凭证添加成功");
	}catch (JCoException e){
		LOG.info(e.toString());
		}
		return "ok";
	}

	public void insertQuaTest(String sql,final List<QuaTestRa> list){
		this.getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter(){
			String squSql="select sap_quaTest.NEXTVAL tt from dual";
			{
			}
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				// TODO Auto-generated method stub

				Map<String, Object> queryForMap = getJdbcTemplate().queryForMap(squSql);
				Object object = queryForMap.get("tt");//获取序列自增id
				ps.setString(1, list.get(i).getMblnr());
				ps.setInt(2, list.get(i).getZeile());
				ps.setInt(3, list.get(i).getMjahr());
				ps.setString(4, list.get(i).getWerks());
				ps.setString(5, list.get(i).getEbeln());
				ps.setInt(6, list.get(i).getEbelp());
				ps.setString(7, list.get(i).getCharg());
				ps.setString(8, list.get(i).getMeins());
				ps.setDouble(9, list.get(i).getMenge());
				ps.setString(10, list.get(i).getLifnr());
				ps.setString(11, list.get(i).getBldat());
				ps.setString(12, list.get(i).getCputm());
				ps.setString(13, list.get(i).getMatnr());
				ps.setString(14, list.get(i).getMaktx());
				ps.setString(15, list.get(i).getUsnam());
				ps.setString(16, list.get(i).getBktxt());
				ps.setString(17, list.get(i).getBwart());
				ps.setString(18, list.get(i).getShkzg());
				ps.setString(19, list.get(i).getInsmk());
				ps.setString(20, list.get(i).getName1());
				ps.setObject(21, object);
				if(i%5000==0 && i!=0){
					ps.executeBatch();
				}
			}

			@Override
			public int getBatchSize() {

				return list.size();
			}
		});
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
		LOG.info("当前同步质检日期时间为："+format);
		return format;
	}
}
