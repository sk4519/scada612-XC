package cn.bp.scada.mapper.scada;

import java.util.List;
import java.util.Map;


import cn.bp.scada.sap.bean.BatchMatInfo;
import cn.bp.scada.sap.bean.Material;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface BatchMat {

	/**
	 * 批量增加批次物料
	 * @param list
	 * @returns
	 */
	public int addBatchMat(List<BatchMatInfo> list);

	public List<String> seleExistsSn(Map<String,String> param);

	/**
	 * 批量增加到物料组
	 * @param li
	 * @return
	 */
	public int addBatchGrp(List<Material> list);

	List<Map<String,Object>> queryDeviceBg();

	int upDeviceBg(@Param("bgDt") String bgDt, @Param("stopSum") long stopSum,@Param("etCd") String etCd);
}
