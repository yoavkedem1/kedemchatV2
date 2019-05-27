package kchat.client.test;

import java.awt.Color;
import java.awt.Font;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import kchat.client.Client;
import kchat.client.ClientChat;
import kchat.client.Commands;
import kchat.common.ClientData;
import kchat.common.StringMessage;

public class TestClient {
	
	public static boolean bindClient(Client client) {
		try {
			client.bind( InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			//e.printStackTrace();
			return false;
		}
		return true;
	}
	
	

	public static void main(String[] args) throws UnknownHostException, InterruptedException {
		
		//<Segment 1>
		Client.debug("Segment 1");
		Client client = new Client(new ClientData(3, "Jeff"));
		
		//__Bind Client__//
		boolean bound = bindClient(client);
		
		if (!bound) 
			Client.debug("Client failed to bind to server");
		else 
			Client.debug("Client succesfully binded to server!");
		
		
		//__Start UI__//
		client.startUI();
		
		
		//__Prepare new chat packet__//
		Set<Long> set = new HashSet<Long>() {{
			add(client.getUUID());
		}};
		
		Commands.send(client, new Commands.NewChat("My", set, System.currentTimeMillis()));
		
		Thread.sleep(1500l);
		
		
		
		//<Segment 2> 
		Client.debug("Segment 2");
		
		ClientChat chat = client.getChats().get(0);
		
		
		chat.sendMessage(new StringMessage("Arazim", Color.red, Font.PLAIN, System.currentTimeMillis(), client.getUUID(), -1));
		
		Thread.sleep(500l);
		
	//	ui.setManager(new ChatMenuUIManager(client));
			
		
		//<Segment 3>
		Client.debug("Segment 3");
		Client.debug(chat.getMessages().toString());
		client.getAllWindows().forEach(x -> x.open());
		Thread.sleep(500l);
	
		
	}
}
