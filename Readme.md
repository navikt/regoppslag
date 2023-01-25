# Regoppslag
Regoppslag er en applikasjon som gjør oppslag mot ulike tjenester hos registerne.

Appen tilbyr én Rest-tjeneste og to Soap-tjenester med [dokumentasjon i Swagger](https://regoppslag.dev.intern.nav.no/swagger-ui/index.html):
- [treg001 ValiderOgKompletterBrevdata](https://confluence.adeo.no/pages/viewpage.action?pageId=261918448) - en del av verdikjeden for dokumentproduksjon. Den tar XML som input og beriker elementene i XML'en med data fra registere ved å benytte Berikerplugins.
- [treg002 HentMottakerOgAdresse](https://confluence.adeo.no/pages/viewpage.action?pageId=257685047) - en del av verdikjeden for dokumentproduksjon. Brukes for å utlede navn og adresse gitt en ID.
- [rreg003 Postadresse](https://confluence.adeo.no/pages/viewpage.action?pageId=366967381) - Rest-tjeneste som tar en organisasjons eller persons ID som input, og returnerer navn og adresse til konsument.

Mer informasjon om hvordan appen fungerer finner du på [Confluence-siden for regoppslag](https://confluence.adeo.no/display/BOA/Registeroppslag).

## Distribusjon av tjenesten (deployment)
Distribusjon av tjenesten er gjort av Jenkins:
[regoppslag CI / CD](https://dok-jenkins.adeo.no/job/regoppslag/job/master/)
Push/merge til masterbranch vil teste, bygge og deploye til produksjonsmiljø og testmiljø.

## Kjøre prosjektet lokalt
For å kjøre opp applikasjonen lokal, bruk profile `nais` og systemvariabler hentet fra vault: [System variabler](https://vault.adeo.no/ui/vault/secrets/secret/list/dokument/regoppslag/) 

## Cache
Denne applikasjonen bruker Redis cache som er avhengig av en ekstern cache server som den kan koble seg til. 
Når applikasjonen kjøres lokalt vil det istedenfor settes opp cache som kjører lokalt på applikasjonen. Konfigurasjon av denne cachen ligger i `LokalCacheConfig` klassen og vil bare kjøres når Activeprofiles settes `local`.

### Henvendelser
Spørsmål om koden eller prosjektet kan rettes til [Slack-kanalen for \#Team Dokumentløsninger](https://nav-it.slack.com/archives/C6W9E5GPJ).