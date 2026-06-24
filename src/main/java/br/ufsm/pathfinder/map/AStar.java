package br.ufsm.pathfinder.map;

import java.util.*;

public class AStar {

    // movimentos possíveis: 4 cardinais + 4 diagonais
    private static final int[][] DIRECTIONS = {
            {0, -1}, {0, 1}, {-1, 0}, {1, 0},   // cima, baixo, esquerda, direita
            {-1, -1}, {1, -1}, {-1, 1}, {1, 1}   // diagonais
    };

    private final Grid grid;
    private int nodesExpanded;

    public AStar(Grid grid) {
        this.grid = grid;
    }

    public List<Cell> search(Cell start, Cell goal) {
        grid.reset();
        nodesExpanded = 0;

        PriorityQueue<Cell> open = new PriorityQueue<>(Comparator.comparingDouble(c -> c.f));
        Set<Cell> closed = new HashSet<>();

        start.g = 0;
        start.h = heuristic(start, goal);
        start.f = start.h;
        open.add(start);

        while (!open.isEmpty()) {
            Cell current = open.poll();

            if (current.equals(goal)) {
                return reconstructPath(current);
            }

            closed.add(current);
            nodesExpanded++;

            for (int[] dir : DIRECTIONS) {
                int nx = current.x + dir[0];
                int ny = current.y + dir[1];

                Cell neighbor = grid.getCell(nx, ny);
                if (neighbor == null || !neighbor.walkable || closed.contains(neighbor)) continue;

                // custo do movimento: diagonal custa mais
                boolean isDiagonal = (dir[0] != 0 && dir[1] != 0);
                double moveCost = isDiagonal ? 1.414 : 1.0;
                double tentativeG = current.g + moveCost;

                if (tentativeG < neighbor.g || neighbor.g == 0) {
                    neighbor.g = tentativeG;
                    neighbor.h = heuristic(neighbor, goal);
                    neighbor.f = neighbor.g + neighbor.h;
                    neighbor.parent = current;

                    if (!open.contains(neighbor)) {
                        open.add(neighbor);
                    }
                }
            }
        }

        return Collections.emptyList(); // caminho não encontrado
    }

    // heurística octile — adequada para grids com diagonais
    private double heuristic(Cell a, Cell b) {
        int dx = Math.abs(a.x - b.x);
        int dy = Math.abs(a.y - b.y);
        return (dx + dy) + (1.414 - 2) * Math.min(dx, dy);
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

    public int getNodesExpanded() {
        return nodesExpanded;
    }
}
