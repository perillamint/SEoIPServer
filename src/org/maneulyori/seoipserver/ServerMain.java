package org.maneulyori.seoipserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.smartcardio.*;

public class ServerMain {

	static TerminalFactory terminalfactory = TerminalFactory.getDefault();
	static CardTerminals cardTerminals = terminalfactory.terminals();

	public static void main(String[] args) throws IOException, CardException {

		Properties config = new java.util.Properties();
		config.load(new FileInputStream("serverConfig.cfg"));

		int port = Integer.parseInt(config.getProperty("port"));
		String keystorePath = config.getProperty("keystorePath");
		String keystoreKey = config.getProperty("keystoreKey");

		System.setProperty("javax.net.ssl.keyStore", keystorePath);
		System.setProperty("javax.net.ssl.keyStorePassword", keystoreKey);

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

			ConnectionHandler handler = new ConnectionHandler(config,
					clientSocket);
			new Thread(handler).start();
		}
	}
}
