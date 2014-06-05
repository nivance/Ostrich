package org.ostrich.api.framework.protocol;

import lombok.Getter;

public enum PacketType {
	heartbeart_req("heartbeat-req"),//心跳
	heartbeart_ans("heartbeat-ans"),//心跳
	auth("auth"),//登录验证包
	error("error"),//错误请求包
	request("request"),//请求包
	result("result"),//响应包
	post("post"),//异步请求包
	post_response("post_response"),//异步响应包
	kickoff("kickoff"),//服務器踢掉客戶端連接
	subscribe("subscribe"),//
	subscribe_response("subscribe_response"),//
	publish("publish"),//
	result_exception("result_exception");//异常回应包

	
	/**
	 * 状态描述
	 */
	private @Getter String name;
	
	PacketType(String name) {
		this.name = name;
	}
}
