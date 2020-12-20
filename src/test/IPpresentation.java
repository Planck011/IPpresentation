package test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sun.org.apache.bcel.internal.generic.GETSTATIC;
import com.sun.org.apache.xerces.internal.impl.dv.dtd.IDDatatypeValidator;
import com.sun.org.apache.xerces.internal.impl.dv.xs.IDDV;
//import com.sun.org.apache.xpath.internal.operations.String;
import com.sun.tools.classfile.Opcode.Set;
import java.lang.*;
import jdd.bdd.*;
import jdd.bdd.sets.*;
import jdk.javadoc.internal.doclets.formats.html.resources.standard_zh_CN;
import test.*;
public class IPpresentation {
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<Integer> port = new ArrayList<>();
		port.add(1);
		port.add(2);
		port.add(3);
		port.add(4);
		port.add(5);
		port.add(6);
		port.add(7);
		ArrayList<Integer> predicate = new ArrayList<>();
		final Integer tr = 1;//TRUE
		predicate.add(tr);
		Map<Integer, ArrayList<Integer>> por = new HashMap<Integer, ArrayList<Integer>>();//predicate->port
		Map<Integer, ArrayList<Integer>> pre = new HashMap<Integer, ArrayList<Integer>>();;//port->predicate
		ArrayList<ArrayList<Integer>> pre_list = new ArrayList<>();
		ArrayList<ArrayList<Integer>> port_list = new ArrayList<>();
		for(int i=0;i<7;i++)
		{
			pre_list.add(predicate);
		}
		port_list.add(port);
		por.put(tr, port_list.get(0));
		for(int i=0;i<7;i++)
		{
			
			pre.put(port.get(i),(ArrayList<Integer>)predicate.clone());//给por和pre的每个list都要分别分配空间
			
		}
		
//		mapPrint(pre);
//		mapPrint(por);
		test1(pre,por);
//		mapPrint(pre);
//		mapPrint(por);
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
		
		
		ArrayList<Integer> D = Update(changes, bdd, pre, por);
//		for(int i=0;i<D.size();i++)
//		{
//			System.out.print(D.get(i));
//		}
		System.out.println("D="+D);
		
		bdd.cleanup();
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
		Map<Integer, ArrayList<Integer>> por = new HashMap<Integer, ArrayList<Integer>>();//predicate->port  谓词端口映射
		Map<Integer, ArrayList<Integer>> pre = new HashMap<Integer, ArrayList<Integer>>();;//port->predicate 端口谓词银蛇
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
				R.get(i).b_hit=bdd.ref(bdd.and(bdd.not(r.b_hit), R.get(i).b_hit));//重写为r'的击中域赋值
//				R.rules[i].changeHit(and);
			}
		}
		bdd.deref(and);
		R.add(r);
		System.out.println("添加规则成功");
	}
	@SuppressWarnings("unchecked")
	public static void Split(int p,int p1,int p2,Map<Integer, ArrayList<Integer>> pre,Map<Integer, ArrayList<Integer>> por,ArrayList<Integer> D)
	{
		int i,j;
		for(i=0;i<por.get(p).size();i++)
		{
			int temp_port=por.get(p).get(i);
			pre.get(temp_port).add(p1);
			pre.get(temp_port).add(p2);
			for(j=0;j<pre.get(temp_port).size();j++)
			{
				if(pre.get(temp_port).get(j)==p)
					pre.get(temp_port).remove(j);
			}
			
		}
		por.put(p1, (ArrayList<Integer>)por.get(p).clone());//深拷贝分配空间
		por.put(p2, (ArrayList<Integer>)por.get(p).clone());
		por.remove(p);//此条不清楚是否需要删除
		//D
		for(i=0;i<D.size();i++)
		{
			if(p==D.get(i))
			{
				D.remove(i);
				D.add(p1);
				D.add(p2);
				break;
			}
		}
		
	}
	public static void Transfer(int p,int from ,int to,Map<Integer, ArrayList<Integer>> pre,Map<Integer, ArrayList<Integer>> por,ArrayList<Integer> D)
	{//118 2 3
		for(int j=0;j<pre.get(from).size();j++)
		{
			if(pre.get(from).get(j)==p)
				pre.get(from).remove(j);
		}
		pre.get(to).add(p);
		por.get(p).add(to);
		for(int j=0;j<por.get(p).size();j++)
		{
			if(por.get(p).get(j)==from)
				por.get(p).remove(j);
		}
		//D
		D.add(p);
	}
	@SuppressWarnings("unchecked")
	public static void Merge(int p1,int p2,int p,Map<Integer, ArrayList<Integer>> pre,Map<Integer, ArrayList<Integer>> por,ArrayList<Integer> D)
	{
		int i,j,temp_port;
		for(i=0;i<por.get(p1).size();i++)
		{
			temp_port=por.get(p1).get(i);
			for(int j=0;j<pre.get(temp_port).size();j++)
			{
				if(pre.get(temp_port).get(j)==p1||pre.get(temp_port).get(j)==p2)
					pre.get(temp_port).remove(j);
			}
			pre.get(temp_port).add(p);
		}
		por.put(p, (ArrayList<Integer>)por.get(p1).clone());//unclear
		for(i=0;i<D.size();i++)                 //Unclear
		{
			for(j=0;j<D.size();j++)
			{
				if(D.get(i)==p1&&D.get(j)==p2)
				{
					D.remove(i);
					D.remove(j);
					D.add(p);
					break;
				}
			}
			
		}
	}
	public static ArrayList<Integer> Update(ArrayList<Change> C,BDD bdd,Map<Integer, ArrayList<Integer>> pre,Map<Integer, ArrayList<Integer>> por) {
		ArrayList<Integer> D = new ArrayList<>();
		for(int i=0;i<C.size();i++)//for each (insertion,from,to)∈C
		{
			for(int j=0;j<pre.get(C.get(i).from).size();j++)//for each p∈pre(from)
			{
				int p = pre.get(C.get(i).from).get(j);
				int and=bdd.ref(bdd.and(p,C.get(i).insertion));//p∧insertion
				if(and!=0)//非空
					if(and!=p)
						Split(p,bdd.and(p, C.get(i).insertion),bdd.and(p, bdd.not(C.get(i).insertion)),pre,por,D);
					Transfer(bdd.and(p, C.get(i).insertion),C.get(i).from,C.get(i).to,pre,por,D);
					for(int c=0;c<pre.get(C.get(i).from).size();c++)//∃p'!=p,por(p'）==por(p)
					{
						int pp=pre.get(C.get(i).from).get(c);
						if(pp==p)
						{
							continue;
						}
						else {	
							if(por.get(pp).equals(por.get(p)))
							{
								Merge(p, pp, bdd.or(p, pp), pre, por, D);
							}
						}
					}
					int no = bdd.ref(bdd.not(p));
					C.get(i).insertion=bdd.and(C.get(i).insertion, no);//insertion<---insertion∧¬p
					bdd.deref(no);
					
			}
		}
		return D;
	}

}
