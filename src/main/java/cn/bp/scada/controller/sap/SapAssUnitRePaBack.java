package cn.bp.scada.controller.sap;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;

import cn.bp.scada.sap.SapConnUtils;

@RestController
public class SapAssUnitRePaBack extends JdbcDaoSupport{
	@Resource
    public void setJb(JdbcTemplate jb) {
        super.setJdbcTemplate(jb);
    }

	@Autowired
	private SapConnUtils sapUtils;

	/**
	 * 部件不良率回传ERP
	 * @return
	 */
	@RequestMapping("/sapAssUnitRePaBack")
	public String assUnitRePaBack(){
		System.out.println("进入:"+Thread.currentThread().getName());
		try {
		JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象
		JCoFunction function = sapUtils.getFunction("ZRFC_UPD_ZMM_BENCHMARK");//获取到函数
		JCoParameterList parameterList = function.getImportParameterList();
		//设置入参
		parameterList.setValue("MATNR", "");  //物料编号
		parameterList.setValue("WERKS", "");  //工厂
		parameterList.setValue("BENCHMARK", "");  //标杆值
		parameterList.setValue("DEFRATE", "");  //不良率
		parameterList.setValue("ZDATE", "");  //更新日期

		//执行函数
	    function.execute(jCoDestination);
	    JCoParameterList exportParameterList = function.getExportParameterList();

	    	System.out.println(exportParameterList.getString("FLAG"));
	    	System.out.println(exportParameterList.getString("MSG"));
	    	System.out.println(exportParameterList.getString("COUNT"));

		}catch (JCoException e){
			System.out.println(e.toString());
			}
		return "ok";
	}
}
