ALTER TABLE `acc_gl_journal_entry`
	ADD COLUMN `group_Id` BIGINT(20) NULL AFTER `payment_details_id`,
	ADD COLUMN `client_Id` BIGINT(20) NULL AFTER `group_Id`,
	ADD CONSTRAINT `FK_acc_gl_journal_entry_m_group` FOREIGN KEY (`group_Id`) REFERENCES `m_group` (`id`),
	ADD CONSTRAINT `FK_acc_gl_journal_entry_m_client` FOREIGN KEY (`client_Id`) REFERENCES `m_client` (`id`);
