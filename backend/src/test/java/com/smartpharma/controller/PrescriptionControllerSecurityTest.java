package com.smartpharma.controller;

import com.smartpharma.entity.Prescription;
import com.smartpharma.service.OperationLogService;
import com.smartpharma.service.PrescriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 处方审核接口权限单元测试（7.3 改进：审核仅 ADMIN、REVIEWER；护士仅可 GET）
 */
@WebMvcTest(controllers = PrescriptionController.class)
@Import(com.smartpharma.config.SecurityConfig.class)
@MockBean(com.smartpharma.security.JwtAuthFilter.class)
@MockBean(PrescriptionService.class)
@MockBean(OperationLogService.class)
class PrescriptionControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PrescriptionService prescriptionService;

    @Test
    @WithMockUser(roles = "DOCTOR")
    void approve_asDoctor_returns403() throws Exception {
        mockMvc.perform(post("/prescription/1/approve"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "reviewer", roles = "REVIEWER")
    void approve_asReviewer_returns200() throws Exception {
        Prescription p = new Prescription();
        p.setId(1L);
        when(prescriptionService.approve(1L, null)).thenReturn(p);

        mockMvc.perform(post("/prescription/1/approve"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void reject_asDoctor_returns403() throws Exception {
        mockMvc.perform(post("/prescription/1/reject").param("remark", "原因"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "NURSE")
    void list_asNurse_returns200() throws Exception {
        when(prescriptionService.list(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(), org.springframework.data.domain.PageRequest.of(0, 10), 0));

        mockMvc.perform(get("/prescription"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "NURSE")
    void create_asNurse_returns403() throws Exception {
        mockMvc.perform(post("/prescription")
                        .contentType("application/json")
                        .content("{\"patientName\":\"张三\",\"doctorName\":\"李医生\"}"))
                .andExpect(status().isForbidden());
    }
}
