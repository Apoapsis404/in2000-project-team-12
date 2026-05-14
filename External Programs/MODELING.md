## Arkitekturskisse

```mermaid
graph TD
subgraph Data Layer
main_activity[MainActivity]
fAPI[(Frost API)] --> climate_ds[ClimateDataSource]
fAPI --> os_ds[ObsStationDataSource]
climate_ds-->climate_r[ClimateDataRepository]
mapAPI[(GoogleMapsSDK)]
plants[(Plant_data.json)] --> plants_r[PlantsRepository]
nve[(NVE API)] --> nve_ds[NVEDataSource]
nve_ds --> nve_r[NVERepository]
kommuner[(municipalities.csv)] --> nve_ds
os_ds --> os_r[ObsStationRepository]
os_r --> climate_r
room[(AppDatabase)]
room --> settings_dao[SettingsDao]
room --> favorites_dao[FavoriteDao]
room --> notes_dao[NotesDao]
wind_d[(high_wind_speed_stations.json)]
rain_d[(torrential_rain_stations.json)]
end
subgraph Ui Layer
mapAPI --> home_s
home_vm[HomeScreenViewModel] --> home_s[HomeScreen]
%%os_ds --> home_vm
settings_vm[SettingsViewModel] --> settings_s[SettingsScreen]
climate_r --> graph_vm[GraphViewModel]
graph_vm --> graph_s[GraphScreen]
fav_vm[FavouritesViewModel] --> fav_s[FavouritesScreen]
notes_vm[NotesScreenViewModel]-->notes_s[NotesScreen]
home_vm --> LoadingScreen
home_vm --> fav_s
plants_r --> graph_vm
nve_r --> home_vm
settings_dao --> settings_vm
favorites_dao --> fav_vm
notes_dao --> notes_vm
wind_d --> home_vm
rain_d --> home_vm
end
```
Forklaring for Aktivitetsdiagram


## Sekvensdiagram
### Bruker ønsker å vise graf for soltimer på valgt område
```mermaid
sequenceDiagram
    actor Bruker
    Bruker->>+UI: Trykker på graf-ikon
    UI->>Navigator: NavHostController(route = 'graph')
    Navigator ->>ViewModel: setMarkerPositionAndFetch()
    ViewModel ->> ViewModel: fetchAllClimateData()
    par Ui displays loading
        UI->>ViewModel: uiState.collectAsState()
        ViewModel-->>UI: uiState: isLoading = true
        UI-->>Bruker: Viser grafskjerm med laste-sirkel
    and backend fetches data

        ViewModel->>Data: GetClimateData()
        loop Repeat til station is found OR 15 times
            Data ->> Frost: GET httpResponse
            Frost -->> Data: Json response
            Data->>Data: Parse
        end
        Data-->>ViewModel: List<ClimateObs>
    end
    ViewModel-->>UI: uiState: isLoading = false
    UI-->>Bruker: Viser temperaturgraf og gjennomsnittlig data
    Bruker ->>UI: Trykker på soltimer-boks
    UI ->> ViewModel: SetClimateType(DURATION_OF_SUN)
    ViewModel ->> ViewModel: _uiState.update(ClimateType = DURATION_OF_SUN)
    ViewModel-->>UI: uiState.ClimateType er oppdatert
    UI -->> Bruker: Tegner graf for soltimer
```
Forklaring for sekvensdiagram

## Aktivitetsdiagram
### Bruker ønsker å legge til en posisjon som favoritt
```mermaid
flowchart TD
	Start@{ shape: circle, label: "start" } -->
	    A{"Velg posisjon"}
	    A --> |Søkefelt|a[Bruker skriver inn addresse] 
	    -->b[Bruker trykker søk]
	    -->c[Posisjon lagres i ViewModel]
	    -->d[Trykker på hjerte-ikon]
	    -->e[Bekreftelses-popup]
	    -->B{"Ønsker du å legge til favoritt?"}
	    -->|Bekreft|f[Navigerer til favoritt-skjerm]
	    -->g[Navn-endring-popup]
	    -->C{"Endre navn?"}
	    -->h[Bruker skriver inn nytt navn]
	    -->D{" "}
	    -->|Lagre|j[Favoritt får ønsket navn]
	    -->Slutt
	    A-->|Trykke på kart|c
	    B-->|Avbryt|Slutt
	    C-->i[Bruker skriver ikke nytt navn]
	    i-->D
	    D-->|Avbryt|k[Favoritt får et standard-navn]
	    k-->
	Slutt@{ shape: dbl-circ, label: "Stop" }
```
Forklaring for aktivitetsdiagram