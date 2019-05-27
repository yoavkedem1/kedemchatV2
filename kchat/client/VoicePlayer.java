package kchat.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;

//play the voice that was sent

public class VoicePlayer {
	
	
	private AudioFormat m_format;
	
	private boolean m_isRunning;
	
	private Thread m_thread;
	
	
	//TODO auto get format
	public VoicePlayer(AudioFormat format) {
		m_format = format;
	}
	
	//TODO Make a thread that runs this
	public void play(byte[] bytes) {
		Clip clip = null;
		try {
			clip = AudioSystem.getClip();
		} catch (LineUnavailableException e) {
			//REEEEEEEEEEEEEEE
			e.printStackTrace();
		}
		
		
		try {
			clip.open(new AudioInputStream(new ByteArrayInputStream(bytes), m_format, bytes.length));
		} catch (LineUnavailableException | IOException e) {
			//REEEEEEE
			e.printStackTrace();
		}
		clip.start();
			
	}
	
	
}
