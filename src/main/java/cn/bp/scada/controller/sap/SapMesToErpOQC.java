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
import com.sap.conn.jco.JCoTable;

import cn.bp.scada.sap.SapConnUtils;

@RestController
public class SapMesToErpOQC extends JdbcDaoSupport{
	@Resource
    public void setJb(JdbcTemplate jb) {
        super.setJdbcTemplate(jb);
    }

	@Autowired
	private SapConnUtils sapUtils;

	/**
	 * MES触发ERP做OQC质检放行功能(改配订单BOM获取)
	 * @return
	 */
	@RequestMapping("/sapMesToErpOQC")
	public String mesToErpOQC(){

		System.out.println("进入:"+Thread.currentThread().getName());
		try {
		JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象
		JCoFunction function = sapUtils.getFunction("ZMES_OQC");//获取到函数

		//入参
				JCoParameterList parameterList = function.getImportParameterList();
			//设置入参
				/*parameterList.setValue("BUDAT", "1234"); //凭证中的过帐日期
				parameterList.setValue("AUFNR", "3333"); //订单号
				parameterList.setValue("VCODE", "3333"); //
*/
			JCoTable  table = function.getTableParameterList().getTable("INPUT");//获取表
			System.out.println(table.getNumRows());
			if(table.getNumRows()>0) {

			}
		function.execute(jCoDestination);
		}catch (JCoException e){
			System.out.println(e.toString());
			}
		return "ok";
	}

}
