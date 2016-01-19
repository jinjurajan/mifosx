package org.mifosplatform.portfolio.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    public static final String ACTUAL_CHARGE = "ACTUAL_CHARGE";
    public static final String SERVICE_TAX = "SERVICE_TAX";
    public static final String EDUCATION_CESS = "EDUCATION_CESS";
    public static final String SH_EDUCATION_CESS = "SH_EDUCATION_CESS";
    public static final String SERVICE_TAX_PERCENTAGE = "SERVICE_TAX_PERCENTAGE";
    public static final String EDUCATION_CESS_PERCENTAGE = "EDUCATION_CESS_PERCENTAGE";
    public static final String SH_EDUCATION_CESS_PERCENTAGE = "SH_EDUCATION_CESS_PERCENTAGE";
    public static final int TAX_SCALE = 6;
    public static final int DEFAULT_SCALE = 2;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;
    public static final int ROUNDOFF_MULTIPLES = 1;

    public static Map<String, BigDecimal> splitCharge(final BigDecimal amount, final Map<String, BigDecimal> percentages) {
        Map<String, BigDecimal> map = new HashMap<>(3);
        if (amount != null) {
            double serviceTaxPercentage = percentages.get(SERVICE_TAX_PERCENTAGE).doubleValue();
            double income = amount.doubleValue();
            double servicetax = 0;
            double educationCess = 0;
            double shEducationCess = 0;
            if (serviceTaxPercentage > 0) {
                double educationCessPercentage = percentages.get(EDUCATION_CESS_PERCENTAGE).doubleValue();
                double shEducationCessPercentage = percentages.get(SH_EDUCATION_CESS_PERCENTAGE).doubleValue();
                double cent_percentage = Double.valueOf("100.0");
                double totalTaxInAmount = serviceTaxPercentage + (educationCessPercentage * serviceTaxPercentage / cent_percentage)
                        + (shEducationCessPercentage * serviceTaxPercentage / cent_percentage);
                double amountVal = amount.doubleValue();

                income = amountVal * cent_percentage / (totalTaxInAmount + cent_percentage);
                servicetax = amountVal * serviceTaxPercentage / (totalTaxInAmount + cent_percentage);
                educationCess = servicetax * educationCessPercentage / cent_percentage;
                shEducationCess = servicetax * shEducationCessPercentage / cent_percentage;
            }
            map.put(ACTUAL_CHARGE, BigDecimal.valueOf(income).setScale(TAX_SCALE, ROUNDING_MODE));
            map.put(SERVICE_TAX, BigDecimal.valueOf(servicetax).setScale(TAX_SCALE, ROUNDING_MODE));
            map.put(EDUCATION_CESS, BigDecimal.valueOf(educationCess).setScale(TAX_SCALE, ROUNDING_MODE));
            map.put(SH_EDUCATION_CESS, BigDecimal.valueOf(shEducationCess).setScale(TAX_SCALE, ROUNDING_MODE));
        }
        return map;
    }

    public static BigDecimal addTax(final BigDecimal amount, final Map<String, BigDecimal> percentages) {
        double serviceTaxPercentage = percentages.get(SERVICE_TAX_PERCENTAGE).doubleValue();
        BigDecimal totalAmount = amount;
        if (serviceTaxPercentage > 0 && amount != null) {
            double educationCessPercentage = percentages.get(EDUCATION_CESS_PERCENTAGE).doubleValue();
            double shEducationCessPercentage = percentages.get(SH_EDUCATION_CESS_PERCENTAGE).doubleValue();
            double cent_percentage = Double.valueOf("100.0");
            double totalTaxInAmount = serviceTaxPercentage + (educationCessPercentage * serviceTaxPercentage / cent_percentage)
                    + (shEducationCessPercentage * serviceTaxPercentage / cent_percentage);

            totalAmount = BigDecimal.valueOf(amount.doubleValue() * (totalTaxInAmount + cent_percentage) / cent_percentage).setScale(
                    TAX_SCALE, ROUNDING_MODE);
        }
        return totalAmount;
    }
}
