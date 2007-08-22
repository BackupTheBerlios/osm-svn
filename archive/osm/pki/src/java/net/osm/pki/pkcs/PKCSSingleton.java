/*
 * PKCSSingleton.java
 *
 * Copyright 2000 OSM SARL All Rights Reserved.
 *
 * This software is the proprietary information of OSM SARL.
 * Use is subject to license terms.
 *
 * @author  Stephen McConnell
 * @version 1.0 31 JUL 2001
 */

package net.osm.pki.pkcs;

import org.omg.CORBA.ORB;


/**
 * PKISingleton is a singleton class that provides a single static 
 * method through which valuetype and respective factories are registered 
 * with the current orb.
 */

public class PKCSSingleton
{
        
   /**
    * Static method to initialize value factories for each valuetype
    * with the ORB.  This method must be invoked during establishment
    * of any client or server using the PKI services.
    */
    public static void init( ORB current_orb ) 
    {
        final org.omg.CORBA_2_3.ORB orb = (org.omg.CORBA_2_3.ORB) current_orb;
        orb.register_value_factory( PKCS10Helper.id(), new PKCS10Wrapper());
        orb.register_value_factory( PKCS7Helper.id(), new PKCS7Base());
    }
}
