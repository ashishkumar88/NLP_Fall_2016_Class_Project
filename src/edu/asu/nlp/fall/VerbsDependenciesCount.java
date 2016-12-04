package edu.asu.nlp.fall;

import java.util.Set;

public class VerbsDependenciesCount {
	private VerbDependency verb1;
	private VerbDependency verb2;
	private String coreferringEntity;
	private int pairCount;

	public VerbsDependenciesCount() {
	}

	public VerbsDependenciesCount(String verb12, String dependency1, int count1, String subject1, String object1,
			String verb22, String dependency2, int count2, String subject2, String object2, String headWord,
			int totalCount) {
		verb1 = new VerbDependency(subject1, verb12, object1, Dependency.parseString(dependency1), count1);
		verb2 = new VerbDependency(subject2, verb22, object2, Dependency.parseString(dependency2), count2);
		pairCount = totalCount;
		coreferringEntity = headWord;
	}

	public VerbDependency getVerb1() {
		return verb1;
	}

	public void setVerb1(VerbDependency verb1) {
		this.verb1 = verb1;
	}

	public VerbDependency getVerb2() {
		return verb2;
	}

	public void setVerb2(VerbDependency verb2) {
		this.verb2 = verb2;
	}

	public String getCoreferringEntity() {
		return coreferringEntity;
	}

	public void setCoreferringEntity(String coreferringEntity) {
		this.coreferringEntity = coreferringEntity;
	}

	public int getPairCount() {
		return pairCount;
	}

	public void setPairCount(int pairCount) {
		this.pairCount = pairCount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((coreferringEntity == null) ? 0 : coreferringEntity.hashCode());
		result = (prime * result) + pairCount;
		result = (prime * result) + ((verb1 == null) ? 0 : verb1.hashCode());
		result = (prime * result) + ((verb2 == null) ? 0 : verb2.hashCode());
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
		if (!(obj instanceof VerbsDependenciesCount)) {
			return false;
		}
		VerbsDependenciesCount other = (VerbsDependenciesCount) obj;
		if (coreferringEntity == null) {
			if (other.coreferringEntity != null) {
				return false;
			}
		} else if (!coreferringEntity.equals(other.coreferringEntity)) {
			return false;
		}
		if (pairCount != other.pairCount) {
			return false;
		}
		if (verb1 == null) {
			if (other.verb1 != null) {
				return false;
			}
		} else if (!verb1.equals(other.verb1)) {
			return false;
		}
		if (verb2 == null) {
			if (other.verb2 != null) {
				return false;
			}
		} else if (!verb2.equals(other.verb2)) {
			return false;
		}
		return true;
	}

	public static VerbsDependenciesCount createNew(NodePair nodePair, CreateDependencyParse createDependencyParse,
			Set<VerbDependency> allVerbDependency) {
		VerbsDependenciesCount dependenciesCount = new VerbsDependenciesCount();
		dependenciesCount.verb1 = new VerbDependency();
		dependenciesCount.verb1.setVerb(createDependencyParse.lemmatize(nodePair.getFirst().getVerb()));
		dependenciesCount.verb1.setSubject(nodePair.getFirst().getSubject());
		dependenciesCount.verb1.setObject(nodePair.getFirst().getObject());
		dependenciesCount.verb1.setDependency(nodePair.getFirst().getDependency());
		allVerbDependency.add(dependenciesCount.verb1);
		dependenciesCount.verb2 = new VerbDependency();
		dependenciesCount.verb2.setVerb(createDependencyParse.lemmatize(nodePair.getSecond().getVerb()));
		dependenciesCount.verb2.setSubject(nodePair.getSecond().getSubject());
		dependenciesCount.verb2.setObject(nodePair.getSecond().getObject());
		dependenciesCount.verb2.setDependency(nodePair.getSecond().getDependency());
		dependenciesCount.coreferringEntity = nodePair.getHeadWord();
		allVerbDependency.add(dependenciesCount.verb2);
		// System.out.println(dependenciesCount);
		return dependenciesCount;
	}

	@Override
	public String toString() {
		return "VerbsDependenciesCount [verb1=" + verb1 + ", verb2=" + verb2 + ", coreferringEntity="
				+ coreferringEntity + ", pairCount=" + pairCount + "]";
	}

}
