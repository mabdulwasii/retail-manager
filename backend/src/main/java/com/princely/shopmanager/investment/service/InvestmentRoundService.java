package com.princely.shopmanager.investment.service;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.investment.domain.Investment;
import com.princely.shopmanager.investment.domain.InvestmentRound;
import com.princely.shopmanager.investment.dto.InvestmentRoundCreateRequest;
import com.princely.shopmanager.investment.dto.InvestmentRoundResponse;
import com.princely.shopmanager.investment.repository.InvestmentRepository;
import com.princely.shopmanager.investment.repository.InvestmentRoundRepository;
import com.princely.shopmanager.investment.validator.InvestmentRoundValidator;
import com.princely.shopmanager.shared.domain.AuditLog;
import com.princely.shopmanager.shared.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing investment rounds.
 *
 * An investment round groups multiple investors with shared configuration:
 * - Same profit sharing model
 * - Same investment type
 * - Same maturity date
 * - Shared tier/time weighting rules (if applicable)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvestmentRoundService {

    private final InvestmentRoundRepository investmentRoundRepository;
    private final InvestmentRepository investmentRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final InvestmentRoundValidator validator;
    private final AuditService auditService;

    /**
     * Create a new investment round with multiple investors.
     *
     * @param request Investment round creation request
     * @param createdBy Username of creator
     * @return Created investment round response
     */
    @Transactional
    public InvestmentRoundResponse createInvestmentRound(InvestmentRoundCreateRequest request, String createdBy) {
        log.info("Creating investment round for shop {} with {} investors",
            request.getShopId(), request.getInvestors().size());

        // Validate request
        List<String> errors = validator.validate(request);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Validation failed: " + String.join(", ", errors));
        }

        // Verify shop exists
        Shop shop = shopRepository.findById(request.getShopId())
            .orElseThrow(() -> new IllegalArgumentException("Shop not found: " + request.getShopId()));

        // Generate round number
        String roundNumber = generateRoundNumber(shop);

        // Build investment round
        InvestmentRound.InvestmentRoundBuilder builder = InvestmentRound.builder()
            .roundNumber(roundNumber)
            .shop(shop)
            .investmentType(request.getInvestmentType())
            .profitSharingModel(request.getProfitSharingModel())
            .maturityDate(request.getMaturityDate() != null ? request.getMaturityDate().atStartOfDay() : null)
            .status(InvestmentRound.RoundStatus.OPEN)
            .notes(request.getNotes());

        // Add tier configuration if TIERED model
        if (request.getProfitSharingModel() == Investment.ProfitSharingModel.TIERED) {
            builder.tierConfiguration(request.getTierConfiguration().toEntity());
        }

        // Add time weighting rules if TIME_WEIGHTED model
        if (request.getProfitSharingModel() == Investment.ProfitSharingModel.TIME_WEIGHTED) {
            builder.timeWeightingRules(request.getTimeWeightingRules().toEntity());
        }

        InvestmentRound round = builder.build();
        round = investmentRoundRepository.save(round);

        // Create investments for each investor
        for (InvestmentRoundCreateRequest.InvestorInput investorInput : request.getInvestors()) {
            User investor = userRepository.findById(investorInput.getInvestorId())
                .orElseThrow(() -> new IllegalArgumentException("Investor not found: " + investorInput.getInvestorId()));

            String investmentNumber = generateInvestmentNumber(shop, round);

            Investment investment = Investment.builder()
                .investmentNumber(investmentNumber)
                .investor(investor)
                .shop(shop)
                .investmentRound(round)
                .amount(investorInput.getAmount())
                .fixedShares(investorInput.getFixedShares())
                .investmentDate(LocalDateTime.now())
                .status(Investment.InvestmentStatus.ACTIVE)
                .totalProfitEarned(BigDecimal.ZERO)
                .totalWithdrawn(BigDecimal.ZERO)
                .notes(investorInput.getNotes())
                .build();

            investmentRepository.save(investment);
            round.getInvestments().add(investment);
        }

        // Audit log
        auditService.logFinancialTransaction(
            shop,
            createdBy,
            createdBy,
            AuditLog.ActionType.INVESTMENT_CREATED,
            round.getId(),
            String.format("Investment round %s created with %d investors - Total: %s",
                roundNumber, request.getInvestors().size(),
                request.getInvestors().stream()
                    .map(InvestmentRoundCreateRequest.InvestorInput::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)),
            true
        );

        log.info("Created investment round {} with {} investments", round.getId(), round.getInvestments().size());

        return mapToResponse(round);
    }

    /**
     * Get investment round by ID.
     *
     * @param roundId Investment round ID
     * @return Investment round response
     */
    @Transactional(readOnly = true)
    public InvestmentRoundResponse getInvestmentRound(String roundId) {
        InvestmentRound round = investmentRoundRepository.findById(roundId)
            .orElseThrow(() -> new IllegalArgumentException("Investment round not found: " + roundId));
        return mapToResponse(round);
    }

    /**
     * List investment rounds for a shop.
     *
     * @param shopId Shop ID
     * @param pageable Pagination parameters
     * @return Page of investment rounds
     */
    @Transactional(readOnly = true)
    public Page<InvestmentRoundResponse> listInvestmentRounds(String shopId, Pageable pageable) {
        return investmentRoundRepository.findByShopId(shopId, pageable)
            .map(this::mapToResponse);
    }

    /**
     * Update investment round configuration.
     * Only allows updating notes and status (cannot change investors or amounts).
     *
     * @param roundId Investment round ID
     * @param request Update request
     * @param updatedBy Username of updater
     * @return Updated investment round
     */
    @Transactional
    public InvestmentRoundResponse updateInvestmentRound(String roundId,
                                                         InvestmentRoundCreateRequest request,
                                                         String updatedBy) {
        log.info("Updating investment round {}", roundId);

        InvestmentRound round = investmentRoundRepository.findById(roundId)
            .orElseThrow(() -> new IllegalArgumentException("Investment round not found: " + roundId));

        if (round.getStatus() == InvestmentRound.RoundStatus.CLOSED ||
            round.getStatus() == InvestmentRound.RoundStatus.COMPLETED) {
            throw new IllegalStateException("Cannot update closed or completed round");
        }

        // Update allowed fields
        round.setNotes(request.getNotes());

        if (request.getMaturityDate() != null) {
            round.setMaturityDate(request.getMaturityDate().atStartOfDay());
        }

        round = investmentRoundRepository.save(round);

        auditService.logFinancialTransaction(
            round.getShop(),
            updatedBy,
            updatedBy,
            AuditLog.ActionType.INVESTMENT_UPDATED,
            round.getId(),
            String.format("Investment round %s updated", round.getRoundNumber()),
            true
        );

        return mapToResponse(round);
    }

    /**
     * Delete an investment round.
     * Only allowed if no profit distributions have been made.
     *
     * @param roundId Investment round ID
     * @param deletedBy Username of deleter
     */
    @Transactional
    public void deleteInvestmentRound(String roundId, String deletedBy) {
        log.info("Deleting investment round {}", roundId);

        InvestmentRound round = investmentRoundRepository.findById(roundId)
            .orElseThrow(() -> new IllegalArgumentException("Investment round not found: " + roundId));

        // Check if any distributions have been made
        boolean hasDistributions = round.getInvestments().stream()
            .anyMatch(inv -> inv.getTotalProfitEarned().compareTo(BigDecimal.ZERO) > 0);

        if (hasDistributions) {
            throw new IllegalStateException("Cannot delete round with profit distributions");
        }

        auditService.logFinancialTransaction(
            round.getShop(),
            deletedBy,
            deletedBy,
            AuditLog.ActionType.INVESTMENT_DELETED,
            round.getId(),
            String.format("Investment round %s deleted", round.getRoundNumber()),
            true
        );

        investmentRoundRepository.delete(round);
        log.info("Deleted investment round {}", roundId);
    }

    /**
     * Close an investment round to new investors.
     *
     * @param roundId Investment round ID
     * @param closedBy Username of user closing round
     * @return Updated investment round
     */
    @Transactional
    public InvestmentRoundResponse closeRound(String roundId, String closedBy) {
        log.info("Closing investment round {}", roundId);

        InvestmentRound round = investmentRoundRepository.findById(roundId)
            .orElseThrow(() -> new IllegalArgumentException("Investment round not found: " + roundId));

        if (round.getStatus() != InvestmentRound.RoundStatus.OPEN) {
            throw new IllegalStateException("Round is not open: " + round.getStatus());
        }

        round.setStatus(InvestmentRound.RoundStatus.CLOSED);
        round = investmentRoundRepository.save(round);

        auditService.logFinancialTransaction(
            round.getShop(),
            closedBy,
            closedBy,
            AuditLog.ActionType.INVESTMENT_UPDATED,
            round.getId(),
            String.format("Investment round %s closed", round.getRoundNumber()),
            true
        );

        log.info("Closed investment round {}", roundId);
        return mapToResponse(round);
    }

    /**
     * Add an investor to an existing open round.
     *
     * @param roundId Investment round ID
     * @param investorInput Investor input data
     * @param addedBy Username of user adding investor
     * @return Updated investment round
     */
    @Transactional
    public InvestmentRoundResponse addInvestorToRound(String roundId,
                                                      InvestmentRoundCreateRequest.InvestorInput investorInput,
                                                      String addedBy) {
        log.info("Adding investor {} to round {}", investorInput.getInvestorId(), roundId);

        InvestmentRound round = investmentRoundRepository.findById(roundId)
            .orElseThrow(() -> new IllegalArgumentException("Investment round not found: " + roundId));

        if (round.getStatus() != InvestmentRound.RoundStatus.OPEN) {
            throw new IllegalStateException("Can only add investors to OPEN rounds");
        }

        User investor = userRepository.findById(investorInput.getInvestorId())
            .orElseThrow(() -> new IllegalArgumentException("Investor not found: " + investorInput.getInvestorId()));

        // Check for duplicate investor
        boolean alreadyInvested = round.getInvestments().stream()
            .anyMatch(inv -> inv.getInvestor().getId().equals(investor.getId()));

        if (alreadyInvested) {
            throw new IllegalArgumentException("Investor already in this round: " + investor.getUsername());
        }

        String investmentNumber = generateInvestmentNumber(round.getShop(), round);

        Investment investment = Investment.builder()
            .investmentNumber(investmentNumber)
            .investor(investor)
            .shop(round.getShop())
            .investmentRound(round)
            .amount(investorInput.getAmount())
            .fixedShares(investorInput.getFixedShares())
            .investmentDate(LocalDateTime.now())
            .status(Investment.InvestmentStatus.ACTIVE)
            .totalProfitEarned(BigDecimal.ZERO)
            .totalWithdrawn(BigDecimal.ZERO)
            .notes(investorInput.getNotes())
            .build();

        investmentRepository.save(investment);
        round.getInvestments().add(investment);

        auditService.logFinancialTransaction(
            round.getShop(),
            addedBy,
            addedBy,
            AuditLog.ActionType.INVESTMENT_CREATED,
            investment.getId(),
            String.format("Investor %s added to round %s - Amount: %s",
                investor.getUsername(), round.getRoundNumber(), investorInput.getAmount()),
            true
        );

        log.info("Added investor {} to round {}", investor.getUsername(), roundId);
        return mapToResponse(round);
    }

    /**
     * Generate round number: ROUND-{SHOP_CODE}-{YEAR}-Q{QUARTER}-{SEQUENCE}
     */
    private String generateRoundNumber(Shop shop) {
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int quarter = (now.getMonthValue() - 1) / 3 + 1;

        String prefix = String.format("ROUND-%s-%d-Q%d-", shop.getName().toUpperCase().substring(0, 3), year, quarter);

        // Find next sequence number from database (thread-safe)
        long count = investmentRoundRepository.countByShopId(shop.getId());

        return prefix + String.format("%03d", count + 1);
    }

    /**
     * Generate investment number: INV-{ROUND_NUMBER}-{SEQUENCE}
     */
    private String generateInvestmentNumber(Shop shop, InvestmentRound round) {
        // Query database for actual count (thread-safe)
        long count = investmentRepository.countByInvestmentRoundId(round.getId());
        int sequence = (int) count + 1;
        return String.format("INV-%s-%03d", round.getRoundNumber(), sequence);
    }

    /**
     * Map InvestmentRound entity to response DTO.
     */
    private InvestmentRoundResponse mapToResponse(InvestmentRound round) {
        BigDecimal totalAmount = round.getInvestments().stream()
            .map(Investment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<InvestmentRoundResponse.InvestmentSummary> investmentSummaries = round.getInvestments().stream()
            .map(inv -> InvestmentRoundResponse.InvestmentSummary.builder()
                .id(inv.getId())
                .investmentNumber(inv.getInvestmentNumber())
                .investorId(inv.getInvestor().getId())
                .investorName(inv.getInvestor().getFullName())
                .amount(inv.getAmount())
                .fixedShares(inv.getFixedShares())
                .investmentDate(inv.getInvestmentDate())
                .status(inv.getStatus())
                .statusDisplay(inv.getStatus().name())
                .totalProfitEarned(inv.getTotalProfitEarned())
                .totalWithdrawn(inv.getTotalWithdrawn())
                .availableBalance(inv.getTotalProfitEarned().subtract(inv.getTotalWithdrawn()))
                .notes(inv.getNotes())
                .build())
            .collect(Collectors.toList());

        InvestmentRoundResponse.InvestmentRoundResponseBuilder responseBuilder = InvestmentRoundResponse.builder()
            .id(round.getId())
            .roundNumber(round.getRoundNumber())
            .shopId(round.getShop().getId())
            .shopName(round.getShop().getName())
            .investmentType(round.getInvestmentType())
            .investmentTypeDisplay(round.getInvestmentType().name())
            .profitSharingModel(round.getProfitSharingModel())
            .profitSharingModelDisplay(round.getProfitSharingModel().name())
            .maturityDate(round.getMaturityDate())
            .status(round.getStatus())
            .statusDisplay(round.getStatus().name())
            .totalAmount(totalAmount)
            .totalInvestors(round.getInvestments().size())
            .notes(round.getNotes())
            .investments(investmentSummaries)
            .createdAt(round.getCreatedAt())
            .updatedAt(round.getUpdatedAt());

        // Add tier configuration if present
        if (round.getTierConfiguration() != null) {
            responseBuilder.tierConfiguration(InvestmentRoundCreateRequest.TierConfigurationDTO.builder()
                .tier1Threshold(round.getTierConfiguration().getTier1Threshold())
                .tier1Multiplier(round.getTierConfiguration().getTier1Multiplier())
                .tier2Threshold(round.getTierConfiguration().getTier2Threshold())
                .tier2Multiplier(round.getTierConfiguration().getTier2Multiplier())
                .tier3Threshold(round.getTierConfiguration().getTier3Threshold())
                .tier3Multiplier(round.getTierConfiguration().getTier3Multiplier())
                .build());
        }

        // Add time weighting rules if present
        if (round.getTimeWeightingRules() != null) {
            responseBuilder.timeWeightingRules(InvestmentRoundCreateRequest.TimeWeightingRulesDTO.builder()
                .baseYears(round.getTimeWeightingRules().getBaseYears())
                .baseMultiplier(round.getTimeWeightingRules().getBaseMultiplier())
                .year2Threshold(round.getTimeWeightingRules().getYear2Threshold())
                .year2Multiplier(round.getTimeWeightingRules().getYear2Multiplier())
                .year3Threshold(round.getTimeWeightingRules().getYear3Threshold())
                .year3Multiplier(round.getTimeWeightingRules().getYear3Multiplier())
                .maxMultiplier(round.getTimeWeightingRules().getMaxMultiplier())
                .build());
        }

        return responseBuilder.build();
    }
}
