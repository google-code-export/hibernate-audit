/**
 * 
 */
package com.googlecode.hibernate.audit.extension.security;

import java.security.Principal;

public class DefaultSecurityInformationProvider implements
		SecurityInformationProvider {
	
	public Principal getPrincipal() {
		return null;
	}
}
