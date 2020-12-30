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
	public ArrayList<Element> elements;
	private static int idcounter=0;
	private final int portcount=7;
	private String model;
	private Int_Type Int_type;
	private Layer layer;
	public String name;
	public Node()
	{
		this.id = ++idcounter;
		this.port_list = new HashSet<>();
		this.connect = new HashSet<>();
		this.name = "on";
		port_list.add("default");
		
	}
	public Node(String name,Set<String> p,Set<String> c,BDD bdd)
	{
		this.id = ++idcounter;
		this.name = name;
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
	}
	public void creatElement() {
		
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
	public String getType() {
		return this.Int_type.toString();
	}
	public String getLayer() {
		return this.layer.toString();
	}
	public String getModel() {
		return this.model;
	}
}
class Node<A> {
	private int id;
	public Set<A> port_list;
	public Set<A> connect;
	public Map<A, Set<Integer>> Pred;//port->predicate
	public Map<Integer, Set<A>> Port;//predicate->port
	public ArrayList<Rule> rules;
	public ArrayList<Element> elements;
	private static int idcounter=0;
	private final int portcount=7;
	private String model;
	private Int_Type Int_type;
	private Layer layer;
	public A name;
	@SuppressWarnings("unchecked")
	public Node()
	{
		this.id = ++idcounter;
		this.port_list = new HashSet<>();
		this.connect = new HashSet<>();
		this.name = (A) "on";
		port_list.add((A) "default");
		
	}
	public Node(A name)
	{
		this.id = ++idcounter;
		this.port_list = new HashSet<>();
		this.connect = new HashSet<>();
		this.name = name;
		
	}
	@SuppressWarnings("unchecked")
	public Node(A name,Set<A> p,Set<A> c,BDD bdd)
	{
		this.id = ++idcounter;
		this.name = name;
		this.port_list = new HashSet<>();
		this.connect = new HashSet<>();
		this.Port = new HashMap<>();
		this.Pred = new HashMap<>();
		this.rules = new ArrayList<>();
		port_list.addAll(p);
		connect.addAll(c);
		Pred.put((A) "default", new HashSet<Integer>());
		Pred.get("default").add(1);
		Port.put(1, new HashSet<>());
		Port.get(1).add((A) "default");
		for(A pp:p)
		{
			Pred.put(pp, new HashSet<>());
		}
		rules.add(new Rule("default","","",3,0,bdd));
	}
	public void creatElement() {
		
	}
	public int getId() {
		return id;
	}
	public boolean findPort(A port) {
		if(port_list.contains(port))
			return true;
		return false;
	}
	public boolean findConnect(A port) {
		if(connect.contains(port))
			return true;
		return false;
	}
	public String getType() {
		return this.Int_type.toString();
	}
	public String getLayer() {
		return this.layer.toString();
	}
	public String getModel() {
		return this.model;
	}
}
