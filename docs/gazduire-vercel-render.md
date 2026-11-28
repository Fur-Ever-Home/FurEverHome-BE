# Autentificarea pe Vercel și Render

Frontendul Vercel redirecționează `/api/*` către backendul Render. Domeniul public
este `https://fur-ever-home-fe.vercel.app`. Linkul de aprobare pornește cu
`/api/auth/shelter-approval` și linkul de resetare cu `/login#reset=`.

## Configurație Render

- Aplică o singură dată `src/main/resources/db/manual/V002__hosted_http_sessions.sql`
  în aceeași bază PostgreSQL folosită de aplicație. Acordă contului aplicației
  acces de citire și scriere la tabelele de sesiuni.
- Setează `SPRING_PROFILES_ACTIVE=hosted` și setează `FUREVERHOME_PUBLIC_URL`
  la originea publică HTTPS, fără un slash la final. Aceeași valoare
  configurează implicit CORS și cele două linkuri. Pentru o listă de origini
  suplimentare folosește `FUREVERHOME_ALLOWED_ORIGINS`.
- Setează `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` și
  `SPRING_DATASOURCE_PASSWORD` în mediul protejat al serviciului.
- Setează `FUREVERHOME_APPROVAL_RECIPIENT` la adresa echipei. Generează două
  chei aleatoare distincte, de cel puțin 32 de octeți, pentru
  `FUREVERHOME_APPROVAL_SIGNING_KEY` și `FUREVERHOME_PASSWORD_RESET_SIGNING_KEY`.
  Păstrează aceleași chei între instanțe și deploy-uri.
- Pentru SMTP, setează un cont SMTP funcțional prin `SPRING_MAIL_*`.
  Gmail folosește portul 587, disponibil pe planurile Render plătite.
- Pentru e-mail prin HTTPS, setează `FUREVERHOME_ACCOUNT_MAIL_TRANSPORT=brevo`,
  `FUREVERHOME_BREVO_API_KEY` și `FUREVERHOME_ACCOUNT_MAIL_FROM` cu un expeditor
  verificat în Brevo. Această opțiune acoperă aprobarea și resetarea parolei.

După publicarea celor două proiecte, verifică ruta publică `/login`,
`/api/auth/session` cu și fără sesiune și un e-mail controlat pentru fiecare
flux. Folosește un cont și un destinatar de test pentru verificarea livrării.
