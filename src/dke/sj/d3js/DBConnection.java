package dke.sj.d3js;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBConnection {
	
	final String driver = "com.mysql.jdbc.Driver";
	final String url = "jdbc:mysql://114.70.235.68:3306/tagcloud";
	final String uId = "wordcloud";
	final String uPwd = "160826";
	private String keyword[];
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
	
	public void insertKeywordsAndSessions(String message, String session) {
		this.keyword = message.split(",");
		this.session = session;
		
		
		/*
		 * keyword_tb에 이미 존재하는 키워드인지 확인 해야함
		 * 이미존재하는 키워드이면 keyword_tb의 해당 keyword의 usage를 증가시킴
		 * 
		 * ??jdbc트랜잭션..?
		 *  
		 */
		//insert keyword
		String sql1 = "INSERT INTO keyword_tb (keyword) VALUES ('";
		int length = keyword.length;
		sql1 = sql1 + keyword[0] + "')"; //0을 자동생성으로 넣을까?
		for(int i=1;i<length;++i){
			sql1 = sql1 +", ('" + keyword[i] + "')";
		}
		
		//insert session
		String sql2 = "INSERT INTO session_tb (session) VALUES ('" + this.session + "')";
		
		//select s_id from session_tb, k_id from keyword_tb and insert these into session_keyword_tb
		String sql3_2 = "SELECT id FROM session_tb WHERE session = '" + this.session.toString();
		String sql3_1 = "SELECT id FROM keyword_tb WHERE keyword = 'exist keyword'";
		String sql3 = "INSERT INTO session_keyword_tb (s_id, k_id) VALUES (" + sql3_2 + ", " + sql3_1 + ")";
		
		try {
			
//			pstmt = conn.prepareStatement(sql);
//			rs = pstmt.executeQuery();
//			while(rs.next()) {
//				System.out.println(rs.getString("keyword"));
//			}
//			rs.close();
//			pstmt.close();
			
			
			//sql1 keyword insert
			pstmt = conn.prepareStatement(sql1);
			int result = pstmt.executeUpdate();
			if(result == length) {
				System.out.println("success: " + result);
			}
			pstmt.close();

			//sql2 session insert
			pstmt = conn.prepareStatement(sql2);
			result = pstmt.executeUpdate();
			if(result == length) {
				System.out.println("success: " + result);
			}
			pstmt.close();

//			//sql3
//			pstmt = conn.prepareStatement(sql3);
//			pstmt.executeUpdate();
//			pstmt.close();
			
			
			
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
	
	public void deleteSession(String session) {
		/*
		 * 
		 * 오류발생(클라이언트 연결 강제종료)시
		 * 
		 * session 테이블에서 session usage 0으로변경
		 * keyword-session 테이블에서 session이 0인 녀석들 키워드 제거
		 * keyword 제거된 놈 keyword테이블에서 usage 숫자변경(-1)
		 * keyword usage 0인 놈 keyword테이블에서 제거
		 *  
		 */

		
	}	
}
