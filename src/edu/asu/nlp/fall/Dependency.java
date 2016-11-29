package edu.asu.nlp.fall;

public enum Dependency {
	subject, object, nulld;

	public static Dependency parseString(String rel) {
		if (rel.indexOf("subj") != -1) {
			return subject;
		} else if (rel.indexOf("obj") != -1) {
			return object;
		}
		return nulld;
	}
}