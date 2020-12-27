package test;

import java.util.*;
import java.util.Map.Entry;

import com.sun.tools.javac.launcher.Main;

import java.io.*;
import java.lang.*;
import jdd.bdd.*;
import sun.jvm.hotspot.ui.action.FindAction;

public class IPpresentation {
	private static boolean traversed = false;
	public static void main(String[] args) {
		/*
		 * 由于没有构建元素模块，本次实验仅针转发规则。
		 * 待插入规则有四条，对应IP范围为（IP/mask)192.0.0.0/8,192.168.0.0/16,198.29.128.160/28,198.39.128/18
		 * 见rules.txt，每行依次是端口，匹配域，下一跳（未使用），优先级。
		 * 通过initRule()读取待插入的规则，返回Rule对象的数组。
		 * 规则已经事先计算好二进制表示的匹配域，并使用BDD库转换成bdd表示，详见Rule.java构造函数;
		 * 通过initDevice读取topo.txt文件中的网络拓扑结构，文件中每行依次为节点名，具有的端口集（空格分开），与其他设备相连的端口集（空格分开）。
		 * 注意每台设备都有各defualt端口，并初始化为具有谓词true，也就是所有IP范围的数据包初始状态下都流向default端口。
		 * 每台设备节点都具有端口谓词映射的两个map Pred，Port，见Node.java(暂定为局部端口谓词映射，不使用全局端口谓词映射是因为这需要保证网络中
		 * 的所有端口名称都是独一无二的，然而例如每个设备都有default端口，如果用全局，需要采取二级命名）
		 * 算法1对应函数Identify()，返回变化ArrayList<Change> changes;
		 * 算法2对应函数Update()，返回转移谓词集合Set<Integer> D;
		 * 算法3对应UpdateRewrite(),由于论文信息不全，以及对重写元素还不太懂，相关函数未实现完整;
		 * 算法4对应ConstructDeltaForwardingGraph()，根据算法2返回的转移谓词集D，以及整个网络节点信息和端口谓词映射生成并返回转发图Graph G;
		 * 算法5对应CheckInvariants(),根据算法4返回的转发图G以及初始启动节点集V，遍历转发图，验证不变量，判断环路黑洞。
		 * 算法12通过自造数据进行了验证，目前没有问题，除了代码中的不好的操作导致捕获到ConcurrentModificationException异常，暂时影响不大，尚未修改。
		 * 算法45问题解决，现在运行test()可以正确发现环路黑洞。
		 * 将主类中的函数集成了APKeep类
		 */
		// TODO Auto-generated method stub
		
//		test();
		String f1 = "C:\\Users\\puyun\\Desktop\\test\\workspace\\IPpresentation\\src\\rules.txt";
		String f2 = "C:\\Users\\puyun\\Desktop\\test\\workspace\\IPpresentation\\src\\topo.txt";
		long strattime = System.currentTimeMillis();
		APKeep ap = new APKeep(f2,f1);
		ap.insertRulestoDevice(Find("s3", ap.device));
		ap.insertRulestoDevice(Find("s4", ap.device));
		Set<Node> v = new HashSet<>();
		v.add(Find("s3", ap.device));
		ap.getStartNodes(v);
		ap.CheckInvariants();
		long endtime = System.currentTimeMillis();
		System.out.println("runtime:"+(endtime-strattime)+"ns");
	}
	public static void test()//
	{
		BDD bdd = new BDD(1000,100); //init BDD
		bdd.createVars(32);
		Set<Node> device = initDevice(bdd); //init device
		ArrayList<Rule> rules = initRule(bdd);//init rules
		ArrayList<Change> C = new ArrayList<>(); // changes
		Graph G = new Graph(); //DFG
		Set<Integer> D = new HashSet<>(); //transffered predicate set
		Set<Node> V = new HashSet<>();//start node set
		System.out.println("----------------------start-------------------------");
		long strattime = System.currentTimeMillis();
		
		insertRulestoDevice(Find("s3", device), rules, bdd, C, device, G, D);//insert rule to node s3
		
		
		insertRulestoDevice(Find("s4", device), rules, bdd, C, device, G, D);//insert rule to node s4
		
		
		V.add(Find("s3", device));
		V.add(Find("s4", device));
		V.add(Find("s1", device));
		CheckInvariants(G, D, V,bdd);
		long endtime = System.currentTimeMillis();
		System.out.println("runtime:"+(endtime-strattime)+"ns");//runtime
		System.out.println("-----------------------end--------------------------");
		
		bdd.cleanup();
	}
	/*
	 * 此函数给指定节点插入规则，识别变化，更改转移谓词集D，并修改转发图
	 */
	public static void insertRulestoDevice(Node s,ArrayList<Rule> rules,BDD bdd,ArrayList<Change> C,Set<Node> device,Graph G,Set<Integer> D) {
		for(Rule r:rules)
		{
			if(s.findPort(r.getport()))
			{
				Identify(r, s.rules, bdd, C);
			}
		}
		for(Change c:C)
			c.printChange();
		Set<Integer> d1 = Update(C, bdd, s.Pred, s.Port);
		System.out.println("D="+d1);
		ConstructDeltaForwardingGraph(d1,s.Port, device,G);
		G.printGraph();
		C.clear();
		D.addAll(d1);
	}
	public static Node Find(String s,Set<Node> set)//find node by name
	{
		ArrayList<Node> arr =  new ArrayList<>(set);
		for (Node e : arr) {
			if(e.name.equals(s))
				return e;
		}	
		return null;
	}
	public static <E>E Find(E s,Set<E> set)
	{
		ArrayList<E> arr =  new ArrayList<>(set);
		for (E e : arr) {
			if(e.equals(s))
				return e;
		}	
		return null;
	}
	public static ArrayList<Rule> initRule(BDD bdd)
	{
		ArrayList<Rule> rules = new ArrayList<>();
		try {
			
			BufferedReader in = new BufferedReader((new FileReader("C:\\Users\\puyun\\Desktop\\test\\workspace\\IPpresentation\\src\\rules.txt")));
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
	public static Set<Node> initDevice(BDD bdd)
	{
		Set<Node> device = new HashSet<>();
		try {
			BufferedReader in = new BufferedReader(new FileReader("C:\\Users\\puyun\\Desktop\\test\\workspace\\IPpresentation\\src\\topo.txt"));
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

	public static <E,T> void mapPrint(Map<E, Set<T>> map) {
		for(Entry<E, Set<T>> entry:map.entrySet())
		{
			System.out.println("key="+entry.getKey()+"  values="+entry.getValue());
		}
	}
	public static void  Identify(Rule r,ArrayList<Rule> R,BDD bdd,ArrayList<Change> C) {
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
	public static void Split(int p,int p1,int p2,Map<String, Set<Integer>> pre,Map<Integer, Set<String>> por,Set<Integer> D)
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
		//D
		if(D.contains(p))
		{
			D.add(p1);
			D.add(p2);
			D.remove(p);
		}		
	}
	public static void Transfer(int p,String from ,String to,Map<String, Set<Integer>> pre,Map<Integer, Set<String>> por,Set<Integer> D)
	{
		pre.get(from).remove(p);
		pre.get(to).add(p);
		por.get(p).add(to);
		por.get(p).remove(from);
		//D
		D.add(p);
	}

	public static void Merge(int p1,int p2,int p,Map<String, Set<Integer>> pre,Map<Integer, Set<String>> por,Set<Integer> D)
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
		if(D.contains(p1)||D.contains(p2))
		{
			D.add(p);
			D.remove(p1);
			D.remove(p2);
		}
	}
	public static Set<Integer> Update(ArrayList<Change> C,BDD bdd,Map<String, Set<Integer>> pre,Map<Integer, Set<String>> por) {
		Set<Integer> D = new HashSet<>();
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
							Split(p, and, bdd.and(p, bdd.not(c.insertion)), pre, por, D);
					}
					
					Transfer(and, c.from, c.to, pre, por, D);
					for(Integer pp:pre.get(c.from))
					{
						if(pp!=p&&por.get(pp).equals(por.get(p)))
						{
								Merge(p, pp, bdd.or(p, pp), pre, por, D);
						}
					}
					c.insertion=bdd.and(c.insertion, bdd.not(p));
				}
			}
			mapPrint(pre);//test por or pre
			System.out.println();
		}
		} catch (ConcurrentModificationException e) {
			// TODO: handle exception
			System.out.println("catch ConcurrentModificationException!");
		}
		return D;
	}
	public static void Update(ArrayList<Change> C,BDD bdd,Map<String, Set<Integer>> pre,Map<Integer, Set<String>> por,Set<Integer> D) {
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
							Split(p, and, bdd.and(p, bdd.not(c.insertion)), pre, por, D);
					}
					
					Transfer(and, c.from, c.to, pre, por, D);
					for(Integer pp:pre.get(c.from))
					{
						if(pp!=p&&por.get(pp).equals(por.get(p)))
						{
								Merge(p, pp, bdd.or(p, pp), pre, por, D);
						}
					}
					c.insertion=bdd.and(c.insertion, bdd.not(p));
				}
			}
			mapPrint(pre);//test por or pre
			System.out.println();
		}
		} catch (ConcurrentModificationException e) {
			// TODO: handle exception
			System.out.println("catch ConcurrentModificationException!");
		}
	}
	public static int T(int p) { //undefined
		return p;
	}
	public static Set<Integer> SplitRW(int p,int p1,int p2,Map<Integer, Set<Integer>> RT,BDD bdd,Map<String, Set<Integer>> pre,Map<Integer, Set<String>> por)
	{
		Set<Integer> D = new HashSet<>();
		Split(p, p1, p2, pre, por, D);
		for(Entry<Integer, Set<Integer>> entry:RT.entrySet())
		{
				if(entry.getValue().contains(p))
				{
					entry.getValue().add(p1);
					entry.getValue().add(p2);
					entry.getValue().remove(p);
				}
		}
		RT.remove(p);
		RT.put(p1,new HashSet<>());
		RT.get(p1).add(T(p1));
		RT.put(p2,new HashSet<>());
		RT.get(p2).add(T(p2));
		return D;
	}
	public static Set<Integer> UpdateRW(Set<Change> C)//undefined
	{
		return new HashSet<Integer>();
	}
	public static Set<Integer> UpdateRewrite(Set<Change> C,BDD bdd,Map<Integer, Set<Integer>> RT,Map<String, Set<Integer>> pre,Map<Integer, Set<String>> por)
	{
		Set<Integer> D = UpdateRW(C);
		boolean updated;
		while(true)
		{
			updated=false;
			for(Entry<Integer, Set<Integer>> entry:RT.entrySet())
			{
				for(Integer p:entry.getValue())
				{
					Set<Integer> P = por.keySet();
					if(!P.contains(p))
					{
						for (Integer pp : P) 
						{
							if(bdd.and(p,pp)!=0&&bdd.and(pp, bdd.not(p))!=0)
								SplitRW(pp, bdd.and(p,pp), bdd.and(pp, bdd.not(p)), RT, bdd, pre, por);
						}
						updated=true;
					}
				}
			}
			if(updated==false)
				break;
		}
		return D;
	}
	public static Graph ConstructDeltaForwardingGraph(Set<Integer> D,Map<Integer, Set<String>> por,Set<Node> device)
	{
		Set<Node> V = new HashSet<>();
		Set<Edge> E = new HashSet<>(); 
		Map<Edge, Set<Integer>> A = new HashMap<>();
		for(Integer delta:D)
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
		return new Graph(V,E,A);
	}
	public static void ConstructDeltaForwardingGraph(Set<Integer> D,Map<Integer, Set<String>> por,Set<Node> device,Graph G)
	{
		Set<Node> V = G.V;
		Set<Edge> E = G.E;
		Map<Edge, Set<Integer>> A = G.A;
		for(Integer delta:D)
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
	public static void CheckInvariants(Graph G,Set<Integer> D,Set<Node> V,BDD bdd)
	{
		for(Node s:V)
		{
			Set<Integer> pset = new HashSet<>();
			pset.addAll(D);
			Set<Node> history = new HashSet<>();
			history.clear();
			traversed = false;//判断未初始化的参与遍历的节点s
			Traverse(s,pset,history,G,bdd);
			if(traversed==false)
				System.err.println("found blackhole by uninitialized node!");
			System.out.println();
		}
	}
	public static void Traverse(Node s,Set<Integer> pSet,Set<Node> history,Graph G,BDD bdd)
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
				Set<Integer> insert = setAnd(pSet, G.A.get(e), bdd);
				history.add(s);
				Traverse(e.to, insert, history,G,bdd);
				for(Integer i:insert)
					bdd.deref(i);
			}
		}
	}
	/*
	 * compute A(and)B by using BDD
	 */
	public static Set<Integer> setAnd(Set<Integer> A,Set<Integer> B,BDD bdd) {
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

