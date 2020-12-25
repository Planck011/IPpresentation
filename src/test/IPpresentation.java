package test;

import java.util.*;
import java.util.Map.Entry;


import java.io.*;
import java.lang.*;
import jdd.bdd.*;

public class IPpresentation {
	public static void main(String[] args) throws ConcurrentModificationException {
		/*
		 * 由于没有构建网络拓扑和元素模块，本次实验仅针对一台设备中的各个端口。
		 * 待插入规则有四条，对应IP范围为（IP/mask)192.0.0.0/8,192.168.0.0/16,198.29.128.160/28,198.39.128/18
		 * 规则已经事先计算好二进制表示的匹配域，并使用BDD库转换成bdd表示，详见Rule.java构造函数;
		 * 使用Map构造端口谓词映射，Map<Integer,Set<Interger>>,端口号采用int类型;
		 * 算法1对应函数Identify()，返回变化Set<Change> changes;
		 * 算法2对应函数Update()，返回转移谓词Set<Integer> D;
		 * 算法3对应UpdateRewrite(),由于论文信息不全，以及对重写元素还不太懂，相关函数未实现完整;
		 * 算法4对应ConstructDeltaForwardingGraph()，根据算法2返回的转移谓词集D，以及整个网络节点信息和端口谓词映射生成并返回转发图Graph G;
		 * 算法5对应CheckInvariants(),根据算法4返回的转发图G以及初始启动节点集V，遍历转发图，验证不变量，判断环路黑洞。
		 * 算法12通过自造数据进行了验证，目前没有问题，除了代码中的不好的操作导致捕获到ConcurrentModificationException异常，暂时影响不大，尚未修改。
		 * 算法4发现一个问题，在构造转发图构造的是有向图，这样会导致生成的转发图边集里会有两条方向相反的边，而这边在进入算法5遍历是会产生loop，目前没有想到好的解决办法。
		 */
		// TODO Auto-generated method stub
		Set<String> port = new HashSet<>();
		port.add("1");
		port.add("2");
		port.add("3");
		port.add("4");
		port.add("5");
		port.add("6");
		port.add("7");
		port.add("8");
		Set<Integer> predicate = new HashSet<>();
		final Integer tr = 1;//TRUE
		predicate.add(tr);
		Map<Integer, Set<String>> por = new HashMap<Integer, Set<String>>();//predicate->port
		Map<String, Set<Integer>> pre = new HashMap<String, Set<Integer>>();;//port->predicate
		por.put(tr, port);
		String[] port_st = port.toArray(new String[port.size()]);
		for(int i=0;i<8;i++)
		{
			Set<Integer> temp = new HashSet<>();
			temp.addAll(predicate);
			pre.put(port_st[i],temp);
		}
		
		Set<Node> device = initDevice();//get network topo
		for (Node node : device) {
			System.out.println(node.str);
		}
		BDD bdd = new BDD(1000,100);
		bdd.createVars(32);//init BDD 32bits
		ArrayList<Rule> rules = initRule(bdd);
		ArrayList<Rule> nowrules = new ArrayList<>();
		int count=1;
		for (Rule rule : rules) { //for every one rule check
			System.out.println("-------------------------------------------------");
			System.out.println("intert "+count+" rules");
			count++;
			Set<Change> changes = new HashSet<>();
			Set<Integer> D = new HashSet<>();
			Identify(rule, nowrules, bdd, changes);
			D = Update(changes, bdd, pre, por);
			System.out.println("D="+D);
			System.out.println(rule.getHit());
			System.out.println("exit "+nowrules.size()+" rules");
			Graph G = ConstructDeltaForwardingGraph(D, por, device);
			G.printGraph();
			Set<Node> V = new HashSet<>();
			V.addAll(device);
			Node s1 = V.toArray(new Node[device.size()])[1];
			V.clear();
			V.add(s1);
			CheckInvariants(G, D, V);
			mapPrint(pre);//print map(port-->predicate)
			System.out.println("-------------------------------------------------");
		}
		
		
		
	}
	public static ArrayList<Rule> initRule(BDD bdd)
	{
		ArrayList<Rule> rules = new ArrayList<>();
		try {
			
			BufferedReader in = new BufferedReader((new FileReader("C:\\Users\\pyh1343122828\\Desktop\\Java\\IPpresentation\\src\\rules.txt")));
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
		}
		return rules;
	}
	public static Set<Node> initDevice()
	{
		Set<Node> device = new HashSet<>();
		try {
			BufferedReader in = new BufferedReader(new FileReader("C:\\Users\\pyh1343122828\\Desktop\\Java\\IPpresentation\\src\\topo.txt"));
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
				Node s = new Node(name,port_list,connect);
				device.add(s);
				
			}
//			for(Node d:device)
//			{
//				System.out.println(d.str);
//			}
			in.close();
			
		} catch (IOException  e) {
			// TODO: handle exception
		}
		return device;
	}
	public static void test1(Map<String, Set<Integer>> pre,Map<Integer, Set<String>> por,Set<Node> device) {
		BDD bdd = new BDD(1000,100);
		final int N=32;
		bdd.createVars(N);
		
		String port1="2",port2="3",port3="4",port4="5";
		String m1="11000000";//192.*.*.*
		String m2="1100000010101000",m3="1100011000100111100000001010",m4="110001100010011110";//192.168.*.*
		String hit0="00000000000000000000000000000000";//0.0.0.0
		int nexthop=3;
		int pr1=1,pr2=5,pr3=4,pr4=3;
		
		Rule ip1 = new Rule(port1,m1,m1,nexthop,pr1,bdd);//Create one Rule
		
		Rule ip2 = new Rule(port2, m2, hit0, nexthop, pr2,bdd);//next Rule ip2
		Rule ip3 = new Rule(port3,m3,hit0,nexthop,pr3,bdd);
		Rule ip4 = new Rule(port4,m4,hit0,nexthop,pr4,bdd);
		ArrayList<Rule> rules = new ArrayList<>(); //Rule set
		Set<Change> changes = new HashSet<>();//some change (predicate,from,to)
		Identify(ip1,rules,bdd,changes);//insert one rule,identify changes
		Identify(ip2,rules,bdd,changes);
		Identify(ip4,rules,bdd,changes);
		Identify(ip3,rules,bdd,changes);
		for(int i=0;i<rules.size();i++)
		{
			rules.get(i).printer();//print rule set
		}
		for(Change change:changes)
		{
			change.printChange();
		}
		try {
			Set<Integer> D = Update(changes, bdd, pre, por);
			System.out.println("D="+D);
			for(Integer d:D)
				bdd.printSet(d);
			mapPrint(por);
			mapPrint(pre);
			for(Node d:device)
				System.out.println(d.str);
			Graph G = ConstructDeltaForwardingGraph(D, por, device);
			Set<Node> V = new HashSet<>();
			V.addAll(device);
			Node s1 = V.toArray(new Node[device.size()])[0];
			V.clear();
			V.add(s1);
			G.printGraph();//print the graph
			CheckInvariants(G, D,V);
		} catch (ConcurrentModificationException e) {
			// TODO: handle exception
			System.out.println("dadda");
		}finally {
			
		}
		
		
		bdd.cleanup();
	}	
	public static <E,T> void mapPrint(Map<E, Set<T>> map) {
		for(Entry<E, Set<T>> entry:map.entrySet())
		{
			System.out.println("key="+entry.getKey()+"  values="+entry.getValue());
		}
	}
	public static void  Identify(Rule r,ArrayList<Rule> R,BDD bdd,Set<Change> C) {
		r.changeHit(r.getMatch());//灏唕鐨勫尮閰嶅煙璧嬬粰鍑讳腑鍩� r.hit <-- r.match;
		int and = bdd.ref(bdd.minterm(""));
//		int hit = bdd.ref(bdd.minterm(r.getMatch()));
		for(int i=0;i<R.size();i++)
		{
//			int temphit = bdd.ref(bdd.minterm(R.rules[i].getMatch()));
			and = bdd.ref(bdd.and(r.b_hit, R.get(i).b_hit));//r'.hit(and)r.hit,涓よ鍒欑殑bdd鍋歛nd杩愮畻
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
				R.get(i).b_hit=bdd.ref(bdd.and(bdd.not(r.b_hit), R.get(i).b_hit));//閲嶅啓涓簉'鐨勫嚮涓煙璧嬪��
//				R.rules[i].changeHit(and);
			}
		}
		bdd.deref(and);
		R.add(r);
		System.out.println("insert one rule!");
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
		por.remove(p);//姝ゆ潯涓嶆竻妤氭槸鍚﹂渶瑕佸垹闄� 
		//D
		if(D.contains(p))
		{
			D.add(p1);
			D.add(p2);
			D.remove(p);
		}		
	}
	public static void Transfer(int p,String from ,String to,Map<String, Set<Integer>> pre,Map<Integer, Set<String>> por,Set<Integer> D)
	{//118 2 3
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
	public static Set<Integer> Update(Set<Change> C,BDD bdd,Map<String, Set<Integer>> pre,Map<Integer, Set<String>> por) {
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
		}
		} catch (ConcurrentModificationException e) {
			// TODO: handle exception
			System.out.println("catch ConcurrentModificationException!");
		}
		return D;
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
		Map<Edge, Set<Integer>> A = new HashMap<>();//A闇�瑕佸垵濮嬪寲鑾峰彇鍚勮竟淇℃伅
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
								Edge e1 =new Edge(s1, s2);
								Edge e2 = new Edge(s2,s1);
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
	public static void CheckInvariants(Graph G,Set<Integer> D,Set<Node> V)//妫�鏌ヤ笉鍙橀噺
	{
		for(Node s:V)
		{
			Set<Integer> pset = new HashSet<>();
			pset.addAll(D);
			Set<Node> history = new HashSet<>();
			history.clear();
			Traverse(s,pset,history,G);
		}
	}
	public static void Traverse(Node s,Set<Integer> pSet,Set<Node> history,Graph G)
	{
		if(pSet.isEmpty())
			return;
		if(history.contains(s))
		{
			System.out.println("found loop!");
			return;
		}
		//
		for (Edge e : G.E) {
			if(e.from.equals(s))
			{
				if(e.to.str.equals("default"))
				{
					System.out.println("found blackhole!");
					return;
				}
				Set<Integer> insert = new HashSet<>();
				insert.clear();
				insert.addAll(pSet);
				insert.retainAll(G.A.get(e));
				history.add(s);
				Traverse(e.to, insert, history,G);
			}
		}
	}
}
