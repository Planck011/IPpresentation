package test;

import java.util.ArrayList;


public class Node {
	private int id;
	public ArrayList<String> port_list;
	public ArrayList<String> connect;
	private static int idcounter=0;
	private final int portcount=7;
	public String str = "default";
	public Node()
	{
		this.id = ++idcounter;
		this.port_list = new ArrayList<>();
		this.connect = new ArrayList<>();
		this.str = "on";
	}
	public int getId() {
		return id;
	}
	public boolean findPort(String port) {
		int i;
		for(i=0;i<port_list.size();i++)
		{
			if(port_list.get(i).equals(port))
				return true;
		}
		return false;
	}
	public boolean findConnect(String port) {
		int i;
		for(i=0;i<connect.size();i++)
		{
			if(connect.get(i).equals(port))
				return true;
		}
		return false;
	}
}
