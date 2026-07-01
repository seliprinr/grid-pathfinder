package br.ufsm.pathfinder.benchmark;

import br.ufsm.pathfinder.algorithms.HPA;
import br.ufsm.pathfinder.hpajps.HPAJps;
import br.ufsm.pathfinder.map.AStar;
import br.ufsm.pathfinder.map.Cell;
import br.ufsm.pathfinder.map.Grid;
import br.ufsm.pathfinder.map.JPS;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Benchmark {

    private static final int CLUSTER_SIZE = 16;
    private static final int RUNS = 3;

    public static void run(String mapPath, String outputCsvPath, boolean ignored) throws IOException {
        run(mapPath, outputCsvPath);
    }

    public static void run(String mapPath, String outputCsvPath) throws IOException {
        System.out.println("Carregando mapa: " + mapPath);
        Grid grid = Grid.loadFromFile(mapPath);
        System.out.println("Mapa carregado: " + grid);

        List<int[]> queries = generateQueries(grid, 5);

        System.out.println("Pré-processando HPA*...");
        HPA hpa = new HPA(grid, CLUSTER_SIZE);

        System.out.println("Pré-processando HPA-JPS...");
        HPAJps hpaJps = new HPAJps(grid, CLUSTER_SIZE);

        System.out.println("Rodando benchmarks...");

        // escreve cabeçalho só se o arquivo ainda não existe ou está vazio
        File outFile = new File(outputCsvPath);
        boolean fileHasData = outFile.exists() && outFile.length() > 0;

        try (CSVWriter writer = new CSVWriter(new FileWriter(outputCsvPath, fileHasData))) {

            if (!fileHasData) {
                writer.writeNext(new String[]{
                        "map", "query", "algorithm",
                        "time_ms", "nodes_expanded",
                        "path_length", "path_cost",
                        "abstract_graph_size", "portal_count", "jump_points"
                });
            }

            String mapName = mapPath.substring(mapPath.lastIndexOf("/") + 1);

            for (int q = 0; q < queries.size(); q++) {
                int[] query = queries.get(q);
                Cell start = grid.getCell(query[0], query[1]);
                Cell goal  = grid.getCell(query[2], query[3]);

                if (start == null || goal == null) continue;
                if (!start.walkable || !goal.walkable) continue;

                System.out.printf("Query %d/%d: (%d,%d) -> (%d,%d)%n",
                        q + 1, queries.size(), start.x, start.y, goal.x, goal.y);

                // A*
                BenchmarkResult astarResult = runAStar(grid, start, goal);
                writer.writeNext(buildRow(mapName, q, "A*", astarResult));

                // JPS
                BenchmarkResult jpsResult = runJPS(grid, start, goal);
                writer.writeNext(buildRow(mapName, q, "JPS", jpsResult));

                // HPA*
                BenchmarkResult hpaResult = runHPA(grid, hpa, start, goal);
                hpaResult.abstractGraphSize = hpa.getAbstractGraphSize();
                hpaResult.portalCount = hpa.getPortalCount();
                writer.writeNext(buildRow(mapName, q, "HPA*", hpaResult));

                // HPA-JPS
                BenchmarkResult hpaJpsResult = runHPAJps(grid, hpaJps, start, goal);
                hpaJpsResult.abstractGraphSize = hpaJps.getAbstractGraphSize();
                hpaJpsResult.portalCount = hpaJps.getPortalCount();
                hpaJpsResult.jumpPointCount = hpaJps.getJumpPointsFound();
                writer.writeNext(buildRow(mapName, q, "HPA-JPS", hpaJpsResult));
            }
        }

        System.out.println("Resultados salvos em: " + outputCsvPath);
    }

    // runners

    private static BenchmarkResult runAStar(Grid grid, Cell start, Cell goal) {
        long totalTime = 0;
        int totalNodes = 0;
        List<Cell> path = List.of();

        for (int i = 0; i < RUNS; i++) {
            AStar astar = new AStar(grid);
            long t0 = System.nanoTime();
            path = astar.search(start, goal);
            long t1 = System.nanoTime();
            totalTime += (t1 - t0);
            totalNodes += astar.getNodesExpanded();
        }

        BenchmarkResult r = new BenchmarkResult();
        r.timeMs = (totalTime / RUNS) / 1_000_000.0;
        r.nodesExpanded = totalNodes / RUNS;
        r.pathLength = path.size();
        r.pathCost = computeCost(path);
        return r;
    }

    private static BenchmarkResult runJPS(Grid grid, Cell start, Cell goal) {
        long totalTime = 0;
        int totalNodes = 0;
        int totalJumpPoints = 0;
        List<Cell> path = List.of();

        for (int i = 0; i < RUNS; i++) {
            JPS jps = new JPS(grid);
            long t0 = System.nanoTime();
            path = jps.search(start, goal);
            long t1 = System.nanoTime();
            totalTime += (t1 - t0);
            totalNodes += jps.getNodesExpanded();
            totalJumpPoints += jps.getJumpPointsFound();
        }

        BenchmarkResult r = new BenchmarkResult();
        r.timeMs = (totalTime / RUNS) / 1_000_000.0;
        r.nodesExpanded = totalNodes / RUNS;
        r.pathLength = path.size();
        r.pathCost = computeCost(path);
        r.jumpPointCount = totalJumpPoints / RUNS;
        return r;
    }

    private static BenchmarkResult runHPA(Grid grid, HPA hpa, Cell start, Cell goal) {
        long totalTime = 0;
        int totalNodes = 0;
        List<Cell> path = List.of();

        for (int i = 0; i < RUNS; i++) {
            long t0 = System.nanoTime();
            path = hpa.search(start, goal);
            long t1 = System.nanoTime();
            totalTime += (t1 - t0);
            totalNodes += hpa.getNodesExpanded();
        }

        BenchmarkResult r = new BenchmarkResult();
        r.timeMs = (totalTime / RUNS) / 1_000_000.0;
        r.nodesExpanded = totalNodes / RUNS;
        r.pathLength = path.size();
        r.pathCost = computeCost(path);
        return r;
    }

    private static BenchmarkResult runHPAJps(Grid grid, HPAJps hpaJps, Cell start, Cell goal) {
        long totalTime = 0;
        int totalNodes = 0;
        List<Cell> path = List.of();

        for (int i = 0; i < RUNS; i++) {
            long t0 = System.nanoTime();
            path = hpaJps.search(start, goal);
            long t1 = System.nanoTime();
            totalTime += (t1 - t0);
            totalNodes += hpaJps.getNodesExpanded();
        }

        BenchmarkResult r = new BenchmarkResult();
        r.timeMs = (totalTime / RUNS) / 1_000_000.0;
        r.nodesExpanded = totalNodes / RUNS;
        r.pathLength = path.size();
        r.pathCost = computeCost(path);
        return r;
    }

    private static List<int[]> generateQueries(Grid grid, int count) {
        List<Cell> walkable = new java.util.ArrayList<>();
        for (int y = 0; y < grid.height; y++) {
            for (int x = 0; x < grid.width; x++) {
                Cell c = grid.getCell(x, y);
                if (c != null && c.walkable) walkable.add(c);
            }
        }

        List<int[]> queries = new java.util.ArrayList<>();
        if (walkable.size() < 2) return queries;

        int step = walkable.size() / (count + 1);
        for (int i = 0; i < count; i++) {
            Cell start = walkable.get(step * (i + 1));
            Cell goal  = walkable.get(walkable.size() - 1 - step * (i + 1));
            if (!start.equals(goal)) {
                queries.add(new int[]{start.x, start.y, goal.x, goal.y});
            }
        }

        return queries;
    }

    private static double computeCost(List<Cell> path) {
        double cost = 0;
        for (int i = 1; i < path.size(); i++) {
            int dx = Math.abs(path.get(i).x - path.get(i - 1).x);
            int dy = Math.abs(path.get(i).y - path.get(i - 1).y);
            cost += (dx == 0 || dy == 0) ? Math.max(dx, dy) : Math.sqrt(dx * dx + dy * dy);
        }
        return cost;
    }

    private static String[] buildRow(String map, int query, String algorithm, BenchmarkResult r) {
        return new String[]{
                map,
                String.valueOf(query),
                algorithm,
                String.format("%.4f", r.timeMs),
                String.valueOf(r.nodesExpanded),
                String.valueOf(r.pathLength),
                String.format("%.4f", r.pathCost),
                String.valueOf(r.abstractGraphSize),
                String.valueOf(r.portalCount),
                String.valueOf(r.jumpPointCount)
        };
    }

    private static class BenchmarkResult {
        double timeMs;
        int nodesExpanded;
        int pathLength;
        double pathCost;
        int abstractGraphSize;
        int portalCount;
        int jumpPointCount;
    }
}
