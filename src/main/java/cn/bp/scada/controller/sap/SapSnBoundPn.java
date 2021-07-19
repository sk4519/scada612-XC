package cn.bp.scada.controller.sap;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

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
import cn.bp.scada.sap.bean.YCSNOrPN;

@RestController
public class SapSnBoundPn extends JdbcDaoSupport{
	@Resource
    public void setJb(JdbcTemplate jb) {
        super.setJdbcTemplate(jb);
    }

	@Autowired
	private SapConnUtils sapUtils;

	/**
	 * 原厂SN集中绑定浪潮PN接口
	 * @return
	 */
	@Scheduled(fixedDelay = 360000) //定时任务，间隔执行，每6分钟
	public String snBoundPn(){

		System.out.println("进入原厂SN集中绑定浪潮PN接口:");
		try {
		JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象
		JCoFunction function = sapUtils.getFunction("ZRFC_PP_YCSN_MATERIAL");//获取到函数

		//入参
				JCoParameterList parameterList = function.getImportParameterList();
			//设置入参
				String sqlMax = "select max(t.erdate) cdte from YCSN_MATERIAL t";
				Map<String, Object> queryForMap = this.getJdbcTemplate().queryForMap(sqlMax);
				Object object = queryForMap.get("cdte");
				String st ="";
				if(object == null) {
					st="20170927000000";
				}else{
					st  =  Long.parseLong(getDateStr(object.toString()))+"";

				}
				System.out.println("起始日期："+st);

				parameterList.setValue("BEGINDATE", st+""); //开始日期
				parameterList.setValue("ENDDATE", "20990927"); //结束日期
				function.execute(jCoDestination);
			parameterList.clear();

			String sql ="insert into YCSN_MATERIAL(matnr,charg,ycqn,maktx,erdate)"
					+ "values(?,?,?,?,to_date(?,'yyyy-mm-dd hh24:mi:ss'))";
			final List<YCSNOrPN> list= new ArrayList<YCSNOrPN>();
			JCoTable  table = function.getTableParameterList().getTable("ET_TAB");//获取表
			System.out.println("原厂SN绑定条数为："+table.getNumRows());
			System.out.println(" ");
			for (int i = 0; i < table.getNumRows(); i++) {
				YCSNOrPN yp = new YCSNOrPN();
				table.setRow(i);
				yp.setMatnr(table.getString("MATNR"));  //物料编号
				yp.setCharg(table.getString("CHARG"));  //批号
				 yp.setYcqn(table.getString("YCQN"));   //原厂SN
				yp.setMaktx(table.getString("MAKTX"));  //物料描述
			   yp.setErdate(table.getString("ERDAT") +table.getString("ERZEIT")); //创建日期
			   list.add(yp);
			}
			insertBatchYCSN(sql,list);
		}catch (JCoException e){
			System.out.println(e.toString());
			}
		return "ok";
	}

	public void insertBatchYCSN(String sql,final List<YCSNOrPN> listItem){
		this.getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter(){

			{
			}

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				// TODO Auto-generated method stub

				ps.setString(1, listItem.get(i).getMatnr());
				ps.setString(2, listItem.get(i).getCharg() );
				ps.setString(3, listItem.get(i).getYcqn());
				ps.setString(4, listItem.get(i).getMaktx());
				ps.setString(5, listItem.get(i).getErdate());

				if(i%5000==0 && i!=0){
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

	public String  getDateStr(String st)  {

		SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date parse = null;
		try {
			parse = sim.parse(st);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		SimpleDateFormat sims = new SimpleDateFormat("yyyyMMddHHmmss");
		String format = sims.format(parse);
		System.out.println("上次同步的最大日期时间为："+format);
		return format;
	}


}
