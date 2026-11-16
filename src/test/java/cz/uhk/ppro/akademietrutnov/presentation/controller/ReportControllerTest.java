package cz.uhk.ppro.akademietrutnov.presentation.controller;

import cz.uhk.ppro.akademietrutnov.domain.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Test
    @DisplayName("GET /reports přesměruje nepřihlášeného uživatele na /login")
    void shouldRedirectAnonymousUser() throws Exception {
        mockMvc.perform(get("/reports"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "student", roles = {"STUDENT"})
    @DisplayName("GET /reports vrátí 403 Forbidden pro studenta")
    void shouldForbidStudentFromReports() throws Exception {
        mockMvc.perform(get("/reports"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("GET /reports zobrazí manažerský přehled pro ADMIN")
    void shouldAllowAdminToAccessReports() throws Exception {
        when(reportService.getTermOccupancyReport()).thenReturn(Collections.emptyList());
        when(reportService.getLecturerHoursReport()).thenReturn(Collections.emptyList());
        when(reportService.getMultiCourseStudentsReport(anyInt())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/reports"))
                .andExpect(status().isOk())
                .andExpect(view().name("reports/dashboard"))
                .andExpect(model().attributeExists("occupancyReport", "lecturerReport", "multiCourseReport"));
    }
}
