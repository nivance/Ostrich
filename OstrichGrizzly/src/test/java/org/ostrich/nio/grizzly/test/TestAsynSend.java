package org.ostrich.nio.grizzly.test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.glassfish.grizzly.impl.FutureImpl;
import org.glassfish.grizzly.impl.SafeFutureImpl;
import org.ostrich.nio.api.framework.basic.JsonPacketResponse;
import org.ostrich.nio.api.framework.client.MsgHandler;
import org.ostrich.nio.api.framework.constants.OstrichConstants;
import org.ostrich.nio.api.framework.exception.ComponentException;
import org.ostrich.nio.api.framework.exception.RemoteCallException;
import org.ostrich.nio.api.framework.exception.RouterException;
import org.ostrich.nio.api.framework.protocol.JID;
import org.ostrich.nio.api.framework.protocol.JsonPacket;
import org.ostrich.nio.grizzly.client.GrizzlyClient;

public class TestAsynSend implements MsgHandler {

	GrizzlyClient rc;

	public static JID myJid = new JID("TestAsynSend@ostrich.com");
	public static String asynaction = "asynAction";
	public static String syncaction = "syncAction";

	public TestAsynSend() {
	}

	public void startup() throws RouterException, IOException {
		rc = new GrizzlyClient(myJid, TestServer.SID, this);
		rc.init("127.0.0.1", 10080, 2, OstrichConstants.loginToken, 20000);
	}

	public static void main(String[] args) {
		// testAsyn();
		// testMultiAsynThread();
		// testMultiSyncThread();
		// testPost();
		// testFutureImpl();
		testMixSend();
	}

	public static void testMixSend() {
		final long start = System.currentTimeMillis();
		final TestAsynSend send = new TestAsynSend();
		try {
			send.startup();
		} catch (RouterException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		for (int i = 0; i < 10; i++) {
			new Thread("thread-" + i) {
				public void run() {
					JsonPacket packet = null;
					try {
						packet = JsonPacket.newPost(
								TestAsynAccept.myJid, asynaction, "Hello");
						JsonPacket resp = send.rc.asynSend(packet);
						boolean sameid = resp.getId().equals(packet.getId());
						System.out.println("time:"
								+ (System.currentTimeMillis() - start) + ", "
								+ Thread.currentThread().getName() + ", id:"
								+ sameid + ", data" + resp);
					} catch (RouterException e) {
						e.printStackTrace();
					} catch (RemoteCallException e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
		for (int i = 0; i < 10; i++) {
			new Thread("thread-" + i) {
				public void run() {
					JsonPacket packet = null;
					try {
						packet = JsonPacket.newRequest(
								TestAsynAccept.myJid, syncaction, "Hello");
						JsonPacket resp = send.rc.syncSend(packet);
						boolean sameid = resp.getId().equals(packet.getId());
						System.out.println("time:"
								+ (System.currentTimeMillis() - start) + ", "
								+ Thread.currentThread().getName() + ", id:"
								+ sameid + ", data" + resp);
					} catch (RouterException e) {
						e.printStackTrace();
					} catch (RemoteCallException e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
	}

	public static void testMultiAsynThread() {
		final long start = System.currentTimeMillis();
		final TestAsynSend send = new TestAsynSend();
		try {
			send.startup();
		} catch (RouterException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		for (int i = 0; i < 10; i++) {
			new Thread("thread-" + i) {
				public void run() {
					JsonPacket packet = null;
					try {
						packet = JsonPacket.newPost(
								TestAsynAccept.myJid, asynaction, "Hello");
						JsonPacket resp = send.rc.asynSend(packet);
						boolean sameid = resp.getId().equals(packet.getId());
						System.out.println("time:"
								+ (System.currentTimeMillis() - start) + ", "
								+ Thread.currentThread().getName() + ", id:"
								+ sameid + ", data" + resp);
					} catch (RouterException e) {
						e.printStackTrace();
					} catch (RemoteCallException e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
	}

	public static void testMultiSyncThread() {
		final long start = System.currentTimeMillis();
		final TestAsynSend send = new TestAsynSend();
		try {
			send.startup();
		} catch (RouterException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		for (int i = 0; i < 10; i++) {
			new Thread("thread-" + i) {
				public void run() {
					JsonPacket packet = null;
					try {
						packet = JsonPacket.newRequest(
								TestAsynAccept.myJid, syncaction, "Hello");
						JsonPacket resp = send.rc.syncSend(packet);
						boolean sameid = resp.getId().equals(packet.getId());
						System.out.println("time:"
								+ (System.currentTimeMillis() - start) + ", "
								+ Thread.currentThread().getName() + ", id:"
								+ sameid + ", data" + resp);
					} catch (RouterException e) {
						e.printStackTrace();
					} catch (RemoteCallException e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
	}

	public static void testAsyn() {
		JsonPacket packet = null;
		try {
			TestAsynSend send = new TestAsynSend();
			send.startup();
			packet = JsonPacket.newPost(TestAsynAccept.myJid,
					asynaction, "Hello");
			JsonPacket resp = send.rc.asynSend(packet);
			boolean sameid = resp.getId().equals(packet.getId());
			System.out.println("time:" + Thread.currentThread().getName()
					+ ", id:" + sameid + ", data" + resp);
			send.rc.shutdown();
		} catch (RouterException e) {
			e.printStackTrace();
		} catch (RemoteCallException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void testPost() {
		try {
			TestAsynSend send = new TestAsynSend();
			send.startup();
			JsonPacket packet = JsonPacket.newPost(
					TestAsynAccept.myJid, asynaction, "Hello");
			send.rc.postPacket(packet);
			// send.rc.shutdown();
		} catch (RouterException e) {
			e.printStackTrace();
		} catch (RemoteCallException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("finished send.");
	}

	@Override
	public void handleIncoming(JsonPacket request,
			JsonPacketResponse response) throws ComponentException {
		// throw new ComponentException("错误期号");
		System.out.println("get handleIncoming:" + request);
		try {
			Thread.sleep(10000);
			response.writePacket(request.asResult(request.getEntity()));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testFutureImpl() {
		final FutureImpl<String> completeFuture = SafeFutureImpl.create();
		new Thread("thread-future2") {
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				completeFuture.result("result");
			}
		}.start();
		new Thread("thread-future2") {
			public void run() {
				try {
					System.out.println(completeFuture.get());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}.start();

	}

}
