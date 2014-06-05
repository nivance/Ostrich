package org.ostrich.nio.api.servlet;

public enum RemoteCommand {
	LIST("list"),
	KICKOFF("kickoff"),
	UNKNOW("unknow");
	
	String command;

	private RemoteCommand(String command) {
		this.command = command;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}
	
	
}
