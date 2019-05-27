package kchat.client.test;

import java.net.UnknownHostException;

import kchat.server.Server;

public class TestServer {
	

	public static void main(String[] args) throws UnknownHostException, InterruptedException {
		//Segment 1
		Server.debug("Segment 1");
		Server server = new Server();
		
		
		//Segment 2
		Server.debug("Segment 2");
		Thread.sleep(500);
		
		
		
		//Segment 3
		Server.debug("Segment 3");
		Thread.sleep(1000);
		
		
		
	
		
	}
}
