package kchat.client;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import kchat.common.ChatMessage;
import kchat.common.ClientData;
import kchat.common.Utils;

//Manages chat commends 

public class Commands extends kchat.common.Commands{
	
	public static class NewMessage extends OutCommand{
		private final ChatMessage m_msg;
		private final ClientChat m_chat;
		
		
		public NewMessage(ClientChat chat, ChatMessage msg) {
			super(NEW_MESSAGE);
			m_msg = msg;
			m_chat = chat;
		}
		public void encode(ByteBuffer buffer) {
			buffer.putLong(m_chat.getUUID());
			buffer.put(m_msg.encode().array());
		}
	}
	
	public static class ConnectedOut extends OutCommand{
		
		private Client m_client;
		// connect out from the server
		public ConnectedOut(Client client) {
			super(CONNECTED_IN);
			m_client = client;
		}
		
		public void encode(ByteBuffer buffer) {
			buffer.putLong(m_client.getUUID());
		}
	}
	
	public static class NamesOut extends OutCommand{
		private Set<Long> m_clients;
		private Client m_client;
		public NamesOut(Client client, Set<Long> clients) {
			super(CLIENT_NAME);
			m_clients = clients;
			m_client = client;
		}
		
		public void encode(ByteBuffer buffer) {
			buffer.putLong(m_client.getUUID());
			buffer.putInt(m_clients.size());
			for (Long client : m_clients) {
				buffer.putLong(client);
			}
		}
	}
	//update data for the user
	public static class UpdateData extends OutCommand{
		private final long m_from;
		private final ClientData m_data;
		public UpdateData(long from, ClientData data) {
			super(UPDATE_DATA);
			m_from = from;
			m_data = data;
		}
		
		public void encode(ByteBuffer buffer) {
			buffer.putLong(m_from);
			buffer.putLong(m_data.getUUID());
		}
	}
	// Generate a new chat
	public static class NewChat extends OutCommand{
		private final String m_name;
		private final Set<Long> m_clients;
		private final long m_date;
		
		public NewChat(String name, Set<Long> clients, long date) {
			super(NEW_CHAT);
			m_name = name;
			m_clients = clients;
			m_date = date;
		}
		
		public void encode(ByteBuffer buffer) {
			buffer.putInt(m_name.getBytes().length);
			buffer.put(m_name.getBytes());
			buffer.putInt(m_clients.size());
			m_clients.forEach((c)-> buffer.putLong(c));
			buffer.putLong(m_date);
		}
	}
	// notify the clients there are new message
	public static class NewMessageIn extends InCommand {
		private final Client m_client;

		public NewMessageIn(Client client) {
			super(NEW_MESSAGE);
			m_client = client;
		}

		public void decode(ByteBuffer buffer) {
			long chat = buffer.getLong();
			ChatMessage message = ChatMessage.read(buffer);
			
			Client.debug("New chat message was received");
			Client.debug(message.toString());
			
			m_client.getChat(chat).ifPresent((c) -> c.addMessage(message));
		}
	}
	// notify the client he is in a new chat
	public static class NewChatIn extends InCommand {

		private Client m_client;

		public NewChatIn(Client client) {
			super(NEW_CHAT);
			this.m_client = client;
		}

		public void decode(ByteBuffer buffer) {
			String str = Utils.readUTF8(buffer);
			int clientCount = buffer.getInt();
			Set<Long> clients = new HashSet<>();
			for (int i = 0; i < clientCount; i++) {
				clients.add(buffer.getLong());
			}

			long date = buffer.getLong();
			long uuid = buffer.getLong();
			
			ClientChat chat = new ClientChat(m_client, uuid, str, date, clients, new HashSet<>());
			
			Client.debug("A new chat was opened!");
			Client.debug(chat.toString());

			m_client.addChat(chat);

		}
	}
	
	// send client ip and id
	public static class ClientIpOut extends OutCommand{
		private long m_uuid;
		
		private Client m_client;
		
		public ClientIpOut(long uuid, Client client) {
			super(CLIENT_IP);
			m_uuid = uuid;
			m_client = client;
		}
		
		
		public void encode(ByteBuffer buf) {
			buf.putLong(m_client.getUUID());
			buf.putLong(m_uuid);
		}
		
		public long getUUID() {
			return m_uuid;
		}
		
		
	}
	// get user ip
	public static class ClientIpIn extends InCommand{
		
		private Client m_client;
		
		public ClientIpIn(Client client) {
			super(CLIENT_IP);
			m_client = client;
		}
		
		public void decode(ByteBuffer buf) {
			long uuid = buf.getLong();
			InetAddress ip = Utils.readIP(buf);
			m_client.setClientIP(uuid, ip);

		}
		
		
		
		
	}
	
	

	
	public static class ConnectedIn extends InCommand {

		private Client m_client;
		
		
		public ConnectedIn(Client client) {
			super(CONNECTED_IN);
			this.m_client = client;
		}

		public void decode(ByteBuffer buffer) {
			int clientCount = buffer.getInt();
			
			List<Long> clients = new LinkedList<>();
			for (int i = 0; i < clientCount; i++) {
				clients.add(buffer.getLong());
			}
			m_client.setConnected(clients);
			
			
		}
		

	}
	
	public static class ClientName extends InCommand {
		private Client m_client;
		
		public ClientName(Client client) {
			super(CLIENT_NAME);
			this.m_client = client;
		}
		
		public void decode(ByteBuffer buffer) {
			int count = buffer.getInt();
			for (int i = 0; i < count; i++) {
				long client = buffer.getLong();
				String str = Utils.readUTF8(buffer);
				m_client.setName(client, str);
			}
		}
		
	}
	
	public static final void send(Client client, OutCommand out) {
		send(client, out, 2048);
	}
	
	public static final void send(Client client, OutCommand out, int capacity) {
		ByteBuffer buf = ByteBuffer.allocate(capacity);
		out.write(buf);
		client.sendBuffer(buf);
	}
	
	
}
