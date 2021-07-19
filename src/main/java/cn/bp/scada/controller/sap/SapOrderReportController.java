package cn.bp.scada.controller.sap;

import cn.bp.scada.sap.SapConnUtils;
import com.sap.conn.jco.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;


@Controller
public class SapOrderReportController extends JdbcDaoSupport{
	@Resource
    public void setJb(JdbcTemplate jb) {
        super.setJdbcTemplate(jb);
    }

	@Autowired
	private SapConnUtils sapUtils;

	/**
	 * 订单报工
	 * @return
	 */
	@RequestMapping("/sapReport")
/*	@Scheduled(fixedDelay = 5000) //定时任务，间隔执行，每5秒
	@Scheduled(cron = "0 0 10 ? * *") //cron表达式定时调用
*/	public String sapReport(){

		System.out.println("进入:"+Thread.currentThread().getName());
		try {
		JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象
		JCoFunction function = sapUtils.getFunction("ZRFC_ORDERCOMFIRM");//获取到函数
		//入参
		JCoParameterList parameterList = function.getImportParameterList();
	//设置入参
			parameterList.setValue(0,"000010290293"); //订单号
			parameterList.setValue(1,1); //数量
		function.execute(jCoDestination);

			//出参
			JCoParameterList list1 = function.getExportParameterList();
			System.out.println("出参:  "+list1.getString(0));//S   E
			System.out.println("出参:  "+list1.getString(1));//报工成功  没有查询到此单号
	}catch (JCoException e){
	System.out.println(e.toString());
	}
		return "vue";
	}
}

