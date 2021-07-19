package cn.bp.scada.mapper.scada;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import cn.bp.scada.sap.bean.MesMoT;

@Mapper
public interface UpdaYieldWo {

	/**
	 * 批量修改工单
	 * @param wo
	 * @return
	 */
	public int updaBatchWo(List<MesMoT> wo);


	public List<Map> seleBom(Map<String, String> o);

	public void proBomBatch(List<Map> list);
}
