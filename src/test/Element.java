package test;

import java.util.*;

public class Element {
	private Rule_Type type;
	private String name;
	private ArrayList<String> port_list;
	public Element()
	{
		this.type = Rule_Type.Faward;
		this.name = "default";
		this.port_list = new ArrayList<>();
		port_list.add("default");
	}
	public String getRuleType() {
		return type.toString();
	}
	public String getName() {
		return name;
	}
	public ArrayList<String> getPort_list() {
		return port_list;
	}
}
class ACLelement extends Element{
	private Rule_Type type = Rule_Type.ACL;
	private String name;
	private ArrayList<String> port_list;
	public ACLelement() {
		// TODO Auto-generated constructor stub
		this.port_list = new ArrayList<>();
		this.name = "defaultacl";
		port_list.add("permit");
		port_list.add("deny");
	}
	public String getRuleType() {
		return type.toString();
	}
	public String getName() {
		return name;
	}
	public ArrayList<String> getPort_list() {
		return port_list;
	}
}
class FWelement extends Element{
	private Rule_Type type = Rule_Type.Faward;
	private String name;
	private ArrayList<String> port_list;
	public FWelement() {
		// TODO Auto-generated constructor stub
		this.port_list = new ArrayList<>();
		this.name = "defaultfw";
		port_list.add("defualt");
	}
	public String getRuleType() {
		return type.toString();
	}
	public String getName() {
		return name;
	}
	public ArrayList<String> getPort_list() {
		return port_list;
	}
}
class NATelement extends Element{
	private Rule_Type type = Rule_Type.NAT;
	private String name;
	private ArrayList<String> port_list;
	public NATelement() {
		// TODO Auto-generated constructor stub
		this.port_list = new ArrayList<>();
		this.name = "defaultnat";
		port_list.add("id");
	}
	public String getRuleType() {
		return type.toString();
	}
	public String getName() {
		return name;
	}
	public ArrayList<String> getPort_list() {
		return port_list;
	}
}
