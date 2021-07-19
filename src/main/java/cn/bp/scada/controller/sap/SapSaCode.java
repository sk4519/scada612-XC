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
import com.sap.conn.jco.JCoTable;

import cn.bp.scada.sap.SapConnUtils;

@RestController
public class SapSaCode extends JdbcDaoSupport{
	@Resource
    public void setJb(JdbcTemplate jb) {
        super.setJdbcTemplate(jb);
    }

	@Autowired
	private SapConnUtils sapUtils;

	/**
	 * SV扫码部件接口
	 * @return
	 */
	@RequestMapping("/sapSaCode")
	public String SaCode(){
		System.out.println("进入:"+Thread.currentThread().getName());
		try {
		JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象
		JCoFunction function = sapUtils.getFunction("ZRFC_MES_SMBJ");//获取到函数
		//执行函数
	    function.execute(jCoDestination);

	    JCoTable  table = function.getTableParameterList().getTable("IT_SMBJ");//获取表
	    if(table.getNumRows()>0){
	    	  System.out.println(table.getString("WLDL"));
	    	  System.out.println(table.getString("SFSM")); //是否要求扫码(1是2否)
	    }
	  System.out.println(table);
		}catch (JCoException e){
			System.out.println(e.toString());
			}
		return "ok";
	}
}
