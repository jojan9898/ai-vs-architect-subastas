import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

// 1000 users bid amount=1 on an auction starting at 0. Only one bid of 1 can win;
// the rest are not strictly higher, so they get 400. The invariant: the highest bid
// only increases and exactly one bid of 1 is accepted. A lost update would let two
// bids of 1 both succeed. Vibecoder: monto/usuarioId as query params (UUID).
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8094';
const ID = 'c0a80001-0000-0000-0000-000000000001';
const won = new Counter('bids_won');

export const options = { vus: 1000, iterations: 1000 };

export default function () {
  const uid = `00000000-0000-0000-0000-${String(__VU).padStart(12, '0')}`;
  let res, attempts = 0;
  do {                                  // retry on 409 (concurrency conflict)
    res = http.post(`${BASE_URL}/subastas/${ID}/ofertar?monto=1&usuarioId=${uid}`);
    if (res.status === 409) sleep(0.01);
  } while (res.status === 409 && ++attempts < 15);
  if (res.status === 200) won.add(1);
  check(res, { 'no 500': (r) => r.status !== 500 });
}

export function handleSummary(data) {
  const w = data.metrics.bids_won ? data.metrics.bids_won.values.count : 0;
  console.log(`\nBids accepted with 200 (must be exactly 1): ${w}`);
  console.log(`Total requests (with 409 retries): ${data.metrics.http_reqs.values.count}`);
  return {};
}
