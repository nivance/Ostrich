package org.ostrich.nio.grizzly.test;

import org.ostrich.nio.api.framework.basic.JsonPacketResponse;
import org.ostrich.nio.api.framework.client.MsgHandler;
import org.ostrich.nio.api.framework.exception.ComponentException;
import org.ostrich.nio.api.framework.protocol.AuthEntity;
import org.ostrich.nio.api.framework.protocol.JID;
import org.ostrich.nio.api.framework.protocol.JsonPacket;
import org.ostrich.nio.api.framework.protocol.StringEntity;
import org.ostrich.nio.grizzly.client.GrizzlyClient;

public class SimBJClient implements MsgHandler {

	static GrizzlyClient rc;

	public static JID bjJid = new JID("bj@ostrich.com");
	public static String ACTION = "bjaction";
	int myid;

	public SimBJClient(int myid) {
		rc = new GrizzlyClient(bjJid, TestServer.SID, this);
	}

	public void startup() {
		try {
			rc.init("127.0.0.1", 10080, 5, new AuthEntity("ostrich"), 60000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SimBJClient client = new SimBJClient(0);
		client.startup();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		System.out.println("start " + bjJid);
		for (int i = 0; i < 2000; i++) {
			new Thread() {
				@Override
				public void run() {
					try {
						JsonPacket req = JsonPacket.newRequest(
								SimPCClient.pcJid, ACTION, new StringEntity(
										"bj sync msgs" + System.currentTimeMillis()));
						System.out.println("req=" + req);
						JsonPacket ret = rc.syncSend(req);
						if (!req.getAction().equals(ret.getAction())) {
							System.out.println("Error:" + req + "==>" + ret);
						}else{
							System.out.println("ret=" + ret);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
	}

	@Override
	public void handleIncoming(JsonPacket request,
			JsonPacketResponse response) throws ComponentException {
		try {
			response.writePacket(request.asResult("ok"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
