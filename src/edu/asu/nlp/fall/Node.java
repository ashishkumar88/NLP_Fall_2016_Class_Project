package edu.asu.nlp.fall;

public class Node {
	private String subject;
	private String verb;
	private String object;

	private Dependency dependency;

	public String getVerb() {
		return verb;
	}

	public void setVerb(String verb) {
		this.verb = verb;
	}

	public Dependency getDependency() {
		return dependency;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getObject() {
		return object;
	}

	public void setObject(String object) {
		this.object = object;
	}

	public void setDependency(Dependency dependency) {
		this.dependency = dependency;
	}

	public Node(String verb, Dependency dependency) {
		super();
		this.verb = verb;
		this.dependency = dependency;
		subject = null;
		object = null;
	}

	public Node() {

	}
}
