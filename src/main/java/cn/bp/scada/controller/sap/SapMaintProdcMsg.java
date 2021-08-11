package cn.bp.scada.controller.sap;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.*;
import org.apache.xml.resolver.helpers.PublicId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoTable;

import cn.bp.scada.sap.SapConnUtils;
import cn.bp.scada.common.utils.dbhelper.DBHelper;
import cn.bp.scada.common.utils.MesApiUtils;
import cn.bp.scada.common.utils.PrimaryHelper;




@Api(value = "产品档案控制层",tags = {"产品档案与维修产品档案访问接口"})
@RestController
public class SapMaintProdcMsg extends JdbcDaoSupport{

	@Resource
    public void setJb(JdbcTemplate jb) {
        super.setJdbcTemplate(jb);
    }
	@Resource
	private MesApiUtils mesApiUtils;
	@Autowired
	private SapConnUtils sapUtils;
	@Resource
	private PrimaryHelper ph;
	@Resource
	private DBHelper db;
	@Resource
	private SapDeleProdcMsg delePro;


	private Logger LOG = LoggerFactory.getLogger(this.getClass());
	/**
	 * 维护产品档案信息(产品档案回传)
	 * @return
	 * @throws JSONException
	 * @throws IOException
	 * @throws SQLException
	 */
	@RequestMapping(value = "/sapMaintProdcMsg",method = RequestMethod.POST)
	@ApiOperation(value = "产品档案回传ERP")
	@ApiResponses(value = {@ApiResponse(code=0,message="成功"),
			 			   @ApiResponse(code = 1,message = "ERP反写失败"),
							@ApiResponse(code = 2,message = "无产品档案")})
	@ApiImplicitParams({@ApiImplicitParam(name="reqStr",value = "json串,产品sn",required = false)})
	public String sapMaintProdcMsg(String reqStr) throws Exception {

		JSONObject json= new JSONObject(reqStr);
		LOG.info("进入产品档案回传，收到的数据："+json);
		String sn = json.getJSONArray("REQ").getJSONObject(0).getJSONObject("REQ_DATA").getString("CRATE");
		//先删除erp档案，再进行上传
		/*JSONObject jsAll = new JSONObject();
		jsAll.put("sn", sn);
		delePro.sapMaintProdcMsgAll(jsAll);*/

		JSONObject reqDataDetail = new JSONObject();
		reqDataDetail.put("IFS", "STA68");
		reqDataDetail.put("PRODUCTSN", sn);
		 JSONArray jsonArray = mesApiUtils.doPost2(reqDataDetail);
		 LOG.info("产品档案回传mes返回："+jsonArray);
		 JSONObject js = new JSONObject();

		 try {
		if(jsonArray.length()>0) {
		JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象
		JCoFunction function = sapUtils.getFunction("ZPP_UPD_ZCPDAT");//获取到函数
		JCoTable table = function.getTableParameterList().getTable("TBL_ZCPDA");
		String times=null;
		for (int i = 0; i < jsonArray.length(); i++) {

			times =ph.getStrTime(jsonArray.getJSONObject(i).getString("NOWTIME"));
			table.appendRow();
			table.setValue("SN",Integer.parseInt(jsonArray.getJSONObject(i).getString("SN_NO"))); //整机SN
			table.setValue("QN", jsonArray.getJSONObject(i).getString("LCQN"));      //散件QN
			//table.setValue("SCSJ", jsonArray.getJSONObject(i).getString("NOWTIME"));    //生产时间
			table.setValue("SCSJ", jsonArray.getJSONObject(i).get("NOWTIME"));    //生产时间
			table.setValue("GPSJ", "");    //改配时间
			//table.setValue(".APPEND", ""); //更改时间
			table.setValue("ZTIME", times);   //生产时间(针对SCSJ字段)
			table.setValue("YCQN", jsonArray.getJSONObject(i).getString("YCSN"));    //原厂QN
			String creator;
			try {
				creator = jsonArray.getJSONObject(i).getString("CREATOR");
			} catch (JSONException e) {
				e.printStackTrace();
				creator="null";
			}
			table.setValue("ZUNAME", creator);  //用户名
			table.setValue("CDATE", ph.getDates());   //创建日期
			table.setValue("CTIME", ph.getTime());   //创建时间
			table.setValue("YCPN", jsonArray.getJSONObject(i).getString("YCPN"));    //原厂PN
			table.setValue("LCPN", jsonArray.getJSONObject(i).getString("LCPN"));    //浪潮PN
			//table.setValue("ZSOCNO", jsonArray.getJSONObject(i).getString("M_SSD_SN"));    //SOC保密卡
		}
		LOG.info("table="+table+"传给ERP tabale的条数为："+table.getNumRows());
		LOG.info("产品档案反写给ERP时间为："+ph.getDateTime());
		function.execute(jCoDestination);

			//出参
			JCoParameterList list1 = function.getExportParameterList();
			LOG.info("产品档案回传出参:  "+list1.getString(0));//S 成功  E 失败


			if(list1.getString(0).equals("S")){

				js.put("CODE", "0");
				js.put("MSG", "成功");


				//反写给测试程序条码信息
				JSONObject test = new JSONObject();
				test.put("IFS", "STA71");
				test.put("PRODUCTSN", sn);
				JSONArray jsonArrayTest = mesApiUtils.doPost2(test);
				LOG.info("产品档案反写条码给测试程序："+jsonArrayTest);
				List<Object> params = new ArrayList<>();
				String sql ="insert into SNToQN(snNumber, qn ,type,fqn,modifyFlag ) values(?,?,?,?,1)";
				for (int i = 0; i < jsonArrayTest.length(); i++) {
					params.add(jsonArrayTest.getJSONObject(i).getString("SNNUMBER")); //序列号
					params.add(jsonArrayTest.getJSONObject(i).getString("QN"));       //物料QN
					params.add(jsonArrayTest.getJSONObject(i).getString("TYPE"));     //物料类型
					params.add(jsonArrayTest.getJSONObject(i).getString("FQN"));      //（MAC地址）
					db.excuteUpdate(sql, params);
					params.clear();
				}


			}else {
				js.put("CODE", "1");
				js.put("MSG", "ERP回写失败");
			}

		} else {

			js.put("CODE", "2");
			js.put("MSG", "无产品档案");
		}

			 LOG.info("产品档案反写给ERP和diag完成，返回前端时间为："+ph.getDateTime());
	}catch (JCoException e){
	LOG.info(e.toString());
	}
		 LOG.info(js.toString());
		return js.toString();
	}

	/**
	 * 维修产品档案
	 * @param req
	 * @return
	 * @throws JSONException
	 * @throws IOException
	 */
	@RequestMapping("/maintain")
	public String labelPrint(HttpServletRequest req,String data) throws JSONException, IOException  {
		JSONObject json= new JSONObject(data);


		String sql ="";
		String result = "";
		LOG.info("进入 维修产品档案,收到的数据："+json);

		String crate = json.getString("CRATE"); //机箱SN
		String newcode = json.getString("NEWCODE"); //新条码
		String oldcode = json.getString("OLDCODE"); //旧条码
		String user = json.getString("USER"); //用户
		String zsocno = json.getString("ZSOCNO"); //保密卡识别码
		String mac_sn = json.getString("MACSN"); //MAC地址

		//先删除产品档案
		JSONObject js = new JSONObject();
		js.put("sn", crate);
		js.put("type",json.getString("CODETYPE"));
		if(json.getString("CODETYPE") .equals("LCQN")) {
			js.put("P_QN", oldcode); //散件QN(浪潮QN)
		} else { //原厂QN
			if(json.getString("CODETYPE") .equals("")) {
				js.put("P_QN", oldcode); //散件QN(浪潮QN)
			} else {
				js.put("P_YCQN", oldcode); //原厂QN
			}

		}

		delePro.sapMaintProdcMsg(js);

		if(!mac_sn.equals(null) && !mac_sn.equals("")) {

		//根据机箱SN和物料QN查询测试程序表，看是否有数据，有则修改modiflag状态为-1，然后新增一条，没有则略过，不管
		 sql = "select snNumber,type from SNToQN where snNumber=?  and qn=?";
		List<Object> params2 = new ArrayList<>();
		 List<Map<String, Object>> executeQuery =  new ArrayList<>();
		try {
			params2.add(crate);
			params2.add(oldcode);
			executeQuery = db.executeQueryTest(sql, params2, null);
			params2.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(executeQuery.size() > 0) {
			sql = "update SNToQN set modifyFlag= -1 where snNumber=?  and qn=?";
			params2.add(crate);
			params2.add(oldcode);
			db.excuteUpdate(sql, params2);
			params2.clear();

			sql ="insert into SNToQN(snNumber, qn ,type,fqn,modifyFlag ) values(?,?,?,?,1)";
			String type = executeQuery.get(0).get("type").toString();
			params2.add(crate); //序列号
			params2.add(newcode);       //物料QN
			params2.add(type);     //物料类型
			params2.add(mac_sn);      //原厂SN（MAC地址）
			db.excuteUpdate(sql, params2);
			params2.clear();

		} else {
			LOG.info("维修档案查询测试程序无数据，不新增");
		}

		}
			//维修回传给ERP一次产品档案
		JSONObject reqDataDetail = new JSONObject();
		reqDataDetail.put("IFS", "STA78");
		reqDataDetail.put("IN_NEWMATSN", newcode);
		 JSONObject jsonObject = mesApiUtils.doPost(reqDataDetail);
		 LOG.info("维修产品档案，调用mes返回"+jsonObject);

		 //回传ERP

					JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象
					JCoFunction function = sapUtils.getFunction("ZPP_UPD_ZCPDA");//获取到函数
					JCoTable table = function.getTableParameterList().getTable("TBL_ZCPDA");
					table.appendRow();
					table.setValue("SN",Integer.parseInt(crate)); //整机SN
					table.setValue("QN", jsonObject.getString("LCQN"));      //散件QN
					table.setValue("SCSJ", ph.getDates());    //生产时间
					table.setValue("GPSJ", ph.getDates());    //改配时间
					table.setValue("ZTIME", ph.getTime());   //生产时间(针对SCSJ字段)
					table.setValue("YCQN", jsonObject.getString("YCSN"));    //原厂QN
					table.setValue("ZUNAME", user);  //用户名
					table.setValue("CDATE", ph.getDates());   //创建日期
					table.setValue("CTIME", ph.getTime());   //创建时间
					table.setValue("YCPN", jsonObject.getString("YCPN"));    //原厂PN
					table.setValue("LCPN", jsonObject.getString("LCPN"));    //浪潮PN
					LOG.info("table="+table+"维修传给ERP tabale的条数为："+table.getNumRows());
					try {
						function.execute(jCoDestination);
					} catch (JCoException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

						//出参
						JCoParameterList list1 = function.getExportParameterList();
						LOG.info("维修产品档案回传出参:  "+list1.getString(0));//S 成功  E 失败

						if(list1.getString(0).equals("S")){
							result="0";
							LOG.info("维修产品档案返回前端反写成功");
						} else {
							LOG.info("维修产品档案返回前端反写失败");
							result="1";
						}

		return  result;
	}

    /**
     * 维修新增物料
     * @param req
     * @return
     * @throws JSONException
     * @throws IOException
     */
    @RequestMapping("/maintainadd")
    @ApiOperation("维修新增物料")
    public String maintainAdd(HttpServletRequest req,String data) throws JSONException, IOException  {
        //data = "{\"CRATE\":\"131221\",\"USER\":\"admin\",\"CODETYPE\": \"MEKB0575A,C0040100000,LCQN|HDTB0575B,C0030534,LCQN|C010601,C010601,LCPN\"}";
        JSONObject json= new JSONObject(data);

        String result = "";
        LOG.info("进入 维修新增物料,收到的数据："+json);
        String crate = json.getString("CRATE"); //机箱SN
        String user = json.getString("USER"); //用户
        String qnAndPN  = json.getString("CODETYPE"); //类型与qn和PN
        try {
            JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象
            JCoFunction function = sapUtils.getFunction("ZPP_UPD_ZCPDA");//获取到函数
            JCoTable table = function.getTableParameterList().getTable("TBL_ZCPDA");
            final String[] split = qnAndPN.split("\\|");
            for (int i=0; i<split.length; i++) {
				try {
					/*if(split[0] == "null")
						continue;*/
					String[] typePQn = split[i].split(",");
					table.appendRow();
					table.setValue("SN",Integer.parseInt(crate)); //整机SN
					if(typePQn[2].equals("LCQN")) {
						table.setValue("QN", typePQn[0]);      //散件QN
						table.setValue("LCPN", typePQn[1]);    //浪潮PN
					}else if(typePQn[2].equals("LCPN")) {
						table.setValue("LCPN", typePQn[1]);    //浪潮PN
					}else if(typePQn[2].equals("YCSN")) {
						table.setValue("YCQN", typePQn[0]);    //原厂SN
						table.setValue("QN", typePQn[0]);      //散件QN
						table.setValue("LCPN", typePQn[1]);    //浪潮PN
					}
					table.setValue("SCSJ", ph.getDates());    //生产时间
					table.setValue("GPSJ", ph.getDates());    //改配时间
					table.setValue("ZTIME", ph.getTime());   //生产时间(针对SCSJ字段)
					table.setValue("ZUNAME", user);  //用户名
					table.setValue("CDATE", ph.getDates());   //创建日期
					table.setValue("CTIME", ph.getTime());   //创建时间
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
            LOG.info("维修新增物料传给ERP数据为："+table+"：共有"+table.getNumRows()+" 条");
            function.execute(jCoDestination);
            //出参
            JCoParameterList list1 = function.getExportParameterList();
            LOG.info("维修产品档案回传出参:  "+list1.getString(0));//S 成功  E 失败
            if(list1.getString(0).equals("S")){
                result="0";
                LOG.info("维修新增物料回写ERP反写成功");
            } else {
                LOG.info("维修新增物料回写ERP反写失败");
                result="1";
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @ApiOperation("OQC数据与SQP交互")
    @PostMapping("/oqcAddSap")
	public String OqcInsertSap(HttpServletRequest req,String data) throws JSONException, IOException  {
    	String result = "1";
		JSONObject json= new JSONObject(data);
		try {
			LOG.info("OQC收到的数据："+json);
			String flag = json.getString("IS_FLAG"); // 0:新增 1：修改

			JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象
			JCoFunction function = sapUtils.getFunction("ZRFC_UPD_ZGZJL");//获取到函数
			JCoTable table = function.getTableParameterList().getTable("TBL_ZGZJL");
			table.appendRow();
			/*if(flag .equals("0")) { //新增

				addOqc(table,json,function,jCoDestination);

			}else if(flag .equals("1")){ //修改
				//先删除
				deleOqc(table,json,function,jCoDestination);
				//再增加
				addOqc(table,json,function,jCoDestination);

			}else { //删除

				deleOqc(table,json,function,jCoDestination);
			}*/
			addOqc(table,json,function,jCoDestination);
			result="0";
		}catch (Exception e) {
			LOG.info(e.toString(),e);
		}

    	return result;
	}

	/**
	 * 删除和修改OQC方法
	 * @param table
	 * @param json
	 */
	public void deleOqc(JCoTable table,JSONObject json,JCoFunction function,JCoDestination jCoDestination) throws JCoException {
		table.setValue("SN",json.getString("PRO_SN")); //sn号
		table.setValue("SCBZ","1"); //删除
		function.execute(jCoDestination);

		JCoParameterList list1 = function.getExportParameterList();
		LOG.info("OQC删除出参:"+list1.getString(0));
	}

	/**
	 * OQC新增公共参数
	 * @param table
	 * @param json
	 * @param function
	 * @param jCoDestination
	 */
	public void addOqc(JCoTable table,JSONObject json,JCoFunction function,JCoDestination jCoDestination) throws JCoException {
		String checkDt = ph.getDates();
		table.setValue("PRODH",json.getString("MAT_CODE"));
		table.setValue("SFHG",json.getString("IS_QUAF")); //是否合格
		table.setValue("JYY",json.getString("USER"));
		table.setValue("DATNM",checkDt); //检验日期，对应交货日期
		table.setValue("BJLB",json.getString("PTS_TYPE")); //部件类别
		table.setValue("SN",json.getString("PRO_SN")); //sn号
		table.setValue("AUFNR",json.getString("ORDER")); //订单号
		table.setValue("MATKL",json.getString("MAT_GRP")); //物料组
		table.setValue("WERKS",json.getString("FACT")); //工厂
		table.setValue("GZMS",json.getString("GZMS")); //综合故障描述
		table.setValue("KURZTEXTW",json.getString("KURZTEXTW")); //外观故障描述
		table.setValue("KURZTEXTX",json.getString("KURZTEXTX")); //性能故障描述
		table.setValue("QT",json.getString("QT")); //齐套故障描述
		function.execute(jCoDestination);

		JCoParameterList list1 = function.getExportParameterList();
		LOG.info("OQC新增数据："+table+"：OQC新增出参:"+list1.getString(0));
	}
}
