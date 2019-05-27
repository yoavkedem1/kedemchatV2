package kchat.server;

import static kchat.common.utils.CollectionUtils.*;

import java.util.List;
import java.util.Set;

import kchat.common.Chat;
import kchat.common.ChatMessage;

@SuppressWarnings("serial")
public class ServerChat extends Chat{
	private final transient Server m_server;
	
	public ServerChat(Server server, long uuid, String name, long date,
			Set<Long> clients, Set<ChatMessage> messages) {
		super(uuid, name, date, clients, messages);
		m_server = server;
	}
	
	public Server getServer() {
		return m_server;
	}
	
	@Override
	protected List<Tuple<String, Object>> dataEntries(){
		List<Tuple<String, Object>> list = super.dataEntries();
		list.add(Tuple("Server", m_server));
		return list;
	}

}
