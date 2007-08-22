/*
 * CertificateInfoBase.java
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

import java.io.Serializable;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.PKI.CertificateInfo;
import org.omg.PKI.CertificateInfoHelper;

/**
 * Valuetype used to contain the information about a repository provider.
 */
public class CertificateInfoBase extends CertificateInfo
implements ValueFactory
{
   /** 
    *  Default null constructor
    */ 
    public CertificateInfoBase(){}

   /**
    * CertificateInfoBase constructor using the supplied initialization values.
    * <p>
    * @param crl_type = the type of CRL
    * @param encoding_type = encoding of the CDL data
    */
    public CertificateInfoBase ( int certificate_type, int encoding_type )
    {
        this.certificate_type = certificate_type;
        this.encoding_type = encoding_type;
    }

    //===========================================================
    // internals
    //===========================================================

    public String toString()
    {
        return "Certificate Info:" + " type: " + certificate_type 
		+ ", encoding: " + encoding_type;
    }

    //===========================================================
    // ValueFactory
    //===========================================================
    
    public Serializable read_value( InputStream is ) {
        return is.read_value( new CertificateInfoBase( ) );
    }
}

