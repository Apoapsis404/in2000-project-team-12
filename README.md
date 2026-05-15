## Deltakere:     
>Vegard van Jole  
>Nicholas Uribarri  
>Heran Jabessa-Wodajo  
>Ada Christine Saastad   
>Marius Iversen   
>Ada Graah    

## Hvordan kjøre appen

MERK: Om kildekoden lastes ned fra devilry.ifi.uio.no, kan man se bort fra punktene om API- nøkler. 

1. **Last ned kildekoden:** Klon repoet ved hjelp av Git:
   ```bash
   git clone git@github.uio.no:IN2000-V26/team-12.git
   ```
2. **Åpne i Android Studio:** Start Android Studio og åpne mappen `team-12`.
3. **Hent Frost API nøkler:** Før du kjører applikasjonen må du legge til “FROST_CLIENT_ID” og “FROST_CLIENT_SECRET” fra Frost i local.properties, som du kan få tak i her: https://frost.met.no/auth/requestCredentials.html
4. **Google Maps API nøkkel:** Legg til “MAPS_API_KEY” i local.properties.
4. **Synkroniser Gradle:** La Android Studio laste ned alle nødvendige avhengigheter.
5. **Koble til enhet:** Koble til en fysisk Android-enhet, eller bruk en Android Emulator.
6. **Kjør appen:** Trykk på den grønne "Run"-knappen øverst i verktøylinjen.

---

## Avhengigheter og Krav

For at appen skal fungere optimalt, kreves følgende:

* **Internettilgang:** Nødvendig for å hente værdata fra eksterne API-er og laste inn kartfliser fra Google Maps.
* **Android API-nivå:** Minimum SDK er satt til 33. 

---

## Biblioteker og Teknologier

### Brukergrensesnitt (UI)
* **Jetpack Compose:** Hoved bibliotek til all UI  
* **Material 3:** Designsystemet som gir appen et moderne og konsistent utseende.
* **Vico:** Bibliotek brukt for å generere grafer for klimadata.

### Kart og Lokasjon
* **Google Maps Compose:** Lar oss integrere Google Maps direkte i Compose-grensesnittet.
* **Play Services Location:** Brukes for å hente nøyaktig geografisk posisjon fra enheten.

### Data og Nettverk
* **Ktor:** En nettverksklient som brukes til å sende asynkroniserte forespørsler til API-er (som MET og Frost) og motta data.
* **Kotlinx Serialization:** Brukes for å oversette JSON-svar fra nettet til Kotlin-objekter som appen kan bruke.
* **Room Database:** Et lag over SQLite som gjør det enkelt å lagre data lokalt på enheten for offline-bruk eller hurtiglagring.
* **Gson:** Et bibliotek for konvertering mellom Java-objekter og JSON (brukt i kombinasjon med Room).

### Analyse
* **Apache Commons Math:** Et bibliotek med avanserte matematiske og statistiske funksjoner, brukt i våre regresjonsanalyser og databeregninger.