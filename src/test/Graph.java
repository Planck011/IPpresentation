package test;

import java.util.*;;

public class Graph {
	public Set<Node> V;
	public Set<Edge> E;
	public Map<Edge, Set<Integer>> A;
	public Graph(Set<Node> v,Set<Edge> e,Map<Edge, Set<Integer>> a) {
		this.A = a;
		this.E = e;
		this.V = v;
	}
}
