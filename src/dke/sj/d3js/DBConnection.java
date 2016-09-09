package dke.sj.d3js;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DBConnection {
	
	final String driver = "com.mysql.jdbc.Driver";
//	final String url = "jdbc:mysql://114.70.235.71:3306/tagcloud";
//	final String uId = "root";
//	final String uPwd = "160905";
	final String url = "jdbc:mysql://114.70.235.40:3306/tagcloud";
	final String uId = "tagcloud";
	final String uPwd = "dke304";
	
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
	
	public void close() {
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * client 정보 저장(session, keyword)
	 * 
	 * ??jdbc트랜잭션..?
	 * 이미 존재하는 세션일 경우 예외처리...?
	 */
	public void insertKeywordsAndSessions(String message, String session) {
		String keyword[];
		String s_id = "";
		int result;
		keyword = message.split(",");
		int length = keyword.length;
		
		//insert session
		String sql = "INSERT INTO session_tb (session) VALUES ('" + session + "')";
		try {
			pstmt = conn.prepareStatement(sql);
			result = pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//get session id
		sql = "SELECT id FROM session_tb WHERE session = '" + session + "'";
		try {
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				s_id = rs.getString("id");
			}
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
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
				} else { // exist keyword
					do { // ? do while 없어도되나...?
						k_id = rs.getString("id");
						k_usage = Integer.parseInt(rs.getString("usage"));
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
	}
	
	/*
	 * websocket connection 끊겼을 때 database수정
	 *  
	 */
	public void deleteSession(String session) {
		String s_id = "";
		String sql1;
		try{ //s table에서 현 세션의 id 가져옴
			sql1 = "SELECT id FROM session_tb WHERE session = '" + session + "'";
			pstmt = conn.prepareStatement(sql1);
			rs = pstmt.executeQuery();
			while(rs.next()){
				s_id = rs.getString("id");
			}
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} 

		ArrayList<String> k_id_arr = new ArrayList<>(); // k_id의 배열
		ArrayList<Integer> k_usage_arr = new ArrayList<>();

		try{ //sk table에서 k_id set 가져옴
			sql1 = "SELECT k_id FROM session_keyword_tb WHERE s_id = '" + s_id + "'";
			pstmt = conn.prepareStatement(sql1);
			rs = pstmt.executeQuery();
			while(rs.next()){
				k_id_arr.add(rs.getString("k_id"));
			}
			rs.close();
			pstmt.close();
			
			
			sql1 = "SELECT `usage`, id FROM keyword_tb";
			pstmt = conn.prepareStatement(sql1);
			rs = pstmt.executeQuery();
			
			int i=0;//usage arr를 저장하기 위한 반복자
			int length = k_id_arr.size();
			while(rs.next() && (i<length)){
				if(rs.getString("id").equals(k_id_arr.get(i))) {
					k_usage_arr.add(Integer.parseInt(rs.getString("usage")) - 1);
					i++;
				} else {
				}
			}
			rs.close();
			pstmt.close();
			
			//k table에서 해당 k_id의 usage를 1씩 감소한 것(k_usage_arr)을 업데이트
			for(i=0, length=k_usage_arr.size();i<length;i++){
				pstmt = conn.prepareStatement("UPDATE keyword_tb SET `usage` = " + k_usage_arr.get(i) + " WHERE id = '" + k_id_arr.get(i) +"'");
				pstmt.executeUpdate();
				pstmt.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		try { 
			//sk table에서 s_id가 현 세션인 것 모두 제거
			sql1 = "DELETE FROM session_keyword_tb WHERE s_id = '" + s_id + "'";
			pstmt = conn.prepareStatement(sql1);
			pstmt.executeUpdate();
			
			//k table 에서 usage가 0인 것 제거
			sql1 = "DELETE FROM keyword_tb WHERE `usage` < 1";
			pstmt = conn.prepareStatement(sql1);
			pstmt.executeUpdate();
			
			//s table에서 현 세션 제거
			sql1 = "DELETE FROM session_tb WHERE id = '" + s_id + "'";
			pstmt = conn.prepareStatement(sql1);
			pstmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}	
	
	/*
	 * 
	 * Trident로부터 들어온 keyword에 대한 Tag를 보낼 session찾기
	 * 
	 */
	public String[] selectSession(String kw) {
		
		String sql;
		String[] sessions = null;
		ArrayList<String> sessions_arr = new ArrayList<>();
		ArrayList<String> k_id_arr = new ArrayList<>();
		ArrayList<String> s_id_arr = new ArrayList<>();
		
		//k table에서 keyword=kw 인 것의 id 가져옴
		try {
			sql = "SELECT id FROM keyword_tb WHERE keyword = '" + kw + "'";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				k_id_arr.add(rs.getString("id"));
			}
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//sk table에서 k_id가 위에서 찾은 id인 s_id가져옴
		try {
			for(int i=0, length = k_id_arr.size();i<length;++i) {
				sql = "SELECT s_id FROM session_keyword_tb WHERE k_id = " + k_id_arr.get(i);
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				while(rs.next()){
					s_id_arr.add(rs.getString("s_id"));
				}
			}
		} catch (SQLException e){
			e.printStackTrace();
		}
		//s table애서 id=s_id인 것의 session 가져옴
		try {
			for(int i=0, length = s_id_arr.size();i<length;++i) {
				sql = "SELECT session FROM session_tb WHERE id = " + s_id_arr.get(i);
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				while(rs.next()){
					sessions_arr.add(rs.getString("session"));
				}
			}
		} catch (SQLException e){
			e.printStackTrace();
		}
		
		sessions = new String[sessions_arr.size()];
		sessions = sessions_arr.toArray(sessions);
		
		return sessions;
	}
	
	public ArrayList<String> selectAllSessions() {
		ArrayList<String> sessions_arr = new ArrayList<>();
		String sql;
		try {
			sql = "SELECT session FROM session_tb";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				sessions_arr.add(rs.getString("session"));
			}
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sessions_arr;
	}
}
