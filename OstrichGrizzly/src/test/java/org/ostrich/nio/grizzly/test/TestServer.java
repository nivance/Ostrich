package org.ostrich.nio.grizzly.test;

import org.ostrich.nio.api.framework.constants.OstrichConstants;
import org.ostrich.nio.api.framework.exception.RouterException;
import org.ostrich.nio.api.framework.protocol.AuthEntity;
import org.ostrich.nio.api.framework.protocol.JID;
import org.ostrich.nio.grizzly.server.GrizzlyServer;

public class TestServer {

	public static JID SID = OstrichConstants.SERVERJID;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			GrizzlyServer rs = new GrizzlyServer(SID);
			rs.startup("127.0.0.1", 10080, new AuthEntity("joyveb"));
			Thread.sleep(100000000);
		} catch (RouterException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
