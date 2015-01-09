package org.ostrich.nio.grizzly.test;

import java.io.IOException;

import org.ostrich.nio.api.framework.basic.JsonPacketResponse;
import org.ostrich.nio.api.framework.client.MsgHandler;
import org.ostrich.nio.api.framework.exception.ComponentException;
import org.ostrich.nio.api.framework.exception.RouterException;
import org.ostrich.nio.api.framework.protocol.AuthEntity;
import org.ostrich.nio.api.framework.protocol.JID;
import org.ostrich.nio.api.framework.protocol.JsonPacket;
import org.ostrich.nio.grizzly.client.GrizzlyClient;

public class TwoRouterClient implements MsgHandler {

	GrizzlyClient rc1;
	GrizzlyClient rc2;

	static JID myJid1 = new JID("test1@ostrich.com");
	static JID myJid2 = new JID("test2@ostrich.com");

	public TwoRouterClient() {
		rc1 = new GrizzlyClient(myJid1, TestServer1.SID, this);
		rc2 = new GrizzlyClient(myJid2, TestServer2.SID, this);
	}

	public void startup() {
		try {
			rc1.init("127.0.0.1", 10080, 2, new AuthEntity("ostrich"), 1000);
			rc2.init("127.0.0.1", 10081, 2, new AuthEntity("ostrich"), 1000);
		} catch (RouterException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		TwoRouterClient client = new TwoRouterClient();
		client.startup();
	}

	@Override
	public void handleIncoming(JsonPacket request,
			JsonPacketResponse response) throws ComponentException {
		System.out.println("get handleIncoming:" + request);
		try {
			if("transfer".equals(request.getAction())){
				
				
				
			}
			Thread.sleep(1000);
			response.writePacket(request.asResult(request.getEntity()
					+ " response"));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
