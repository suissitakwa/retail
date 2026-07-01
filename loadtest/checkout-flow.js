import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend, Rate } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

const productsTrend = new Trend('products_duration');
const productByIdTrend = new Trend('product_by_id_duration');
const cartTrend = new Trend('cart_duration');
const errorRate = new Rate('errors');

// Log in once, before any VUs start, and share the token across the whole run.
// /auth/login is intentionally rate-limited (5 req/min/IP) — hammering it with
// concurrent VUs tests the rate limiter, not the authenticated business logic.
export function setup() {
  const loginRes = http.post(
    `${BASE_URL}/auth/login`,
    JSON.stringify({ email: 'admin@retail.com', password: 'Admin123!' }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  if (loginRes.status !== 200) {
    throw new Error(`Setup login failed: ${loginRes.status} ${loginRes.body}`);
  }
  return { token: loginRes.json('token') };
}

export const options = {
  scenarios: {
    browse_public: {
      executor: 'ramping-vus',
      exec: 'browsePublicCatalog',
      startVUs: 0,
      stages: [
        { duration: '20s', target: 50 },
        { duration: '40s', target: 50 },
        { duration: '10s', target: 0 },
      ],
    },
    authenticated_flow: {
      executor: 'ramping-vus',
      exec: 'authenticatedFlow',
      startVUs: 0,
      stages: [
        { duration: '20s', target: 20 },
        { duration: '40s', target: 20 },
        { duration: '10s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<500', 'p(99)<1000'],
    errors: ['rate<0.01'],
  },
};

// Public product browsing — exercises Redis-cached read path.
export function browsePublicCatalog() {
  group('browse products', () => {
    const listRes = http.get(`${BASE_URL}/api/v1/products`);
    productsTrend.add(listRes.timings.duration);
    check(listRes, { 'products list 200': (r) => r.status === 200 }) || errorRate.add(1);

    const catRes = http.get(`${BASE_URL}/api/v1/categories`);
    check(catRes, { 'categories 200': (r) => r.status === 200 }) || errorRate.add(1);

    const byIdRes = http.get(`${BASE_URL}/api/v1/products/1`);
    productByIdTrend.add(byIdRes.timings.duration);
    check(byIdRes, { 'product by id 200 or 404': (r) => r.status === 200 || r.status === 404 }) || errorRate.add(1);
  });
  sleep(1);
}

// Authenticated flow — NOTE: all VUs intentionally share one JWT/customer here.
// This means concurrent VUs contend for the exact same cart row, which is a
// worst-case, not a representative, access pattern (real traffic spreads across
// distinct customer rows). See README for what this does and doesn't tell you.
export function authenticatedFlow(data) {
  group('cart (authenticated)', () => {
    const authHeaders = { headers: { Authorization: `Bearer ${data.token}` } };
    const cartRes = http.get(`${BASE_URL}/api/v1/cart`, authHeaders);
    cartTrend.add(cartRes.timings.duration);
    check(cartRes, { 'cart 200': (r) => r.status === 200 }) || errorRate.add(1);
  });
  sleep(1);
}
