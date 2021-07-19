package cn.bp.scada.common.utils.agv;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.springframework.stereotype.Component;


@Component
public class AgvUtils {

	/**
	 * http get请求
	 * @param url
	 * @param charset
	 * @return
	 */
	 public static String doGetToAgv(String url) {
		 String result = "";
			HttpGet get = new HttpGet(url);
			try{
				CloseableHttpClient httpClient = HttpClients.createDefault();

				HttpResponse response = httpClient.execute(get);
				HttpEntity entity = response.getEntity();
				if(entity != null){
					InputStream in = entity.getContent();
					BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf-8"));
					StringBuilder strber= new StringBuilder();
					String line = null;
					while((line = br.readLine())!=null){
						strber.append(line+'\n');
					}
					br.close();
					in.close();
					result = strber.toString();
				}

				if(response.getStatusLine().getStatusCode()!=HttpStatus.SC_OK){
					result = "服务器异常";
				}
			} catch (Exception e){
				System.out.println("请求异常");
				throw new RuntimeException(e);
			} finally{
				get.abort();
			}
	        return result.toString();

	    }


	/**
	 * post请求
	 * @param url 接口路径
	 * @param json	传送的json数据
	 * @return
	 */
	public  String doPostToAgv(String url,JSONObject json){
		         HttpURLConnection connection = null;
		         OutputStream dataout = null;
		         BufferedReader reader = null;
		         String line = null;
		         StringBuilder result = new StringBuilder();
		         int p =0;
		         try {
		             URL urls = new URL(url);
		             connection = (HttpURLConnection) urls.openConnection();// 根据URL生成HttpURLConnection
		             connection.setDoOutput(true);// 设置是否向connection输出，因为这个是post请求，参数要放在http正文内，因此需要设为true,默认情况下是false
		             connection.setDoInput(true); // 设置是否从connection读入，默认情况下是true;
		             connection.setRequestMethod("POST");// 设置请求方式为post,默认GET请求
		             connection.setUseCaches(false);// post请求不能使用缓存设为false
		             connection.setConnectTimeout(10000);// 连接主机的超时时间
		             connection.setReadTimeout(10000);// 从主机读取数据的超时时间
		             connection.setInstanceFollowRedirects(true);// 设置该HttpURLConnection实例是否自动执行重定向
		             connection.setRequestProperty("connection", "Keep-Alive");// 连接复用
		             connection.setRequestProperty("charset", "utf-8");

		             connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");

		             dataout = new DataOutputStream(connection.getOutputStream());// 创建输入输出流,用于往连接里面输出携带的参数
		             dataout.write(json.toString().getBytes("UTF-8"));
		             dataout.flush();
		             dataout.close();

		             if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
		                 reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));// 发送http请求

		                 // 循环读取流
		                 while ((line = reader.readLine()) != null) {
		                     result.append(line).append(System.getProperty("line.separator"));//
		                 }
		             }
		         } catch (IOException e) {
		             System.out.println("请求异常");
		             p=1;
		         } finally {
		             try {
		            	 if(p == 0){
		            		 reader.close();
		            	 }

		             } catch (IOException e) {
		                 e.printStackTrace();
		             }
		             connection.disconnect();
		         }
		         return result.toString();
	}

}
