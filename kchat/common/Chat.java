package kchat.common;

import static kchat.common.utils.CollectionUtils.*;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

//simple general commends for chat
@SuppressWarnings("serial")
public abstract class Chat implements Serializable{
	
	protected long m_uuid;
	protected String m_name;
	protected long m_date;
	protected Set<Long> m_clients;
	protected Set<ChatMessage> m_messages;
	
	public Chat(long uuid, String name, long date, Set<Long> clients, Set<ChatMessage> messages) {
		m_uuid = uuid;
		m_name = name;
		m_date = date;
		m_clients = clients;
		m_messages = messages;
	}
	
	public Chat(long uuid, String name, long date, Set<Long> clients) {
		this(uuid, name, date, clients, new HashSet<ChatMessage>());
	}

	public void addMessage(ChatMessage message) {
		if (message.getID() == -1) {
			message.setID(m_messages.size()+1);
		}
		m_messages.add(message);
	}
	//filter message by generic
	public Set<ChatMessage> filter(Predicate<? super ChatMessage> pred) {
		return m_messages.stream().filter(pred).collect(Collectors.toSet());
	}
	//filter message by date
	public Set<ChatMessage> fromDate(long date) {
		return filter((m) -> m.getDate() >= date);
	}
	//Message in specific time
	public Set<ChatMessage> inRange(long from, long to) {
		return filter((m) -> m.getDate() >= from && m.getDate() <= to);
	}
	//message for specific sender
	public Set<ChatMessage> fromSender(long sender) {
		return filter((m) -> m.getSenderID() == sender);
	}
	//newest message sent
	public Set<ChatMessage> peekNewest(int count) {
		return m_messages.stream().sorted().limit(count).collect(Collectors.toSet());
	}

	public String getName() {
		return m_name;
	}

	public long getDate() {
		return m_date;
	}

	public Set<Long> getClients() {
		return m_clients;
	}

	public Set<ChatMessage> getMessages() {
		return m_messages;
	}
	
	public boolean isInChat(long client) {
		return m_clients.contains(client);
	}
	
	public long getUUID() {
		return m_uuid;
	}
	
	public String toString() {
		String message = getClass().getSimpleName() + ":" + System.lineSeparator();
		for (Tuple<String, Object> tup : dataEntries()) {
			message += "\t - " + tup._1 + ": " + tup._2 + System.lineSeparator();
		}
		return message;
	}
	
	protected List<Tuple<String, Object>> dataEntries(){
		return new LinkedList<Tuple<String, Object>>() {{
			add(Tuple("Name",m_name));
			add(Tuple("Created", new Date(m_date)));
			add(Tuple("UUID", m_uuid));
			add(Tuple("Clients", m_clients));
			add(Tuple("Messages", m_messages));
		}};
	}
	
	
}
