package cn.bp.scada.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SocketClient {
	private static final SocketClient INSTANCE = new SocketClient();

	@Value("${socket.ipAddress}")
	private String ipAddress;

	@Value("${socket.client.port}")
	private int port;

	public Socket socket;

	ExecutorService fixedThreadPool;

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	private SocketClient() {
//		try {
//			startClient();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	public static SocketClient getInstance() {
		return INSTANCE;
	}

	/**
	 * 启动socket客户端
	 *
	 * @return
	 */
	public String startClient() {
		String msg = "socket客户端已启动！";
		if (socket == null) {
			try {
				socket = new Socket(ipAddress, port);
				new RecvThread().start();
				msg = "成功启动socket客户端！";
			} catch (IOException e) {
				e.printStackTrace();
				msg = "建立socket连接时发生错误！";
			}
		}
		LOG.info(msg);
		return msg;
	}

	/**
	 * 客户端接收数据线程
	 *
	 * @author Administrator
	 *
	 */
	private class RecvThread extends Thread {
		@Override
		public void run() {
			try {
				InputStream iReader = socket.getInputStream();
				fixedThreadPool = Executors.newFixedThreadPool(200);
				byte[] buff = new byte[1024];
				int len = -1;
				while ((len = iReader.read(buff)) != -1) {
					String x = new String(buff, 0, len, "UTF8");
					LOG.info("收到服务端数据：" + x);

					fixedThreadPool.execute(new Runnable() {
						@Override
						public void run() {
							try {
								JSONObject jsonObject = new JSONObject();
								//new ExpoAgvServiceImpl().addAgvMissions(jsonObject);
							} catch (Exception e) {
								LOG.error("转化数据时发生错误！");
							}
						}
					});
				}
				iReader.close();
				socket = null;
				LOG.error("与服务器断开连接！");
			} catch (Exception e) {
				socket = null;
				LOG.error("与服务器断开连接！");
				e.printStackTrace();
			}
		}
	}
}
