package br.ufsm.pathfinder.map;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Grid {

    public final int width;
    public final int height;
    private final Cell[][] cells;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new Cell[height][width];
    }

    // lê arquivo .map do movingai
    public static Grid loadFromFile(String path) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine(); // type octile
            int height = Integer.parseInt(br.readLine().split(" ")[1]);
            int width  = Integer.parseInt(br.readLine().split(" ")[1]);
            br.readLine(); // map

            Grid grid = new Grid(width, height);

            for (int y = 0; y < height; y++) {
                String line = br.readLine();
                for (int x = 0; x < width; x++) {
                    char c = line.charAt(x);
                    boolean walkable = (c == '.' || c == 'G' || c == 'S');
                    grid.cells[y][x] = new Cell(x, y, walkable);
                }
            }

            return grid;
        }
    }

    public Cell getCell(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return null;
        return cells[y][x];
    }

    public boolean isWalkable(int x, int y) {
        Cell c = getCell(x, y);
        return c != null && c.walkable;
    }

    // reseta custos de todas as células entre buscas
    public void reset() {
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                cells[y][x].reset();
    }

    @Override
    public String toString() {
        return "Grid(" + width + "x" + height + ")";
    }
}