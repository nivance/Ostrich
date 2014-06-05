package org.ostrich.api.framework.constants;

import org.ostrich.api.framework.protocol.AuthEntity;
import org.ostrich.api.framework.protocol.JID;

public class RouterConstants {
	
	/**
	 * 路由IP地址
	 */
	public final static String ROUTERADDRESS = "192.168.3.8";
	/**
	 * 路由注册端口
	 */
	public final static int PORT = 10080;
	
	/**
	 * 客户端与路由协商的登录口令
	 */
	public final static AuthEntity loginToken = new AuthEntity("ostrich");
	/**
	 * 路由JID
	 */
	public static final JID ROUTERJID = new JID("router.ostrich/local");
	
	
	/**
	 * 请求到响应的最大处理时间
	 */
	public static final long MAX_DEAL_TIME = 60 * 1000;
	
	/**
	 * 服务端维护的最大并发心跳线程数
	 */
	public static final int MAX_BEATER_THREAD_NUM = 20;

}
