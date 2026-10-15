// Pure Algorithm Test Harness for GraphLab Frontend JS algorithms
// Extracted directly from frontend/index.html without altering UI/DOM code.

const assert = require('assert');

function createEnvironment(initialState = {}) {
  let nodes = initialState.nodes ? [...initialState.nodes] : [];
  let edges = initialState.edges ? initialState.edges.map(e => [...e]) : [];
  let start = initialState.start !== undefined ? initialState.start : 0;
  let target = initialState.target !== undefined ? initialState.target : 0;
  let isDirected = !!initialState.directed;
  let isWeighted = initialState.weighted !== undefined ? !!initialState.weighted : true;

  const D = () => isDirected;
  const WT = () => isWeighted;
  const Wt = e => WT() ? e[2] : 1;
  const nm = i => String.fromCharCode(65 + i);

  function adj() {
    const A = nodes.map(() => []);
    edges.forEach((e, i) => {
      A[e[0]].push({ to: e[1], w: Wt(e), ei: i });
      if (!D()) A[e[1]].push({ to: e[0], w: Wt(e), ei: i });
    });
    return A;
  }

  const mk = (cur, done, fr, tree, label, notes, x) => ({ cur, done: [...done], fr: [...fr], tree, label, notes, ...x });
  const treeOf = (par, done) => [...done].filter(x => par[x]).map(x => par[x].ei);
  const notesD = d => d.map((v, i) => v < Infinity ? `${nm(i)}=${v}` : null).filter(Boolean);
  const findEdge = (u, v) => edges.findIndex(e => (e[0] === u && e[1] === v) || (!D() && e[0] === v && e[1] === u));
  function pathNodes(par, t) {
    const p = [t];
    let c = t, g = 0;
    while (c !== start && par[c] && g++ < nodes.length) {
      c = par[c].u;
      p.push(c);
    }
    return c === start ? p.reverse() : [];
  }
  function uf(n) {
    const p = [...Array(n).keys()], f = x => p[x] === x ? x : (p[x] = f(p[x]));
    return {
      f,
      u: (a, b) => {
        a = f(a);
        b = f(b);
        if (a === b) return false;
        p[a] = b;
        return true;
      }
    };
  }
  const comps = U => nodes.map((_, i) => U.f(i));
  const groups = U => {
    const m = {};
    nodes.forEach((_, i) => (m[U.f(i)] = m[U.f(i)] || []).push(nm(i)));
    return Object.values(m).map(g => '{' + g.join('') + '}');
  };

  function bfs() {
    const A = adj(), S = [], done = new Set(), seen = new Set([start]), q = [start], par = {};
    while (q.length) {
      const u = q.shift(); done.add(u);
      for (const { to, ei } of A[u]) if (!seen.has(to)) { seen.add(to); q.push(to); par[to] = { u, ei } }
      S.push(mk(u, done, q, treeOf(par, done), `Visit ${nm(u)}. Queue: ${q.map(nm).join(' ') || 'empty'}`, q.map(nm)))
    }
    return { S, par, title: 'Queue (first in, first out)' }
  }

  function dfs() {
    const A = adj(), S = [], done = new Set(), st = [start], par = {};
    while (st.length) {
      const u = st.pop(); if (done.has(u)) continue; done.add(u);
      for (const { to, ei } of A[u].slice().reverse()) if (!done.has(to)) { st.push(to); par[to] = { u, ei } }
      S.push(mk(u, done, st.filter(x => !done.has(x)), treeOf(par, done), `Dive into ${nm(u)}. Stack: ${st.map(nm).join(' ') || 'empty'}`, st.map(nm)))
    }
    return { S, par, title: 'Stack (last in, first out)' }
  }

  function dij() {
    const A = adj(), S = [], done = new Set(), d = nodes.map(() => Infinity), par = {}; d[start] = 0;
    if (edges.some(e => Wt(e) < 0)) return { S: [mk(null, [], [], [], 'Dijkstra breaks on negative weights. Try Bellman-Ford.', [])], par, title: 'Distances', end: 'Dijkstra needs non-negative weights.' };
    while (true) {
      let u = -1; for (let i = 0; i < nodes.length; i++)if (!done.has(i) && d[i] < Infinity && (u < 0 || d[i] < d[u])) u = i; if (u < 0) break;
      done.add(u); for (const { to, w, ei } of A[u]) if (!done.has(to) && d[u] + w < d[to]) { d[to] = d[u] + w; par[to] = { u, ei } }
      S.push(mk(u, done, d.map((v, i) => v < Infinity && !done.has(i) ? i : -1).filter(i => i >= 0), treeOf(par, done), `Lock in ${nm(u)} at distance ${d[u]}.`, notesD(d)))
    }
    return { S, par, d, title: 'Distance table (best known so far)' }
  }

  function bf() {
    const S = [], d = nodes.map(() => Infinity), par = {}, E = []; d[start] = 0; let neg = false;
    edges.forEach((e, i) => { E.push([e[0], e[1], Wt(e), i]); if (!D()) E.push([e[1], e[0], Wt(e), i]) });
    for (let r = 1; r <= nodes.length; r++) {
      let ch = false;
      for (const [a, b, w, i] of E) if (d[a] + w < d[b]) { d[b] = d[a] + w; par[b] = { u: a, ei: i }; ch = true }
      const reach = d.map((v, i) => v < Infinity ? i : -1).filter(i => i >= 0);
      if (r === nodes.length && ch) { neg = true; S.push(mk(null, reach, [], [], `Pass ${r} still improved things: negative cycle.`, notesD(d))); break }
      S.push(mk(null, reach, [], treeOf(par, reach), `Pass ${r}: ${ch ? 'some distances improved.' : 'nothing changed, finished early.'}`, notesD(d))); if (!ch) break
    }
    return { S, par, d, neg, title: 'Distances after each pass' }
  }

  function fw() {
    const n = nodes.length, S = [], d = [], nx = [];
    for (let i = 0; i < n; i++) { d.push(Array(n).fill(Infinity)); nx.push(Array(n).fill(-1)); d[i][i] = 0; nx[i][i] = i }
    edges.forEach(e => {
      const w = Wt(e); if (w < d[e[0]][e[1]]) { d[e[0]][e[1]] = w; nx[e[0]][e[1]] = e[1] }
      if (!D() && w < d[e[1]][e[0]]) { d[e[1]][e[0]] = w; nx[e[1]][e[0]] = e[0] }
    });
    for (let k = 0; k < n; k++) {
      for (let i = 0; i < n; i++)for (let j = 0; j < n; j++)if (d[i][k] + d[k][j] < d[i][j]) { d[i][j] = d[i][k] + d[k][j]; nx[i][j] = nx[i][k] }
      S.push(mk(k, [], [], [], `Allow ${nm(k)} as a stop-over. Every pair is updated; showing distances from ${nm(start)}.`, notesD(d[start])))
    }
    const neg = d.some((r, i) => r[i] < 0), pn = [start]; let u = start, g = 0;
    while (!neg && u !== target && nx[u][target] >= 0 && g++ < n) { u = nx[u][target]; pn.push(u) }
    return { S, neg, d, pn: u === target ? pn : [], title: 'Distances from start, per stop-over' }
  }

  function kruskal() {
    const S = [], U = uf(nodes.length), tree = [], rej = []; let tot = 0;
    edges.map((e, i) => i).sort((a, b) => Wt(edges[a]) - Wt(edges[b])).forEach(i => {
      const e = edges[i], ok = U.u(e[0], e[1]);
      if (ok) { tree.push(i); tot += Wt(e) } else rej.push(i);
      S.push(mk(null, [], [], [...tree], `${nm(e[0])}-${nm(e[1])} (${Wt(e)}): ${ok ? 'joins two groups, keep it.' : 'already connected, skip it.'}`, groups(U), { rej: [...rej], hot: i, comp: comps(U) }))
    });
    return { S, tree, tot, end: `Minimum spanning forest: ${tree.length} edges, total weight ${tot}. Edges treated as undirected.`, title: 'Groups (union-find sets)' }
  }

  function prim() {
    const S = [], inT = new Set([start]), tree = []; let tot = 0;
    while (true) {
      let b = -1; edges.forEach((e, i) => { if (inT.has(e[0]) !== inT.has(e[1]) && (b < 0 || Wt(e) < Wt(edges[b]))) b = i }); if (b < 0) break;
      const e = edges[b], v = inT.has(e[0]) ? e[1] : e[0]; inT.add(v); tree.push(b); tot += Wt(e);
      const cr = edges.map((x, i) => i).filter(i => inT.has(edges[i][0]) !== inT.has(edges[i][1]));
      S.push(mk(v, inT, cr.map(i => inT.has(edges[i][0]) ? edges[i][1] : edges[i][0]), [...tree], `Cheapest edge leaving the tree: ${nm(e[0])}-${nm(e[1])} (${Wt(e)}). Add ${nm(v)}.`, cr.map(i => `${nm(edges[i][0])}-${nm(edges[i][1])}:${Wt(edges[i])}`)))
    }
    return { S, tree, tot, end: `Spanning tree from ${nm(start)}: ${tree.length} edges, total weight ${tot}. Edges treated as undirected.`, title: 'Edges crossing the cut' }
  }

  function ufDemo() {
    const S = [], U = uf(nodes.length);
    edges.forEach((e, i) => { const ok = U.u(e[0], e[1]); S.push(mk(null, [], [], [], `union(${nm(e[0])}, ${nm(e[1])}): ${ok ? 'merged two groups.' : 'already in the same group.'}`, groups(U), { hot: i, comp: comps(U) })) });
    return { S, U, end: `${new Set(comps(U)).size} connected group(s). Colors show the groups.`, title: 'Groups (union-find sets)' }
  }

  function runResult(kind) {
    const algos = { bfs, dfs, dij, bf, fw, kru: kruskal, prim, uf: ufDemo };
    const R = algos[kind]();
    const last = R.S.length ? R.S[R.S.length - 1] : { cur: null, done: [], fr: [], tree: [], notes: [] };
    let label = R.end, path = [], done = last.done || [];
    if (!label) {
      if (R.neg) label = 'Negative cycle found, so shortest paths are undefined.';
      else {
        const pn = R.pn || pathNodes(R.par || {}, target);
        for (let i = 0; i + 1 < pn.length; i++) path.push(findEdge(pn[i], pn[i + 1]));
        if (!done.length) done = pn;
        label = pn.length ? `Path: ${pn.map(nm).join(' > ')}` : `${nm(target)} can't be reached from ${nm(start)}.`;
      }
    }
    return { R, label, path, pn: R.pn || pathNodes(R.par || {}, target) };
  }

  return {
    nodes, edges, start, target,
    bfs, dfs, dij, bf, fw, kruskal, prim, ufDemo, pathNodes, runResult
  };
}

// ---------------------- TEST SUITE ----------------------
let passed = 0;
let failed = 0;

function runTest(name, fn) {
  try {
    fn();
    console.log(`[PASS] ${name}`);
    passed++;
  } catch (err) {
    console.error(`[FAIL] ${name}: ${err.message}`);
    failed++;
  }
}

// Reference graph configuration
const refNodes = [
  { x: 80, y: 200 },  // A (0)
  { x: 250, y: 110 }, // B (1)
  { x: 240, y: 300 }, // C (2)
  { x: 440, y: 120 }, // D (3)
  { x: 460, y: 300 }, // E (4)
  { x: 630, y: 200 }  // F (5)
];
const refEdges = [
  [0, 1, 4], // A-B 4
  [0, 2, 2], // A-C 2
  [1, 2, 1], // B-C 1
  [1, 3, 5], // B-D 5
  [2, 4, 7], // C-E 7
  [3, 4, 4], // D-E 4
  [3, 5, 3], // D-F 3
  [4, 5, 2]  // E-F 2
];

console.log('=== RUNNING FRONTEND JS ALGORITHM TESTS ===\n');

// STEP 2 Tests on Reference Graph
runTest('Reference Graph - Dijkstra from A', () => {
  const env = createEnvironment({ nodes: refNodes, edges: refEdges, start: 0, target: 5 });
  const res = env.runResult('dij');
  assert.deepStrictEqual(res.R.d, [0, 3, 2, 8, 9, 11]);
  assert.strictEqual(res.R.d[5], 11);
  const pathStr = res.pn.map(i => String.fromCharCode(65 + i)).join(' > ');
  assert(pathStr === 'A > C > B > D > F' || pathStr === 'A > C > E > F', `Unexpected path: ${pathStr}`);
});

runTest('Reference Graph - Bellman-Ford from A', () => {
  const env = createEnvironment({ nodes: refNodes, edges: refEdges, start: 0, target: 5 });
  const res = env.runResult('bf');
  assert.deepStrictEqual(res.R.d, [0, 3, 2, 8, 9, 11]);
  assert.strictEqual(res.R.neg, false);
  const pathStr = res.pn.map(i => String.fromCharCode(65 + i)).join(' > ');
  assert(pathStr === 'A > C > B > D > F' || pathStr === 'A > C > E > F', `Unexpected path: ${pathStr}`);
});

runTest('Reference Graph - Kruskal MST', () => {
  const env = createEnvironment({ nodes: refNodes, edges: refEdges, start: 0, target: 5 });
  const res = env.kruskal();
  assert.strictEqual(res.tot, 13, `MST total weight was ${res.tot}, expected 13`);
  assert.strictEqual(res.tree.length, 5, `MST edge count was ${res.tree.length}, expected 5`);
});

runTest('Reference Graph - Prim MST', () => {
  const env = createEnvironment({ nodes: refNodes, edges: refEdges, start: 0, target: 5 });
  const res = env.prim();
  assert.strictEqual(res.tot, 13, `MST total weight was ${res.tot}, expected 13`);
  assert.strictEqual(res.tree.length, 5, `MST edge count was ${res.tree.length}, expected 5`);
});

runTest('Reference Graph - Floyd-Warshall All-Pairs', () => {
  const env = createEnvironment({ nodes: refNodes, edges: refEdges, start: 0, target: 5 });
  const res = env.fw();
  assert.strictEqual(res.neg, false);
  assert.deepStrictEqual(res.d[0], [0, 3, 2, 8, 9, 11], `Row A does not match Dijkstra: ${JSON.stringify(res.d[0])}`);
  assert.strictEqual(res.d[0][5], 11);
});

runTest('Reference Graph - BFS traversal & path', () => {
  const env = createEnvironment({ nodes: refNodes, edges: refEdges, start: 0, target: 5 });
  const res = env.runResult('bfs');
  const visited = res.R.S.map(s => s.cur);
  assert.strictEqual(new Set(visited).size, 6, 'BFS did not visit all 6 nodes');
  assert(res.pn.length > 0, 'BFS did not find path');
  assert.strictEqual(res.pn[0], 0);
  assert.strictEqual(res.pn[res.pn.length - 1], 5);
});

runTest('Reference Graph - DFS traversal', () => {
  const env = createEnvironment({ nodes: refNodes, edges: refEdges, start: 0, target: 5 });
  const res = env.runResult('dfs');
  const visited = res.R.S.map(s => s.cur);
  assert.strictEqual(new Set(visited).size, 6, 'DFS did not visit all 6 nodes');
});

runTest('Reference Graph - Union-Find Demo', () => {
  const env = createEnvironment({ nodes: refNodes, edges: refEdges, start: 0, target: 5 });
  const res = env.ufDemo();
  assert(res.end.includes('1 connected group(s)'), `Unexpected group count message: ${res.end}`);
});

// STEP 3 - Edge Cases
runTest('Edge Case - Disconnected Graph (target unreachable)', () => {
  const discNodes = [...refNodes, { x: 700, y: 350 }]; // Node G (6)
  const env = createEnvironment({ nodes: discNodes, edges: refEdges, start: 0, target: 6 });
  const dRes = env.runResult('dij');
  assert.strictEqual(dRes.label, "G can't be reached from A.");
  const bfRes = env.runResult('bf');
  assert.strictEqual(bfRes.label, "G can't be reached from A.");
  const bfsRes = env.runResult('bfs');
  assert.strictEqual(bfsRes.label, "G can't be reached from A.");
});

runTest('Edge Case - Start Equals Target (start=A, target=A)', () => {
  const env = createEnvironment({ nodes: refNodes, edges: refEdges, start: 0, target: 0 });
  const dRes = env.runResult('dij');
  assert.strictEqual(dRes.label, 'Path: A');
  assert.deepStrictEqual(dRes.pn, [0]);
  const fwRes = env.runResult('fw');
  assert.strictEqual(fwRes.label, 'Path: A');
});

runTest('Edge Case - Single Node Graph', () => {
  const env = createEnvironment({ nodes: [{ x: 100, y: 100 }], edges: [], start: 0, target: 0 });
  const dRes = env.runResult('dij');
  assert.strictEqual(dRes.label, 'Path: A');
  const kRes = env.kruskal();
  assert.strictEqual(kRes.tot, 0);
  assert.strictEqual(kRes.tree.length, 0);
  const pRes = env.prim();
  assert.strictEqual(pRes.tot, 0);
  assert.strictEqual(pRes.tree.length, 0);
});

runTest('Edge Case - Empty Graph (no crash)', () => {
  const env = createEnvironment({ nodes: [], edges: [], start: 0, target: 0 });
  const kRes = env.kruskal();
  assert.strictEqual(kRes.tot, 0);
  assert.strictEqual(kRes.tree.length, 0);
});

runTest('Edge Case - Directed Mode On', () => {
  // A -> B (4), B -> C (1). C cannot reach A.
  const dirNodes = [{ x: 100, y: 100 }, { x: 200, y: 100 }, { x: 300, y: 100 }];
  const dirEdges = [[0, 1, 4], [1, 2, 1]];
  const env = createEnvironment({ nodes: dirNodes, edges: dirEdges, start: 2, target: 0, directed: true });
  const dRes = env.runResult('dij');
  assert.strictEqual(dRes.label, "A can't be reached from C.");
});

runTest('Edge Case - Negative Weights without Negative Cycle', () => {
  // A -> B (4), B -> C (-2), A -> C (5)
  const dirNodes = [{ x: 100, y: 100 }, { x: 200, y: 100 }, { x: 300, y: 100 }];
  const dirEdges = [[0, 1, 4], [1, 2, -2], [0, 2, 5]];
  const env = createEnvironment({ nodes: dirNodes, edges: dirEdges, start: 0, target: 2, directed: true });
  // Bellman-Ford should compute shortest path A->B->C = 2
  const bfRes = env.bf();
  assert.strictEqual(bfRes.d[2], 2);
  assert.strictEqual(bfRes.neg, false);
  // Dijkstra should flag negative weights
  const dijRes = env.dij();
  assert.strictEqual(dijRes.end, 'Dijkstra needs non-negative weights.');
});

runTest('Edge Case - Negative Cycle Detection', () => {
  // Directed cycle: A -> B (1), B -> C (-4), C -> A (1). Sum = -2
  const cycleNodes = [{ x: 100, y: 100 }, { x: 200, y: 100 }, { x: 300, y: 100 }];
  const cycleEdges = [[0, 1, 1], [1, 2, -4], [2, 0, 1]];
  const env = createEnvironment({ nodes: cycleNodes, edges: cycleEdges, start: 0, target: 2, directed: true });
  const bfRes = env.bf();
  assert.strictEqual(bfRes.neg, true, 'Bellman-Ford failed to detect negative cycle');
  const fwRes = env.fw();
  assert.strictEqual(fwRes.neg, true, 'Floyd-Warshall failed to detect negative cycle');
});

runTest('Edge Case - Duplicate Edges / Equal-Weight Ties', () => {
  const nodes = [{ x: 100, y: 100 }, { x: 200, y: 100 }];
  const edges = [[0, 1, 3], [0, 1, 3]];
  const env = createEnvironment({ nodes, edges, start: 0, target: 1 });
  const dRes = env.runResult('dij');
  assert.strictEqual(dRes.R.d[1], 3);
  const kRes = env.kruskal();
  assert.strictEqual(kRes.tot, 3);
  assert.strictEqual(kRes.tree.length, 1);
});

console.log(`\n=== FRONTEND JS SUMMARY: ${passed} passed, ${failed} failed ===`);
if (failed > 0) process.exit(1);
