# Vibecoder + Skills vs Arquitecto — Subastas (Ofertas)

Cuarto proyecto de la serie `ai-vs-architect`, **rehecho con la metodología v2**.

> **Metodología v2 (qué cambió respecto de la versión original)**
> 1. **Generación en directorios neutros** (`auction-api`, `auction-backend`), porque el
>    modelo lee el nombre de la carpeta y, si dice "vibecoder", se auto-sabotea.
> 2. **Ambas sesiones con las mismas skills `java-*`.** Paridad real.
> 3. Única diferencia entre prompts: el arquitecto recibe además los **conceptos** nombrados.

---

## El experimento

Mismo modelo (GLM 5.2), mismo dominio, dos sesiones independientes.

- **Vibecoder**: "una subasta con ofertas; una oferta debe superar a la actual".
- **Arquitecto**: lo mismo + "aplicá Aggregate Root, Value Objects, manejá la concurrencia
  sin pérdida de datos, conflictos visibles".

**Invariante**: bajo 1000 ofertas concurrentes de monto 1 sobre una subasta en 0, gana
exactamente una; la oferta más alta solo aumenta.

---

## Resultado: convergencia (con más profundidad del arquitecto)

| Dimensión | Vibecoder + skills | Arquitecto |
|---|---|---|
| Layering `domain/application/infrastructure` | ✅ | ✅ |
| Aggregate Root (`Subasta` / `Auction`) | ✅ | ✅ |
| Value Object (`Oferta` / `Bid`) | ✅ | ✅ |
| Puerto + adaptador (hexagonal) | ✅ | ✅ |
| `@Version` (optimistic locking) | ✅ | ✅ |
| HTTP 409 en conflicto | ✅ | ✅ |
| **Una sola oferta ganadora bajo carga** | ✅ | ✅ |
| Tests | 19 ✅ | 27 ✅ |

Ver [`load-tests/report-results.md`](load-tests/report-results.md) para los números reales.

**La lección**: igual que wallet e inventory, la concurrencia tiene solución única y las
skills la transfieren. El vibecoder llegó a un modelo rico (aggregate, value object,
puerto/adaptador, use cases). El arquitecto sumó algo más de cobertura de tests (27 vs 19,
con un test dedicado al Value Object y al caso de uso de listado), pero ambos protegen el
invariante por igual.

---

## Qué prueban los tests (y qué previenen)

| Test | Qué previene |
|---|---|
| `SubastaTest` / `AuctionTest` | que el aggregate acepte una oferta no mayor — rompe "la oferta solo aumenta" |
| `BidTest` (arch) | que se construya una oferta inválida (Value Object) |
| `HacerOfertaUseCaseTest` / `PlaceBidUseCaseTest` | que la orquestación acepte una oferta insuficiente |
| `SubastaControllerTest` / `AuctionControllerTest` | que la API devuelva el status equivocado (200/400/409) |
| `SubastaConcurrencyTest` / `AuctionConcurrencyTest` | **lost update** — dos ofertas del mismo monto aceptadas bajo concurrencia |

El test de concurrencia es el corazón: varios hilos ofertan el mismo monto a la vez y se
verifica que solo uno gane. Es la versión unitaria del stress test de k6.

---

## Cómo ejecutarlo

```bash
docker compose up -d --build          # vibecoder :8094, architect :8095

cd vibecoder-approach && mvn test     # 19
cd ../architect-approach && mvn test  # 27
```

---

## Nota de honestidad

Ambas implementaciones fueron generadas por IA (GLM 5.2) en sesiones separadas, con las
mismas skills disponibles, en directorios de nombre neutro. El autor es un vibecoder con
conocimiento teórico de DDD: no escribió el código Java.

## Stack

Java 21 · Spring Boot 3.x (Web + Data JPA) · PostgreSQL 16 · Docker Compose · k6 · JUnit 5 + Mockito + H2
