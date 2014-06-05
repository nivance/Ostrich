package org.ostrich.nio.grizzly.test;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.ostrich.nio.api.framework.basic.JsonPacketResponse;
import org.ostrich.nio.api.framework.client.MsgHandler;
import org.ostrich.nio.api.framework.exception.ComponentException;
import org.ostrich.nio.api.framework.protocol.AuthEntity;
import org.ostrich.nio.api.framework.protocol.JID;
import org.ostrich.nio.api.framework.protocol.JsonPacket;
import org.ostrich.nio.api.framework.protocol.PacketType;
import org.ostrich.nio.api.framework.protocol.StringEntity;
import org.ostrich.nio.grizzly.client.GrizzlyClient;

//VS4E -- DO NOT REMOVE THIS LINE!
public class PacketDebuger extends JFrame implements MsgHandler {

	private static final long serialVersionUID = 1L;
	private JLabel jLabel0;
	private JLabel jLabel1;
	private JTextField serverAddr;
	private JTextField servport;
	private JTextField fromJID;
	private JLabel jLabel2;
	private JLabel jLabel3;
	private JButton btnLogin;
	private JButton btnSend;
	private JTextArea txtSend;
	private JTextArea txtSocket;
	private JScrollPane jScrollPane0;
	private JTextArea txtRecv;
	private JScrollPane jScrollPane1;
	private GrizzlyClient rc;
	private JsonPacket jrp;
	private JButton btnLogout;
	private JButton btnValidate;
	private JButton btnClear;
	private JLabel jLabel4;
	private JTextField txtAction;
	private JComboBox toJID;
	private JScrollPane jScrollPane2;
	private static final String PREFERRED_LOOK_AND_FEEL = "javax.swing.plaf.metal.MetalLookAndFeel";

	public PacketDebuger() {
		initComponents();
	}

	private void initComponents() {
		setTitle("包调试工具");
	/*	setLayout(new GroupLayout());
		add(getJLabel3(), new Constraints(new Leading(34, 82, 12, 12),
				new Leading(83, 12, 12)));
		add(getBtnLogout(), new Constraints(new Leading(609, 10, 10),
				new Leading(51, 12, 12)));
		add(getBtnLogin(), new Constraints(new Leading(477, 110, 10, 10),
				new Leading(52, 12, 12)));
		add(getBtnSend(), new Constraints(new Leading(535, 104, 10, 10),
				new Leading(96, 33, 12, 12)));
		add(getValidateBtn(), new Constraints(new Leading(429, 96, 10, 10),
				new Leading(96, 33, 12, 12)));
		add(getClearBtn(), new Constraints(new Leading(649, 96, 10, 10),
				new Leading(96, 33, 12, 12)));
		add(getJScrollPane1(), new Constraints(new Bilateral(8, 12, 22),
				new Leading(318, 140, 10, 10)));
		add(getJScrollPane2(), new Constraints(new Bilateral(8, 12, 22),
				new Leading(463, 145, 12, 12)));
		add(getJLabel0(), new Constraints(new Leading(29, 116, 10, 10),
				new Leading(12, 12, 12)));
		add(getServerAddr(), new Constraints(new Leading(126, 148, 10, 10),
				new Leading(10, 12, 12)));
		add(getServportLabel(), new Constraints(new Leading(296, 106, 10, 10),
				new Leading(12, 12, 12)));
		add(getServport(), new Constraints(new Leading(396, 105, 10, 10),
				new Leading(10, 12, 12)));
		add(getJLabel2(), new Constraints(new Leading(29, 82, 12, 12),
				new Leading(46, 12, 12)));
		add(getJfromJID(), new Constraints(new Leading(126, 196, 12, 12),
				new Leading(46, 12, 12)));
		add(getToJID(), new Constraints(new Leading(126, 196, 12, 12),
				new Leading(82, 12, 12)));
		add(getTxtAction(), new Constraints(new Leading(124, 198, 12, 12),
				new Leading(121, 12, 12)));
		add(getJScrollPane0(), new Constraints(new Bilateral(8, 12, 22),
				new Leading(153, 159, 12, 12)));
		add(getJLabel4(), new Constraints(new Leading(34, 60, 12, 12),
				new Leading(123, 12, 12)));
		setSize(777, 623);*/
	}

	private JComboBox getToJID() {
		if (toJID == null) {
			toJID = new JComboBox();
			toJID.setEditable(true);
			toJID.setModel(new DefaultComboBoxModel(new Object[] {"test0@joyveb.com"}));
			toJID.setDoubleBuffered(false);
			toJID.setBorder(null);
			toJID.setRequestFocusEnabled(false);
		}
		return toJID;
	}

	private JTextField getTxtAction() {
		if (txtAction == null) {
			txtAction = new JTextField();
			txtAction.setFont(new Font("Dialog", Font.PLAIN, 15));
			txtAction.setText("action");
		}
		return txtAction;
	}

	private JTextField getJTextField0() {
		if (txtAction == null) {
			txtAction = new JTextField();
			txtAction.setFont(new Font("Dialog", Font.PLAIN, 15));
			txtAction.setText("jTextField0");
		}
		return txtAction;
	}

	private JLabel getJLabel4() {
		if (jLabel4 == null) {
			jLabel4 = new JLabel();
			jLabel4.setFont(new Font("Dialog", Font.BOLD, 15));
			jLabel4.setText("Action:");
		}
		return jLabel4;
	}

	private JButton getBtnLogout() {
		if (btnLogout == null) {
			btnLogout = new JButton();
			btnLogout.setFont(new Font("Dialog", Font.PLAIN, 15));
			btnLogout.setText("断开连接");
			btnLogout.setEnabled(false);
			btnLogout.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent event) {
					btnLogoutActionActionPerformed(event);
				}
			});
		}
		return btnLogout;
	}

	public JButton getValidateBtn() {
		if (btnValidate == null) {
			btnValidate = new JButton();
			btnValidate.setFont(new Font("Dialog", Font.PLAIN, 15));
			btnValidate.setText("验证数据");
			btnValidate.setEnabled(false);
			btnValidate.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					btnValidateActionActionPerformed(event);
				}
			});
		}
		return btnValidate;
	}
	static ObjectMapper mapper = new ObjectMapper();
	{
		mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	protected void btnValidateActionActionPerformed(ActionEvent event) {
		String recvtxt = null;
		String enTxt = null;
		try {
			
			JsonNode jo=mapper.readTree(txtSend.getText());
			if (jo == null) {
				jrp = JsonPacket.newRequest(new JID(toJID
						.getSelectedItem().toString()), txtAction.getText(),
						new StringEntity(txtSend.getText()));
			} else {
				jrp = new JsonPacket(new JID(fromJID.getText()
						.toString()), new JID(toJID.getSelectedItem()
						.toString()), PacketType.request, txtAction.getText(),
						jo);
			}
		} catch (Exception e) {
			e.printStackTrace();
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(bout));
			recvtxt = new String(bout.toByteArray());
			txtRecv.setText("Json格式数据验证失败！\n" + recvtxt);
			btnSend.setEnabled(false);
			return;
		}
		txtRecv.setText("Json格式数据验证成功，可以发送数据！");
		btnSend.setEnabled(true);
		enTxt = jrp.toJsonArrayTxt();
		txtSocket.setText("encode:[" + enTxt.length() + "]" + enTxt);
	}

	public JButton getClearBtn() {
		if (btnClear == null) {
			btnClear = new JButton();
			btnClear.setFont(new Font("Dialog", Font.PLAIN, 15));
			btnClear.setText("清除数据");
			btnClear.setEnabled(false);
			btnClear.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					btnClearActionActionPerformed(event);
				}
			});
		}
		return btnClear;
	}

	protected void btnClearActionActionPerformed(ActionEvent event) {
		txtSend.setText(null);
		txtRecv.setText(null);
		txtSocket.setText(null);
	}

	private JScrollPane getJScrollPane1() {
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setViewportView(getJTextRecv());
		}
		return jScrollPane1;
	}

	private JScrollPane getJScrollPane0() {
		if (jScrollPane0 == null) {
			jScrollPane0 = new JScrollPane();
			jScrollPane0.setViewportView(getJTextSend());
		}
		return jScrollPane0;
	}

	private JScrollPane getJScrollPane2() {
		if (jScrollPane2 == null) {
			jScrollPane2 = new JScrollPane();
			jScrollPane2.setViewportView(getJSocketData());
		}
		return jScrollPane2;
	}

	private JTextArea getJTextRecv() {
		if (txtRecv == null) {
			txtRecv = new JTextArea();
			txtRecv.setText("txtRecv");
		}
		return txtRecv;
	}

	private JTextArea getJTextSend() {
		if (txtSend == null) {
			txtSend = new JTextArea();
			txtSend.setText("txtSend");
			txtSend.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					// btnValidate.setEnabled(false);
				}

				@Override
				public void focusGained(FocusEvent e) {
					btnValidate.setEnabled(true);
					btnClear.setEnabled(true);
				}
			});
		}
		return txtSend;
	}

	private JTextArea getJSocketData() {
		if (txtSocket == null) {
			txtSocket = new JTextArea();
			txtSocket.setText("txtSocket");
		}
		return txtSocket;
	}

	private JButton getBtnSend() {
		if (btnSend == null) {
			btnSend = new JButton();
			btnSend.setFont(new Font("Dialog", Font.BOLD, 15));
			btnSend.setText("发送数据");
			btnSend.setEnabled(false);
			btnSend.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					btnSendActionActionPerformed(event);
				}
			});
		}
		return btnSend;
	}

	private JButton getBtnLogin() {
		if (btnLogin == null) {
			btnLogin = new JButton();
			btnLogin.setFont(new Font("Dialog", Font.BOLD, 15));
			btnLogin.setText("登录");
			btnLogin.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent event) {
					btnLoginActionActionPerformed(event);
				}
			});
		}
		return btnLogin;
	}

	private JLabel getJLabel3() {
		if (jLabel3 == null) {
			jLabel3 = new JLabel();
			jLabel3.setFont(new Font("Dialog", Font.BOLD, 15));
			jLabel3.setText("toJID：");
		}
		return jLabel3;
	}

	private JLabel getJLabel2() {
		if (jLabel2 == null) {
			jLabel2 = new JLabel();
			jLabel2.setFont(new Font("Dialog", Font.BOLD, 15));
			jLabel2.setText("fromJID：");
		}
		return jLabel2;
	}

	private JTextField getJfromJID() {
		if (fromJID == null) {
			fromJID = new JTextField();
			fromJID.setFont(new Font("Dialog", Font.PLAIN, 15));
			fromJID.setText("debuger@joyveb/local");
		}
		return fromJID;
	}

	private JTextField getServerAddr() {
		if (serverAddr == null) {
			serverAddr = new JTextField();
			serverAddr.setFont(new Font("Dialog", Font.PLAIN, 15));
			serverAddr.setText("192.168.3.10");
		}
		return serverAddr;
	}

	private JLabel getServportLabel() {
		if (jLabel1 == null) {
			jLabel1 = new JLabel();
			jLabel1.setFont(new Font("Dialog", Font.BOLD, 15));
			jLabel1.setText("Router端口：");
		}
		return jLabel1;
	}

	private JTextField getServport() {
		if (servport == null) {
			servport = new JTextField();
			servport.setFont(new Font("Dialog", Font.PLAIN, 15));
			servport.setText("10080");
		}
		return servport;
	}

	private JLabel getJLabel0() {
		if (jLabel0 == null) {
			jLabel0 = new JLabel();
			jLabel0.setFont(new Font("Dialog", Font.BOLD, 15));
			jLabel0.setText("Router地址：");
		}
		return jLabel0;
	}

	private static void installLnF() {
		try {
			String lnfClassname = PREFERRED_LOOK_AND_FEEL;
			if (lnfClassname == null)
				lnfClassname = UIManager.getCrossPlatformLookAndFeelClassName();
			UIManager.setLookAndFeel(lnfClassname);
		} catch (Exception e) {
			System.err.println("Cannot install " + PREFERRED_LOOK_AND_FEEL
					+ " on this platform:" + e.getMessage());
		}
	}

	/**
	 * Main entry of the class. Note: This class is only created so that you can
	 * easily preview the result at runtime. It is not expected to be managed by
	 * the designer. You can modify it as you like.
	 */
	public static void main(String[] args) {
		installLnF();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				PacketDebuger frame = new PacketDebuger();
				frame.setDefaultCloseOperation(PacketDebuger.EXIT_ON_CLOSE);
				frame.setTitle("PacketDebuger");
				frame.getContentPane().setPreferredSize(frame.getSize());
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}

	private void btnLoginActionActionPerformed(ActionEvent event) {
		if (rc != null) {
			rc.shutdown();
		}
		try {
			rc = new GrizzlyClient(new JID(fromJID.getText()), new JID(
					"router@joyve/local"), this);
			rc.init(this.serverAddr.getText(),
					Integer.parseInt(this.servport.getText()), 2,
					new AuthEntity("joyveb"), 60);
			btnLogin.setEnabled(false);
			btnLogout.setEnabled(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void btnSendActionActionPerformed(ActionEvent event) {
		System.out.println("send::" + txtSend.getText());
		String recvtxt;
		try {
			JsonPacket recv = rc.syncSend(jrp);

			recvtxt = recv.toJsonArrayTxt();

		} catch (Exception e) {
			e.printStackTrace();
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(bout));
			recvtxt = new String(bout.toByteArray());
		}
		txtRecv.setText(recvtxt);
	}

	private void btnLogoutActionActionPerformed(ActionEvent event) {
		if (rc != null) {
			rc.shutdown();
		}
		rc = null;
		btnLogin.setEnabled(true);
		btnLogout.setEnabled(false);
		btnSend.setEnabled(false);
		btnValidate.setEnabled(false);
		btnClear.setEnabled(false);
	}

	@Override
	public void handleIncoming(JsonPacket en,JsonPacketResponse response)
			throws ComponentException {
		System.out.println("recv:" + en);
	}

}
