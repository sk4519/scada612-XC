package cn.bp.scada.service.scada;

import java.io.IOException;

import cn.bp.scada.modle.scada.ScadaResponShow;
import org.json.JSONException;

import cn.bp.scada.modle.scada.ScadaRespon;


/**
 * 1.3 SCADA系统与AGV配送的交互接口(自动线设备)
 * @author Administrator
 *
 */
public interface ScadaToAgvSend {

	/**
	 * 物料料箱到达下料点,上位机调scada
	 * @param msg
	 * @return
	 */
	public ScadaResponShow matBoxDown(Object msg) throws JSONException, IOException;


}
