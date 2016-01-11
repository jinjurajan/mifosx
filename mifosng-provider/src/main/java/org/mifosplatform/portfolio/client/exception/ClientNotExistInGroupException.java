package org.mifosplatform.portfolio.client.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;


public class ClientNotExistInGroupException extends AbstractPlatformResourceNotFoundException{

	public ClientNotExistInGroupException(final Long clientId, final Long groupId) {
		super("error.msg.client.not.active.exception","The client with clientId "+clientId+"  Is not Exist in Group with groupId "+groupId);
		
	}

	
}
