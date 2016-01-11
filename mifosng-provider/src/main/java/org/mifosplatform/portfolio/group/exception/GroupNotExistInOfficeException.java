package org.mifosplatform.portfolio.group.exception;
import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;
public class GroupNotExistInOfficeException extends AbstractPlatformDomainRuleException {

	public GroupNotExistInOfficeException( final Long groupId, final Long officeID) {
		super("error.msg.group.not.in.center","The group with groupId "+groupId+" Is not Not found in the Office with officeId "+officeID);
	}

	
}
