package cn.bp.scada.controller.sap;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;

import cn.bp.scada.sap.SapConnUtils;

@RestController
public class SapInSingle extends JdbcDaoSupport{
	@Resource
    public void setJb(JdbcTemplate jb) {
        super.setJdbcTemplate(jb);
    }

	@Autowired
	private SapConnUtils sapUtils;

	/**
	 * 装箱单机器型号接口
	 * @return
	 */
	@RequestMapping("/SapInSingle")
	public String sapInSingle(){
		try{
			System.out.println("进入:"+Thread.currentThread().getName());
			JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象
			JCoFunction function = sapUtils.getFunction("ZRFC_MES_JQXH");//获取到函数
			function.execute(jCoDestination);

			//如果入参是内表形式，就以如下代码传入sap系统
			JCoTable  table = function.getTableParameterList().getTable("IT_JQXHZPTS");//获取表
			System.out.println(table.getNumRows());
			JCoTable  table1 = function.getTableParameterList().getTable("IT_JQXHJYJL");//获取表
			System.out.println(table1.getNumRows());
		}catch(Exception e){
			System.out.println(e.toString());
		}

		return "ok";
	}
}
