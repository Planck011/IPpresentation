package test;

import java.util.*;
import java.util.Map.Entry;
import java.lang.*;
import jdd.bdd.*;
public class IPpresentation {
	public static void main(String[] args) throws ConcurrentModificationException {
		/*
		 * 由于没有构建网络拓扑和元素模块，本次实验仅针对一台设备中的各个端口。
		 * 待插入规则有四条，对应IP范围为（IP/mask)192.0.0.0/8,192.168.0.0/16,198.29.128.160/28,198.39.128/18
		 * 规则已经事先计算好二进制表示的匹配域，并使用BDD库转换成bdd表示，详见Rule.java构造函数;
		 * 使用Map构造端口谓词映射，Map<Integer,Set<Interger>>,端口号采用int类型;
		 * 算法1对应函数Identity()，返回变化Set<Change> changes;
		 * 算法2对应函数Update()，返回转移谓词Set<Integer> D;
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
		Set<Integer> predicate = new HashSet<>();
		final Integer tr = 1;//TRUE
		predicate.add(tr);
		Map<Integer, Set<String>> por = new HashMap<Integer, Set<String>>();//predicate->port
		Map<String, Set<Integer>> pre = new HashMap<String, Set<Integer>>();;//port->predicate
		Set<Set<Integer>> pre_list = new HashSet<>();
		Set<Set<String>> port_list = new HashSet<>();
		for(int i=0;i<7;i++)
		{
			pre_list.add(predicate);
		}
		port_list.add(port);
		por.put(tr, port);
		String[] port_st = port.toArray(new String[port.size()]);
		for(int i=0;i<7;i++)
		{
			Set<Integer> temp = new HashSet<>();
			temp.addAll(predicate);
			pre.put(port_st[i],temp);//给por和pre的每个list都要分别分配空间
		}
		
//		mapPrint(pre);
//		mapPrint(por);
		try {
			test1(pre,por);
		} catch (ConcurrentModificationException e) {
			// TODO: handle exception 
			//ignore
			System.out.println("catch ConcurrentModificationException！");
		}
		
//		mapPrint(pre);
		mapPrint(por);
	}	
	public static void test1(Map<String, Set<Integer>> pre,Map<Integer, Set<String>> por) {
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
//		ip1.printer();
		
		Rule ip2 = new Rule(port2, m2, hit0, nexthop, pr2,bdd);//next Rule ip2
//		ip2.printer();
		Rule ip3 = new Rule(port3,m3,hit0,nexthop,pr3,bdd);
		Rule ip4 = new Rule(port4,m4,hit0,nexthop,pr4,bdd);
		ArrayList<Rule> rules = new ArrayList<>(); //Rule set
//		rules.add(ip1);
		
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
		Set<Integer> D = Update(changes, bdd, pre, por);
		System.out.println("D="+D);
		for(Integer d:D)
			bdd.printSet(d);
		
		bdd.cleanup();
	}

	public static void test2() {
		BDD bdd =new BDD(1);
		bdd.createVars(5);
		
		Integer[] ar=new Integer[7];
		 ar[0] = bdd.minterm("00001");
		 ar[1] = bdd.minterm("11000");
		 ar[2] = bdd.minterm("11001");
		 ar[3] = bdd.minterm("11010");
		 ar[4] = bdd.minterm("11100");
		 ar[5] = bdd.minterm("11110");
		 ar[6] = bdd.minterm("1101");
		Integer[] port = new Integer[]{1,2,3,4,5,6,7};
		final Integer tr = 1;//TRUE
		Map<Integer, Set<Integer>> por = new HashMap<Integer, Set<Integer>>();//predicate->port  璋撹瘝绔彛鏄犲皠
		Map<Integer, Set<Integer>> pre = new HashMap<Integer, Set<Integer>>();;//port->predicate 绔彛璋撹瘝閾惰泧
		Set<Integer> pre_list = new HashSet<>();
		pre_list.add(tr);
		Set<Integer> port_list = new HashSet<>();
		for(int i=0;i<7;i++)
		{
			port_list.add(port[i]);
		};
		por.put(tr, port_list);
		for(int i=0;i<7;i++)
		{
			pre.put(port[i],pre_list);
			
		}
		for(int i=0;i<7;i++)
			System.out.println(por.get(1));//print the key value
		
		Iterator<Integer> iterator = por.keySet().iterator();//the iterator of por.key
		while(iterator.hasNext())//Traverse key
		{
			Integer countInteger =iterator.next();
			bdd.printSet(countInteger);
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
		r.changeHit(r.getMatch());//将r的匹配域赋给击中域 r.hit <-- r.match;
		int and = bdd.ref(bdd.minterm(""));
//		int hit = bdd.ref(bdd.minterm(r.getMatch()));
		for(int i=0;i<R.size();i++)
		{
//			int temphit = bdd.ref(bdd.minterm(R.rules[i].getMatch()));
			and = bdd.ref(bdd.and(r.b_hit, R.get(i).b_hit));//r'.hit(and)r.hit,两规则的bdd做and运算
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
					System.out.println("identify success！");
				}
				R.get(i).b_hit=bdd.ref(bdd.and(bdd.not(r.b_hit), R.get(i).b_hit));//重写为r'的击中域赋值
//				R.rules[i].changeHit(and);
			}
		}
		bdd.deref(and);
		R.add(r);
		System.out.println("insert one rule！");
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
		por.remove(p);//此条不清楚是否需要删除 
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

		return D;
	}
	public static Graph ConstructDeltaForwardingGraph(Set<Integer> D,Map<Integer, Set<String>> por,Set<Node> device)
	{
		Set<Node> V = new HashSet<>();
		Set<Edge> E = new HashSet<>(); 
		Map<Edge, Set<Integer>> A = new HashMap<>();//A需要初始化获取各边信息
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
							if(!V.contains(s2))
							{
								V.add(s2);
								Edge e =new Edge(s1, s2);
								if(!E.contains(e))
								{
									A.get(e).clear();
									E.add(e);
								}
								A.get(e).add(delta);
							}
						}
						
					}
				}
			}
		}
		return new Graph(V,E,A);
	}
	public static void CheckInvariants(Graph G,Set<Integer> D,Set<Node> V)
	{
		for(Node s:V)
		{
			Set<Integer> pset = new HashSet<>();
			pset.addAll(D);
			Set<Node> history = new HashSet<>();
			history.clear();
			Traverse(s,pset,history);
		}
	}
	public static void Traverse(Node s,Set<Integer> pSet,Set<Node> history)
	{
		if(pSet.isEmpty())
			return;
		if(history.contains(s))
		{
			System.out.println("found loop！");
			return;
		}
		//
	}
}
