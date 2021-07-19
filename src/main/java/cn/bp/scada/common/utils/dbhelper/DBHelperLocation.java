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

import org.springframework.stereotype.Component;

@Component
public class DBHelperLocation {

	@Resource
	private DBToolLocation dbLocation;

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
			conn = dbLocation.getDateSource().getConnection();
			pstmt = conn.prepareStatement(sql);// 装载sql语句
			if (params != null) {
				// 加入有？占位符，在执行之前把？占位符替换掉
				for (int i = 0; i < params.size(); i++) {
					pstmt.setObject(i + 1, params.get(i));
				}
			}
			res = pstmt.executeUpdate();

		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			closeAll(rs, pstmt, conn);
		}
		return res > 0 ? true : false;
	}
/*
	*//**
	 * 批量插入
	 *
	 * @param sql
	 * @param params
	 * @return true or false
	 *//*
	public boolean excuteInsert(String sql, List<Map<String, Object>> params,int num) {
		int res[] = {};// 受影响的行数
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = dbLocation.getDateSource().getConnection();
			pstmt = conn.prepareStatement(sql);// 装载sql语句
			conn.setAutoCommit(false);
			if (params != null) {
				// 加入有？占位符，在执行之前把？占位符替换掉
				for (int i = 0; i < params.size(); i++) {
					for (int j = 0; j < num; j++) {
						pstmt.setObject(j + 1, params.get(j).get("rn"));

					}
					pstmt.addBatch();
				}
			}
			res = pstmt.executeBatch();
			conn.commit();

		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			closeAll(rs, pstmt, conn);
		}
		return res[0] > 0 ? true : false;
	}
	*/

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
			conn = dbLocation.getDateSource().getConnection();
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
			conn = dbLocation.getDateSource().getConnection();
			//System.out.println("数据库连接："+conn);
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