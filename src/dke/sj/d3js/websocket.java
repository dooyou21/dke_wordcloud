package dke.sj.d3js;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/websocket")
public class websocket {
private static Set<Session> clients = Collections.synchronizedSet(new HashSet<Session>());
	
	/*
	 * when message(including socket open message) received!
	 */
	@OnMessage
	public void onMessage(String message, Session session) throws IOException {
		String words[] = message.split(",");
		for(String s : words) System.out.println(s);

		synchronized(clients) {
			for(Session client: clients) {
				if(client.equals(session)) {
					client.getBasicRemote().sendText(message+" received");
				}
			}
		}
	}
	
	@OnOpen
	public void onOpen(Session session) {
		System.out.println(session);
		clients.add(session); 
	}
	
	@OnClose
	public void onClose(Session session) {
		clients.remove(session);
	}
	
	

}
