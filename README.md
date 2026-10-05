# Akademie Trutnov – Informační systém pro kurzy vzdělávacího centra

> **Předmět:** Pokročilé programování (PPRO) – ZS 2026/2027, FIM UHK  
> **Zadání:** Varianta C – Kurzy vzdělávacího centra  
> **Klient:** Ing. Lucie Bartošová, Akademie Trutnov  
> **Konzultant předmětu:** dominik.palla@uhk.cz  

---

## 1. Kontext a současný stav (As-Is)

Akademie Trutnov nabízí rekvalifikační a firemní kurzy (přibližně 40 běhů ročně). Vzdělávání zajišťují externí lektoři.

### Současné fungování:
* **Přihlášky:** Přicházejí z webového formuláře do e-mailu a ručně se přepisují do tabulky v Excelu (jedna záložka pro každý kurz).
* **Kapacita a pořadník:** Kapacity kurzů se hlídají manuálně („od oka“). Náhradníci (pořadník) jsou řešeni formou tužkových poznámek pod seznamem.
* **Osvědčení:** Vypisují se ručně v MS Wordu. Čísla osvědčení se přidělují odhadem/pamětí podle počtu vydaných v daném roce, což vedlo k duplicitnímu přidělení stejného čísla.
* **Odhlášení na poslední chvíli:** Největší bolestí klienta je situace, kdy účastník na poslední chvíli účast zruší a organizátor nemá přehled, koho z náhradníků kontaktovat.

---

## 2. Požadavky klienta

### A. Co klient požaduje (Funkční požadavky)
1. **Evidence kurzů:**
   * Název kurzu, anotace, rozsah v hodinách, cena, příznak akreditace (akreditovaný / neakreditovaný).
2. **Termíny kurzů:**
   * Každý kurz se opakuje několikrát do roka.
   * Termín má datum a čas od–do, místo konání (učebnu) a stanovenou kapacitu (kapacita se liší podle kapacity konkrétní učebny).
3. **Lektoři a jejich přiřazení k termínům:**
   * Evidence externích lektorů.
   * Jeden lektor může vést více termínů; u delších kurzů se na jednom termínu střídají dva lektoři.
4. **Přihlášky studentů na termíny:**
   * Evidence studentů/účastníků.
   * Jeden student může navštěvovat více kurzů; na jednom termínu je více studentů.
5. **Kapacita a pořadník (Fronta náhradníků):**
   * Pokud je kapacita termínu naplněna, další zájemce je zařazen do pořadníku s přesným zachováním pořadí registrace.
   * Při uvolnění místa systém identifikuje, kdo je na řadě.
6. **Evidence docházky:**
   * U vícedenních termínů detailní evidence přítomnosti studentů v jednotlivých dnech.
7. **Dokončení a osvědčení o absolvování:**
   * Po skončení termínu generování a evidence seznamu úspěšných absolventů.
   * Vydání osvědčení s unikátním evidenčním číslem a datem vydání (číslo se nesmí opakovat ani měnit).
8. **Statistiky a přehledy:**
   * Přehled naplněnosti jednotlivých termínů.
   * Počet odučených hodin podle jednotlivých lektorů.
   * Seznam studentů, kteří se v daném roce přihlásili na více než jeden kurz.

---

### B. Obchodní pravidla (Na čem klient trvá)
* **Nepřekročitelnost kapacity:** Kapacitu termínu nelze za žádných okolností překročit.
* **Přísné pořadí v pořadníku:** Pořadí náhradníků se nesmí přeskakovat.
* **Integrita a neměnnost osvědčení:** Číslo osvědčení je striktně jedinečné napříč celým systémem. Již vydané osvědčení je neměnné (auditovatelný záznam).
* **Historie a dohledatelnost zrušených přihlášek:** Zrušená přihláška se fyzicky nemaže z databáze (soft-delete/historizace stavů), aby bylo možné zpětně doložit průběh přihlášení a reklamací.

---

### C. Doplňková přání a výhled (Nekritické)
* **Budoucí fakturace:** Datový model a architektura by měly být připraveny na případné budoucí napojení fakturace.
* **Ergonomie a jednoduchost:** Přehledné a intuitivní rozhraní i pro méně technicky zdatné uživatele (starší externí lektoři).
* **Notifikace:** Příprava na e-mailové notifikace (potvrzení přihlášky, výzva pro náhradníka apod.).
* **Výkazy pro MŠMT/ministerstvo:** Možnost exportu podkladů pro oficiální výkaznictví.

---

### D. Otevřené body (Klíčové k rozhodnutí)
1. **Pravidlo pro úspěšné dokončení kurzu:** Klient zmiňuje 70% docházku vs. subjektivní posouzení lektorem.
2. **Mechanismus posunu z pořadníku:** Automatické zařazení do kurzu vs. notifikace náhradníka s časovým limitem na potvrzení (pro případ, že náhradník již ztratil zájem).
3. **Souběžné přihlášky na více termínů:** Zda se student smí současně přihlásit na více termínů téhož kurzu.
4. **Přihlášení po zahájení kurzu:** Za jakých specifických podmínek a s jakým schválením lze studenta dodatečně zapsat po startu výuky.
5. **Dělení hodin mezi lektory:** Jak evidovat a vykazovat hodiny u termínů, kde se střídají dva lektoři.

---

## 3. Povinné technické minimum (Zadání PPRO)

* **Architektura:** Třívrstvá architektura se striktními jednosměrnými závislostmi:
  * Prezentační vrstva (UI / REST API)
  * Aplikační / Doménová vrstva (obchodní logika, pravidla pořadníku, validace)
  * Datová / Infrastrukturní vrstva (persistence, repozitáře)
* **Databáze:** Relační databáze provozovaná v Docker kontejneru včetně verzovaných migrací.
* **Datový model:** Minimálně 5 doménových entit a alespoň jedna vazba M:N.
* **Testování:** Sada automatizovaných testů ověřující doménová pravidla a klíčové scénáře.
* **Orchestrace:** Kompletní spuštění aplikace a závislostí jediným příkazem:
  ```bash
  docker compose up
  ```
* **Bezpečnost a data:** Výhradně syntetická testovací data (žádná reálná osobní data, žádné tajné přístupové údaje v gitu).
* **Klíčový zlom návrhu:** Návrh pořadníku (fronty náhradníků) a doménové pravidlo pro vyhodnocení dokončení kurzu.

---

## 4. Deník architektonických rozhodnutí (ADR / Rozhodnutí)

Tato sekce průběžně dokumentuje klíčová rozhodnutí učiněná během návrhu a vývoje aplikace.

| ID | Oblast / Téma | Rozhodnutí a zdůvodnění | Stav |
|---|---|---|---|
| **ADR-01** | **Pravidlo dokončení kurzu** | Bude vyžadována minimální docházka 70 % jako systémové prerekvizitum + finální formální potvrzení/posouzení lektorem v systému. Tím jsou splněny obě podmínky. | *Návrh k potvrzení* |
| **ADR-02** | **Posun z pořadníku** | Při uvolnění místa systém nevloží náhradníka „napevno“, ale nabídne mu rezervaci se lhůtou (např. 24–48 hod.) k potvrzení, případně umožní administrátorovi manuální posun po telefonické domluvě. | *Návrh k potvrzení* |
| **ADR-03** | **Souběh termínů téhož kurzu** | Student se může přihlásit pouze na jeden aktivní řádný termín daného kurzu; na další termíny může být maximálně v pozici náhradníka v pořadníku. | *Návrh k potvrzení* |
| **ADR-04** | **Dodatečný zápis po zahájení** | Běžný uživatel se po zahájení zapsat nemůže; zápis po zahájení je umožněn pouze administrátorovi/vedení s povinným vyplněním důvodu výjimky. | *Návrh k potvrzení* |
| **ADR-05** | **Rozdělení hodin lektorů** | U termínů s více lektory se lektoři přiřazují k jednotlivým dnům/blokům docházky. Vykazované hodiny lektora se pak počítají ze skutečně odučených bloků. | *Návrh k potvrzení* |
| **ADR-06** | **Technologický stack** | *Bude doplněno po dohodě s uživatelem (např. C#/.NET, Java/Spring, Node/NestJS/TypeScript, Python/FastAPI apod.).* | *V řešení* |
