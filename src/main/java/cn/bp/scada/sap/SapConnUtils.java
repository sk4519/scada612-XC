package cn.bp.scada.sap;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.ext.DestinationDataProvider;
import cn.bp.scada.sap.bean.SapBean;


@Component
public class SapConnUtils {
	 private static final String ABAP_AS_POOLED = "ABAP_AS_WITH_POOL";
    private static Logger logger = LoggerFactory.getLogger(SapConnUtils.class);
	 public  JCoDestination jCoDestination=null;

	 public SapConnUtils(){
		 connect();
	 }

	 /**
	     * 创建SAP接口属性文件。
	     * @param name  ABAP管道名称
	     * @param suffix    属性文件后缀
	     * @param properties    属性文件内容
	     */
	    private static void createDataFile(String name, String suffix, Properties properties){
	        File cfg = new File(name+"."+suffix);
	        if(cfg.exists()){
	            cfg.deleteOnExit();
	        }
	        try{
	            FileOutputStream fos = new FileOutputStream(cfg, false);
	            properties.store(fos, "for tests only !");
	            fos.close();
	        }catch (Exception e){
                logger.error("Create Data file fault, error msg: " + e.toString());
	            throw new RuntimeException("Unable to create the destination file " + cfg.getName(), e);
	        }
	    }

	    /**
	     * 初始化SAP连接
	     */
	    private static void initProperties(SapBean sapBean) {
	        Properties connectProperties = new Properties();
	        // SAP服务器
	        connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, sapBean.getJCO_ASHOST());
	        // SAP系统编号
	        connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR,  sapBean.getJCO_SYSNR());
	        // SAP集团
	        connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, sapBean.getJCO_CLIENT());
	        // SAP用户名
	        connectProperties.setProperty(DestinationDataProvider.JCO_USER,   sapBean.getJCO_USER());
	        // SAP密码
	        connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, sapBean.getJCO_PASSWD());
	        // SAP登录语言
	        connectProperties.setProperty(DestinationDataProvider.JCO_LANG,   sapBean.getJCO_LANG());
	        // 最大连接数
	        connectProperties.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY, sapBean.getJCO_POOL_CAPACITY());
	        // 最大连接线程
	        connectProperties.setProperty(DestinationDataProvider.JCO_PEAK_LIMIT, sapBean.getJCO_PEAK_LIMIT());
	        // SAP ROUTER
	        connectProperties.setProperty(DestinationDataProvider.JCO_SAPROUTER, sapBean.getJCO_SAPROUTER());

	        //createDataFile(ABAP_AS_POOLED, "jcoDestination", connectProperties);
	    }

	    /**
	     * 获取SAP连接
	     * @return  SAP连接对象
	     */
	    public  JCoDestination connect(){
	    	logger.info("正在连接至SAP...");
	        SapBean sapBean = new SapBean("10.113.1.26","00","888","SCADA_JK","inspur","ZH","150","25","");//实例化连接参数
	        initProperties(sapBean);  // 10.111.4.1     实例编号：51  测试系统
	        						// 10.113.1.26     实例编号：00  正式系统
	        try {
	        	jCoDestination = JCoDestinationManager.getDestination(ABAP_AS_POOLED);
	        	jCoDestination.ping();
	            logger.info("已成功建立sap的连接");

	        } catch (JCoException e) {
	        	logger.error("Connect SAP fault, error msg: " + e.toString());
	        }


	        return jCoDestination;
	    }

	    /**
		 * 获取rfc函数
		 * @param funcName 函数名称
		 * @param
		 * @return
		 */
		public  JCoFunction getFunction(String funcName){
			JCoFunction function = null;//调用rfc函数
			try {
				function = jCoDestination.getRepository().getFunction(funcName);
			} catch (JCoException e) {
				throw new RuntimeException("get function not found in sap");
			}
			return function;
		}
}
