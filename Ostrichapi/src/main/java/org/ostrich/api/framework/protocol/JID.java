package org.ostrich.api.framework.protocol;

import java.io.Serializable;

import lombok.Data;

@Data
public class JID implements Serializable {

	private static final long serialVersionUID = -6695075781128921376L;
	private String node;
	private String domain;
	private String resource;
	private String cachedFullJID;
	private String cachedBareJID;

	public JID(String node, String domain, String resource) {
		super();
		this.node = node;
		this.domain = domain;
		this.resource = resource;
		updateCache();
	}

	public JID() {}

	public JID(String jid) {
		if (jid == null) {
			return;
		}
		String[] parts = getParts(jid);
		init(parts[0], parts[1], parts[2]);
	}

	static String[] getParts(String jid) {
		String[] parts = new String[3];
		String node = null, domain, resource;
		if (jid == null) {
			return parts;
		}
		int atIndex = jid.indexOf("@");
		int slashIndex = jid.indexOf("/");
		// Node
		if (atIndex > 0) {
			node = jid.substring(0, atIndex);
		}
		// Domain
		if (atIndex + 1 > jid.length()) {
			throw new IllegalArgumentException(
					"JID with empty domain not valid");
		}
		if (atIndex < 0) {
			if (slashIndex > 0) {
				domain = jid.substring(0, slashIndex);
			} else {
				domain = jid;
			}
		} else {
			if (slashIndex > 0) {
				domain = jid.substring(atIndex + 1, slashIndex);
			} else {
				domain = jid.substring(atIndex + 1);
			}
		}
		// Resource
		if (slashIndex + 1 > jid.length() || slashIndex < 0) {
			resource = null;
		} else {
			resource = jid.substring(slashIndex + 1);
		}
		parts[0] = node;
		parts[1] = domain;
		parts[2] = resource;
		return parts;
	}

	private void init(String node, String domain, String resource) {
		if (node != null && node.equals("")) {
			node = null;
		}
		if (resource != null && resource.equals("")) {
			resource = null;
		}
		try {
			this.node = node;
			this.domain = domain;
			this.resource = resource;
			updateCache();
		} catch (Exception e) {
			StringBuilder buf = new StringBuilder();
			if (node != null) {
				buf.append(node).append("@");
			}
			buf.append(domain);
			if (resource != null) {
				buf.append("/").append(resource);
			}
			throw new IllegalArgumentException(
					"Illegal JID: " + buf.toString(), e);
		}
	}

	public final static JID nilJID = new JID("null", "null", "null");

	private void updateCache() {
		// Cache the bare JID
		StringBuilder buf = new StringBuilder(40);
		if (node != null) {
			buf.append(node).append("@");
		}
		buf.append(domain);
		cachedBareJID = buf.toString();
		// Cache the full JID
		if (resource != null) {
			buf.append("/").append(resource);
			cachedFullJID = buf.toString();
		} else {
			cachedFullJID = cachedBareJID;
		}
	}

	public String toString() {
		return cachedFullJID;
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public boolean equals(Object object) {
		if (!(object instanceof JID)) {
			return false;
		}
		if (this == object) {
			return true;
		}
		JID jid = (JID) object;
		return jid.cachedFullJID.equals(cachedFullJID);
	}

	public int compareTo(Object o) {
		if (!(o instanceof JID)) {
			throw new ClassCastException("Ojbect not instanceof JID: " + o);
		}
		JID jid = (JID) o;
		return cachedFullJID.compareTo(jid.cachedFullJID);
	}

}
