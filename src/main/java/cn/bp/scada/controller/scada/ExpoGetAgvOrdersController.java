package cn.bp.scada.controller.scada;

import javax.annotation.Resource;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.bp.scada.common.utils.agv.AgvHelper;
/**
 * 此类没有使用
 * @author Administrator
 *
 */
@RestController
public class ExpoGetAgvOrdersController {
	@Resource
	private AgvHelper agvHelper;

	@GetMapping("/testAddOrders")
	public String getOrders(String name) throws JSONException {
		String guid = agvHelper.getGuid(name);
		String msg=agvHelper.addOrders(guid);
		JSONObject jsonObject =new JSONObject(msg);
		System.out.println(jsonObject.getString("id"));
		String id =jsonObject.getString("id");
		String staResult="";
		JSONObject jsonResult;
		while(true) {
			staResult=agvHelper.getOrderState(id);
			jsonResult=new JSONObject(staResult);
			if (jsonResult.get("state").equals("Done")) {
				System.out.println("任务已经完成。。。");
				return staResult;
			}
		}
	}
}
