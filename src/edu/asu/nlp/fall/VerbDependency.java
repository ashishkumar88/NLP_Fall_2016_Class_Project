package edu.asu.nlp.fall;

public class VerbDependency {

	private String subject;
	private String verb;
	private String object;
	private Dependency dependency;
	private int count;

	public VerbDependency() {

	}

	public VerbDependency(String verb, Dependency dependency, int count) {
		super();
		this.verb = verb;
		this.dependency = dependency;
		this.count = count;
	}

	public VerbDependency(String subject, String verb, String object, Dependency dependency, int count) {
		super();
		this.subject = subject;
		this.verb = verb;
		this.object = object;
		this.dependency = dependency;
		this.count = count;
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

	public String getVerb() {
		return verb;
	}

	public void setVerb(String verb) {
		this.verb = verb;
	}

	public Dependency getDependency() {
		return dependency;
	}

	public void setDependency(Dependency dependency) {
		this.dependency = dependency;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + count;
		result = (prime * result) + ((dependency == null) ? 0 : dependency.hashCode());
		result = (prime * result) + ((verb == null) ? 0 : verb.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof VerbDependency)) {
			return false;
		}
		VerbDependency other = (VerbDependency) obj;
		if (count != other.count) {
			return false;
		}
		if (dependency != other.dependency) {
			return false;
		}
		if (verb == null) {
			if (other.verb != null) {
				return false;
			}
		} else if (!verb.equals(other.verb)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "VerbDependency [subject=" + subject + ", verb=" + verb + ", object=" + object + ", dependency="
				+ dependency + ", count=" + count + "]";
	}

}
