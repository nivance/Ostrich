package org.ostrich.nio.grizzly.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import lombok.extern.slf4j.Slf4j;

import org.ostrich.nio.api.framework.constants.OstrichConstants;
import org.ostrich.nio.api.framework.exception.RouterException;
import org.ostrich.nio.api.framework.protocol.JID;
import org.ostrich.nio.grizzly.server.GrizzlyClientSession;
import org.ostrich.nio.grizzly.server.GrizzlyServer;

/**
 * Servlet implementation class RouterServerController
 */
@Slf4j
public class GrizzlyServerController extends HttpServlet {
	private static final long serialVersionUID = 8775418883787782916L;
	
	public static GrizzlyServer rs;
	
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GrizzlyServerController() {
		super();
	}
	
	@Override
	public void init() throws ServletException {
		super.init();
		log.info("Router Server Starting");
		try {
			rs = new GrizzlyServer(new JID("router@joyveb/local"));
			rs.startup("0.0.0.0", 10080, OstrichConstants.loginToken);
			log.info("Router Server Started!" + rs.getJid() + ",address="
					+ rs.getAddress() + ":" + rs.getPort());
			for(GrizzlyClientSession clientSession : rs.getSessions()){
				System.out.println(clientSession.getProirity());
			}
		} catch (RouterException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void destroy() {
		try {
			rs.shutdown();
			log.info("Router Server stoped");
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.destroy();
	}
}
