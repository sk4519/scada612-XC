package cn.bp.scada.service.scada;

import com.google.gson.JsonObject;
import org.json.JSONObject;

public interface OEEService {
    /**
     * 时间段设定
     *
     * @param json
     * @return
     */
    String OEETimeSet(JSONObject json);
    /**
     * 时间段设定
     *
     * @param json
     * @return
     */
//    String OEEClassSet(JSONObject json);
    String OEETimeAClassSet(JSONObject json);
}