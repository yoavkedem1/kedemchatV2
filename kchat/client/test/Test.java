package kchat.client.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import kchat.client.AudioFormats;
import kchat.client.VoicePlayer;
import kchat.client.VoiceRecorder;

public class Test {

	private static boolean update = false;

	public static void main(String[] args) throws Throwable {
		DatagramSocket sockLeft = new DatagramSocket(1337);
		DatagramSocket sockRight = new DatagramSocket(1338);
		VoiceRecorder rek = new VoiceRecorder(AudioFormats._44100HZ_16BIT, (bytes) -> { 
			try {
				sockLeft.send(new DatagramPacket(bytes, bytes.length, InetAddress.getLocalHost(), 1338));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		);
		
		
		rek.start();

		byte[] arr = new byte[60000];
		
		VoicePlayer wmp/* Windows Media Player */ = new VoicePlayer(AudioFormats._44100HZ_16BIT);
		
		while (true) {
			System.out.println("hi");
			DatagramPacket pack = new DatagramPacket(arr, arr.length);
			sockRight.receive(pack);
			wmp.play(arr);
		}
	}
}
