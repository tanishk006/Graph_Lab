# GraphTheoryEngineAPI

A Spring Boot REST wrapper around the `GraphTheoryEngine` core library (`Graph`, `Edge`, `AdjacencyListGraph`, `AdjacencyMatrixGraph`). Stateless, no auth, no signup — anyone can send a graph and get its structure back as JSON. Built to sit behind a public visualizer frontend.

This is a separate Maven project from the core `GraphTheoryEngine` repo. The core classes (`Graph.java`, `Edge.java`, `AdjacencyListGraph.java`, `AdjacencyMatrixGraph.java`) are copied in as-is under `src/main/java/graph/` so the API has no external dependency on the other project — just plain Java source, same package.

## Two small fixes made to the copied core files (flagging these clearly — not silent changes)

1. **`AdjacencyListGraph.getNeighbours`** — added a null check. If a vertex was never used as a `source` in any `addEdge` call, `adglist.get(vertex)` returns `null`, and looping over `null` throws a `NullPointerException`. Now it returns an empty list instead. Worth pulling this same fix into your original file too, since it'll crash for the same reason there.

2. **`AdjacencyMatrixGraph` constructor** — now fills every cell with `Double.POSITIVE_INFINITY` on creation. This is a real bug worth knowing about in your original: Java initializes `double[][]` arrays to `0.0` by default, not infinity. Since your `getNeighbours` checks `matrix[vertex][i] < Double.POSITIVE_INFINITY`, every untouched cell (which is `0.0`) was passing that check — meaning **every vertex looked connected to every other vertex with weight 0** before any edge was even added. Worth fixing in your own file the same way.

## How to run this

1. Open this folder (`GraphTheoryEngineAPI/`) as its own project in IntelliJ (File → Open, point at this folder specifically, not the parent repo)
2. IntelliJ will detect the `pom.xml` and prompt to load it as a Maven project — accept
3. Let Maven download dependencies (needs internet access, first run only)
4. Run `GraphEngineApiApplication.java` (right-click → Run)
5. Server starts on `http://localhost:8080`

## Endpoints

### `GET /`
Health check. Returns `{"status": "up", "service": "GraphTheoryEngineAPI"}`.

### `POST /api/graph/build`
Builds a graph and returns every vertex's neighbours.

**Request body:**
```json
{
  "type": "list",
  "vertexCount": 4,
  "edges": [
    { "source": 0, "destination": 1, "weight": 4.0 },
    { "source": 1, "destination": 2, "weight": 2.5 },
    { "source": 0, "destination": 3, "weight": 7.0 }
  ]
}
```
- `type`: `"list"` or `"matrix"`
- `vertexCount`: required for `"matrix"` (fixed-size array needs it upfront). Optional for `"list"` — if omitted, it's inferred from the highest vertex number in your edges.
- `edges`: array of `{source, destination, weight}`

**Response:**
```json
{
  "type": "list",
  "vertexCount": 4,
  "neighbours": {
    "0": [1, 3],
    "1": [2],
    "2": [],
    "3": []
  }
}
```

### `GET /api/stats/runs`
Returns how many times `/api/graph/build` has been called since the server started.
```json
{ "totalRuns": 17 }
```
Note: this is an in-memory counter — it resets every time the server restarts. If you want the count to survive restarts/deployments, that needs a database or a file to persist it — flagging this now so it's not a surprise later.

## What's intentionally not here yet

No BFS/DFS/Dijkstra endpoints — the core project doesn't have those algorithm classes yet. Once you write them (as their own classes, following your usual interface pattern), tell me and I'll add the matching endpoints (e.g. `POST /api/algorithms/bfs`) that call into them and return the traversal order/steps.

No database, no auth, no rate limiting — matches the "no signup, just use it" goal. If this ever goes properly public, rate limiting is worth adding so one person can't hammer the free-tier server, but not needed for a first version.

## CORS

Every controller currently allows requests from any origin (`@CrossOrigin(origins = "*")`), since the frontend isn't built yet and will likely be hosted on a different domain than the API. Once you know the actual frontend URL, this should be tightened to that specific origin instead of `*`.
