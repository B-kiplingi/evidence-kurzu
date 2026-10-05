package cz.uhk.ppro.akademietrutnov.presentation.controller;

import cz.uhk.ppro.akademietrutnov.domain.model.Course;
import cz.uhk.ppro.akademietrutnov.domain.service.CourseService;
import cz.uhk.ppro.akademietrutnov.presentation.dto.CourseDto;
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
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @Test
    @DisplayName("GET /courses je veřejně dostupný i pro nepřihlášené uživatele")
    void shouldAllowPublicAccessToCoursesList() throws Exception {
        Course course = new Course("Java Core", "Popis", 40, new BigDecimal("10000.00"), true);
        course.setId(1L);
        when(courseService.searchByName(any())).thenReturn(List.of(course));

        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/list"))
                .andExpect(model().attributeExists("courses"));
    }

    @Test
    @DisplayName("GET /courses/new přesměruje nepřihlášeného uživatele na /login")
    void shouldRedirectAnonymousUserFromCreateForm() throws Exception {
        mockMvc.perform(get("/courses/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("GET /courses/new je přístupný pro uživatele s rolí ADMIN")
    void shouldAllowAdminToAccessCreateForm() throws Exception {
        mockMvc.perform(get("/courses/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/form"))
                .andExpect(model().attributeExists("course"))
                .andExpect(model().attribute("isEdit", false));
    }

    @Test
    @WithMockUser(username = "student", roles = {"STUDENT"})
    @DisplayName("GET /courses/new vrátí 403 Forbidden pro studenta bez role ADMIN")
    void shouldForbidStudentFromCreateForm() throws Exception {
        mockMvc.perform(get("/courses/new"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("POST /courses vytvoří kurz a přesměruje na detail")
    void shouldCreateCourseWhenAdmin() throws Exception {
        Course created = new Course("Nový kurz", "Popis", 30, new BigDecimal("9000.00"), false);
        created.setId(5L);
        when(courseService.create(any(CourseDto.class))).thenReturn(created);

        mockMvc.perform(post("/courses")
                        .with(csrf())
                        .param("name", "Nový kurz")
                        .param("hoursDuration", "30")
                        .param("price", "9000.00")
                        .param("annotation", "Popis")
                        .param("accredited", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/5"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(courseService).create(any(CourseDto.class));
    }
}
