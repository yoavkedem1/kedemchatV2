package kchat.client;

import java.io.ByteArrayOutputStream;
import java.util.function.Consumer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

//record the voice

public class VoiceRecorder {
	
	private boolean m_isRunning;
	
	private volatile ByteArrayOutputStream m_baos = new ByteArrayOutputStream();

	private Thread m_thread;
	private Thread m_callbackThread;
	
	private AudioFormat m_format;
	
	private Consumer<byte[]> m_callback;
	
	private boolean m_update;
	
	//TODO auto get format
	public VoiceRecorder(AudioFormat format, Consumer<byte[]> callback) {
		m_thread = new Thread(this::record);
		m_callbackThread = new Thread(this::callbackThread);
		m_callback = callback;
		m_format = format;
	}
	//start recording
	public void start() {
		if (!m_isRunning) {
			m_isRunning = true;
			m_thread.start();
			//m_callbackThread.start();
		}
	}
	//stop recording 
	public void end() {
		m_isRunning = false;
		m_thread.interrupt();
		m_callbackThread.interrupt();
	}
	
	// a callback for the recording of the voice
	protected void callbackThread() {
		while (!Thread.interrupted() && this.m_isRunning) {
			while (!m_update);
			byte[] arr;
			synchronized (m_baos) {
				arr = m_baos.toByteArray().clone();
				m_baos.reset();
				m_update = false;
			}
			m_callback.accept(arr);
			 
			
		}
	}
	//record the voice
	//TODO maybe merge with peek/pop
	public void record() {
		//AudioFormat format = new AudioFormat(44100, 16 , 1, true, true);
		TargetDataLine line = null;
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, m_format); 
		
		try {
		    line = (TargetDataLine) AudioSystem.getLine(info);
		    line.open(m_format);
		} catch (LineUnavailableException e) {}
		
		int readCount = 0;
		byte[] data = new byte[(int)line.getBufferSize()];

		line.start();
		
		while (m_isRunning && !Thread.interrupted()) {
		   readCount = line.read(data, 0, data.length);
		   synchronized (m_baos) {
			   m_baos.write(data, 0, readCount);
		       m_callback.accept(m_baos.toByteArray());

		   }
	       m_baos.reset();
		   m_update = true;
		}     
	}
	

}
