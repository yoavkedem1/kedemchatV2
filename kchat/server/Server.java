package kchat.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import kchat.common.ClientData;
import kchat.common.Utils;

public class Server {
	
	
	private List<ServerChat> m_chats;
	private List<ClientData> m_clients;
	private ServerSocket m_socket;
	private Map<Long, Socket> m_sockets;

	private boolean shouldRun = true;
	
	//set up the server
	
	public Server() {
		m_chats = new LinkedList<>();
		m_sockets = new HashMap<>();
		m_clients = new LinkedList<>();
		try {
			m_socket = new ServerSocket(1337);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		new Commands.NewChatIn(this);
		new Commands.NewMessageIn(this);
		new Commands.UpdateCommandIn(this);
		new Commands.ClientNameIn(this);
		new Commands.ConnectedIn(this);
		new Commands.ClientIpIn(this);
		new Thread(this::listenThread).start();

	}

	public static void debug(String msg) {
		if (Utils.DEBUG_ENABLED) {
			StackTraceElement ste = Utils.stackTrace(3);
			String[] split = ste.getClassName().split("\\.");
			System.out.println("[" + split[split.length - 1] + "." +
			ste.getMethodName() + ":" + ste.getLineNumber() + "/Server] " + msg);
		}
	}
	
	// setup a thread that listen to client request 
	
	private void listenThread() {
		while (shouldRun && !Thread.interrupted()) {
			Socket socket = null;
			try {
			
				socket = m_socket.accept();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			final Socket finalSock = socket;
			new Thread(() -> {
				try {
					comThread(finalSock);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}).start();

		}
	}
	
	//set up the sockets with the clients
	
	private void comThread(Socket socket) throws IOException {
		InputStream sis = socket.getInputStream();
		byte[] arr = new byte[256];

		sis.read(arr);

		ByteBuffer buf = ByteBuffer.wrap(arr);
		long uuid = buf.getLong();
		String name = Utils.readUTF8(buf);
		if (m_clients.stream().noneMatch(c -> c.getUUID() == uuid)) {
			m_clients.add(new ClientData(uuid, name));
		}

		m_sockets.put(uuid, socket);
		try {
			while (shouldRun && !Thread.interrupted()) {
				byte[] bytes = new byte[2048];
				sis.read(bytes);
				Commands.decode(ByteBuffer.wrap(bytes));
			}
		} catch (SocketException e) {
			disconnect(name);
		}
	}
	
	//remove the client from the server
	
	public void disconnect(String clientName) {
		ClientData cd = m_clients.stream().filter(x -> x.getName().equals(clientName)).findFirst().get();
		m_clients.remove(cd);
		m_sockets.remove(cd.getUUID());
		Server.debug("Client `" + clientName + "` has been disconnected!");
		
		
	}
	
	public List<ServerChat> getChats() {
		return m_chats;
	}

	public Optional<ServerChat> getChat(long uuid) {
		return m_chats.stream().filter((chat) -> chat.getUUID() == uuid).findFirst();
	}

	public void addChat(ServerChat chat) {
		m_chats.add(chat);
	}
	
	public Set<ClientData> getClients(){
		return m_clients.stream().collect(Collectors.toSet());
	}
	
	public ClientData getClient(long uuid){
		return m_clients.stream().filter((x) -> x.getUUID() == uuid).findFirst().get();
	}
	
	public void sendBuffer(ByteBuffer buffer, List<Long> clients) {

		List<Socket> sockets = clients.stream().filter(c -> 
		m_sockets.keySet().stream().anyMatch(x -> x == c)).map(c -> m_sockets.get(c))
				.collect(Collectors.toList());
		sockets.forEach(x -> {
			try {
				x.getOutputStream().write(buffer.array());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	public InetAddress getClientIP(long m_uuid) {
		return m_sockets.get(m_uuid).getInetAddress();
	}
}
