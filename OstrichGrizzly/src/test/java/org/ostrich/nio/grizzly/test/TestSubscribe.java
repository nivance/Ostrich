package org.ostrich.nio.grizzly.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ostrich.nio.api.framework.basic.JsonPacketResponse;
import org.ostrich.nio.api.framework.client.MsgHandler;
import org.ostrich.nio.api.framework.client.SubscriptionHandler;
import org.ostrich.nio.api.framework.exception.ComponentException;
import org.ostrich.nio.api.framework.exception.RemoteCallException;
import org.ostrich.nio.api.framework.exception.RouterException;
import org.ostrich.nio.api.framework.protocol.AuthEntity;
import org.ostrich.nio.api.framework.protocol.JID;
import org.ostrich.nio.api.framework.protocol.JsonPacket;
import org.ostrich.nio.api.framework.protocol.SubscribeEntity;
import org.ostrich.nio.grizzly.client.GrizzlySubscriber;

public class TestSubscribe implements MsgHandler,SubscriptionHandler{

	GrizzlySubscriber rs;

	JID myJid;
	int myid;

	public TestSubscribe(int myid) {
		this.myid = myid;
		System.out.println("TestClient:"+this.myid);
		this.myJid = new JID("testsubscribe" + myid + "@joyveb.com");
		rs = new GrizzlySubscriber(myJid, TestServer.SID, this);
	}

	public void startup() {
		try {
			SubscribeEntity subentity=new SubscribeEntity("test1@joyveb.com", null, null, null);
			subentity.setSubkey("sub1");
			rs.addSubscribe(subentity,this);
			rs.init("192.168.0.121", 10080, 2, new AuthEntity("joyveb",3), 60*1000);
		} catch (RouterException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RemoteCallException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static List<TestSubscribe> clients = new ArrayList<TestSubscribe>();
	public final static int maxclient = 1;

	public static void main(String[] args) {
		
		for (int i = 0; i < maxclient; i++) {
			TestSubscribe client = new TestSubscribe(i);
			client.startup();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			clients.add(client);
		}
		System.out.println("start!");
		try {
			Thread.sleep(10000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void handleIncoming(JsonPacket request,JsonPacketResponse response)throws ComponentException{
		System.out.println("get incoming:"+request.toJsonArrayTxt());
	}

	@Override
	public void handlePublish(String subkey,JsonPacket packet) {
		System.out.println("handlePublish:subkey="+subkey+":body="+packet.toJsonArrayTxt());

	}

}
