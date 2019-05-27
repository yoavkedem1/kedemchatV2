package kchat.server;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import kchat.client.Client;
import kchat.common.ChatMessage;
import kchat.common.ClientData;
import kchat.common.Utils;
import kchat.common.Commands.InCommand;
import kchat.common.Commands.OutCommand;

public class Commands extends kchat.common.Commands {

	//send client ip by request 
	
	public static class ClientIpOut extends OutCommand{
		private InetAddress m_ip;
		private long m_uuid;
		public ClientIpOut(long uuid, InetAddress ip) {
			super(CLIENT_IP);
			m_ip = ip;
			m_uuid = uuid;
		}
		
		
		public void encode(ByteBuffer buf) {
			buf.putLong(m_uuid);
			Utils.writeIP(buf, m_ip);
			
		}
		
		
	}
	
	// get ip from client to send it to other client
	
	public static class ClientIpIn extends InCommand{
		
		private long m_uuid;
		private Server m_server;
		
		public ClientIpIn(Server server) {
			super(CLIENT_IP);
			m_server = server;
		}
		
		public void decode(ByteBuffer buf) {
			long client = buf.getLong();
			m_uuid = buf.getLong();
			Server.debug("The client " + client + " requested the ip of client " + m_uuid);
			send(m_server, new ClientIpOut(m_uuid, m_server.getClientIP(m_uuid)), Arrays.asList(client), 1024);
		}
	
		
		public long getUUID() {
			return m_uuid;
		}
		
		
	}
	
	//add the connected client to the client list
	
	public static class ConnectedIn extends InCommand {
		
		private Server m_server;
		
		public ConnectedIn(Server server) {
			super(CONNECTED_IN);
			m_server = server;
		}
		
		public void decode(ByteBuffer buffer) {
			long client = buffer.getLong();
			List<Long> c_list = new LinkedList<>();
			c_list.add(client);
			Commands.send(m_server, new ConnectedOut(m_server), c_list, 1024);
		}
	}
	
	// get the name of the specific client
	
	public static class ClientNameIn extends InCommand{
		
		private Server m_server;
		
		public ClientNameIn(Server server) {
			super(CLIENT_NAME);
			m_server = server;
		}
		
		public void decode(ByteBuffer buffer) {
			long client = buffer.getLong();
			int length = buffer.getInt();
			Set<ClientData> clients = new HashSet<>();
			for (int i = 0; i < length; i++) {
				long uuid = buffer.getLong();
				clients.add(m_server.getClients().stream().filter(x -> x.getUUID() == uuid).findFirst().orElseGet(() -> new ClientData(uuid, "Client#" + uuid)));
			}
			List<Long> c_list = new LinkedList<>();
			c_list.add(client);
			Commands.send(m_server, new ClientNameOut(clients), c_list, 1024);
			
			
		}
		
	}
	//send the connected client list
	public static class ConnectedOut extends OutCommand{
		
		private Server m_server;
		
		public ConnectedOut(Server server) {
			super(CONNECTED_IN);
			m_server = server;
		}
		
		public void encode(ByteBuffer buffer) {
			Set<ClientData> clients = m_server.getClients();
			buffer.putInt(clients.size());
			for (ClientData cd: clients) {
				buffer.putLong(cd.getUUID());
			}
		}
	}
	
	//send the clients name
	
	public static class ClientNameOut extends OutCommand{
		
		private Set<ClientData> m_clients;
		
		public ClientNameOut(Set<ClientData> clients) {
			super(CLIENT_NAME);
			m_clients = clients;
		}
		
		public void encode(ByteBuffer buffer) {
			buffer.putInt(m_clients.size());
			for (ClientData cd: m_clients){
				buffer.putLong(cd.getUUID());
				Utils.writeUTF8(buffer, cd.getName());
			}
		}
	}
	//get a message from a client
	public static class NewMessageIn extends InCommand {
		private final Server m_server;

		public NewMessageIn(Server server) {
			super(NEW_MESSAGE);
			m_server = server;
		}

		public void decode(ByteBuffer buffer) {
			long chat = buffer.getLong();
			ChatMessage message = ChatMessage.read(buffer);
			
			Server.debug("New chat message was received");
			Server.debug(message.toString());
			

			m_server.getChat(chat).ifPresent((c) -> c.addMessage(message));
		}
	}
	
	//get a request to start a chat
	
	public static class NewChatIn extends InCommand {

		private Server m_server;

		public NewChatIn(Server server) {
			super(NEW_CHAT);
			this.m_server = server;
		}

		public void decode(ByteBuffer buffer) {
			String str = Utils.readUTF8(buffer);
			int clientCount = buffer.getInt();
			Set<Long> clients = new HashSet<>();
			for (int i = 0; i < clientCount; i++) {
				clients.add(buffer.getLong());
			}
			
			
			
			long date = buffer.getLong();
			ServerChat chat = new ServerChat(m_server, new Random().nextLong() + 1, str, date, clients,
					new HashSet<>());
			
			Server.debug("New chat requested by a client!");
			Server.debug(chat.toString());

			m_server.addChat(chat);

		}
	}
	
	//organize client chats and messages  
	
	public static class UpdateCommandIn extends InCommand {

		private Server m_server;

		public UpdateCommandIn(Server server) {
			super(UPDATE_DATA);
			this.m_server = server;
		}

		public void decode(ByteBuffer buffer) {
			long from = buffer.getLong();
			long client = buffer.getLong();
			Map<ServerChat, Set<ChatMessage>> msg_lst = new HashMap<>();
			List<ServerChat> chats = new LinkedList<>();
			Supplier<Stream<ServerChat>> stm = () -> m_server.getChats().stream();
			stm.get().filter(x -> x.getDate() >= from).forEach(c -> chats.add(c));
			
			stm.get().filter(x -> x.getClients().contains(client)).forEach(
				c -> msg_lst.put(c, c.fromDate(from)));
			
			for (ServerChat chat : chats) {
				if (chat.getClients().contains(client))
					sendChat(client, chat);
			}
			
			for (ServerChat chat : msg_lst.keySet()) {
				if (!msg_lst.get(chat).isEmpty()) {
					for (ChatMessage msg: msg_lst.get(chat)) {
						sendMessage(client, msg, chat);
					}
				}
			}
			
			
		}
	}
	
	//encode message
	
	public static class NewMessageOut extends OutCommand{
		private final ChatMessage m_msg;
		private final ServerChat m_chat;
		
		public NewMessageOut(ServerChat chat, ChatMessage msg) {
			super(NEW_MESSAGE);
			m_msg = msg;
			m_chat = chat;
		}
		
		public void encode(ByteBuffer buffer) {
			buffer.putLong(m_chat.getUUID());
			buffer.put(m_msg.encode().array());
		}
	}
	
	//encode the chat information 
	
	public static class NewChatOut extends OutCommand{
		private final ServerChat m_chat;
		
		public NewChatOut(ServerChat chat) {
			super(NEW_CHAT);
			m_chat = chat;
		}
		
		public void encode(ByteBuffer buffer) {
			Utils.writeUTF8(buffer, m_chat.getName());
			buffer.putInt(m_chat.getClients().size());
			m_chat.getClients().forEach((c)-> buffer.putLong(c));
			buffer.putLong(m_chat.getDate());
			buffer.putLong(m_chat.getUUID());
		}
	}
	
	//send the chat information to all the required clients
	
	public static final void sendChat(long client, ServerChat chat) {
		Commands.send(chat.getServer(), new NewChatOut(chat), Arrays.asList(client), 2048);
	}	
	
	//send message for clients
	
	public static final void sendMessage(long client, ChatMessage msg, ServerChat chat) {
		Commands.send(chat.getServer(), new NewMessageOut(chat, msg), Arrays.asList(client), 2048);
	}
	
	// send out info from the socket to the client
	public static final void send(Server server, OutCommand out, List<Long> clients, int capacity) {
		ByteBuffer buf = ByteBuffer.allocate(capacity);
		out.write(buf);
		server.sendBuffer(buf, clients);
	}

}
