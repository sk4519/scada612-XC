package cn.bp.scada.common.utils;

import java.net.ServerSocket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * socket服务类
 *
 * @author wuzhining
 *
 */
@Component
public class SocketServer {
	private final SocketServer INSTANCE = this;
	private ExecutorService clientSocketPool;//服务器与客户端通信的线程池
	private ExecutorService clientHandlerPool;//处理客户端信息的线程池
	private Logger LOG = LoggerFactory.getLogger(this.getClass());
	public static List<Map<String, Object>> clientSocketList;
	public ServerSocket server;
	public ExecutorService serverThreadExecutor;//socket服务器运行线程

	/*private SocketServer() {
		try {
			clientSocketList = new CopyOnWriteArrayList();
			clientHandlerPool = Executors.newFixedThreadPool(2000);

			serverThreadExecutor = Executors.newSingleThreadExecutor();
			serverThreadExecutor.execute(new Runnable() {

				@Override
				public void run() {
					startServer();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

	public SocketServer getInstance() {
		return INSTANCE;
	}

	/**
	 * 启动socket服务器
	 */
	/*public String startServer() {
		String ipAddress = null;
		String result=null;
		try {
			if (server == null) {
				server = new ServerSocket(10000);
			}
			result="服务器准备就绪";
			LOG.info(result);

			if (clientSocketPool == null) {
				clientSocketPool = Executors.newFixedThreadPool(2000);
			}

			while (true) {
				Socket localSocket = server.accept();
				Map<String, Object> localSocketClient = new HashMap();

				ipAddress = localSocket.getInetAddress().toString();

				localSocketClient.put("ip", ipAddress);
				localSocketClient.put("socket", localSocket);
				clientSocketList.add(localSocketClient);
				LOG.info(ipAddress + "用户上线了，当前在线客户端数为" + clientSocketList.size() + "个");

				clientSocketPool.execute(new Runnable() {

					@Override
					public void run() {
						serverReceiving(localSocket);
					}
				});
			}

		} catch (IOException e) {
			result="socket服务器启动失败";
			LOG.error(result);
			e.printStackTrace();
		}
		return result;
	}*/

	/**
	 * 接收客户端数据方法
	 *
	 * @param socket
	 */
	/*private void serverReceiving(Socket socket) {
		String ip = socket.getInetAddress().toString();

		try {
			InputStream inputStream = socket.getInputStream();
			byte[] buff = new byte[1024];
			int len = -1;

			while ((len = inputStream.read(buff)) != -1) {
				String x = new String(buff, 0, len, "UTF8").intern();

				clientHandlerPool.execute(new Runnable() {

					@Override
					public void run() {
						LOG.info("收到客户端：" + ip + " 数据：" + x);
						JSONObject jsonObject = null;
						try {
							jsonObject = new JSONObject(x);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try {
							new ExpoAgvServiceImpl(socket).addAgvMissions(jsonObject);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}

			for (Map<String, Object> map : clientSocketList) {
				if (map.get("socket") == socket) {
					clientSocketList.remove(map);
					break;
				}
			}

			inputStream.close();
			LOG.error(ip + "用户已经下线，当前客户端数量" + clientSocketList.size() + "个");
		} catch (Exception e) {
			LOG.error("接收客户端数据时发生错误");
		}
	}*/
}
