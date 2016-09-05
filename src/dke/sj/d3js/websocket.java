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
	 * when message(including socket open message) received!
	 */
	
	@OnMessage
	public void onMessage(String message, Session session) throws IOException {
		String m[] = message.split(":");//triednt 인지 client인지 판별
		if(m[0].equals("CLIENT")){ //client: client에서 온 메시지이면 키워드요청이므로 데이터 insert
			System.out.println("client's message: "+m[1]);
//			String words[] = m[1].split(","); //안쓰는듯?
			dbcon = new DBConnection();//connection open
			dbcon.insertKeywordsAndSessions(m[1], session.toString()); //connection close
			
		} else {//DEBUG: trident로부터 온 데이터. 함수포인터 써서? 할 일 지정. trident에서 온 메시지이면 skmap테이블에서 해당하는 키워드를 요청한 세션에게 보낼 json스트링에 추가함
//			System.out.println(m[1]);
			System.out.println("trident's message");
			//trident로 로부터 받아온 데이터를 sql연산을 수행한 후 client로 결과를 전송한다
//			synchronized(clients) {
//				for(Session client: clients) {
//					if(client.equals(session)) {
//						client.getBasicRemote().sendText(message+" received");
//					}
//				}
			
			//이렇게 쓰면되겠다!
//				for(Session client: clients) {
//					if(client.toString().equals(session)) {
//						client.getBasicRemote().sendText(message+" received");
//					}
//				}
//			}
		}
		
	}
	
	@OnOpen
	public void onOpen(Session session) {
		System.out.println(session);
		clients.add(session);
		//insert session은 message에서! trident와 client를 나눠야하기때문에
	}
	
	@OnClose
	public void onClose(Session session) {
		clients.remove(session);
		dbcon = new DBConnection();
		dbcon.deleteSession(session.toString());
	}
	
	@OnError
	public void onError(Session session, Throwable t) {
		System.out.println(session);
		//소켓 강제로끊김 등 에러났을때
		clients.remove(session);
		dbcon = new DBConnection();
		dbcon.deleteSession(session.toString());
	}
	
	

}
