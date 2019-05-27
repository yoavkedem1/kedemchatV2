package kchat.client;

import static kchat.common.utils.CollectionUtils.Tuple;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import kchat.common.Utils;
import kchat.common.utils.CollectionUtils;

public class ChatMenuUIManager implements UIManager {
	
	private JScrollPane m_panel;
	private JPanel m_chats;
	private Client m_client;
	
	private int m_windowId;
	
	private List<ClientChat> m_shown = new LinkedList<>();
	
	public ChatMenuUIManager(Client client, int windowId) {
		m_client = client;
		m_windowId = windowId;
	}
	
	// a method the define what will happen we the chat menu will start
	
	@Override
	public void onStart(JFrame frame) {
		m_chats = new JPanel();
		m_chats.setBackground(Color.WHITE);
		
		m_chats.setLayout(new BoxLayout(m_chats, BoxLayout.PAGE_AXIS));
		m_chats.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JPanel m_flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		m_flowPanel.setBorder(new TitledBorder(new EtchedBorder(), "Chats"));
		m_flowPanel.setMaximumSize(frame.getSize());
		m_flowPanel.setBackground(Color.white);
		
		m_flowPanel.add(m_chats);
		
		JButton m_voiceCall = new JButton("Voice Call");
		m_voiceCall.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				Request<List<Long>> reqId = m_client.getOnlineClients();
				List<String> online = reqId.flatMap(m_client::getClientNames).fetch();
				Iterable<Tuple<Long, String>> tupled = CollectionUtils.zip(reqId.fetch(), online);
				List<Tuple<Long, JButton>> buttons = new LinkedList<>();
				tupled.forEach(x -> buttons.add(Tuple(x._1, new JButton(x._2))));
				
				JFrame i_frame = new JFrame("Choose a callee");
				i_frame.setSize(600, 600);
				i_frame.setLocationRelativeTo(null);
				JPanel i_users = new JPanel();
				i_users.setBackground(Color.WHITE);
				
				i_users.setLayout(new BoxLayout(i_users, BoxLayout.PAGE_AXIS));
				i_users.setAlignmentX(Component.LEFT_ALIGNMENT);
				
				JPanel i_flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

				i_flowPanel.setBorder(new TitledBorder(new EtchedBorder(), "Users"));
				i_flowPanel.setMaximumSize(i_frame.getSize());
				i_flowPanel.setBackground(Color.white);
				

				for (Tuple<Long, JButton> tup : buttons) {
					if (tup._1 != m_client.getUUID()) {
						i_users.add(tup._2);
						tup._2.addMouseListener(new MouseAdapter() {
							public void mouseReleased(MouseEvent e) {
								Request<InetAddress> rip = m_client.requestClientIP(tup._1);
								InetAddress ip = rip.fetch();
								DatagramSocket socket = m_client.getCallSocket();
								ByteBuffer buf = ByteBuffer.allocate(1024);
								Utils.writeIP(buf, m_client.requestClientIP(m_client.getUUID()).fetch());
								Utils.writeUTF8(buf, m_client.getName());
								DatagramPacket packet = new DatagramPacket(buf.array(), 1024, ip, socket.getPort());
								try {
									socket.send(packet);
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								byte[] arr = new byte[1024];
								DatagramPacket packIn = new DatagramPacket(arr, 1024);
								try {
									socket.receive(packIn);
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								if (arr[0] != 0x01) return;
								if (arr[1] == 0x01) {
									Voice voice = new Voice(m_client, tup._2.getText(), ip, AudioFormats._44100HZ_16BIT);
									
									voice.start();
								}
								
								i_frame.dispose();
							}
						});
					}
				}
				i_flowPanel.add(i_users);
				
				
				i_frame.add(i_flowPanel);
				i_frame.setVisible(true);
			}
		});
		JButton m_newChat = new JButton("New Chat");
		m_newChat.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				String chatName = JOptionPane.showInputDialog(frame, "Enter the chat's name:", "New Chat!", JOptionPane.QUESTION_MESSAGE);
			
				if (chatName != null && !chatName.isEmpty()) {
					Request<List<Long>> reqId = m_client.getOnlineClients();
					
					List<String> online = reqId.flatMap(m_client::getClientNames).fetch();
					Iterable<Tuple<Long, String>> tupled = CollectionUtils.zip(reqId.fetch(), online);
					List<Tuple<Long, JCheckBox>> boxes = new LinkedList<>();
					tupled.forEach(x -> boxes.add(Tuple(x._1, new JCheckBox(x._2))));
					
					JFrame i_frame = new JFrame("Chat Participants");
					i_frame.setSize(600, 600);
					i_frame.setLocationRelativeTo(null);
					JPanel i_users = new JPanel();
					i_users.setBackground(Color.WHITE);
					
					i_users.setLayout(new BoxLayout(i_users, BoxLayout.PAGE_AXIS));
					i_users.setAlignmentX(Component.LEFT_ALIGNMENT);
					
					JPanel i_flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

					i_flowPanel.setBorder(new TitledBorder(new EtchedBorder(), "Users"));
					i_flowPanel.setMaximumSize(i_frame.getSize());
					i_flowPanel.setBackground(Color.white);
					
					i_flowPanel.add(i_users);
					for (Tuple<Long, JCheckBox> tup : boxes) {
						if (tup._1 != m_client.getUUID())
							i_users.add(tup._2);
					}
					
					JButton i_button = new JButton("Confirm!");
					i_button.addMouseListener(new MouseAdapter() {
						public void mouseReleased(MouseEvent e) {
							Set<Long> set = boxes.stream().filter(x -> x._2.isSelected()).map(x -> x._1)
									.collect(Collectors.toSet());
							set.add(m_client.getUUID());
							Commands.send(m_client, new Commands.NewChat(chatName, 
									set,
										System.currentTimeMillis()));
							i_frame.setVisible(false);
							i_frame.dispose();
						}
					});
					i_users.add(i_button);
					i_frame.add(i_flowPanel);
					i_frame.setVisible(true);
					
				}
			}
		});
		

		m_chats.add(m_newChat);
		m_chats.add(m_voiceCall);
		m_panel = new JScrollPane(m_flowPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		m_panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		m_panel.setAlignmentY(JTextPane.TOP_ALIGNMENT);
		int frameWidth = frame.getWidth();
		for (ClientChat chat: m_client.getChats()) {
			m_chats.add(makeChatBox(chat, frameWidth));
			m_shown.add(chat);
		}
		
		frame.getContentPane().add(m_panel);		
	}
	
	// a class the define the chat ui
	
	private JPanel makeChatBox(ClientChat chat, int width){
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(width - 40, 50));
		panel.setBackground(Color.white);
		panel.setBorder(new TitledBorder(new EtchedBorder(), chat.getName()));
		
		panel.add(new JLabel("Members: " + chat.getClient().getClientNames(chat.getClients().stream().collect(Collectors.toList())).get().orElse(Arrays.asList("ERROR"))));
		
		//mouse listeners
		
		MouseAdapter m_adapter = new MouseAdapter() {
			private long last = 0;
			public boolean lastState = false;
			
			public void mouseReleased(MouseEvent e) {
				m_client.getUI(0).getFrame().requestFocus();
				m_client.getUI(0).setManager(new ChatUIManager(chat));
			}
			
			public void mouseEntered(MouseEvent e) {
				if (e.getWhen() < last)
					return;
				last = e.getWhen();
				if (!lastState) {
					lastState = true;
					panel.setBorder(new TitledBorder(
							new EtchedBorder(new Color(0, 230, 230), Color.blue), chat.getName(),
							0, 0, panel.getFont(), Color.blue));
					
				}
			}
			
			public void mouseExited(MouseEvent e) {
				if (e.getWhen() < last) 
					return;
				last = e.getWhen();
				if (lastState) {
					lastState = false;
					panel.setBorder(
						new TitledBorder(new EtchedBorder(), chat.getName()));
				}
			}
		};
		
		panel.addMouseListener(m_adapter);		
		
		return panel;
	}

	// a method that delete the ui when it closes
	
	@Override
	public void onClose(JFrame frame) {
		frame.getContentPane().removeAll();
	}
	
	// a method that update the ui 
	
	@Override
	public void update(JFrame frame) {
		
		int frameWidth = frame.getWidth();
		for (ClientChat chat: m_client.getChats().stream().
				filter(x -> !m_shown.contains(x)).collect(Collectors.toList())) {
			m_shown.add(chat);
			m_chats.add(makeChatBox(chat, frameWidth));
		}
		
	}

}
