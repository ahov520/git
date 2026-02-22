import dotenv from 'dotenv'
import express from 'express'

dotenv.config()

const app = express()
const port = Number(process.env.PORT || 8787)

const githubTokenUrl = 'https://github.com/login/oauth/access_token'

app.use(express.json({ limit: '256kb' }))
app.use((req, res, next) => {
  res.setHeader('Access-Control-Allow-Origin', '*')
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization')
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
  if (req.method === 'OPTIONS') {
    res.status(204).end()
    return
  }
  next()
})

function getEnv(name) {
  return process.env[name]?.trim() || ''
}

function getClientConfig() {
  return {
    clientId: getEnv('GH_OAUTH_CLIENT_ID'),
    clientSecret: getEnv('GH_OAUTH_CLIENT_SECRET'),
  }
}

function getAllowedRedirectUris() {
  const raw = getEnv('OAUTH_REDIRECT_URI_ALLOWLIST')
  if (!raw) {
    return ['githubmobile://auth']
  }
  return raw
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

function requireServerConfig(res) {
  const { clientId, clientSecret } = getClientConfig()
  if (!clientId || !clientSecret) {
    res.status(500).json({
      error: 'server_misconfigured',
      error_description: 'GH_OAUTH_CLIENT_ID 或 GH_OAUTH_CLIENT_SECRET 未配置',
    })
    return null
  }
  return { clientId, clientSecret }
}

async function requestGitHubToken(params) {
  const response = await fetch(githubTokenUrl, {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/x-www-form-urlencoded',
      'User-Agent': 'github-mobile-auth-proxy',
    },
    body: new URLSearchParams(params),
  })

  const payload = await response.json().catch(() => ({}))
  return { response, payload }
}

function normalizeTokenResponse(payload) {
  return {
    access_token: payload.access_token,
    token_type: payload.token_type || 'bearer',
    scope: payload.scope || '',
    refresh_token: payload.refresh_token || null,
    expires_in: payload.expires_in ?? null,
    refresh_token_expires_in: payload.refresh_token_expires_in ?? null,
  }
}

app.get('/health', (_req, res) => {
  const { clientId, clientSecret } = getClientConfig()
  res.json({
    ok: true,
    provider: 'github-oauth',
    hasClientId: Boolean(clientId),
    hasClientSecret: Boolean(clientSecret),
    allowedRedirectUris: getAllowedRedirectUris(),
    now: new Date().toISOString(),
  })
})

app.post('/oauth/github/exchange', async (req, res) => {
  const cfg = requireServerConfig(res)
  if (!cfg) return

  const { code, code_verifier: codeVerifier, redirect_uri: redirectUri } = req.body || {}

  if (!code || !codeVerifier || !redirectUri) {
    res.status(400).json({
      error: 'invalid_request',
      error_description: 'code/code_verifier/redirect_uri 均为必填',
    })
    return
  }

  if (!getAllowedRedirectUris().includes(redirectUri)) {
    res.status(400).json({
      error: 'invalid_request',
      error_description: `redirect_uri 不在允许列表: ${redirectUri}`,
    })
    return
  }

  try {
    const { payload } = await requestGitHubToken({
      client_id: cfg.clientId,
      client_secret: cfg.clientSecret,
      code,
      code_verifier: codeVerifier,
      redirect_uri: redirectUri,
    })

    if (!payload.access_token) {
      res.status(400).json({
        error: payload.error || 'exchange_failed',
        error_description: payload.error_description || 'GitHub 未返回 access_token',
      })
      return
    }

    res.status(200).json(normalizeTokenResponse(payload))
  } catch (error) {
    res.status(502).json({
      error: 'upstream_error',
      error_description: error instanceof Error ? error.message : 'Unknown upstream error',
    })
  }
})

app.post('/oauth/github/refresh', async (req, res) => {
  const cfg = requireServerConfig(res)
  if (!cfg) return

  const { refresh_token: refreshToken } = req.body || {}

  if (!refreshToken) {
    res.status(400).json({
      error: 'invalid_request',
      error_description: 'refresh_token 为必填',
    })
    return
  }

  try {
    const { payload } = await requestGitHubToken({
      client_id: cfg.clientId,
      client_secret: cfg.clientSecret,
      grant_type: 'refresh_token',
      refresh_token: refreshToken,
    })

    if (!payload.access_token) {
      res.status(400).json({
        error: payload.error || 'refresh_failed',
        error_description: payload.error_description || 'GitHub 未返回 access_token',
      })
      return
    }

    res.status(200).json(normalizeTokenResponse(payload))
  } catch (error) {
    res.status(502).json({
      error: 'upstream_error',
      error_description: error instanceof Error ? error.message : 'Unknown upstream error',
    })
  }
})

app.listen(port, () => {
  console.log(`[auth-proxy] listening on :${port}`)
})
