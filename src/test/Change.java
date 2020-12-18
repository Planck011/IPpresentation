package test;

import jdd.bdd.BDD;

public class Change {
	public int from;
	public int to;
	public BDD bdd;
	public int insertion;
	public Change(int in,int p1,int p2,BDD bdd){
		this.from=p1;
		this.to=p2;
		this.bdd=bdd;
		int temp=bdd.ref(bdd.minterm(""));
		this.insertion=bdd.ref(bdd.and(temp, in));
		bdd.deref(temp);
	}
	public void printChange() {
		System.out.print("bdd:");
		bdd.printSet(insertion);
		System.out.println("from:"+from+"  to:"+to);
	}
}
