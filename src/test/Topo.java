package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import jdd.bdd.BDD;

public class Topo {
	private String port_map_file = "/home/planck/桌面/myWorkspace/eclipse/IPpresentation/data/tf_stanford_backbone/port_map.txt";
	private String topo_file = "/home/planck/桌面/myWorkspace/eclipse/IPpresentation/data/tf_stanford_backbone/topo.txt";
	private int PORT_TYPE_MULTIPLIER = 10000;
	private int SWITCH_ID_MULTIPLIER = 100000;
	public Topo() {
		// TODO Auto-generated constructor stub
	}
	public Map<Integer, Set<Integer>> load_Port()
	{
		Map<Integer, Set<Integer>> ports = new HashMap<>();
		try {
			
			BufferedReader in = new BufferedReader((new FileReader(port_map_file)));
			String str = in.readLine();
			while((str=in.readLine())!=null)
			{	
				if(!str.startsWith("$"))
				{
					String [] tokens = str.split("\\:");
					int port_flat = Integer.parseInt(tokens[1]);
					int dpid = port_flat/SWITCH_ID_MULTIPLIER;
					int port = port_flat%PORT_TYPE_MULTIPLIER;
					
					if(!ports.keySet().contains(dpid))
						ports.put(dpid, new HashSet<>());
					if(!ports.get(dpid).contains(port))
						ports.get(dpid).add(port);
				}
			}
			in.close();
		} catch (IOException e) {
			// TODO: handle exception
			System.err.println("no such rules file,please check");
		}
		return ports;
	}
	/*
	 * links = set()
        f = open(filename, 'r')
        for line in f:
            if line.startswith("link"):
                tokens = line.split('$')
                src_port_flat = int(tokens[1].strip('[]').split(', ')[0])
                dst_port_flat = int(tokens[7].strip('[]').split(', ')[0])
                links.add((src_port_flat, dst_port_flat))
        f.close()
        return links
	 */
	public Map<Link<Integer>,Link<Integer>> load_link() {
//		Set<Link<Integer>> links_port = new HashSet<>();
//		Set<Link<Integer>> links_dpid = new HashSet<>();
		Map<Link<Integer>, Link<Integer>> links = new HashMap<>();
		try {
			
			BufferedReader in = new BufferedReader((new FileReader(topo_file)));
			String str = in.readLine();
			while((str=in.readLine())!=null)
			{	
				if(str.startsWith("link"))
				{
					String [] tokens = str.split("\\$");
					int scr_port_flat = Integer.parseInt(tokens[1].substring(1,tokens[1].length()-1));
					int dst_port_flat = Integer.parseInt(tokens[7].substring(1,tokens[7].length()-1));
					int src_dpid = scr_port_flat/SWITCH_ID_MULTIPLIER;
					int dst_dpid = dst_port_flat/SWITCH_ID_MULTIPLIER;
					int src_port = scr_port_flat/PORT_TYPE_MULTIPLIER;
					int dst_port = dst_port_flat/PORT_TYPE_MULTIPLIER;
//					links_port.add(new Link<Integer>(src_port, dst_port));
//					links_dpid.add(new Link<Integer>(src_dpid,dst_dpid));
					links.put(new Link<Integer>(src_dpid,dst_dpid), new Link<Integer>(src_port, dst_port));
				}
			}
			in.close();
		} catch (IOException e) {
			// TODO: handle exception
			System.err.println("no such rules file,please check");
		}
		
		return links;
	}
//	public void creat_links(Set<Link<Integer>> links,Map<Integer, Set<Integer>> ports) {
//		Map<Link<Integer>, Link<Integer>> first_pass = new HashMap<>();
//		for(Link<Integer> link:links)
//		{
//			int src_dpid = link.src/SWITCH_ID_MULTIPLIER;
//			int dst_dpid = link.dst/SWITCH_ID_MULTIPLIER;
//			int src_port = link.src/PORT_TYPE_MULTIPLIER;
//			int dst_port = link.dst/PORT_TYPE_MULTIPLIER;
//			Link<Integer> l1 = new Link<Integer>(src_dpid,src_port);
//			Link<Integer> l12 = new Link<Integer>(l1);
//			Link<Integer> l2 = new Link<Integer>(dst_dpid, dst_port);
//			Link<Integer> l22 = new Link<Integer>(l2);
//			if(!first_pass.keySet().contains(l1))
//			{
//				first_pass.put(l1, l2);
//			}
//			if(!first_pass.keySet().contains(o))
//		}
//	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Topo ttTopo = new Topo();
		BDD bdd = new BDD(1000,100);
		bdd.createVars(32);
		Map<Integer, Set<Integer>> sMap = ttTopo.load_Port();
//		System.out.println(sMap);
		Map<Link<Integer>,Link<Integer>> links = ttTopo.load_link();
		Set<Node<Integer>> device = new HashSet<>();
		for(Entry<Integer, Set<Integer>> s:sMap.entrySet()) {
			Node<Integer> temp = new Node<Integer>(s.getKey());
			device.add(temp);
		}
		for(Entry<Link<Integer>, Link<Integer>> l:links.entrySet())
		{
			for(Node<Integer> d:device)
			{
				if(d.name.equals(l.getKey().src))
				{
					d.connect.add(l.getValue().dst);
				}
			}
		}
		System.out.println(device);
	}

}
