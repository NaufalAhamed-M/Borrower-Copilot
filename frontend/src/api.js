const API = 'http://localhost:8080/api'

async function post(path, payload) {
  const response = await fetch(`${API}${path}`, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(payload)
  })
  const data = await response.json().catch(() => ({}))
  if (!response.ok) throw new Error(data.error || 'Request failed')
  return data
}

export function assess(payload) {
  return post('/assessment', payload)
}

export function checkOffer(payload) {
  return post('/offer-check', payload)
}
