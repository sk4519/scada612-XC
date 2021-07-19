package cn.bp.scada.controller.scada;

import cn.bp.scada.controller.sap.SapCpletionConfir;
import cn.bp.scada.modle.scada.ScadaRespon;
import cn.bp.scada.service.scada.ScadaCameraService;
import cn.bp.scada.common.utils.redis.RedisUtils;
import cn.bp.scada.service.scada.impl.OtherLostRequest;
import io.swagger.annotations.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 前端访问数据模板格式
 * {"REQ":[{"REQ_DATA":{"print":true,"CRATE":"131002558","IFS":"sapLabelPrintOut","PROCESS":"ASSY0501","reqType":"APP","WORK":"G001","KEYPN":"V120F0FHE0104000","USER":"L109001"}}]}
 */

@Api(value = "相机拍照控制层", tags = {"PQC触发拍照"})
@RestController
public class ScadaCameraController {

    @Autowired
    private OtherLostRequest otherLostRequest;
    @Autowired
    private RedisUtils redisUtil;
    @Resource
    private ScadaCameraService camera;
    @Resource
    private RedisUtils redisUtils;
    @Autowired
    private SapCpletionConfir machineConfir;

    private int count = 0;
    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    @PostMapping("/cameraPQC")
    @ApiOperation(value = "PQC触发拍照")
    @ApiImplicitParam(name = "reqStr", value = "json字符串，需要的字段：工位(WORK)与机箱SN(CRATE)", required = false, dataType = "String")
    @ApiResponses({@ApiResponse(code = 0, message = "拍照成功"), @ApiResponse(code = 1, message = "上位机拍照失败"),
            @ApiResponse(code = 2, message = "上位机无响应，拍照失败,请检查上位机程序"), @ApiResponse(code = 3, message = "接口异常")})
    public String cameraPQC(String reqStr) throws Exception {
        ScadaRespon scadaRespon;
        String result = "";
        JSONObject json = new JSONObject(reqStr);
        JSONObject js = new JSONObject();
        System.out.println("进入触发PQC拍照，收到的数据：" + json);
        JSONObject jsonMes = json.getJSONArray("REQ").getJSONObject(0).getJSONObject("REQ_DATA");

        try {
            String work = jsonMes.getString("WORK"); //工位
            String sn = jsonMes.getString("CRATE"); //SN
            scadaRespon = camera.cameraPQC(sn, work);
            result = scadaRespon.getResult_flag();
            if (result.equals("0")) {
                js.put("CODE", "0");
                js.put("MSG", "拍照成功");
                js.put("ID", scadaRespon.getItem_cd());
                //调取损失工时接口
                if (work.equals("C013")) {
                    otherLostRequest.requestLost("机箱组装A线体");
                } else if(work.equals("C213")) {
                    otherLostRequest.requestLost("机箱组装B线体");
                }
            } else if (result.equals("1")) {
                js.put("CODE", "1");
                js.put("MSG", "上位机拍照失败，请检查程序");
            } else if (result.equals("2")) {
                js.put("CODE", "2");
                js.put("MSG", "上位机无响应，拍照失败,请检查上位机程序");
            } else {
                js.put("CODE", "4");
                js.put("MSG", "图片保存数据库失败，请重新触发拍照");
            }
            try {
                String sure = machineConfir.cpletionConfir(sn, "10");//10完工确认
                LOG.info("完工确认10接口哦已返回" + sure + "sn号码为：" + sn);
            } catch (Exception e) {
                LOG.info("完工确认10接口哦已返回异常!" + "sn号码为：" + sn);
                e.printStackTrace();
            }
        } catch (Exception e) {
            js.put("CODE", "3");
            js.put("MSG", "上位机无响应，拍照失败,请重新拍照");
            e.printStackTrace();
        }
        LOG.info("返回PQC拍照给前端：" + js);
        return js.toString();
    }

    /**
     * 自己切换模式前测
     *
     * @param iscg OK：参观 NO:生产
     * @return
     */
    @PostMapping("/iscgs")
    public String xidiaoCg(String iscg) {
        String result = "";
        try {
            redisUtils.set("iscg", iscg);
            if (iscg.equals("OK")) {
                LOG.info("收到自己前测切换参观模式");
                result = "收到自己前测切换参观模式";
            } else {
                LOG.info("收到自己前测切换生产模式");
                result = "收到自己前测切换生产模式";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
