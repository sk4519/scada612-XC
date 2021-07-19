package cn.bp.scada.service.scada;

import cn.bp.scada.modle.scada.ScadaRespon;

/**
 * 1.2SCADA系统与物料配送接泊的接口(自动线设备)
 * @author Administrator
 *
 */
public interface ScadaSortMat {
	/**
	 * 物料传送带与上位机交互
	 * @param msg
	 * @return
	 */
	public ScadaRespon checkData(Object msg) throws  Exception;

	/**
	 * 分拣区料箱扫描交互
	 * @param msg
	 * @return
	 */
	public ScadaRespon sortScan(Object msg) throws  Exception;
}
