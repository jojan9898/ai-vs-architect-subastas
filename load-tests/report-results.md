# Resultados de la Prueba de Carga

## Configuración

- 1000 usuarios intentan pujar el mismo monto (1) en una subasta que arranca en 0
- Solo 1 puja puede ganar — la siguiente debe ser MAYOR a la actual
- Si más de 1 devuelve HTTP 200 con el mismo monto: la invariante falló

## Cómo reproducirlo

```bash
docker compose up -d --build

# Vibecoder (puerto 8094)
docker run --rm --network host \
  -v $(pwd)/load-tests:/tests grafana/k6 run \
  -e BASE_URL=http://localhost:8094 /tests/stress-test-vibecoder.js

# Arquitecto (puerto 8095)
docker run --rm --network host \
  -v $(pwd)/load-tests:/tests grafana/k6 run \
  -e BASE_URL=http://localhost:8095 /tests/stress-test-architect.js
```

## Resultados

| | Vibecoder (con skills) | Arquitecto DDD |
|---|---|---|
| HTTP requests totales | 1000 | 1009 (9 reintentos por 409) |
| HTTP 200 responses | **1** (`user-492`) | **1** (`usuario-011`) |
| HTTP 400 responses | 999 | ~999 |
| HTTP 409 responses | 0 | ~9 (conflictos visibles, retried) |
| Dobles pujas aceptadas | **0** | **0** |
| `version` final en BD | 1 | 1 |
| Ganador en BD | `user-492` | `00000000-...-000000000011` |

Ambos enfoques protegen el invariante. La diferencia está en cómo:

- **Vibecoder**: el locking pesimista (`SELECT FOR UPDATE`) serializa el acceso — una transacción bloquea la fila, las demás esperan. Sin 409 para el cliente.
- **Arquitecto**: el `@Version` detecta conflictos y retorna 409 explícito — el cliente sabe exactamente qué pasó y puede reintentar con información.
