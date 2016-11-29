package edu.asu.nlp.fall;

public class NodePair {
	private int key;
	private String headWord;
	private Node first;
	private Node second;

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}

	public Node getFirst() {
		return first;
	}

	public void setFirst(Node first) {
		this.first = first;
	}

	public Node getSecond() {
		return second;
	}

	public void setSecond(Node second) {
		this.second = second;
	}

	public NodePair() {

	}

	public String getHeadWord() {
		return headWord;
	}

	public void setHeadWord(String headWord) {
		this.headWord = headWord;
	}

	public NodePair(int key, Node first, Node second) {
		super();
		this.key = key;
		this.first = first;
		this.second = second;
	}

	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer();
		if (first != null) {
			ret.append("first Node : " + first.getVerb() + " : " + first.getDependency().name());
		}
		if (second != null) {
			ret.append(" second Node : " + second.getVerb() + " : " + second.getDependency().name());
		} else {
			ret.append(" second pair is no there for entity");
		}
		return ret.toString();
	}
}
