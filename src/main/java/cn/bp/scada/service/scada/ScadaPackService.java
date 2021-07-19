package cn.bp.scada.service.scada;

import cn.bp.scada.modle.scada.ScadaRespon;
import org.json.JSONObject;

public interface ScadaPackService {

    /**
     * 能耗标签打印
     * @param json
     * @return
     */
    String labelPrintNh(JSONObject json);

    /**
     * 上位机告知SCADA包装线哪条启用和关闭
     * @param json
     * @return
     */
    ScadaRespon packLine(JSONObject json);

    /**
     * 包装返回称重状态，是否进入OQC
     * @param json
     * @return
     */
    String weighStatus(JSONObject json);

    /**
     * 打印外箱SN
     * @return
     */
    String sapLabelPrintOut(JSONObject json) throws Exception;

    /**
     * 打印外箱SN--线下包装功能
     * @return
     */
    String sapLabelPrintOut1(JSONObject json) throws Exception;

    /**
     * 上位机获取标签打印信息
     * @return
     */
    ScadaRespon getlable();

    /**
     * 上位机取标后告知
     * @return
     */
    ScadaRespon labelOver();

    /**
     * 获取称重数据
     * @return
     */
    String sapGetWeighData();

    /**
     * PQC与分拣触发放行
     * @param json
     * @return
     */
    String discharged(JSONObject json);

    /**
     * 内外标签是否一致确认放行接口
     * @param data
     * @return
     */
    String inGoes(String data,String sn);

    /**
     * 称重工位是否放行
     * @param data
     * @return
     */
    String weighGoes(String data);

    /**
     * 报工补报
     * @param data
     * @return
     */
    String repairSn(String data);

    /**
     * 批量报工
     * @param data
     * @return
     */
    String repairSnAll(String data);

    /**
     * 动态补报
     * @param
     * @return
     */
    String repairSnDynamic();
}
