import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8094';

export const options = {
  vus: 1000,
  iterations: 1000,
};

// Todos los VUs intentan pujar 1 en una subasta que arranca en 0
// Solo 1 puede ganar: la puja debe ser MAYOR a la actual
export default function () {
  const res = http.post(`${BASE_URL}/auctions/1/bid?amount=1&userId=user-${__VU}`);
  check(res, { 'status 200 o 400': (r) => r.status === 200 || r.status === 400 });
}

export function handleSummary(data) {
  console.log('\n=== RESULTADO VIBECODER ===');
  console.log(`Total requests: ${data.metrics['http_reqs'].values.count}`);
  console.log('Si más de 1 HTTP 200: la puja se aceptó dos veces con el mismo monto.');
}
