
package com.shengyi.fec.reimbursement.util;

import java.math.BigDecimal;

public class SubsidyCalculateUtil {

    public static BigDecimal calculateSubsidy(Integer cityLevel) {
        return switch (cityLevel) {
            case 1 -> new BigDecimal("200.00");
            case 2 -> new BigDecimal("150.00");
            case 3 -> new BigDecimal("100.00");
            default -> new BigDecimal("80.00");
        };
    }

    public static BigDecimal calculateTotalAmount(BigDecimal dailyAmount, int days) {
        return dailyAmount.multiply(BigDecimal.valueOf(days));
    }

    public static String getCityLevelDesc(Integer cityLevel) {
        return switch (cityLevel) {
            case 1 -> "一线城市";
            case 2 -> "二线城市";
            case 3 -> "三线城市";
            default -> "其他城市";
        };
    }
}
