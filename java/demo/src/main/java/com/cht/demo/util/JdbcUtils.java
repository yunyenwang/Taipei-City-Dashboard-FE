package com.cht.demo.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * JDBC 小工具
 * 
 * @author rickwang
 *
 */

@Slf4j
public class JdbcUtils {

	/**
	 * 使用 PreparedStatement 執行一段更新指令
	 * 
	 * @param con
	 * @param sql
	 * @param editor
	 * @throws SQLException
	 */
	public static int update(Connection con, String sql, Consumer<PreparedStatement> editor) throws SQLException {
		try (var ps = con.prepareStatement(sql)) {
			if (editor != null) {
				editor.accept(ps);
			}
				
			var count = ps.executeUpdate();
			log.info("Affected: {} by {}", count, sql);
			
			return count;
		}
	}
	
	public static int update(Connection con, String sql) throws SQLException {
		return update(con, sql, null);
	}
	
	public static void query(
			Connection con,
			String sql,
			Consumer<PreparedStatement> editor,
			Consumer<ResultSet> consumer) throws SQLException {
		
		try (var ps = con.prepareStatement(sql)) {
			if (editor != null) {
				editor.accept(ps);
			}
				
			var rs = ps.executeQuery();
			consumer.accept(rs);
		}
	}
	
	public static int executeBatch(PreparedStatement ps) throws SQLException {
		int count = 0;
		for (var c : ps.executeBatch()) {
			count += c;
		}
		ps.clearBatch();
		
		return count;
	}
	
	public static void setString(PreparedStatement ps, int index, String s) throws SQLException {
		if (StringUtils.isNotBlank(s)) {
			ps.setString(index, s);
			
		} else {
			ps.setNull(index, Types.VARCHAR);
		}
	}
}
