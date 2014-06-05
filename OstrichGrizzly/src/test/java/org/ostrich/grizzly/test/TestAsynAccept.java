package org.ostrich.grizzly.test;

import java.io.IOException;

import org.ostrich.api.framework.basic.JsonPacketResponse;
import org.ostrich.api.framework.client.MsgHandler;
import org.ostrich.api.framework.exception.ComponentException;
import org.ostrich.api.framework.exception.RouterException;
import org.ostrich.api.framework.protocol.AuthEntity;
import org.ostrich.api.framework.protocol.JID;
import org.ostrich.api.framework.protocol.JsonPacket;
import org.ostrich.grizzly.client.GrizzlyClient;

public class TestAsynAccept implements MsgHandler {

	GrizzlyClient rc;

	public static JID myJid = new JID("test1.joyveb/local");

	public TestAsynAccept() {
	}

	public void startup() {
		try {
			rc = new GrizzlyClient(myJid, TestServer.SID, this);
			rc.init("127.0.0.1", 10080, 5, new AuthEntity("joyveb"), 60000);
		} catch (RouterException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		TestAsynAccept accept = new TestAsynAccept();
		accept.startup();
		
		while (true){
			try {
				Thread.sleep(5000);
				
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void handleIncoming(JsonPacket request,
			JsonPacketResponse response) throws ComponentException {
		System.out.println("get handleIncoming:" + request);
		try {
			if(TestAsynSend.asynaction.equals(request.getAction())){
				JsonPacket resp = request.asPostResult(request.getId() + " OK");
				System.out.println("response:" + resp.getId() + ", data:" + resp);
				response.writePacket(resp);
			}else if(TestAsynSend.syncaction.equals(request.getAction())){
				JsonPacket resp = request.asResult(request.getId() + " OK");
				response.writePacket(resp);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
}
