package cz.uhk.ppro.akademietrutnov.infrastructure.config;

import cz.uhk.ppro.akademietrutnov.domain.model.*;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseTermRepository courseTermRepository;
    private final TermDayRepository termDayRepository;
    private final RegistrationRepository registrationRepository;
    private final AttendanceRepository attendanceRepository;
    private final CertificateRepository certificateRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           CourseRepository courseRepository,
                           CourseTermRepository courseTermRepository,
                           TermDayRepository termDayRepository,
                           RegistrationRepository registrationRepository,
                           AttendanceRepository attendanceRepository,
                           CertificateRepository certificateRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.courseTermRepository = courseTermRepository;
        this.termDayRepository = termDayRepository;
        this.registrationRepository = registrationRepository;
        this.attendanceRepository = attendanceRepository;
        this.certificateRepository = certificateRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Inicializace výchozích dat a rolí...");

        Role roleAdmin = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));
        Role roleLecturer = roleRepository.findByName("ROLE_LECTURER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_LECTURER")));
        Role roleStudent = roleRepository.findByName("ROLE_STUDENT")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_STUDENT")));

        // 1. Uživatelé
        User admin = userRepository.findByUsername("admin").orElseGet(() -> {
            User u = new User("admin", passwordEncoder.encode("admin123"), "Správce Akademie", "admin@akademietrutnov.cz", "+420 499 111 222");
            u.addRole(roleAdmin);
            u.addRole(roleLecturer);
            return userRepository.save(u);
        });

        User lektorKarel = userRepository.findByUsername("lektor").orElseGet(() -> {
            User u = new User("lektor", passwordEncoder.encode("lektor123"), "Ing. Karel Lektor", "karel.lektor@akademietrutnov.cz", "+420 777 987 654");
            u.addRole(roleLecturer);
            return userRepository.save(u);
        });

        User lektorPetra = userRepository.findByUsername("petra").orElseGet(() -> {
            User u = new User("petra", passwordEncoder.encode("petra123"), "Mgr. Petra Svobodová", "petra.svobodova@akademietrutnov.cz", "+420 777 123 456");
            u.addRole(roleLecturer);
            return userRepository.save(u);
        });

        User studentJan = userRepository.findByUsername("student").orElseGet(() -> {
            User u = new User("student", passwordEncoder.encode("student123"), "Jan Novák", "jan.novak@example.cz", "+420 601 111 222");
            u.addRole(roleStudent);
            return userRepository.save(u);
        });

        User studentEva = userRepository.findByUsername("eva").orElseGet(() -> {
            User u = new User("eva", passwordEncoder.encode("eva123"), "Eva Černá", "eva.cerna@example.cz", "+420 602 333 444");
            u.addRole(roleStudent);
            return userRepository.save(u);
        });

        User studentTomas = userRepository.findByUsername("tomas").orElseGet(() -> {
            User u = new User("tomas", passwordEncoder.encode("tomas123"), "Tomáš Dvořák", "tomas.dvorak@example.cz", "+420 603 555 666");
            u.addRole(roleStudent);
            return userRepository.save(u);
        });

        User studentLucie = userRepository.findByUsername("lucie").orElseGet(() -> {
            User u = new User("lucie", passwordEncoder.encode("lucie123"), "Lucie Veselá", "lucie.vesela@example.cz", "+420 604 777 888");
            u.addRole(roleStudent);
            return userRepository.save(u);
        });

        User studentMartin = userRepository.findByUsername("martin").orElseGet(() -> {
            User u = new User("martin", passwordEncoder.encode("martin123"), "Martin Horák", "martin.horak@example.cz", "+420 605 999 000");
            u.addRole(roleStudent);
            return userRepository.save(u);
        });

        if (admin == null || studentJan == null || studentEva == null || lektorKarel == null) {
            log.info("UserRepository je mockován v testu nebo nevrací entity, inicializace ukázkových dat přeskočena.");
            return;
        }

        // 2. Kurzy (Idempotentní vytvoření s ochranou proti duplicitám)
        Course cJava = getOrCreateCourse(
                "Programování v Javě a Spring Boot",
                "Komplexní kurz vývoje backendových aplikací s využitím moderního ekosystému Java a Spring Framework.",
                80,
                new BigDecimal("24900.00"),
                true
        );

        Course cUcetnictvi = getOrCreateCourse(
                "Podvojné účetnictví a daňová evidence",
                "Rekvalifikační kurz zakončený osvědčením. Teorie i praktické procvičování účetních případů v moderním softwaru.",
                120,
                new BigDecimal("18500.00"),
                true
        );

        Course cFoto = getOrCreateCourse(
                "Základy digitální fotografie a grafiky",
                "Intenzivní praktický workshop pro začínající fotografy a grafiky – práce se zrcadlovkou a úprava fotografií.",
                24,
                new BigDecimal("6900.00"),
                false
        );

        Course cProjekt = getOrCreateCourse(
                "Projektové řízení a agilní metody",
                "Příprava na vedení moderních projektů s využitím metodik Scrum, Kanban a osvědčených technik time managementu.",
                40,
                new BigDecimal("14500.00"),
                true
        );

        Course cKomunikace = getOrCreateCourse(
                "Efektivní komunikace a asertivita",
                "Rozvoj měkkých dovedností pro vedoucí pracovníky, lektory a pracovníky v klientských službách.",
                16,
                new BigDecimal("4900.00"),
                false
        );

        Course cWeb = getOrCreateCourse(
                "Tvorba webových stránek v HTML, CSS a JavaScriptu",
                "Praktický kurz moderního webového vývoje od základů HTML5 a flexibilního CSS layoutu až po interaktivní webové skripty.",
                40,
                new BigDecimal("11900.00"),
                false
        );

        Course cMzdy = getOrCreateCourse(
                "Mzdové účetnictví a personalistika pro praxi",
                "Odborný rekvalifikační kurz zaměřený na výpočet mezd, zákonné odvody, daňová zvýhodnění a personální agendu.",
                60,
                new BigDecimal("15900.00"),
                true
        );

        Course cCyber = getOrCreateCourse(
                "Základy kybernetické bezpečnosti pro firmy",
                "Školení bezpečného chování ve firemní síti, prevence sociálního inženýrství, ochrana hesel a šifrování dat.",
                16,
                new BigDecimal("5900.00"),
                false
        );

        // 3. Termíny a navazující data (pouze pokud v DB zatím žádné termíny nejsou)
        if (courseTermRepository.count() == 0) {
            log.info("Vkládání ukázkových termínů, rozvrhu, pořadníku a osvědčení...");

            // Termín 1: Java (Plná kapacita 3 účastníci + 2 náhradníci v pořadníku + 2 lektoři Karel & Petra)
            CourseTerm termJava = new CourseTerm(cJava, LocalDate.now().plusDays(10), LocalDate.now().plusDays(25), "Učebna A1 (Počítačová)", 3);
            termJava.setStatus(TermStatus.PLANNED);
            termJava.addLecturer(lektorKarel);
            termJava.addLecturer(lektorPetra);
            termJava = courseTermRepository.save(termJava);

            // Dny rozvrhu pro termín Java (střídání dvou lektorů - ADR-05)
            termDayRepository.save(new TermDay(termJava, LocalDate.now().plusDays(10), LocalTime.of(9, 0), LocalTime.of(16, 0), 7, lektorKarel, "Úvod do OOP a Java Core"));
            termDayRepository.save(new TermDay(termJava, LocalDate.now().plusDays(12), LocalTime.of(9, 0), LocalTime.of(16, 0), 7, lektorKarel, "Kolekce, Streams a Lambda"));
            termDayRepository.save(new TermDay(termJava, LocalDate.now().plusDays(17), LocalTime.of(9, 0), LocalTime.of(16, 0), 7, lektorPetra, "Spring Boot & REST API"));
            termDayRepository.save(new TermDay(termJava, LocalDate.now().plusDays(24), LocalTime.of(9, 0), LocalTime.of(16, 0), 7, lektorPetra, "JPA, Hibernate & Závěrečný projekt"));

            // Přihlášky: 3 potvrzení (kapacita je plná!)
            registrationRepository.save(new Registration(termJava, studentJan, RegistrationStatus.CONFIRMED));
            registrationRepository.save(new Registration(termJava, studentEva, RegistrationStatus.CONFIRMED));
            registrationRepository.save(new Registration(termJava, studentTomas, RegistrationStatus.CONFIRMED));

            // Pořadník náhradníků: 2 čekající s přesným zachováním pořadí
            registrationRepository.save(new Registration(termJava, studentLucie, RegistrationStatus.WAITLISTED, 1, null));
            registrationRepository.save(new Registration(termJava, studentMartin, RegistrationStatus.WAITLISTED, 2, null));

            // Termín 2: Účetnictví (proběhl v minulosti, úspěšně dokončen, vydána osvědčení)
            CourseTerm termUcto = new CourseTerm(cUcetnictvi, LocalDate.now().minusDays(40), LocalDate.now().minusDays(10), "Učebna B2 (Přednášková)", 10);
            termUcto.setStatus(TermStatus.COMPLETED);
            termUcto.addLecturer(lektorPetra);
            termUcto = courseTermRepository.save(termUcto);

            TermDay tdU1 = termDayRepository.save(new TermDay(termUcto, LocalDate.now().minusDays(40), LocalTime.of(8, 0), LocalTime.of(16, 0), 8, lektorPetra, "Základy účetnictví"));
            TermDay tdU2 = termDayRepository.save(new TermDay(termUcto, LocalDate.now().minusDays(30), LocalTime.of(8, 0), LocalTime.of(16, 0), 8, lektorPetra, "Daňová soustava ČR"));
            TermDay tdU3 = termDayRepository.save(new TermDay(termUcto, LocalDate.now().minusDays(20), LocalTime.of(8, 0), LocalTime.of(16, 0), 8, lektorPetra, "Účetní závěrka"));
            TermDay tdU4 = termDayRepository.save(new TermDay(termUcto, LocalDate.now().minusDays(10), LocalTime.of(8, 0), LocalTime.of(16, 0), 8, lektorPetra, "Praktický zápočet"));

            Registration rU1 = registrationRepository.save(new Registration(termUcto, studentJan, RegistrationStatus.COMPLETED));
            Registration rU2 = registrationRepository.save(new Registration(termUcto, studentEva, RegistrationStatus.COMPLETED));

            // Docházka pro termUcto (100% přítomnost pro oba)
            for (TermDay td : List.of(tdU1, tdU2, tdU3, tdU4)) {
                attendanceRepository.save(new Attendance(td, rU1, true, "Přítomen"));
                attendanceRepository.save(new Attendance(td, rU2, true, "Přítomen"));
            }

            // Osvědčení pro studenta Jana a Evu (unikátní čísla)
            int year = LocalDate.now().getYear();
            certificateRepository.save(new Certificate(rU1, String.format("OSV-%d-0001", year), LocalDate.now().minusDays(10),
                    studentJan.getFullName(), cUcetnictvi.getName(), cUcetnictvi.getHoursDuration(), cUcetnictvi.isAccredited()));
            certificateRepository.save(new Certificate(rU2, String.format("OSV-%d-0002", year), LocalDate.now().minusDays(10),
                    studentEva.getFullName(), cUcetnictvi.getName(), cUcetnictvi.getHoursDuration(), cUcetnictvi.isAccredited()));

            // Termín 3: Fotografie (probíhající kurz, připravený k evidenci docházky)
            CourseTerm termFoto = new CourseTerm(cFoto, LocalDate.now().minusDays(2), LocalDate.now().plusDays(10), "Ateliér C1", 6);
            termFoto.setStatus(TermStatus.IN_PROGRESS);
            termFoto.addLecturer(lektorKarel);
            termFoto = courseTermRepository.save(termFoto);

            TermDay tdF1 = termDayRepository.save(new TermDay(termFoto, LocalDate.now().minusDays(2), LocalTime.of(10, 0), LocalTime.of(17, 0), 6, lektorKarel, "Ovládání zrcadlovky a kompozice"));
            termDayRepository.save(new TermDay(termFoto, LocalDate.now().plusDays(5), LocalTime.of(10, 0), LocalTime.of(17, 0), 6, lektorKarel, "Ateliérové svícení a portrét"));
            termDayRepository.save(new TermDay(termFoto, LocalDate.now().plusDays(10), LocalTime.of(10, 0), LocalTime.of(17, 0), 6, lektorKarel, "Postprodukce v Adobe Lightroom"));

            Registration rF1 = registrationRepository.save(new Registration(termFoto, studentTomas, RegistrationStatus.CONFIRMED));
            Registration rF2 = registrationRepository.save(new Registration(termFoto, studentLucie, RegistrationStatus.CONFIRMED));

            // Docházka pro 1. den: Tomáš přítomen, Lucie chyběla
            attendanceRepository.save(new Attendance(tdF1, rF1, true, "Přítomen"));
            attendanceRepository.save(new Attendance(tdF1, rF2, false, "Omluvena – nemoc"));

            // Termín 4: Projektové řízení (Plánovaný v budoucnu, volná kapacita)
            CourseTerm termProjekt = new CourseTerm(cProjekt, LocalDate.now().plusDays(30), LocalDate.now().plusDays(45), "Učebna B1", 8);
            termProjekt.setStatus(TermStatus.PLANNED);
            termProjekt.addLecturer(lektorPetra);
            termProjekt = courseTermRepository.save(termProjekt);

            termDayRepository.save(new TermDay(termProjekt, LocalDate.now().plusDays(30), LocalTime.of(9, 0), LocalTime.of(16, 0), 7, lektorPetra, "Agilní metody a Scrum"));
            termDayRepository.save(new TermDay(termProjekt, LocalDate.now().plusDays(45), LocalTime.of(9, 0), LocalTime.of(16, 0), 7, lektorPetra, "Řízení rozpočtu a rizik"));

            registrationRepository.save(new Registration(termProjekt, studentJan, RegistrationStatus.CONFIRMED));
            registrationRepository.save(new Registration(termProjekt, studentTomas, RegistrationStatus.CONFIRMED));

            // Termín 5: Webový vývoj (Plánovaný, volná kapacita)
            CourseTerm termWeb = new CourseTerm(cWeb, LocalDate.now().plusDays(15), LocalDate.now().plusDays(35), "Učebna A1 (Počítačová)", 5);
            termWeb.setStatus(TermStatus.PLANNED);
            termWeb.addLecturer(lektorKarel);
            termWeb = courseTermRepository.save(termWeb);

            termDayRepository.save(new TermDay(termWeb, LocalDate.now().plusDays(15), LocalTime.of(9, 0), LocalTime.of(15, 0), 6, lektorKarel, "HTML5 a CSS layout"));
            termDayRepository.save(new TermDay(termWeb, LocalDate.now().plusDays(25), LocalTime.of(9, 0), LocalTime.of(15, 0), 6, lektorKarel, "Responzivní design a Grid"));
            termDayRepository.save(new TermDay(termWeb, LocalDate.now().plusDays(35), LocalTime.of(9, 0), LocalTime.of(15, 0), 6, lektorKarel, "JavaScript & DOM"));

            registrationRepository.save(new Registration(termWeb, studentMartin, RegistrationStatus.CONFIRMED));
            registrationRepository.save(new Registration(termWeb, studentEva, RegistrationStatus.CONFIRMED));

            // Termín 6: Mzdové účetnictví (Plánovaný, volná kapacita)
            CourseTerm termMzdy = new CourseTerm(cMzdy, LocalDate.now().plusDays(50), LocalDate.now().plusDays(70), "Učebna B2 (Přednášková)", 12);
            termMzdy.setStatus(TermStatus.PLANNED);
            termMzdy.addLecturer(lektorPetra);
            termMzdy = courseTermRepository.save(termMzdy);

            termDayRepository.save(new TermDay(termMzdy, LocalDate.now().plusDays(50), LocalTime.of(8, 30), LocalTime.of(16, 30), 8, lektorPetra, "Pracovní právo a smlouvy"));
            termDayRepository.save(new TermDay(termMzdy, LocalDate.now().plusDays(60), LocalTime.of(8, 30), LocalTime.of(16, 30), 8, lektorPetra, "Výpočet mezd a odvody"));
            termDayRepository.save(new TermDay(termMzdy, LocalDate.now().plusDays(70), LocalTime.of(8, 30), LocalTime.of(16, 30), 8, lektorPetra, "Roční zúčtování a praxe v SW"));

            registrationRepository.save(new Registration(termMzdy, studentLucie, RegistrationStatus.CONFIRMED));

            // Termín 7: Kybernetická bezpečnost (Dokončený, osvědčení)
            CourseTerm termCyber = new CourseTerm(cCyber, LocalDate.now().minusDays(70), LocalDate.now().minusDays(55), "Učebna A1 (Počítačová)", 10);
            termCyber.setStatus(TermStatus.COMPLETED);
            termCyber.addLecturer(lektorKarel);
            termCyber = courseTermRepository.save(termCyber);

            TermDay tdC1 = termDayRepository.save(new TermDay(termCyber, LocalDate.now().minusDays(70), LocalTime.of(9, 0), LocalTime.of(17, 0), 8, lektorKarel, "Sociální inženýrství a hrozby"));
            TermDay tdC2 = termDayRepository.save(new TermDay(termCyber, LocalDate.now().minusDays(55), LocalTime.of(9, 0), LocalTime.of(17, 0), 8, lektorKarel, "Zabezpečení firemní sítě a dat"));

            Registration rC1 = registrationRepository.save(new Registration(termCyber, studentTomas, RegistrationStatus.COMPLETED));
            Registration rC2 = registrationRepository.save(new Registration(termCyber, studentMartin, RegistrationStatus.COMPLETED));

            for (TermDay td : List.of(tdC1, tdC2)) {
                attendanceRepository.save(new Attendance(td, rC1, true, "Přítomen"));
                attendanceRepository.save(new Attendance(td, rC2, true, "Přítomen"));
            }

            certificateRepository.save(new Certificate(rC1, String.format("OSV-%d-0003", year), LocalDate.now().minusDays(55),
                    studentTomas.getFullName(), cCyber.getName(), cCyber.getHoursDuration(), cCyber.isAccredited()));
            certificateRepository.save(new Certificate(rC2, String.format("OSV-%d-0004", year), LocalDate.now().minusDays(55),
                    studentMartin.getFullName(), cCyber.getName(), cCyber.getHoursDuration(), cCyber.isAccredited()));

            // Termín 8: Efektivní komunikace (Zrušený termín)
            CourseTerm termKomunikace = new CourseTerm(cKomunikace, LocalDate.now().minusDays(15), LocalDate.now().minusDays(14), "Učebna B1", 8);
            termKomunikace.setStatus(TermStatus.CANCELLED);
            termKomunikace.addLecturer(lektorPetra);
            termKomunikace = courseTermRepository.save(termKomunikace);

            Registration rK1 = new Registration(termKomunikace, studentJan, RegistrationStatus.CANCELLED);
            rK1.setCancellationReason("Termín byl zrušen vzdělávacím centrem z organizačních důvodů.");
            rK1.setCancelledAt(LocalDateTime.now().minusDays(16));
            registrationRepository.save(rK1);

            log.info("Ukázková data úspěšně inicializována.");
        }
    }

    private Course getOrCreateCourse(String name, String annotation, int hours, BigDecimal price, boolean accredited) {
        return courseRepository.findByName(name)
                .orElseGet(() -> courseRepository.save(new Course(name, annotation, hours, price, accredited)));
    }
}
