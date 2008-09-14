package com.googlecode.hibernate.audit.delta;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public interface CollectionDelta extends MemberVariableDelta
{
    String getMemberEntityName();
    Collection<Serializable> getIds();
}
