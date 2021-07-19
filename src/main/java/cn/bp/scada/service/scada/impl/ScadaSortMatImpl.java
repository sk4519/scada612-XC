package cn.bp.scada.service.scada.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import cn.bp.scada.service.scada.ScadaSortMat;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import cn.bp.scada.modle.scada.ScadaRespon;
import cn.bp.scada.common.utils.MesApiUtils;

/*
料箱去生产位步骤：
1.页面UI扫描物料后把扫描的物料信息会加入到历史记录表（MES1.RMBOX_HIS） ,mes会把载具和分拣位绑定
2.人工工位上把对应的物料放入空框中，出分拣位的时候上位机扫描该料箱，SCADA返回该料箱对应的生产位，随后放上皮带线，料框去到生产区，经过上位机扫描，scada把该料箱对应的点位回传给上位机,把该料箱状态改为使用中

空料箱返回步骤：
1.料箱回到对应的分拣位，返回时经过上位机扫描，scada把该箱号对应的分拣位回传给上位机
2.进行解绑，把料箱状态改为可使用
 */
@Service
public class ScadaSortMatImpl implements ScadaSortMat {
	@Resource
	private MesApiUtils mesApiUtils;
	@Resource
	private JdbcTemplate template;
	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	/**
	 * 分拣区到二楼具体的工位(& agv点位）
	 * @throws IOException
	 */
	public ScadaRespon checkData(Object msg) throws  JSONException, IOException {
		JSONObject conveyorRequest = new JSONObject(msg.toString());
		//分拣完物料上二楼，到达每个口经过上位机扫描该料箱，上位机把料箱SN传给SCADA
		//SCADA把料箱条码传给MES，返回物料需求位，线别给SCADA

		JSONObject reqDataDetail = new JSONObject();
		reqDataDetail.put("IFS", "ACT0050");
		reqDataDetail.put("IN_PSN_NO", conveyorRequest.getString("con_sn"));//获取物料周转箱SN

		JSONObject responseData = mesApiUtils.doPost(reqDataDetail); //请求mes，mes返回agv点位给SCADA
		ScadaRespon result = new ScadaRespon();


		if(responseData.getString("STATUS").equals("1")){ //验证成功

		String line = responseData.getString("LINECD"); //mes返回的产线，1代表1线，2代表2线

		Map<String, Object> map = new HashMap<String, Object>();
		String sql ="";
		String flowCode=""; //料箱去到的点位
		String poin =responseData.getString("LOCATIONID"); //AGV供料点

	if(!poin.equals("1") && !poin.equals("2") && !poin.equals("3") && !poin.equals("4")){
			//不进设备的情况下
		if(line.equals("1")){ //查询1线
			LOG.info("此料箱为一线，料箱SN为："+conveyorRequest.getString("con_sn")+",供料点为："+poin);
			sql="select BOX_EXIST from ET_IP_AGV where MAT_POINT='A' ";
			map = template.queryForMap(sql);
			Object resl="";

			switch (map.size()) {
				case 1: //查出了数据
					resl = map.get("BOX_EXIST");
					int num =Integer.parseInt(resl.toString());
					//LOG.info("分拣线A点num为："+num);
					if(num == 0) { //判断小口已存数量是0，就流入小口
						flowCode = "A";  //1线的小口对应的agv点位给上位机(点位定死)
						//然后将小口的物料存放数量+1
						sql= "update ET_IP_AGV set BOX_EXIST ="+num+"+1 where MAT_POINT='A' ";

					}else { //流入大口
						flowCode = "C";  //1线的大口对应的agv点位给上位机
						sql= "update ET_IP_AGV set BOX_EXIST =BOX_EXIST+1 where MAT_POINT='C' ";

					}
				break;

				default:
				break;
			}

		}else { //查询2线
			LOG.info("此料箱为二线，料箱SN为："+conveyorRequest.getString("con_sn")+",供料点为："+poin);
			sql="select BOX_EXIST from ET_IP_AGV where MAT_POINT='B' ";
			map = template.queryForMap(sql);
			Object resl="";

			switch (map.size()) {
				case 1: //查出了数据
					resl = map.get("BOX_EXIST");
					int num =Integer.parseInt(resl.toString());
					if(num == 0) { //判断小口已存数量是0，就流入小口
						flowCode = "B";  //1线的小口对应的agv点位给上位机(点位定死)
						//然后将小口的物料存放数量+1
						sql= "update ET_IP_AGV set BOX_EXIST ="+num+"+1 where MAT_POINT='B' ";

					}else { //流入大口
						flowCode = "D";  //1线的大口对应的agv点位给上位机
						sql= "update ET_IP_AGV set BOX_EXIST =BOX_EXIST+1 where MAT_POINT='D' ";

					}
				break;

				default:
				break;
			}
		}
		template.update(sql);


	}else {
		//进设备
		flowCode= responseData.getString("LOCATIONID"); //给上位机物料流入点位
		LOG.info("进设备，给的flowCode为："+flowCode);
	}


			result.setResult_flag("OK");
			result.setResult_message(responseData.getString("MESSAGE"));
			result.setLine_code(line); //
			result.setFlow_code(flowCode); //料框终点位
			result.setCon_sn(conveyorRequest.getString("con_sn"));
		} else {
			result.setResult_flag("NG");
			result.setResult_message(responseData.getString("MESSAGE"));
		}

		 return result;
	}


	/**
	 * 物料到设备(工位)后空箱传回一楼分拣位
	 */
	public ScadaRespon sortScan(Object msg) throws Exception {
		JSONObject ScanRequest = new JSONObject(msg.toString());
		ScadaRespon sr=new ScadaRespon();
		LOG.info("进入空箱回一楼接口");
		try {

			Object object = ScanRequest.get("con_sn"); //获取物料周转箱SN

			JSONObject reqDataDetail = new JSONObject();
			reqDataDetail.put("IFS", "STA113");
			reqDataDetail.put("IN_VEHICLES", object.toString());//获取物料周转箱SN

			JSONObject responseData = mesApiUtils.doPost(reqDataDetail); //请求mes
			String sql = "SELECT WORKNO,WO_NO  FROM (SELECT WORKNO,WO_NO \n" +
					"       FROM MES1.RMBOX_HIS WHERE BOXNO=? ORDER BY NOWTIME DESC) WHERE 1=1 AND ROWNUM=1";
			 Map<String, Object> map = template.queryForMap(sql,object);

				sr.setResult_flag("OK");
				sr.setFlow_code(map.get("WORKNO").toString());
				sr.setResult_message(responseData.getString("MSG"));

		}catch(Exception e) {
			sr.setResult_flag("NG");
			e.printStackTrace();
		}
		return sr;
	}
}

/**
 * // 去历史记录表查询料箱对应的工位
 * 		String sortSql = "select BOXNO,BOXQTY,WORKNO,WO_NO from (select BOXNO,BOXQTY,WORKNO,WO_NO from MES1.RMBOX_HIS "
 * 						+ "where BOXNO='"+object+"' order by creatime desc) a where rownum=1 ";
 *
 * 		Map<String, Object> map = new HashMap<String, Object>();
 *
 * 		try{
 * 			map = template.queryForMap(sortSql);
 *                } catch(Exception e){
 * 			LOG.info("Exception: 没有该料箱");
 *        }
 *
 * 		if(map.size() >0) {
 *
 *
 *
 * 			String object2 = map.get("WORKNO").toString(); // 获取工位编码
 * 			//返回信息给上位机
 * 					sr.setResult_flag("OK");
 * 					sr.setFlow_code(object2);
 * 					sr.setResult_message("查询成功");
 *
 * 					// 将料箱状态改为Y(可使用)
 * 					String stateSql = "update MES1.MES_TURNOVER_TOOL_CREATE set USER_YN = 'Y',upt_dt= sysdate where SN= ?";
 * 					template.update(stateSql, object);
 *
 *        } else {
 * 			sr.setResult_flag("NG");
 * 			sr.setResult_message("没有查询到该料箱");
 *        }
 */