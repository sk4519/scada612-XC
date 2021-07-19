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
public class SapInstList extends JdbcDaoSupport{
	@Resource
    public void setJb(JdbcTemplate jb) {
        super.setJdbcTemplate(jb);
    }

	@Autowired
	private SapConnUtils sapUtils;

	/**
	 * 装箱清单接口
	 * @return
	 */
	@RequestMapping("/sapInstList")
	public String instList(){
		System.out.println("进入:"+Thread.currentThread().getName());
		try {
		JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象
		JCoFunction function = sapUtils.getFunction("ZRFC_ZXDP1");//获取到函数

		//入参
				JCoParameterList parameterList = function.getImportParameterList();
			//设置入参
				parameterList.setValue("P_AUFNR", "1234"); //订单号

			JCoTable  table = function.getTableParameterList().getTable("TBL_DETAIL");//获取表
			System.out.println(table.getNumRows());

		function.execute(jCoDestination);
		}catch (JCoException e){
			System.out.println(e.toString());
			}
			return "ok";
	}
}
