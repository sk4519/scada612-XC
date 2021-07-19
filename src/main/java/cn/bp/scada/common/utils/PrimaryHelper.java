package cn.bp.scada.common.utils;

import java.text.*;
import java.util.Calendar;
import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class PrimaryHelper {

    /**
     *当前月份加时分秒加4位数的随机数生成主键id
     * @return
     */
    public  String getId() {
        Format dateformat = new SimpleDateFormat("MddHHmmss");
        FieldPosition position = new FieldPosition(0);
        Calendar now = Calendar.getInstance();
        StringBuffer sb = new StringBuffer();
        ((SimpleDateFormat) dateformat).format(now.getTime(), sb, position);
        int x = (int) (Math.random() * 100);
        sb.append(x);
        return sb.toString();
    }

    /**
     *当前月份加时分秒加5位数的随机数生成主键id
     * @return
     */
    public  String getIdFive() {
        Format dateformat = new SimpleDateFormat("MddHHmmss");
        FieldPosition position = new FieldPosition(0);
        Calendar now = Calendar.getInstance();
        StringBuffer sb = new StringBuffer();
        ((SimpleDateFormat) dateformat).format(now.getTime(), sb, position);
        int x = (int) (Math.random() * 1000);
        sb.append(x);
        return sb.toString();
    }

    /**
     * 单个获取时间
     * @return
     */
    public  String getTime(){
    	Date d = new Date();
    	SimpleDateFormat sim = new SimpleDateFormat("HHmmss");
    	String format = sim.format(d);

    	return format;
    }

    /**
     * 单个获取日期
     * @return
     */
    public  String getDates(){
    	Date d = new Date();
    	SimpleDateFormat sim = new SimpleDateFormat("yyyyMMdd");
    	String format = sim.format(d);
    	return format;
    }

    public  String getDateDev(){
        Date d = new Date();
        SimpleDateFormat sim = new SimpleDateFormat("yyyy/MM/dd");
        String format = sim.format(d);
        return format;
    }

    /**
     * 获取日期,机箱打印
     * @return
     */
    public  String getDatesy(){
    	Date d = new Date();
    	SimpleDateFormat sim = new SimpleDateFormat("yyyy.MM");
    	String format = sim.format(d);
    	return format;
    }

    /**
     * 获取日期时间
     * @return
     */
    public  String getDateTime(){
    	Date d = new Date();
    	SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	String format = sim.format(d);

    	return format;
    }
    /**
     * 获取日期时间
     * @return
     */
    public  String getDateTimes(){
        Date d = new Date();
        SimpleDateFormat sim = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String format = sim.format(d);

        return format;
    }

    /**
     * 获取PQC拍照日期时间
     * @return
     */
    public  String getCameraDateTime(){
        Date d = new Date();
        SimpleDateFormat sim = new SimpleDateFormat("HHmmss");
        String format = sim.format(d);

        return format;
    }

    /**
     * 获取传入参数的时分秒
     * @param da
     * @return
     */
    public String getStrTime(String da){
    	SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	String format=null;
    	try {
			Date d = sim.parse(da);
			SimpleDateFormat sim2 = new SimpleDateFormat("HH:mm:ss");
	    	 format = sim2.format(d);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return format;
    }



}
