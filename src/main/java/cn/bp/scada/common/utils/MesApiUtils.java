package cn.bp.scada.common.utils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import com.squareup.okhttp.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 连接德富莱原云平台项目api的工具类
 *
 * @author wuzhining
 *
 */
@Component
public class MesApiUtils {
	@Value("${mes.url}")
	private String mesUrl;
	private Logger LOG = LoggerFactory.getLogger(this.getClass());
	/**
	 *
	 * @param reqDataDetail reqStr={ "REQ": [{"REQ_DATA":{"APP_NAMES":"BP_PDA","IFS":"S00001","reqType":"APP"}}]}，
	 * 					reqDataDetail的值是key“REQ_DATA”对应的值
	 * @return mes系统返回的操作结果集 例：{"MESSAGE":"Z01,该工序流程验证错误!","STATUS":"0"}
	 * @throws IOException
	 * @throws JSONException
	 */
	public JSONObject doPost(JSONObject reqDataDetail) throws IOException, JSONException {
		System.out.println(Thread.currentThread().getName());
		reqDataDetail.put("reqType", "APP");

		JSONObject reqData = new JSONObject();
		reqData.put("REQ_DATA", reqDataDetail);

		JSONArray reqDataArray = new JSONArray();
		reqDataArray.put(reqData);

		JSONObject requestJson = new JSONObject();
		requestJson.put("REQ", reqDataArray);


		RequestBody formBody = new FormEncodingBuilder()//
				.add("reqStr", requestJson.toString())//
				.build();//

		Request request = new Request.Builder()//
				.url(mesUrl)//
				.post(formBody)//
				.build();//

		OkHttpClient client = new OkHttpClient();
		client.setConnectTimeout(30, TimeUnit.SECONDS);
		client.setReadTimeout(35, TimeUnit.SECONDS);
		client.setWriteTimeout(20, TimeUnit.SECONDS);

		Response response = client.newCall(request).execute();

		if (!response.isSuccessful()) {
			throw new IOException("服务器端错误: " + response);
		}

		String result = response.body().string();



		JSONObject resultJson = new JSONObject(result);
		JSONObject responseData = resultJson.getJSONArray("RESPONSE").getJSONObject(0).getJSONArray("RESPONSE_DATA")
				.getJSONObject(0);
		LOG.info("mes的返回："+responseData.toString());
		return responseData;
	}

	//产品档案请求MES返回多条数据
	public JSONArray doPost2(JSONObject reqDataDetail) throws IOException, JSONException {
		reqDataDetail.put("reqType", "APP");

		JSONObject reqData = new JSONObject();
		reqData.put("REQ_DATA", reqDataDetail);

		JSONArray reqDataArray = new JSONArray();
		reqDataArray.put(reqData);

		JSONObject requestJson = new JSONObject();
		requestJson.put("REQ", reqDataArray);

		RequestBody formBody = new FormEncodingBuilder()//
				.add("reqStr", requestJson.toString())//
				.build();//

		Request request = new Request.Builder()//
				.url(mesUrl)//
				.post(formBody)//
				.build();//

		OkHttpClient client = new OkHttpClient();
		Response response = client.newCall(request).execute();

		if (!response.isSuccessful()) {
			throw new IOException("服务器端错误: " + response);
		}

		String result = response.body().string();


		JSONObject resultJson = new JSONObject(result);
		JSONArray jsonArray = resultJson.getJSONArray("RESPONSE").getJSONObject(0).getJSONArray("RESPONSE_DATA");
		LOG.info("mes的返回："+jsonArray.toString());
		return jsonArray;
	}

}



