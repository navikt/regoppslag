# Regoppslag

Regoppslag er en applikasjon som gjør oppslag mot ulike tjenester hos registerne.

Appen tilbyr tre Rest-tjenester med [dokumentasjon i Swagger (Nav-internt)](https://regoppslag.intern.dev.nav.no/swagger-ui/index.html):
- [treg001 ValiderOgKompletterBrevdata (Nav-internt)](https://confluence.adeo.no/pages/viewpage.action?pageId=261918448) - en del av verdikjeden for dokumentproduksjon. Den tar XML som input og beriker elementene i XML'en med data fra registere ved å benytte Berikerplugins.
- [treg002 HentMottakerOgAdresse (Nav-internt)](https://confluence.adeo.no/pages/viewpage.action?pageId=257685047) - en del av verdikjeden for dokumentproduksjon. Brukes for å utlede navn og adresse gitt en ID.
- [rreg003 Postadresse (Nav-internt)](https://confluence.adeo.no/pages/viewpage.action?pageId=366967381) - Rest-tjeneste som tar en organisasjons eller persons ID som input, og returnerer navn og adresse til konsument.

Mer informasjon om hvordan appen fungerer finner du på [Confluence-siden for regoppslag (Nav-internt)](https://confluence.adeo.no/display/BOA/Registeroppslag).

## Komme i gang

Kjør tester og bygg appen

```
mvn clean verify
```

## Distribusjon av tjenesten (deployment)

Distribusjon av tjenesten er gjort av dok-workflows (GHA):
[Reusable dok-workflows repo](https://github.com/navikt/dok-workflows)

---

## Henvendelser

Lag en issue i repository.

### For Nav-ansatte

Spørsmål om appen kan stilles på [#team_dokumentløsninger](https://nav-it.slack.com/archives/C6W9E5GPJ)

## Lisens

[MIT](LICENSE.md)
