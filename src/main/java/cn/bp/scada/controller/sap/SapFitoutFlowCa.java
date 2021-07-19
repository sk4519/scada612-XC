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
public class SapFitoutFlowCa extends JdbcDaoSupport{
	@Resource
    public void setJb(JdbcTemplate jb) {
        super.setJdbcTemplate(jb);
    }

	@Autowired
	private SapConnUtils sapUtils;

	/**
	 * 装配流程卡接口
	 * @return
	 */
	@RequestMapping("/sapFitoutFlowCa")
	public String fitoutFlowCa(){
		System.out.println("进入:"+Thread.currentThread().getName());
		try {
		JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象
		JCoFunction function = sapUtils.getFunction("ZRFC_MES_LCK");//获取到函数

		//入参
				JCoParameterList parameterList = function.getImportParameterList();
			//设置入参
				parameterList.setValue("P_AUFNR", "10290689"); //订单号
				/*parameterList.setValue("P_JQXH", "3333"); //机器型号
*/
				function.execute(jCoDestination);

			JCoTable  table = function.getTableParameterList().getTable("IT_LCKA");//获取表
			if(table.getNumRows() >0) {
				for (int i = 0; i < table.getNumRows(); i++) {
					table.setRow(i);
					System.out.println("生产单号："+table.getValue("SCDH"));
					System.out.println("序列号 ："+table.getValue("XLH"));
					System.out.println("序号 ："+table.getValue("XH"));
					System.out.println("描述 ："+table.getValue("CPXH"));
				}
			}

			JCoTable  table1 = function.getTableParameterList().getTable("IT_JYJL");//获取表
			System.out.println("------------------------------");
			if(table1.getNumRows() >0) {
				for (int i = 0; i < table1.getNumRows(); i++) {
					table.setRow(i);
				System.out.println("装箱清单打印机器型号 :"+table1.getValue("JQXH"));
				System.out.println("序号 :"+table1.getValue("XH"));
				System.out.println("描述 :"+table1.getValue("MSH"));
				System.out.println("物料大类 :"+table1.getValue("WLDL"));
				System.out.println("显示顺序:"+table1.getValue("XSSX"));
				System.out.println("是否有工艺路线 :"+table1.getValue("GYLX"));
				System.out.println("是否带出数量:"+table1.getValue("SL"));
				System.out.println("是否带出描述:"+table1.getValue("DCMS"));
				}
			}
		function.execute(jCoDestination);
		}catch (JCoException e){
			System.out.println(e.toString());
			}
			return "ok";
	}
}
