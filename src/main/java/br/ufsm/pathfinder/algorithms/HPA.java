package br.ufsm.pathfinder.algorithms;

import br.ufsm.pathfinder.map.Cell;
import br.ufsm.pathfinder.map.Grid;

import java.util.*;

public class HPA {

    private final Grid grid;
    private final int clusterSize;
    private int nodesExpanded;

    // grafo abstrato: cada portal é um nó, com arestas para outros portais
    private final List<Cell> abstractNodes = new ArrayList<>();
    private final Map<Cell, List<Edge>> abstractGraph = new HashMap<>();

    public HPA(Grid grid, int clusterSize) {
        this.grid = grid;
        this.clusterSize = clusterSize;
        buildAbstractGraph();
    }

    // pré-processamento

    private void buildAbstractGraph() {
        abstractNodes.clear();
        abstractGraph.clear();

        // 1. identifica portais entre clusters vizinhos
        identifyPortals();

        // 2. conecta portais dentro do mesmo cluster
        connectPortalsIntraClusters();
    }

    private void identifyPortals() {
        // portais horizontais (bordas verticais entre clusters)
        for (int y = 0; y < grid.height; y++) {
            for (int x = clusterSize - 1; x < grid.width - 1; x += clusterSize) {
                Cell a = grid.getCell(x, y);
                Cell b = grid.getCell(x + 1, y);
                if (a != null && b != null && a.walkable && b.walkable) {
                    addPortalPair(a, b);
                }
            }
        }

        // portais verticais (bordas horizontais entre clusters)
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

        // conecta os dois lados do portal com custo 1
        abstractGraph.get(a).add(new Edge(b, 1.0));
        abstractGraph.get(b).add(new Edge(a, 1.0));
    }

    private void connectPortalsIntraClusters() {
        // para cada par de portais no mesmo cluster, roda A* local
        int clustersX = (int) Math.ceil((double) grid.width / clusterSize);
        int clustersY = (int) Math.ceil((double) grid.height / clusterSize);

        for (int cy = 0; cy < clustersY; cy++) {
            for (int cx = 0; cx < clustersX; cx++) {
                int x0 = cx * clusterSize;
                int y0 = cy * clusterSize;
                int x1 = Math.min(x0 + clusterSize - 1, grid.width - 1);
                int y1 = Math.min(y0 + clusterSize - 1, grid.height - 1);

                // portais deste cluster
                List<Cell> clusterPortals = new ArrayList<>();
                for (Cell node : abstractNodes) {
                    if (node.x >= x0 && node.x <= x1 && node.y >= y0 && node.y <= y1) {
                        clusterPortals.add(node);
                    }
                }

                // conecta todos os pares dentro do cluster
                for (int i = 0; i < clusterPortals.size(); i++) {
                    for (int j = i + 1; j < clusterPortals.size(); j++) {
                        Cell from = clusterPortals.get(i);
                        Cell to   = clusterPortals.get(j);

                        double cost = localSearch(from, to, x0, y0, x1, y1);
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

        // insere start e goal temporariamente no grafo
        abstractGraph.put(start, new ArrayList<>());
        abstractGraph.put(goal, new ArrayList<>());
        connectToAbstractGraph(start);
        connectToAbstractGraph(goal);

        // busca abstrata
        List<Cell> abstractPath = abstractSearch(start, goal);

        // remove start e goal do grafo
        abstractGraph.remove(start);
        abstractGraph.remove(goal);

        if (abstractPath.isEmpty()) return Collections.emptyList();

        // refinamento local
        return refinePath(abstractPath);
    }

    private void connectToAbstractGraph(Cell node) {
        // descobre em qual cluster o nó está
        int cx = (node.x / clusterSize) * clusterSize;
        int cy = (node.y / clusterSize) * clusterSize;
        int x1 = Math.min(cx + clusterSize - 1, grid.width - 1);
        int y1 = Math.min(cy + clusterSize - 1, grid.height - 1);

        for (Cell portal : abstractNodes) {
            if (portal.x >= cx && portal.x <= x1 && portal.y >= cy && portal.y <= y1) {
                double cost = localSearch(node, portal, cx, cy, x1, y1);
                if (cost >= 0) {
                    abstractGraph.get(node).add(new Edge(portal, cost));
                    abstractGraph.get(portal).add(new Edge(node, cost));
                }
            }
        }
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

            List<Edge> edges = abstractGraph.getOrDefault(current, Collections.emptyList());
            for (Edge edge : edges) {
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

            List<Cell> segment = localSearchPath(from, to, x0, y0, x1, y1);
            if (!segment.isEmpty()) {
                if (!fullPath.isEmpty()) segment.remove(0); // evita duplicar nós
                fullPath.addAll(segment);
            }
        }

        return fullPath;
    }

    // busca local (dentro do cluster)

    // retorna custo ou -1 se não achou caminho
    private double localSearch(Cell start, Cell goal, int x0, int y0, int x1, int y1) {
        List<Cell> path = localSearchPath(start, goal, x0, y0, x1, y1);
        if (path.isEmpty()) return -1;

        double cost = 0;
        for (int i = 1; i < path.size(); i++) {
            cost += distance(path.get(i - 1), path.get(i));
        }
        return cost;
    }

    private List<Cell> localSearchPath(Cell start, Cell goal, int x0, int y0, int x1, int y1) {
        grid.reset();

        PriorityQueue<Cell> open = new PriorityQueue<>(Comparator.comparingDouble(c -> c.f));
        Set<Cell> closed = new HashSet<>();

        start.g = 0;
        start.h = heuristic(start, goal);
        start.f = start.h;
        open.add(start);

        int[][] dirs = {
                {0,-1},{0,1},{-1,0},{1,0},
                {-1,-1},{1,-1},{-1,1},{1,1}
        };

        while (!open.isEmpty()) {
            Cell current = open.poll();
            if (current.equals(goal)) return reconstructPath(current);

            closed.add(current);

            for (int[] dir : dirs) {
                int nx = current.x + dir[0];
                int ny = current.y + dir[1];

                // restringe busca ao cluster
                if (nx < x0 || nx > x1 || ny < y0 || ny > y1) continue;

                Cell neighbor = grid.getCell(nx, ny);
                if (neighbor == null || !neighbor.walkable || closed.contains(neighbor)) continue;

                boolean isDiagonal = (dir[0] != 0 && dir[1] != 0);
                double moveCost = isDiagonal ? 1.414 : 1.0;
                double tentativeG = current.g + moveCost;

                if (tentativeG < neighbor.g || neighbor.g == 0) {
                    neighbor.g = tentativeG;
                    neighbor.h = heuristic(neighbor, goal);
                    neighbor.f = neighbor.g + neighbor.h;
                    neighbor.parent = current;
                    if (!open.contains(neighbor)) open.add(neighbor);
                }
            }
        }

        return Collections.emptyList();
    }

    // utilitários

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

    private List<Cell> reconstructPath(Cell goal) {
        List<Cell> path = new ArrayList<>();
        Cell current = goal;
        while (current != null) {
            path.add(current);
            current = current.parent;
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
        return abstractNodes.size();
    }

    // classe interna

    private static class Edge {
        Cell target;
        double cost;

        Edge(Cell target, double cost) {
            this.target = target;
            this.cost = cost;
        }
    }
}