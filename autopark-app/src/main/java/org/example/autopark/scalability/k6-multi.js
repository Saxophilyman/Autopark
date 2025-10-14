// k6-multi.js
import http from 'k6/http';

const BASE = __ENV.BASE || 'http://host.docker.internal:8080';
const VEH_ID = __ENV.VEH_ID || '1';
const params = __ENV.JWT ? { headers: { Authorization: `Bearer ${__ENV.JWT}` } } : undefined;

export const options = {
    discardResponseBodies: true,
    scenarios: {
        // 1) /rps/pure: 0-3 мин
        pure: {
            executor: 'ramping-arrival-rate',
            exec: 'hitPure',
            startTime: '0s',                // старт сразу
            timeUnit: '1s',
            startRate: 200,
            preAllocatedVUs: 200, maxVUs: 2000,
            stages: [
                { target: 1000, duration: '1m' },
                { target: 3000, duration: '1m' },
                { target: 5000, duration: '1m' }
            ],
            tags: { endpoint: 'pure' }
        },

        // 2) /rps/json: 3-6 мин
        json: {
            executor: 'ramping-arrival-rate',
            exec: 'hitJson',
            startTime: '3m',                // запускаем после pure
            timeUnit: '1s',
            startRate: 200,
            preAllocatedVUs: 200, maxVUs: 2000,
            stages: [
                { target: 1000, duration: '1m' },
                { target: 3000, duration: '1m' },
                { target: 5000, duration: '1m' }
            ],
            tags: { endpoint: 'json' }
        },

        // 3) /rps/api/vehicles/{id}: 6-9 мин
        vehicle: {
            executor: 'ramping-arrival-rate',
            exec: 'hitVehicle',
            startTime: '6m',                // запускаем после json
            timeUnit: '1s',
            startRate: 100,
            preAllocatedVUs: 200, maxVUs: 2000,
            stages: [
                { target: 500,  duration: '1m' },
                { target: 1500, duration: '1m' },
                { target: 3000, duration: '1m' }
            ],
            tags: { endpoint: 'vehicle' }
        }
    },

    // Пороговые условия отдельно по каждому сценарию (через теги)
    thresholds: {
        'http_req_failed{endpoint:pure}':   ['rate<0.01'],
        'http_req_duration{endpoint:pure}': ['p(95)<200'],

        'http_req_failed{endpoint:json}':   ['rate<0.01'],
        'http_req_duration{endpoint:json}': ['p(95)<200'],

        'http_req_failed{endpoint:vehicle}':   ['rate<0.01'],
        'http_req_duration{endpoint:vehicle}': ['p(95)<300']
    }
};

export function hitPure()    { http.get(`${BASE}/rps/pure`, params); }
export function hitJson()    { http.get(`${BASE}/rps/json`, params); }
export function hitVehicle() { http.get(`${BASE}/rps/api/vehicles/${VEH_ID}`, params); }

// (Опционально) общий JSON-итог прогона, если смонтируешь /out
export function handleSummary(data) {
    return { '/out/summary.json': JSON.stringify(data, null, 2) };
}
