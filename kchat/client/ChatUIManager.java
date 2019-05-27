package kchat.client;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import kchat.common.ChatMessage;
import kchat.common.CompoundMessage;
import kchat.common.StringMessage;

// a class the manage the chat ui

public class ChatUIManager implements UIManager{

	private long fromDate = 0;
	private long toDate = System.currentTimeMillis();
	
	private ClientChat m_chat;
	
	private JScrollPane m_pane;
	private JPanel m_chatBox;
	private JTextField m_chatField;
	private JButton m_sendButton;
	private JPanel m_panel;
	private GridBagConstraints m_gbc = new GridBagConstraints();
	
	public ChatUIManager(ClientChat chat) {
		m_chat = chat;
	}
	
	
	
	//send the message in the chatField to all the members of the chat
	protected void sendMessage() {
		String text = m_chatField.getText();
		m_chat.sendMessage(
			new StringMessage(text, Color.black, Font.PLAIN, System.currentTimeMillis(), m_chat.getClient().getUUID(), 1));
		
		m_chatField.setText("");
	}
	
	
	//define how the ui will lool
	@Override
	public void onStart(JFrame frame) {
		m_chatBox = new JPanel();
		m_chatBox.setBackground(Color.WHITE);
		GridBagLayout gbl = new GridBagLayout();
		m_gbc.anchor = GridBagConstraints.WEST;
		m_gbc.gridx = 0;
		
		m_chatBox.setLayout(gbl);
		m_chatBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		
		JPanel m_flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		m_flowPanel.setBorder(new TitledBorder(new EtchedBorder(), "Chat - " + m_chat.getName()));
		m_flowPanel.setMaximumSize(frame.getSize());
		m_flowPanel.setBackground(Color.white);
		m_flowPanel.add(m_chatBox);
		
		
		m_pane = new JScrollPane(m_flowPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		m_pane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		m_pane.setAlignmentY(JTextPane.TOP_ALIGNMENT);
		
		frame.getContentPane().add(m_pane);
		
		m_sendButton = new JButton("Send");
		m_sendButton.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				sendMessage();
			}});
		
		m_panel = new JPanel(new FlowLayout());
		m_panel.setMaximumSize(new Dimension(frame.getWidth(), 24));
		m_panel.add(m_sendButton);
		
		frame.getContentPane().add(m_panel);
		
		
		Dimension dim1 = m_panel.getMaximumSize();
		Dimension dim2 = m_sendButton.getPreferredSize();
		
		m_chatField = new JTextField("");
		m_chatField.setPreferredSize(new Dimension(dim1.width - dim2.width - 30 , dim1.height));
		m_chatField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					sendMessage();
				}
			}
		});
		
		m_panel.add(m_chatField);
		
		List<ChatMessage> messages = 
				m_chat.getMessages().stream().sorted().collect(Collectors.toList());
		
		drawMessages(frame, messages);
		
		m_chatField.requestFocus();
		//m_chatField.setSele
	}
	
	
	//closes the ui
	@Override
	public void onClose(JFrame frame) {
		frame.remove(m_pane);
		frame.remove(m_panel);
	}
	
	@Override
	public void update(JFrame frame) {
		
		m_panel.setMaximumSize(new Dimension(frame.getWidth(), 20));
		
		toDate = System.currentTimeMillis();
		drawMessages(frame, m_chat.inRange(fromDate, toDate).stream().sorted().collect(Collectors.toList()));
		
		
		Dimension dim1 = m_panel.getMaximumSize();
		Dimension dim2 = m_sendButton.getPreferredSize();
		
		m_chatField.setPreferredSize(new Dimension(dim1.width - dim2.width - 30 , dim1.height));
		
		
	}
	//show message that have been sent
	private void drawMessages(JFrame frame, List<ChatMessage> messages) {
		m_chatBox.removeAll();
		for (ChatMessage msg: messages) {
			JPanel comp = makeComponent(frame, msg);
			comp.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			m_chatBox.add(comp, m_gbc);
			comp.setLocation(0, comp.getY());
		}
	}
	
	// a sub method of drawMessages, it get the messege format
	
	private JPanel makeComponent(JFrame frame, ChatMessage message) {
		switch (message.getMessageId()){
		case ChatMessage.STRING_MESSAGE:
		{
			JPanel panel = new JPanel();
			panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			
			String name = m_chat.getClient().getName(message.getSenderID()).orElse("Client#" + message.getSenderID());
			
			JLabel nameLabel = new JLabel("<" + new Date(message.getDate()) + "|" + name + ">:");
			nameLabel.setFont(frame.getContentPane().getFont());
			
			StringMessage strMsg = (StringMessage) message;
			
			JLabel messageLabel = 
					new JLabel(strMsg.getMessage());
			
			messageLabel.setForeground(strMsg.getColor());
			messageLabel.setFont(frame.getContentPane().getFont().deriveFont(strMsg.getFlags()));
			
			
			panel.add("name", nameLabel);
			panel.add("msg", messageLabel);
			panel.setBackground(Color.white);
			
			return panel;
		}
		case ChatMessage.COMPOUND_MESSAGE:
		{
			JPanel panel = new JPanel();
			panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			
			String name = m_chat.getClient().getName(message.getSenderID()).orElse("Client#" + message.getSenderID());
			
			JLabel nameLabel = new JLabel("<" + new Date(message.getDate()) + "|" + name + ">:");
				
			CompoundMessage cmpMsg = (CompoundMessage) message;
			
			JPanel msg_panel = new JPanel();
			msg_panel.setLayout(new CardLayout());
			msg_panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			
			for (ChatMessage msg : cmpMsg.getMessages()) {
				JPanel pane = makeComponent(frame, msg);
				msg_panel.add(pane.getComponents()[1]);
			}
			
			panel.add("name", nameLabel);
			panel.add("msg", msg_panel);
			panel.setBackground(Color.white);
			
			return panel;
		}
		default:
			return null;
	}
		
		
		
	}

}
