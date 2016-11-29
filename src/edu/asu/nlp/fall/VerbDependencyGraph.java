package edu.asu.nlp.fall;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VerbDependencyGraph {

	private Set<VerbDependency> nodes;

	@SuppressWarnings("rawtypes")
	private ArrayList<Edge> edges;
	private ArrayList<Map<String, ArrayList<VerbDependency>>> headWordDependencies;
	private ArrayList<VerbsDependenciesCount> dependenciesCounts;
	private Integer sumsC;

	private Map<VerbDependency, Integer> sumsCDependency;
	private Integer sumsCDependencies;

	public VerbDependencyGraph() {
		nodes = new HashSet<>();
		edges = new ArrayList<>();
		headWordDependencies = new ArrayList<>();

		sumsC = 0;
		sumsCDependencies = Integer.MIN_VALUE;
		sumsCDependency = new HashMap<>();
	}

	public Set<VerbDependency> getNodes() {
		return nodes;
	}

	public void setNodes(Set<VerbDependency> nodes) {
		this.nodes = nodes;
	}

	public ArrayList<Edge> getEdges() {
		return edges;
	}

	public void setEdges(ArrayList<Edge> edges) {
		this.edges = edges;
	}

	public ArrayList<Map<String, ArrayList<VerbDependency>>> getHeadWordDependencies() {
		return headWordDependencies;
	}

	public void setHeadWordDependencies(ArrayList<Map<String, ArrayList<VerbDependency>>> headWordDependencies) {
		this.headWordDependencies = headWordDependencies;
	}

	public ArrayList<VerbsDependenciesCount> getDependenciesCounts() {
		return dependenciesCounts;
	}

	public void setDependenciesCounts(ArrayList<VerbsDependenciesCount> dependenciesCounts) {
		this.dependenciesCounts = dependenciesCounts;
	}

	public void publishDependencies(ArrayList<VerbsDependenciesCount> allEventPairs,
			Set<VerbDependency> allVerbDependency) {
		dependenciesCounts = allEventPairs;
		Set<VerbDependency> temp = new HashSet<>();
		for (VerbDependency dependency : allVerbDependency) {
			int count = 0;
			for (VerbsDependenciesCount dependenciesCount : allEventPairs) {
				if (dependency.equals(dependenciesCount.getVerb1())
						|| dependency.equals(dependenciesCount.getVerb2())) {
					count++;
				}
			}
			dependency.setCount(count);
			temp.add(dependency);
		}
		nodes = temp;
	}

	public void createGraph() {
		for (VerbsDependenciesCount dependenciesCount : dependenciesCounts) {
			System.out.println(dependenciesCount);
			edges.add(new Edge<VerbDependency>(dependenciesCount.getVerb1(), dependenciesCount.getVerb2(),
					calculatePMI(dependenciesCount.getVerb1(), dependenciesCount.getVerb2())));
		}
	}

	private double calculatePMI(VerbDependency w, VerbDependency v) {
		return Math.log(P(w, v)) / Math.log(P(w) * P(v));
	}

	private double P(VerbDependency w, VerbDependency v) {
		int sumCWV = Integer.MIN_VALUE;

		for (VerbsDependenciesCount dependenciesCount : dependenciesCounts) {
			if (dependenciesCount.getVerb1().equals(w) && dependenciesCount.getVerb2().equals(v)) {
				sumCWV = dependenciesCount.getPairCount();
			}
			sumsC = sumsC + dependenciesCount.getPairCount();
		}
		return (sumCWV * 1.0) / sumsC;
	}

	private double P(VerbDependency v) {
		Integer sumCV = Integer.MIN_VALUE;
		Integer sumCVTotal = Integer.MIN_VALUE;

		if (sumsCDependency.get(v) != null) {
			sumCV = sumsCDependency.get(v);
		} else {
			sumCV = v.getCount();
			sumsCDependency.put(v, sumCV);
		}

		if (sumsCDependencies != Integer.MIN_VALUE) {
			sumCVTotal = sumsCDependencies;
		} else {
			sumCVTotal = 0;
			for (VerbDependency dependency : nodes) {
				sumCVTotal = sumCVTotal + dependency.getCount();
			}
			sumsCDependencies = sumCVTotal;
		}
		return (sumCV * 1.0) / sumCVTotal;
	}

	public EventChains getEventChains() {
		EventChains eventChains = new EventChains();
		for (VerbsDependenciesCount dependenciesCount : dependenciesCounts) {
			eventChains.addEventChain(new EventChain(dependenciesCount));
		}
		return eventChains;
	}
}

@SuppressWarnings("hiding")
class Edge<VerbDependencyGraph> {
	private final VerbDependencyGraph x;
	private final VerbDependencyGraph y;
	private double cost;

	public Edge(VerbDependencyGraph x, VerbDependencyGraph y, double cost) {
		this.x = x;
		this.cost = cost;
		this.y = y;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public VerbDependencyGraph getX() {
		return x;
	}

	public VerbDependencyGraph getY() {
		return y;
	}
}
