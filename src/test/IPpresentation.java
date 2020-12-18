package test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sun.org.apache.bcel.internal.generic.GETSTATIC;
import com.sun.org.apache.xerces.internal.impl.dv.dtd.IDDatatypeValidator;
import com.sun.org.apache.xerces.internal.impl.dv.xs.IDDV;
import com.sun.org.apache.xpath.internal.operations.String;
import com.sun.tools.classfile.Opcode.Set;

import jdd.bdd.*;
import jdd.bdd.sets.*;
import jdk.javadoc.internal.doclets.formats.html.resources.standard_zh_CN;
import test.*;
public class IPpresentation {
	public static int copy(int s,BDD bdd) {
		int tr = bdd.ref(bdd.minterm(""));
		int ss = bdd.ref(bdd.and(tr, s));
		bdd.deref(tr);
		return ss;
	}
	public static void test1(Map<Integer, ArrayList<Integer>> pre,Map<Integer, ArrayList<Integer>> por) {
		BDD bdd = new BDD(1000,100);
		final int N=32;
		bdd.createVars(N);
		
		int port1=2,port2=3;
		String m1="11000000";//192.*.*.*
		String m2="1100000010100000";//192.168.*.*
		String hit0="00000000000000000000000000000000";//0.0.0.0
		int nexthop=3;
		int pr1=1,pr2=5;
		
		Rule ip1 = new Rule(port1,m1,m1,nexthop,pr1,bdd);//创建一条规则
//		ip1.printer();
		
		Rule ip2 = new Rule(port2, m2, hit0, nexthop, pr2,bdd);//待加入的规则ip2
		ip2.printer();
		
		ArrayList<Rule> rules = new ArrayList<>(); //现有规则链表
//		rules.add(ip1);//将ip1加入链表，表示目前已有的规则
		
		ArrayList<Change> changes = new ArrayList<>();//算法1的输出变化，三元组（predicate，from，to）
		Identify(ip1,rules,bdd,changes);//算法1，加入ip2后识别出变化
		Identify(ip2,rules,bdd,changes);
		for(int i=0;i<rules.size();i++)
		{
			rules.get(i).printer();//打印规则链表中已有规则的信息
		}
		
		
		Update(changes, bdd, pre, por);
		
		
		bdd.cleanup();
	}
	public static void init() {
		Integer[] port = new Integer[]{1,2,3,4,5,6,7};
		final Integer tr = 1;//TRUE
		Map<Integer, ArrayList<Integer>> por = new HashMap<Integer, ArrayList<Integer>>();//predicate->port
		Map<Integer, ArrayList<Integer>> pre = new HashMap<Integer, ArrayList<Integer>>();;//port->predicate
		ArrayList<Integer> pre_list = new ArrayList<>();
		pre_list.add(tr);
		ArrayList<Integer> port_list = new ArrayList<>();
		for(int i=0;i<7;i++)
		{
			port_list.add(port[i]);
		};
		por.put(tr, port_list);
		for(int i=0;i<7;i++)
		{
			pre.put(port[i],pre_list);
			
		}
	}
	public static void test2() {
		BDD bdd =new BDD(1);
		bdd.createVars(5);
		
		Integer a=15;
		
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
		Map<Integer, ArrayList<Integer>> por = new HashMap<Integer, ArrayList<Integer>>();//predicate->port
		Map<Integer, ArrayList<Integer>> pre = new HashMap<Integer, ArrayList<Integer>>();;//port->predicate
		ArrayList<Integer> pre_list = new ArrayList<>();
		pre_list.add(tr);
		ArrayList<Integer> port_list = new ArrayList<>();
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
			System.out.println(por.get(1));//输出key对应的键值
		
		Iterator<Integer> iterator = por.keySet().iterator();//por的key迭代器
		while(iterator.hasNext())//遍历输出key
		{
			Integer countInteger =iterator.next();
			bdd.printSet(countInteger);
		}
		bdd.cleanup();
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		
		Integer[] port = new Integer[]{1,2,3,4,5,6,7};
		final Integer tr = 1;//TRUE
		Map<Integer, ArrayList<Integer>> por = new HashMap<Integer, ArrayList<Integer>>();//predicate->port
		Map<Integer, ArrayList<Integer>> pre = new HashMap<Integer, ArrayList<Integer>>();;//port->predicate
		ArrayList<Integer> pre_list = new ArrayList<>();
		pre_list.add(tr);
		ArrayList<Integer> port_list = new ArrayList<>();
		for(int i=0;i<7;i++)
		{
			port_list.add(port[i]);
		}
		por.put(tr, port_list);
		for(int i=0;i<7;i++)
		{
//			pre.put(port[i],);//给por和pre的每个list都要分别分配空间
			
		}
		mapPrint(pre);
		mapPrint(por);
		test1(pre,por);
		mapPrint(pre);
		mapPrint(por);
	}
	public static void mapPrint(Map<Integer, ArrayList<Integer>> map) {
		for(Map.Entry<Integer, ArrayList<Integer>> entry:map.entrySet())
		{
			System.out.println("key="+entry.getKey()+"  values="+entry.getValue());
		}
	}
	public static void  Identify(Rule r,ArrayList<Rule> R,BDD bdd,ArrayList<Change> C) {
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
					Change change = new Change(and, R.get(i).getport(),r.getport(), bdd);//创建新变化（predicate，from，to）
//					change.printChange();
					C.add(change);//加入
					System.out.println("识别成功！");
				}
				R.get(i).b_hit=bdd.ref(bdd.and(r.b_hit, R.get(i).b_hit));//重写为r'的击中域赋值
//				R.rules[i].changeHit(and);
			}
		}
		bdd.deref(and);
		R.add(r);
		System.out.println("添加规则成功");
	}
	public static void Split(int p,int p1,int p2,Map<Integer, ArrayList<Integer>> pre,Map<Integer, ArrayList<Integer>> por)
	{
		for(int i=0;i<por.get(p).size();i++)
		{
			int temp_port=por.get(p).get(i);
			pre.get(temp_port).add(p1);
			pre.get(temp_port).add(p2);
			for(int j=0;j<pre.get(temp_port).size();j++)
			{
				if(pre.get(temp_port).get(j)==p)
					pre.get(temp_port).remove(j);
			}
			
		}
		por.put(p1, por.get(p));
		por.put(p2, por.get(p));
		por.remove(p);
		
	}
	public static void Transfer()
	{
		
	}
	public static void Merge()
	{
		
	}
	public static void Update(ArrayList<Change> C,BDD bdd,Map<Integer, ArrayList<Integer>> pre,Map<Integer, ArrayList<Integer>> por) {
		for(int i=0;i<C.size();i++)
		{
			for(int j=0;j<pre.get(C.get(i).from).size();j++)
			{
				Integer p = pre.get(C.get(i).from).get(j);
				int and=bdd.ref(bdd.and(p,C.get(i).insertion));
				if(and!=0)
					if(and!=p)
						Split(p,bdd.and(p, C.get(i).insertion),bdd.and(p, bdd.not(C.get(i).insertion)),pre,por);
//					Transfer();
//					if(true)//
//						Merge();
					int no = bdd.ref(bdd.not(p));
					C.get(i).insertion=bdd.and(C.get(i).insertion, no);
					bdd.deref(no);
					
			}
		}
	}

}
