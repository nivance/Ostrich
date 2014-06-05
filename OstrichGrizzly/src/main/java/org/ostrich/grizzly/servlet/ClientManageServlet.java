package org.ostrich.grizzly.servlet;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.ostrich.grizzly.server.GrizzlyClientSession;

/**
 * ClientSession管理Servlet
 */

public class ClientManageServlet extends HttpServlet {
	private static final long serialVersionUID = 1816571926102510789L;

	private static final String user1Name = "lottery";
	private static final String user1Pass = "123456";
	private static final String user2Name = "admin";
	private static final String user2Pass = "admin111";
	private static final String NORMAL_ROLE = "1";
	private static final String ADMIN_ROLE = "2";

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");
		if ("login".equals(action)) {
			login(request, response);
		} else if ("kickoff".equals(action)) {
			kickOff(request, response);
		} else if ("logout".endsWith(action)) {
			request.getRequestDispatcher("/index.jsp").forward(request,
					response);
		}
	}

	private void login(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String name = request.getParameter("username");
		String passwd = request.getParameter("userpass");
		String role = "";
		if (user1Name.equals(name) && user1Pass.equals(passwd)) {
			role = NORMAL_ROLE;
			setRequestAttribute(request, name, passwd, role);
			request.setAttribute("loginflag", "success");
			request.getRequestDispatcher("/cls.jsp").forward(request, response);
		} else if (user2Name.equals(name) && user2Pass.equals(passwd)) {
			role = ADMIN_ROLE;
			setRequestAttribute(request, name, passwd, role);
			request.setAttribute("loginflag", "success");
			request.getRequestDispatcher("/cls.jsp").forward(request, response);
		} else {
			request.setAttribute("loginflag", "failed");
			request.getRequestDispatcher("/index.jsp").forward(request,
					response);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	public void kickOff(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String jid = request.getParameter("jid");
		String msg = request.getParameter("msg");
		if (jid != null && jid.length() > 0) {
			GrizzlyServerController.rs.kickOffSession(jid, msg);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		response.sendRedirect("cls.jsp");
		//login(request, response);
	}

	private void setRequestAttribute(HttpServletRequest request, String name,
			String passwd, String role) {
		if (StringUtils.isNotBlank(role)) {
			request.setAttribute("name", name);
		}
		if (StringUtils.isNotBlank(passwd)) {
			request.setAttribute("passwd", passwd);
		}
		if (StringUtils.isNotBlank(role)) {
			request.setAttribute("role", role);
		}
		Collection<GrizzlyClientSession> sessions = GrizzlyServerController.rs
				.getSessions();
		if (sessions != null && sessions.size() > 0) {
			request.setAttribute("cls", sessions);
		}
	}

}
