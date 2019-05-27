package kchat.client;

import java.util.List;
import java.util.Set;

import kchat.common.Chat;
import kchat.common.ChatMessage;
import static kchat.common.utils.CollectionUtils.*;

@SuppressWarnings("serial")
public class ClientChat extends Chat{
	
	private final transient Client m_client;
	
	public ClientChat(Client client, long uuid, String name, long date,
			Set<Long> clients, Set<ChatMessage> messages) {
		super(uuid, name, date, clients, messages);
		m_client = client;
	}
	
	public Client getClient() {
		return m_client;
	}
	// send the meesege 
	public void sendMessage(ChatMessage message) {
		Commands.send(m_client, new Commands.NewMessage(this, message));
	}
	
	@Override
	protected List<Tuple<String, Object>> dataEntries(){
		List<Tuple<String, Object>> list = super.dataEntries();
		list.add(Tuple("Client", m_client));
		return list;
	}
}
