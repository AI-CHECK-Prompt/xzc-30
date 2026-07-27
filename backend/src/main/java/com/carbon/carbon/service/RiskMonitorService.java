package com.carbon.carbon.service;

import com.carbon.carbon.entity.CarbonOrder;
import com.carbon.carbon.entity.Company;
import com.carbon.carbon.entity.RiskAlert;
import com.carbon.carbon.repository.CarbonOrderRepository;
import com.carbon.carbon.repository.CompanyRepository;
import com.carbon.carbon.repository.RiskAlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class RiskMonitorService {

    @Autowired
    private RiskAlertRepository riskAlertRepository;

    @Autowired
    private CarbonOrderRepository carbonOrderRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private static final BigDecimal LARGE_TRANSACTION_THRESHOLD = new BigDecimal("10000");
    private static final BigDecimal POSITION_CONCENTRATION_THRESHOLD = new BigDecimal("100000");
    private static final int TRADING_FREQUENCY_THRESHOLD = 10;
    private static final int TRADING_FREQUENCY_WINDOW_HOURS = 1;

    public void checkLargeTransaction(CarbonOrder order) {
        if (order.getQuantity().compareTo(LARGE_TRANSACTION_THRESHOLD) > 0) {
            createAlert(
                "LARGE_TRANSACTION",
                "大额交易预警",
                order.getSeller(),
                "HIGH",
                "交易数量 " + order + " 超过大额交易阈值",
                LARGE_TRANSACTION_THRESHOLD,
                order.getQuantity()
            );
        }
    }

    public void checkRelatedPartyTransaction(CarbonOrder order) {
        if (order.getSeller() != null && order.getBuyer() != null) {
            if (isRelatedParty(order.getSeller(), order.getBuyer())) {
                createAlert(
                    "RELATED_PARTY",
                    "关联交易监测",
                    order.getSeller(),
                    "MEDIUM",
                    "检测到关联交易: " + order.getSeller().getName() + " -> " + order.getBuyer().getName(),
                    BigDecimal.ZERO,
                    order.getQuantity()
                );
            }
        }
    }

    public void checkPositionConcentration(Long companyId) {
        companyRepository.findById(companyId).ifPresent(company -> {
            BigDecimal totalHeld = calculateTotalPosition(companyId);
            if (totalHeld.compareTo(POSITION_CONCENTRATION_THRESHOLD) > 0) {
                createAlert(
                    "POSITION_CONCENTRATION",
                    "持仓集中度预警",
                    company,
                    "MEDIUM",
                    "企业持仓量 " + totalHeld + " 超过持仓集中度阈值",
                    POSITION_CONCENTRATION_THRESHOLD,
                    totalHeld
                );
            }
        });
    }

    public void checkAbnormalTradingBehavior(Long companyId) {
        LocalDateTime windowStart = LocalDateTime.now().minusHours(TRADING_FREQUENCY_WINDOW_HOURS);
        List<CarbonOrder> recentOrders = carbonOrderRepository.findByCompanyIdAndOrderTimeAfter(companyId, windowStart);

        if (recentOrders.size() > TRADING_FREQUENCY_THRESHOLD) {
            companyRepository.findById(companyId).ifPresent(company ->
                createAlert(
                    "ABNORMAL_TRADING",
                    "异常交易行为识别",
                    company,
                    "HIGH",
                    "企业在短时间内交易次数过于频繁",
                    new BigDecimal(TRADING_FREQUENCY_THRESHOLD),
                    new BigDecimal(recentOrders.size())
                )
            );
        }
    }

    public void checkComplianceGap(Long companyId, Integer year) {
        companyRepository.findById(companyId).ifPresent(company -> {
            if (company.getTotalEmissions() != null && company.getQuotaHeld() != null) {
                BigDecimal gap = company.getTotalEmissions().subtract(company.getQuotaHeld());
                if (gap.compareTo(BigDecimal.ZERO) > 0) {
                    createAlert(
                        "COMPLIANCE_GAP",
                        "履约缺口预警",
                        company,
                        "HIGH",
                        "企业履约缺口: " + gap,
                        BigDecimal.ZERO,
                        gap
                    );
                }
            }
        });
    }

    public void checkAllCompaniesComplianceGap(Integer year) {
        List<Company> companies = companyRepository.findAll();
        for (Company company : companies) {
            checkComplianceGap(company.getId(), year);
        }
    }

    public RiskAlert createAlert(String alertType, String alertName, Company company,
                                  String alertLevel, String content,
                                  BigDecimal threshold, BigDecimal current) {
        RiskAlert alert = new RiskAlert();
        alert.setAlertType(alertType);
        alert.setAlertName(alertName);
        alert.setCompany(company);
        alert.setAlertLevel(alertLevel);
        alert.setAlertContent(content);
        alert.setThresholdValue(threshold);
        alert.setCurrentValue(current);
        alert.setAlertStatus("PENDING");
        alert.setAlertTime(LocalDateTime.now());
        return riskAlertRepository.save(alert);
    }

    public RiskAlert processAlert(Long alertId, String processingResult) {
        return riskAlertRepository.findById(alertId).map(alert -> {
            alert.setAlertStatus("PROCESSED");
            alert.setProcessedTime(LocalDateTime.now());
            alert.setProcessingResult(processingResult);
            return riskAlertRepository.save(alert);
        }).orElseThrow(() -> new RuntimeException("预警记录不存在"));
    }

    public List<RiskAlert> findPendingAlerts() {
        return riskAlertRepository.findPendingAlerts();
    }

    public Long countPendingAlerts() {
        return riskAlertRepository.countPendingAlerts();
    }

    public List<RiskAlert> findByAlertType(String alertType) {
        return riskAlertRepository.findByAlertType(alertType);
    }

    public List<RiskAlert> findByAlertLevel(String alertLevel) {
        return riskAlertRepository.findByAlertLevel(alertLevel);
    }

    public List<RiskAlert> findAllAlerts() {
        return riskAlertRepository.findAll();
    }

    private boolean isRelatedParty(Company company1, Company company2) {
        if (company1.getRegion() != null && company1.getRegion().equals(company2.getRegion())) {
            return true;
        }
        return false;
    }

    private BigDecimal calculateTotalPosition(Long companyId) {
        return companyRepository.findById(companyId)
            .map(Company::getQuotaHeld)
            .orElse(BigDecimal.ZERO);
    }
}
