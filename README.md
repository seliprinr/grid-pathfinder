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

### Teste rápido (mapa 10x10 gerado automaticamente)

```bash
mvn compile exec:java
```

### Benchmark em mapa real

```bash
mvn compile exec:java -Dexec.args="maps/seu_mapa.map results/output.csv"
```

Os mapas devem estar no formato `.map` do [Moving AI Lab](https://www.movingai.com/benchmarks/).

### Resultado esperado

```
Mapa carregado: Grid(256x256)
Pré-processando HPA*...
Pré-processando HPA-JPS...
Rodando benchmarks...
Resultados salvos em: results/output.csv
```

---

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

---

## Mapas utilizados

Mapas obtidos em [movingai.com/benchmarks](https://www.movingai.com/benchmarks/grids.html).

| Mapa | Tamanho | Densidade |
|------|---------|-----------|
| maze512.map | 512×512 | Alta |
| room256.map | 256×256 | Média |
| open1024.map | 1024×1024 | Baixa |

---

## Referências

- Botea, A., Müller, M., Schaeffer, J. *Near optimal hierarchical pathfinding*. J. Game Dev., 2004.
- Jansen, M., Buro, M. *HPA\* enhancements*. AAAI AIIDE, 2007.
- Harabor, D., Grastien, A. *The JPS pathfinding system*. SoCS, 2012.
- Harabor, D., Grastien, A. *Improving jump point search*. ICAPS, 2014.

---

## Autores

Desenvolvido por **Rodrigo Seliprin** e **Gabriel Fuentes** — UFSM, 2025.
