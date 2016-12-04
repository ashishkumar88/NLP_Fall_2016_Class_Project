package edu.asu.nlp.fall;

import java.util.ArrayList;

public class EventChain {
	private ArrayList<Event> eventChain;

	public EventChain() {
		eventChain = new ArrayList<>();
	}

	public EventChain(ArrayList<Event> eventChain) {
		super();
		this.eventChain = eventChain;
	}

	public EventChain(VerbsDependenciesCount dependenciesCount) {
		eventChain = new ArrayList<>();
		eventChain.add(new Event(dependenciesCount.getVerb1().getSubject(), dependenciesCount.getVerb1().getVerb(),
				dependenciesCount.getVerb1().getObject()));
		eventChain.add(new Event(dependenciesCount.getVerb2().getSubject(), dependenciesCount.getVerb2().getVerb(),
				dependenciesCount.getVerb2().getObject()));
	}

	public ArrayList<Event> getEventChain() {
		return eventChain;
	}

	public void setEventChain(ArrayList<Event> eventChain) {
		this.eventChain = eventChain;
	}

	public void addEvent(Event event) {
		eventChain.add(event);
	}

}
