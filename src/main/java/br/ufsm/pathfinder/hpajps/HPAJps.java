package br.ufsm.pathfinder.hpajps;

import br.ufsm.pathfinder.map.Cell;
import br.ufsm.pathfinder.map.Grid;
import br.ufsm.pathfinder.map.JPS;

import java.util.*;

public class HPAJps {

    private final Grid grid;
    private final int clusterSize;
    private int nodesExpanded;

    private final List<Cell> abstractNodes = new ArrayList<>();
    private final Map<Cell, List<Edge>> abstractGraph = new HashMap<>();
    private int portalPairCount;
    private int jumpPointsFound;

    public HPAJps(Grid grid, int clusterSize) {
        this.grid = grid;
        this.clusterSize = clusterSize;
        buildAbstractGraph();
    }

    // pré-processamento

    private void buildAbstractGraph() {
        abstractNodes.clear();
        abstractGraph.clear();
        portalPairCount = 0;
        identifyPortals();
        connectPortalsIntraClusters();
    }

    private void identifyPortals() {
        // portais horizontais
        for (int y = 0; y < grid.height; y++) {
            for (int x = clusterSize - 1; x < grid.width - 1; x += clusterSize) {
                Cell a = grid.getCell(x, y);
                Cell b = grid.getCell(x + 1, y);
                if (a != null && b != null && a.walkable && b.walkable) {
                    addPortalPair(a, b);
                }
            }
        }

        // portais verticais
        for (int x = 0; x < grid.width; x++) {
            for (int y = clusterSize - 1; y < grid.height - 1; y += clusterSize) {
                Cell a = grid.getCell(x, y);
                Cell b = grid.getCell(x, y + 1);
                if (a != null && b != null && a.walkable && b.walkable) {
                    addPortalPair(a, b);
                }
            }
        }
    }

    private void addPortalPair(Cell a, Cell b) {
        if (!abstractNodes.contains(a)) {
            abstractNodes.add(a);
            abstractGraph.put(a, new ArrayList<>());
        }
        if (!abstractNodes.contains(b)) {
            abstractNodes.add(b);
            abstractGraph.put(b, new ArrayList<>());
        }
        abstractGraph.get(a).add(new Edge(b, 1.0));
        abstractGraph.get(b).add(new Edge(a, 1.0));
        portalPairCount++;
    }

    private void connectPortalsIntraClusters() {
        int clustersX = (int) Math.ceil((double) grid.width / clusterSize);
        int clustersY = (int) Math.ceil((double) grid.height / clusterSize);

        for (int cy = 0; cy < clustersY; cy++) {
            for (int cx = 0; cx < clustersX; cx++) {
                int x0 = cx * clusterSize;
                int y0 = cy * clusterSize;
                int x1 = Math.min(x0 + clusterSize - 1, grid.width - 1);
                int y1 = Math.min(y0 + clusterSize - 1, grid.height - 1);

                List<Cell> clusterPortals = new ArrayList<>();
                for (Cell node : abstractNodes) {
                    if (node.x >= x0 && node.x <= x1 && node.y >= y0 && node.y <= y1) {
                        clusterPortals.add(node);
                    }
                }

                for (int i = 0; i < clusterPortals.size(); i++) {
                    for (int j = i + 1; j < clusterPortals.size(); j++) {
                        Cell from = clusterPortals.get(i);
                        Cell to   = clusterPortals.get(j);

                        // usa JPS no lugar de A* local
                        double cost = localSearchWithJPS(from, to, x0, y0, x1, y1);
                        if (cost >= 0) {
                            abstractGraph.get(from).add(new Edge(to, cost));
                            abstractGraph.get(to).add(new Edge(from, cost));
                        }
                    }
                }
            }
        }
    }

    // busca

    public List<Cell> search(Cell start, Cell goal) {
        nodesExpanded = 0;
        jumpPointsFound = 0;

        List<Edge> prevStartEdges = abstractGraph.get(start);
        List<Edge> prevGoalEdges  = abstractGraph.get(goal);

        abstractGraph.put(start, new ArrayList<>());
        abstractGraph.put(goal,  new ArrayList<>());
        List<Cell> startPortals = connectToAbstractGraph(start);
        List<Cell> goalPortals  = connectToAbstractGraph(goal);

        List<Cell> abstractPath = abstractSearch(start, goal);

        for (Cell p : startPortals) abstractGraph.get(p).removeIf(e -> e.target.equals(start));
        for (Cell p : goalPortals)  abstractGraph.get(p).removeIf(e -> e.target.equals(goal));

        if (prevStartEdges != null) abstractGraph.put(start, prevStartEdges);
        else                        abstractGraph.remove(start);
        if (prevGoalEdges != null)  abstractGraph.put(goal, prevGoalEdges);
        else                        abstractGraph.remove(goal);

        if (abstractPath.isEmpty()) return Collections.emptyList();

        return refinePath(abstractPath);
    }

    private List<Cell> connectToAbstractGraph(Cell node) {
        int cx = (node.x / clusterSize) * clusterSize;
        int cy = (node.y / clusterSize) * clusterSize;
        int x1 = Math.min(cx + clusterSize - 1, grid.width - 1);
        int y1 = Math.min(cy + clusterSize - 1, grid.height - 1);

        List<Cell> connected = new ArrayList<>();
        for (Cell portal : abstractNodes) {
            if (portal.x >= cx && portal.x <= x1 && portal.y >= cy && portal.y <= y1) {
                double cost = localSearchWithJPS(node, portal, cx, cy, x1, y1);
                if (cost >= 0) {
                    abstractGraph.get(node).add(new Edge(portal, cost));
                    abstractGraph.get(portal).add(new Edge(node, cost));
                    connected.add(portal);
                }
            }
        }
        return connected;
    }

    private List<Cell> abstractSearch(Cell start, Cell goal) {
        Map<Cell, Double> gScore = new HashMap<>();
        Map<Cell, Cell> parentMap = new HashMap<>();
        PriorityQueue<Cell> open = new PriorityQueue<>(
                Comparator.comparingDouble(c -> gScore.getOrDefault(c, Double.MAX_VALUE) + heuristic(c, goal))
        );

        gScore.put(start, 0.0);
        open.add(start);

        while (!open.isEmpty()) {
            Cell current = open.poll();
            nodesExpanded++;

            if (current.equals(goal)) {
                return reconstructAbstractPath(parentMap, goal);
            }

            for (Edge edge : abstractGraph.getOrDefault(current, Collections.emptyList())) {
                double tentativeG = gScore.getOrDefault(current, Double.MAX_VALUE) + edge.cost;
                if (tentativeG < gScore.getOrDefault(edge.target, Double.MAX_VALUE)) {
                    gScore.put(edge.target, tentativeG);
                    parentMap.put(edge.target, current);
                    open.add(edge.target);
                }
            }
        }

        return Collections.emptyList();
    }

    private List<Cell> refinePath(List<Cell> abstractPath) {
        List<Cell> fullPath = new ArrayList<>();

        for (int i = 0; i < abstractPath.size() - 1; i++) {
            Cell from = abstractPath.get(i);
            Cell to   = abstractPath.get(i + 1);

            int x0 = Math.min(from.x, to.x) / clusterSize * clusterSize;
            int y0 = Math.min(from.y, to.y) / clusterSize * clusterSize;
            int x1 = Math.min(x0 + clusterSize * 2 - 1, grid.width - 1);
            int y1 = Math.min(y0 + clusterSize * 2 - 1, grid.height - 1);

            // usa JPS no refinamento
            List<Cell> segment = localSearchPathWithJPS(from, to, x0, y0, x1, y1);
            if (!segment.isEmpty()) {
                if (!fullPath.isEmpty()) segment.remove(0);
                fullPath.addAll(segment);
            }
        }

        return fullPath;
    }

    // busca local com jps

    private double localSearchWithJPS(Cell start, Cell goal, int x0, int y0, int x1, int y1) {
        List<Cell> path = localSearchPathWithJPS(start, goal, x0, y0, x1, y1);
        if (path.isEmpty()) return -1;

        double cost = 0;
        for (int i = 1; i < path.size(); i++) {
            cost += distance(path.get(i - 1), path.get(i));
        }
        return cost;
    }

    private List<Cell> localSearchPathWithJPS(Cell start, Cell goal, int x0, int y0, int x1, int y1) {
        Grid subGrid = new ClippedGrid(grid, x0, y0, x1, y1);
        JPS jps = new JPS(subGrid);

        Cell localStart = subGrid.getCell(start.x, start.y);
        Cell localGoal  = subGrid.getCell(goal.x, goal.y);

        if (localStart == null || localGoal == null) return Collections.emptyList();

        List<Cell> path = jps.search(localStart, localGoal);
        jumpPointsFound += jps.getJumpPointsFound();
        return path;
    }

    private List<Cell> reconstructAbstractPath(Map<Cell, Cell> parentMap, Cell goal) {
        List<Cell> path = new ArrayList<>();
        Cell current = goal;
        while (current != null) {
            path.add(current);
            current = parentMap.get(current);
        }
        Collections.reverse(path);
        return path;
    }

    private double heuristic(Cell a, Cell b) {
        int dx = Math.abs(a.x - b.x);
        int dy = Math.abs(a.y - b.y);
        return (dx + dy) + (1.414 - 2) * Math.min(dx, dy);
    }

    private double distance(Cell a, Cell b) {
        int dx = Math.abs(a.x - b.x);
        int dy = Math.abs(a.y - b.y);
        return (dx == 0 || dy == 0) ? Math.max(dx, dy) : Math.sqrt(dx * dx + dy * dy);
    }

    public int getNodesExpanded() {
        return nodesExpanded;
    }

    public int getAbstractGraphSize() {
        return abstractNodes.size();
    }

    public int getPortalCount() {
        return portalPairCount;
    }

    public int getJumpPointsFound() {
        return jumpPointsFound;
    }

    private static class Edge {
        Cell target;
        double cost;

        Edge(Cell target, double cost) {
            this.target = target;
            this.cost = cost;
        }
    }
}