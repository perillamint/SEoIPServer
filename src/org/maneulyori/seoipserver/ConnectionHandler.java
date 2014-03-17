package org.maneulyori.seoipserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

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

	public ConnectionHandler(Socket socket) throws IOException {
		this.socket = socket;
		this.socketInputStream = socket.getInputStream();
		this.socketOutputStream = socket.getOutputStream();
	}

	public void Terminate(boolean terminate) {
		this.terminate = terminate;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
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
				}

				if (command == null) {
					socket.close();
					cardlock.freeLock(cardIdx);
					break;
				}

				String[] splittedCommand = command.split(" ");

				socketPrintStream.println(doCommand(splittedCommand));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (CardException e) {
			// TODO Auto-generated catch block
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
		default:
			return "ERROR INVALIDCMD";
		}
	}

}
