/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package net.osm.merlin.kernel;

import org.apache.excalibur.containerkit.verifier.VerifyException;

/**
 * A service that provides support for the management of a set of component types
 * and factories.
 * @author <a href="mailto:mcconnell@apache.org">Stephen McConnell</a>
 * @version $Revision: 1.1 $ $Date: 2002/07/04 05:51:01 $
 */
public interface Container
{
 
   /**
    * Request the startup of the kernel.
    */
    void startup() throws Exception;

   /**
    * Request the shutdown of the kernel.
    */
    void shutdown();

}
