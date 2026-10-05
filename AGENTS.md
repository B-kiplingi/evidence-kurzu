# AGENTS.md – Pravidla a kontext projektu pro asistenta

## Kontext projektu
* **Předmět:** Pokročilé programování (PPRO) – FIM UHK, zimní semestr 2026/2027.
* **Zadání:** Varianta C – *Kurzy vzdělávacího centra (Akademie Trutnov)*.
* **Cíl:** Vývoj uceleného informačního systému pro správu kurzů, termínů, lektorů, přihlášek s frontou náhradníků (pořadník), docházky, generování unikátních osvědčení a přehledových statistik.

## Klíčová pravidla a omezení
1. **Architektura:** Striktní třívrstvá architektura se závislostmi jedním směrem (Prezentace → Aplikační/Doménová logika → Datová/Infrastrukturní vrstva).
2. **Klíčové uzly návrhu:** Správná realizace a integrita **pořadníku náhradníků** a **pravidla pro úspěšné dokončení kurzu**.
3. **Infrastruktura:** Relační databáze s verzovanými migracemi v Dockeru, kompletní běh ověřitelný přes `docker compose up`.
4. **Data:** Výhradně syntetická testovací data – žádná reálná osobní data ani tajné přihlašovací údaje v repozitáři.
5. **Kvalita:** Automatizované testy (unit i integrační), čistý a čitelný kód s ošetřením chyb.
6. **Rozhodování:** Veškerá schválená architektonická a funkční rozhodnutí (např. otevřené body klienta či technologický stack) průběžně zaznamenávat a aktualizovat v tabulce ADR v [README.md](file:///home/lansky/UHK/7.semestr/PPRO/projekt/README.md).
