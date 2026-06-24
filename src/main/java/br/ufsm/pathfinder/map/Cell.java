package br.ufsm.pathfinder.map;

public class Cell {

    public final int x;
    public final int y;
    public final boolean walkable;

    // custos usados pelo A* / JPS
    public double g;
    public double h;
    public double f;

    // pai no caminho (para reconstrução)
    public Cell parent;

    public Cell(int x, int y, boolean walkable) {
        this.x = x;
        this.y = y;
        this.walkable = walkable;
    }

    public void reset() {
        this.g = 0;
        this.h = 0;
        this.f = 0;
        this.parent = null;
    }

    @Override
    public String toString() {
        return "Cell(" + x + ", " + y + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cell other)) return false;
        return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }
}
