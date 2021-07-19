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
public class SapOrderScanCode extends JdbcDaoSupport{
	@Resource
    public void setJb(JdbcTemplate jb) {
        super.setJdbcTemplate(jb);
    }

	@Autowired
	private SapConnUtils sapUtils;

	/**
	 * PC-SV改配订单扫码部件接口
	 * @return
	 */
	@RequestMapping("/sapOrderScanCode")
	public String orderScanCode(){
		System.out.println("进入:"+Thread.currentThread().getName());
		try {
		JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象
		JCoFunction function = sapUtils.getFunction("ZRFC_PC_SV_SMBJ");//获取到函数

		function.execute(jCoDestination);

		JCoTable  table = function.getTableParameterList().getTable("OUTPUT");//获取表
		if(table.getNumRows()>0){
			System.out.println(table.getString("WLDL"));//物料大类
		}else{
			System.out.println("返回为空");
		}
		}catch (JCoException e){
			System.out.println(e.toString());
			}
			return "ok";
	}
}
