const path = require("path");
const os = require("os");
const dist = path.resolve("../../node_modules/sql.js/dist/")
const wasm = path.join(dist, "sql-wasm.wasm")
const worker = path.resolve("kotlin/sqljs.worker.js")
const badWorker = path.resolve("kotlin/bad.worker.js")

config.files.push({
    pattern: wasm,
    served: true,
    watched: false,
    included: false,
    nocache: false,
}, {
    pattern: worker,
    served: true,
    watched: false,
    included: false,
    nocache: false,
}, {
    pattern: badWorker,
    served: true,
    watched: false,
    included: false,
    nocache: false,
});

config.proxies["/sql-wasm.wasm"] = path.join("/absolute/", wasm)
config.proxies["/sqljs.worker.js"] = path.join("/absolute/", worker)
config.proxies["/bad.worker.js"] = path.join("/absolute/", badWorker)

// Adapted from: https://github.com/ryanclark/karma-webpack/issues/498#issuecomment-790040818
const output = {
  path: path.join(os.tmpdir(), '_karma_webpack_') + Math.floor(Math.random() * 1000000),
}
config.set({
  webpack: {...config.webpack, output}
});
config.files.push({
  pattern: `${output.path}/**/*`,
  watched: false,
  included: false,
});
