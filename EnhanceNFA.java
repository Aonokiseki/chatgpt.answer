package execrise;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import utility.DirectedGraph;
import utility.Graph.Arc;
import utility.Graph.DepthFirstPaths;
import utility.Graph.Edge;
import utility.Graph.Vertex;

public class EnhanceNFA {
	
	public static void main(String[] args) {
		String pattern = "((A*B|AC)D)";
		String text = "AABD";
		EnhanceNFA nfa = new EnhanceNFA(pattern);
		boolean result = nfa.recognizes(text);
		System.out.println(result);
	}
	
	private char[] symbols;
	private DirectedGraph<Character, Integer> epsilon;
	
	public EnhanceNFA(String pattern) {
		Deque<Integer> operations = new LinkedList<Integer>();
		symbols = pattern.toCharArray();
		epsilon = new DirectedGraph<Character, Integer>();
		
		for(int i = 0; i < symbols.length + 1; i++)
			epsilon.addVertex(new Vertex<Character>(i, null));
		
		for(int i = 0; i < symbols.length; i++) {
			int leftPointer = i;
			if(symbols[i] == '(' || symbols[i] == '|') {
				operations.push(i);
			}
			else if(symbols[i] == ')') {
				List<Integer> orIndexs = new ArrayList<Integer>();
				while(symbols[operations.peekFirst()] != '(') {
					int orIndex = operations.pop();
					if(symbols[orIndex] == '|') {
						epsilon.addEdge(new Edge<Integer>(orIndex, i));
						orIndexs.add(orIndex);
					}
					else {
						leftPointer = orIndex;
					}
				}
				int leftParenthesesIndex = operations.pop();
				for(int orIndex : orIndexs) {
					epsilon.addEdge(new Edge<Integer>(leftParenthesesIndex, orIndex + 1));
				}
			}
			
			if(i < symbols.length - 1 && symbols[i + 1] == '*') {
				epsilon.addEdge(new Edge<Integer>(leftPointer, i+1));
				epsilon.addEdge(new Edge<Integer>(i+1, leftPointer));
			}
			if(i < symbols.length - 1 && symbols[i] == '.') {
				epsilon.addEdge(new Edge<Integer>(leftPointer, i));
			}
			if(symbols[i] == '(' || symbols[i] == '*' || symbols[i] == ')') {
				epsilon.addEdge(new Edge<Integer>(i, i + 1));
			}
		}
		
		Map<Integer, Set<Arc<Integer>>> adj = epsilon.unmodifiableAdjacency();
		for(Entry<Integer, Set<Arc<Integer>>> entry : adj.entrySet())
			System.out.println(String.format("vertexId=%d, neighbour=%s", entry.getKey(), entry.getValue()));
	}
	
	public boolean recognizes(String txt) {
		List<Integer> pc = new ArrayList<Integer>();
		DepthFirstPaths dfs = new DepthFirstPaths(epsilon, 0);
		int count = epsilon.countOfVertex();
		System.out.println(count);
		for(int v = 0; v < count; v++)
			if(dfs.hasPathTo(v))
				pc.add(v);
		System.out.println("pc="+pc);
		for(int i = 0; i < txt.length(); i++) {
			List<Integer> match = new ArrayList<Integer>();
			for(int v : pc) {
				if (v < symbols.length)
					if(symbols[v] == txt.charAt(i) || symbols[v] == '.')
						match.add(v + 1);
			}
			pc = new ArrayList<Integer>();
			System.out.println(String.format("new DepthFirstPaths(epsilon, %s)", match));
			dfs = new DepthFirstPaths(epsilon, match);
			for(int v = 0; v < count; v++)
				if(dfs.hasPathTo(v))
					pc.add(v);
			System.out.println("pc="+pc);
		}
		for (int v: pc)
			if(v == symbols.length)
				return true;
		return false;
	}
}
