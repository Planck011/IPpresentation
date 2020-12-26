package test;

import java.util.*;
import java.util.Map.Entry;;

public class Graph {
	public Set<Node> V;
	public Set<Edge> E;
	public Map<Edge, Set<Integer>> A;
	public Graph() {
		this(new HashSet<>(), new HashSet<>(), new HashMap<>());
	}
	public Graph(Set<Node> v,Set<Edge> e,Map<Edge, Set<Integer>> a) {
		this.A = a;
		this.E = e;
		this.V = v;
	}
	public void printGraph()
	{
		System.out.print("node:");
		for(Node v:V)
			System.out.print(v.name+" ");
		System.out.print("\nedge:");
		for(Entry<Edge, Set<Integer>> entry:A.entrySet())
		{
			System.out.print("("+entry.getKey().from.name+","+entry.getKey().to.name+","+entry.getKey().fport+")"+"{"+entry.getValue()+"}");
		}
		System.out.println();
	}
	public void clear()
	{
		this.A.clear();
		this.E.clear();
		this.V.clear();
	}
}
