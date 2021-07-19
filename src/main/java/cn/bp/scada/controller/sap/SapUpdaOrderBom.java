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
public class SapUpdaOrderBom extends JdbcDaoSupport{
	@Resource
    public void setJb(JdbcTemplate jb) {
        super.setJdbcTemplate(jb);
    }

	@Autowired
	private SapConnUtils sapUtils;

	/**
	 * 改配订单BOM获取
	 * @return
	 */
	@RequestMapping("/sapUpdaOrderBomllll")
	public String updaOrderBom(){

		System.out.println("进入:"+Thread.currentThread().getName());
		try {
		JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象
		JCoFunction function = sapUtils.getFunction("ZRFC_ZPP97");//获取到函数

		//入参
				JCoParameterList parameterList = function.getImportParameterList();
			//设置入参
				parameterList.setValue("P_AUFNR", "000010481310"); //订单号 1
				/*parameterList.setValue("P_AUFNR2", "3333"); //订单号2
				parameterList.setValue("P_SERNR", "3333"); //序列号1
				parameterList.setValue("P_SERNR2", "3333"); //序列号2
*/
				function.execute(jCoDestination);

			JCoTable  table = function.getTableParameterList().getTable("LT_RETURN");//获取表
			System.out.println(table.getNumRows());
			if(table.getNumRows()>0) {
				for (int i = 0; i < table.getNumRows(); i++) {
				table.setRow(i);
				System.out.println("物料编号：" +table.getValue("MATNR"));
				System.out.println("物料描述：" +table.getValue("MAKTX"));
				System.out.println("需求量：" +table.getValue("MENGE1"));
				System.out.println("基本计量单位：" +table.getValue("MEINS1"));
				}
			}

		}catch (JCoException e){
			System.out.println(e.toString());
			}
		return "ok";
	}
}
