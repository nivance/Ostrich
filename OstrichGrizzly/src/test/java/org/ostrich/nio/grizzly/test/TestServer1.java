package org.ostrich.nio.grizzly.test;

import org.ostrich.nio.api.framework.exception.RouterException;
import org.ostrich.nio.api.framework.protocol.AuthEntity;
import org.ostrich.nio.api.framework.protocol.JID;
import org.ostrich.nio.api.framework.protocol.StringEntity;
import org.ostrich.nio.grizzly.server.GrizzlyServer;

public class TestServer1 {
	public final static JID SID=new JID("router@ostrich.com/tany");

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.out.println("JSON="+new StringEntity("hello"));
			GrizzlyServer rs = new GrizzlyServer(
					SID);
			rs.startup("127.0.0.1", 10080, new AuthEntity("ostrich"));
			Thread.sleep(100000000);
		} catch (RouterException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
