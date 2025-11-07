package com.princely.shopmanager.investment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.princely.shopmanager.investment.domain.Investment;
import com.princely.shopmanager.investment.dto.InvestmentCreateRequest;
import com.princely.shopmanager.investment.dto.WithdrawalRequest;
import com.princely.shopmanager.test.TestConstants;
import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import com.princely.shopmanager.test.security.WithMockPermissions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for InvestmentController.
 * Tests granular permission-based authorization for investment management.
 */
@DisplayName("Investment Controller Integration Tests")
class InvestmentControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ============== Create Investment Tests ==============

    @Test
    @DisplayName("OWNER should be able to create investment")
    @WithMockPermissions(role = "OWNER", tenantId = TestConstants.TEST_TENANT_001, shopId = TestConstants.TEST_SHOP_001)
    void ownerShouldCreateInvestment() throws Exception {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId(TestConstants.USER_INVESTOR_001)
            .shopId(TestConstants.TEST_SHOP_001)
            .amount(BigDecimal.valueOf(50000.00))
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .profitPercentage(BigDecimal.valueOf(30.00))
            .maturityDate(LocalDateTime.now().plusMonths(6))
            .notes("Test investment")
            .build();

        mockMvc.perform(post("/api/investments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.amount").value(50000.00))
            .andExpect(jsonPath("$.investmentType").value("SHOP_WIDE"))
            .andExpect(jsonPath("$.profitSharingModel").value("PROPORTIONAL_BY_AMOUNT"))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("MANAGER should be able to create investment")
    @WithMockPermissions(role = "MANAGER", tenantId = TestConstants.TEST_TENANT_001, shopId = TestConstants.TEST_SHOP_001)
    void managerShouldCreateInvestment() throws Exception {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId(TestConstants.USER_INVESTOR_001)
            .shopId(TestConstants.TEST_SHOP_001)
            .amount(BigDecimal.valueOf(25000.00))
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .maturityDate(LocalDateTime.now().plusMonths(3))
            .profitPercentage(BigDecimal.valueOf(25.00))
            .notes("Manager created investment")
            .build();

        mockMvc.perform(post("/api/investments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.amount").value(25000.00));
    }

    @Test
    @DisplayName("EMPLOYEE should NOT be able to create investment")
    @WithMockPermissions(role = "EMPLOYEE", tenantId = TestConstants.TEST_TENANT_001, shopId = TestConstants.TEST_SHOP_001)
    void employeeShouldNotCreateInvestment() throws Exception {

        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId(TestConstants.USER_INVESTOR_001)
            .shopId(TestConstants.TEST_SHOP_001)
            .amount(BigDecimal.valueOf(10000.00))
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .maturityDate(LocalDateTime.now().plusMonths(3))
            .profitPercentage(BigDecimal.valueOf(20.00))
            .build();

        mockMvc.perform(post("/api/investments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("INVESTOR role should be able to create investment")
    @WithMockPermissions(role = "INVESTOR", tenantId = TestConstants.TEST_TENANT_001, shopId = TestConstants.TEST_SHOP_001)
    void investorShouldCreateInvestment() throws Exception {

        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId(TestConstants.USER_INVESTOR_001)
            .shopId(TestConstants.TEST_SHOP_001)
            .amount(BigDecimal.valueOf(75000.00))
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .maturityDate(LocalDateTime.now().plusYears(1))
            .profitPercentage(BigDecimal.valueOf(40.00))
            .notes("Investor created investment")
            .build();

        mockMvc.perform(post("/api/investments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.amount").value(75000.00));
    }

    // ============== Get Investments Tests ==============

    @Test
    @DisplayName("OWNER should list shop investments")
    @WithMockPermissions(role = "OWNER", tenantId = TestConstants.TEST_TENANT_001, shopId = TestConstants.TEST_SHOP_001)
    void ownerShouldListShopInvestments() throws Exception {
        mockMvc.perform(get("/api/shops/{shopId}/investments", TestConstants.TEST_SHOP_001)
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", isA(Iterable.class)));
    }

    @Test
    @DisplayName("MANAGER should list shop investments")
    @WithMockPermissions(role = "MANAGER", tenantId = TestConstants.TEST_TENANT_001, shopId = TestConstants.TEST_SHOP_001)
    void managerShouldListShopInvestments() throws Exception {
        mockMvc.perform(get("/api/shops/{shopId}/investments", TestConstants.TEST_SHOP_001))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("EMPLOYEE should NOT list shop investments")
    @WithMockPermissions(role = "EMPLOYEE", tenantId = TestConstants.TEST_TENANT_001, shopId = TestConstants.TEST_SHOP_001)
    void employeeShouldNotListShopInvestments() throws Exception {
        mockMvc.perform(get("/api/shops/{shopId}/investments", TestConstants.TEST_SHOP_001))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("INVESTOR should list their own investments")
    @WithMockPermissions(role = "INVESTOR", tenantId = TestConstants.TEST_TENANT_001, shopId = TestConstants.TEST_SHOP_001)
    void investorShouldListOwnInvestments() throws Exception {
        mockMvc.perform(get("/api/my-investments")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", isA(Iterable.class)));
    }

    // ============== Get Investment by ID Tests ==============

    @Test
    @DisplayName("OWNER should read investment details")
    @WithMockPermissions(role = "OWNER", tenantId = TestConstants.TEST_TENANT_001, shopId = TestConstants.TEST_SHOP_001)
    void ownerShouldReadInvestmentDetails() throws Exception {
        // First create an investment

        InvestmentCreateRequest createRequest = InvestmentCreateRequest.builder()
            .investorId(TestConstants.USER_INVESTOR_001)
            .shopId(TestConstants.TEST_SHOP_001)
            .amount(BigDecimal.valueOf(10000.00))
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .maturityDate(LocalDateTime.now().plusMonths(6))
            .profitPercentage(BigDecimal.valueOf(30.00))
            .build();

        String createResponse = mockMvc.perform(post("/api/investments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String investmentId = objectMapper.readTree(createResponse).get("id").asText();

        // Then read it
        mockMvc.perform(get("/api/investments/{investmentId}", investmentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(investmentId))
            .andExpect(jsonPath("$.amount").value(10000.00));
    }

    @Test
    @DisplayName("EMPLOYEE should NOT read investment details")
    @WithMockPermissions(role = "EMPLOYEE")
    void employeeShouldNotReadInvestmentDetails() throws Exception {
        mockMvc.perform(get("/api/investments/{investmentId}", "any-investment-id"))
            .andExpect(status().isForbidden());
    }

    // ============== Update Investment Status Tests ==============

    @Test
    @DisplayName("OWNER should update investment status")
    @WithMockPermissions(role = "OWNER", tenantId = TestConstants.TEST_TENANT_001, shopId = TestConstants.TEST_SHOP_001)
    void ownerShouldUpdateInvestmentStatus() throws Exception {
        // Create investment first

        InvestmentCreateRequest createRequest = InvestmentCreateRequest.builder()
            .investorId(TestConstants.USER_INVESTOR_001)
            .shopId(TestConstants.TEST_SHOP_001)
            .amount(BigDecimal.valueOf(15000.00))
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .maturityDate(LocalDateTime.now().plusMonths(6))
            .profitPercentage(BigDecimal.valueOf(30.00))
            .build();

        String createResponse = mockMvc.perform(post("/api/investments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String investmentId = objectMapper.readTree(createResponse).get("id").asText();

        // Update status
        mockMvc.perform(put("/api/investments/{investmentId}/status", investmentId)
                .with(csrf())
                .param("status", "MATURE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("MATURE"));
    }

    @Test
    @DisplayName("EMPLOYEE should NOT update investment status")
    @WithMockPermissions(role = "EMPLOYEE")
    void employeeShouldNotUpdateInvestmentStatus() throws Exception {
        mockMvc.perform(put("/api/investments/{investmentId}/status", "any-id")
                .with(csrf())
                .param("status", "MATURE"))
            .andExpect(status().isForbidden());
    }

    // ============== Withdrawal Tests ==============

    @Test
    @DisplayName("OWNER should process withdrawal")
    @WithMockPermissions(role = "OWNER", tenantId = TestConstants.TEST_TENANT_001, shopId = TestConstants.TEST_SHOP_001)
    void ownerShouldProcessWithdrawal() throws Exception {
        // Create investment first

        InvestmentCreateRequest createRequest = InvestmentCreateRequest.builder()
            .investorId(TestConstants.USER_INVESTOR_001)
            .shopId(TestConstants.TEST_SHOP_001)
            .amount(BigDecimal.valueOf(20000.00))
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .maturityDate(LocalDateTime.now().plusMonths(6))
            .profitPercentage(BigDecimal.valueOf(30.00))
            .build();

        String createResponse = mockMvc.perform(post("/api/investments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String investmentId = objectMapper.readTree(createResponse).get("id").asText();

        // Process withdrawal
        WithdrawalRequest withdrawalRequest = WithdrawalRequest.builder()
            .amount(BigDecimal.valueOf(5000.00))
            .reason("Partial withdrawal")
            .build();

        mockMvc.perform(post("/api/investments/{investmentId}/withdraw", investmentId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withdrawalRequest)))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("MANAGER should NOT process withdrawal")
    @WithMockPermissions(role = "MANAGER")
    void managerShouldNotProcessWithdrawal() throws Exception {
        WithdrawalRequest withdrawalRequest = WithdrawalRequest.builder()
            .amount(BigDecimal.valueOf(5000.00))
            .reason("Test")
            .build();

        mockMvc.perform(post("/api/investments/{investmentId}/withdraw", "any-id")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withdrawalRequest)))
            .andExpect(status().isForbidden());
    }

    // ============== Distribution Tests ==============

    @Test
    @DisplayName("OWNER should approve distribution")
    @WithMockPermissions(role = "OWNER")
    void ownerShouldApproveDistribution() throws Exception {
        // This will return 404 since we don't have a distribution created, but tests permission
        mockMvc.perform(post("/api/distributions/{distributionId}/approve", "dist-id")
                .with(csrf())
                .param("notes", "Approved"))
            .andExpect(status().isNotFound()); // 404 not 403, proving permission is granted
    }

    @Test
    @DisplayName("EMPLOYEE should NOT approve distribution")
    @WithMockPermissions(role = "EMPLOYEE")
    void employeeShouldNotApproveDistribution() throws Exception {
        mockMvc.perform(post("/api/distributions/{distributionId}/approve", "dist-id")
                .with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("OWNER should mark distribution as paid")
    @WithMockPermissions(role = "OWNER")
    void ownerShouldMarkDistributionAsPaid() throws Exception {
        mockMvc.perform(post("/api/distributions/{distributionId}/mark-paid", "dist-id")
                .with(csrf())
                .param("paymentReference", "PAY-123"))
            .andExpect(status().isNotFound()); // 404 not 403, proving permission is granted
    }

    @Test
    @DisplayName("MANAGER should NOT mark distribution as paid")
    @WithMockPermissions(role = "MANAGER")
    void managerShouldNotMarkDistributionAsPaid() throws Exception {
        mockMvc.perform(post("/api/distributions/{distributionId}/mark-paid", "dist-id")
                .with(csrf())
                .param("paymentReference", "PAY-123"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("INVESTOR should get their distributions")
    @WithMockPermissions(role = "INVESTOR")
    void investorShouldGetTheirDistributions() throws Exception {
        mockMvc.perform(get("/api/my-distributions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", isA(Iterable.class)));
    }

    // ============== Validation Tests ==============

    @Test
    @DisplayName("Should reject investment without investor ID")
    @WithMockPermissions(role = "OWNER", tenantId = TestConstants.TEST_TENANT_001, shopId = TestConstants.TEST_SHOP_001)
    void shouldRejectInvestmentWithoutInvestorId() throws Exception {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .shopId(TestConstants.TEST_SHOP_001)
            .amount(BigDecimal.valueOf(50000.00))
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .profitPercentage(BigDecimal.valueOf(30.00))
            .build();

        mockMvc.perform(post("/api/investments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject PRODUCT_SPECIFIC investment without product IDs")
    @WithMockPermissions(role = "OWNER", tenantId = TestConstants.TEST_TENANT_001, shopId = TestConstants.TEST_SHOP_001)
    void shouldRejectProductSpecificInvestmentWithoutProducts() throws Exception {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId(TestConstants.USER_INVESTOR_001)
            .shopId(TestConstants.TEST_SHOP_001)
            .amount(BigDecimal.valueOf(50000.00))
            .investmentType(Investment.InvestmentType.PRODUCT_SPECIFIC)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .profitPercentage(BigDecimal.valueOf(30.00))
            .build();

        mockMvc.perform(post("/api/investments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should accept PRODUCT_SPECIFIC investment with product IDs")
    @WithMockPermissions(role = "OWNER", tenantId = TestConstants.TEST_TENANT_001, shopId = TestConstants.TEST_SHOP_001)
    void shouldAcceptProductSpecificInvestmentWithProducts() throws Exception {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId(TestConstants.USER_INVESTOR_001)
            .shopId(TestConstants.TEST_SHOP_001)
            .amount(BigDecimal.valueOf(50000.00))
            .investmentType(Investment.InvestmentType.PRODUCT_SPECIFIC)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .profitPercentage(BigDecimal.valueOf(30.00))
            .productIds(List.of(TestConstants.PROD_WIRELESS_MOUSE, TestConstants.PROD_USB_KEYBOARD).stream()
                    .collect(java.util.stream.Collectors.toSet()))
            .build();

        mockMvc.perform(post("/api/investments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.investmentType").value("PRODUCT_SPECIFIC"));
    }

    @Test
    @DisplayName("Should reject CATEGORY_SPECIFIC investment without category filter")
    @WithMockPermissions(role = "OWNER", tenantId = TestConstants.TEST_TENANT_001, shopId = TestConstants.TEST_SHOP_001)
    void shouldRejectCategorySpecificInvestmentWithoutCategory() throws Exception {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId(TestConstants.USER_INVESTOR_001)
            .shopId(TestConstants.TEST_SHOP_001)
            .amount(BigDecimal.valueOf(50000.00))
            .investmentType(Investment.InvestmentType.CATEGORY_SPECIFIC)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .profitPercentage(BigDecimal.valueOf(30.00))
            .build();

        mockMvc.perform(post("/api/investments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should accept CATEGORY_SPECIFIC investment with category filter")
    @WithMockPermissions(role = "OWNER", tenantId = TestConstants.TEST_TENANT_001, shopId = TestConstants.TEST_SHOP_001)
    void shouldAcceptCategorySpecificInvestmentWithCategory() throws Exception {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId(TestConstants.USER_INVESTOR_001)
            .shopId(TestConstants.TEST_SHOP_001)
            .amount(BigDecimal.valueOf(50000.00))
            .investmentType(Investment.InvestmentType.CATEGORY_SPECIFIC)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .profitPercentage(BigDecimal.valueOf(30.00))
            .categoryFilter(TestConstants.CAT_ELECTRONICS)
            .build();

        mockMvc.perform(post("/api/investments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.investmentType").value("CATEGORY_SPECIFIC"));
    }

    @Test
    @DisplayName("Should reject SHOP_WIDE investment with product IDs")
    @WithMockPermissions(role = "OWNER", tenantId = TestConstants.TEST_TENANT_001, shopId = TestConstants.TEST_SHOP_001)
    void shouldRejectShopWideInvestmentWithProducts() throws Exception {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId(TestConstants.USER_INVESTOR_001)
            .shopId(TestConstants.TEST_SHOP_001)
            .amount(BigDecimal.valueOf(50000.00))
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .profitPercentage(BigDecimal.valueOf(30.00))
            .productIds(List.of(TestConstants.PROD_WIRELESS_MOUSE).stream()
                    .collect(java.util.stream.Collectors.toSet()))
            .build();

        mockMvc.perform(post("/api/investments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject PROPORTIONAL_BY_AMOUNT without profit percentage")
    @WithMockPermissions(role = "OWNER", tenantId = TestConstants.TEST_TENANT_001, shopId = TestConstants.TEST_SHOP_001)
    void shouldRejectProportionalByAmountWithoutProfitPercentage() throws Exception {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId(TestConstants.USER_INVESTOR_001)
            .shopId(TestConstants.TEST_SHOP_001)
            .amount(BigDecimal.valueOf(50000.00))
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .build();

        mockMvc.perform(post("/api/investments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject FIXED_SHARES without fixed shares value")
    @WithMockPermissions(role = "OWNER", tenantId = TestConstants.TEST_TENANT_001, shopId = TestConstants.TEST_SHOP_001)
    void shouldRejectFixedSharesWithoutSharesValue() throws Exception {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId(TestConstants.USER_INVESTOR_001)
            .shopId(TestConstants.TEST_SHOP_001)
            .amount(BigDecimal.valueOf(50000.00))
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.FIXED_SHARES)
            .build();

        mockMvc.perform(post("/api/investments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should accept FIXED_SHARES with valid shares value")
    @WithMockPermissions(role = "OWNER", tenantId = TestConstants.TEST_TENANT_001, shopId = TestConstants.TEST_SHOP_001)
    void shouldAcceptFixedSharesWithSharesValue() throws Exception {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId(TestConstants.USER_INVESTOR_001)
            .shopId(TestConstants.TEST_SHOP_001)
            .amount(BigDecimal.valueOf(50000.00))
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.FIXED_SHARES)
            .fixedShares(10)
            .build();

        mockMvc.perform(post("/api/investments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.profitSharingModel").value("FIXED_SHARES"))
            .andExpect(jsonPath("$.fixedShares").value(10));
    }

    @Test
    @DisplayName("Should reject TIME_WEIGHTED without profit percentage")
    @WithMockPermissions(role = "OWNER", tenantId = TestConstants.TEST_TENANT_001, shopId = TestConstants.TEST_SHOP_001)
    void shouldRejectTimeWeightedWithoutProfitPercentage() throws Exception {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId(TestConstants.USER_INVESTOR_001)
            .shopId(TestConstants.TEST_SHOP_001)
            .amount(BigDecimal.valueOf(50000.00))
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.TIME_WEIGHTED)
            .maturityDate(LocalDateTime.now().plusMonths(6))
            .build();

        mockMvc.perform(post("/api/investments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject profit percentage above 100")
    @WithMockPermissions(role = "OWNER", tenantId = TestConstants.TEST_TENANT_001, shopId = TestConstants.TEST_SHOP_001)
    void shouldRejectProfitPercentageAbove100() throws Exception {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId(TestConstants.USER_INVESTOR_001)
            .shopId(TestConstants.TEST_SHOP_001)
            .amount(BigDecimal.valueOf(50000.00))
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .profitPercentage(BigDecimal.valueOf(150.00))
            .build();

        mockMvc.perform(post("/api/investments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}
