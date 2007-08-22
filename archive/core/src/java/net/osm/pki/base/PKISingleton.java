/*
 * PKISingleton.java
 *
 * Copyright 2000 OSM SARL All Rights Reserved.
 *
 * This software is the proprietary information of OSM SARL.
 * Use is subject to license terms.
 *
 * @author  Stephen McConnell
 * @version 1.0 31 JUL 2001
 */

package net.osm.pki.base;

import org.omg.CORBA.ORB;
import org.omg.PKI.CRLInfoHelper;
import org.omg.PKI.CRLHelper;
import org.omg.PKI.CertificateInfoHelper;
import org.omg.PKI.CertificateRequestInfoHelper;
import org.omg.PKI.ContinueHelper;
import org.omg.PKI.CertificateHelper;
import org.omg.PKI.CertificateRequestHelper;

/**
 * PKISingleton is a singleton class that provides a single static 
 * method through which valuetype and respective factories are registered 
 * with the current orb.
 */

public class PKISingleton
{
        
   /**
    * Static method to initialize value factories for each valuetype
    * with the ORB.  This method must be invoked during establishment
    * of any client or server using the PKI services.
    */
    public static void init( ORB current_orb ) 
    {
        final org.omg.CORBA_2_3.ORB orb = (org.omg.CORBA_2_3.ORB) current_orb;
        orb.register_value_factory( CertificateHelper.id(), new CertificateBase());
        orb.register_value_factory( CertificateInfoHelper.id(), new CertificateInfoBase());
        orb.register_value_factory( CertificateRequestHelper.id(), new CertificateRequestBase());
        orb.register_value_factory( CertificateRequestInfoHelper.id(), new CertificateRequestInfoBase());
        orb.register_value_factory( ContinueHelper.id(), new ContinueBase());
        orb.register_value_factory( CRLHelper.id(), new CRLBase());
        orb.register_value_factory( CRLInfoHelper.id(), new CRLInfoBase());
    }
}
