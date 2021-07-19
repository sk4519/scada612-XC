package cn.bp.scada.service.scada;

import org.json.JSONException;

import io.netty.channel.ChannelHandlerContext;

public interface ExpoAgvService {
	public void addAgvMissions(ChannelHandlerContext ctx, String data) throws JSONException;

	public void deviceOutputMaterial(String type);

	public void deviceInputMaterial(String type);

	public void sendToClient(String DeviceNum, String PLCAddress, String Value) throws JSONException;
}
