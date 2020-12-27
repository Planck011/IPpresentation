package test;

import jdd.bdd.BDD;

public class Rule {
	private static int idcounter=0;
	private int id;//编号
	private String port;//端口
	private String match; //匹配域名
	private String hit ;//击中域
	private int nexthop;//未启动
	private int prior;//优先级
	public BDD bdd;
	public int b_match;
	public int b_hit;
//	private int lport;
	public Rule(String p,String m,String h,int n,int pr,BDD bdd) {
		this.id=++idcounter;
		this.port=p;
		this.match=m;
		this.hit=h;
		this.nexthop=n;
		this.prior=pr;
		this.bdd=bdd;
		this.b_hit=bdd.ref(bdd.minterm(hit));
		this.b_match=bdd.ref(bdd.minterm(match));
		
	}
	public Rule(Rule r) {
		this.id=++idcounter;
		this.port=r.getport();
		this.match=r.getMatch();
		this.hit=r.getHit();
		this.nexthop=r.getNetxhop();
		this.prior=r.getPrior();
		this.bdd=r.bdd;
		this.b_hit=bdd.ref(bdd.minterm(hit));
		this.b_match=bdd.ref(bdd.minterm(match));
		
	}
	public int getId() {
		return id;
	}
	public String getport(){
		return port;
	}
	public int getNetxhop(){
		return nexthop;
	}
	public int getPrior(){
		return prior;
	}
	public String getMatch(){
		return match;
	}
	public String getHit(){
		return hit;
	}
	public void  changeHit(String h){
		this.hit=h;
		this.b_hit=bdd.ref(bdd.minterm(h));
	}
	public void clean()
	{
		bdd.deref(b_hit);
		bdd.deref(b_match);
	}
	public void printer() {
		System.out.println("rule"+id+"  "+"port:"+port+"  "+"prior:"+prior+"  "+"match(string):"+match);
		System.out.print("match(bdd):");
		bdd.printSet(b_match);
		System.out.println("hit(string):"+hit);
		System.out.print("hit(bdd)");
		bdd.printSet(b_hit);
	}
}
