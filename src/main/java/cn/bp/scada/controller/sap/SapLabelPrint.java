package cn.bp.scada.controller.sap;
/**
 * *************************************************************
 * COL1=打印方式@0|模板名称@V100008000000000|产品名称@浪潮英政计算机|标签数量@1|生产订单@10420852|销售订单@|产品编码@110046|销售订单行项目@|导航编码@|商品编码@PCE3000L0004|机器序列号@131002048|产品型号@CE3000L|类号@|产品层次@/8G/SSD_256G///////DR//|CPU@|内存@8G|硬盘@256G|CPU描述 *****************************
 * COL2=@|CPU描述(缓存)@|CPU1@|内存1@8G|硬盘1@256G|软驱@|光驱@|网卡@|电源@|电源标识@180-240V～ 3A 50Hz|导轨@|其他@|盘位@|电源数量@1|打印长描述@|MainPartPN@|工单下达日期@2020.05|电源功率@220 *****************************
 * ************************************************************
 */

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.sap.conn.jco.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.bp.scada.sap.PrintLabel;
import cn.bp.scada.sap.SapConnUtils;
import cn.bp.scada.common.utils.dbhelper.DBHelper;
import cn.bp.scada.common.utils.MesApiUtils;
import cn.bp.scada.common.utils.PrimaryHelper;

@Api(value = "打印控制层", tags = {"机箱铭牌打印与保密机型打印"})
@RestController
public class SapLabelPrint extends JdbcDaoSupport {
    @Resource
    public void setJb(JdbcTemplate jb) {
        super.setJdbcTemplate(jb);
    }

    @Resource
    PrintLabel pLabel;
    @Autowired
    private SapConnUtils sapUtils;
    @Resource
    private DBHelper db;
    @Resource
    private PrimaryHelper ph;
    @Resource
    private MesApiUtils mesApiUtils;
    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    //手动测试
    @RequestMapping("/sapGet")
    public void testSapGet() {
        try {
            JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象
            JCoFunction function = sapUtils.getFunction("ZRFC_GET_LABELFILE");//获取到函数
            //入参
            JCoParameterList parameterList = function.getImportParameterList();
            //设置入参
            parameterList.setValue("P_SERNR", "000000000" + "132013092"); //序列号
            parameterList.setValue("P_AUFNR", "000060034420"); //订单号
            parameterList.setValue("P_STYLE", "B001"); //标签类型
            parameterList.setValue("P_LANGUAGE", "0010"); //语言
            function.execute(jCoDestination);
            parameterList.clear();

            JCoTable table = function.getTableParameterList().getTable("ET_OUTPUT");//获取表

            final String spl = table.getValue("COL1").toString() + table.getValue("COL2").toString()
                    + table.getValue("COL3").toString() + table.getValue("COL4").toString();
            LOG.info("*************************************************************");
            LOG.info("手动获取的标签：" + spl);
            LOG.info("*************************************************************");
        } catch (JCoException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/sap")
    public Map<String, Object> testSap(String sn, String order) {
        Map<String, Object> map = new HashMap<>();
        try {
            JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象
            JCoFunction function = sapUtils.getFunction("ZRFC_GET_LABELFILE");//获取到函数

            //入参
            JCoParameterList parameterList = function.getImportParameterList();
            //设置入参
            parameterList.setValue("P_SERNR", "000000000" + sn); //序列号
            parameterList.setValue("P_AUFNR", order); //订单号
            parameterList.setValue("P_STYLE", "B001"); //标签类型
            parameterList.setValue("P_LANGUAGE", "0010"); //语言
            function.execute(jCoDestination);
            parameterList.clear();

            JCoTable table = function.getTableParameterList().getTable("ET_OUTPUT");//获取表

            final String spl = table.getValue("COL1").toString() + table.getValue("COL2").toString()
                    + table.getValue("COL3").toString() + table.getValue("COL4").toString();
            LOG.info("*************************************************************");
            LOG.info("获取的标签：" + spl);
            LOG.info("*************************************************************");
            String[] split1 = spl.split("\\|");
            for (int i = 0; i < split1.length; i++) {
                if (split1[i].contains("@")) {
                    String str = split1[i].substring(0, split1[i].indexOf("@"));
                    if (str.equals("产品名称")) {
                        map.put("prodName", split1[i].substring(split1[i].indexOf("@") + 1));
                    } else if (str.equals("产品型号")) {
                        map.put("prodType", split1[i].substring(split1[i].indexOf("@") + 1));
                    } else if (str.equals("销售订单")) {
                        map.put("SALE_NO", split1[i].substring(split1[i].indexOf("@") + 1));
                    } else if (str.equals("产品编码")) {
                        map.put("PRO_CD", split1[i].substring(split1[i].indexOf("@") + 1));
                    } else if (str.equals("销售订单行项目")) {
                        map.put("PRO_NO", split1[i].substring(split1[i].indexOf("@") + 1));
                    } else if (str.equals("商品编码")) {
                        map.put("PRO_NM", split1[i].substring(split1[i].indexOf("@") + 1));
                    } else if (str.equals("CPU")) {
                        map.put("CPU", split1[i].substring(split1[i].indexOf("@") + 1));
                    } else if (str.equals("内存")) {
                        map.put("NC_NM", split1[i].substring(split1[i].indexOf("@") + 1));
                    } else if (str.equals("生产订单")) {
                        map.put("O_NO", split1[i].substring(split1[i].indexOf("@") + 1));
                    } else if (str.equals("导航编码")) {
                        map.put("NVG_CODE", split1[i].substring(split1[i].indexOf("@") + 1));
                    } else if (str.equals("电源标识")) {
                        map.put("powerLogo", split1[i].substring(split1[i].indexOf("@") + 1));
                    } else if (str.equals("硬盘")) {
                        map.put("YP_NM", split1[i].substring(split1[i].indexOf("@") + 1));
                    } else if (str.equals("其他")) {
                        map.put("OTHER", split1[i].substring(split1[i].indexOf("@") + 1));
                    } else if (str.equals("网卡")) {
                        map.put("NET_CARD", split1[i].substring(split1[i].indexOf("@") + 1));
                    } else if (str.equals("导轨")) {
                        map.put("WAY", split1[i].substring(split1[i].indexOf("@") + 1));
                    } else if (str.equals("光驱")) {
                        map.put("CD", split1[i].substring(split1[i].indexOf("@") + 1));
                    } else if (str.equals("生产下达日期")) {
                        map.put("proDt", split1[i].substring(split1[i].indexOf("@") + 1));
                    }
                }
            }

            if (map.size() < 1) {
                map.put("erp", "1");
            } else {
                map.put("erp", "0");
            }

        } catch (Exception e) {
            LOG.info("ERP获取标签失败");
            map.put("erp", "1");
            e.printStackTrace();
        }

        return map;
    }

    /**
     * 机箱标签打印接口
     *
     * @param reqStr 获取jsonp的函数名
     * @param reqStr 获取前台传过来的json字符串参数
     * @return
     * @throws JSONException
     * @throws IOException
     */
    @RequestMapping(value = "/sapLabelPrint", method = RequestMethod.POST)
    @ApiOperation(value = "机箱标签打印")
    @ApiImplicitParam(name = "reqStr", value = "json字符串，需要的字段：(LINE)与)与订单号（ORDER），（LINE）", required = false, dataType = "String")
    public synchronized String labelPrint(String reqStr) throws JSONException, IOException {
        JSONObject json = null;
        JSONObject js = new JSONObject();
        try {
            json = new JSONObject(reqStr);
            LOG.info("进入机箱标签打印，收到的数据：" + json + "时间为：" + ph.getDateTime());
            JSONObject jsonMes = json.getJSONArray("REQ").getJSONObject(0).getJSONObject("REQ_DATA");


            String order = jsonMes.getString("ORDER");
            String sql = "select SERNR from (select * from PRODUCT_SN where AUFNR=? and USE_STA='N'  order by SERNR) where rownum=1";
            Map<String, Object> map = this.getJdbcTemplate().queryForMap(sql, order);
            String sn = map.get("SERNR").toString();

            String line = jsonMes.getString("LINE");
            String url = "";

            LOG.info("机箱SN:" + Integer.parseInt(sn));
            LOG.info("订单号：" + order);
            LOG.info("线别：" + line);
            if (line.equals("LCLINE001")) {
                //url="//10.50.7.221/FilePrintPath//";
                url = "//192.168.10.47/FilePrintPath//";
            } else {
                //url="//10.50.6.166/FilePrintPath//"; //二线
                url = "//192.168.10.73/FilePrintPath//";
            }

            //调用方法获取电源标识等
            Map<String, Object> testSap = testSap(sn, order);
            if (testSap.get("erp").equals("0")) {
                String label = pLabel.masterLabel(Integer.parseInt(sn) + "", line, url, testSap);
                JSONObject reqDataDetail = new JSONObject();
                reqDataDetail.put("IFS", "STA33");
                reqDataDetail.put("IN_VEHICLES", jsonMes.getString("IN_VEHICLES"));
                reqDataDetail.put("IN_SERIALNUM", jsonMes.getString("IN_SERIALNUM"));
                reqDataDetail.put("IN_WORKNO", jsonMes.getString("IN_WORKNO"));
                reqDataDetail.put("IN_WONO", jsonMes.getString("IN_WONO"));
                reqDataDetail.put("IN_USERID", jsonMes.getString("IN_USERID"));
                reqDataDetail.put("IN_PRODUCTSN", sn);

                JSONObject mesResult = null;
                switch (label) {
                    case "0":
                        //调用物料过站
                        mesResult = mesApiUtils.doPost(reqDataDetail);
                        if (mesResult.get("CODE").toString().equals("0")) {
                            js.put("CODE", "0");
                            js.put("MSG", "打印成功");
                            js.put("SN", sn);
                            js.put("FLAG", mesResult.get("FLAG"));
                        } else {
                            js.put("CODE", "1");
                            js.put("MSG", mesResult.getString("MSG"));
                        }
                        break;
                    case "1":
                        js.put("CODE", "1");
                        js.put("MSG", "打印机打印失败");
                        break;
                    case "10":
                        js.put("CODE", "0");
                        js.put("MSG", "文件已存在,已删除重写");

                        //调用物料过站
                        mesResult = mesApiUtils.doPost(reqDataDetail);
                        if (mesResult.get("CODE").toString().equals("0")) {
                            js.put("CODE", "0");
                            js.put("MSG", "打印成功");
                            js.put("SN", sn);
                            js.put("FLAG", mesResult.get("FLAG"));
                        } else {
                            js.put("CODE", "1");
                            js.put("MSG", mesResult.getString("MSG"));
                        }
                        break;
                    default:
                        js.put("CODE", "1");
                        js.put("MSG", "创建新文件失败");
                        break;
                }

            } else {
                js.put("CODE", "1");
                js.put("MSG", "ERP获取标签失败");
            }


        } catch (Exception e) {
            js.put("CODE", "1");
            js.put("MSG", "打印失败");
            e.printStackTrace();
        }

        LOG.info("返回的json:" + js.toString());
        LOG.info("打印返回给前端的时间：" + ph.getDateTime());
        return js.toString();
    }


    /**
     * 一线机箱标签重新打印接口
     *
     * @param req 获取jsonp的函数名
     * @param req 获取前台传过来的json字符串参数
     * @return
     * @throws JSONException
     * @throws IOException
     */
    @RequestMapping("/reSapLabelPrint1")
    public JSONPObject reLablePrint1(HttpServletRequest req, String data) throws JSONException, IOException {
        String name = req.getParameter("callbackparam");//获取到jsonp的函数名
        JSONObject js = null;
        try {
            LOG.info("进入一线机箱标签打印，收到的数据：" + req + "sn为" + data);
            //查询线别和订单号
            String sql = "SELECT * FROM (SELECT T1.LINE_CD ,T2.AUFNR, T2.CRT_DT FROM (SELECT * FROM MES1.R_MES_MO_BC_T WHERE SN_NO=?) T1 LEFT JOIN MES1.PRODUCT_SN T2 ON T2.SERNR=? ORDER BY CRT_DT ) WHERE ROWNUM=1";
            Map<String, Object> map = this.getJdbcTemplate().queryForMap(sql, data, data);
            String line = "LCLINE001";
            String order = map.get("AUFNR").toString();
            String sn = data;
            String url = "";
            LOG.info("机箱SN:" + Integer.parseInt(sn));
            LOG.info("订单号：" + order);
            LOG.info("线别：" + line);
            url = "//192.168.10.47/FilePrintPath//";
            //调用方法获取电源标识等
            Map<String, Object> testSap = testSap(sn, order);
            String label = pLabel.masterLabel(Integer.parseInt(sn) + "", line, url, testSap);
            js.put("CODE", "0");
            js.put("MSG", "打印成功");
        } catch (Exception e) {
            js.put("CODE", "1");
            js.put("MSG", "打印失败");
            e.printStackTrace();
        }
        LOG.info("返回的json:" + js.toString());
        LOG.info("打印返回给前端的时间：" + ph.getDateTime());
        return new JSONPObject(name, js.toString());
    }

    /**
     * 二线机箱标签重新打印接口
     *
     * @param req 获取jsonp的函数名
     * @param req 获取前台传过来的json字符串参数
     * @return
     * @throws JSONException
     * @throws IOException
     */
    @RequestMapping("/reSapLabelPrint2")
    public JSONPObject reLablePrint2(HttpServletRequest req, String data) throws JSONException, IOException {
        String name = req.getParameter("callbackparam");//获取到jsonp的函数名
        JSONObject js = null;
        try {
            LOG.info("进入二线机箱标签打印，收到的数据：" + req + "sn为" + data);
            //查询线别和订单号
            String sql = "SELECT * FROM (SELECT T1.LINE_CD ,T2.AUFNR, T2.CRT_DT FROM (SELECT * FROM MES1.R_MES_MO_BC_T WHERE SN_NO=?) T1 LEFT JOIN MES1.PRODUCT_SN T2 ON T2.SERNR=? ORDER BY CRT_DT ) WHERE ROWNUM=1";
            Map<String, Object> map = this.getJdbcTemplate().queryForMap(sql, data, data);
            String line = "LCLINE002";
            String order = map.get("AUFNR").toString();
            String sn = data;
            String url = "";

            LOG.info("机箱SN:" + Integer.parseInt(sn));
            LOG.info("订单号：" + order);
            LOG.info("线别：" + line);
            url = "//192.168.10.73/FilePrintPath//";
            //调用方法获取电源标识等
            Map<String, Object> testSap = testSap(sn, order);
            String label = pLabel.masterLabel(Integer.parseInt(sn) + "", line, url, testSap);
            js.put("CODE", "0");
            js.put("MSG", "打印成功");
        } catch (Exception e) {
            js.put("CODE", "1");
            js.put("MSG", "打印失败");
            e.printStackTrace();
        }
        LOG.info("返回的json:" + js.toString());
        LOG.info("打印返回给前端的时间：" + ph.getDateTime());
        return new JSONPObject(name, js.toString());
    }

    /**
     * 保密卡标签打印接口
     *
     * @param reqStr 获取jsonp的函数名
     * @param reqStr 获取前台传过来的json字符串参数
     * @return
     * @throws JSONException
     * @throws IOException
     */
    @RequestMapping(value = "/sapLabelPrintBm", method = RequestMethod.POST)
    @ApiOperation(value = "保密卡打印接口")
    @ApiImplicitParam(name = "reqStr", value = "json字符串，需要的字段：工位(LINE)与机箱SN(CRATE)与订单号（ORDER），工位（LINE）", required = false, dataType = "String")
    public String labelPrintBm(String reqStr) throws JSONException, IOException {
        JSONObject json = new JSONObject(reqStr);
        LOG.info("进入保密卡标签打印，收到的数据：" + json + ",时间为：" + ph.getDateTime());
        JSONObject js = new JSONObject();
        JSONObject jsonMes = json.getJSONArray("REQ").getJSONObject(0).getJSONObject("REQ_DATA");
        try {


            String sn = jsonMes.getString("CRATE");
            String order = jsonMes.getString("ORDER");
            LOG.info("rout=" + jsonMes.getString("LINE"));
            String rout = jsonMes.getString("LINE");
            String url = "";

            LOG.info("机箱SN:" + Integer.parseInt(sn));
            LOG.info("订单号：" + order);


            JSONObject reqDataDetail = new JSONObject();
            reqDataDetail.put("IFS", "STA01");
            reqDataDetail.put("LINE", rout);

            JSONObject responseData = mesApiUtils.doPost(reqDataDetail);
            String line = responseData.getString("PD_LN_CD");
            if (line.equals("LCLINE001")) {
                //url="//10.50.6.168/FilePrintPath//";
                url = "//192.168.10.156/LabelFile/FilePrintPath//";
            } else {
                //url="//10.50.6.166/FilePrintPath//"; //二线
                url = "//192.168.10.46/LabelFile/FilePrintPath//";
            }
            LOG.info("线别：" + line);
            JSONObject reqDataDetail1 = new JSONObject();
            reqDataDetail1.put("IFS", "STA92");
            reqDataDetail1.put("IN_PRODUCTSN", sn);
            JSONObject responseData1 = mesApiUtils.doPost(reqDataDetail1);


            String bcd = responseData1.getString("BMCARD_SN"); //标识码
            String hard1 = responseData1.getString("YCSN1"); //硬盘1
            String hard2 = responseData1.getString("YCSN2"); //硬盘2
            if (null == hard1 || "null".equals(hard1)) {
                hard1 = "";
            }

            if (null == hard2 || "null".equals(hard2)) {
                hard2 = "";
            }
            Map<String, Object> testSap = testSap(sn, order);

            if (testSap.get("erp").equals("0")) {
                String label = pLabel.masterLabelBm(Integer.parseInt(sn) + "", line, url, hard1, hard2, bcd, testSap);
                String labe2 = pLabel.masterLabelBm(Integer.parseInt(sn) + "", line, url, hard1, hard2, bcd, testSap);
                switch (label) {
                    case "0":
                        js.put("CODE", "0");
                        js.put("MSG", "打印成功");
                        break;
                    case "1":
                        js.put("CODE", "1");
                        js.put("MSG", "打印机打印失败");
                        break;
                    case "10":
                        js.put("CODE", "0");
                        js.put("MSG", "文件已存在,已删除重写");
                        break;
                    default:
                        js.put("CODE", "1");
                        js.put("MSG", "创建新文件失败");
                        break;
                }

            } else {
                js.put("CODE", "1");
                js.put("MSG", "ERP获取标签失败");
            }

        } catch (Exception e) {
            js.put("CODE", "1");
            js.put("MSG", "异常，打印失败");
            e.printStackTrace();
        }
        LOG.info("返回的json:" + js.toString());
        LOG.info("打印返回给前端的时间：" + ph.getDateTime());
        return js.toString();
    }


    /**
     * 机箱标签打印接口
     * @param reqStr  获取jsonp的函数名
     * @param reqStr 获取前台传过来的json字符串参数
     * @return
     * @throws JSONException
     * @throws IOException
     */
	/*@RequestMapping("/sapLabelPrint")
	public String labelPrint(String reqStr) throws JSONException, IOException  {
		JSONObject json= new JSONObject(reqStr);
		LOG.info("进入标签打印，收到的数据："+json+"时间为："+ph.getDateTime());
		JSONObject js = new JSONObject();
		try {
		JSONObject jsonMes= json.getJSONArray("REQ").getJSONObject(0).getJSONObject("REQ_DATA");

		String sn =jsonMes.getString("CRATE");
		String order = jsonMes.getString("ORDER");
		String line = jsonMes.getString("LINE");
		String url = "";

		LOG.info("机箱SN:"+Integer.parseInt(sn));
		LOG.info("订单号："+order);
		LOG.info("线别："+line);
		if(line.equals("LCLINE001")) {
			//url="//10.50.6.168/FilePrintPath//";
			url="//192.168.10.47/FilePrintPath//";
		}else {
			//url="//10.50.6.166/FilePrintPath//"; //二线
			url="//192.168.10.73/FilePrintPath//";
		}

		//调用方法获取电源标识等
		Map<String, Object> testSap = testSap(sn,order);
		if(testSap.get("erp").equals("0")) {
			String label = pLabel.masterLabel(Integer.parseInt(sn)+"", line,url,testSap);

			switch (label) {
				case "0":
					js.put("CODE", "0");
					js.put("MSG", "打印成功");
					break;
				case "1":
					js.put("CODE", "1");
					js.put("MSG", "打印机打印失败");
					break;
				case "10":
					js.put("CODE", "1");
					js.put("MSG", "文件已存在,已删除重写");
					break;
				default:
					js.put("CODE", "1");
					js.put("MSG", "创建新文件失败");
					break;
			}

		} else {
			js.put("CODE", "1");
			js.put("MSG", "ERP获取标签失败");
		}

	}catch (Exception e) {
		js.put("CODE", "1");
		js.put("MSG", "打印失败");
		e.printStackTrace();
	}


		LOG.info("返回的json:"+js.toString());
		LOG.info("打印返回给前端的时间："+ph.getDateTime());
		return  js.toString();
	}*/


    /**
     * 主板安装反写信息给测试程序
     *
     * @param reqStr
     * @return
     * @throws JSONException
     */
    @RequestMapping("/writeTest")
    public String writeTest(String reqStr) throws JSONException {
        JSONObject json = new JSONObject(reqStr);
        LOG.info("进入主板安装反写信息，收到的数据：" + json);
        JSONObject jsonMes = json.getJSONArray("REQ").getJSONObject(0).getJSONObject("REQ_DATA");
        String sn = jsonMes.getString("CRATE");
        String order = jsonMes.getString("ORDER");

        LOG.info("序列号:" + sn);
        LOG.info("订单号：" + order);

		/*String sql ="insert into ListToSN(listId,snNumber) values(?,?)";
		List<Object> params = new ArrayList<>();
		params.add(Integer.parseInt(order));
		params.add(sn);
		boolean flag = db.excuteUpdate(sql, params);*/

        JSONObject js = new JSONObject();

        js.put("CODE", "0");
        js.put("MSG", "成功");

        LOG.info("返回的json:" + js.toString());
        return js.toString();
    }
}
