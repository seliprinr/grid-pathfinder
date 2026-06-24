package br.ufsm.pathfinder.hpajps;

import br.ufsm.pathfinder.map.Cell;
import br.ufsm.pathfinder.map.Grid;

public class ClippedGrid extends Grid {

    private final Grid original;
    private final int x0, y0, x1, y1;

    public ClippedGrid(Grid original, int x0, int y0, int x1, int y1) {
        super(original.width, original.height);
        this.original = original;
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
    }

    @Override
    public Cell getCell(int x, int y) {
        if (x < x0 || x > x1 || y < y0 || y > y1) return null;
        return original.getCell(x, y);
    }

    @Override
    public boolean isWalkable(int x, int y) {
        if (x < x0 || x > x1 || y < y0 || y > y1) return false;
        return original.isWalkable(x, y);
    }

    @Override
    public void reset() {
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                Cell c = original.getCell(x, y);
                if (c != null) c.reset();
            }
        }
    }
}