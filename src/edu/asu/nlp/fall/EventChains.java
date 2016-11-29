package edu.asu.nlp.fall;

import java.util.ArrayList;

public class EventChains {
	private ArrayList<EventChain> eventChains;

	public ArrayList<EventChain> getEventChains() {
		return eventChains;
	}

	public void setEventChains(ArrayList<EventChain> eventChains) {
		this.eventChains = eventChains;
	}

	public EventChains() {
		eventChains = new ArrayList<>();
	}

	public void addEventChain(EventChain chain) {
		eventChains.add(chain);
	}
}
