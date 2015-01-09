package org.ostrich.nio.grizzly.test;

import org.ostrich.nio.api.framework.basic.JsonPacketResponse;
import org.ostrich.nio.api.framework.client.MsgHandler;
import org.ostrich.nio.api.framework.exception.ComponentException;
import org.ostrich.nio.api.framework.protocol.AuthEntity;
import org.ostrich.nio.api.framework.protocol.JID;
import org.ostrich.nio.api.framework.protocol.JsonPacket;
import org.ostrich.nio.api.framework.protocol.StringEntity;
import org.ostrich.nio.grizzly.client.GrizzlyClient;

public class SimGXClient implements MsgHandler {

	static GrizzlyClient rc;

	public static JID gcJid = new JID("gx@ostrich.com");
	int myid;

	public SimGXClient(int myid) {
		rc = new GrizzlyClient(gcJid, TestServer.SID, this);
	}

	public void startup() {
		try {
			rc.init("127.0.0.1", 10080, 5, new AuthEntity("ostrich"), 60000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SimGXClient client = new SimGXClient(0);
		client.startup();
	}

	@Override
	public void handleIncoming(JsonPacket req,
			JsonPacketResponse response) throws ComponentException {
		try {
			if (SimPCClient.ACTION.equals(req.getAction())) {
				System.out.println("get handle Incoming:" + req);
				JsonPacket packet = JsonPacket.newPostResult(
						req.getFrom(), req.getId(), new StringEntity("asyc resp ok "
								+ System.currentTimeMillis()));
				rc.postPacket(packet);
			}else{
				System.out.println("error action[" + req.getAction()
						+ "], request:" + req);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
