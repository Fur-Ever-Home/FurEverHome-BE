# Adopție la distanță — backend

## API

- `GET /api/animals?adoptionType=la_distanta` (sau `distanta`, `la distanta`): numai animale cu `tip_adoptie = distanta`, fără potriviri parțiale. Compararea ignoră majusculele, spațiile marginale și diacriticele.
- `GET /api/animals/{id}`: detaliile unui animal; 404 dacă nu există, 400 pentru ID nenumeric.
- Lista și detaliile folosesc același DTO. Câmpurile vechi sunt păstrate.

Detaliile comune ambelor tipuri de adopție: `description`, `descriptionEn`, `locationType` (cazarea actuală), `apartmentFriendly`, `houseFriendly`, `images` (principala prima), `imageUrl`, `vaccinated`, `dewormed`, `sterilized`, `microchipped`, `allergiesAndConditions`, `healthStatus`, `treatments`.

Fiecare tratament conține `id`, `name`, `category`, `description`, `recordedAt` (YYYY-MM-DD), din tabelele `fisa_medicala`, `inregistrari_medicale` și `tratamente`. Istoricul este ordonat descrescător după dată.

`isDistanceAdoption` este disponibil și pentru frontendul care verifică acest indicator.

## Susținere lunară

Înainte de pornire, execută o singură dată `src/main/resources/db/manual/V001__distance_adoption.sql` în PostgreSQL. Scriptul adaugă coloane nullable și un index; nu atribuie și nu confirmă donații existente.

- `animale.necesar_donatii_lunar`: necesarul real lunar al animalului, strict pozitiv, în aceeași monedă/unitate ca `donatii_bani.suma`. Trebuie stabilit de adăpost; backendul nu inventează o sumă.
- `donatii_bani.id_animal`: animalul susținut. Donațiile generale ale adăpostului, fără animal, sunt excluse.
- `donatii_bani.status_plata`: doar valoarea exactă `confirmata` este inclusă în calcul. Se setează de fluxul de plată de încredere după confirmare. Valorile NULL, `in_asteptare`, `esuata` și `rambursata` sunt excluse. Nu se deduce succesul doar din existența unui ID Stripe.
- `data_donatie`: data plății, interpretată în fusul configurat; coloana existentă este timestamp fără fus orar.

`fundingPercent` reproduce valoarea din `animale.procent_sustinere_lunara`, pentru fiecare animal, la fiecare citire. Nu este înlocuit cu o valoare demonstrativă sau cu procentul calculat din donații. O modificare în baza de date devine vizibilă la următoarea încărcare a datelor în site.

Pentru adopția la distanță, API-ul expune suplimentar `monthlyDonationTarget`, `fundedAmount`, `remainingAmount`, `calculatedFundingPercent`, `fundingMonth` (YYYY-MM), `fundingTargetConfigured`.

Se însumează sumele pozitive confirmate pentru animal, între începutul lunii inclusiv și începutul lunii următoare exclusiv. Fus implicit `Europe/Bucharest`, configurabil prin `app.funding.time-zone`.

`calculatedFundingPercent = min(100, floor(100 × fundedAmount / monthlyDonationTarget))`.

Suma rămasă nu coboară sub zero; totalul încasat rămâne vizibil integral. Fără necesar configurat, `fundingTargetConfigured=false`, `remainingAmount=null`, `calculatedFundingPercent=null`. Calculul este refăcut la fiecare citire, fără actualizări periodice în DB. Nu modifică procentul afișat din coloana existentă.

Această modificare citește donațiile; nu implementează checkout Stripe sau webhook de plată. Fluxul de plată trebuie să înregistreze animalul, data și starea de plată și să păstreze protecția existentă contra înregistrării duble a aceleiași tranzacții.

## Integrarea frontendului

Frontendul primește datele prin lista de animale. Pop-up-ul actual nu randează secțiunile noi de tratament/necesar/sumă strânsă. Pentru afișarea lor trebuie conectate ulterior câmpurile din acest contract.

Cardul folosește acum `fundingPercent ?? 0`; păstrează procentul real de 0 și nu introduce valoarea 45. Actualizarea datelor se vede după reîncărcarea paginii; nu există un flux live de notificări din DB.

## Verificare

Java 21 sau mai nou: `./mvnw verify` (Windows: `mvnw.cmd verify`). Testele folosesc mockuri și H2 izolat, fără scrieri în baza partajată.
