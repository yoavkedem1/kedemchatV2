package kchat.client;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.function.Supplier;

import javax.sound.sampled.AudioFormat;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Voice {
	
	private static int PLAY_BUFFER_SIZE = 60000;
	private static int LISTEN_PORT = 2005;
	private static int OUT_PORT = 2004;
	
	private Client m_local;
	private InetAddress m_remote;
	private AudioFormat m_format;
	private String m_remoteName;
	
	public Voice(Client client, String remoteName, InetAddress remote, AudioFormat format) {
		m_local = client;
		m_remoteName = remoteName;
		m_remote = remote;
		m_format = format;
	}
	
	
	public Runnable periodicPlay(Supplier<Boolean> shouldRun) {
		DatagramSocket _listenSock = null;
		try {
			_listenSock = new DatagramSocket(LISTEN_PORT);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		
		DatagramSocket listenSock = _listenSock;
		
		byte[] arr = new byte[PLAY_BUFFER_SIZE];
		
		VoicePlayer player = new VoicePlayer(m_format);
		return () -> {
		while (shouldRun.get()) {
			DatagramPacket pack = new DatagramPacket(arr, arr.length);
			try {
				listenSock.receive(pack);
			} catch (IOException e) {
				e.printStackTrace();
			}
			player.play(arr);
		}
		};
	}
	
	public Runnable periodicRecord(Supplier<Boolean> shouldRun) {
		DatagramSocket _writeSock = null;
		try {
			_writeSock = new DatagramSocket(OUT_PORT);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		
		DatagramSocket writeSock = _writeSock;
		
		VoiceRecorder record = new VoiceRecorder(m_format, (bytes) -> { 
			try {
				writeSock.send(new DatagramPacket(bytes, bytes.length, m_remote, LISTEN_PORT));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		);
		
		
		return () -> record.start();
	}
	
	public void start() {
		JFrame callingFrame = new JFrame("Calling " + m_remoteName);
		callingFrame.add(new JLabel("Calling " + m_remoteName));
		callingFrame.setSize(400, 200);
		callingFrame.setLocationRelativeTo(null);
		callingFrame.setVisible(true);
		
		JButton endCall = new JButton("End Call!");
		JFrame callFrame = new JFrame();
		callFrame.add(new JLabel("You are currently in a call with: " +
					m_remoteName));
		boolean[] encap = new boolean[] {true};
		endCall.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				encap[0] = false;
				callFrame.dispose();
			}
		});
		callFrame.add(endCall);
		callFrame.setVisible(true);
		
		Runnable play = periodicPlay(() -> encap[0]);
		Runnable record = periodicRecord(() -> encap[0]);
		Thread playThread = new Thread(play);
		Thread recordThread = new Thread(record);
		playThread.start();
		recordThread.start();
	}

	
	

	
	
}
