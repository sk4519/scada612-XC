package cn.bp.scada.service.scada.impl;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import cn.bp.scada.service.scada.ExpoAgvService;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cn.bp.scada.modle.scada.AgvModle;
import cn.bp.scada.common.utils.agv.AgvAndDeviceState;
import cn.bp.scada.common.utils.agv.AgvHelper;
import cn.bp.scada.common.utils.dbhelper.DBHelper;
import cn.bp.scada.common.utils.SocketClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

/*
 * 设备请求出料流程
1、设备（编号40 or 20）请求出料 --> 发出 2100 （程序锁定AGV状态，isAgvUse值改为true）
2、AGV接到2100命令，等待堆垛机进入待命状态，之后查询立库信息，有物料则给堆垛机发送立库坐标,没有则结束本次请求（程序解除AGV锁定状态，isAgvUse值改为false）
3、AGV前往设备出料口，到位之后滚动滚筒并同时给设备（编号40 or 20）发出信号 --> 2100
4、设备（编号40 or 20）接到2100命令，滚动滚筒出料
5、AGV收到物料之后停止滚动滚筒，撤离设备出料口，同时向设备（编号40 or 20）发送完成收料信号 --> 2101
6、AGV前往堆垛机，到位后向堆垛机（编号60）发送到位信号 --> 2100
7、堆垛机（编号60）接到2100，滚动滚筒后给AGV发送信号 --> 2100
8、AGV收到堆垛机（编号60）的2100命令，滚动滚筒出料
9、堆垛机（编号60）完成收料后，发出收料完成信号 —> 2101
10、AGV接收到堆垛机（编号60）的2101命令，停止滚动滚筒，脱离堆垛机并发出撤离信号 —> 2101
11、程序解除AGV锁定状态，isAgvUse值改为false
*
* 设备请求进料流程
1、设备（编号40 or 20）请求上料 --> 发出 2000 （程序锁定AGV状态，isAgvUse值改为true）
2、AGV接到2000命令，等待堆垛机进入待命状态，之后查询立库信息，有物料则给堆垛机发送立库坐标,没有则结束本次请求（程序解除AGV锁定状态，isAgvUse值改为false）
3、AGV前往堆垛机，到位后滚动滚筒，同时向堆垛机（编号60）发送到位信号 --> 2005
4、AGV收到物料之后停止滚动滚筒，撤离堆垛机，同时向堆垛机（编号60）发送完成收料信号 --> 2006
5、AGV前往设备进料口，到位之后给设备（编号40 or 20）发出信号 --> 2000
6、设备（编号40 or 20）收到 2000 命令，滚动滚筒后给AGV发出出料信号 --> 2001
7、AGV接到 2001 命令，一直滚动滚筒出料
8、设备（编号40 or 20）收到物料后，停止滚动并发出信号 --> 2002
9、AGV接到2002命令，停止滚动滚筒，撤离设备（编号40 or 20）进料口，给设备发出撤离信号 --> 2002（程序解除AGV锁定状态，isAgvUse值改为false）
*/
/**
 * 实现调度AGV的服务类
 *
 * @author wuzhining
 *
 */
@Service
public class ExpoAgvServiceImpl implements ExpoAgvService {
	private Socket socket;
	private AgvHelper agvHelper = new AgvHelper();
	private AgvAndDeviceState agvAndDeviceState = AgvAndDeviceState.getInstance();;
	private String DeviceNum;
	private Logger LOG = LoggerFactory.getLogger(this.getClass());
	private ChannelHandlerContext ctx;


	public ExpoAgvServiceImpl() {
		this.socket = SocketClient.getInstance().socket;
	}

	public ExpoAgvServiceImpl(Socket socket) {
		this.socket = socket;
	}

	/**
	 * 添加AGV任务
	 *
	 * @param orderName 任务名称
	 */
	public void addAgvOrders(String orderName) {
		printLog("开始" + orderName);
		JSONObject jsonResult;
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(agvHelper.addOrders(agvHelper.getGuid(orderName)));
			while (true) {

				jsonResult = new JSONObject(agvHelper.getOrderState(jsonObject.getString("id")));
				if (jsonResult.get("state").equals("Done")) {
//				printLog(orderName + "任务已经完成。。。");
					LOG.error(orderName + "任务已经完成。。。");
					break;
				}

			}
		} catch (Exception e) {
			LOG.error("添加AGV任务时发生错误！");
		}
	}

	public void printLog(String msg) {
		LOG.info(msg);
	}


	/**
	 * 根据收到的设备指令给AGV分配任务（处理NettyServer接收的数据）
	 *
	 * @param ctx  服务端与客户端的socket连接
	 * @param data 客户端上传的数据
	 * @throws JSONException
	 */
	public void addAgvMissions(ChannelHandlerContext ctx, String data) throws JSONException {
		this.ctx = ctx;
		DBHelper db = new DBHelper();
		JSONObject deviceData = null;
		try {
			deviceData = new JSONObject(data);
			/*if((deviceData.getString("DeviceNum").equals("40") && deviceData.getString("Value").equals("2000"))
				|| (deviceData.getString("DeviceNum").equals("40") && deviceData.getString("Value").equals("2100"))
				|| deviceData.getString("DeviceNum").equals("20") && deviceData.getString("Value").equals("2000")
				|| deviceData.getString("DeviceNum").equals("20") && deviceData.getString("Value").equals("2100")){
				String sql ="select * from (select * from device  order by time asc)  where rownum=1";
				Device device = db.executeQuerys(sql);
				LOG.info("进入，从数据库按时间查询存储");
				deviceData.put("DeviceNum", device.getDeviceNum());
				deviceData.put("Value", device.getValue());
				String deleSql ="delete from device where ID= "+device.getId()+" ";
				db.excuteUpdate(deleSql, null);
			}*/
		} catch (Exception e) {
			LOG.error("转化数据时发生错误！");
			return;
		}

		String Value = deviceData.getString("Value");
		DeviceNum = deviceData.getString("DeviceNum");
		System.out.println("设备编号："+DeviceNum);
		switch (deviceData.getString("DeviceNum")) {
		case "60":// 堆垛机
			switch (deviceData.getString("Value")) {
			case "1000":
				if (this.agvAndDeviceState.isPilerUse() == true) {
					LOG.error("堆垛机进入待命状态。。。");
					this.agvAndDeviceState.setPilerUse(false);
				}
				break;

			case "2100":
				sendToClient("60", "22", "2101");
				break;

			case "2101":
				LOG.info("60机状态："+this.agvAndDeviceState.isPilerUse);
				if(this.agvAndDeviceState.isPilerUse == false){
				addAgvOrders("滚筒一直转");


				printLog("堆垛机进入工作转态。。。");

				sendToClient("60", "22", "2110");
				}
				// 堆垛机进入忙碌状态
				this.agvAndDeviceState.setPilerUse(false);
				this.agvAndDeviceState.fk=1;
				LOG.info("更改后60机状态："+this.agvAndDeviceState.isPilerUse);
				break;
			case "2110":
				LOG.info("60机状态："+this.agvAndDeviceState.isPilerUse);
				if(this.agvAndDeviceState.isPilerUse == false && this.agvAndDeviceState.fk>0){
				// AGV发出撤离信号
				addAgvOrders("停止");
				addAgvOrders("脱离堆垛机");

				addAgvOrders("回原点");
				this.agvAndDeviceState.fk=0;
				this.agvAndDeviceState.setAgvUse(false);

				}

				LOG.info("更改后60机2110状态："+this.agvAndDeviceState.isPilerUse);
				LOG.info("agv状态："+this.agvAndDeviceState.isAgvUse);

				/*deviceData.put("DeviceNum", "20");
				deviceData.put("Value", "2000");
				addAgvMissions(ctx,deviceData.toString());*/
				break;
			}
			break;

		case "20":// 组装机
			switch (Value) {
			case "2000":
				LOG.info("20机，2000命令：AGV状态是："+this.agvAndDeviceState.isAgvUse());
				if (this.agvAndDeviceState.isAgvUse() == false) {
					this.agvAndDeviceState.setAgvUse(true);
					printLog("组装机请求上料");
					deviceInputMaterial("getMaterial");
				}
				break;

			case "2001":
				// AGV开始出料
				addAgvOrders("滚筒一直右转");
				break;

			case "2002":
				LOG.info("20机，2002，ku:"+this.agvAndDeviceState.ku);
				if(this.agvAndDeviceState.isAgvUse == true &&  this.agvAndDeviceState.ku>0){
					LOG.info("进入一次");
				// AGV停止出料
				agvHelper.addOrders(agvHelper.getGuid("停止"));
				this.agvAndDeviceState.tp=1;
				// AGV发出撤离信号
				sendToClient("20", "22", "2002");

				addAgvOrders("脱离半成品进料口");
				this.agvAndDeviceState.ku=0;
				LOG.info("脱离进料口ku"+this.agvAndDeviceState.ku);
				this.agvAndDeviceState.setPilerUse(false);
				// 解除AGV锁定状态
				this.agvAndDeviceState.setAgvUse(false);
				this.agvAndDeviceState.setCount(0);
				}
				this.agvAndDeviceState.setPilerUse(false);
				break;
			case "2100":
				LOG.info("20机，2100请求出料命令：AGV状态是："+this.agvAndDeviceState.isAgvUse());
				if (this.agvAndDeviceState.isAgvUse() == false) {
					this.agvAndDeviceState.setAgvUse(true);
					printLog("组装机请求出空框");
					deviceOutputMaterial("putBasket");
				}
				break;

			case "2101":
				printLog("组装机收到AGV撤离信号...");
				if(this.agvAndDeviceState.tp>0){
					this.agvAndDeviceState.setCount(0);
					this.agvAndDeviceState.tp=0;
				}
				LOG.info("组装机接空框，AGV撤离，count为："+this.agvAndDeviceState.getCount());
				break;
			}
			break;

		case "40":// 成品机
			switch (Value) {
			case "2000":
				LOG.info("40机，2000命令：AGV状态是："+this.agvAndDeviceState.isAgvUse());
				if (this.agvAndDeviceState.isAgvUse() == false) {
					this.agvAndDeviceState.setAgvUse(true);
					printLog("成品机请求上料");
					deviceInputMaterial("getBasket");
				}
				break;


			case "2001":
				if(this.agvAndDeviceState.getProductCount()>0){

				sendToClient("40", "22", "2001");
				// AGV停止出料
				agvHelper.addOrders(agvHelper.getGuid("停止"));

				/*// AGV发出撤离信号
				sendToClient("40", "22", "2002");*/

				addAgvOrders("脱离成品进出料口");
				addAgvOrders("回原点");
				// 解除AGV锁定状态
				this.agvAndDeviceState.setAgvUse(false);
				this.agvAndDeviceState.setProductCount(0);
				}
				break;

			case "2100":
				LOG.info("40机，2100命令：AGV状态是："+this.agvAndDeviceState.isAgvUse());
				if (this.agvAndDeviceState.isAgvUse() == false) {
					this.agvAndDeviceState.setAgvUse(true);
					printLog("成品机请求出料");
					deviceOutputMaterial("putProduct");
				}
				break;

			case "2101":
				// AGV发送完成收料信号
				LOG.info("40机，2101修改putNew之前为："+this.agvAndDeviceState.getPutNewProduct());
				if(this.agvAndDeviceState.isAgvUse==true && this.agvAndDeviceState.getPutNewProduct()>0){
				sendToClient(DeviceNum, "22", "2101");
				printLog("成品机收到AGV撤离信号...");

				// AGV小车脱离成品出料口
				addAgvOrders("脱离成品进出料口");

				addAgvOrders("前往堆垛机");

				this.agvAndDeviceState.setPutNewProduct(0);
				LOG.info("40机，2101修改putNew后为："+this.agvAndDeviceState.getPutNewProduct());
				}
				break;
			}
			break;
		}
	}

	/**
	 * 给设备发送指令（NettyServer使用）
	 *
	 * @param DeviceNum
	 * @param PLCAddress
	 * @param Value
	 * @throws JSONException
	 */
	@SuppressWarnings("unused")
	public void sendToClient(String DeviceNum, String PLCAddress, String Value) throws JSONException {

			/*PrintWriter out= null;
			try {
				out = new PrintWriter(socket.getOutputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			JSONObject json = new JSONObject();

			json.put("DeviceNum", DeviceNum);
			json.put("PLCAddress", PLCAddress);
			json.put("Value", Value);
			//out.print(json.toString());
			LOG.info("给设备发送的数据:"+json.toString());
			//out.flush();

			ByteBuf result = Unpooled.copiedBuffer(json.toString().getBytes());
			ctx.write(result);
			ctx.flush();
	}

	/**
	 * 设备请求进料
	 */
	public void deviceInputMaterial(String type) {
		String sql = "";
		List<Object> list = new ArrayList<>();
		List<AgvModle> agvList = null;
		DBHelper db = new DBHelper();

		// 查询堆垛机状态，空闲时发送坐标
		printLog("正在查询堆垛机状态。。。");
		while (true) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//if (this.agvAndDeviceState.isPilerUse() == false) {
				printLog("堆垛机当前待命，可以使用，正在发送坐标。。。");
				break;
			//}
		}

		switch (type) {
		case "getMaterial":
			list.add("RAW");
			list.add("Y");
			break;

		case "getBasket":
			list.add("BA");
			list.add("Y");
			break;
		}

		sql = "select wh_name,wh_row, wh_column, wh_type, wh_flag,wh_id from wh_demo t where t.wh_type=? and wh_flag=? order by t.wh_row asc";
		String column = "";
		String row = "";
		try {

			// 查询立库信息，有物料则给堆垛机发送立库坐标,没有则结束本次请求（程序解除任务锁定状态，isAgvUse值改为false）
			agvList = (List<AgvModle>) db.executeQuery(sql, list, AgvModle.class);
			if (agvList.size() <= 0) {
				printLog("当前立库没有空框或物料，AGV小车放弃本次任务。。。");
				this.agvAndDeviceState.setAgvUse(false);
				return;
			}
			if (agvList.size() > 0) {
				switch (type) {
				case "getMaterial":
					// 取半成品位置坐标
					// 列
					column = "1";
					sendToClient("60", "30", column);

					// 行
					row = agvList.get(0).getWhRow().toString();
					sendToClient("60", "32", row);
					Thread.sleep(300);
					sendToClient("60", "32", row);
					LOG.info("取半成品的行："+row);
					this.agvAndDeviceState.ku=1;
					LOG.info("取半成品修改ku:"+this.agvAndDeviceState.ku);
					break;

				case "getBasket":
					// 取空框放置坐标
					// 列
					column = "2";
					sendToClient("60", "30", column);

					// 行
					Thread.sleep(200);
					row = agvList.get(0).getWhRow().toString();
					sendToClient("60", "32", row);
					LOG.info("取空框的行："+row);
					break;
				}
				printLog("发送"+type+"坐标,列为:" + column + ",行为:" + row);

				//发送2004给堆垛机
				sendToClient("60", "22", "2004");

				// 堆垛机状态更新为忙碌状态
				this.agvAndDeviceState.setPilerUse(true);
				printLog("堆垛机进入工作状态。。。");

				// 通知AGV小车到堆垛机接料
				addAgvOrders("前往堆垛机");

				// 给堆垛机发送AGV小车到位信号
				sendToClient("60", "22", "2005");

				// 滚筒开始滚动，左进料
				addAgvOrders("左进料");

				// AGV发送收料完成信号，通知堆垛机停止出料
				sendToClient("60", "22", "2006");

				addAgvOrders("脱离堆垛机");

				String orderName = "";
				String deviceType = "";

				switch (type) {
				case "getMaterial":
					// AGV前往半成品进料口
					deviceType = "20";
					orderName = "前往半成品进料口";
					break;

				case "getBasket":
					// AGV前往成品机出料口
					deviceType = "40";
					orderName = "前往成品进出料口";
					this.agvAndDeviceState.setProductCount(1);
					LOG.info("前往成品出料口cout："+this.agvAndDeviceState.getProductCount());
					break;
				}

				addAgvOrders(orderName);
				// 通知设备进料
				sendToClient(deviceType, "22", "2000");
				this.agvAndDeviceState.setPilerUse(true);
				if(deviceType.equals("40")){
					addAgvOrders("滚筒一直右转");

					LOG.info("60机状态："+this.agvAndDeviceState.isPilerUse());
				}
				// 更新立库坐标
				list.clear();
				list.add(agvList.get(0).getWhId());

				switch (type) {
				case "getMaterial":
					sql = "update wh_demo set wh_flag='N' where wh_id=?";
					System.out.println(list);
					break;

				case "getBasket":
					sql = "update wh_demo set wh_flag='N' where wh_id=?";
					System.out.println(list);
					break;
				}

				db.excuteUpdate(sql, list);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 设备请求出料
	 */
	public void deviceOutputMaterial(String type) {
		String sql = "";
		List<Object> list = new ArrayList<>();
		List<AgvModle> agvList = null;
		DBHelper db = new DBHelper();

		// 查询堆垛机状态，空闲时发送坐标
		printLog("正在查询堆垛机状态。。。");
		while (true) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			//if (this.agvAndDeviceState.isPilerUse() == false) {
				printLog("堆垛机当前待命，可以使用，正在发送坐标。。。");
				break;
			//}
		}

		// 查询立库信息，立库有空位则给堆垛机发送立库坐标,没有则结束本次请求（程序解除AGV锁定状态，isAgvUse值改为false）
		switch (type) {
		case "putBasket": //空框
			list.add("BA");
			list.add("N");
			break;

		case "putProduct":
			list.add("FG"); //成品
			list.add("N");
			break;
		}

		sql = "select wh_name,wh_row, wh_column, wh_type, wh_flag,wh_id from wh_demo t where t.wh_type=? and wh_flag=? order by t.wh_row asc";
		String column = "";
		String row = "";

		try {
			agvList = (List<AgvModle>) db.executeQuery(sql, list, AgvModle.class);
			if (agvList.size() <= 0) {
				printLog("当前立库没有空位，AGV小车放弃本次任务。。。");
				// 解除AGV锁定状态
				this.agvAndDeviceState.setAgvUse(false);
				return;
			}
			if (agvList.size() > 0) {
				switch (type) {
				case "putBasket":
					// 放空框放置坐标
					// 列
					//this.agvAndDeviceState.ku=1;
					LOG.info("放空ku:"+this.agvAndDeviceState.ku);
					column = "2";
					sendToClient("60", "30", column);

					this.agvAndDeviceState.fk=1;
					Thread.sleep(200);
					// 行
					row = agvList.get(0).getWhRow().toString();
					sendToClient("60", "32", row);
					LOG.info("放空框的行："+row);
					this.agvAndDeviceState.setCount(1);
					break;

				case "putProduct":
					// 放成品放置坐标
					// 列
					column = "3";
					sendToClient("60", "30", column);
					Thread.sleep(200);
					// 行
					row = agvList.get(0).getWhRow().toString();
					sendToClient("60", "32", row);
					LOG.info("放成品的行："+row);
					break;
				}
				printLog("发送"+type+"坐标："+"列：" + column + ",行：" + row);

				// 堆垛机状态更新为忙碌状态
				//this.agvAndDeviceState.setPilerUse(true);
				printLog("堆垛机进入工作状态。。。");

				// AGV前往设备出料口，到位之后滚动滚筒并同时给设备（编号40 or 20）发出信号2100
				// AGV收到物料之后停止滚动滚筒，撤离设备出料口，同时向设备（编号40 or 20）发送完成收料信号2101
				if (type.equals("putBasket")) {
					addAgvOrders("前往半成品出料口");

					// 小车到位，通知设备出料
					sendToClient(DeviceNum, "26", "2100");

					// AGV滚动滚筒接料
					addAgvOrders("右进料");

					// AGV发送完成收料信号
					sendToClient(DeviceNum, "26", "2101");

					// AGV小车脱离半成品出料口
					addAgvOrders("脱离半成品出料口");

					this.agvAndDeviceState.setPilerUse(true);
				}
				if (type.equals("putProduct")) {
					addAgvOrders("前往成品进出料口");

					// 小车到位，通知设备出料
					sendToClient(DeviceNum, "22", "2100");

					// AGV滚动滚筒接料
					addAgvOrders("右进料");

					this.agvAndDeviceState.setPilerUse(false);
					this.agvAndDeviceState.setPutNewProduct(1);

				}

				// AGV向堆垛机（60）发送到位信号
				sendToClient("60", "22", "2100");
				// AGV小车前往堆垛机
				if(type.equals("putBasket")){
				addAgvOrders("前往堆垛机");
				}
				// 更新立库坐标
				list.clear();
				list.add(agvList.get(0).getWhId());
				switch (type) {
				case "putBasket":
					sql = "update wh_demo set wh_flag='Y' where wh_id=?";
					break;

				case "putProduct":
					sql = "update wh_demo set wh_flag='Y' where wh_id=?";
					break;
				}

				db.excuteUpdate(sql, list);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
