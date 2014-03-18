package org.maneulyori.seoipserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLException;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

public class ConnectionHandler implements Runnable {

	private Socket socket;
	private boolean terminate = false;
	private CardTerminal cardTerminal;
	private Card card;
	private int cardIdx;
	private CardManager cardlock = new CardManager();
	private InputStream socketInputStream;
	private OutputStream socketOutputStream;
	private String remoteAddr;
	private boolean auth = false;
	private String key = "changethis";

	public ConnectionHandler(Socket socket) throws IOException {
		remoteAddr = socket.getRemoteSocketAddress().toString();
		System.out.println("Connection from " + remoteAddr);
		socket.setSoTimeout(10000);

		this.socket = socket;
		this.socketInputStream = socket.getInputStream();
		this.socketOutputStream = socket.getOutputStream();
	}

	public void Terminate(boolean terminate) {
		this.terminate = terminate;
	}

	@Override
	public void run() {
		try {
			BufferedReader socketReader = new BufferedReader(
					new InputStreamReader(socketInputStream));

			PrintStream socketPrintStream = new PrintStream(socketOutputStream);

			while (!terminate) {
				String command;

				try {
					command = socketReader.readLine();
				} catch (SSLException e) {
					System.out.println("SSL Exception.");
					return;
				} catch (SocketTimeoutException e) {
					System.out.println("Connection from " + remoteAddr
							+ " timed out.");

					socket.close();
					cardlock.freeLock(cardIdx);
					return;
				}

				if (command == null) {
					System.out.println("Connection from " + remoteAddr
							+ " closed.");
					socket.close();
					cardlock.freeLock(cardIdx);
					break;
				}

				String[] splittedCommand = command.split(" ");

				if (auth == false) {
					if (splittedCommand.length == 2
							&& splittedCommand[1].equals(key)) {
						auth = true;
						socketPrintStream.println("OK");
					} else {
						socketPrintStream.println("ERROR AUTHFAIL");
						socket.close();
						cardlock.freeLock(cardIdx);
						System.out.println("Auth from " + remoteAddr + " failed.");
						System.out.println("Connection from " + remoteAddr
								+ " closed.");
						break;
					}
				} else {
					socketPrintStream.println(doCommand(splittedCommand));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (CardException e) {
			e.printStackTrace();
		}
	}

	private String doCommand(String[] splittedCommand) throws CardException {
		switch (splittedCommand[0]) {
		case "LIST":
			return cardlock.listReader();
		case "LOCK":
			try {
				if (splittedCommand[1] != null && cardTerminal == null) {

					try {
						cardIdx = Integer.parseInt(splittedCommand[1], 10);
					} catch (NumberFormatException e) {
						return "ERROR INVALIDFORMAT";
					}
					cardTerminal = cardlock.acquireCard(cardIdx);

					if (cardTerminal == null) {
						return "ERROR NOCARDRDR";
					}

					cardTerminal.waitForCardPresent(1000);

					if (!cardTerminal.isCardPresent())
						return "ERROR NOCARD";

					card = cardTerminal.connect("*");
					return "OK";
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				return "ERROR NOARG";
			}
			return "ERROR ALREADYLOCK";
		case "UNLOCK":
			if (cardTerminal != null)
				cardTerminal = null;
			if (card != null)
				card = null;

			String retstr = cardlock.freeLock(cardIdx) ? "OK" : "ERROR NOLOCK";
			return retstr;
		case "APDU":
			if (card == null)
				return "ERROR NOCARD";

			CardChannel cardChannel = card.getBasicChannel();

			byte[] apdu = new byte[splittedCommand.length - 1];

			try {
				for (int i = 1; i < splittedCommand.length; i++) {
					apdu[i - 1] = (byte) Integer.parseInt(splittedCommand[i],
							16);
				}
			} catch (NumberFormatException e) {
				return "ERROR ILLEGALFORMAT";
			}

			ResponseAPDU response;

			try {
				response = cardChannel.transmit(new CommandAPDU(apdu));
			} catch (IllegalArgumentException e) {
				return "ERROR ILLEGALARG";
			}

			byte[] resp = response.getBytes();
			StringBuilder retval = new StringBuilder();
			retval.append("OK\nAPDU");

			for (int i = 0; i < resp.length; i++) {
				retval.append(" " + String.format("%02X", resp[i] & 0xFF));
			}

			return retval.toString();
		case "PING":
			return "PONG";
		case "PONG":
			return "PING";
		default:
			return "ERROR INVALIDCMD";
		}
	}

}
