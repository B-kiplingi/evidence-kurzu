package cz.uhk.ppro.akademietrutnov.presentation.controller;

import cz.uhk.ppro.akademietrutnov.domain.model.Course;
import cz.uhk.ppro.akademietrutnov.domain.model.CourseTerm;
import cz.uhk.ppro.akademietrutnov.domain.model.Registration;
import cz.uhk.ppro.akademietrutnov.domain.model.User;
import cz.uhk.ppro.akademietrutnov.domain.service.CourseService;
import cz.uhk.ppro.akademietrutnov.domain.service.CourseTermService;
import cz.uhk.ppro.akademietrutnov.domain.service.RegistrationService;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class TermControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseTermService courseTermService;

    @MockBean
    private CourseService courseService;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("GET /terms je veřejně přístupný i bez přihlášení")
    void shouldAllowPublicAccessToTermsList() throws Exception {
        Course course = new Course("Java", "Anotace", 40, new BigDecimal("10000.00"), true);
        course.setId(1L);
        CourseTerm term = new CourseTerm(course, LocalDate.now(), LocalDate.now().plusDays(5), "A1", 5);
        term.setId(10L);

        when(courseTermService.findAll()).thenReturn(List.of(term));
        when(courseService.findAll()).thenReturn(List.of(course));

        mockMvc.perform(get("/terms"))
                .andExpect(status().isOk())
                .andExpect(view().name("terms/list"))
                .andExpect(model().attributeExists("terms", "courses"));
    }

    @Test
    @DisplayName("GET /terms/new vyžaduje roli ADMIN (přesměruje nepřihlášeného)")
    void shouldRedirectAnonymousFromNewTerm() throws Exception {
        mockMvc.perform(get("/terms/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("GET /terms/new je přístupný pro ADMIN")
    void shouldAllowAdminToAccessNewTermForm() throws Exception {
        when(courseService.findAll()).thenReturn(Collections.emptyList());
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/terms/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("terms/form"))
                .andExpect(model().attributeExists("termDto", "courses", "lecturers"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("POST /terms/{id}/promote-next povýší náhradníka na účastníka")
    void shouldPromoteNextWaitlistedWhenAdmin() throws Exception {
        User student = new User("st", "p", "Lucie Veselá", "l@ex.cz");
        Registration promoted = new Registration(new CourseTerm(), student, null);

        when(registrationService.promoteFirstWaitlisted(10L)).thenReturn(promoted);

        mockMvc.perform(post("/terms/10/promote-next").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/terms/10"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(registrationService).promoteFirstWaitlisted(10L);
    }
}
