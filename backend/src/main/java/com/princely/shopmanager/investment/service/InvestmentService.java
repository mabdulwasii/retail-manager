package com.princely.shopmanager.investment.service;

import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.core.domain.Product;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.ProductRepository;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.investment.domain.Investment;
import com.princely.shopmanager.investment.domain.InvestorDistribution;
import com.princely.shopmanager.investment.dto.*;
import com.princely.shopmanager.investment.repository.InvestmentRepository;
import com.princely.shopmanager.investment.repository.InvestorDistributionRepository;
import com.princely.shopmanager.shared.service.AuditService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final InvestorDistributionRepository distributionRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final AuditService auditService;

    public InvestmentResponse createInvestment(InvestmentCreateRequest request, String investorId) {
        log.info("Creating investment for investor: {}, shop: {}", investorId, request.getShopId());

        User investor = userRepository.findById(investorId)
            .orElseThrow(() -> new EntityNotFoundException("Investor not found"));

        Shop shop = shopRepository.findById(request.getShopId())
            .orElseThrow(() -> new EntityNotFoundException("Shop not found"));

        // Validate products if specified
        Set<Product> products = Set.of();
        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            products = productRepository.findAllById(request.getProductIds())
                .stream()
                .collect(Collectors.toSet());

            if (products.size() != request.getProductIds().size()) {
                throw new IllegalArgumentException("Some specified products were not found");
            }
        }

        // Generate investment number
        String investmentNumber = generateInvestmentNumber();

        Investment investment = Investment.builder()
            .investmentNumber(investmentNumber)
            .investor(investor)
            .shop(shop)
            .investmentType(request.getInvestmentType())
            .amount(request.getAmount())
            .profitSharingModel(request.getProfitSharingModel())
            .profitPercentage(request.getProfitPercentage())
            .fixedShares(request.getFixedShares())
            .investmentDate(LocalDateTime.now())
            .maturityDate(request.getMaturityDate())
            .products(products)
            .notes(request.getNotes())
            .status(Investment.InvestmentStatus.ACTIVE)
            .build();

        investment = investmentRepository.save(investment);

        auditService.logEntityCreation("Investment", investment.getId(),
            String.format("Created investment %s for ₦%s by investor %s",
                investmentNumber, request.getAmount(), investor.getEmail()));

        return mapToResponse(investment);
    }

    @Transactional(readOnly = true)
    public Page<InvestmentResponse> getInvestments(String shopId, Pageable pageable) {
        return investmentRepository.findByShopId(shopId, pageable)
            .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<InvestmentResponse> getInvestmentsByInvestor(String investorId, Pageable pageable) {
        return investmentRepository.findByInvestorId(investorId, pageable)
            .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public InvestmentResponse getInvestmentById(String investmentId) {
        Investment investment = investmentRepository.findById(investmentId)
            .orElseThrow(() -> new EntityNotFoundException("Investment not found"));
        return mapToResponse(investment);
    }

    public InvestmentResponse updateInvestmentStatus(String investmentId, Investment.InvestmentStatus status) {
        Investment investment = investmentRepository.findById(investmentId)
            .orElseThrow(() -> new EntityNotFoundException("Investment not found"));

        Investment.InvestmentStatus previousStatus = investment.getStatus();
        investment.setStatus(status);
        investment = investmentRepository.save(investment);

        auditService.logEntityModification("Investment", investment.getId(),
            String.format("Status changed from %s to %s", previousStatus, status));

        return mapToResponse(investment);
    }

    public InvestmentResponse processWithdrawal(String investmentId, WithdrawalRequest request) {
        Investment investment = investmentRepository.findById(investmentId)
            .orElseThrow(() -> new EntityNotFoundException("Investment not found"));

        if (!investment.canWithdraw(request.getAmount())) {
            throw new IllegalStateException("Insufficient balance for withdrawal");
        }

        BigDecimal previousWithdrawn = investment.getTotalWithdrawn();
        investment.setTotalWithdrawn(previousWithdrawn.add(request.getAmount()));
        investment = investmentRepository.save(investment);

        auditService.logEntityModification("Investment", investment.getId(),
            String.format("Processed withdrawal of ₦%s. Reason: %s",
                request.getAmount(), request.getReason()));

        log.info("Processed withdrawal of ₦{} for investment {}", request.getAmount(), investmentId);

        return mapToResponse(investment);
    }

    @Transactional(readOnly = true)
    public List<InvestorDistributionResponse> getDistributions(String investmentId) {
        return distributionRepository.findByInvestmentIdOrderByPeriodStartDesc(investmentId)
            .stream()
            .map(this::mapDistributionToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InvestorDistributionResponse> getDistributionsByInvestor(String investorId) {
        return distributionRepository.findByInvestmentInvestorIdOrderByPeriodStartDesc(investorId)
            .stream()
            .map(this::mapDistributionToResponse)
            .collect(Collectors.toList());
    }

    public InvestorDistributionResponse approveDistribution(String distributionId, String notes) {
        InvestorDistribution distribution = distributionRepository.findById(distributionId)
            .orElseThrow(() -> new EntityNotFoundException("Distribution not found"));

        if (distribution.getStatus() != InvestorDistribution.DistributionStatus.CALCULATED) {
            throw new IllegalStateException("Distribution can only be approved if it's in CALCULATED status");
        }

        distribution.setStatus(InvestorDistribution.DistributionStatus.APPROVED);
        distribution.setNotes(notes);
        distribution = distributionRepository.save(distribution);

        auditService.logEntityModification("InvestorDistribution", distribution.getId(),
            "Distribution approved for payment");

        return mapDistributionToResponse(distribution);
    }

    public InvestorDistributionResponse markDistributionAsPaid(String distributionId, String paymentReference) {
        InvestorDistribution distribution = distributionRepository.findById(distributionId)
            .orElseThrow(() -> new EntityNotFoundException("Distribution not found"));

        if (!distribution.canBePaid()) {
            throw new IllegalStateException("Distribution must be approved before it can be marked as paid");
        }

        distribution.markAsPaid(paymentReference);
        distribution = distributionRepository.save(distribution);

        // Update investment total profit earned
        Investment investment = distribution.getInvestment();
        investment.setTotalProfitEarned(
            investment.getTotalProfitEarned().add(distribution.getDistributionAmount())
        );
        investmentRepository.save(investment);

        auditService.logEntityModification("InvestorDistribution", distribution.getId(),
            String.format("Distribution marked as paid. Payment reference: %s", paymentReference));

        return mapDistributionToResponse(distribution);
    }

    private String generateInvestmentNumber() {
        return "INV-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private InvestmentResponse mapToResponse(Investment investment) {
        Set<InvestmentResponse.ProductInfo> productInfos = investment.getProducts().stream()
            .map(product -> InvestmentResponse.ProductInfo.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory() != null ? product.getCategory().getName() : null)
                .price(product.getPrice())
                .build())
            .collect(Collectors.toSet());

        return InvestmentResponse.builder()
            .id(investment.getId())
            .investmentNumber(investment.getInvestmentNumber())
            .investorId(investment.getInvestor().getId())
            .investorName(investment.getInvestor().getName())
            .investorEmail(investment.getInvestor().getEmail())
            .shopId(investment.getShop().getId())
            .shopName(investment.getShop().getName())
            .investmentType(investment.getInvestmentType())
            .amount(investment.getAmount())
            .profitSharingModel(investment.getProfitSharingModel())
            .profitPercentage(investment.getProfitPercentage())
            .fixedShares(investment.getFixedShares())
            .investmentDate(investment.getInvestmentDate())
            .maturityDate(investment.getMaturityDate())
            .status(investment.getStatus())
            .totalProfitEarned(investment.getTotalProfitEarned())
            .totalWithdrawn(investment.getTotalWithdrawn())
            .availableBalance(investment.getAvailableBalance())
            .lastProfitCalculation(investment.getLastProfitCalculation())
            .products(productInfos)
            .notes(investment.getNotes())
            .createdAt(investment.getCreatedAt())
            .updatedAt(investment.getUpdatedAt())
            .build();
    }

    private InvestorDistributionResponse mapDistributionToResponse(InvestorDistribution distribution) {
        return InvestorDistributionResponse.builder()
            .id(distribution.getId())
            .investmentId(distribution.getInvestment().getId())
            .investmentNumber(distribution.getInvestment().getInvestmentNumber())
            .investorName(distribution.getInvestment().getInvestor().getName())
            .periodStart(distribution.getPeriodStart())
            .periodEnd(distribution.getPeriodEnd())
            .totalSalesRevenue(distribution.getTotalSalesRevenue())
            .totalProfit(distribution.getTotalProfit())
            .investorSharePercentage(distribution.getInvestorSharePercentage())
            .investorProfitAmount(distribution.getInvestorProfitAmount())
            .distributionAmount(distribution.getDistributionAmount())
            .status(distribution.getStatus())
            .distributionDate(distribution.getDistributionDate())
            .paymentReference(distribution.getPaymentReference())
            .notes(distribution.getNotes())
            .calculationDetails(distribution.getCalculationDetails())
            .createdAt(distribution.getCreatedAt())
            .build();
    }
}