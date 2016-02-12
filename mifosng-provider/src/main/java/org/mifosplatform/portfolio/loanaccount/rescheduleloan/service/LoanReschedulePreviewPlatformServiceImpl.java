/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.rescheduleloan.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

import org.mifosplatform.infrastructure.configuration.domain.ConfigurationDomainService;
import org.mifosplatform.organisation.holiday.domain.Holiday;
import org.mifosplatform.organisation.holiday.domain.HolidayRepository;
import org.mifosplatform.organisation.holiday.domain.HolidayStatusType;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.MoneyHelper;
import org.mifosplatform.organisation.workingdays.domain.WorkingDays;
import org.mifosplatform.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.mifosplatform.portfolio.calendar.domain.Calendar;
import org.mifosplatform.portfolio.calendar.domain.CalendarEntityType;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstance;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstanceRepository;
import org.mifosplatform.portfolio.floatingrates.data.FloatingRateDTO;
import org.mifosplatform.portfolio.floatingrates.data.FloatingRatePeriodData;
import org.mifosplatform.portfolio.floatingrates.exception.FloatingRateNotFoundException;
import org.mifosplatform.portfolio.floatingrates.service.FloatingRatesReadPlatformService;
import org.mifosplatform.portfolio.loanaccount.data.HolidayDetailDTO;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanRepaymentScheduleHistory;
import org.mifosplatform.portfolio.loanaccount.loanschedule.service.LoanScheduleHistoryWritePlatformService;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.domain.DefaultLoanReschedulerFactory;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleModel;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequest;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequestRepository;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.exception.LoanRescheduleRequestNotFoundException;
import org.mifosplatform.portfolio.loanproduct.domain.InterestMethod;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductMinimumRepaymentScheduleRelatedDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanReschedulePreviewPlatformServiceImpl implements LoanReschedulePreviewPlatformService {

    private final LoanRescheduleRequestRepository loanRescheduleRequestRepository;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final HolidayRepository holidayRepository;
    private final WorkingDaysRepositoryWrapper workingDaysRepository;
    private final LoanScheduleHistoryWritePlatformService loanScheduleHistoryWritePlatformService;
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final FloatingRatesReadPlatformService floatingRatesReadPlatformService;

    @Autowired
    public LoanReschedulePreviewPlatformServiceImpl(final LoanRescheduleRequestRepository loanRescheduleRequestRepository,
            final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository,
            final ConfigurationDomainService configurationDomainService, final HolidayRepository holidayRepository,
            final WorkingDaysRepositoryWrapper workingDaysRepository,
            final LoanScheduleHistoryWritePlatformService loanScheduleHistoryWritePlatformService,
            final CalendarInstanceRepository calendarInstanceRepository,
            final FloatingRatesReadPlatformService floatingRatesReadPlatformService) {
        this.loanRescheduleRequestRepository = loanRescheduleRequestRepository;
        this.applicationCurrencyRepository = applicationCurrencyRepository;
        this.configurationDomainService = configurationDomainService;
        this.holidayRepository = holidayRepository;
        this.workingDaysRepository = workingDaysRepository;
        this.loanScheduleHistoryWritePlatformService = loanScheduleHistoryWritePlatformService;
        this.calendarInstanceRepository = calendarInstanceRepository;
        this.floatingRatesReadPlatformService = floatingRatesReadPlatformService;
    }

    @Override
    public LoanRescheduleModel previewLoanReschedule(Long requestId) {
        final LoanRescheduleRequest loanRescheduleRequest = this.loanRescheduleRequestRepository.findOne(requestId);

        if (loanRescheduleRequest == null) { throw new LoanRescheduleRequestNotFoundException(requestId); }

        Loan loan = loanRescheduleRequest.getLoan();

        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(), loan
                .getDisbursementDate().toDate(), HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail = loan.getLoanRepaymentScheduleDetail();
        final MonetaryCurrency currency = loanProductRelatedDetail.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);

        final InterestMethod interestMethod = loan.getLoanRepaymentScheduleDetail().getInterestMethod();
        final RoundingMode roundingMode = MoneyHelper.getRoundingMode();
        final MathContext mathContext = new MathContext(8, roundingMode);
        List<LoanRepaymentScheduleHistory> oldPeriods = this.loanScheduleHistoryWritePlatformService.createLoanScheduleArchive(
                loan.getRepaymentScheduleInstallments(), loan, loanRescheduleRequest);
        HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays);
        CalendarInstance restCalendarInstance = null;
        CalendarInstance compoundingCalendarInstance = null;
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            restCalendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(loan.loanInterestRecalculationDetailId(),
                    CalendarEntityType.LOAN_RECALCULATION_REST_DETAIL.getValue());
            compoundingCalendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(
                    loan.loanInterestRecalculationDetailId(), CalendarEntityType.LOAN_RECALCULATION_COMPOUNDING_DETAIL.getValue());
        }
        final CalendarInstance loanCalendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(loan.getId(),
                CalendarEntityType.LOANS.getValue());
        Calendar loanCalendar = null;
        if (loanCalendarInstance != null) {
            loanCalendar = loanCalendarInstance.getCalendar();
        }
        final FloatingRateDTO floatingRateDTO = constructFloatingRateDTO(loan);
        
        boolean isMeetingSkipOnFirstDayOfMonth=configurationDomainService.isSkippingMeetingOnFirstDayOfMonthEnabled();
        
        boolean isClanderBelongsGroup=false;
        
        if(loan.getGroupId() !=null){isClanderBelongsGroup=true;}
        int numberOfDays=configurationDomainService.retrieveSkippingMeetingPeriod().intValue();
        LoanRescheduleModel loanRescheduleModel = new DefaultLoanReschedulerFactory().reschedule(mathContext, interestMethod,
                loanRescheduleRequest, applicationCurrency, holidayDetailDTO, restCalendarInstance, compoundingCalendarInstance,
                loanCalendar, floatingRateDTO,isMeetingSkipOnFirstDayOfMonth,isClanderBelongsGroup,numberOfDays);
        LoanRescheduleModel loanRescheduleModelWithOldPeriods = LoanRescheduleModel.createWithSchedulehistory(loanRescheduleModel,
                oldPeriods);
        return loanRescheduleModelWithOldPeriods;
    }

    private FloatingRateDTO constructFloatingRateDTO(final Loan loan) {
        FloatingRateDTO floatingRateDTO = null;
        if (loan.loanProduct().isLinkedToFloatingInterestRate()) {
            boolean isFloatingInterestRate = loan.getIsFloatingInterestRate();
            BigDecimal interestRateDiff = loan.getInterestRateDifferential();
            List<FloatingRatePeriodData> baseLendingRatePeriods = null;
            try{
            	baseLendingRatePeriods = this.floatingRatesReadPlatformService.retrieveBaseLendingRate()
            								.getRatePeriods();
            }catch(final FloatingRateNotFoundException ex){
            	// Do not do anything
            }
            floatingRateDTO = new FloatingRateDTO(isFloatingInterestRate, loan.getDisbursementDate(), interestRateDiff,
                    baseLendingRatePeriods);
        }
        return floatingRateDTO;
    }

}
