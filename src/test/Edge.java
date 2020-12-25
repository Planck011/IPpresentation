package test;
import java.util.*;
public class Edge {
	public Node from;
	public String fport;
	public Node to;
	public Set<Integer> predicate;
	public Edge(Node s1,Node s2,String p)
	{
		this.from=s1;
		this.to=s2;
		this.fport=p;
	}
}
