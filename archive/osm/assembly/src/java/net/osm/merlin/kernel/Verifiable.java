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
 * A interface declaring operations related to type and assembly validation.
 * @author <a href="mailto:mcconnell@apache.org">Stephen McConnell</a>
 * @version $Revision: 1.1 $ $Date: 2002/07/04 05:51:00 $
 */
public interface Verifiable
{
 
   /**
    * Method invoked by a parent container to request type level validation of 
    * the container.
    *
    * @exception ValidationException if a validation failure occurs
    */
    void verify() throws VerifyException;

}
