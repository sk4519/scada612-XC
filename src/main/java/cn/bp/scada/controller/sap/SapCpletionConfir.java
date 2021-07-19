package cn.bp.scada.controller.sap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.util.JSONPObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoTable;

import cn.bp.scada.sap.SapConnUtils;

@Slf4j
@RestController
public class SapCpletionConfir extends JdbcDaoSupport{
	@Resource
    public void setJb(JdbcTemplate jb) {
        super.setJdbcTemplate(jb);
    }

	@Autowired
	private SapConnUtils sapUtils;

	@RequestMapping("/sapRepairSn")
	public JSONPObject repairSn(HttpServletRequest req, String data) {

		String name = req.getParameter("callbackparam");//获取到jsonp的函数名

		String result =cpletionConfir(data, "50");
		return new JSONPObject(name, result);
	}

	/**
	 * 完工确认接口
	 * @return
	 */
	@RequestMapping("/sapCpletionConfir")
	public String cpletionConfir(String sn,String code){
		String result = "OK";
		try{
			System.out.println("进入完工确认，按SN");
			JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象
			JCoFunction function = sapUtils.getFunction("ZRFC_AUFNR_BG");//获取到函数

			//入参
			JCoParameterList parameterList = function.getImportParameterList();
			parameterList.setValue("I_SERNR", "000000000"+sn); //序列号
			parameterList.setValue("I_VORNR", "00"+code); //活动编号（工序）
			parameterList.setValue("I_FLAG", ""); //是否检查档案（空不检查）
			function.execute(jCoDestination);

			//如果入参是内表形式，就以如下代码传入sap系统
			JCoTable  table = function.getTableParameterList().getTable("OUTPUT");//获取表
			log.info("完工确认出参："+table.getValue("SERNR"));
			log.info("完工确认出参："+table.getValue("VORNR"));
			log.info("完工确认出参："+table.getValue("FLAG"));
			log.info("完工确认出参："+table.getValue("MESSAGE"));
			result =table.getValue("MESSAGE").toString();
		}catch(Exception e){
			result = "报工失败";
			System.out.println("按SN完工确认失败");
		}

		return result;
	}

	/**
	 * 完工确认接口
	 * @return
	 */
	@RequestMapping("/sapCpletionConfirAll")
	public String cpletionConfirAll(String sn,String code,JCoFunction function,JCoDestination jCoDestination){
		String result = "OK";
		try{
			//入参
			JCoParameterList parameterList = function.getImportParameterList();
			parameterList.setValue("I_SERNR", "000000000"+sn); //序列号
			parameterList.setValue("I_VORNR", "00"+code); //活动编号（工序）
			parameterList.setValue("I_FLAG", ""); //是否检查档案（空不检查）
			function.execute(jCoDestination);

			//如果入参是内表形式，就以如下代码传入sap系统
			JCoTable  table = function.getTableParameterList().getTable("OUTPUT");//获取表
			log.info("完工确认出参："+table.getValue("SERNR"));
			log.info("完工确认出参："+table.getValue("VORNR"));
			log.info("完工确认出参："+table.getValue("FLAG"));
			log.info("完工确认出参："+table.getValue("MESSAGE"));
			result =table.getValue("MESSAGE").toString();
		}catch(Exception e){
			result = "操作失败";
		}

		return result;
	}
}
