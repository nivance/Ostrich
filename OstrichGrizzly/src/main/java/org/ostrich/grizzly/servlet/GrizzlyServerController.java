package org.ostrich.grizzly.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.ostrich.api.framework.constants.RouterConstants;
import org.ostrich.api.framework.exception.RouterException;
import org.ostrich.api.framework.protocol.JID;
import org.ostrich.grizzly.server.GrizzlyClientSession;
import org.ostrich.grizzly.server.GrizzlyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet implementation class RouterServerController
 */
public class GrizzlyServerController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory
			.getLogger(GrizzlyServerController.class);
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
			rs.startup("0.0.0.0", 10080, RouterConstants.loginToken);
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
