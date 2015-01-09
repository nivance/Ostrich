package org.ostrich.nio.grizzly.test;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.ostrich.nio.api.framework.basic.JsonPacketResponse;
import org.ostrich.nio.api.framework.client.MsgHandler;
import org.ostrich.nio.api.framework.constants.OstrichConstants;
import org.ostrich.nio.api.framework.exception.ComponentException;
import org.ostrich.nio.api.framework.exception.RouterException;
import org.ostrich.nio.api.framework.protocol.JID;
import org.ostrich.nio.api.framework.protocol.JsonPacket;
import org.ostrich.nio.grizzly.client.GrizzlyClient;

@Slf4j
public class TestAsynAccept implements MsgHandler {

	GrizzlyClient rc;

	public static JID myJid = new JID("test1.ostrich/local");

	public TestAsynAccept() {
	}

	public void startup() {
		try {
			rc = new GrizzlyClient(myJid, TestServer.SID, this);
			rc.init("127.0.0.1", 10080, 3, OstrichConstants.loginToken, 60000);
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
				log.debug("response:" + resp.getId() + ", data:" + resp);
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
