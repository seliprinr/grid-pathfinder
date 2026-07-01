# 🗺️ Grid Pathfinder

Implementação e comparação de algoritmos de busca de caminhos em mapas do tipo grid, desenvolvida como trabalho acadêmico para a disciplina de **Metodologias e Técnicas da Computação** — UFSM.

O projeto implementa e integra os algoritmos **HPA\*** e **Jump Point Search (JPS)**, propondo uma abordagem híbrida denominada **HPA-JPS**.

---

## Algoritmos implementados

| Algoritmo | Descrição |
|-----------|-----------|
| **A\*** | Algoritmo de busca heurística clássico com movimentos cardinais e diagonais |
| **JPS** | Jump Point Search — extensão do A\* que reduz expansões identificando Jump Points |
| **HPA\*** | Hierarchical Pathfinding A\* — busca hierárquica com clusters e portais |
| **HPA-JPS** | Integração proposta: HPA\* com JPS no refinamento local dos clusters |

---

## Hipótese investigada

> É possível substituir o A\* local do HPA\* por JPS no refinamento dentro dos clusters, reduzindo o tempo de busca sem comprometer a qualidade do caminho?

---

## Estrutura do projeto

```
grid-pathfinder/
├── src/
│   └── main/
│       └── java/
│           └── br/ufsm/pathfinder/
│               ├── Main.java
│               ├── map/
│               │   ├── Cell.java
│               │   └── Grid.java
│               ├── algorithms/
│               │   ├── AStar.java
│               │   ├── JPS.java
│               │   └── HPA.java
│               ├── hpajps/
│               │   ├── HPAJps.java
│               │   └── ClippedGrid.java
│               └── benchmark/
│                   └── Benchmark.java
├── maps/          # Arquivos .map do movingai
├── results/       # CSVs com resultados dos benchmarks
└── pom.xml
```

---

## Pré-requisitos

- Java 21+
- Maven 3.8+

---

## Como executar

### Benchmark completo (todos os mapas)

Execute cada bloco em ordem — os resultados são acumulados no mesmo CSV:

```bash
mvn compile

mvn exec:java -Dexec.args="maps/256            results/results.csv"
mvn exec:java -Dexec.args="maps/512/maze-map   results/results.csv"
mvn exec:java -Dexec.args="maps/512/room-map   results/results.csv"
mvn exec:java -Dexec.args="maps/512/random-map results/results.csv"
mvn exec:java -Dexec.args="maps/1024           results/results.csv"
```

## Métricas coletadas

O benchmark gera um CSV com as seguintes colunas:

| Coluna | Descrição |
|--------|-----------|
| `map` | Nome do arquivo de mapa |
| `query` | Índice da query (par start→goal) |
| `algorithm` | A\*, JPS, HPA\* ou HPA-JPS |
| `time_ms` | Tempo de execução em milissegundos |
| `nodes_expanded` | Número de nós expandidos |
| `path_length` | Número de células no caminho |
| `path_cost` | Custo total do caminho |
| `abstract_graph_size` | Tamanho do grafo abstrato (HPA\* e HPA-JPS) |
| `portal_count` | Número de portais identificados |
| `jump_points` | Número de jump points encontrados (JPS e HPA-JPS) |

---

## Mapas utilizados

Mapas obtidos em [movingai.com/benchmarks](https://www.movingai.com/benchmarks/grids.html).

### 256×256 — Mapas de cidade

| Arquivo | Tipo | Descrição |
|---------|------|-----------|
| `Berlin_0_256.map` | Cidade | Mapa urbano de Berlim |
| `Denver_1_256.map` | Cidade | Mapa urbano de Denver |

### 512×512 — Mapas artificiais

| Arquivo | Tipo | Densidade |
|---------|------|-----------|
| `maze512-1-0/1/2.map` | Labirinto | Alta — corredores de 1 célula |
| `8room_000/001/002.map` | Salas | Média — blocos de salas com aberturas |
| `random512-10-0/1/2.map` | Aleatório | Baixa — 10% de obstáculos |

### 1024×1024 — Mapas de jogo

| Arquivo | Tipo | Descrição |
|---------|------|-----------|
| `Expedition.map` | Jogo | Mapa de jogo comercial |

---

## Referências

- Botea, A., Müller, M., Schaeffer, J. *Near optimal hierarchical pathfinding*. J. Game Dev., 2004.
- Jansen, M., Buro, M. *HPA\* enhancements*. AAAI AIIDE, 2007.
- Harabor, D., Grastien, A. *The JPS pathfinding system*. SoCS, 2012.
- Harabor, D., Grastien, A. *Improving jump point search*. ICAPS, 2014.

---

## Autores

Desenvolvido por **Rodrigo Seliprin** e **Gabriel Fuentes** — UFSM, 2025.
