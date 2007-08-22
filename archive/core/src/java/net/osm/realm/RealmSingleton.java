/*
 * RealmSingleton.java
 *
 * Copyright 2000 OSM SARL All Rights Reserved.
 *
 * This software is the proprietary information of OSM SARL.
 * Use is subject to license terms.
 *
 * @author  Stephen McConnell
 * @version 1.0 31 JUL 2001
 */

package net.osm.realm;

import org.omg.CORBA.ORB;


/**
 * RealmSingleton is a singleton class that provides a single static 
 * method through which valuetype and respective factories are registered 
 * with the current orb.
 */

public class RealmSingleton
{
        
   /**
    * OSM service context identifier is used as the unique slot
    * allocated by the OMG.  This value is used to identify the 
    * slot containing a client principal.
    */
    public static final int SERVICE_CONTEXT_IDENTIFIER = 0x4F534D01;

   /**
    * Static method to initialize value factories for each valuetype
    * with the ORB.  This method must be invoked during establishment
    * of any client or server using the realm services.
    */
    
    public static void init( ORB current_orb ) 
    {
        final org.omg.CORBA_2_3.ORB orb = (org.omg.CORBA_2_3.ORB) current_orb;
        orb.register_value_factory( PrincipalServiceContextHelper.id(), new PrincipalServiceContextBase());
        orb.register_value_factory( StandardPrincipalHelper.id(), new StandardPrincipalBase());
    }
}
