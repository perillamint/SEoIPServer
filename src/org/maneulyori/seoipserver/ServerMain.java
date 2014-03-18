package org.maneulyori.seoipserver;

import java.io.IOException;
import java.net.Socket;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.smartcardio.*;

public class ServerMain {

	static TerminalFactory terminalfactory = TerminalFactory.getDefault();
	static CardTerminals cardTerminals = terminalfactory.terminals();

	public static void main(String[] args) throws IOException, CardException {

		int port = 1337;

		System.setProperty("javax.net.ssl.keyStore", "./sslkeystore.jks");
		System.setProperty("javax.net.ssl.keyStorePassword", "111111");

		SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory
				.getDefault();

		SSLServerSocket serverSocket = (SSLServerSocket) sslserversocketfactory
				.createServerSocket(port);

		serverSocket.setEnabledProtocols(new String[] { "TLSv1", "TLSv1.1",
				"TLSv1.2", "SSLv3" });

		TerminalFactory factory = TerminalFactory.getDefault();
		CardManager cardmanager = new CardManager();
		cardmanager.setCardTerminals(factory.terminals());

		while (true) {
			Socket clientSocket = serverSocket.accept();

			ConnectionHandler handler = new ConnectionHandler(clientSocket);
			new Thread(handler).start();
		}
	}
}
