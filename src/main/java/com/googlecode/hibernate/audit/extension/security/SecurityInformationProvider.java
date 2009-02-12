package com.googlecode.hibernate.audit.extension.security;

import java.security.Principal;

public interface SecurityInformationProvider {
	Principal getPrincipal();
}
