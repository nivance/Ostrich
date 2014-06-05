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

public class TestHeartBeat2 implements MsgHandler {

	GrizzlyClient rc;
	public static JID myJid = new JID("test2@joyveb.com");

	public TestHeartBeat2() {
		rc = new GrizzlyClient(myJid, TestServer.SID, this);
		try {
			this.rc.init("127.0.0.1", 10080, 5, new AuthEntity("joyveb"), 60000);
		} catch (RouterException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		final TestHeartBeat2 client = new TestHeartBeat2();
		new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(2000);
						long start = System.currentTimeMillis();
						JsonPacket packet = JsonPacket.newPost(
								TestHeartBeat1.myJid, TestAsynSend.asynaction,
								"Hello");
						JsonPacket resp = client.rc.asynSend(packet);
						System.out.println("asyn-----time:"
								+ (System.currentTimeMillis() - start) + ", "
								+ Thread.currentThread().getName() + ", data"
								+ resp);
						Thread.sleep(1000);
						packet = JsonPacket.newRequest(
								TestHeartBeat1.myJid, TestAsynSend.syncaction,
								"Hello");
						resp = client.rc.syncSend(packet);
						System.out.println("sync-----time:"
								+ (System.currentTimeMillis() - start) + ", "
								+ Thread.currentThread().getName() + ", data"
								+ resp);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	@Override
	public void handleIncoming(JsonPacket request,
			JsonPacketResponse response) throws ComponentException {
		try {
			if (TestAsynSend.asynaction.equals(request.getAction())) {
				JsonPacket resp = request.asPostResult(request.getId()
						+ " OK");
				System.out.println("response:" + resp.getId() + ", data:"
						+ resp);
				this.rc.postPacket(resp);
			} else if (TestAsynSend.syncaction.equals(request.getAction())) {
				JsonPacket resp = request.asResult(request.getId()
						+ " OK");
				response.writePacket(resp);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
