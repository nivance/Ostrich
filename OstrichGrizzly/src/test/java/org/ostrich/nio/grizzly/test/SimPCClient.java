package org.ostrich.nio.grizzly.test;

import org.ostrich.nio.api.framework.basic.JsonPacketResponse;
import org.ostrich.nio.api.framework.client.MsgHandler;
import org.ostrich.nio.api.framework.exception.ComponentException;
import org.ostrich.nio.api.framework.protocol.AuthEntity;
import org.ostrich.nio.api.framework.protocol.JID;
import org.ostrich.nio.api.framework.protocol.JsonPacket;
import org.ostrich.nio.api.framework.protocol.StringEntity;
import org.ostrich.nio.grizzly.client.GrizzlyClient;



public class SimPCClient implements MsgHandler {

	static GrizzlyClient rc;

	public static JID pcJid = new JID("pc@joyveb.com");
	public static String ACTION = "pcaction";
	int myid;

	public SimPCClient(int myid) {
		rc = new GrizzlyClient(pcJid, TestServer.SID, this);
	}

	public void startup() {
		try {
			rc.init("127.0.0.1", 10080, 5, new AuthEntity("joyveb"), 60000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SimPCClient client = new SimPCClient(0);
		client.startup();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		System.out.println("start " + pcJid);
		for (int i = 0; i < 2000; i++) {
			new Thread() {
				@Override
				public void run() {
					try {
						JsonPacket req = JsonPacket.newPost(
								SimGXClient.gcJid, ACTION, new StringEntity(
										"asyn msg from pc"));
						System.out.println("req=" + req);
						JsonPacket ret = rc.asynSend(req);
						System.out.println("ret=" + ret);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
	}

	@Override
	public void handleIncoming(JsonPacket req, JsonPacketResponse resp)
			throws ComponentException {
		try {
			if (SimBJClient.ACTION.equals(req.getAction())) {
				System.out.println("get handle Incoming:" + req);
				resp.writePacket(req.asResult(new StringEntity("pc ok "
						+ System.currentTimeMillis())));
			} else {
				System.out.println("error action[" + req.getAction()
						+ "], request:" + req);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
