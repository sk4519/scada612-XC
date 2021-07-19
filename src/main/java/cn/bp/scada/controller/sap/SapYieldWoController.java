package cn.bp.scada.controller.sap;


import cn.bp.scada.common.utils.PrimaryHelper;
import cn.bp.scada.mapper.scada.UpdaYieldWo;
import cn.bp.scada.sap.SapConnUtils;
import cn.bp.scada.sap.bean.MesMoT;
import cn.bp.scada.sap.bean.WorkOrder;
import cn.bp.scada.sap.bean.WorkOrderBom;
import cn.bp.scada.sap.bean.WorkOrderItem;
import com.sap.conn.jco.*;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;

/*@Transactional(rollbackFor=Exception.class)*/
@RestController
public class SapYieldWoController extends JdbcDaoSupport{
	@Resource
	public void setJb(JdbcTemplate jb) {
		super.setJdbcTemplate(jb);
	}

	@Autowired
	private UpdaYieldWo yield;

	@Autowired
	private SapConnUtils sapUtils;
	@Resource
	private PrimaryHelper ph;

	/**
	 * 生产任务单获取修改信息接口（与ERP数据保持一致）          工单信息&工单明细&工单bom
	 *
	 * @return
	 * @throws JSONException
	 */
	//@Scheduled(fixedDelay = 240000) //定时任务，间隔执行，每4分钟
	public String yieldWo() throws JSONException{
		try {
			JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象

			 JCoFunction	 function = sapUtils.getFunction("ZPP_GET_WO_MODIFY_DETAIL");//获取到函数

			//入参
			JCoParameterList parameterList = function.getImportParameterList();
			String startDown = ph.getDates();
			String overDown = ph.getDates();
			//设置入参
			parameterList.setValue("I_AUFNR_FRM", "");//起止订单号 (前面需要加4个0)
			parameterList.setValue("I_AUFNR_TO", "");//结束订单号
			parameterList.setValue("I_WERKS_FRM", "8100");//起止工厂
			parameterList.setValue("I_WERKS_TO", "8100");//结束工厂
			parameterList.setValue("I_PDAT1_FRM",startDown);//起止下达日期
			parameterList.setValue("I_PDAT1_TO",overDown);//结束下达日期
			parameterList.setValue("I_STAT","");//对象的单个状态
			parameterList.setValue("I_TIMS_FRM","00:00:00");//起始修改时间
			parameterList.setValue("I_TIMS_TO","24:00:00");//结束修改时间

			function.execute(jCoDestination);

			//出参
			JCoParameterList list1 = function.getExportParameterList();
			System.out.println("出参:  "+list1.getString(0));
			System.out.println("出参:  "+list1.getString(1));

			final List<WorkOrder> list= new ArrayList<WorkOrder>();
			final List<WorkOrderItem> listItem= new ArrayList<WorkOrderItem>();
			final List<WorkOrderBom> listBom= new ArrayList<WorkOrderBom>();

			//如果入参是内表形式，就以如下代码传入sap系统
			JCoTable  table = function.getTableParameterList().getTable("TBL_HEAD");//获取表  TBL_HEAD工单信息
			for (int i = 0; i < table.getNumRows(); i++) {
				WorkOrder m = new WorkOrder();
				table.setRow(i);
				m.setO_no(table.getString("AUFNR")); //订单号
				m.setFct_code(table.getString("WERKS")); //工厂
				m.setMt_tnr(table.getString("PLNBEZ")) ; //物料编号
				//m.setMt_ds(table.getString("MAKTX")); //物料描述
				m.setM_pt_maktx(table.getString("V_PT_MAKTX")); //产品层次结构(产品型号)
				m.setM_pt_matnr(table.getString("V_PT_MATNR")); //订单的物料编号
				m.setPc_md(table.getString("V_PT_MAXH")); //产品型号
				m.setO_num_sum((table.getString("GAMNG"))); //订单数量总计
				m.setSp_code(table.getString("V_PRODH")); //商品编码
				m.setAuart(table.getString("AUART")); //订单类型
				m.setFtrmi(table.getString("FTRMI")); //实际下达日期
				m.setFtrmi_tm(table.getString("FTRMI_TM")); //下达时间
				m.setStatus(table.getString("STATUS")); //订单状态
				m.setV_pt_matnr(table.getString("V_PT_MATNR"));
				m.setV_sernr(table.getString("V_SERNR"));
				m.setV_tdline(table.getString("V_TDLINE")); //产品配置
				m.setV_memo(table.getString("V_MEMO")); //长文本
				m.setRkrq(table.getString("RKRQ"));
				m.setRkrq_tm(table.getString("RKRQ_TM"));
				m.setTech_inst(table.getString("TECH_INST")); //技术指令号
				m.setProj_name(table.getString("PROJ_NAME"));
				m.setEmergency(table.getString("EMERGENCY"));
				m.setDispo(table.getString("DISPO"));
				m.setErnam(table.getString("ERNAM"));
				m.setPzms(table.getString("PZMS")); //配置描述
				m.setWrkst(table.getString("WRKST"));
				m.setBismt(table.getString("BISMT"));
				m.setZxghsj(table.getString("ZXGHSJ"));
				m.setVkbur(table.getString("VKBUR"));
				m.setGstrs(table.getString("GSTRS"));
				m.setGsuzs(table.getString("GSUZS"));
				m.setGltrs(table.getString("GLTRS"));
				m.setGluzs(table.getString("GLUZS"));
				m.setArbpl(table.getString("ARBPL"));
				m.setAufnr_a(table.getString("AUFNR_A"));
				m.setCplb(table.getString("CPLB"));
				list.add(m);
			}
			//工单信息,修改sql，根据订单号
			System.out.println(list.size()+" 条工单数据: --");
			//工单明细
			String wkItemsql="";
			JCoTable tableItem = function.getTableParameterList().getTable("TBL_DETAIL");//获取表  TBL_DETAIL 工单明细
			for (int i = 0; i < tableItem.getNumRows(); i++) {
				WorkOrderItem item = new WorkOrderItem();
				tableItem.setRow(i);
				item.setAufnr(tableItem.getString("AUFNR"));
				item.setV_sj_matnr(tableItem.getString("V_SJ_MATNR"));
				item.setV_bdmng(tableItem.getString("V_BDMNG"));
				item.setV_sj_maktx(tableItem.getString("V_SJ_MAKTX"));
				item.setDumps(tableItem.getString("DUMPS"));
				item.setV_lgort(tableItem.getString("V_LGORT"));
				item.setV_charg(tableItem.getString("V_CHARG"));
				item.setStatus(tableItem.getString("STATUS"));
				item.setProj_info(tableItem.getString("PROJ_INFO"));
				item.setMatkl(tableItem.getString("MATKL"));
				item.setWgbez(tableItem.getString("WGBEZ"));
				item.setPn(tableItem.getString("PN"));
				listItem.add(item);
			}

			System.out.println(listItem.size()+" 条工单明细数据: --");
			String bomSql="";
			JCoTable tableBom = function.getTableParameterList().getTable("TBL_DETAIL_OM");//获取表  TBL_DETAIL_OM 工单bom
			for (int i = 0; i < tableBom.getNumRows(); i++) {
				WorkOrderBom bom = new WorkOrderBom();
				tableBom.setRow(i);
				bom.setMatnr(tableBom.getString("MATNR"));
				bom.setMaktx(tableBom.getString("MAKTX"));
				bom.setMenge(tableBom.getString("MENGE"));
				bom.setPosnr(tableBom.getString("POSNR"));
				bom.setPostp(tableBom.getString("POSTP"));
				bom.setIdnrk(tableBom.getString("IDNRK"));
				bom.setIdktx(tableBom.getString("IDKTX"));
				bom.setIdnge(tableBom.getString("IDNGE"));
				bom.setMeins(tableBom.getString("MEINS"));
				bom.setNotes(tableBom.getString("NOTES"));
				bom.setPn(tableBom.getString("PN"));
				listBom.add(bom);
			}
			System.out.println(listBom.size()+" 条bom组件数据: --");

		}catch (JCoException e){
		}
		return "";

	}
}

