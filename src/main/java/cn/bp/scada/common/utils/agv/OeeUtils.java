package cn.bp.scada.common.utils.agv;

import org.json.JSONObject;
import org.springframework.stereotype.Component;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


@Component
public class OeeUtils {

	/**
	 * post请求
	 * @param url 接口路径
	 * @param json	传送的json数据
	 * @return
	 */
	public  String doPostToOee(String url,JSONObject json){
		         HttpURLConnection connection = null;
		         OutputStream dataout = null;
		         BufferedReader reader = null;
		         String line = null;
		         StringBuilder result = new StringBuilder();
		         try {
		             URL urls = new URL(url);
		             connection = (HttpURLConnection) urls.openConnection();// 根据URL生成HttpURLConnection
		             connection.setDoOutput(true);// 设置是否向connection输出，因为这个是post请求，参数要放在http正文内，因此需要设为true,默认情况下是false
		             connection.setDoInput(true); // 设置是否从connection读入，默认情况下是true;
		             connection.setRequestMethod("POST");// 设置请求方式为post,默认GET请求
		             connection.setUseCaches(false);// post请求不能使用缓存设为false
		             connection.setConnectTimeout(5000);// 连接主机的超时时间
		             connection.setReadTimeout(5000);// 从主机读取数据的超时时间
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
					 reader.close();
					 connection.disconnect();
		         } catch (MalformedURLException e) {
					 // TODO Auto-generated catch block
					 e.printStackTrace();
		             System.out.println("请求异常");
		         } catch (UnsupportedEncodingException e){
					 // TODO Auto-generated catch block
					 e.printStackTrace();
					 System.out.println("请求异常");
				 } catch (IOException e){
		         	// TODO Auto-generated catch block
					 e.printStackTrace();
					 System.out.println("请求异常");
				 }
		         return result.toString();
	}

}
