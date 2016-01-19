/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain.transactionprocessor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;

public interface LoanRepaymentScheduleTransactionProcessor {

   /* void handleTransaction(LoanTransaction loanTransaction, MonetaryCurrency currency, List<LoanRepaymentScheduleInstallment> installments,
            Set<LoanCharge> charges);
    */
    
	  void handleTransaction(LoanTransaction loanTransaction, MonetaryCurrency currency, List<LoanRepaymentScheduleInstallment> installments,
	            Set<LoanCharge> charges, boolean resetUnrecignizedPortion);

    ChangedTransactionDetail handleTransaction(LocalDate disbursementDate, List<LoanTransaction> repaymentsOrWaivers,
            MonetaryCurrency currency, List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments, Set<LoanCharge> charges);

  /*  void handleWriteOff(LoanTransaction loanTransaction, MonetaryCurrency loanCurrency,
            List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments);*/
    
    /*void handleWriteOff(LoanTransaction loanTransaction, MonetaryCurrency loanCurrency,
            List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments, Money[] receivableIncomes, Set<LoanCharge> charges, List<LoanTransaction> accrualTransactions);*/

    
    void handleWriteOff(LoanTransaction loanTransaction, MonetaryCurrency loanCurrency,
            List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments, Money[] receivableIncomes, Set<LoanCharge> charges, List<LoanTransaction> accrualTransactions);

    
    
    Money handleRepaymentSchedule(List<LoanTransaction> transactionsPostDisbursement, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments);

    /**
     * Used in interest recalculation to introduce new interest only
     * installment.
     */
    boolean isInterestFirstRepaymentScheduleTransactionProcessor();

    /*ChangedTransactionDetail populateDerivedFeildsWithoutReprocess(LocalDate disbursementDate,
            List<LoanTransaction> transactionsPostDisbursement, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments, Set<LoanCharge> charges);*/
    
    
    ChangedTransactionDetail populateDerivedFeildsWithoutReprocess(LocalDate disbursementDate,
            List<LoanTransaction> transactionsPostDisbursement, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments, Set<LoanCharge> charges, LocalDate recalculateChargesFrom,
            Map<String, BigDecimal> taxComponents);
    
    ChangedTransactionDetail handleTransaction(LocalDate disbursementDate, List<LoanTransaction> repaymentsOrWaivers,
            MonetaryCurrency currency, List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments, Set<LoanCharge> charges,
            LocalDate recalculateChargesFrom, Map<String, BigDecimal> taxComponents);


    void handleRefund(LoanTransaction loanTransaction, MonetaryCurrency currency, List<LoanRepaymentScheduleInstallment> installments,
            final Set<LoanCharge> charges);

}