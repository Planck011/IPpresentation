package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.HashedMap;

import com.sun.org.apache.bcel.internal.generic.NEW;

public class Topo {
	private String port_map_file = "C:\\Users\\puyun\\Desktop\\test\\workspace\\IPpresentation\\data\\tf_stanford_backbone\\port_map.txt";
	private String topo_file = "C:\\Users\\puyun\\Desktop\\test\\workspace\\IPpresentation\\data\\tf_stanford_backbone\\topo.txt";
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
	public Set<Link<Integer>> load_link() {
		Set<Link<Integer>> links = new HashSet<>();
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
					links.add(new Link<Integer>(scr_port_flat, dst_port_flat));
				}
			}
			in.close();
		} catch (IOException e) {
			// TODO: handle exception
			System.err.println("no such rules file,please check");
		}
		return links;
	}
	/*
	 *  def create_links(self, links, ports):  
        '''Generate dummy switches
           For example, interface A1 connects to B1 and C1 at the same time. Since
           Mininet uses veth, which supports point to point communication only,
           we need to manually create dummy switches

        @param links link info from the file
        @param ports port info from the file
        ''' 
        # First pass, find special ports with more than 1 peer port
        first_pass = {}
        for (src_port_flat, dst_port_flat) in links:
            src_dpid = src_port_flat / self.SWITCH_ID_MULTIPLIER
            dst_dpid = dst_port_flat / self.SWITCH_ID_MULTIPLIER
            src_port = src_port_flat % self.PORT_TYPE_MULTIPLIER
            dst_port = dst_port_flat % self.PORT_TYPE_MULTIPLIER
            
            if (src_dpid, src_port) not in first_pass.keys():
                first_pass[(src_dpid, src_port)] = set()
            first_pass[(src_dpid, src_port)].add((dst_dpid, dst_port))
            if (dst_dpid, dst_port) not in first_pass.keys():
                first_pass[(dst_dpid, dst_port)] = set()
            first_pass[(dst_dpid, dst_port)].add((src_dpid, src_port))
            
        # Second pass, create new links for those special ports
        dummy_switch_id = self.DUMMY_SWITCH_BASE
        for (dpid, port) in first_pass.keys():
            # Special ports!
            if(len(first_pass[(dpid,port)])>1):
                self.add_switch( "s%s" % dummy_switch_id )
                self.dummy_switches.add(dummy_switch_id)
            
                self.add_link( node1="s%s" % dpid, node2="s%s" % dummy_switch_id, port1=port, port2=1 )
                dummy_switch_port = 2
                for (dst_dpid, dst_port) in first_pass[(dpid,port)]:
                    first_pass[(dst_dpid, dst_port)].discard((dpid,port))
                    self.add_link( node1="s%s" % dummy_switch_id, node2="s%s" % dst_dpid, port1=dummy_switch_port, port2=dst_port)
                    ports[dst_dpid].discard(dst_port)
                    dummy_switch_port += 1
                dummy_switch_id += 1  
                first_pass[(dpid,port)] = set()    
            ports[dpid].discard(port)
        
        # Third pass, create the remaining links
        for (dpid, port) in first_pass.keys():
            for (dst_dpid, dst_port) in first_pass[(dpid,port)]:
                self.add_link( node1="s%s" % dpid, node2="s%s" % dst_dpid, port1=port, port2=dst_port )
                ports[dst_dpid].discard(dst_port)     
            ports[dpid].discard(port)   
	 */
	public void creat_links(Set<Link<Integer>> links,Map<Integer, Set<Integer>> ports) {
		Map<Link<Integer>, Link<Integer>> first_pass = new HashMap<>();
		for(Link<Integer> link:links)
		{
			int src_dpid = link.src/SWITCH_ID_MULTIPLIER;
			int dst_dpid = link.dst/SWITCH_ID_MULTIPLIER;
			int src_port = link.src/PORT_TYPE_MULTIPLIER;
			int dst_port = link.dst/PORT_TYPE_MULTIPLIER;
			Link<Integer> l1Link = new Link<Integer>(src_dpid,src_port);
			if(!first_pass.keySet().contains(l1Link))
			{
//				first_pass.put(l1Link, )
			}
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Topo ttTopo = new Topo();
		Map<Integer, Set<Integer>> sMap = ttTopo.load_Port();
		System.out.println(sMap);
		Set<Link<Integer>> links = ttTopo.load_link();
		for(Link<Integer> l:links)
		{
			System.out.print("("+l.src+","+l.dst+")");
		}
	}

}
