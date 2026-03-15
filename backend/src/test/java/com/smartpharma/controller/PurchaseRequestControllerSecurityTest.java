package com.smartpharma.controller;

import com.smartpharma.entity.PurchaseRequest;
import com.smartpharma.entity.User;
import com.smartpharma.service.OperationLogService;
import com.smartpharma.service.PurchaseRequestService;
import com.smartpharma.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 采购审批接口权限单元测试（7.3 改进：审批仅 ADMIN、REVIEWER）
 */
@WebMvcTest(controllers = PurchaseRequestController.class)
@Import(com.smartpharma.config.SecurityConfig.class)
@MockBean(com.smartpharma.security.JwtAuthFilter.class)
@MockBean(PurchaseRequestService.class)
@MockBean(UserService.class)
@MockBean(OperationLogService.class)
class PurchaseRequestControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PurchaseRequestService purchaseRequestService;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(roles = "PURCHASER")
    void approve_asPurchaser_returns403() throws Exception {
        mockMvc.perform(post("/purchase/1/approve"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PHARMACIST")
    void approve_asPharmacist_returns403() throws Exception {
        mockMvc.perform(post("/purchase/1/approve"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "reviewer", roles = "REVIEWER")
    void approve_asReviewer_returns200Or404() throws Exception {
        User reviewer = new User();
        reviewer.setId(2L);
        reviewer.setUsername("reviewer");
        when(userService.getByUsername("reviewer")).thenReturn(reviewer);
        PurchaseRequest approved = new PurchaseRequest();
        approved.setId(1L);
        when(purchaseRequestService.approve(1L, 2L)).thenReturn(approved);

        mockMvc.perform(post("/purchase/1/approve"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PURCHASER")
    void reject_asPurchaser_returns403() throws Exception {
        mockMvc.perform(post("/purchase/1/reject").param("rejectReason", "不需要"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "reviewer", roles = "REVIEWER")
    void reject_asReviewer_returns200Or404() throws Exception {
        User reviewer = new User();
        reviewer.setId(2L);
        reviewer.setUsername("reviewer");
        when(userService.getByUsername("reviewer")).thenReturn(reviewer);
        PurchaseRequest rejected = new PurchaseRequest();
        rejected.setId(1L);
        when(purchaseRequestService.reject(1L, 2L, "不需要")).thenReturn(rejected);

        mockMvc.perform(post("/purchase/1/reject").param("rejectReason", "不需要"))
                .andExpect(status().isOk());
    }
}
