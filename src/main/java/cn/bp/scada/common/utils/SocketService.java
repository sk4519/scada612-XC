/*package cn.bp.scada.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

import javax.annotation.Resource;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cn.bp.scada.common.utils.SocketClient;
import cn.bp.scada.common.utils.SocketServer;

*//**
 * socket服务类
 *//*
@Service
public class SocketService {
//	private SocketUtils socketUtil = SocketUtils.INSTANCE;
	@Resource
	private SocketClient socketClient;
	@Resource
	private SocketServer socketServer;
	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	*//**
	 * 启动socket服务端
	 *
	 * @return
	 *//*
	public String startSocketServer() {
//		socketServer.startServer();
		return socketServer.startServer();
	}

	*//**
	 * socket服务端发送数据至连接到本服务器的socket客户端
	 *
	 * @param msg
	 * @return
	 *//*
	public String sendToClient(String msg) {
		String result="数据已发送！";
		if (socketServer.clientSocketList.size()>0) {
			for (Map<String, Object> client : socketServer.clientSocketList) {
				Socket socket=(Socket) client.get("socket");
				try {
					PrintWriter out = new PrintWriter(socket.getOutputStream());
					out.print(msg);
					out.flush();
				} catch (IOException e) {
					result="发送数据时发生错误！";
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	*//**
	 * socket客户端发送数据至服务端
	 *
	 * @param msg
	 * @return
	 *//*
	public String sendToServer(String msg) {
		String result = "";
		try {
			if (socketClient.socket == null) {
				result = "客户端未启动！";
				return result;
			}
			PrintWriter out = new PrintWriter(socketClient.socket.getOutputStream());
			JSONObject json = new JSONObject(msg);

			out.print(json.toString());
			out.flush();

			result = "客户端发送数据：" + json.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOG.info(result);
		return "PLC指令已发出";
	}

	*//**
	 * 启动客户端
	 *
	 * @return
	 *//*
	public String startClient() {
		return socketClient.startClient();
	}

}
*/