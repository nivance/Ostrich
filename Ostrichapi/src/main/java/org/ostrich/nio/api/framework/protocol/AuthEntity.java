package org.ostrich.nio.api.framework.protocol;

import java.io.Serializable;

import lombok.Data;

@Data
public class AuthEntity implements Serializable {
	private static final long serialVersionUID = -4410990677698277037L;

	public final static AuthEntity LoginSuccessed = new AuthEntity(
			"LoginSuccess", 1);
	public final static AuthEntity LoginFailed = new AuthEntity("LoginFailed",
			1);

	private String key;
	private String token;// 服务器返回的token
	private int priority;// 优先级别,

	public AuthEntity(String key, int priority) {
		this.key = key;
		this.priority = priority;
	}

	public AuthEntity(String key) {
		this.key = key;
		this.priority = 0;
	}

	public AuthEntity() {
		super();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof AuthEntity) {
			AuthEntity ao = (AuthEntity) o;
			if (ao.key == null) {
				if (key == null) {
					return true;
				}
			} else {
				return ao.key.equals(key);
			}
		}
		return false;
	}
	
	public int hashCode(){
		return super.hashCode();
	}

	@Override
	public String toString() {
		return "AuthEntity[" + key + "," + token + "," + priority + "]@"
				+ this.hashCode();
	}

}
