/*
 * PKCS7Base.java
 *
 * Copyright 2001-2002 OSM SARL All Rights Reserved.
 *
 * This software is the proprietary information of OSM SARL.
 * Use is subject to license terms.
 *
 * @author  Stephen McConnell
 * @version 1.0 04 JAN 2002
 */

package net.osm.pki.pkcs;

import java.io.Serializable;
import java.io.IOException;
import java.util.Set;
import java.util.Iterator;
import java.security.SignatureException;
import java.security.NoSuchAlgorithmException;

import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.PKI.CertificateRequest;
import org.omg.PKI.CertificateRequestHelper;
import org.omg.PKI.PKCS10CertificateRequest;
import org.omg.PKI.EncodedData;
import org.omg.PKI.DEREncoding;

import java.security.Signature;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500PrivateCredential;
import javax.security.auth.Subject;

/**
 * Certificate request.
 */
public class PKCS7Base extends net.osm.pki.pkcs.PKCS7
implements ValueFactory
{

    //================================================================
    // state
    //================================================================

    private sun.security.pkcs.PKCS7 pkcs7;

    //================================================================
    // constructor
    //================================================================

   /**
    *  Default null constructor
    */ 
    public PKCS7Base()
    {
        this.data = new EncodedData( DEREncoding.value, new byte[0] );
    }

   /**
    * PKCS7Base valuetype constructor using a supplied byte array.
    * @param bytes a PKCS7 DER encoded byte array
    */
    public PKCS7Base( byte[] bytes ) 
    {
        this.data = new EncodedData( DEREncoding.value, bytes );
    }

    //===========================================================
    // PKCS7
    //===========================================================

   /**
    * Returns the encoded and signed certificate request as a DER-encoded byte array.
    */
    public byte[] getEncoded()
    {
        return data.data;
    }

    public sun.security.pkcs.PKCS7 getPKCS7() throws Exception
    {
        if( pkcs7 != null ) return pkcs7;
	  pkcs7 = new sun.security.pkcs.PKCS7( getEncoded() );
	  return pkcs7;
    }

    public String toString()
    {
	  try
	  {
            return getPKCS7().toString();
	  }
	  catch( Throwable e )
	  {
		return this.getClass().getName();
	  }
    }

    //===========================================================
    // ValueFactory
    //===========================================================
    
    public Serializable read_value( InputStream is ) {
        return is.read_value( new PKCS7Base( ) );
    }
}

