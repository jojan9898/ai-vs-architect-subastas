# Vibecoder vs Arquitecto — Sistema de Subastas

Una comparación empírica de dos enfoques para el mismo problema: un sistema de pujas bajo carga concurrente.

**La pregunta**: ¿alcanza con usar skills y patrones que la IA conoce, o hace falta guía de dominio explícita?

---

## El experimento

1000 usuarios intentan hacer la misma puja (monto 1) en una subasta que arranca en 0, simultáneamente.
Solo 1 puede ganar — la siguiente puja debe ser **mayor** a la actual.
Si más de 1 devuelve HTTP 200 con el mismo monto: la invariante falló.

---

## Dos enfoques

### `vibecoder-approach` — IA con skills, prompt básico

El vibecoder usó skills de JPA disponibles en el entorno y llegó a una solución que funciona:

```java
// Locking PESIMISTA: SELECT FOR UPDATE bloquea la fila
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<Auction> findByIdForUpdate(@Param("id") Long id);
```

- Previene la doble puja serializando el acceso a la fila
- Sin 409 — los conflictos de concurrencia no son visibles para el cliente
- Modelo anémico: `auction.setCurrentHighestBid(amount)` — el objeto no protege su propio estado
- Sin tests
- Sin Value Object para la oferta (solo `BigDecimal amount + String userId`)

### `architect-approach` — IA guiada con diseño DDD

- `Oferta` es un **Objeto de Valor inmutable** — monto, usuario y timestamp como unidad
- `Subasta` es la **Raíz del Agregado** — solo ella puede aceptar o rechazar una oferta
- `@Version` (Bloqueo Optimista) hace los conflictos **visibles** como HTTP 409
- El cliente puede reintentar con información — no es un error de servidor
- Tests de dominio + test de concurrencia

```
domain/
  Subasta.java     ← Raíz del Agregado
  Oferta.java      ← Objeto de Valor (inmutable)
  SubastaRepository ← puerto (interfaz pura)
application/
  HacerOfertaUseCase.java
infrastructure/
  jpa/             ← OfertaEmbeddable, SubastaEntity (@Version)
  controllers/     ← SubastaController
  exceptionhandler/ ← 409 en OptimisticLockException
```

---

## La diferencia real

Ambos previenen la doble puja. La diferencia está en el modelo:

| | Vibecoder | Arquitecto DDD |
|---|---|---|
| Locking | Pesimista (bloquea la BD) | Optimista (detecta conflictos) |
| Conflictos visibles | No (cliente no sabe) | Sí (HTTP 409) |
| Modelo de dominio | Anémico (setters externos) | Rico (aggregate protege invariante) |
| Oferta como concepto | `BigDecimal + String` | Value Object `Oferta` |
| Tests | 0 | 5 (dominio + concurrencia) |

El locking pesimista **funciona**. Pero serializa todas las escrituras en la base de datos, escala mal bajo alta carga, y esconde los conflictos como si no existieran. El cliente no puede distinguir "bid no válido" de "hubo contención".

---

## Cómo ejecutarlo

```bash
docker compose up -d --build

# Vibecoder — puerto 8094
docker run --rm --network host \
  -v $(pwd)/load-tests:/tests grafana/k6 run \
  -e BASE_URL=http://localhost:8094 /tests/stress-test-vibecoder.js

# Arquitecto — puerto 8095
docker run --rm --network host \
  -v $(pwd)/load-tests:/tests grafana/k6 run \
  -e BASE_URL=http://localhost:8095 /tests/stress-test-architect.js
```

Ver [`load-tests/report-results.md`](load-tests/report-results.md) para resultados reales.

---

## Nota de honestidad

Ambas implementaciones fueron generadas por IA (GLM 5.2 vía opencode) en **sesiones separadas, con skills habilitadas en ambas**.

- La sesión **vibecoder**: solo el requerimiento de negocio. La IA usó skills de JPA y llegó al locking pesimista — su mejor esfuerzo sin guía de dominio.
- La sesión **arquitecto**: mismo requerimiento más diseño DDD explícito. La IA ejecutó el modelo que el autor supo pedirle.

Este es el primer proyecto de la serie con metodología limpia: mismas condiciones para ambas sesiones. La diferencia está en el **conocimiento del dominio que el autor aporta al prompt** — no en la IA.

El autor es un vibecoder con conocimiento teórico de DDD. La dirección fue humana; el código fue de la máquina.

---

## Stack

- Java 21 + Spring Boot 3.x (Web + Data JPA)
- PostgreSQL 16
- k6 (Grafana) para pruebas de carga
- Docker + Docker Compose
