package dke.sj.d3js;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/websocket")
public class websocket {
private static Set<Session> clients = Collections.synchronizedSet(new HashSet<Session>());
	private DBConnection dbcon = null;
	/*
	 * when message(including socket open message) received
	 */
	
	@OnMessage
	public void onMessage(String message, Session session) throws IOException {
		System.out.println(message);
		String m[] = message.split(":");//triednt 인지 client인지 판별
		if(m[0].equals("CLIENT")){ //client: client에서 온 메시지이면 키워드요청이므로 데이터 insert
			System.out.println("client's message: "+m[1]);
//			
			dbcon = new DBConnection();//connection open
			dbcon.insertKeywordsAndSessions(m[1], session.toString()); //connection close

		} else { //trident에서 온 메시지이면 skmap테이블에서 해당하는 키워드를 요청한 세션에게 보낼 json스트링에 추가함
			System.out.println(m[1]);
			System.out.println("trident's message");
			
			//m1을 json 형태로 변환
			//m1으로부터 키워드 뽑아냄
			String kw = "storm";
			
			dbcon = new DBConnection();
			String[] sessions = dbcon.selectSession(kw);
			for(int i=0;i<sessions.length;++i){
				System.out.println(sessions[i]);
			}
			
			//client로 결과를 전송한다. 대상은 sessions에 포함된 세션
			synchronized(clients) {
				for(Session client: clients) {
					for(String s: sessions){
						if(client.toString().equals(s)) {
							client.getBasicRemote().sendText("data");
						}
					}
				}
			}
		}
	}
	
	@OnOpen
	public void onOpen(Session session) {
		System.out.println("open: "+session);
		clients.add(session);
		//insert session은 message에서! trident와 client를 나눠야하기때문에
	}
	
	@OnClose
	public void onClose(Session session) {
		clients.remove("close: "+session);
		dbcon = new DBConnection();
		dbcon.deleteSession(session.toString());
	}
	
	@OnError
	public void onError(Session session, Throwable t) {
		System.out.println("error: "+session);
		t.printStackTrace();
	}
	
	

}
