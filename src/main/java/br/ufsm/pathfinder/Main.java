package br.ufsm.pathfinder;

import br.ufsm.pathfinder.algorithms.HPA;
import br.ufsm.pathfinder.benchmark.Benchmark;
import br.ufsm.pathfinder.hpajps.HPAJps;
import br.ufsm.pathfinder.map.AStar;
import br.ufsm.pathfinder.map.Cell;
import br.ufsm.pathfinder.map.Grid;
import br.ufsm.pathfinder.map.JPS;

import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {

        // mapa pequeno
        if (args.length == 0) {
            System.out.println("Quick teste");
            quickTest();
            return;
        }

        // benchmark em mapa real
        String mapPath = args[0];
        String csvPath = args.length > 1 ? args[1] : "results/output.csv";

        Benchmark.run(mapPath, csvPath);
    }

    private static void quickTest() throws IOException {
        // cria um mapa pequeno manualmente para testar
        String testMap = "maps/test.map";
        createTestMap(testMap);

        Grid grid = Grid.loadFromFile(testMap);
        System.out.println("Mapa carregado: " + grid);

        Cell start = grid.getCell(0, 0);
        Cell goal  = grid.getCell(9, 9);

        System.out.println("Start: " + start);
        System.out.println("Goal:  " + goal);
        System.out.println();

        // A*
        AStar astar = new AStar(grid);
        long t0 = System.nanoTime();
        List<Cell> astarPath = astar.search(start, goal);
        long t1 = System.nanoTime();
        System.out.printf("A*:      %d nós | %d células | %.2f ms%n",
                astar.getNodesExpanded(), astarPath.size(), (t1 - t0) / 1_000_000.0);

        // JPS
        JPS jps = new JPS(grid);
        t0 = System.nanoTime();
        List<Cell> jpsPath = jps.search(start, goal);
        t1 = System.nanoTime();
        System.out.printf("JPS:     %d nós | %d células | %.2f ms%n",
                jps.getNodesExpanded(), jpsPath.size(), (t1 - t0) / 1_000_000.0);

        // HPA*
        HPA hpa = new HPA(grid, 4);
        t0 = System.nanoTime();
        List<Cell> hpaPath = hpa.search(start, goal);
        t1 = System.nanoTime();
        System.out.printf("HPA*:    %d nós | %d células | %.2f ms | portais: %d%n",
                hpa.getNodesExpanded(), hpaPath.size(), (t1 - t0) / 1_000_000.0, hpa.getPortalCount());

        // HPA-JPS
        HPAJps hpaJps = new HPAJps(grid, 4);
        t0 = System.nanoTime();
        List<Cell> hpaJpsPath = hpaJps.search(start, goal);
        t1 = System.nanoTime();
        System.out.printf("HPA-JPS: %d nós | %d células | %.2f ms | portais: %d%n",
                hpaJps.getNodesExpanded(), hpaJpsPath.size(), (t1 - t0) / 1_000_000.0, hpaJps.getPortalCount());
    }

    private static void createTestMap(String path) throws IOException {
        // mapa 10x10 simples para teste
        java.io.File dir = new java.io.File("maps");
        if (!dir.exists()) dir.mkdirs();

        java.io.File dir2 = new java.io.File("results");
        if (!dir2.exists()) dir2.mkdirs();

        String[] mapLines = {
                "type octile",
                "height 10",
                "width 10",
                "map",
                "..........",
                ".@@@@.....",
                "......@...",
                "..@...@...",
                "..@...@...",
                "..@@@@@...",
                "..........",
                "...@@@....",
                "..........",
                ".........."
        };

        try (java.io.PrintWriter pw = new java.io.PrintWriter(path)) {
            for (String line : mapLines) pw.println(line);
        }

        System.out.println("Mapa de teste criado: " + path);
    }
}
