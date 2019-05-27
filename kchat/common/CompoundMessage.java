package kchat.common;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static kchat.common.utils.CollectionUtils.*;

// a class for complicated message

@SuppressWarnings("serial")
public class CompoundMessage extends ChatMessage{

	private ChatMessage[] m_messages;
	public CompoundMessage(ChatMessage[] messages, long date, long sender, long id) {
		super(date, sender, id, ChatMessage.COMPOUND_MESSAGE);
		m_messages = messages;
	}

	@Override
	public ByteBuffer encode() {
		ByteBuffer enc = super.encode();
		
		List<ChatMessage> msgs = Arrays.asList(m_messages);
		Stream<ByteBuffer> encStream = msgs.stream().map(x -> x.encode());
		
		int capacity = 32 + encStream.reduce(0, (sum, buffer) -> 
			sum + buffer.capacity(), (sum1,sum2) -> sum1 + sum2);
		
		ByteBuffer buf = ByteBuffer.allocate(capacity);
		
		buf.put(enc.array());
		buf.putInt(m_messages.length);
		
		encStream.forEach((buffer) -> buf.put(buffer.array()));
		
		return buf;
	}
	
	public ChatMessage[] getMessages() {
		return m_messages;
	}

	public static final CompoundMessage decode(ByteBuffer buf) {
		long date = buf.getLong();
		long sender = buf.getLong();
		long id = buf.getLong();
		int amt = buf.getInt();
		ChatMessage[] arr = new ChatMessage[amt];
		for (int i = 0; i < amt; i++) {
			arr[i] = ChatMessage.read(buf);
		}
		return new CompoundMessage(arr, date, sender, id);
	}
	
	@Override
	protected List<Tuple<String, Object>> dataEntries(){
		List<Tuple<String, Object>> list = super.dataEntries();
		list.add(Tuple("Children", Arrays.asList(m_messages)));
		return list;
	}
	
}
