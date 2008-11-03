package com.googlecode.hibernate.audit.test.write_collision;

import com.googlecode.hibernate.audit.test.write_collision.data.Root;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public interface ModificationStrategy
{
    void modify(Root root) throws Exception;
}
