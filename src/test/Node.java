package test;

import java.util.*;
import java.util.Map.Entry;

import jdd.bdd.BDD;


/**
 * @author puyun
 *
 */
/**
 * @author puyun
 *
 */
public class Node {
	private int id;
	public Set<String> interfaces;
	public Set<String> port_list;
	public Set<String> connect;
	public Map<String, Set<Integer>> Pred;//port->predicate
	public Map<Integer, Set<String>> Port;//predicate->port
	public Map<String, Set<Integer>> Pred_interface;
	public Map<Integer, Set<String>> Port_interface;
	public Set<Rule> rules;
	public Map<String, Set<String>> portMap;//interface->{port}
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
		this.interfaces = new HashSet<>();
		this.Port = new HashMap<>();
		this.Pred = new HashMap<>();
		this.Port_interface = new HashMap<>();
		this.Pred_interface = new HashMap<>();
		this.rules = new HashSet<>();
		this.elements = new ArrayList<>();
		creatFWElement();
//		port_list.add("default");
		
	}
	/**
	 * @param name node name
	 * @param inter node interfaces
	 * @param vlans node's vlans
	 * @param acls node's acl_group
	 * @param map node's map interface->port
	 */
	public Node(String name,Set<String> inter,Set<String> vlans,Set<String> acls,Map<String, Set<String>> map,BDD bdd)
	{
		this.id = ++idcounter;
		this.name = name;
		this.interfaces = new HashSet<>(inter);
		this.port_list = new HashSet<>();
		this.connect = new HashSet<>();
		this.Port = new HashMap<>();
		this.Pred = new HashMap<>();
		this.Port_interface = new HashMap<>();
		this.Pred_interface = new HashMap<>();
		this.rules = new HashSet<>();
		this.elements = new ArrayList<>();
		creatFWElement(name,vlans);
		for(String acl:acls)
		{
			creatACLElement(acl);
		}
		this.portMap = new HashMap<>(map);
		rules.add(new Rule(name,"default","","",3,0,bdd));
	}
	public Node(String name,Set<String> p,Set<String> c,BDD bdd)
	{
		this.id = ++idcounter;
		this.name = name;
		this.port_list = new HashSet<>();
		this.connect = new HashSet<>();
		this.Port = new HashMap<>();
		this.Pred = new HashMap<>();
		this.Port_interface = new HashMap<>();
		this.Pred_interface = new HashMap<>();
		this.rules = new HashSet<>();
		this.elements = new ArrayList<>();
		this.interfaces = new HashSet<>();
		interfaces.addAll(p);
		connect.addAll(c);
		Pred.put("default", new HashSet<Integer>());
		Pred.get("default").add(1);
		Port.put(1, new HashSet<>());
		Port.get(1).add("default");
		for(String pp:interfaces)
		{
			Pred_interface.put(pp, new HashSet<>());
		}
		rules.add(new Rule(name,"default","","",3,0,bdd));
	}
	public void updateInterfaces() {
		for(Entry<String, Set<String>> en:portMap.entrySet())
		{
			for(String v:en.getValue())
			{
				if(Pred.get(v)!=null)
				{
					Pred_interface.get(en.getKey()).addAll(Pred.get(v));
					for(Integer p:Pred.get(v))
					{
						Set<String> s1 = new HashSet<>();
						if(Port_interface.get(p)==null)
							Port_interface.put(p, s1);
						Port_interface.get(p).add(en.getKey());
					}
					
				}
			}
		}
	}
	public void creatFWElement() {
		Element e = new FWelement();
		elements.add(e);
		port_list.addAll(e.getPort_list());
	}
	
	/**
	 * @param sn 节点名
	 * @param p vlan端口集合
	 */
	public void creatFWElement(String sn, Set<String> p) {
		Element e = new FWelement(sn,p);
		elements.add(e);
		port_list.addAll(e.getPort_list());
		for (String v: port_list) {
			Pred.put(v, new HashSet<>());
		}
		Pred.get("defualt").add(1);
		Port.put(1, new HashSet<>());
		Port.get(1).add("default");
	}
	public void creatACLElement(String sn) {
		Element e = new ACLelement(sn);
		elements.add(e);
		port_list.addAll(e.getPort_list());
		for (String v: port_list) {//modify
			Pred.put(v, new HashSet<>());
		}
	}
	public int getId() {
		return id;
	}
	public boolean findPort(String port) {
		if(port_list.contains(port))
			return true;
		return false;
	}
	public boolean findInterface(String port) {
		if(interfaces.contains(port))
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
class Node_1<A> {
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
	public Node_1()
	{
		this.id = ++idcounter;
		this.port_list = new HashSet<>();
		this.connect = new HashSet<>();
		this.name = (A) "on";
		port_list.add((A) "default");
		
	}
	public Node_1(A name)
	{
		this.id = ++idcounter;
		this.port_list = new HashSet<>();
		this.connect = new HashSet<>();
		this.name = name;
		
	}
	@SuppressWarnings("unchecked")
	public Node_1(A name,Set<A> p,Set<A> c,BDD bdd)
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
