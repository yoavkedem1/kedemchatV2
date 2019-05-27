package kchat.common;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Commands {
	
	private final static Map<Integer, InCommand> DECODERS = new HashMap<>();
	
	// a class for sending data
	
	public static abstract class OutCommand{
		private final int id;
		
		public OutCommand(int id) {
			this.id = id;
		}
		
		public int getId() {
			return id;
		}
		
		public void write(ByteBuffer buffer) {
			buffer.putInt(id);
			encode(buffer);
		}
		
		protected abstract void encode(ByteBuffer buffer);
		
	}
	
	// a class for receving data
	
	public static abstract class InCommand{
		private final int id;
		
		public InCommand(int id) {
			DECODERS.put(id, this);
			this.id = id;
		}
		public int getId() {
			return id;
		}
		
		public void read(ByteBuffer buffer) {
			decode(buffer);
		}
		protected abstract void decode(ByteBuffer buffer);
	}
	// each commends have a number, the number are set up here.
	public static final int NEW_MESSAGE = 1;
	public static final int NEW_CHAT = 2;
	public static final int VOICE_CHAT = 3;
	public static final int UPDATE_DATA = 4;
	public static final int CONNECTED_IN = 5;
	public static final int CLIENT_NAME = 6;
	public static final int CLIENT_IP = 7;
	public static final int REQUEST_CALL = 8;
	
	public static final void decode(ByteBuffer buffer) {
		exit:
		do {
			for (byte b : buffer.array()) {
				if (b != 0x00) break exit;
			}
			return;
		} while (false);
		
		int id = buffer.getInt();
		//System.out.println(DECODERS.get(id).getClass().getName());
		if (DECODERS.containsKey(id)) {
			DECODERS.get(id).decode(buffer);
		} else {
			System.err.println("Unknown Command With Buffer" + Arrays.toString(buffer.array()));
		}
	}
	
}