package cn.bp.scada.common.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局状态码定义
 */
public class ResponseCode  {

    /** JSON解析错误 */
    public static final int JSON_RESOLVE = 2;

    /** 类型不匹配 */
    public static final int TRANSTYPE_NO = 3;

    /** 系统处理正常 */
    public static final int SUCCESS_HEAD = 0;

    /** 系统处理未知异常 */
    public static final int EXCEPTION_HEAD = 1;

    /** src校验不通过 */
    public static final int HEAD_SRC_NULL = 10;

    /** 协议包含非法字符 */
    public static final int ILLEGAL_MESSAGE = 11;

    /** 数据库异常 */
    public static final int DATABASE_EXCEPTION = 9;

    public static final Map<Integer, String> RESP_INFO = new HashMap<Integer, String>();

    static {
        // Head 相关
        RESP_INFO.put(SUCCESS_HEAD, "系统处理正常");
        RESP_INFO.put(EXCEPTION_HEAD, "系统处理未知异常");
        RESP_INFO.put(JSON_RESOLVE, "JSON解析错误");
        RESP_INFO.put(TRANSTYPE_NO, "类型不匹配");
        RESP_INFO.put(DATABASE_EXCEPTION, "数据库异常");
        RESP_INFO.put(HEAD_SRC_NULL, "src未赋值");
        RESP_INFO.put(ILLEGAL_MESSAGE, "协议包含非法字符");
    }
}
