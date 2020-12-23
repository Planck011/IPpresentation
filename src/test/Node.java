package test;

import java.util.*;


public class Node {
	private int id;
	public Set<String> port_list;
	public Set<String> connect;
	private static int idcounter=0;
	private final int portcount=7;
	public String str = "default";
	public Node()
	{
		this.id = ++idcounter;
		this.port_list = new HashSet<>();
		this.connect = new HashSet<>();
		this.str = "on";
	}
	public Node(String name,Set<String> p,Set<String> c)
	{
		this.id = ++idcounter;
		this.port_list = new HashSet<>();
		this.connect = new HashSet<>();
		port_list.addAll(p);
		connect.addAll(c);
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
