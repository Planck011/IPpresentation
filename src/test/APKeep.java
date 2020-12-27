package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import jdd.bdd.BDD;

/**
 * @author Planck
 *
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
			while((str=in.readLine())!=null)
			{
				String[] rule = str.split("\\,");
				String port=rule[0];
				String match=rule[1];
				int next=Integer.parseInt(rule[2]);
				int prir=Integer.parseInt(rule[3]);
				Rule r = new Rule(port,match,hit,next,prir,bdd);
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
	public  void insertRulestoDevice(Node s) {
		for(Rule r:rules)
		{
			if(s.findPort(r.getport()))
			{
				Identify(r, s.rules);
			}
		}
//		for(Change c:C)
//			c.printChange();
		Set<Integer> d1 = Update(s.Pred, s.Port);
//		System.out.println("D="+d1);
		ConstructDeltaForwardingGraph(d1,s.Port);
		G.printGraph();
		C.clear();
		D.addAll(d1);
	}
	/**
	 * @param r  插入的一条规则
	 * @param R  当前设备的已有规则链表
	 */
	public  void  Identify(Rule r,ArrayList<Rule> R) {
		r.changeHit(r.getMatch());// r.hit <-- r.match;
		int and = bdd.ref(bdd.minterm(""));
		for(int i=0;i<R.size();i++)
		{
			and = bdd.ref(bdd.and(r.b_hit, R.get(i).b_hit));//r'.hit(and)r.hit
			if(R.get(i).getPrior()>r.getPrior()&&and!=0)
			{
				r.b_hit=bdd.ref(bdd.and(r.b_hit, R.get(i).b_hit));
			}
			if(R.get(i).getPrior()<r.getPrior()&&and!=0) {
				if(r.getport()!=R.get(i).getport())
				{
					Change change = new Change(and, R.get(i).getport(),r.getport(), bdd);//
//					change.printChange();
					C.add(change);//add to changes
					System.out.println("-------->identify success!<--------");
				}
				R.get(i).b_hit=bdd.ref(bdd.and(bdd.not(r.b_hit), R.get(i).b_hit));//
//				R.rules[i].changeHit(and);
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
			for(Integer p:pre.get(c.from))
			{
				int and=bdd.ref(bdd.and(p,c.insertion));
				if(and!=0)
				{
					if(and!=p)
					{
							Split(p, and, bdd.and(p, bdd.not(c.insertion)), pre, por, d);
					}
					
					Transfer(and, c.from, c.to, pre, por, d);
					for(Integer pp:pre.get(c.from))
					{
						if(pp!=p&&por.get(pp).equals(por.get(p)))
						{
								Merge(p, pp, bdd.or(p, pp), pre, por, d);
						}
					}
					c.insertion=bdd.and(c.insertion, bdd.not(p));
				}
			}
//			mapPrint(pre);//test por or pre
//			System.out.println();
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
					if(s1.findPort(port))
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
			if(s.findPort(e.fport))//e.from.comtains(s)
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
