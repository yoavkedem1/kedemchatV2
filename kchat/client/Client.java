package kchat.client;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;

import kchat.common.ClientData;
import kchat.common.Utils;
import kchat.common.utils.CollectionUtils.Tuple;

public class Client {
	// a class the does the handling on the client side
	public static void debug(String msg) {

		if (Utils.DEBUG_ENABLED) {
			StackTraceElement ste = Utils.stackTrace(3);
			String[] split = ste.getClassName().split("\\.");
			System.out.println("[" + split[split.length - 1] + "." + ste.getMethodName() + ":" + ste.getLineNumber()
					+ "/Client] " + msg);
		}
	}

	private List<ClientChat> m_chats;
	private ClientData m_data;
	private Socket m_socket;
	private List<ClientUI> m_uis;
	private Map<Long, String> m_cachedNames;
	private boolean m_updateConnect;
	private boolean m_updateNames;
	private DatagramSocket m_callSocket;

	private Map<Long, Tuple<InetAddress, Long>> m_cachedIps;

	private List<Long> m_connected;

	public Client(ClientData data) {
		this(data, 3339);
	}
	
	// the constructor of the class 
	
	public Client(ClientData data, int callPort) {
		m_data = data;
		m_chats = new LinkedList<>();

		m_uis = new LinkedList<>();

		m_cachedNames = new HashMap<>();
		m_cachedIps = new HashMap<>();
		m_connected = new LinkedList<>();
		m_callSocket = null;
		try {
			m_callSocket = new DatagramSocket(callPort);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		Thread callThread = new Thread(this::listenForCalls);
		callThread.start();

		new Commands.NewChatIn(this);
		new Commands.NewMessageIn(this);
		new Commands.ConnectedIn(this);
		new Commands.ClientName(this);
		new Commands.ClientIpIn(this);
	}
	
	protected DatagramSocket getCallSocket() {
		return m_callSocket;
	}
	
	// a method that run forever that wait for a voice call and handle it
	
	private void listenForCalls() {
		while (true) {
			byte[] arr = new byte[1024];
			DatagramPacket packet = new DatagramPacket(arr, 1024);
			try {
				m_callSocket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			ByteBuffer buf = ByteBuffer.wrap(arr);
			if (buf.get() != 1)
				continue;
			InetAddress remote = Utils.readIP(buf);
			String remoteName = Utils.readUTF8(buf);
			JFrame answerFrame = new JFrame(remoteName + " is calling");
			answerFrame.add(new JLabel(remoteName + " is calling"));
			JButton accept = new JButton("Accept");
			JButton decline = new JButton("Decline");
			accept.addMouseListener(new MouseAdapter() {
				public void mouseReleased(MouseEvent e) {
					answerFrame.dispose();
					DatagramPacket out = new DatagramPacket(new byte[] { 0x01, 0x01 }, 2, remote,
							m_callSocket.getPort());
					try {
						m_callSocket.send(out);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					Voice voice = new Voice(Client.this, remoteName, remote, AudioFormats._44100HZ_16BIT);
					voice.start();
				}
			});
			decline.addMouseListener(new MouseAdapter() {

				public void mouseReleased(MouseEvent e) {
					answerFrame.dispose();
					DatagramPacket out = new DatagramPacket(new byte[] { 0x01, 0x02 }, 2, remote,
							m_callSocket.getPort());
					try {
						m_callSocket.send(out);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
				}
			});
			answerFrame.add(accept);
			answerFrame.add(decline);

		}
	}
	// start the gui
	public void startUI() {
		m_uis.add(new ClientUI(this, 0, 640, 480)); // Main UI
		m_uis.add(new ClientUI(this, 1, 200, 480)); // Secondary UI
		m_uis.get(1).setManager(new ChatMenuUIManager(this, 1));
	}

	public ClientUI getUI(int id) {
		return m_uis.get(id);
	}

	public int windowCount() {
		return m_uis.size();
	}

	public List<ClientUI> getAllWindows() {
		return m_uis;
	}
	//call the bind function
	public boolean bind(InetAddress address) {
		return bind(address, 1337);
	}
	
	// wait for information from the server
	
	private void listenThread() throws IOException {
		Timer t = new Timer(1000,
				a -> Commands.send(this, new Commands.UpdateData(System.currentTimeMillis() - 1000, m_data)));
		t.start();

		InputStream is = m_socket.getInputStream();
		while (!Thread.interrupted()) {
			byte[] bytes = new byte[2048];
			is.read(bytes);
			ByteBuffer buf = ByteBuffer.wrap(bytes);
			Commands.decode(buf);
		}
	}
	// bind the socket to the server
	public boolean bind(InetAddress address, int port) {
		try {
			m_socket = new Socket();
			SocketAddress addr = new InetSocketAddress(address, port);

			m_socket.connect(addr, 500);
			ByteBuffer buf = ByteBuffer.allocate(256);
			buf.putLong(m_data.getUUID());
			Utils.writeUTF8(buf, m_data.getName()); // decode the data that will be sent in UTF-8
			m_socket.getOutputStream().write(buf.array());
			Commands.send(this, new Commands.UpdateData(0l, m_data));
			new Thread(() -> {
				try {
					listenThread();			// run the listing thread
				} catch (IOException e) {
					e.printStackTrace();
				}
			}).start();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public String getName() {
		return m_data.getName();
	}

	public long getUUID() {
		return m_data.getUUID();
	}

	public Optional<ClientChat> getChat(long id) {
		return m_chats.stream().filter((chat) -> chat.getUUID() == id).findFirst();
	}

	public void addChat(ClientChat chat) {
		m_chats.add(chat);
	}

	public List<ClientChat> getChats() {
		return m_chats;
	}

	public void setName(long client, String name) {
		m_cachedNames.put(client, name);
	}

	public Request<String> getName(long client) {
		return getClientNames(Arrays.asList(client)).map(x -> x.get(0));
	}

	public boolean sendBuffer(ByteBuffer buffer) {
		try {
			m_socket.getOutputStream().write(buffer.array());
		} catch (IOException e) {
			System.out.println(e);
			return false;
		}
		return true;
	}
	
	// gets clients from server
	
	public Request<List<Long>> getOnlineClients() {
		m_updateConnect = false;
		Commands.send(this, new Commands.ConnectedOut(this));

		return Request.future(() -> m_updateConnect ? Optional.of(m_connected) : Optional.empty());
	}
	
	// get the name of the clients
	
	public Request<List<String>> getClientNames(final List<Long> clients) {
		List<Long> missing = clients.stream().filter(x -> !m_cachedNames.containsKey(x)).collect(Collectors.toList());
		if (missing.isEmpty()) {
			return Request.pure(clients.stream().map(m_cachedNames::get).collect(Collectors.toList()));
		}

		m_updateNames = false;
		Commands.send(this, new Commands.NamesOut(this, clients.stream().collect(Collectors.toSet())));
		Commands.send(this, new Commands.NamesOut(this, clients.stream().collect(Collectors.toSet())));

		return Request.future(() -> m_updateNames
				? Optional.ofNullable(clients.stream().map(m_cachedNames::get).collect(Collectors.toList()))
				: Optional.empty());

	}

	public void setConnected(List<Long> clients) {
		m_connected = clients;
		m_updateConnect = true;
	}
	//get your ip from the server
	public Request<InetAddress> requestClientIP(long uuid) {
		Commands.send(this, new Commands.ClientIpOut(uuid, this));
		return Request.future(
				() -> (m_cachedIps.containsKey(uuid) && System.currentTimeMillis() - m_cachedIps.get(uuid)._2 < 2000)
						? Optional.ofNullable(m_cachedIps.get(uuid)._1)
						: Optional.empty());
	}

	public void setClientIP(long uuid, InetAddress ip) {
		m_cachedIps.put(uuid, new Tuple<>(ip, System.currentTimeMillis()));

	}

}
