package br.ufsm.pathfinder.map;

import java.util.*;

public class JPS {

    private final Grid grid;
    private int nodesExpanded;
    private Cell goal;

    public JPS(Grid grid) {
        this.grid = grid;
    }

    public List<Cell> search(Cell start, Cell goal) {
        grid.reset();
        nodesExpanded = 0;
        this.goal = goal;

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

            for (Cell successor : identifySuccessors(current)) {
                if (closed.contains(successor)) continue;

                double tentativeG = current.g + distance(current, successor);

                if (tentativeG < successor.g || successor.g == 0) {
                    successor.g = tentativeG;
                    successor.h = heuristic(successor, goal);
                    successor.f = successor.g + successor.h;
                    successor.parent = current;

                    if (!open.contains(successor)) {
                        open.add(successor);
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    private List<Cell> identifySuccessors(Cell current) {
        List<Cell> successors = new ArrayList<>();

        for (int[] dir : getDirections(current)) {
            Cell jp = jump(current, dir[0], dir[1]);
            if (jp != null) {
                successors.add(jp);
            }
        }

        return successors;
    }

    // salta na direção dada até achar jump point ou obstáculo
    private Cell jump(Cell current, int dx, int dy) {
        int nx = current.x + dx;
        int ny = current.y + dy;

        Cell next = grid.getCell(nx, ny);
        if (next == null || !next.walkable) return null;

        if (next.equals(goal)) return next;

        // verifica vizinhos forçados
        if (hasForcedNeighbor(next, dx, dy)) return next;

        // movimento diagonal: primeiro expande cardinais
        if (dx != 0 && dy != 0) {
            if (jump(next, dx, 0) != null || jump(next, 0, dy) != null) {
                return next;
            }
        }

        return jump(next, dx, dy);
    }

    // verifica se o nó tem vizinho forçado na direção dada
    private boolean hasForcedNeighbor(Cell cell, int dx, int dy) {
        int x = cell.x;
        int y = cell.y;

        if (dx != 0 && dy == 0) {
            // movimento horizontal
            return (!grid.isWalkable(x, y - 1) && grid.isWalkable(x + dx, y - 1))
                    || (!grid.isWalkable(x, y + 1) && grid.isWalkable(x + dx, y + 1));
        }

        if (dx == 0 && dy != 0) {
            // movimento vertical
            return (!grid.isWalkable(x - 1, y) && grid.isWalkable(x - 1, y + dy))
                    || (!grid.isWalkable(x + 1, y) && grid.isWalkable(x + 1, y + dy));
        }

        if (dx != 0 && dy != 0) {
            // movimento diagonal
            return (!grid.isWalkable(x - dx, y) && grid.isWalkable(x - dx, y + dy))
                    || (!grid.isWalkable(x, y - dy) && grid.isWalkable(x + dx, y - dy));
        }

        return false;
    }

    // determina direções a explorar baseado no pai
    private List<int[]> getDirections(Cell cell) {
        List<int[]> dirs = new ArrayList<>();

        if (cell.parent == null) {
            // nó inicial: explora todas as direções
            dirs.addAll(Arrays.asList(
                    new int[]{0, -1}, new int[]{0, 1},
                    new int[]{-1, 0}, new int[]{1, 0},
                    new int[]{-1, -1}, new int[]{1, -1},
                    new int[]{-1, 1}, new int[]{1, 1}
            ));
            return dirs;
        }

        int dx = Integer.signum(cell.x - cell.parent.x);
        int dy = Integer.signum(cell.y - cell.parent.y);

        if (dx != 0 && dy != 0) {
            // diagonal
            if (grid.isWalkable(cell.x + dx, cell.y)) dirs.add(new int[]{dx, 0});
            if (grid.isWalkable(cell.x, cell.y + dy)) dirs.add(new int[]{0, dy});
            if (grid.isWalkable(cell.x + dx, cell.y) || grid.isWalkable(cell.x, cell.y + dy))
                dirs.add(new int[]{dx, dy});
            if (!grid.isWalkable(cell.x - dx, cell.y)) dirs.add(new int[]{-dx, dy});
            if (!grid.isWalkable(cell.x, cell.y - dy)) dirs.add(new int[]{dx, -dy});
        } else if (dx != 0) {
            // horizontal
            if (grid.isWalkable(cell.x + dx, cell.y)) dirs.add(new int[]{dx, 0});
            if (!grid.isWalkable(cell.x, cell.y - 1)) dirs.add(new int[]{dx, -1});
            if (!grid.isWalkable(cell.x, cell.y + 1)) dirs.add(new int[]{dx, 1});
        } else {
            // vertical
            if (grid.isWalkable(cell.x, cell.y + dy)) dirs.add(new int[]{0, dy});
            if (!grid.isWalkable(cell.x - 1, cell.y)) dirs.add(new int[]{-1, dy});
            if (!grid.isWalkable(cell.x + 1, cell.y)) dirs.add(new int[]{1, dy});
        }

        return dirs;
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
