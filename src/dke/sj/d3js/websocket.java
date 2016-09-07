package dke.sj.d3js;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
	
	@OnMessage
	public void onMessage(String message, Session session) throws IOException {
		String m[] = message.split("::");//triednt 인지 client인지 판별
		if(m[0].equals("CLIENT")){ //client: client에서 온 메시지이면 키워드요청이므로 데이터 insert
			System.out.println("client's message: "+m[1]);
			
			dbcon = new DBConnection();//connection open
			dbcon.insertKeywordsAndSessions(m[1], session.getId()); //connection close

//			synchronized(clients) {
//				for(Session client: clients) {
//					if(client.equals(session)) {
//						client.getBasicRemote().sendText(message);
//					}
//				}
//			}
			
			//=========테스트용================================
			System.out.println("trident's message: "+m[1]);
			
			dbcon = new DBConnection();
			ArrayList<String> allSessions = dbcon.selectAllSessions();
			
			JSONObject[] data = new JSONObject[allSessions.size()];
			for(int i=0, length=data.length;i<length;++i)
				data[i] = new JSONObject();
			
			String[] selectedSessions = null;
			
//			String kw = "{\"data\":[[\"KFCBurrito\", 3, \"storm\"],[\"McDonald\", 3, \"hello\"],[\"Cola\", 3, \"keyword3\"]]}";
			String kw = "{\"data\":[{\"word\":\"KFCBurrito\", \"weight\":8, \"keyword\":\"storm\"},{\"word\":\"McDonald\", \"weight\":30, \"keyword\":\"hello\"},{\"word\":\"Cola\", \"weight\":20, \"keyword\":\"esper\"}]}";
			//T: {"data":[["ports",1,"storm"],["ALwx",1,"storm"],["Sunset",1,"storm"],["Bigbang",1,"빅뱅"],["BW4HANA",1,"data"],["digitaltransformation",1,"data"],["juicy",1,"data"],["ProyectoPF428",7,"data"],["CX",1,"data"],["Visioneering",1,"storm"],["Undisputed",1,"hello"],["엑소",1,"엑소"]]}
			JSONParser parser = new JSONParser();
			try {
				Object obj = parser.parse(kw);
//				Object obj = parser.parse(m[1]);
				JSONObject jobj = (JSONObject) obj;
				System.out.println(jobj);
				
				JSONArray jarr = (JSONArray) jobj.get("data");
				Iterator<Object> iterator = jarr.iterator();
				int idx;
				while(iterator.hasNext()) {
					JSONObject jobj2 = (JSONObject)parser.parse(iterator.next().toString());
					
					dbcon = new DBConnection();
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
				System.out.println("data: "+data[i]);
			}
			
			//client로 결과를 전송한다. 대상은 sessions에 포함된 세션
			synchronized(clients) {
				int i;
				for(Session client: clients) {
					i = Integer.parseInt(client.getId());
					if(!data[i].isEmpty()) {
						client.getBasicRemote().sendText(data[i].toString());
					}
				}
			}
			//=====================================================
			
		} else { //trident에서 온 메시지이면 skmap테이블에서 해당하는 키워드를 요청한 세션에게 보낼 json스트링에 추가함
			//m[1] = T::{"weight":1,"keyword":"hello","word":"KFCBurrito"};
			System.out.println("trident's message: "+m[1]);
			
			dbcon = new DBConnection();
			ArrayList<String> allSessions = dbcon.selectAllSessions();
			
			JSONObject[] data = new JSONObject[allSessions.size()];
			for(int i=0, length=data.length;i<length;++i)
				data[i] = new JSONObject();
			
			String[] selectedSessions = null;
			
//			String kw = "{\"data\":[[\"KFCBurrito\", 3, \"storm\"],[\"McDonald\", 3, \"hello\"],[\"Cola\", 3, \"keyword3\"]]}";
			String kw = "{\"data\":[{\"word\":\"KFCBurrito\", \"weight\":8, \"keyword\":\"storm\"},{\"word\":\"McDonald\", \"weight\":30, \"keyword\":\"hello\"},{\"word\":\"Cola\", \"weight\":20, \"keyword\":\"esper\"}]}";
			//T: {"data":[["ports",1,"storm"],["ALwx",1,"storm"],["Sunset",1,"storm"],["Bigbang",1,"빅뱅"],["BW4HANA",1,"data"],["digitaltransformation",1,"data"],["juicy",1,"data"],["ProyectoPF428",7,"data"],["CX",1,"data"],["Visioneering",1,"storm"],["Undisputed",1,"hello"],["엑소",1,"엑소"]]}
			JSONParser parser = new JSONParser();
			try {
				Object obj = parser.parse(kw);
//				Object obj = parser.parse(m[1]);
				JSONObject jobj = (JSONObject) obj;
				System.out.println(jobj);
				
				JSONArray jarr = (JSONArray) jobj.get("data");
				Iterator<Object> iterator = jarr.iterator();
				int idx;
				while(iterator.hasNext()) {
					JSONObject jobj2 = (JSONObject)parser.parse(iterator.next().toString());
					
					dbcon = new DBConnection();
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
				System.out.println("data: "+data[i]);
			}
			
			//client로 결과를 전송한다. 대상은 sessions에 포함된 세션
			synchronized(clients) {
				int i;
				for(Session client: clients) {
					i = Integer.parseInt(client.getId());
					if(!data[i].isEmpty()) {
						client.getBasicRemote().sendText(data[i].toString());
					}
				}
			}
			
		}
	}
	
	@OnOpen
	public void onOpen(Session session) {
		System.out.println("open: "+session.getId());
		clients.add(session);
		//insert session은 message에서! trident와 client를 나눠야하기때문에
	}
	
	@OnClose
	public void onClose(Session session) {
		clients.remove("close: "+session.getId());
		dbcon = new DBConnection();
		dbcon.deleteSession(session.getId());
	}
	
	@OnError
	public void onError(Session session, Throwable t) {
		System.out.println("error: "+session.getId());
		t.printStackTrace();
	}
	
	

}
