package cn.bp.scada.controller.scada;


import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import cn.bp.scada.mapper.scada.Pack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.bp.scada.common.utils.dbhelper.DBHelperLocation;
import cn.bp.scada.common.utils.dbhelper.DBHelperOld;
import cn.bp.scada.common.utils.MesApiUtils;
import cn.bp.scada.common.utils.PrimaryHelper;

@RestController
public class SynOldTest extends JdbcDaoSupport{
	@Resource
    public void setJb(JdbcTemplate jb) {
        super.setJdbcTemplate(jb);
    }
	@Resource
	private DBHelperOld db;
	@Resource
	private MesApiUtils mesApiUtils;
	@Autowired
	private PrimaryHelper ph;
	@Resource
	private ScadaToAgvBan sab;
	@Resource
	private DBHelperLocation dbLocation;
	@Resource
	private ScadaToAgvBan agvBan;
	@Autowired
	private Pack pack;

	private Logger LOG = LoggerFactory.getLogger(this.getClass());
	private static int count =0;


	/**
	 * 同步老化测试程序
	 * @param reqStr
	 * @return
	 * @throws JSONException
	 */
	@Scheduled(fixedDelay = 60000) //定时任务，间隔执行，每1分钟
	public String writeTest() throws JSONException  {
		try {
				LOG.info("进入判断老化完成逻辑");
				count = 0;
			//从diag程序获取已经老化完成的产品，根据老化载具绑定表获取SN
			String passSql = "select PRODUCTSN from OLD_SHELF_BIND m "
					+ "where m.BIND_STA = 0 ";
			List<Map<String, Object>> passList = this.getJdbcTemplate().queryForList(passSql);
		 if(passList.size()>0) {
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < passList.size(); i++) {

				if(i == passList.size() -1 ) {
					buffer.append("'"+passList.get(i).get("PRODUCTSN")+"'");
				} else {
					buffer.append("'"+passList.get(i).get("PRODUCTSN")+"'"+",");
				}

			}
			//从uplaod_mes表找老化pass的数据，
			String uplaodSql = "select sn,TEST_STATUS from UPLOAD_MES where sn in("+buffer+") and test_station=2 and test_status='Pass'";
			String failSql ="select sn,TEST_STATUS from UPLOAD_MES where sn in("+buffer+") and test_station=2 and test_status='Fail'";
			List<Map<String, Object>> uplaodList = this.getJdbcTemplate().queryForList(uplaodSql);
			 List<Map<String, Object>> failList = this.getJdbcTemplate().queryForList(failSql);
		 if(uplaodList.size()>0) {
			StringBuffer uplaodBuffer = new StringBuffer();
			for (int i = 0; i < uplaodList.size(); i++) {
				if(i == uplaodList.size() -1 ) {
					uplaodBuffer.append("'"+uplaodList.get(i).get("sn")+"'");
				} else {
					uplaodBuffer.append("'"+uplaodList.get(i).get("sn")+"'"+",");
				}
			}


			//把pass的SN更新绑定载具表
			 if(uplaodList.size()>0) {
				 pack.updateBatchBind(uplaodList);
			 }
			 //把Fail的SN更新绑定载具表
			 if(failList.size()>0){
				 pack.updateBatchBindFail(failList);
			 }
			String testLogSql = "select * from (select count(*) count,SUBSTR(LOCATION,1,5) as shift,REVERSE(substring(REVERSE(LOCATION),1,6)) framegr "
					+ "from LOCATION_INFO where sn in("+uplaodBuffer+") GROUP BY  shift)a left JOIN"
					+ "(select sn,SUBSTR(LOCATION,1,5) as shift2,count(*) count1,REVERSE(substring(REVERSE(LOCATION),1,6)) frame from LOCATION_INFO "
					+ "GROUP BY shift2)  b on a.count=b.count1 and a.framegr=b.frame;";
			List<Map<String, Object>> LocationList = dbLocation.executeQueryTest(testLogSql, null, null);


			StringBuffer buffer2 = new StringBuffer();
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			if(LocationList.size()<1) {
				buffer2.append("null");
			}

			for (int i = 0; i < LocationList.size(); i++) {
				Map<String,Object> ma = new HashMap<String,Object>();
			if(LocationList.get(i).get("frame") != null) {
				//LOG.info("老化完成的货架号为："+LocationList.get(i).get("frame")+",位置为："+LocationList.get(i).get("shift2"));
				if(i == LocationList.size() -1 ) {
					buffer2.append("'"+LocationList.get(i).get("frame")+"'");

					ma.put("BOXNO",LocationList.get(i).get("frame"));
				} else {
					buffer2.append("'"+LocationList.get(i).get("frame")+"'"+",");
					ma.put("BOXNO",LocationList.get(i).get("frame"));
				}
				list.add(ma);
				} else {
				if(i == LocationList.size() -1 ) {
					buffer2.append("'null'");
				}else{
					buffer2.append("'null'"+',');
				}

				}

			}


			// 得到已完成的老化架 - locationList,把所有完成的老化架更改状态为老化完成
			String updaSql = "UPDATE MES1.MES_TURNOVER_TOOL_CREATE SET SHELF_STA = 2 WHERE SN = ?";
			updateBatchTool(updaSql,list);


			String completeSql = "select OLD_COMPLETE,SN from MES_TURNOVER_TOOL_CREATE where SN  IN ("+buffer2+") AND OLD_COMPLETE = 0";
			List<Map<String, Object>> completeList = this.getJdbcTemplate().queryForList(completeSql);

			JSONObject jsonOld = new JSONObject();
			//查出老化完成的货架，发送给AGV，有则发，没有则不进循环
			 LOG.info("老化完成的货架："+completeList);
			for (int i = 0; i < completeList.size(); i++) {

				jsonOld.put("podCode", completeList.get(i).get("SN"));
				jsonOld.put("podStatus", "1");
				//根据老化架编号查出老化室位置
				Map<String, Object> map = null;
				try {
					String Oldsql = "SELECT * FROM (SELECT OLD_STATION_ID FROM MES1.OLD_SHELF_BIND WHERE BOXNO='"+completeList.get(i).get("SN")+"') WHERE ROWNUM =1 ";
					map = this.getJdbcTemplate().queryForMap(Oldsql);
					jsonOld.put("destWb", map.get("OLD_STATION_ID").toString());
					agvBan.modifyPodStatus(jsonOld);
				} catch (DataAccessException e) {
					e.printStackTrace();
				}

			}

			//1.根据后测排产工单得到当前排产的工单号
			String afterSql = "select MO_NO from (select mo_no from MES1.R_MES_WO_T where AFTER_STATE = 5) where rownum  = 1";
			Map<String, Object> afterMap = this.getJdbcTemplate().queryForMap(afterSql);

			//2.在去周转工具列表查出老化架是否已拔线,是否已发送过老化完成
			String lineSql = "SELECT SN FROM MES_TURNOVER_TOOL_CREATE WHERE PULL_LINE=1 AND OLD_COMPLETE = 1";
			List<Map<String, Object>> podList = this.getJdbcTemplate().queryForList(lineSql);
			StringBuffer moBuffer = new StringBuffer();
			if(podList.size()<1) {
				moBuffer.append("null");
			}
			for (int i = 0; i < podList.size(); i++) {
				if(i == podList.size() -1 ) {
					moBuffer.append("'"+podList.get(i).get("SN")+"'");
				} else {
					moBuffer.append("'"+podList.get(i).get("SN")+"'"+",");
				}
			}

			//3.根据工单号和拔线老化架、是否发送过创建任务给AGV判定哪些要被拉走
			String moToOldSql = "SELECT DISTINCT BOXNO,OLD_STATION_ID FROM MES1.OLD_SHELF_BIND WHERE IS_HAND = 0 AND BIND_STA = 0 AND BOXNO IN("+moBuffer+") AND MO_NO='"+afterMap.get("MO_NO")+"' ";
			List<Map<String, Object>> moList = this.getJdbcTemplate().queryForList(moToOldSql);
			JSONObject jsonMo = new JSONObject();
			 LOG.info("要拉走的货架："+moList);
			for (int i = 0; i < moList.size(); i++) {
				jsonMo.put("taskType", "agingToQuaInsp");
				jsonMo.put("srcWb", moList.get(i).get("OLD_STATION_ID"));
				jsonMo.put("podCode", moList.get(i).get("BOXNO"));
				//sab.agvHandlingTask(jsonMo);
			}
		  } //从uplaod_mes表找老化pass的数据的结尾
		 }//if passList(载具绑定表查询绑定=0的结尾)
			//} //if count=4的结尾
		} catch (Exception e) {
			e.printStackTrace();
		}


		return  "success";
	}

	public void insertOld(String sql,final List<Map<String,Object>> list){
		this.getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter(){
			{
			}
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				// TODO Auto-generated method stub

				ps.setObject(1, list.get(i).get("ID"));
				ps.setObject(2, list.get(i).get("list_id"));
				ps.setObject(3, list.get(i).get("pn"));
				ps.setObject(4, list.get(i).get("SN"));
				ps.setObject(5, list.get(i).get("NODE_SN"));
				ps.setObject(6, list.get(i).get("MBQN"));
				ps.setObject(7, list.get(i).get("TEST_STATION"));
				ps.setObject(8, list.get(i).get("TEST_ITEM"));
				ps.setObject(9, list.get(i).get("TEST_STATUS"));
				ps.setObject(10, list.get(i).get("ERROR_CODE"));
				ps.setObject(11, list.get(i).get("ERROR_MSG"));
				ps.setObject(12, list.get(i).get("PROCESS_TIME"));

				if(i%5000==0 && i!=0){
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

	public String  getDateStr(String st)  {

		SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date parse = null;
		try {
			parse = sim.parse(st);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		SimpleDateFormat sims = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String format = sims.format(parse);
		return format;
	}

	public void updateBatchTool(String sql,final List<Map<String,Object>> list){
		this.getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter(){
			{
			}

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				// TODO Auto-generated method stub
				ps.setObject(1, list.get(i).get("BOXNO"));
				if(i>= list.size()){
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

	public void updateBatchOld(String sql,final List<Map<String,Object>> list){
		this.getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter(){
			{
			}

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				// TODO Auto-generated method stub
			}

			@Override
			public int getBatchSize() {
				// TODO Auto-generated method stub
				return list.size();
			}
		});
	}

	public void saveOrUpdate(String sql){//
		this.getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter(){

			{
			}
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				// TODO Auto-generated method stub
				if(i%2==0 && i!=0){
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

