ALTER TABLE `m_loan_charge`
	ADD COLUMN `amount_income_derived` DECIMAL(19,6) NULL DEFAULT NULL AFTER `amount`;