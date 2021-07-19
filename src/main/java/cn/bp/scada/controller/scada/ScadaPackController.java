package cn.bp.scada.controller.scada;

import cn.bp.scada.modle.scada.ScadaRespon;
import cn.bp.scada.service.scada.ScadaPackService;
import com.fasterxml.jackson.databind.util.JSONPObject;
import io.swagger.annotations.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 包装
 *
 * @author Administrator
 */
@Api(value = "包装线控制层", tags = {"包装功能前端与上位机访问接口"})
@RestController
public class ScadaPackController extends JdbcDaoSupport {
    @Resource
    public void setJb(JdbcTemplate jb) {
        super.setJdbcTemplate(jb);
    }

    @Autowired
    private ScadaPackService scadaPackService;
    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    /**
     * 能耗标签打印接口
     *
     * @param req    获取jsonp的函数名
     * @param reqStr 获取前台传过来的json字符串参数
     * @return
     * @throws JSONException
     */
    @PostMapping("/sapLabelPrintNh")
    @ApiOperation("能耗标签打印")
    @ApiImplicitParam(name = "reqStr", value = "json字符串，需要的字段：CRATE", required = false, dataType = "String")
    @ApiResponses({@ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "\"ERP获取标签失败"),
            @ApiResponse(code = 2, message = "异常，打印失败")})
    public String labelPrintNh(String reqStr) throws JSONException {
        JSONObject json = new JSONObject(reqStr);

        LOG.info("进入能耗标签打印，收到的数据：" + json);

        return scadaPackService.labelPrintNh(json);
    }

    /**
     * 上位机告知SCADA包装线哪条启用和关闭
     *
     * @param msg
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public ScadaRespon packLine(Object msg) throws JSONException, IOException {
        JSONObject deviceRequest = new JSONObject(msg.toString());

        LOG.info("进入上位机告知SCADA包装线哪条启用和关闭");

        return scadaPackService.packLine(deviceRequest);
    }

    /**
     * 称重状态,是否进入OQC
     *
     * @param reqStr
     * @throws JSONException
     */
    @PostMapping("/sapWeigh")
    @ApiOperation("前端触发返回称重状态接口")
    @ApiImplicitParam(name = "reqStr", value = "json字符串，需要的字段：STATUS", required = false, dataType = "String")
    @ApiResponses({@ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "ERP打印信息获取失败"),
            @ApiResponse(code = 2, message = "上位机程序异常")})
    public String weighStatus(String reqStr) throws JSONException {
        JSONObject json = new JSONObject(reqStr);

        LOG.info("进入包装称重返回状态，收到的数据：" + json);

        return scadaPackService.weighStatus(json);
    }

    /**
     * 打印外箱SN
     *
     * @param reqStr
     * @throws JSONException
     */
    @PostMapping(value = "/sapLabelPrintOut")
    @ApiOperation("前端触发保存打印信息接口")
    @ApiImplicitParams({@ApiImplicitParam(name = "reqStr", value = "json串", required = false)})
    @ApiResponses({@ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "ERP打印信息获取失败"),
            @ApiResponse(code = 2, message = "CODE表不存在数据"), @ApiResponse(code = 3, message = "接口异常")})
    public String sapLabelPrintOut(String reqStr) throws Exception {
        JSONObject json = new JSONObject(reqStr);
        LOG.info("进入扫码打印外箱SN，收到的数据：" + json);

        return scadaPackService.sapLabelPrintOut(json);
    }

    /**
     * 打印外箱SN--线下包装功能
     *
     * @param reqStr
     * @throws JSONException
     */
    @PostMapping(value = "/sapLabelPrintOut1")
    @ApiOperation("前端触发保存打印信息接口--线下包装")
    @ApiImplicitParams({@ApiImplicitParam(name = "reqStr", value = "json串", required = false)})
    @ApiResponses({@ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "ERP打印信息获取失败"),
            @ApiResponse(code = 2, message = "CODE表不存在数据"), @ApiResponse(code = 3, message = "接口异常")})
    public String sapLabelPrintOut1(String reqStr) throws Exception {
        JSONObject json = new JSONObject(reqStr);
        LOG.info("进入线下扫码打印外箱SN，收到的数据：" + json);
        return scadaPackService.sapLabelPrintOut1(json);
    }

    /**
     * 上位机获取标签打印信息
     *
     * @param msg
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public ScadaRespon getlable(Object msg) throws JSONException {

        LOG.info("进入包装线上位机获取标签信息");

        return scadaPackService.getlable();
    }

    /**
     * 上位机取标后告知
     *
     * @param msg
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public ScadaRespon labelOver(Object msg) throws IOException, JSONException {

        LOG.info("进入包装线上位机取标后告知");
        return scadaPackService.labelOver();
    }

    /**
     * 获取称重数据
     *
     * @throws JSONException
     */
    @PostMapping("/sapGetWeighData")
    @ApiOperation("前端获取称重数据接口")
    @ApiResponses({@ApiResponse(code = 0, message = "成功,返回重量一和重量二"), @ApiResponse(code = 1, message = "没有获取到称重数据"),
            @ApiResponse(code = 2, message = "上位机程序异常"), @ApiResponse(code = 3, message = "接口异常")})
    public String sapGetWeighData() throws JSONException {
        LOG.info("进入获取称重数据接口");

        return scadaPackService.sapGetWeighData();
    }

    /**
     * PQC与分拣触发放行
     *
     * @param reqStr
     * @return
     * @throws JSONException
     */
    @PostMapping("/discharged")
    @ApiOperation("前端触发PQC与分拣放行接口")
    @ApiImplicitParams({@ApiImplicitParam(name = "reqStr", value = "json串,TYPE与WORK", required = false)})
    @ApiResponses({@ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "上位机程序异常，无响应"),
            @ApiResponse(code = 2, message = "接口异常")})
    public String discharged(String reqStr) throws JSONException {
        JSONObject json = new JSONObject(reqStr);

        LOG.info("进入PQC与分拣触发放行，收到的数据：" + json);

        return scadaPackService.discharged(json);
    }

    /**
     * 内外标签是否一致确认放行接口
     *
     * @param data
     * @return
     */
    @RequestMapping("/inGoes")
    public String inGoes(String data, String sn) {
        try {
            LOG.info("进入内外标签是否一致接口，收到的数据：" + data + "sn  :" + sn);
        } catch (Exception e) {

        }
        String sn1 = sn.substring(3, 12);
        return scadaPackService.inGoes(data, sn1);
    }

    /**
     * 称重工位是否放行接口
     *
     * @param
     * @return
     */
    @RequestMapping("/weighSta")
    public String weighGoes(String reqStr) {

        LOG.info("进入称重工位是否放行接口，收到的数据：" + reqStr);
        JSONObject json = new JSONObject(reqStr);
        JSONObject jsonMes = json.getJSONArray("REQ").getJSONObject(0).getJSONObject("REQ_DATA");
        String status = jsonMes.getString("STATUS");
        return scadaPackService.weighGoes(status);
    }

    /**
     * 报工补报
     * baogong
     *
     * @param data
     * @return
     */
    @RequestMapping("/repairSn")
    public JSONPObject repairSn(HttpServletRequest req, String data) {
        String name = req.getParameter("callbackparam");//获取到jsonp的函数名
        LOG.info("进入报工补报，收到的数据：" + data);
        String s = scadaPackService.repairSn(data);
        return new JSONPObject(name, s);
    }

    /**
     * 批量报工
     * baogong
     *
     * @param data
     * @return
     */
    @RequestMapping("/repairSnAll")
    public JSONPObject repairSnAll(HttpServletRequest req, String data) {
        String name = req.getParameter("callbackparam");//获取到jsonp的函数名
        LOG.info("进入批量报工，收到的数据：" + data);
        String s = scadaPackService.repairSnAll(data);
        return new JSONPObject(name, s);
    }

    /**
     * 未进行报工的继续定时报工
     *
     * @param
     * @param
     * @return
     */
    @Scheduled(fixedDelay = 600000) //定时任务，间隔执行，每5分钟
    public void dynamicRepairSn() {
        LOG.info("《《《《《  进入动态循环报工  》》》》》");
        String s = scadaPackService.repairSnDynamic();
        LOG.info("动态循环报工情况:  " + s);
    }
}
