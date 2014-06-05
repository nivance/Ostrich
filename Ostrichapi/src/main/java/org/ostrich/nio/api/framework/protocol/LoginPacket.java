package org.ostrich.nio.api.framework.protocol;

import org.ostrich.nio.api.framework.tool.JsonUtil;

public class LoginPacket extends JsonPacket {

	private static final long serialVersionUID = 267976694162370977L;

	public LoginPacket(JID from, JID to, AuthEntity autho) {
		super(from, to, PacketType.auth, "login", JsonUtil.bean2Json(autho));
	}


}
