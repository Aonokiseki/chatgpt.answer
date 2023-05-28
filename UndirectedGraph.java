package utility;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
/**
 * 无向图
 *
 * @param <T>
 * @param <E>
 */
public class UndirectedGraph<T1 extends Comparable<T1>, T2 extends Number> extends Graph<T1, T2> {

	public UndirectedGraph(Set<Vertex<T1>> vertexs, Set<Edge<T2>> edges) {
		super(vertexs, edges);
	}
	
	@Override
	public boolean addVertex(Vertex<T1> vertex) {
		if(dictionary.containsKey(vertex.id) || adjacency.containsKey(vertex.id))
			return false;
		dictionary.put(vertex.id, vertex);
		adjacency.put(vertex.id, new HashSet<Arc<T2>>());
		return true;
	}

	@Override
	public boolean addEdge(Edge<T2> edge) {
		if(dictionary == null || dictionary.isEmpty())
			return false;
		int startid = edge.getStartid();
		int endid = edge.getEndid();
		T2 comparableKey = edge.getWeight();
		if(startid == endid)
			return false;
		if(!dictionary.containsKey(startid) || !dictionary.containsKey(endid))
			return false;
		edgeSet.add(edge);
		if(!adjacency.containsKey(startid))
			adjacency.put(startid, new HashSet<Arc<T2>>());
		if(!adjacency.containsKey(endid))
			adjacency.put(endid, new HashSet<Arc<T2>>());
		boolean addTo = adjacency.get(startid).add(new Arc<T2>(endid, comparableKey, startid));
		boolean addFrom = adjacency.get(endid).add(new Arc<T2>(startid, comparableKey, endid));
		if(addTo && addFrom)
			this.countOfArc++;
		return addTo && addFrom;
	}

	@Override
	public String toString() {
		return String.format("{adjacency=%s, inverseAdjacency=%s}", adjacency, inverseAdjacency);
	}
	
	/**
	 * 判断二分图
	 */
	public static class Bipartite{
		private enum Color {
			UNCOLORED, RED, BLACK
		};
		
		private boolean isValid;
		private Map<Integer, Color> colors;
		
		public <T1 extends Comparable<T1>, T2 extends Number> Bipartite(UndirectedGraph<T1, T2> graph){
			if(graph.adjacency == null || graph.adjacency.isEmpty())
				return;
			Set<Integer> vertexIds = graph.adjacency.keySet();
			isValid = true;
			colors = new HashMap<Integer, Color>();
			for(int vertexId : vertexIds)
				colors.put(vertexId, Color.UNCOLORED);
			calculate(graph);
		}
		private <T1 extends Comparable<T1>, T2 extends Number> void calculate(UndirectedGraph<T1, T2> graph) {
			if(!isValid)
				return;
			Set<Integer> vertexIds = graph.adjacency.keySet();
			for(int vertexId : vertexIds) {
				if(!isValid)
					break;
				if(colors.get(vertexId).equals(Color.UNCOLORED))
					depthFirstSearch(graph, vertexId, Color.RED);
			}
		}
		private <T extends Comparable<T>, T2 extends Number>void depthFirstSearch(
				UndirectedGraph<T, T2> graph, int current, Color color) {
			colors.put(current, color);
			Color anotherColor = (color == Color.RED) ? Color.BLACK : Color.RED;
			for(Arc<T2> neighbour : graph.adjacency.get(current)) {
				if(colors.get(neighbour.getId()).equals(Color.UNCOLORED)) {
					depthFirstSearch(graph, neighbour.getId(), anotherColor);
					if(!isValid)
						return;
				}
				else if(colors.get(neighbour.getId()).equals(color)) {
					isValid = false;
					return;
				}
			}
		}
		public boolean isValid() {
			return isValid;
		}
	}
	
	/**
	 * 检测图是否有环
	 */
	public static class CycleDetecting{
		private Map<Integer, Boolean> marked;
		private boolean hasCycle;
		
		public <T1 extends Comparable<T1>, T2 extends Number> CycleDetecting(Graph<T1, T2> graph){
			this.marked = new HashMap<Integer, Boolean>();
			this.hasCycle = false;
			initialize(graph);
		}
		private <T1 extends Comparable<T1>, T2 extends Number> void initialize(Graph<T1, T2> graph){
			Set<Integer> vertexIds = graph.dictionary.keySet();
			for(int vertexId : vertexIds) {
				if(marked.containsKey(vertexId) && marked.get(vertexId).booleanValue())
					continue;
				depthFirstSearch(graph, vertexId, vertexId);
			}
		}
		private <T1 extends Comparable<T1>, T2 extends Number> void depthFirstSearch(Graph<T1, T2> graph, int vertexId1, int vertexId2) {
			marked.put(vertexId1, true);
			if(graph.adjacency == null || !graph.adjacency.containsKey(vertexId1)) {
				hasCycle = false;
				return;
			}
			for(Arc<T2> neighbour : graph.adjacency.get(vertexId1)) {
				if(!marked.containsKey(neighbour.getId()) || !marked.get(neighbour.getId()).booleanValue())
					depthFirstSearch(graph, neighbour.getId(), vertexId1);
				else if(neighbour.getId() != vertexId2)
					hasCycle = true;
			}
		}
		public boolean hasCycle() {
			return hasCycle;
		}
	}
	
	/* 等我学会了索引优先队列再来搞你 */
	public static class PrimMinimumSpanningTree{
		
	}
}
