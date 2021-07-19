package cn.bp.scada.common.utils.dbhelper;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cn.bp.scada.sap.bean.ProductSn;

@Component
public class DBHelper {

	@Resource
	private DBToolTest dbTest;
	private Logger LOG = LoggerFactory.getLogger(this.getClass());
	/**
	 * 连接数据库
	 *
	 * @return 链接数据库对象
	 */
	/*public Connection dbTest.getDateSource().getConnection() {
		Connection conn= null;
		try {
			Class.forName(DRIVER);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			conn = DriverManager.getConnection(URL, USER, PASSWORD);
			System.out.println("数据库连接："+conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conn;
	}
*/
	/**
	 * 释放相应的资源
	 *
	 * @param rs
	 * @param pstmt
	 * @param conn
	 */
	public void closeAll(ResultSet rs, PreparedStatement pstmt, Connection conn) {
		try {
			if (rs != null) {
				rs.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 此方法可以完成增删改所有的操作
	 *
	 * @param sql
	 * @param params
	 * @return true or false
	 */
	public boolean excuteUpdate(String sql, List<Object> params) {
		int res = 0;// 受影响的行数
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = dbTest.getDateSource().getConnection();
			pstmt = conn.prepareStatement(sql);// 装载sql语句
			if (params != null) {
				// 加入有？占位符，在执行之前把？占位符替换掉
				for (int i = 0; i < params.size(); i++) {
					pstmt.setObject(i + 1, params.get(i));
				}
			}
			res = pstmt.executeUpdate();
			LOG.info("mysql语句成功，sql为："+sql+",参数为："+params+"，受影响的行数res="+res);
		} catch (SQLException e) {
			LOG.info("mysql语句异常，sql为："+sql+",参数为："+params);
			e.printStackTrace();
		} finally {
			closeAll(rs, pstmt, conn);
		}
		return res > 0 ? true : false;
	}

	/**
	 * 批量新增测试程序LISTtoSN
	 * @param sql
	 * @param params
	 * @return
	 */
	public boolean addNewsPaper(String sql,List<ProductSn> params){//增
		int[] res ={};
		Connection conn = null;
		PreparedStatement ptmt = null;
		ResultSet rs = null;
		try {
			conn = dbTest.getDateSource().getConnection();
			conn.setAutoCommit(false);// 关闭事务
			ptmt = conn.prepareStatement(sql);// 装载sql语句



			for (ProductSn paperaper : params) {

				ptmt.setString(1, Integer.parseInt(paperaper.getAufnr())+"");
				ptmt.setString(2, paperaper.getSernr());
				ptmt.addBatch();
			}



			res = ptmt.executeBatch();//执行给定的SQL语句，该语句可能返回多个结果
			conn.commit();
		} catch (SQLException e) {
			LOG.info("mysql语句异常，sql为："+sql+",参数为："+params);
			e.printStackTrace();
		} finally {
			closeAll(rs, ptmt, conn);
		}
		return res[0] > 0 ? true : false;
	}


	/**
	 * 使用泛型方法和反射机制进行封装
	 *
	 * @param sql
	 * @param params
	 * @param cls
	 * @return
	 */
	public <T> List<T> executeQuery(String sql, List<Object> params, Class<T> cls) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<T> data = new ArrayList<T>();
		try {
			conn = dbTest.getDateSource().getConnection();
			pstmt = conn.prepareStatement(sql);// 装载sql语句
			if (params != null) {
				// 加入有？占位符，在执行之前把？占位符替换掉
				for (int i = 0; i < params.size(); i++) {
					pstmt.setObject(i + 1, params.get(i));
				}
			}
			rs = pstmt.executeQuery();
			// 把查询出来的记录封装成对应的实体类对象
			ResultSetMetaData rsd = rs.getMetaData();// 获得列对象,通过此对象可以得到表的结构，包括，列名，列的个数，列的数据类型
			while (rs.next()) {
				T m = cls.newInstance();
				for (int i = 0; i < rsd.getColumnCount(); i++) {
					String col_name = rsd.getColumnName(i + 1);// 获得列名
					Object value = rs.getObject(col_name);// 获得列所对应的值

					col_name = replacColName(col_name);// 将数据库带下划线的字段转化为驼峰规则的java类属性
					Field field = cls.getDeclaredField(col_name);
					field.setAccessible(true);// 给私有属性设置可访问权
					field.set(m, value);// 给对象的私有属性赋值
				}
				data.add(m);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			closeAll(rs, pstmt, conn);
		}
		return data;
	}

	/**
	 * 使用泛型方法和反射机制进行封装
	 *
	 * @param sql
	 * @param params
	 * @param cls
	 * @return
	 */
	public <T> List<Map<String,Object>> executeQueryTest(String sql, List<Object> params, Class<T> cls) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
		try {
			conn = dbTest.getDateSource().getConnection();
			pstmt = conn.prepareStatement(sql);// 装载sql语句
			if (params != null) {
				// 加入有？占位符，在执行之前把？占位符替换掉
				for (int i = 0; i < params.size(); i++) {
					pstmt.setObject(i + 1, params.get(i));
				}
			}
			rs = pstmt.executeQuery();
			// 把查询出来的记录封装成对应的实体类对象
			ResultSetMetaData rsd = rs.getMetaData();// 获得列对象,通过此对象可以得到表的结构，包括，列名，列的个数，列的数据类型
			while (rs.next()) {
					Map<String,Object> m = new HashMap<String,Object>();
					/*m.put("id", rs.getObject("id"));
					m.put("listId", rs.getObject("listId"));
					m.put("snNumber", rs.getObject("snNumber"));*/
					for (int i = 0; i < rsd.getColumnCount(); i++) {
						String col_name = rsd.getColumnName(i + 1);// 获得列名
						Object value = rs.getObject(col_name);// 获得列所对应的值

						m.put(col_name, value);
					}
					data.add(m);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			closeAll(rs, pstmt, conn);
		}
		return data;
	}

	/**
	 * 去掉数据库字段的下划线“_”，下划线之后的第一个字母大写
	 *
	 * @param col_name
	 * @return
	 */
	private String replacColName(String col_name) {
		String[] values = col_name.toLowerCase().split("_");
		String result = "";
		for (int i = 0; i < values.length; i++) {
			if (values[i].indexOf("ng") != -1 && values[i].length() < 4) {
				result += values[i].toUpperCase();
			} else {
				char[] cs = values[i].toCharArray();
				if (i > 0) {
					cs[0] -= 32;
				}
				result += String.valueOf(cs);
			}
		}
		return result;
	}
}