package br.ufsm.pathfinder;

import br.ufsm.pathfinder.benchmark.Benchmark;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            System.out.println("Uso:");
            System.out.println("  mvn exec:java -Dexec.args=\"<mapa_ou_pasta> <resultado.csv>\"");
            System.out.println();
            System.out.println("Exemplos:");
            System.out.println("  mvn exec:java -Dexec.args=\"maps/512/maze-map results/results.csv\"");
            System.out.println("  mvn exec:java -Dexec.args=\"maps/512/maze-map/maze512-1-0.map results/output.csv\"");
            return;
        }

        String input   = args[0];
        String csvPath = args.length > 1 ? args[1] : "results/output.csv";

        new File("results").mkdirs();

        File f = new File(input);
        if (f.isDirectory()) {
            File[] maps = f.listFiles((dir, name) -> name.endsWith(".map"));
            if (maps == null || maps.length == 0) {
                System.out.println("Nenhum arquivo .map encontrado em: " + input);
                return;
            }
            Arrays.sort(maps);
            System.out.printf("Batch: %d mapas encontrados em %s%n", maps.length, input);
            for (int i = 0; i < maps.length; i++) {
                System.out.printf("%n[%d/%d] %s%n", i + 1, maps.length, maps[i].getName());
                Benchmark.run(maps[i].getPath(), csvPath, i == 0);
            }
            System.out.println("\nTodos os resultados salvos em: " + csvPath);
        } else {
            Benchmark.run(input, csvPath, true);
        }
    }
}
