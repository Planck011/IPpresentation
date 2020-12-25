package test;

import java.util.*;

import jdd.bdd.BDD;


public class Node {
	private int id;
	public Set<String> port_list;
	public Set<String> connect;
	public Map<String, Set<Integer>> Pred;//port->predicate
	public Map<Integer, Set<String>> Port;//predicate->port
	public ArrayList<Rule> rules;
	private static int idcounter=0;
	private final int portcount=7;
	public String str;
	public Node()
	{
		this.id = ++idcounter;
		this.port_list = new HashSet<>();
		port_list.add("default");
		this.connect = new HashSet<>();
		this.str = "on";
	}
	public Node(String name,Set<String> p,Set<String> c,BDD bdd)
	{
		this.id = ++idcounter;
		this.port_list = new HashSet<>();
		this.connect = new HashSet<>();
		this.Port = new HashMap<>();
		this.Pred = new HashMap<>();
		this.rules = new ArrayList<>();
		port_list.addAll(p);
		connect.addAll(c);
		Pred.put("default", new HashSet<Integer>());
		Pred.get("default").add(1);
		Port.put(1, new HashSet<>());
		Port.get(1).add("default");
		for(String pp:p)
		{
			Pred.put(pp, new HashSet<>());
		}
		rules.add(new Rule("default","","",3,0,bdd));
		this.str = name;
	}
	public int getId() {
		return id;
	}
	public boolean findPort(String port) {
		if(port_list.contains(port))
			return true;
		return false;
	}
	public boolean findConnect(String port) {
		if(connect.contains(port))
			return true;
		return false;
	}
}
