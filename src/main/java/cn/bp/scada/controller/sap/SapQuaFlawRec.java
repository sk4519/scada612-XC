package cn.bp.scada.controller.sap;


import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoTable;

import cn.bp.scada.sap.JsonUtils;
import cn.bp.scada.sap.SapConnUtils;
import cn.bp.scada.sap.bean.QuaFlawRec;


@RestController
public class SapQuaFlawRec extends JdbcDaoSupport{
	@Resource
    public void setJb(JdbcTemplate jb) {
        super.setJdbcTemplate(jb);
    }

	@Autowired
	private SapConnUtils sapUtils;



	/**
	 * 质量缺陷记录
	 * @Param("data") 获取前台的JSON数组
	 * @return
	 * @throws JSONException
	 */
	@RequestMapping("/sapQuaFlawRec")
	public JSONPObject quaFlawRec(HttpServletRequest req,@RequestParam("data") String data) {
		System.out.println("进入:"+Thread.currentThread().getName());
		String result="失败";
		List<QuaFlawRec> jsonToList = new JsonUtils().jsonToList(data,QuaFlawRec.class);//将json数组转为list
		String name= req.getParameter("callbackparam");//获取到jsonp的函数名
		try {
		JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象
		JCoFunction function = sapUtils.getFunction("ZRFC_UPD_ZGZJL");//获取到函数
		JCoTable table = function.getTableParameterList().getTable("TBL_ZGZJL");//设置内表，返回数据给ERP
		if(jsonToList.size()>0){
		 for (int i = 0; i < jsonToList.size(); i++) {
			table.appendRow();
			table.setValue("PZH", "啊啦C吆");       //凭证号
			table.setValue("HXM", "");       //
			table.setValue("JYLY", "");      //检验来源
			table.setValue("QN", "");        //QN号
			table.setValue("SN", "jhjh");        //SN
			table.setValue("WERKS", jsonToList.get(i).getWerks());     //工厂
			table.setValue("PRODH", "");     //物料编号
			table.setValue("MAKTX", "");     //物料描述
			table.setValue("LIFNR", "");     //供应商或债权人的帐号
			table.setValue("QTMS", "");      //此对象的短文本/描述
			table.setValue("ZHMS", "");      //此对象的短文本/描述
			table.setValue("NAME1", "");     //名称
			table.setValue("CODEW", "");     //
			table.setValue("KURZTEXTW", ""); //此对象的短文本/描述
			table.setValue("CODEX", "");     //
			table.setValue("KURZTEXTX", ""); //此对象的短文本/描述
			table.setValue("QT", "");        //齐套描述
			table.setValue("GFSN", "");      //供方SN
			table.setValue("TJBM", "");      //退件单位/部门
			table.setValue("GZMS", "");      //故障描述
			table.setValue("BJLB", "");      //部件类别
			table.setValue("AUFNR", "");     //订单
			table.setValue("JYY", "");       //操作员
			table.setValue("VORNR", "");     //活动编号
			table.setValue("DATNM", "");     //交货日期
			table.setValue("MATKL", "");     //物料组
			table.setValue("SCBZ", "");      //删除标志（1-删除）
			table.setValue("TKYY", "");      //退库原因
			table.setValue("SFHG", "");      //是否合格
			table.setValue("FWDH", "");      //服务单号
			table.setValue("CJSN", "");      //备注
			table.setValue("SFFH", "");      //是否符合服务渠道描述
			table.setValue("SL", "");        //数量
			table.setValue("TEAM", "");      //责任班组
			table.setValue("ZJWR", "");      //借物人
			table.setValue("ZJWDH", "");     //借物单号
			table.setValue("DATECODE", "");  //dateCode
			table.setValue("TJD", "");       //特价单
		}
	  }
		function.execute(jCoDestination);
		System.out.println(table);
		JCoParameterList exportParameter = function.getExportParameterList();
		System.out.println(exportParameter.getString("RTN_CD"));//S:成功；E:失败
		System.out.println(exportParameter.getString("RTN_MSG"));

				if(exportParameter.getString("RTN_CD").equals("S")){
					result="成功";
				}
		}catch (JCoException e){
			System.out.println(e.toString());
			}
			return new JSONPObject(name, result);
		}
}
