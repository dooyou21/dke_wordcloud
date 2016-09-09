package dke.sj.d3js;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@ServerEndpoint("/websocket")
public class websocket {
private static Set<Session> clients = Collections.synchronizedSet(new HashSet<Session>());
	private DBConnection dbcon = null;
	/*
	 * when message(including socket open message) received
	 */
	
	private String TridentSessionId;
	private Boolean IsTridentConnected = false;
	
	@OnMessage
	public void onMessage(String message, Session session) throws IOException {
		dbcon = new DBConnection();
		String m[] = message.split("::");//triednt 인지 client인지 판별
		if(m[0].equals("CLIENT")){ //client: client에서 온 메시지이면 키워드요청이므로 데이터 insert
//			System.out.println("client's message: "+m[1]);
			
			dbcon.insertKeywordsAndSessions(m[1], session.getId()); //connection close

			
		} else { //trident에서 온 메시지이면 skmap테이블에서 해당하는 키워드를 요청한 세션에게 보낼 json스트링에 추가함
//			System.out.println("trident's message: "+m[1]);
			TridentSessionId = session.getId();
			IsTridentConnected = true;
			
			ArrayList<String> allSessions = dbcon.selectAllSessions();
			
			if(allSessions.size()>0){
				
				JSONObject[] data = new JSONObject[allSessions.size()];
				for(int i=0, length=data.length;i<length;++i)
					data[i] = new JSONObject();
				
				String[] selectedSessions = null;
				
				JSONParser parser = new JSONParser();
				try {
					Object obj = parser.parse(m[1]);
					JSONObject jobj = (JSONObject) obj;
//					System.out.println(jobj);
					
					JSONArray jarr = (JSONArray) jobj.get("data");
					Iterator<Object> iterator = jarr.iterator();
					int idx;
					while(iterator.hasNext()) {
						JSONObject jobj2 = (JSONObject)parser.parse(iterator.next().toString());
						
						selectedSessions = dbcon.selectSession(jobj2.get("keyword").toString());
						//selectedSessions는 해당 키워드를 검색한 session들
						for(String s: selectedSessions) {
							idx = allSessions.indexOf(s);
							data[idx].put(jobj2.get("word"), jobj2.get("weight"));
						}
					}
					
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				for(int i=0;i<data.length;++i){
//					System.out.println("data: "+data[i]);
				}
				
				//client로 결과를 전송한다. 대상은 sessions에 포함된 세션
				synchronized(clients) {
					int i;
					for(String client: allSessions) {
//						System.err.println(client);
						i = allSessions.indexOf(client);
						if(client.equals(TridentSessionId)) continue;
//						System.err.println(i+","+data.length);
						if(!data[i].isEmpty()) {
							for(Session s: clients){
								if(s.getId().equals(client)){
									s.getBasicRemote().sendText(data[i].toString());
									break;
								}
							}
						}
					}
				}
			}
		}
		dbcon.close();
	}
	
	@OnOpen
	public void onOpen(Session session) {
//		System.out.println("open: "+session.getId());
		clients.add(session);
		//insert session은 message에서! trident와 client를 나눠야하기때문에
	}
	
	@OnClose
	public void onClose(Session session) {
		clients.remove("close: "+session.getId());
		if(IsTridentConnected){
			if(!session.getId().equals(TridentSessionId)){
				IsTridentConnected = false;
			} else {
				dbcon = new DBConnection();
				dbcon.deleteSession(session.getId());
				dbcon.close();
			}
		} else {
			dbcon = new DBConnection();
			dbcon.deleteSession(session.getId());
			dbcon.close();
		}
	}
	
	@OnError
	public void onError(Session session, Throwable t) {
//		System.out.println("error: "+session.getId());
		t.printStackTrace();
	}
	
	

}
