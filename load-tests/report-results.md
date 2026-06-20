# Resultados — Vibecoder+skills vs Architect (Subastas)

## El experimento

1000 usuarios ofertan monto 1 en una subasta que arranca en 0, simultáneamente. Solo
una oferta de 1 puede ganar (las demás no son estrictamente mayores → 400). **El
invariante: la oferta más alta solo aumenta, y un mismo monto se acepta una sola vez.**
Sin locking, dos ofertas leerían la oferta actual como 0 y ambas aceptarían 1 — pérdida
de datos.

## Cómo reproducir

```bash
docker compose up -d --build
docker run --rm --network host -v $(pwd)/load-tests:/tests grafana/k6 run \
  -e BASE_URL=http://localhost:8094 /tests/stress-test-vibecoder.js
docker run --rm --network host -v $(pwd)/load-tests:/tests grafana/k6 run \
  -e BASE_URL=http://localhost:8095 /tests/stress-test-architect.js
```

(Endpoints distintos: el vibecoder usa `POST /subastas/{id}/ofertar?monto&usuarioId`;
el arquitecto usa `POST /auctions/{id}/bids` con body JSON.)

## Resultados reales

| | Vibecoder + skills | Architect |
|---|---|---|
| Ofertas aceptadas con 200 (debe ser exactamente 1) | **1 ✅** | **1 ✅** |
| Oferta más alta final | 1.00 | 1.00 |
| Doble aceptación del mismo monto | **0** | **0** |
| Requests totales (con reintentos 409) | 1009 | 1009 |
| Tests | 19 ✅ | 27 ✅ |

Ambos respetan el invariante: una sola oferta de 1 gana, el resto recibe 400 o 409. Los
dos aplican `@Version`. La oferta más alta nunca retrocede ni se duplica.

En la versión vieja de esta serie el naive aceptaba el mismo monto más de una vez bajo
carga. Las skills `java-concurrency-integrity` le dieron al vibecoder la misma defensa.
