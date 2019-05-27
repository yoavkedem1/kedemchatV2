package kchat.common;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class Utils {
	
	public static final boolean DEBUG_ENABLED = true;
	
	public static StackTraceElement stackTrace(int depth) {
		StackTraceElement[] arr = Thread.currentThread().getStackTrace();
		return arr[depth];
	}
	// Encrypt string to UTF8
	public static final void writeUTF8(ByteBuffer buffer, String string) {
		byte[] bytes;
		try {
			bytes = string.getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Dafuck?");
		}
		buffer.putInt(bytes.length);
		buffer.put(bytes);
	}
	
	
	//decrypt byte to string with UTF8
	public static final String readUTF8(ByteBuffer buffer) {
		int length = buffer.getInt();
		byte[] bytes = new byte[length];
		buffer.get(bytes);
		try {
			return new String(bytes, "UTF8");
		} catch (UnsupportedEncodingException e) {
			System.err.println("Sometimes exceptions are used as bullshit, seriously though");
		}
		return "Somehow the code has crashed, fuck you!" + null;
	}

	public static final InetAddress readIP(ByteBuffer buf) {
		try {
			return InetAddress.getByAddress(new byte[] {buf.get(), buf.get(), buf.get(), buf.get()});
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void writeIP(ByteBuffer buf, InetAddress m_ip) {
		buf.put(m_ip.getAddress());
	}

	
}
