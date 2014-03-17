package org.maneulyori.seoipserver;

import java.util.List;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;

public class CardManager {
	private static CardManager[] cardlock;
	private static CardTerminals cardTerminals;
	private static List<CardTerminal> cardTerminalList;

	public CardManager() {
		// Do nothing
	}

	public boolean setCardTerminals(CardTerminals cardTerminals)
			throws CardException {
		if (CardManager.cardTerminals != null)
			return false;

		CardManager.cardTerminals = cardTerminals;
		CardManager.cardTerminalList = cardTerminals.list();
		CardManager.cardlock = new CardManager[CardManager.cardTerminalList
				.size()];

		return true;
	}
	
	public String listReader() {
		StringBuilder sb = new StringBuilder();
		
		int i = 0;
		for(CardTerminal terminal : cardTerminalList)
		{
			sb.append("LIST " + i + " " + terminal.getName() + "\n");
			i++;
		}
		
		sb.append("ENDLIST");
		
		return sb.toString();
	}

	public CardTerminal acquireCard(int idx) throws CardException {
		try {
			if (CardManager.cardlock[idx] != this
					&& CardManager.cardlock[idx] != null) {
				return null;
			}

			CardManager.cardlock[idx] = this;

			CardTerminal terminal = cardTerminalList.get(idx);

			return terminal;
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

	public boolean freeLock(int idx) {
		if (CardManager.cardlock[idx] == this) {
			CardManager.cardlock[idx] = null;
			return true;
		}

		return false;

	}
}
