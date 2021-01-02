package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import jdd.bdd.BDD;

/**
 * @author Planck
 * @version 0.0.1
 * @see Node
 * @see Change
 * @see Graph
 * @see Rule
 */
public class APKeep {
	public BDD bdd;
	public Set<Node> device; //init device
	public ArrayList<Rule> rules;//init rules
	public ArrayList<Change> C ; // changes
	public Graph G ; //DFG
	public Set<Integer> D ; //transffered predicate set
	public Set<Node> V ;
	private static boolean traversed = false;
	
	public APKeep() {
		this.bdd=new BDD(1000,100);
		bdd.createVars(32);
		this.device = new HashSet<>();
		this.rules = new ArrayList<>();
		this.C = new ArrayList<>();
		this.G = new Graph();
		this.D = new HashSet<>();
		this.V = null;
	}
	/**
	 * @param device 设备拓扑信息文件路径
	 * @param rule 待插入规则信息文件路径
	 */
	public APKeep(String device,String rule) {
		// TODO Auto-generated constructor stub
		this.bdd=new BDD(1000,100);
		bdd.createVars(32);
		this.device = initDevice(device);
		this.rules = initRule(rule);
		this.C = new ArrayList<>();
		this.G = new Graph();
		this.D = new HashSet<>();
		this.V = null;
	}
	public APKeep(String device,String rule,String dd) {
		// TODO Auto-generated constructor stub
		this.bdd=new BDD(1000,100);
		bdd.createVars(32);
		this.device = initDevice(device);
		this.rules = initRule(rule);
		setDevice(dd);
		this.C = new ArrayList<>();
		this.G = new Graph();
		this.D = new HashSet<>();
		this.V = null;
	}
	/**
	 * @param set 进行遍历验证不变量的初始节点集
	 */
	public void getStartNodes(Set<Node> set)
	{
		V = new HashSet<>();
		for(Node s:set)
			V.add(s);
	}
	/**
	 * @throws IOException 
	 */
	public  ArrayList<Rule> initRule(String f)
	{
		ArrayList<Rule> rules = new ArrayList<>();
		try {
			
			BufferedReader in = new BufferedReader((new FileReader(f)));
			String str;
			String hit="00000000000000000000000000000000";
			while((str=in.readLine())!=null&&str!="")
			{
				String[] rule = str.split("\\,");
				String loc = rule[0];
				String port=rule[1];
				String match=rule[2];
				int next=Integer.parseInt(rule[3]);
				int prir=Integer.parseInt(rule[4]);
				Rule r = new Rule(loc,port,match,hit,next,prir,bdd);
				rules.add(r);
				
			}
			in.close();
		} catch (IOException e) {
			// TODO: handle exception
			System.err.println("no such rules file,please check");
		}
		return rules;
	}
 	public  Set<Node> initDevice(String f)
	{
		Set<Node> device = new HashSet<>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(f));
			String str;
			while((str=in.readLine())!=null)
			{
				String[] newstr = str.split("\\,");
				String name = newstr[0];
				String[] port = newstr[1].split(" ");
				Set<String> port_list = new HashSet<>();
				for(String p:port)
				{
					port_list.add(p);
				}
				Set<String> connect = new HashSet<>();
				String[] con = newstr[2].split(" ");
				for(String c:con)
				{
					connect.add(c);
				}
				Node s = new Node(name,port_list,connect,bdd);
				device.add(s);
				
			}
//			for(Node d:device)
//			{
//				System.out.println(d.str);
//			}
			in.close();
			
		} catch (IOException  e) {
			// TODO: handle exception
			System.err.println("no such topo file,please check");
		}
		return device;
	}
	public void setDevice(String f)
	{
		try {
			BufferedReader in = new BufferedReader(new FileReader(f));
			String str;
			String sname = new String();
			while((str=in.readLine())!=null)
			{
				if(str.startsWith("$"))
				{
					sname = str.substring(1, str.length()-1);
					continue;
				}
				Node s = Find(sname, device);
				String[] tokens = str.split("\\;");
				String[] vlans = tokens[0].split(" ");
				Set<String> port_list = new HashSet<>();
				for(String p:vlans)
				{
					port_list.add(p);
				}
				Map<String, Set<String>> map = new HashMap<>();
				String[] maps = tokens[1].split("\\,");
				for(String m:maps) {
					String[] kv = m.split(" ");
					map.put(kv[0], new HashSet<>());
					for(int i=1;i<kv.length;i++)
					{
						map.get(kv[0]).add(kv[i]);
					}
				}
				s.portMap = map;
				s.creatFWElement(s.name,port_list);					
			}
//			for(Node d:device)
//			{
//				System.out.println(d.str);
//			}
			in.close();
			
		} catch (IOException  e) {
			// TODO: handle exception
			System.err.println("no such topo file,please check");
		}
	}
	public static  <E,T> void mapPrint(Map<E, Set<T>> map) {
		for(Entry<E, Set<T>> entry:map.entrySet())
		{
			System.out.println("key="+entry.getKey()+"  values="+entry.getValue());
		}
	}
	public  Node Find(String s,Set<Node> set)//find node by name
	{
		ArrayList<Node> arr =  new ArrayList<>(set);
		for (Node e : arr) {
			if(e.name.equals(s))
				return e;
		}	
		return null;
	}
	public void buildPPM(Node s) {
		
	}
	public  void insertRulestoDevice(Node s) {
		for(Rule r:rules)
		{
			if(s.name.equals(r.getLoc()))
			{
				Identify(r, s.rules);
			}
		}
		for(Change c:C)
			c.printChange();
		Set<Integer> d1 = Update(s.Pred, s.Port);
		s.updateInterfaces();
		mapPrint(s.Pred_interface);
		mapPrint(s.Port_interface);
		System.out.println("D="+d1);
		ConstructDeltaForwardingGraph(d1,s.Port_interface);
		G.printGraph();
		C.clear();
		D.addAll(d1);
	}
	/**
	 * @param r  插入的一条规则
	 * @param R  当前设备的已有规则链表
	 */
	public  void  Identify(Rule r,Set<Rule> R) {
		
		r.changeHit(r.getMatch());// r.hit <-- r.match;
		int and = bdd.ref(bdd.minterm(""));
		for (Rule rule : R) {
			and = bdd.ref(bdd.and(r.b_hit, rule.b_hit));
			if(rule.getPrior()>r.getPrior()&&and!=0)
			{
				r.b_hit = bdd.ref(bdd.and(r.b_hit, rule.b_hit));
			}
			if(rule.getPrior()<r.getPrior()&&and!=0)
			{
				if(r.getport()!=rule.getport())
				{
					Change change = new Change(and, rule.getport(),r.getport(), bdd);//
//					change.printChange();
					boolean flag = false;
					for(Change c:C)
					{
						if(c.from.equals(change.from)&&c.to.equals(change.to)&&c.insertion==change.insertion)
							flag = true;
					}
					if(flag == false)
						C.add(change);//add to changes
					System.out.println("-------->identify success!<--------");
				}
				rule.b_hit = bdd.ref(bdd.and(bdd.not(r.b_hit), rule.b_hit));
			}
		}
		bdd.deref(and);
		R.add(r);
		System.out.println("-------->insert one rule!<---------");
	}
	private  void Split(int p,int p1,int p2,Map<String, Set<Integer>> pre,Map<Integer, Set<String>> por,Set<Integer> d)
	{
		for(String temp_port:por.get(p))
		{
			pre.get(temp_port).add(p1);
			pre.get(temp_port).add(p2);
			pre.get(temp_port).remove(p);
			
		}
		Set<String> s1 = new HashSet<>();
		Set<String> s2 = new HashSet<>();
		s1.addAll(por.get(p));
		s2.addAll(por.get(p));
		por.put(p1, s1);
		por.put(p2, s2);
		por.remove(p);//unclear
		//d
		if(d.contains(p))
		{
			d.add(p1);
			d.add(p2);
			d.remove(p);
		}		
	}
	private  void Transfer(int p,String from ,String to,Map<String, Set<Integer>> pre,Map<Integer, Set<String>> por,Set<Integer> d)
	{
		pre.get(from).remove(p);
		pre.get(to).add(p);
		por.get(p).add(to);
		por.get(p).remove(from);
		//d
		d.add(p);
	}

	private  void Merge(int p1,int p2,int p,Map<String, Set<Integer>> pre,Map<Integer, Set<String>> por,Set<Integer> d)
	{

		for(String temp_port:por.get(p1))
		{
			pre.get(temp_port).add(p);
			pre.get(temp_port).remove(p1);
			pre.get(temp_port).remove(p2);
		}
		Set<String> s1 = new HashSet<>();
		s1.addAll(por.get(p1));
		por.put(p, s1);//unclear
		if(d.contains(p1)||d.contains(p2))
		{
			d.add(p);
			d.remove(p1);
			d.remove(p2);
		}
	}
	/**
	 * @param pre 当前设备端口谓词映射
	 * @param por 当前设备谓词端口映射
	 * @return 当前设备的转移谓词集d
	 * @throws ConcurrentModificationException 
	 */
	public  Set<Integer> Update(Map<String, Set<Integer>> pre,Map<Integer, Set<String>> por) {
		Set<Integer> d = new HashSet<>();
		try {
		for(Change c:C)
		{
			for(int i=0;i<pre.get(c.from).size();i++)
			{
				Integer[] P = pre.get(c.from).toArray(new Integer[pre.get(c.from).size()]);
				int and=bdd.ref(bdd.and(P[i],c.insertion));
				if(and!=0)
				{
					if(and!=P[i])
					{
							Split(P[i], and, bdd.and(P[i], bdd.not(c.insertion)), pre, por, d);
					}
					
					Transfer(and, c.from, c.to, pre, por, d);
					for(Integer pp:pre.get(c.from))
					{
						if(pp!=P[i]&&por.get(pp).equals(por.get(P[i])))
						{
								Merge(P[i], pp, bdd.or(P[i], pp), pre, por, d);
						}
					}
					c.insertion=bdd.and(c.insertion, bdd.not(P[i]));
				}
			}
			mapPrint(pre);//test por or pre
			mapPrint(por);
			System.out.println();
		}
		} catch (ConcurrentModificationException e) {
			// TODO: handle exception
			System.out.println("catch ConcurrentModificationException!");
		}
		return d;
	}
	/**
	 * 创建转发图G(V,E,A)，将相关的V E A添加进G
	 * @param d 当前设备转移谓词集
	 * @param por 当前设备谓词端口映射
	 */
	public void ConstructDeltaForwardingGraph(Set<Integer> d,Map<Integer, Set<String>> por)
	{
		Set<Node> V = G.V;
		Set<Edge> E = G.E;
		Map<Edge, Set<Integer>> A = G.A;
		for(Integer delta:d)
		{
			for(String port:por.get(delta))
			{
				for(Node s1:device)
				{
					if(s1.findInterface(port))
					{
						if(!V.contains(s1))
						{
							V.add(s1);
						}
						for(Node s2:device)
						{
							if(s2.findConnect(port))
							{
								if(!V.contains(s2))
								{
									V.add(s2);
								}
								Edge e1 =new Edge(s1, s2,port);
//								Edge e2 = new Edge(s2,s1);
								if(!E.contains(e1))
								{
									if(A.get(e1) != null) {
										A.get(e1).clear();
									}
									else {
										A.put(e1, new HashSet<>());
										A.get(e1).clear();
									}
									E.add(e1);
								}
								A.get(e1).add(delta);
							}
						}
					}
				}
			}
		}
	}
	/**
	 * traverse the graph checking invariants
	 */
	public  void CheckInvariants()
	{
		if(V==null)
		{
			System.err.println("please initailize start node set V");
			return;
		}
		for(Node s:V)
		{
			Set<Integer> pset = new HashSet<>();
			pset.addAll(D);
			Set<Node> history = new HashSet<>();
			history.clear();
			traversed = false;//判断未初始化的参与遍历的节点s
			Traverse(s,pset,history);
			if(traversed==false)
				System.err.println("found blackhole by uninitialized node!");
			System.out.println();
		}
	}
	private  void Traverse(Node s,Set<Integer> pSet,Set<Node> history)
	{
		if(pSet.isEmpty())
			return;
		if(history.contains(s))
		{
			System.err.println("found loop!");
			return;
		}
		//
		for (Edge e : G.E) {
			if(s.findInterface(e.fport)&&s.equals(e.from))//e.from.comtains(s)
			{
				traversed  = true;
				for(Integer p:e.to.Pred.get("default"))//判断节点default端口上的谓词是否包含了这条边的谓词，如果是则出现黑洞
				{
					for(Integer pp:G.A.get(e))
					{
						if(bdd.and(p, pp)!=0)//
						{
							System.err.println("found blackhole!");
//							return;
						}
					}
				}
				Set<Integer> insert = setAnd(pSet, G.A.get(e));
				history.add(s);
				Traverse(e.to, insert, history);
				for(Integer i:insert)
					bdd.deref(i);
			}
		}
	}
	/**
	 * compute A(and)B by using BDD
	 * @param A pset
	 * @param B 边上的谓词集
	 * @return 两集合的交集
	 */
	private  Set<Integer> setAnd(Set<Integer> A,Set<Integer> B) {
		Set<Integer> set = new HashSet<>();
		for(Integer a:A)
		{
			for(Integer b:B)
			{
				int temp = bdd.ref(bdd.and(a, b));
				if(temp!=0)
				{
					set.add(temp);
					continue;
				}
				bdd.deref(temp);
			}
		}
		return set;
	}
}
