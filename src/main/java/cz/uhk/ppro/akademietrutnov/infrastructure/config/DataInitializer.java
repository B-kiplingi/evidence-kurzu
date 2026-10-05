package cz.uhk.ppro.akademietrutnov.infrastructure.config;

import cz.uhk.ppro.akademietrutnov.domain.model.Course;
import cz.uhk.ppro.akademietrutnov.domain.model.Role;
import cz.uhk.ppro.akademietrutnov.domain.model.User;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.CourseRepository;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.RoleRepository;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           CourseRepository courseRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
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

        if (!userRepository.existsByUsername("admin")) {
            User admin = new User("admin", passwordEncoder.encode("admin123"), "Správce Akademie", "admin@akademietrutnov.cz");
            admin.addRole(roleAdmin);
            admin.addRole(roleLecturer);
            userRepository.save(admin);
            log.info("Vytvořen výchozí uživatel: admin / admin123");
        }

        if (!userRepository.existsByUsername("lektor")) {
            User lektor = new User("lektor", passwordEncoder.encode("lektor123"), "Ing. Karel Lektor", "karel.lektor@akademietrutnov.cz");
            lektor.addRole(roleLecturer);
            userRepository.save(lektor);
            log.info("Vytvořen výchozí uživatel: lektor / lektor123");
        }

        if (!userRepository.existsByUsername("student")) {
            User student = new User("student", passwordEncoder.encode("student123"), "Jan Novák", "jan.novak@example.cz");
            student.addRole(roleStudent);
            userRepository.save(student);
            log.info("Vytvořen výchozí uživatel: student / student123");
        }

        if (courseRepository.count() == 0) {
            log.info("Vkládání ukázkových kurzů...");
            courseRepository.saveAll(List.of(
                    new Course(
                            "Programování v Javě a Spring Boot",
                            "Komplexní kurz vývoje backendových aplikací s využitím moderního ekosystému Java a Spring Framework.",
                            80,
                            new BigDecimal("24900.00"),
                            true
                    ),
                    new Course(
                            "Podvojné účetnictví a daňová evidence",
                            "Rekvalifikační kurz zakončený osvědčením. Teorie i praktické procvičování účetních případů v moderním softwaru.",
                            120,
                            new BigDecimal("18500.00"),
                            true
                    ),
                    new Course(
                            "Základy digitální fotografie a grafiky",
                            "Intenzivní praktický workshop pro začínající fotografy a grafiky – práce se zrcadlovkou a úprava fotografií.",
                            24,
                            new BigDecimal("6900.00"),
                            false
                    ),
                    new Course(
                            "Projektové řízení a agilní metody",
                            "Příprava na vedení moderních projektů s využitím metodik Scrum, Kanban a osvědčených technik time managementu.",
                            40,
                            new BigDecimal("14500.00"),
                            true
                    ),
                    new Course(
                            "Efektivní komunikace a asertivita",
                            "Rozvoj měkkých dovedností pro vedoucí pracovníky, lektory a pracovníky v klientských službách.",
                            16,
                            new BigDecimal("4900.00"),
                            false
                    )
            ));
            log.info("Ukázkové kurzy úspěšně vloženy.");
        }
    }
}
