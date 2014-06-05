package org.ostrich.nio.grizzly.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ostrich.nio.api.framework.basic.JsonPacketResponse;
import org.ostrich.nio.api.framework.client.MsgHandler;
import org.ostrich.nio.api.framework.exception.ComponentException;
import org.ostrich.nio.api.framework.exception.RemoteCallException;
import org.ostrich.nio.api.framework.exception.RouterException;
import org.ostrich.nio.api.framework.protocol.AuthEntity;
import org.ostrich.nio.api.framework.protocol.JID;
import org.ostrich.nio.api.framework.protocol.JsonPacket;
import org.ostrich.nio.api.framework.protocol.StringEntity;
import org.ostrich.nio.grizzly.client.GrizzlyClient;

public class TestClient implements MsgHandler {

	GrizzlyClient rc;

	JID myJid;
	int myid;

	public TestClient(int myid) {
		this.myid = myid;
		System.out.println("TestClient:"+this.myid);
		this.myJid = new JID("test" + myid + "@joyveb.com");
		rc = new GrizzlyClient(myJid, TestServer.SID, this);
	}

	public void startup() {
		try {
			rc.init("127.0.0.1", 10080, 2, new AuthEntity("joyveb"), 1000);
		} catch (RouterException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static List<TestClient> clients = new ArrayList<TestClient>();
	public final static int maxclient = 2;

	public static void main(String[] args) {
		for (int i = 0; i < maxclient; i++) {
			TestClient client = new TestClient(i);
			client.startup();
			clients.add(client);
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("start!");
		for (int i = 0; i < 1; i++) {
			new Thread() {
				@Override
				public void run() {
					Random rand = new Random();
					int randint = 0;
					while (true) {
						int si = 0;//Math.abs(rand.nextInt() % maxclient);
						int di = 1;//Math.abs(rand.nextInt() % maxclient);
						if (di != si) {
							try {
								randint = rand.nextInt();
								// randint++;
								JsonPacket req = JsonPacket
										.newRequest(new JID("test" + di
												+ "@joyveb.com"), "action", new StringEntity(""+randint));

//								System.out.println("req=" + req);
								JsonPacket ret = clients.get(si).rc
										.syncSend(req);
//								System.out.println("ret=" + ret);

								if (!req.getAction().equals(ret.getAction())) {
									System.out.println("Error:" + req + "==>"
											+ ret);
								}
							} catch (RemoteCallException e) {
//								e.printStackTrace();
								System.out.println("get RemoteException:"+e.getMessage());
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						try {
							Thread.sleep(10*1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}.start();
		}

		try {
			Thread.sleep(1000000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handleIncoming(JsonPacket request,JsonPacketResponse response)throws ComponentException{
//		throw new ComponentException("错误期号");
		System.out.println("get handleIncoming:"+request);
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

}
