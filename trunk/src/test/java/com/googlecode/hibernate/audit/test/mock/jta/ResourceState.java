package com.googlecode.hibernate.audit.test.mock.jta;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public enum ResourceState
{
    NEW,           // not yet enlisted
    ENLISTED,      // enlisted
    ENDED,         // not associated
    SUSPENDED,     // suspended
    VOTE_READONLY, // voted read-only
    VOTE_OK,       // voted ok
    FORGOT         // resource manager has forgotten

}
