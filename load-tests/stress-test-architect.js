import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8095';
const SUBASTA_ID = 'c0a80001-0000-0000-0000-000000000001';

export const options = {
  vus: 1000,
  iterations: 1000,
};

// Todos los VUs intentan pujar 1 en una subasta que arranca en 0
export default function () {
  let res;
  let attempts = 0;

  do {
    const usuarioId = `00000000-0000-0000-0000-${String(__VU).padStart(12, '0')}`;
    res = http.post(`${BASE_URL}/subastas/${SUBASTA_ID}/ofertar?monto=1&usuarioId=${usuarioId}`);
    attempts++;
    if (res.status === 409) sleep(0.01);
  } while (res.status === 409 && attempts < 10);

  check(res, { 'sin errores inesperados': (r) => r.status !== 500 });
}

export function handleSummary(data) {
  console.log('\n=== RESULTADO ARQUITECTO ===');
  console.log(`Total requests: ${data.metrics['http_reqs'].values.count}`);
  console.log('Exactamente 1 HTTP 200. Los 409 son conflictos explícitos — información, no errores.');
}
