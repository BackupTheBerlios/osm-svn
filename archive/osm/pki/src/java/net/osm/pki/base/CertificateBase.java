/*
 * CertificateBase.java
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
import java.security.cert.CertificateEncodingException;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.PKI.Certificate;
import org.omg.PKI.CertificateHelper;
import org.omg.PKI.EncodedData;
import org.omg.PKI.X509v3Certificate;
import org.omg.PKI.X509v2Certificate;
import org.omg.PKI.X509v1Certificate;
import org.omg.PKI.DEREncoding;


/**
 * Valuetype containing a certificate.
 */
public class CertificateBase extends Certificate
implements ValueFactory
{
    //===========================================================
    // constructors
    //===========================================================

   /** 
    *  Default null constructor
    */ 
    public CertificateBase(){}

   /**
    * CertificateBase constructor using the supplied initialization values.
    * <p>
    * @param crl_type = the type of CRL
    * @param data = encoding data
    */
    public CertificateBase ( int certificate_type, EncodedData data )
    {
        this.certificate_type = certificate_type;
        this.data = data;
    }

   /**
    * Creation ofg a new certificate given a supplied 
    * <code>java.security.cert.Certificate</code>.
    *
    * @param certificate the certificate to encode
    */
    public CertificateBase( java.security.cert.X509Certificate certificate ) throws CertificateEncodingException
    {
        data = new EncodedData( DEREncoding.value, certificate.getEncoded() );	
        int version = certificate.getVersion();
	  if( version == 3 )
	  {
            certificate_type = X509v3Certificate.value;
	  }
	  else if( version == 2 )
	  {
            certificate_type = X509v2Certificate.value;
	  }
	  else
	  {
            certificate_type = X509v1Certificate.value;
	  }
    }

    //===========================================================
    // internals
    //===========================================================

    public String toString()
    {
        return "Certificate:" + " type: " + certificate_type;
    }

    //===========================================================
    // ValueFactory
    //===========================================================
    
    public Serializable read_value( InputStream is ) {
        return is.read_value( new CertificateBase( ) );
    }
}

