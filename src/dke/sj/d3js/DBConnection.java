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
	private String s_id;
	private int result;
	
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
		int length = keyword.length;
		System.out.println(length+"");
		
		/*
		 * 
		 * ??jdbc트랜잭션..?
		 * 이미 존재하는 세션일 경우 예외처리...?
		 *  
		 */
		//insert session
		String sql = "INSERT INTO session_tb (session) VALUES ('" + this.session + "')";
		try {
			pstmt = conn.prepareStatement(sql);
			result = pstmt.executeUpdate();
			if(result > 0) {
				System.out.println("session insert successed!");
			}
			pstmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//get session id
		sql = "SELECT id FROM session_tb WHERE session = '" + this.session + "'";
		try {
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				s_id = rs.getString("id");
				System.out.println("session id: "+s_id);
			}
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			String sql2, sql3;
			//반복문필요. 각 키워드마다 수행해야함
			String k_id = "";
			int	k_usage = 0;
			for(int i=0;i<length;++i) {
				//keyword exist or not
				sql = "SELECT id, `usage` FROM keyword_tb WHERE keyword = '" + keyword[i] + "'"; // 'usage' 기존에 있는 예약어..?
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				
				if (!rs.next()) {  //empty rs == new keyword
				    System.out.println("new keyword");
					//insert keyword
					sql2 = "INSERT INTO keyword_tb (keyword) VALUES ('" + keyword[i] + "')";
					pstmt = conn.prepareStatement(sql2);
					result = pstmt.executeUpdate();
					//select k_id
					sql2 = "SELECT id FROM keyword_tb WHERE keyword = '" + keyword[i] + "'";
					pstmt = conn.prepareStatement(sql2);
					rs = pstmt.executeQuery();
					while(rs.next()){
						k_id = rs.getString("id");
					}
					System.out.println("get k_id"+k_id);
				} else { // exist keyword
					do {
						k_id = rs.getString("id");
						k_usage = Integer.parseInt(rs.getString("usage"));
						System.out.println("keyword already exist. keyword_id: "+k_id);
						System.out.println("keyword already exist. usage: "+k_usage);
						++k_usage;
						sql2 = "UPDATE keyword_tb SET `usage` = " + k_usage + " WHERE id = " + k_id;
						pstmt = conn.prepareStatement(sql2);
						result = pstmt.executeUpdate();
					  } while (rs.next());
				}
				
				sql3 = "INSERT INTO session_keyword_tb (s_id, k_id) VALUES(" + s_id + ", " + k_id + ")";
				pstmt = conn.prepareStatement(sql3);
				result = pstmt.executeUpdate();
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
