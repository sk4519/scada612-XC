package cn.bp.scada.sap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cn.bp.scada.common.utils.PrimaryHelper;

@Component
public class PrintLabel {
	@Resource
	private  PrimaryHelper ph;
	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	public String masterLabel(String sn,String line,String url,Map<String,Object> map){
		String result = "0"; //打印成功
	    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-ddHHmmss");
	    String fileName=sdf.format(new Date())+"file.txt";
	    String datesy = ph.getDatesy();

	    File file=new File(url+fileName);
	    if(file.exists()){
	     LOG.info("文件已存在");
	     file.delete();
	     result = "10"; //文件已存在
	    }else{
	     try{
	      file.createNewFile();
	      LOG.info("创建新文件，文件路径为："+url+fileName);
	     }catch(IOException e){
	    	 result = "3"; //创建新文件失败
	      e.printStackTrace();
	     }
	    }

	    try{
	    	Writer fw = new BufferedWriter(
					new OutputStreamWriter(
							new FileOutputStream(file), "GBK"));
	    	LOG.info("打印型号："+map.get("prodType"));
	     fw.write("打印方式@0|模板名称@小号标签|标签数量@1|序列号@"+sn+"|产品名称@"+map.get("prodName")+"|产品型号@"+map.get("prodType")+"|电源标识@"+map.get("powerLogo")+"|生产下达日期@"+map.get("proDt")+"");
	     fw.flush();
	     fw.close();
	     LOG.info("写入成功,时间为："+ph.getDateTime());

	    }catch(IOException e){
	    	result = "1"; //打印失败
	     e.printStackTrace();
	    }
	    return result ;
	   }

	public String masterLabelPack(String sn,String url,Map<String,Object> map){
		String result = "0"; //打印成功
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-ddHHmmss");
		String fileName=sdf.format(new Date())+"file.txt";
		String datesy = ph.getDatesy();

		File file=new File(url+fileName);
		if(file.exists()){
			LOG.info("文件已存在");
			file.delete();
			result = "10"; //文件已存在
		}else{
			try{
				file.createNewFile();
				LOG.info("创建新文件，文件路径为："+url+fileName);
			}catch(IOException e){
				result = "3"; //创建新文件失败
				e.printStackTrace();
			}
		}

		try{
			Writer fw = new BufferedWriter(
					new OutputStreamWriter(
							new FileOutputStream(file), "GBK"));
			LOG.info("打印型号："+map.get("prodType"));
			fw.write("打印方式@0|模板名称@大号标签|标签数量@1|机器序列号@"+sn+"|产品名称@"+map.get("prodName")+"|产品型号@"+map.get("prodType")+"|生产订单@"+map.get("O_NO")+"|销售订单@"+map.get("SALE_NO")+"|销售订单行项目@"+map.get("PRO_NO")+"|商品编码@"+map.get("PRO_NM")+"|产品编码@"+map.get("PRO_CD")+"|CPU@"+map.get("CPU")+"|内存@"+map.get("NC_NM")+"|硬盘1@"+map.get("YP_NM")+"|电源标识@"+map.get("powerLogo")+"|生产下达日期@"+map.get("proDt")+"");
			fw.flush();
			fw.close();
			LOG.info("写入成功,时间为："+ph.getDateTime());

		}catch(IOException e){
			result = "1"; //打印失败
			e.printStackTrace();
		}
		return result ;
	}

	public String masterLabelBm(String sn,String line,String url,String hard1,String hard2,String BMCARD,Map<String,Object> map){
		LOG.info("进入保密标签打印接口");
		int rad = (int) (Math.random() * 1000);
	    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
	    String fileName=sdf.format(new Date())+rad+"file.txt";
	    String result = "0"; //打印成功
	    File file=new File(url+fileName);
	    if(file.exists()){
	     LOG.info("文件已存在");
	     file.delete();
	     result = "10"; //文件已存在
	    }else{
	     try{
	      file.createNewFile();
	      LOG.info("保密机型打印，创建新文件，文件路径为："+url+fileName);
	     }catch(IOException e){
	    	 result = "3"; //创建新文件失败
	      e.printStackTrace();
	     }
	    }

	    try{
	    	Writer fw = new BufferedWriter(
					new OutputStreamWriter(
							new FileOutputStream(file), "GBK"));
			LOG.info("打印型号："+map.get("prodType"));
	    	 fw.write("打印方式@0|模板名称@全国产保密机标签模板|标签数量@1|序列号@"+sn+"|产品名称@"+map.get("prodName")+"|产品型号@"+map.get("prodType")+"|硬盘序列号@"+hard1+"|硬盘序列号1@"+hard2+"|标识码@"+BMCARD+"");
	     fw.flush();
	     fw.close();
	     LOG.info("写入成功,时间为："+ph.getDateTime());

	    }catch(IOException e){
	    	result = "1"; //打印失败
	     e.printStackTrace();
	    }
	    return result;
	   }
    }
