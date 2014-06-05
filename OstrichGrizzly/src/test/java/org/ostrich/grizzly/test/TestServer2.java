package org.ostrich.grizzly.test;

import org.ostrich.api.framework.exception.RouterException;
import org.ostrich.api.framework.protocol.AuthEntity;
import org.ostrich.api.framework.protocol.JID;
import org.ostrich.api.framework.protocol.StringEntity;
import org.ostrich.grizzly.server.GrizzlyServer;

public class TestServer2 {
	public final static JID SID=new JID("router@joyveb.com/tany");

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.out.println("JSON="+new StringEntity("hello"));
			GrizzlyServer rs = new GrizzlyServer(
					SID);
			rs.startup("127.0.0.1", 10081, new AuthEntity("joyveb"));
			Thread.sleep(100000000);
		} catch (RouterException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
