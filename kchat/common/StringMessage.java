package kchat.common;

import static kchat.common.utils.CollectionUtils.*;

import java.awt.Color;
import java.nio.ByteBuffer;

import java.util.List;

// a string message class

@SuppressWarnings("serial")
public class StringMessage extends ChatMessage{

	private String m_text;
	private Color m_color;
	private int m_flags;
	public StringMessage(String text, Color color, int flags, long date, long sender, long id) {
		super(date, sender, id, ChatMessage.STRING_MESSAGE);
		m_text = text;
		m_color = color;
		m_flags = flags;
	}

	//encoding the message
	
	@Override
	public ByteBuffer encode() {
		int strLen = m_text.getBytes().length;

		ByteBuffer enc = super.encode();
		
		// 16 for color, 4 for string length, 4 for flags, rest for text
		ByteBuffer buf = ByteBuffer.allocate(52 + strLen);
		
		buf.put(enc.array());
		buf.putInt(m_flags);
		buf.putInt(m_color.getRGB());
		Utils.writeUTF8(buf, m_text);

		return buf;
	}
	
	public String getMessage() {
		return m_text;
	}
	
	public Color getColor() {
		return m_color;
	}
	
	public int getFlags() {
		return m_flags;
	}
	public static final StringMessage decode(ByteBuffer buf) {
		long date = buf.getLong();
		long sender = buf.getLong();
		System.out.println("Sender = " + sender);
		long id = buf.getLong();
		int flags = buf.getInt();
		Color color = new Color(buf.getInt(), true);
		String text = Utils.readUTF8(buf);
		return new StringMessage(text, color, flags, date, sender, id);
	}
	
	@Override
	protected List<Tuple<String, Object>> dataEntries(){
		List<Tuple<String, Object>> list = super.dataEntries();
		list.add(Tuple("Text", m_text));
		list.add(Tuple("Color", m_color));
		list.add(Tuple("Flag Mask", m_flags));
		return list;
	}
	
}
