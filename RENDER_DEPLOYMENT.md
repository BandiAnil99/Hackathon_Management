# Render deployment

1. Push this repository to GitHub and create a **Web Service** in Render with runtime **Docker**.
2. Create a Render PostgreSQL database in the same region and use its internal connection details for `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
3. Add a persistent disk mounted at `/app/storage` if resumes and photos must survive redeploys. Set `APP_UPLOAD_DIR=/app/storage/uploads`.
4. Add the variables from `.env.example` in the Render service's **Environment** page. Keep database, JWT, Gemini, SMTP, and admin-password values marked as secrets.
5. Set the health-check path to `/api/system/keep-alive`.

## Required variables

| Variable | Render value |
| --- | --- |
| `DB_URL` | JDBC PostgreSQL URL for the Render database |
| `DB_USERNAME` | Render database user |
| `DB_PASSWORD` | Render database password |
| `JWT_SECRET` | A newly generated long random secret |
| `APP_BASE_URL` | `https://<backend-service>.onrender.com` |
| `APP_FRONTEND_URL` | Public frontend URL |
| `APP_QR_URL_BASE` | Backend public URL, because QR check-in links target `/api/...` |
| `APP_CORS_ALLOWED_ORIGINS` | Frontend URL; separate multiple origins with commas |
| `APP_UPLOAD_DIR` | `/app/storage/uploads` when using a persistent disk |

Render supplies `PORT`; the application now binds to it automatically. Do not set `PORT` unless you have a specific reason to override it.

## Optional services

- Set `GEMINI_API_KEY` to enable AI resume analysis. Without it, only AI analysis requests fail; core event and participant APIs still run.
- Set the `SMTP_*` values to enable registration emails and optional panelist invite emails.
- Set `ADMIN_SEED_ENABLED=true`, `ADMIN_EMAIL`, and `ADMIN_PASSWORD` only for the first deploy if an initial admin must be created. Disable the flag after that account exists.

## Panelist invites

`POST /api/panelists/invite` still returns a copyable one-time registration link. To also send it by email, call:

```text
POST /api/panelists/invite?email=panelist@example.com
```

The link in the response remains the fallback if mail delivery is delayed or blocked.
