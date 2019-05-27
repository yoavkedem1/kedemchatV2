package kchat.common;

import static kchat.common.utils.CollectionUtils.*;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


@SuppressWarnings("serial")
public abstract class ChatMessage implements Comparable<ChatMessage>, Serializable{
	
	public static final int STRING_MESSAGE = 0x00;
	public static final int COMPOUND_MESSAGE = 0x01;
	

	private final long m_date;
	private final long m_sender;
	private long m_id;
	
	private final int MSG_ID;
	
	public ChatMessage(long date, long sender, long id, int msg_id) {
		m_date = date;
		m_sender = sender;
		m_id = id;
		MSG_ID = msg_id;
	}
	//buffer that is sent in every message
	public ByteBuffer encode() {
		ByteBuffer buf = ByteBuffer.allocate(28);
		buf.putInt(MSG_ID);
		buf.putLong(m_date);
		buf.putLong(m_sender);
		buf.putLong(m_id);
		return buf;
	}

	@Override
	public int compareTo(ChatMessage o) {
		return (int) Math.signum(m_date - o.m_date); 
	}
	
	public long getDate() {
		return m_date;
	}
	
	public long getID() {
		return m_id;
	}
	
	public void setID(long id) {
		m_id = id;
	}
	
	public long getSenderID() {
		return m_sender;
	}
	
	public int getMessageId() {
		return MSG_ID;
	}
	//decode the message from byte to string
	public static final ChatMessage read(ByteBuffer buffer) {
		int type = buffer.getInt();
		switch (type) {
		case STRING_MESSAGE:
			StringMessage str = StringMessage.decode(buffer);
			return str;
		case COMPOUND_MESSAGE:
			CompoundMessage cmp = CompoundMessage.decode(buffer);
			return cmp;
		default:
			return null;
		}
		
	}
	
	//build the message 
	public String toString() {
		String message = getClass().getSimpleName() + ":" + System.lineSeparator();
		for (Tuple<String, Object> tup : dataEntries()) {
			message += "\t - " + tup._1 + ": " + tup._2 + System.lineSeparator();
		}
		return message;
	}
	
	protected List<Tuple<String, Object>> dataEntries(){
		return new LinkedList<Tuple<String, Object>>() {{
			add(Tuple("Msg Type", MSG_ID));
			add(Tuple("Sender", m_sender));
			add(Tuple("Id", m_id));
			add(Tuple("Sent", new Date(m_date)));
		}};
	}
	

	
	
}
