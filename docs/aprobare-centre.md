# Aprobarea conturilor de centru

Configurează aceste variabile de mediu în backend (în IntelliJ, la configurația `ProiectIspglApplication`).

| Variabilă | Valoare |
| --- | --- |
| `FUREVERHOME_APPROVAL_RECIPIENT` | Adresa echipei care verifică și aprobă centrele. |
| `FUREVERHOME_APPROVAL_FROM` | Adresa autorizată de furnizorul SMTP. Implicit se folosește utilizatorul configurat prin `spring.mail.username`. |
| `FUREVERHOME_APPROVAL_BASE_URL` | URL-ul backendului accesibil echipei. Folosește HTTPS pentru serverul public sau `http://localhost:8080` pentru verificarea locală. |
| `FUREVERHOME_APPROVAL_SIGNING_KEY` | Cheie stabilă folosită doar pentru semnarea linkurilor. Generează 32 de octeți aleatori și configurează-i în Base64 prin mecanismul de secrete. Folosește aceeași cheie pe toate instanțele. |

Pentru SMTP se folosesc proprietățile Spring Boot `spring.mail.*`. De exemplu,
`SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`, `SPRING_MAIL_USERNAME` și
`SPRING_MAIL_PASSWORD` permit suprascrierea configurației prin mediu. Verifică
adresa echipei, expeditorul și setările de autentificare și TLS ale furnizorului
înaintea unui test de livrare.

După ce serverul SMTP acceptă notificarea, înregistrarea finalizează salvarea
contului cu statusul `IN_ASTEPTARE` și răspunde cu `201`. Dacă notificarea
eșuează, înregistrarea răspunde cu `503`, iar utilizatorul poate retrimite
formularul cu aceleași date.

Linkul din mesaj deschide pagina de confirmare. Butonul „Aprobă centrul”
actualizează statusul contului la `ACTIV`. Un link deja folosit este respins.
