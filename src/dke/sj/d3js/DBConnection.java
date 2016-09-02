package dke.sj.d3js;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBConnection {
	
	final String driver = "com.mysql.jdbc.Driver";
	final String url = "jdbc:mysql://114.70.235.132:3306/demo";
	final String uId = "root";
	final String uPwd = "950926";
	private String keyword = "";
	private String session = "";
	
	Connection conn;
	PreparedStatement pstmt;
	ResultSet rs;
	
	DBConnection() {
		try{
			Class.forName("com.mysql.jdbc.Driver");

			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(url,uId,uPwd);
			
			if(conn != null)
				System.out.println("성공");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("드라이버 로드 실패");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("접속실패");
		}
		
	}
	
	public void insertKeywords(String keyword, String session) {
		this.keyword = keyword;
		this.session = session;
		String sql = "SELECT * FROM user";
		sql = "SELECT * FROM user";//INSERT INTO user VALUES (session, keyword) keyword수만큼 반복
		sql = "SELECT * FROM input";
		try {
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();

			while(rs.next()){
				System.out.println(rs.getString("uuid"));
				System.out.println(rs.getString("keyword"));
			}
			
			rs.close();
			pstmt.close();
		}catch(SQLException se){
			se.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{ 
			try{
				if(pstmt != null)
					pstmt.close();
			}catch(SQLException se2){
				
			}
			try{
				if(conn != null)
					conn.close();
			}catch(SQLException se){
				se.printStackTrace();
			}
		}
		//System.out.println("Goodbye");
	}
}
