package org.maneulyori.seoipserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.xml.bind.DatatypeConverter;
import javax.smartcardio.*;

public class ServerMain {

	static TerminalFactory terminalfactory = TerminalFactory.getDefault();
	static CardTerminals cardTerminals = terminalfactory.terminals();

	public static String toHexString(byte[] array) {
	    return DatatypeConverter.printHexBinary(array);
	}

	public static byte[] toByteArray(String s) {
	    return DatatypeConverter.parseHexBinary(s);
	}
	
	public static void main(String[] args) throws IOException, CardException {

		int port = 1337;
		ServerSocket serverSocket = new ServerSocket(port);
		
		TerminalFactory factory = TerminalFactory.getDefault();
		CardManager cardmanager = new CardManager();
		cardmanager.setCardTerminals(factory.terminals());
		
		
		while(true) {
			Socket clientSocket = serverSocket.accept();
			
			ConnectionHandler handler = new ConnectionHandler(clientSocket);
			new Thread(handler).start();
		}
	}
}
