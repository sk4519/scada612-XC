package cn.bp.scada.controller.sap;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.util.JSONPObject;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
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
				String sqlMax = "select t.erdate cdte,ycqn from YCSN_MATERIAL t where erdate=(select max(erdate) from ycsn_material) and rownum=1";
				Map<String, Object> queryForMap = this.getJdbcTemplate().queryForMap(sqlMax);
			    Object object = queryForMap.get("cdte");
			    Object object1 = queryForMap.get("YCQN");
				String st ="";
				if(object == null) {
					st="20170927000000";
				}else{
					st  =  Long.parseLong(getDateStr(object.toString()))+"";

				}
				System.out.println("起始日期："+st);

				parameterList.setValue("BEGINDATE", st+""); //开始日期
				parameterList.setValue("ENDDATE", "20990927"); //结束日期
			//parameterList.setValue("YCSN", "2L0300105975"); //物料名称
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
				String ycsn=table.getString("YCQN");
				if (!ycsn.equals(object1.toString())){
				yp.setMatnr(table.getString("MATNR"));  //物料编号
				yp.setCharg(table.getString("CHARG"));  //批号
				yp.setYcqn(ycsn);   //原厂SN
				yp.setMaktx(table.getString("MAKTX"));  //物料描述
			   yp.setErdate(table.getString("ERDAT") +table.getString("ERZEIT")); //创建日期
			   list.add(yp);}
			}
			insertBatchYCSN(sql,list);
		}catch (JCoException e){
			System.out.println(e.toString());
			}
		return "ok";
	}
	//手动获取原厂sn
	@RequestMapping("/sapGainYcsnByHand")
	public JSONPObject snBoundPnByHand(HttpServletRequest req, String data){
		Map<String, Object> map = new HashMap<>();
		String name = req.getParameter("callbackparam");//获取到jsonp的函数名
		JSONObject json = new JSONObject(data);
		String ycsnNo = json.getString("ycsnNo");
		System.out.println("进入手动同步原厂SN接口，入参："+ycsnNo);
		try {
			JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象
			JCoFunction function = sapUtils.getFunction("ZRFC_PP_YCSN_MATERIAL");//获取到函数
			//入参
			JCoParameterList parameterList = function.getImportParameterList();
			//设置入参
			String sqlMax = "select COUNT(1) from YCSN_MATERIAL where ycqn='"+ycsnNo+"'";
			Map<String, Object> queryForMap = this.getJdbcTemplate().queryForMap(sqlMax);
			if(Integer.parseInt(queryForMap.get("COUNT(1)").toString())!=0){
				return new JSONPObject(name, "已经存在系统中，请勿重复同步！");
			}
			parameterList.setValue("YCSN", ycsnNo); //物料名称
			function.execute(jCoDestination);
			parameterList.clear();
			String sql ="insert into YCSN_MATERIAL(matnr,charg,ycqn,maktx,erdate)"
					+ "values(?,?,?,?,to_date(?,'yyyy-mm-dd hh24:mi:ss'))";
			final List<YCSNOrPN> list= new ArrayList<YCSNOrPN>();
			JCoTable  table = function.getTableParameterList().getTable("ET_TAB");//获取表
			System.out.println("原厂SN绑定条数为："+table.getNumRows());
			if(table.getNumRows()==0){
				return new JSONPObject(name, "sap中没有对应维护数据，请联系仓库维护！");
			}
			for (int i = 0; i < table.getNumRows(); i++) {
				YCSNOrPN yp = new YCSNOrPN();
				table.setRow(i);
				yp.setMatnr(table.getString("MATNR"));  //物料编号
				yp.setCharg(table.getString("CHARG"));  //批号
				yp.setYcqn(table.getString("YCQN"));   //原厂SN
				yp.setMaktx(table.getString("MAKTX"));  //物料描述
				yp.setErdate(table.getString("ERDAT") + table.getString("ERZEIT")); //创建日期
				list.add(yp);
			}
			insertBatchYCSN(sql,list);
		}catch (JCoException e){
			System.out.println(e.toString());
		}
		return new JSONPObject(name, ycsnNo+"物料，同步成功！");
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
