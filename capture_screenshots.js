const { spawn } = require('child_process');
const http = require('http');
const fs = require('fs');
const path = require('path');

const CHROME_PATH = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';
const PORT = 9345;
const HTML_URL = 'file:///c:/Users/Tanishk/OneDrive/Desktop/Graph_Lab-1/frontend/index.html';
const OUT_DIR = path.resolve(__dirname, 'test-screenshots');

if (!fs.existsSync(OUT_DIR)) {
  fs.mkdirSync(OUT_DIR, { recursive: true });
}

function getJson(url) {
  return new Promise((resolve, reject) => {
    http.get(url, res => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        try { resolve(JSON.parse(data)); } catch (e) { reject(e); }
      });
    }).on('error', reject);
  });
}

function sleep(ms) {
  return new Promise(r => setTimeout(r, ms));
}

class CDPClient {
  constructor(wsUrl) {
    this.wsUrl = wsUrl;
    this.id = 1;
    this.callbacks = new Map();
    this.consoleErrors = [];
  }

  connect() {
    return new Promise((resolve, reject) => {
      this.ws = new WebSocket(this.wsUrl);
      this.ws.onopen = () => resolve();
      this.ws.onerror = err => reject(err);
      this.ws.onmessage = evt => {
        const msg = JSON.parse(evt.data);
        if (msg.method === 'Runtime.consoleAPICalled') {
          if (msg.params.type === 'error') {
            const text = msg.params.args.map(a => a.value || a.description).join(' ');
            this.consoleErrors.push(text);
          }
        }
        if (msg.method === 'Runtime.exceptionThrown') {
          this.consoleErrors.push(msg.params.exceptionDetails.text);
        }
        if (msg.id && this.callbacks.has(msg.id)) {
          const { res, rej } = this.callbacks.get(msg.id);
          this.callbacks.delete(msg.id);
          if (msg.error) rej(msg.error);
          else res(msg.result);
        }
      };
    });
  }

  send(method, params = {}) {
    return new Promise((res, rej) => {
      const id = this.id++;
      this.callbacks.set(id, { res, rej });
      this.ws.send(JSON.stringify({ id, method, params }));
    });
  }

  async eval(expression) {
    const res = await this.send('Runtime.evaluate', {
      expression,
      awaitPromise: true,
      returnByValue: true
    });
    if (res.exceptionDetails) {
      const desc = res.exceptionDetails.exception ? (res.exceptionDetails.exception.description || res.exceptionDetails.exception.value) : res.exceptionDetails.text;
      throw new Error(desc || 'Eval exception');
    }
    return res.result ? res.result.value : undefined;
  }

  async screenshot(filePath) {
    const res = await this.send('Page.captureScreenshot', { format: 'png', captureBeyondViewport: true });
    fs.writeFileSync(filePath, Buffer.from(res.data, 'base64'));
  }
}

async function main() {
  console.log('Launching headless Chrome...');
  const tmpProfile = path.join(require('os').tmpdir(), 'chrome_gl_' + Date.now());
  const chrome = spawn(CHROME_PATH, [
    '--headless=new',
    `--remote-debugging-port=${PORT}`,
    `--user-data-dir=${tmpProfile}`,
    '--disable-gpu',
    '--no-first-run',
    '--no-default-browser-check',
    '--window-size=1200,1200',
    '--hide-scrollbars'
  ]);

  let cdp = null;
  const results = [];

  try {
    let versionInfo = null;
    for (let i = 0; i < 30; i++) {
      await sleep(300);
      try {
        versionInfo = await getJson(`http://127.0.0.1:${PORT}/json/version`);
        break;
      } catch (e) {}
    }
    if (!versionInfo) throw new Error('Could not connect to Chrome debugging port');

    const targets = await getJson(`http://127.0.0.1:${PORT}/json/list`);
    const pageTarget = targets.find(t => t.type === 'page') || targets[0];
    cdp = new CDPClient(pageTarget.webSocketDebuggerUrl);
    await cdp.connect();
    await cdp.send('Page.enable');
    await cdp.send('Runtime.enable');

    console.log('Navigating to', HTML_URL);
    await cdp.send('Page.navigate', { url: HTML_URL });
    
    // Wait for document to be ready
    for (let i = 0; i < 50; i++) {
      await sleep(200);
      try {
        const ready = await cdp.eval("document.readyState === 'complete' && !!document.getElementById('spd')");
        if (ready) break;
      } catch (e) {}
    }

    // Speed up pen animation to 10
    await cdp.eval(`document.getElementById('spd').value = 10;`);

    // Helper to reload reference graph
    const resetRefGraph = async () => {
      await cdp.eval(`
        nodes = [{ x: 80, y: 200 }, { x: 250, y: 110 }, { x: 240, y: 300 }, { x: 440, y: 120 }, { x: 460, y: 300 }, { x: 630, y: 200 }];
        edges = [[0, 1, 4], [0, 2, 2], [1, 2, 1], [1, 3, 5], [2, 4, 7], [3, 4, 4], [3, 5, 3], [4, 5, 2]];
        start = 0; target = 5; view = null; running = false;
        $('wt').checked = true;
        $('dir').checked = false;
        $('spd').value = 10;
        render();
      `);
      await sleep(100);
    };

    const algos = [
      { key: 'bfs', name: 'BFS', file: 'bfs_reference.png' },
      { key: 'dfs', name: 'DFS', file: 'dfs_reference.png' },
      { key: 'dij', name: 'Dijkstra', file: 'dijkstra_reference.png' },
      { key: 'bf', name: 'Bellman-Ford', file: 'bellman_ford_reference.png' },
      { key: 'fw', name: 'Floyd-Warshall', file: 'floyd_warshall_reference.png' },
      { key: 'kru', name: 'Kruskal', file: 'kruskal_reference.png' },
      { key: 'prim', name: 'Prim', file: 'prim_reference.png' },
      { key: 'uf', name: 'Union-Find', file: 'union_find_reference.png' },
    ];

    for (const alg of algos) {
      console.log(`\nTesting ${alg.name}...`);
      await resetRefGraph();
      await cdp.eval(`run('${alg.key}')`);

      // Poll until running is false
      let done = false;
      for (let t = 0; t < 60; t++) {
        await sleep(200);
        const isRunning = await cdp.eval(`running`);
        if (!isRunning) {
          done = true;
          break;
        }
      }
      if (!done) console.warn(`Timeout waiting for ${alg.name}`);
      await sleep(300);

      const logText = await cdp.eval(`$('log').textContent`);
      const notesTitle = await cdp.eval(`$('ft').textContent`);
      const notesList = await cdp.eval(`Array.from(document.querySelectorAll('#fr .note')).map(e => e.textContent)`);
      const filePath = path.join(OUT_DIR, alg.file);
      await cdp.screenshot(filePath);

      console.log(`Saved screenshot to ${filePath}`);
      console.log(`  Log: "${logText}"`);
      console.log(`  Title: "${notesTitle}"`);
      console.log(`  Notes: [${notesList.join(', ')}]`);

      results.push({
        algo: alg.name,
        scenario: 'Reference Graph (A to F)',
        file: alg.file,
        log: logText,
        title: notesTitle,
        notes: notesList
      });
    }

    // SCENARIO: Directed Mode
    console.log(`\nTesting Directed Mode...`);
    await resetRefGraph();
    await cdp.eval(`
      $('dir').checked = true;
      start = 0; target = 5;
      render();
    `);
    await cdp.eval(`run('dij')`);
    for (let t = 0; t < 60; t++) {
      await sleep(200);
      if (!(await cdp.eval(`running`))) break;
    }
    await sleep(300);
    const dirLog = await cdp.eval(`$('log').textContent`);
    const dirFile = 'directed_mode_dijkstra.png';
    await cdp.screenshot(path.join(OUT_DIR, dirFile));
    console.log(`Directed mode log: "${dirLog}"`);
    results.push({
      algo: 'Dijkstra',
      scenario: 'Directed Mode on Reference Graph',
      file: dirFile,
      log: dirLog
    });

    // SCENARIO: Negative Weight Edge (Bellman-Ford & Dijkstra)
    console.log(`\nTesting Negative Weight Edge...`);
    await resetRefGraph();
    // Change edge 2 (B-C) to -3
    await cdp.eval(`
      edges[2][2] = -3;
      render();
    `);
    // Run Bellman-Ford
    await cdp.eval(`run('bf')`);
    for (let t = 0; t < 60; t++) {
      await sleep(200);
      if (!(await cdp.eval(`running`))) break;
    }
    await sleep(300);
    const bfNegLog = await cdp.eval(`$('log').textContent`);
    const bfNegFile = 'negative_weight_bellman_ford.png';
    await cdp.screenshot(path.join(OUT_DIR, bfNegFile));
    console.log(`Bellman-Ford negative weight log: "${bfNegLog}"`);
    results.push({
      algo: 'Bellman-Ford',
      scenario: 'Negative Edge Weight (B-C=-3)',
      file: bfNegFile,
      log: bfNegLog
    });

    // Run Dijkstra on Negative Weight Edge
    await cdp.eval(`run('dij')`);
    await sleep(500);
    const dijNegLog = await cdp.eval(`$('log').textContent`);
    const dijNegFile = 'negative_weight_dijkstra.png';
    await cdp.screenshot(path.join(OUT_DIR, dijNegFile));
    console.log(`Dijkstra negative weight log: "${dijNegLog}"`);
    results.push({
      algo: 'Dijkstra',
      scenario: 'Negative Edge Weight (Flagged)',
      file: dijNegFile,
      log: dijNegLog
    });

    // SCENARIO: Disconnected Node
    console.log(`\nTesting Disconnected Node...`);
    await resetRefGraph();
    await cdp.eval(`
      nodes.push({ x: 680, y: 350 }); // Node G (6)
      start = 0; target = 6;
      render();
    `);
    await cdp.eval(`run('dij')`);
    for (let t = 0; t < 60; t++) {
      await sleep(200);
      if (!(await cdp.eval(`running`))) break;
    }
    await sleep(300);
    const discLog = await cdp.eval(`$('log').textContent`);
    const discFile = 'disconnected_node_dijkstra.png';
    await cdp.screenshot(path.join(OUT_DIR, discFile));
    console.log(`Disconnected node log: "${discLog}"`);
    results.push({
      algo: 'Dijkstra',
      scenario: 'Disconnected Node G unreachable',
      file: discFile,
      log: discLog
    });

    // SCENARIO: Directed Graph with Negative Weight (No cycle)
    console.log(`\nTesting Directed Negative Weight (DAG)...`);
    await cdp.eval(`
      nodes = [{ x: 150, y: 200 }, { x: 350, y: 120 }, { x: 550, y: 200 }]; // A(0), B(1), C(2)
      edges = [[0, 1, 4], [1, 2, -2], [0, 2, 5]]; // A->B 4, B->C -2, A->C 5
      start = 0; target = 2;
      $('wt').checked = true;
      $('dir').checked = true;
      render();
    `);
    await cdp.eval(`run('bf')`);
    for (let t = 0; t < 60; t++) {
      await sleep(200);
      if (!(await cdp.eval(`running`))) break;
    }
    await sleep(300);
    const dagNegLog = await cdp.eval(`$('log').textContent`);
    const dagNegFile = 'directed_negative_weight_bellman_ford.png';
    await cdp.screenshot(path.join(OUT_DIR, dagNegFile));
    console.log(`Directed Bellman-Ford negative weight log: "${dagNegLog}"`);
    results.push({
      algo: 'Bellman-Ford',
      scenario: 'Directed DAG Negative Weight (A->B->C cost 2)',
      file: dagNegFile,
      log: dagNegLog
    });

    // SCENARIO: Start equals Target
    console.log(`\nTesting Start Equals Target...`);
    await resetRefGraph();
    await cdp.eval(`
      start = 0; target = 0;
      render();
    `);
    await cdp.eval(`run('dij')`);
    for (let t = 0; t < 60; t++) {
      await sleep(200);
      if (!(await cdp.eval(`running`))) break;
    }
    await sleep(300);
    const sameLog = await cdp.eval(`$('log').textContent`);
    const sameFile = 'start_equals_target_dijkstra.png';
    await cdp.screenshot(path.join(OUT_DIR, sameFile));
    console.log(`Start equals target log: "${sameLog}"`);
    results.push({
      algo: 'Dijkstra',
      scenario: 'Start Equals Target (A to A)',
      file: sameFile,
      log: sameLog
    });

    console.log('\n=== BROWSER CONSOLE ERRORS ===');
    if (cdp.consoleErrors.length === 0) {
      console.log('No console errors reported! (Clean pass)');
    } else {
      console.warn('Console errors detected:');
      cdp.consoleErrors.forEach(e => console.warn('  ' + e));
    }

    fs.writeFileSync(path.join(OUT_DIR, 'test_results.json'), JSON.stringify({
      results,
      consoleErrors: cdp.consoleErrors
    }, null, 2));

  } finally {
    if (chrome) {
      chrome.kill();
    }
  }
}

main().catch(err => {
  console.error('Fatal error:', err);
  process.exit(1);
});
