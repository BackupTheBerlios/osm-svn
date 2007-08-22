/*
 * CertificateRequestInfoBase.java
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
import org.omg.PKI.CertificateRequestInfo;
import org.omg.PKI.CertificateRequestInfoHelper;

/**
 * Valuetype used to contain the information about a repository provider.
 */
public class CertificateRequestInfoBase extends CertificateRequestInfo
implements ValueFactory
{
   /** 
    *  Default null constructor
    */
    public CertificateRequestInfoBase(){}

   /**
    * CertificateRequestInfoBase constructor using the supplied initialization values.
    * <p>
    * @param crl_type = the type of CRL
    * @param encoding_type = encoding of the CDL data
    */
    public CertificateRequestInfoBase( int cert_request_type, int encoding_type )
    {
        this.cert_request_type = cert_request_type;
        this.encoding_type = encoding_type;
    }


    //===========================================================
    // internals
    //===========================================================

    public String toString()
    {
        return "Certificate Request Info:" + " type: " + cert_request_type
		+ ", encoding: " + encoding_type;
    }

    //===========================================================
    // ValueFactory
    //===========================================================
    
    public Serializable read_value( InputStream is ) {
        return is.read_value( new CertificateRequestInfoBase( ) );
    }
}

