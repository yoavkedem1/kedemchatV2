package kchat.common;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ClientData implements Serializable {

	private final long m_uuid;
	private final String m_name;

	public ClientData(long uuid, String name) {
		m_uuid = uuid;
		m_name = name;
	}

	public long getUUID() {
		return m_uuid;
	}

	public String getName() {
		return m_name;
	}
}
