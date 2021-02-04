# regoppslag

* [Funksjonelle Krav](#1-funksjonelle-krav)
* [Distribusjon av tjenesten (deployment)](#2-distribusjon-av-tjenesten-deployment)
* [Utviklingsmiljø](#3-utviklingsmilj)
* [Drift og støtte](#4-drift-og-sttte)

## Funksjonelle krav
Regoppslag er en applikasjon som gjør oppslag mot ulike tjenester hos registerne. 

For mer informasjon: [confluence](https://confluence.adeo.no/display/BOA/Registeroppslag)


## Distribusjon av tjenesten (deployment)
Distribusjon av tjenesten er gjort av Jenkins:
[regoppslag CI / CD](https://dok-jenkins.adeo.no/job/regoppslag/job/master/)
Push/merge til masterbranch vil teste, bygge og deploye til produksjonsmiljø og testmiljø.


## Utviklingsmiljø
### Forutsetninger
* Java 11
* Kubectl
* Maven

### Kjøre prosjektet lokalt
For å kjøre opp applikasjonen lokal, bruk profile `nais` og systemvariabler hentet fra vault: [System variabler](https://vault.adeo.no/ui/vault/secrets/secret/list/dokument/regoppslag/) 

### Bygge app.jar og kjøre tester
`mvn clean package`/`mvn clean install`

### Cache
Denne applikasjonen bruker Redis cache som er avhengig av en ekstern cache server som den kan koble seg til. 
Når applikasjonen kjøres lokalt vil det istedenfor settes opp cache som kjører lokalt på applikasjonen. Konfigurasjon av denne cachen ligger i `LokalCacheConfig` klassen og vil bare kjøres når Activeprofiles settes `local`.


## Drift og støtte
### Logging
Loggene til tjenesten kan leses på to måter:

### Kibana
For [dev-fss](https://logs.adeo.no/goto/73e5f3ceed036fe47affe43670459266)

For [prod-fss](https://logs.adeo.no/goto/8c0004b421ed20528f0aad8600b4f0ea)

### Kubectl
For dev-fss:
```shell script
kubectl config use-context dev-fss
kubectl get pods -n q1 -l app=regoppslag
kubectl logs -f regoppslag-<POD-ID> -n teamdokumenthandtering -c regoppslag
```

For prod-fss:
```shell script
kubectl config use-context prod-fss
kubectl get pods -l app=regoppslag
kubectl logs -f regoppslag-<POD-ID> -n teamdokumenthandtering -c regoppslag
```


### Henvendelser
Spørsmål til koden eller prosjektet kan rettes til Team Dokumentløsninger på:
* [\#Team Dokumentløsninger](https://nav-it.slack.com/client/T5LNAMWNA/C6W9E5GPJ)
