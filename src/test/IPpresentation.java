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
	public static void test1() {
		BDD bdd = new BDD(1000,100);
		final int N=32;
		bdd.createVars(N);
		
		int port1=2,port2=3;
		String m1="11000000";
		String m2="1100000010100000";
		String hit0="00000000000000000000000000000000";
		int nexthop=3;
		int pr1=1,pr2=5;
		
		Rule ip1 = new Rule(port1,m1,m1,nexthop,pr1,bdd);
		ip1.printer();
		
		Rule ip2 = new Rule(port2, m2, hit0, nexthop, pr2,bdd);
		ip2.printer();
		
		ArrayList<Rule> rules = new ArrayList<>(); //现有规则链表
		rules.add(ip1);
		
		ArrayList<Change> changes = new ArrayList<>();//算法1的变化
		Identify(ip2,rules,bdd,changes);
//		Identify(ip2,rules,bdd,changes);
		for(int i=0;i<rules.size();i++)
		{
			rules.get(i).printer();
		}
		bdd.cleanup();
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
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
		
		Map<Integer, ArrayList<Integer>> por = new HashMap<Integer, ArrayList<Integer>>();//predicate->port
		Map<Integer, ArrayList<Integer>> pre;//port->predicate
		ArrayList<Integer> brr = new ArrayList<>();
		brr.add(a);
		for(int i=0;i<7;i++)
		{
//			ar[i]=t2;
//			System.out.print(ar[i]+"  ");
//			bdd.printSet(ar[i]);
			por.put(ar[i], brr);
			
		}
		for(int i=0;i<por.get(ar[2]).size();i++)
			System.out.println(por.get(ar[2]).get(i));
		
		Iterator<Integer> iterator = por.keySet().iterator();
		while(iterator.hasNext())
		{
			Integer countInteger =iterator.next();
			bdd.printSet(countInteger);
		}
		bdd.cleanup();
//		rules.add(ip1);
//		Rule ip3 =new Rule(rules.get(0));
//		int temp1 = bdd.ref(bdd.minterm(ip1.getMatch()));
//		int temp2 = bdd.ref(bdd.minterm(ip2.getMatch()));
//		int ip1_match = bdd.ref(bdd.and(temp1, temp2));
		
//		System.out.println(ip1.getMatch().toString()+"\n"+ip2.getMatch().toString());
		
//		bdd.printSet(temp1);
////		bdd.printSet(temp2);
//		bdd.printSet(ip1_match);
//		bdd.deref(temp1);
//		bdd.deref(temp2);
//		bdd.deref(ip1_match);
		
	}
	public static void  Identify(Rule r,ArrayList<Rule> R,BDD bdd,ArrayList<Change> C) {
		r.changeHit(r.getMatch());
		int and = bdd.ref(bdd.minterm(""));
//		int hit = bdd.ref(bdd.minterm(r.getMatch()));
		for(int i=0;i<R.size();i++)
		{
//			int temphit = bdd.ref(bdd.minterm(R.rules[i].getMatch()));
			and = bdd.ref(bdd.and(r.b_hit, R.get(i).b_hit));
			if(R.get(i).getPrior()>r.getPrior()&&and!=0)
			{
				r.b_hit=bdd.ref(bdd.and(r.b_hit, R.get(i).b_hit));
			}
			if(R.get(i).getPrior()<r.getPrior()&&and!=0) {
				if(r.getport()!=R.get(i).getport())
				{
					System.out.println("转移成功！");
					Change change = new Change(and, R.get(i).getport(),r.getport(), bdd);
//					change.printChange();
					C.add(change);
				}
				R.get(i).b_hit=bdd.ref(bdd.and(r.b_hit, R.get(i).b_hit));
//				R.rules[i].changeHit(and);
			}
		}
		bdd.deref(and);
		System.out.println("添加元素成功");
		R.add(r);
	}
	public static void Split()
	{
		
	}
	public static void Transfer()
	{
		
	}
	public static void Merge()
	{
		
	}
	public static void Update(int[] P,ArrayList<Change> C,BDD bdd,Map<Integer, ArrayList<Integer>> pre,Map<Integer, ArrayList<Integer>> por) {
		for(int i=0;i<C.size();i++)
		{
			for(int j=0;j<pre.get(C.get(i).from).size();j++)
			{
				int and=bdd.ref(bdd.and(P[j],C.get(i).insertion));
				if(and!=0)
					if(and!=P[j])
						Split();
					Transfer();
					if(true)//
						Merge();
					int no = bdd.ref(bdd.not(P[i]));
					C.get(i).insertion=bdd.and(C.get(i).insertion, no);
					bdd.deref(no);
					
			}
		}
	}

}
