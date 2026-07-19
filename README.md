# GraphTheoryEngine

A graph theory library I'm building from scratch in Java, mainly as a self-study project to actually understand how graph algorithms work under the hood instead of just calling library functions.

## Why I'm building this

I'm on semester break and wanted a project that combines what I've already studied (discrete math, linear data structures) with the Java I'll be using next semester. I got curious about how algorithms like Dijkstra's and A* actually work after seeing them compared online, and figured the best way to really understand them was to build a mini version of a graph engine myself instead of just reading about it.

The goal isn't to make the next JGraphT — it's to genuinely learn how the algorithms that power things like Google Maps routing or LinkedIn's "degrees of connection" actually work internally.

## Rules I'm following for this project

- No external graph libraries (no JGraphT, no shortcuts) — everything is built by hand
- Interfaces first, implementations second, so algorithms don't care whether the graph is stored as a list or a matrix underneath
- No copying from tutorials or repos — I only use official documentation to understand concepts, then write the logic myself
- 
## Project structure

```
GraphTheoryEngine/
├── src/
│   ├── Main.java
│   ├── graph/
│   │   ├── Graph.java                 -> interface all graph types follow
│   │   ├── Edge.java                  -> holds a destination + weight
│   │   ├── AdjacencyListGraph.java    -> graph stored as a HashMap of lists
│   │   └── AdjacencyMatrixGraph.java  -> graph stored as a 2D array
│   ├── algorithms/                    -> BFS, DFS, Dijkstra etc. go here later
│   └── utils/
├── .gitignore
└── README.md
```

## Progress

### Phase 1 — Core graph representations (in progress)
- [x] Graph interface
- [x] Edge class
- [x] Adjacency list implementation
- [ ] Adjacency matrix implementation

### Phase 2 — Classic algorithms
- [ ] BFS
- [ ] DFS
- [ ] Dijkstra's shortest path
- [ ] Bellman-Ford
- [ ] Floyd-Warshall
- [ ] Kruskal's MST
- [ ] Prim's MST

### Phase 3 — Harder stuff
- [ ] Max flow (Ford-Fulkerson)
- [ ] Graph coloring
- [ ] Cycle detection

### Phase 4 — NP-hard territory
- [ ] Hamiltonian path/cycle
- [ ] Tarjan's SCC

### Phase 5 — Visualizer (the fun part)
- [ ] Turn this into a mini town-route simulator with nodes as landmarks and animated pathfinding

## Tools I'm using

- Java (plain, no Maven/Gradle, keeping it simple)
- IntelliJ IDEA Community Edition
- Git + GitHub for version control

## Notes

This is a learning project, so the code isn't going to be perfectly optimized or "production ready" — the point is understanding,
not polish. I'm building it step by step, explaining every concept to myself before writing it, 
and only writing code after I actually get the logic on my own.
