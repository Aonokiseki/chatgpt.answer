package execrise;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class NFA {
	
	public static void main(String[] args) {
		String regexp = "(A|B|(EE|F))D";
		String text = "EED";
		NFA nfa = new NFA(regexp);
		boolean result = nfa.recognizes(text);
		System.out.println(result);
	}
	
	private char[] re;
	private Digraph G;
	private int M;
	
	public NFA(String regexp) {
		Deque<Integer> ops = new LinkedList<Integer>();
		re = regexp.toCharArray();
		M = re.length;
		G = new Digraph(M+1);
		
		for(int i = 0; i < M; i++) {
			int lp = i;
			if(re[i] == '(' || re[i] == '|') {
				ops.push(i);
			}else if(re[i] == ')') {
//				int or = ops.pop();
//				if(re[or] == '|') {
//					lp = ops.pop();
//					G.addEdge(lp, or + 1);
//					G.addEdge(or, i);
//				}
//				else {
//					lp = or;
//				}
				List<Integer> orLocs = new ArrayList<Integer>();
				while(re[ops.peekFirst()] != '(') {
					int or = ops.pop();
					System.out.println("or = " + or);
					if(re[or] == '|') {
						System.out.println(String.format("G.addEdge(or=%d, i=%d)", or, i));
						G.addEdge(or, i);
						orLocs.add(or);
					}else {
						System.out.println(String.format("re[or]!='|', let lp = or, which or is %d", or));
						lp = or;
					}
				}
				int leftPar = ops.pop();
				for(int orLoc : orLocs) {
					System.out.println(String.format("G.addEdge(leftPar=%d, orLoc+1=%d)", leftPar, orLoc+1));
					G.addEdge(leftPar, orLoc+1);
				}
			}
			if(i < M - 1 && re[i + 1] == '*') {
				G.addEdge(lp, i+1);
				G.addEdge(i+1, lp);
			}
			if(i < M - 1 && re[i] == '.') {
				G.addEdge(lp, i);
			}
			if(re[i] == '(' || re[i] == '*' || re[i] == ')')
				G.addEdge(i, i + 1);
		}
		
		for(int i = 0; i < G.V(); i++) {
			System.out.println(String.format("G.adj(%d)=%s", i, G.adj(i)));
		}
	}
	
	public boolean recognizes(String txt) {
		List<Integer> pc = new ArrayList<Integer>();
		DirectedDFS dfs = new DirectedDFS(G, 0);
		for(int v = 0; v < G.V(); v++)
			if(dfs.marked(v))
				pc.add(v);
		for(int i = 0; i < txt.length(); i++) {
			List<Integer> match = new ArrayList<Integer>();
			for(int v : pc) {
				if (v < M)
					if(re[v] == txt.charAt(i) || re[v] == '.')
						match.add(v + 1);
			}
			pc = new ArrayList<Integer>();
			dfs = new DirectedDFS(G, match);
			for(int v = 0; v < G.V(); v++)
				if(dfs.marked(v))
					pc.add(v);
		}
		for (int v: pc)
			if(v == M)
				return true;
		return false;
	}
}

class DirectedDFS {
	private boolean[] marked;
	
	public DirectedDFS(Digraph G, int s) {
		marked = new boolean[G.V()];
		dfs(G, s);
	}
	
	public DirectedDFS(Digraph G, Iterable<Integer> sources) {
		marked = new boolean[G.V()];
		for(int s : sources)
			if(!marked[s])
				dfs(G, s);
	}
	
	private void dfs(Digraph G, int v) {
		marked[v] = true;
		for(int w: G.adj(v)) {
			if(!marked[w])
				dfs(G, w);
		}
	}
	
	public boolean marked(int v) {
		return marked[v];
	}
}


class Digraph {
	private final int V;
	private int E;
	private List<Integer>[] adj;
	
	public Digraph(int V) {
		this.V = V;
		this.E = 0;
		adj = (List<Integer>[])new List[V];
		for(int v = 0; v < V; v++) {
			adj[v] = new ArrayList<Integer>();
		}
	}
	
	public int V() {
		return V;
	}
	
	public int E() {
		return E;
	}
	
	public void addEdge(int v, int w) {
		adj[v].add(w);
		E++;
	}
	
	public Iterable<Integer> adj(int v){
		return adj[v];
	}
	
	public Digraph reverse() {
		Digraph R = new Digraph(V);
		for(int v = 0; v < V; v++) {
			for(int w : adj(v)) {
				R.addEdge(w, v);
			}
		}
		return R;
	}
}
