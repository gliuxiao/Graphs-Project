//The following class was pre-written by the UMD CS Department

package graphs;

/**
 * Represents the processing we apply to a vertex of a graph.
 */
public interface CallBack<E> {
	public void processVertex(String vertex, E vertexData);
}
