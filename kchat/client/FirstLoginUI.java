package kchat.client;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UnsupportedLookAndFeelException;

import kchat.common.ClientData;
import kchat.common.StringMessage;

//manage ui screen for login

public class FirstLoginUI extends JFrame{
	public FirstLoginUI(int width, int height) {
		super("Kedem Chat - Join a server!");
		try {
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		setSize(width, height);
		setLayout(new FlowLayout());
		setPreferredSize(getSize());
		setLocationRelativeTo(null);
		JLabel serverLabel = new JLabel("Server IP:");
		JLabel nameLabel = new JLabel("Username:");
		JTextField serverField = new JTextField(30);
		JTextField nameField = new JTextField(30);
		JButton submit = new JButton("Submit");
		submit.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				if (serverField.getText().isEmpty()) {
					JOptionPane.showMessageDialog(FirstLoginUI.this, "Server IP cannot be empty!", "Login Error", JOptionPane.ERROR_MESSAGE);
				}
				else if (nameField.getText().isEmpty()) {
					JOptionPane.showMessageDialog(FirstLoginUI.this, "Username cannot be empty!", "Login Error", JOptionPane.ERROR_MESSAGE);
				}
				else {
					InetAddress ip = null;
					boolean hasCrashed = false;
					try {
						ip = InetAddress.getByName(serverField.getText());
					} catch (UnknownHostException e1) {
						hasCrashed = true;
						JOptionPane.showMessageDialog(FirstLoginUI.this, "Could not find `" + serverField.getText() + "`", "Login Error", JOptionPane.ERROR_MESSAGE);
					}
					if (!hasCrashed) {
						Client client = new Client(new ClientData(new Random().nextInt(0xff), nameField.getText()));
						client.bind(ip);
						FirstLoginUI.this.dispose();
						HashSet<Long> m_set = new HashSet<Long>();
						m_set.add(client.getUUID());
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
						client.startUI();
						
						
						try {
							Thread.sleep(1500);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						//<Segment 2> 
						Client.debug("Segment 2");
						
					//	ui.setManager(new ChatMenuUIManager(client));
							
						
						//<Segment 3>
						
						client.getAllWindows().forEach(x -> x.open());
					}
			
					
				}
			}
		});
		add(serverLabel);
		add(serverField);
		add(nameLabel);
		add(nameField);
		add(submit);
	}
	
	
	
	
}
