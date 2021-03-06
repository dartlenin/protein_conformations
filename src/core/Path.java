package core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Path<T extends AbstractNode<T>> {
    private final Set<Integer> usedIds;
    private final List<Edge<T>> edges;
    private double totalWeight = 0;
    private int totalNodes = 0;

    public Path() {
        this(new HashSet<>(), new ArrayList<>());
    }

    public Path(Set<Integer> usedIds, List<Edge<T>> edges) {
        this.usedIds = usedIds;
        this.edges = edges;
    }

    public Path(Set<Integer> usedIds, List<Edge<T>> edges, Path other) {
        this(usedIds, edges);
        totalWeight = other.totalWeight;
        totalNodes = other.totalNodes;
    }

    public static <T extends AbstractNode<T>> Path<T> getTrivialPath(T singleNode) {
        Path<T> newPath = new Path<>();
        newPath.usedIds.add(singleNode.getId());
        newPath.edges.add(new Edge<>(null, singleNode, 0));
        newPath.totalNodes = 1;
        if (singleNode instanceof HierarchicalNode) {
            newPath.totalNodes = ((HierarchicalNode) singleNode).getSize();
        }
        return newPath;
    }

    // connect path1BreakIndex node in path1 with path2BreakIndex node in path2
    public static <T extends AbstractNode<T>> Path<T> merge(Path<T> path1, Path<T> path2, int path1BreakIndex, int path2BreakIndex,
                                                            Edge<T> bridge) {
        Path<T> newPath = new Path<>();

        newPath.edges.addAll(path1.edges.subList(0, path1BreakIndex + 1));
        newPath.edges.add(bridge);
        newPath.edges.addAll(path2.edges.subList(path2BreakIndex + 1, path2.edges.size()));

        newPath.edges.forEach(e -> newPath.usedIds.add(e.getSecond().getId()));

        newPath.totalWeight = newPath.edges.stream().collect(Collectors.summingDouble(Edge::getWeight));
        newPath.totalNodes = newPath.edges.size();
        if (newPath.getEdges().get(0).getSecond() instanceof HierarchicalNode) {
            newPath.totalNodes = newPath.edges.stream().mapToInt(e -> ((HierarchicalNode) e.getSecond()).getSize()).sum();
        }
        return newPath;
    }

    public List<Edge<T>> getEdges() {
        return edges;
    }

    public Path<T> extendWith(Edge<T> nextEdge) {
        if (nextEdge.getFirst() != edges.get(edges.size() - 1).getSecond()) {
            throw new IllegalArgumentException("First node of new edge doesn't match last node of this path");
        }
        usedIds.add(nextEdge.getSecond().getId());
        edges.add(nextEdge);
        totalWeight += nextEdge.getWeight();
        if (nextEdge.getSecond() instanceof HierarchicalNode) {
            totalNodes += ((HierarchicalNode) nextEdge.getSecond()).getSize();
        } else {
            totalNodes += 1;
        }
        return this;
    }

    public Set<Integer> getUsedIds() {
        return usedIds;
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public int getTotalNodes() {
        return totalNodes;
    }

    public void setTotalNodes(int totalNodes) {
        this.totalNodes = totalNodes;
    }

    @Override
    public int hashCode() {
        int result = 17;
        for (Edge edge : edges) {
            result = 31 * result + edge.hashCode();
        }
        return result;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Path)) {
            return false;
        }
        Path other = (Path) object;
        return edges.size() == other.edges.size() &&
                IntStream.range(0, edges.size()).mapToObj(i -> edges.get(i).equals(other.edges.get(i))).allMatch(b -> b);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("void");
        edges.forEach(e -> builder.append(" -> ").append(e.getSecond().getId()));
        builder.append("; total nodes: ").append(totalNodes);
        builder.append(String.format("; total weight: %.0f", totalWeight));
        return builder.toString();
    }
}
