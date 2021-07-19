package cn.bp.scada.common.utils.redis;

import cn.bp.scada.common.constant.DeviceCode;
import cn.bp.scada.common.constant.DeviceIp;
import cn.bp.scada.common.constant.Matpoint;
import cn.bp.scada.common.utils.PrimaryHelper;
import cn.bp.scada.common.utils.data.DateUtils;
import cn.bp.scada.controller.scada.ScadaAndAgvMt;
import cn.bp.scada.modle.scada.ScadaRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * redis五大数据类型使用实例
 */
@Slf4j
@Controller
public class RedisExample {
    @Autowired
    private RedisUtils redisUtil;
    @Autowired
    PrimaryHelper pri;
    @Resource
    private ScadaAndAgvMt smt ;

    /* ============================string=============================*/
    //设置单项string类型，并获取值
    public String setString(String key, String str) {
        redisUtil.set(key, str);
        return (String) redisUtil.get("str1");
    }

    //设置单项string类型，设置过期时间，并获取值
    public String setTimeString(String str) {
        redisUtil.set("str1", str, 30000);
        return (String) redisUtil.get("str1");
    }

    //递增某项key
    public long incrString(String str) {
        return redisUtil.incr("str1", 1);
    }

    //删除key,传入一个keys数组
    public void delKeys(String... keys) {
        redisUtil.del(keys);
    }

    @PostMapping("/redisString")
    @ResponseBody
    public void test() {
        setString("st1", "龙华");
        setString("st2", "元彪");
        setString("st3", "轩辕");
        String[] strArray = {"st1", "st2", "st3"};
        delKeys(strArray);
    }

    /* ============================hash=============================*/

    /**
     * 维护设备点位与IP的对应关系到Redis
     */
    @PostMapping("/insertPoint")
    @ResponseBody
    public Map<Object, Object> insertPoint() throws Exception {

        Class<?> clazz = Class.forName("cn.bp.scada.common.constant.Matpoint");
        Matpoint matpoint = (Matpoint) clazz.newInstance();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            switch (field.getName()) {
                case "GT_A_POINT_LINE1":
                    redisUtil.hset("matPoint", Matpoint.GT_A_POINT_LINE1, DeviceIp.GT_FJ_IP);
                    break;
                case "GT_C_POINT_LINE1":
                    redisUtil.hset("matPoint", Matpoint.GT_C_POINT_LINE1, DeviceIp.GT_FJ_IP);
                    break;
                case "GT_B_POINT_LINE2":
                    redisUtil.hset("matPoint", Matpoint.GT_B_POINT_LINE2, DeviceIp.GT_FJ_IP);
                    break;
                case "GT_D_POINT_LINE2":
                    redisUtil.hset("matPoint", Matpoint.GT_D_POINT_LINE2, DeviceIp.GT_FJ_IP);
                    break;
                case "ZB_EMPTY_POINT_LINE1":
                    redisUtil.hset("matPoint", Matpoint.ZB_EMPTY_POINT_LINE1, DeviceIp.ZB_IP_LINE1);
                    break;
                case "ZB_TAKE_POINT_LINE1":
                    redisUtil.hset("matPoint", Matpoint.ZB_TAKE_POINT_LINE1, DeviceIp.ZB_IP_LINE1);
                    break;
                case "JX_TAKE1_POINT_LINE1":
                    redisUtil.hset("matPoint", Matpoint.JX_TAKE1_POINT_LINE1, DeviceIp.JX_IP_LINE1);
                    break;
                case "JX_TAKE2_POINT_LINE1":
                    redisUtil.hset("matPoint", Matpoint.JX_TAKE2_POINT_LINE1, DeviceIp.JX_IP_LINE1);
                    break;
                case "JX_TAKE3_POINT_LINE1":
                    redisUtil.hset("matPoint", Matpoint.JX_TAKE3_POINT_LINE1, DeviceIp.JX_IP_LINE1);
                    break;
                case "CD_TAKE_POINT_LINE1":
                    redisUtil.hset("matPoint", Matpoint.CD_TAKE_POINT_LINE1, DeviceIp.CD_YP_IP_LINE1);
                    break;
                case "YP_TAKE_POINT_LINE1":
                    redisUtil.hset("matPoint", Matpoint.YP_TAKE_POINT_LINE1, DeviceIp.CD_YP_IP_LINE1);
                    break;
                case "JX_TAKE4_POINT_LINE1":
                    redisUtil.hset("matPoint", Matpoint.JX_TAKE4_POINT_LINE1, DeviceIp.JX_IP_LINE1);
                    break;
                case "JX_TAKE5_POINT_LINE1":
                    redisUtil.hset("matPoint", Matpoint.JX_TAKE5_POINT_LINE1, DeviceIp.JX_IP_LINE1);
                    break;
                case "TB_TAKE6_POINT_LINE1":
                    redisUtil.hset("matPoint", Matpoint.TB_TAKE6_POINT_LINE1, DeviceIp.TB_IP_LINE1);
                    break;
                case "TB_TAKE7_POINT_LINE1":
                    redisUtil.hset("matPoint", Matpoint.TB_TAKE7_POINT_LINE1, DeviceIp.TB_IP_LINE1);
                    break;
                case "TB_TAKE8_POINT_LINE1":
                    redisUtil.hset("matPoint", Matpoint.TB_TAKE8_POINT_LINE1, DeviceIp.TB_IP_LINE1);
                    break;
                case "TB_TAKE9_POINT_LINE1":
                    redisUtil.hset("matPoint", Matpoint.TB_TAKE9_POINT_LINE1, DeviceIp.TB_IP_LINE1);
                    break;

                case "ZB_EMPTY_POINT_LINE2":
                    redisUtil.hset("matPoint", Matpoint.ZB_EMPTY_POINT_LINE2, DeviceIp.ZB_IP_LINE2);
                    break;
                case "ZB_TAKE_POINT_LINE2":
                    redisUtil.hset("matPoint", Matpoint.ZB_TAKE_POINT_LINE2, DeviceIp.ZB_IP_LINE2);
                    break;
                case "JX_TAKE1_POINT_LINE2":
                    redisUtil.hset("matPoint", Matpoint.JX_TAKE1_POINT_LINE2, DeviceIp.JX_IP_LINE2);
                    break;
                case "JX_TAKE2_POINT_LINE2":
                    redisUtil.hset("matPoint", Matpoint.JX_TAKE2_POINT_LINE2, DeviceIp.JX_IP_LINE2);
                    break;
                case "JX_TAKE3_POINT_LINE2":
                    redisUtil.hset("matPoint", Matpoint.JX_TAKE3_POINT_LINE2, DeviceIp.JX_IP_LINE2);
                    break;
                case "CD_TAKE_POINT_LINE2":
                    redisUtil.hset("matPoint", Matpoint.CD_TAKE_POINT_LINE2, DeviceIp.CD_YP_IP_LINE2);
                    break;
                case "YP_TAKE_POINT_LINE2":
                    redisUtil.hset("matPoint", Matpoint.YP_TAKE_POINT_LINE2, DeviceIp.CD_YP_IP_LINE2);
                    break;
                case "JX_TAKE4_POINT_LINE2":
                    redisUtil.hset("matPoint", Matpoint.JX_TAKE4_POINT_LINE2, DeviceIp.JX_IP_LINE2);
                    break;
                case "JX_TAKE5_POINT_LINE2":
                    redisUtil.hset("matPoint", Matpoint.JX_TAKE5_POINT_LINE2, DeviceIp.JX_IP_LINE2);
                    break;
                case "TB_TAKE6_POINT_LINE2":
                    redisUtil.hset("matPoint", Matpoint.TB_TAKE6_POINT_LINE2, DeviceIp.TB_IP_LINE2);
                    break;
                case "TB_TAKE7_POINT_LINE2":
                    redisUtil.hset("matPoint", Matpoint.TB_TAKE7_POINT_LINE2, DeviceIp.TB_IP_LINE2);
                    break;
                case "TB_TAKE8_POINT_LINE2":
                    redisUtil.hset("matPoint", Matpoint.TB_TAKE8_POINT_LINE2, DeviceIp.TB_IP_LINE2);
                    break;
                case "TB_TAKE9_POINT_LINE2":
                    redisUtil.hset("matPoint", Matpoint.TB_TAKE9_POINT_LINE2, DeviceIp.TB_IP_LINE2);
                    break;
                default:

                    break;
            }
        }
        Map<Object, Object> matPointMap = redisUtil.hmget("matPoint");
        System.out.println("wei:" + matPointMap);
        return matPointMap;
    }

    /**
     * 往redis里存入上位机编码和对应的心跳时间
     *
     * @return
     */
    @PostMapping("/state")
    @ResponseBody
    public Map<String, Object> deviceSt() {
        Map<String, Object> map = new HashMap<>();
        map.put(DeviceCode.FJ_CODE, new Date());
        map.put(DeviceCode.ZB_CODE_LINE1,new Date());
        map.put(DeviceCode.JX_CODE_LINE1,new Date());
        map.put(DeviceCode.GQ_YP_CODE_LINE1,new Date());
        map.put(DeviceCode.TB_CODE_LINE1,new Date());
        map.put(DeviceCode.XDYZ_CODE_LINE1,new Date());
        map.put(DeviceCode.ZB_CODE_LINE2,new Date());
        map.put(DeviceCode.JX_CODE_LINE2,new Date());
        map.put(DeviceCode.GQ_YP_CODE_LINE2,new Date());
        map.put(DeviceCode.TB_CODE_LINE2,new Date());
        map.put(DeviceCode.XDYZ_CODE_LINE2,new Date());
        map.put(DeviceCode.OLD_CODE,new Date());
        map.put(DeviceCode.HC_XIDIAO,new Date());
        redisUtil.hmset("state", map);
        return map;
    }

    @PostMapping("/states")
    @ResponseBody
    public List<Integer> deviceSts() throws ParseException {
       /* redisUtil.del("device");
       List<Object> li = new ArrayList<>();
        li.add(DeviceCode.FJ_CODE);
        li.add(DeviceCode.ZB_CODE_LINE1);
        li.add(DeviceCode.JX_CODE_LINE1);
        li.add(DeviceCode.GQ_YP_CODE_LINE1);
        li.add(DeviceCode.TB_CODE_LINE1);
        li.add(DeviceCode.XDYZ_CODE_LINE1);
        li.add(DeviceCode.ZB_CODE_LINE2);
        li.add(DeviceCode.JX_CODE_LINE2);
        li.add(DeviceCode.GQ_YP_CODE_LINE2);
        li.add(DeviceCode.TB_CODE_LINE2);
        li.add(DeviceCode.XDYZ_CODE_LINE2);
        li.add(DeviceCode.OLD_CODE);
        li.add(DeviceCode.HC_XIDIAO);
        redisUtil.lSet("device",li);*/
        final List<Object> device = redisUtil.lGet("device", 0, -1);
        Map<Object, Object> map = redisUtil.hmget("state");
        List<Integer> list = new ArrayList<>();
        for (int i=0; i<device.size(); i++) {
            for (Object key:map.keySet()) {
                if(key.toString().equals(device.get(i))) {
                    String o = map.get(key).toString(); //值，时间
                    SimpleDateFormat sdf1= new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                    Date parse = sdf1.parse(o);
                    long datePoor = DateUtils.getDatePoorMin(new Date(), parse);
                    if(datePoor > 2) {
                        list.add(0);
                    }else {
                        list.add(1);
                    }
                    break;
                }

            }
        }



        return list;
    }

    /**
     * 全体灭灯，老化室
     */
    @PostMapping("/oldPoint")
    @ResponseBody
   public void oldPoint(String ism,String opFlag,String moNum) {
        ScadaRequest sr = new ScadaRequest();
        String A1 ="A0";
        int count1 =100;
        int count2 =200;
        int count3 =300;
        String  ET_IP = "192.168.10.253";
        if(ism .equals("1") ) {
            for (int i= 0; i<26 ;i++) {
                count1++;
                sr.setMat_point(A1+count1);
                sr.setOp_flag(opFlag);
                sr.setMo_num(moNum);
                sr.setEt_ip(ET_IP); //灭灯
                smt.toDevice(sr);
            }
        } else if(ism .equals("2")  ) {
            for (int i= 0; i<36 ;i++) {
                count2++;
                sr.setMat_point(A1+count2);
                sr.setOp_flag(opFlag);
                sr.setMo_num(moNum);
                sr.setEt_ip(ET_IP); //灭灯
                smt.toDevice(sr);
            }
        } else {
            for (int i= 0; i<34 ;i++) {
                count3++;
                sr.setMat_point(A1+count3);
                sr.setOp_flag(opFlag);
                sr.setMo_num(moNum);
                sr.setEt_ip(ET_IP); //灭灯
                smt.toDevice(sr);
            }
        }

    }
}
