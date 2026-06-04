
# Performance Testing

> Use when writing performance tests or setting up benchmarking and baseline profiles.

- A dedicated **Macrobenchmark** module (`com.android.test`, the `:benchmark` module) measures startup and generates **Baseline Profiles**.
- Baseline Profiles ship with the app so hot code paths are AOT-compiled, improving cold-start and scroll performance.
- Keep the benchmark module separate from production modules; it depends on `:app` to exercise real user journeys.

## Single-module variant

Unchanged: the Macrobenchmark stays a **separate** `com.android.test` module even when the app itself is a single module — that's required for Macrobenchmark and Baseline Profiles. It depends on the app module and exercises real journeys the same way.
