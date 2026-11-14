package proiect_ispgl.proiect_ispgl.dto;

import java.math.BigDecimal;

public record AnimalFundingDto(
        String month,
        BigDecimal monthlyTarget,
        BigDecimal fundedAmount,
        BigDecimal remainingAmount,
        int percent,
        boolean targetConfigured) {
}
