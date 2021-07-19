package cn.bp.scada.common.utils.agv;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 与AGV通讯的工具类，可通过本类获取AGV的任务列表、任务状态以及添加任务的操作
 *
 * @author wuzhining
 *
 */
@Service
public class AgvHelper {
//获取AGV任务列表的接口地址
	@Value("${agv.getMissionsUrl}")
	private  String getMissionsUrl;

	// 添加AGV任务的接口地址
	@Value("${agv.addMissionUrl}")
	private  String addMissionUrl;

	// 授权码
	@Value("${agv.Authorization}")
	private  String Authorization;

	private HttpClient client;

	public AgvHelper() {
		try {
			InputStream is = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("application.properties");
			Properties p = new Properties();
			p.load(is);
			is.close();

			getMissionsUrl = p.getProperty("agv.getMissionsUrl");
			addMissionUrl = p.getProperty("agv.addMissionUrl");
			Authorization = p.getProperty("agv.Authorization");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 获取agv任务对应的guid
	 *
	 * @param name AGV预先设置好的任务名称
	 * @return 任务对应的guid
	 */
	public String getGuid(String name) {
		String guid = "";
		try {
			String orders = this.getOrders();
			JSONArray jsonArray = new JSONArray(orders.toString());

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = new JSONObject(jsonArray.get(i).toString());
				if (jsonObject.getString("name").equals(name)) {
					guid = jsonObject.get("guid").toString();
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return guid;
	}

	/**
	 * AGV添加需执行的任务
	 *
	 * @param guid 需执行任务的guid
	 * @return 一个json格式的字符串，包含刚刚添加的任务的执行id，通过这个id可以查询改任务的执行状态
	 */
	public String addOrders(String guid) {

		client = HttpClients.createDefault();
		HttpPost postRequest = new HttpPost(addMissionUrl);

		postRequest.setHeader("Authorization", Authorization);
		postRequest.setHeader("Content-Type", "application/json");

		JSONObject jsonObject = new JSONObject();
		//jsonObject.put("mission_id", guid);

		StringBuffer sBuffer = new StringBuffer();
		try {
			StringEntity formEntity = new StringEntity(jsonObject.toString());
			postRequest.setEntity(formEntity);

			HttpResponse response = client.execute(postRequest);
			HttpEntity entity = response.getEntity();

			InputStream inputStream = entity.getContent();
			InputStreamReader iReader = new InputStreamReader(inputStream, "UTF8");

			char[] buff = new char[1024];
			int length = 0;
			while ((length = iReader.read(buff)) != -1) {
				String x = new String(buff, 0, length);
				sBuffer.append(x);
			}

			iReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sBuffer.toString();
	}

	/**
	 * 获取agv任务列表
	 *
	 * @return AGV预先设定好的任务列表
	 */
	public String getOrders() {

		StringBuffer sBuffer = new StringBuffer();
		try {
			// 1、获取任务列表
			client = HttpClients.createDefault();
			HttpGet getRequest = new HttpGet(getMissionsUrl);
			getRequest.setHeader("Authorization", Authorization);

			HttpResponse response = client.execute(getRequest);
			HttpEntity getEntity = response.getEntity();

			InputStream inputStream = getEntity.getContent();
			InputStreamReader iReader = new InputStreamReader(inputStream, "UTF8");

			char[] buff = new char[1024];
			int length = 0;
			while ((length = iReader.read(buff)) != -1) {
				String x = new String(buff, 0, length);
				sBuffer.append(x);
			}

			iReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sBuffer.toString();
	}

	/**
	 * 获取任务状态
	 *
	 * @param id 任务的执行id，可通过方法 {@link addOrders} 的返回值得到这个id
	 * @return 一个json格式的字符串，包含任务的执行状态
	 */
	public String getOrderState(String id) {
		StringBuffer sBuffer = new StringBuffer();
		try {
			client = HttpClients.createDefault();
			HttpGet getRequest = new HttpGet(addMissionUrl + id);
			getRequest.setHeader("Authorization", Authorization);
			HttpResponse response = client.execute(getRequest);
			HttpEntity getEntity = response.getEntity();

			InputStream inputStream = getEntity.getContent();
			InputStreamReader iReader = new InputStreamReader(inputStream, "UTF8");

			char[] buff = new char[1024];
			int length = 0;
			while ((length = iReader.read(buff)) != -1) {
				String x = new String(buff, 0, length);
				sBuffer.append(x);
			}

			iReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return sBuffer.toString();
	}
}
