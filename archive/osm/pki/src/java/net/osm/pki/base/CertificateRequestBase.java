/*
 * CertificateRequestBase.java
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
import org.omg.PKI.CertificateRequest;
import org.omg.PKI.CertificateRequestHelper;
import org.omg.PKI.EncodedData;

/**
 * Certificate request.
 */
public class CertificateRequestBase extends CertificateRequest
implements ValueFactory
{
   /**
    *  Default null constructor
    */ 
    public CertificateRequestBase(){}

   /**
    * CertificateRequestBase constructor using the supplied initialization values.
    * <p>
    * @param crl_type = the type of CRL
    * @param encoding_type = encoding of the CDL data
    */
    public CertificateRequestBase( int cert_request_type, EncodedData data )
    {
        this.cert_request_type = cert_request_type;
        this.data = data;
    }

    //===========================================================
    // internals
    //===========================================================

    public String toString()
    {
        return "Certificate Request Info:" + " type: " + cert_request_type
		+ ", data: " + data;
    }

    //===========================================================
    // ValueFactory
    //===========================================================
    
    public Serializable read_value( InputStream is ) {
        return is.read_value( new CertificateRequestBase( ) );
    }
}

