package test;

import java.util.*;
import java.util.Map.Entry;;

public class Graph {
	public Set<Node> V;
	public Set<Edge> E;
	public Map<Edge, Set<Integer>> A;
	public Graph(Set<Node> v,Set<Edge> e,Map<Edge, Set<Integer>> a) {
		this.A = a;
		this.E = e;
		this.V = v;
	}
	public void printGraph()
	{
		System.out.print("node:");
		for(Node v:V)
			System.out.print(v.str+" ");
		System.out.print("\nedge:");
//		for(Edge e:E)
//		{
//			System.out.print("("+e.from.str+","+e.to.str+","+e.fport+")");
//		}
		for(Entry<Edge, Set<Integer>> entry:A.entrySet())
		{
			System.out.print("("+entry.getKey().from.str+","+entry.getKey().to.str+","+entry.getKey().fport+")"+"{"+entry.getValue()+"}");
		}
		System.out.println();
	}
}
