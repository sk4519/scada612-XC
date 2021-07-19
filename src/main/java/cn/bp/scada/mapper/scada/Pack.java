package cn.bp.scada.mapper.scada;

import cn.bp.scada.sap.bean.BatchMatInfo;
import cn.bp.scada.sap.bean.Material;
import org.apache.ibatis.annotations.Mapper;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public interface Pack {

	/**
	 * 新增包装打印缓存表
	 * @param param
	 * @return
	 */
	public int insertPack(Map<String, Object> param);

	/**
	 * 批量修改载具绑定表 PASS的数据
	 * @param list
	 * @return
	 */
	public int updateBatchBind(List<Map<String, Object>> list) ;

	/**
	 * 批量修改载具绑定表 Fail的数据
	 * @param list
	 * @return
	 */
	public int updateBatchBindFail(List<Map<String, Object>> list) ;

	/**
	 * byName
	 * @param etNm
	 * @return
	 */
	String queryDevice(String etNm);

	/**
	 * byName query 点击类别编号
	 * @param CheckType
	 * @return
	 */
	String queryCheckType(String CheckType);

	/**
	 * 批量插入点检项目
	 * @param list
	 * @return
	 */
	int insertCheck(ArrayList<Map> list);

	List<String> seleCKCD(List<String> ckList);
}
