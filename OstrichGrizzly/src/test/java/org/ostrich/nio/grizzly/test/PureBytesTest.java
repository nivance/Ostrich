package org.ostrich.nio.grizzly.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class PureBytesTest {

	public static void main(String[] args) {
		try (Socket socket = new Socket("127.0.0.1", 10080);
				OutputStream ous = socket.getOutputStream();
				InputStream ins = socket.getInputStream();) {
			// from, to, PacketType(auth, request), action, value
			String login = "[\"bj@joyveb.com\",\"bj@joyveb.com\",\"auth\",\"login\",{\"key\":\"joyveb\",\"token\":null,\"priority\":0}]";
			ByteBuffer bb = ByteBuffer.allocate(4 + login.length());
			bb.putInt(login.length());
			bb.put(login.getBytes());
			ous.write(bb.array());
			byte[] bytes = new byte[1024];
			ins.read(bytes);
			System.out.println(new String(bytes));

			// from, to, PacketType(auth, request), action, value
			String addPropery = "[null,\"notifier.joyveb/local\",\"request\",\"addProperty\",{\"firstName\":\"John\", \"LashName\":\"Jordan\"}]";
			ByteBuffer ba = ByteBuffer.allocate(4 + addPropery.length());
			ba.putInt(addPropery.length());
			ba.put(addPropery.getBytes());
			ous.write(ba.array());
			byte[] bytesa = new byte[1024];
			ins.read(bytesa);
			System.out.println(new String(bytesa));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
