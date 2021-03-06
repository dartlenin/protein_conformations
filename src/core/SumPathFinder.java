package core;

import java.util.*;
import java.util.stream.Collectors;

public class SumPathFinder<T extends AbstractNode<T>> implements PathFinder<T> {
    private Graph<T> graph;
    private double kParam;
    private double detour;
    private int maxPaths;
    private PathFilter<T> pathFilter;

    public SumPathFinder(Graph<T> graph, double kParam, int maxPaths, PathFilter<T> pathFilter) {
        this(graph, kParam, 2 / (kParam + 1), maxPaths, pathFilter);
    }

    // y = a * n - x;
    // y = n - k * x;
    // y = (n - x) / k;
    // x = n * (a - 1) / (1 - k)
    // k * a * n - k * x = n - x; x = (n - k * a * n) / (1 - k)
    // n * (a - 1) = n - k * a * n; k = (2 - a) / a = 2 / a - 1;

    public SumPathFinder(Graph<T> graph, double kParam, double detour, int maxPaths, PathFilter<T> pathFilter) {
        this.graph = graph;
        this.kParam = kParam;
        this.detour = detour;
        this.maxPaths = maxPaths;
        this.pathFilter = pathFilter;
    }

    public double getKParam() {
        return kParam;
    }

    private boolean edgesLengthRestriction(double distanceA, double distanceB, double distanceAB) {
        double maxBValue = Collections.min(
                Arrays.asList(distanceAB - kParam * distanceA, (distanceAB - distanceA) / kParam, detour * distanceAB - distanceA));
//        double shortDistance = Double.min(distanceA, distanceB);
//        double longDistance = Double.max(distanceA, distanceB);
//        double maxDetour = kParam * distanceAB;
//
//        double maxLongDistance = distanceAB - (2 * distanceAB / maxDetour - 1) * shortDistance;
//        return shortDistance + longDistance <= maxDetour && longDistance <= maxLongDistance;
        return distanceB <= maxBValue;
    }

    private boolean useNodeAsIntermediate(T start, T end, T intermediate) {
        Edge<T> direct = Graph.getConnectingEdge(start, end);
        Edge<T> edge1 = Graph.getConnectingEdge(start, intermediate);
        Edge<T> edge2 = Graph.getConnectingEdge(end, intermediate);
        return direct != null && edge1 != null && edge2 != null &&
                edgesLengthRestriction(edge1.getWeight(), edge2.getWeight(), direct.getWeight());
    }

    private Set<Path<T>> mergePaths(Set<Path<T>> pathsTo, Set<Path<T>> pathsFrom) {
        Set<Path<T>> result = new HashSet<>(pathsTo.size() * pathsFrom.size());
        for (Path<T> path1 : pathsTo) {
            for (Path<T> path2 : pathsFrom) {
                result.add(Path.merge(path1, path2, path1.getEdges().size() - 1, 1, path2.getEdges().get(1)));
            }
        }
        return result;
    }

    private Set<Path<T>> filterPaths(Collection<Path<T>> allPaths) {
        Set<Path<T>> result = pathFilter.filterPaths(allPaths, maxPaths);
        if (result.size() > maxPaths) {
            throw new IllegalArgumentException("Provided filter failed to restrict amount of paths");
        }
        return result;
    }

    private Set<Path<T>> findPathsHelper(T start, T end, Set<Integer> usedIds) {
        Set<Path<T>> allPaths = new HashSet<>();
        Path<T> trivial = Path.getTrivialPath(start);
        trivial.extendWith(Graph.getConnectingEdge(start, end));
        allPaths.add(trivial);

        List<T> intermediateNodes = graph.getNodes().stream().
                filter(n -> !usedIds.contains(n.getId()) && useNodeAsIntermediate(start, end, n)).collect(Collectors.toList());
        for (T node : intermediateNodes) {
            usedIds.add(node.getId());
            Set<Path<T>> pathsTo = findPathsHelper(start, node, usedIds);
            Set<Path<T>> pathsFrom = findPathsHelper(node, end, usedIds);
            usedIds.remove(node.getId());
            Set<Path<T>> mergedPaths = mergePaths(pathsTo, pathsFrom);
            allPaths.addAll(mergedPaths);
        }

        return filterPaths(allPaths);
    }

    public Set<Path<T>> findPaths(int startId, int endId) {
        T start = graph.getNodes().get(startId);
        T end = graph.getNodes().get(endId);
        if (Graph.getConnectingEdge(start, end) == null) {
            throw new IllegalArgumentException("Specified nodes are not connected");
        }

        return findPathsHelper(start, end, new HashSet<>(Arrays.asList(startId, endId)));
    }
}
