package cn.bp.scada.common.utils.dbhelper;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.stereotype.Component;

/**
 * DIAG测试程序数据库连接池
 * @author Administrator
 *
 */
@Component
public class DBToolTest {
	//建立连接的驱动名称
    public static final String DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";
    //数据库链接数据哭的url
    public static final String URL = "jdbc:mysql://10.50.0.94:3306/laohua";
    //链接的数据库账号
    public static final String USERNAME = "qryinfo";
    //链接的数据库密码
    public static final String PASSWORD = "qryinfo123";
    //最大空闲链接
    private static final int MAX_IDLE = 20;
    //最小空闲连接
    private static final int MinIdle=8;
    //最大等待时间
    private static final long MAX_WAIT_Millis = 15000;
    //最大活动链接
    private static final int MAX_TOTAl = 9;
    //初始化时链接池的数量
    private static final int INITIAL_SIZE = 25;

  //一个被抛弃连接可以被移除的超时时间
    private static final int RemoveAbandonedTimeout=180;
    //只会发现当前连接失效，再创建一个连接供当前查询使用
    private static final boolean TOBESTNORROW =true;

    private static  BasicDataSource DATA_SOURCE;
    static {
        DATA_SOURCE=new BasicDataSource();
        DATA_SOURCE.setDriverClassName(DRIVER_CLASS_NAME);
        DATA_SOURCE.setUrl(URL);
        DATA_SOURCE.setUsername(USERNAME);
        DATA_SOURCE.setPassword(PASSWORD);
        DATA_SOURCE.setMaxTotal(MAX_TOTAl);
        DATA_SOURCE.setInitialSize(INITIAL_SIZE);
        DATA_SOURCE.setMinIdle(MinIdle);
        DATA_SOURCE.setMaxIdle(MAX_IDLE);
        DATA_SOURCE.setMaxWaitMillis(MAX_WAIT_Millis);
        DATA_SOURCE.setTestOnBorrow(TOBESTNORROW);
        DATA_SOURCE.setRemoveAbandonedTimeout(RemoveAbandonedTimeout);

    }
    //提供获得数据源
    public  DataSource getDateSource(){
        return DATA_SOURCE;
    }
    //提供获得链接
    public  Connection getConnection() throws SQLException {
        return DATA_SOURCE.getConnection();
    }

}
