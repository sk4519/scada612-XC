package cn.bp.scada.service.scada;

import cn.bp.scada.modle.scada.ScadaRespon;
import org.json.JSONException;
import org.json.JSONObject;

public interface ScadaCameraService {
    /**
     * 将图片转为二进制
     *
     * @param Imgpath
     * @return
     */
    String getImageBinary(String Imgpath);

    /**
     * 获取二进制图片字符串进行保存
     */
    ScadaRespon base64StringToImage(JSONObject json) throws JSONException;


    /**
     * 触发上位机拍照
     *
     * @param proSn
     * @param work
     * @throws Exception
     */
    ScadaRespon cameraPQC(String proSn, String work) throws Exception;

    /**
     * 拍照功能
     * @return
     */
    String takePictures();
}
