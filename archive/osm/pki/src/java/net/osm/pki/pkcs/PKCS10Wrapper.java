/*
 * PKCS10.java
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
public class PKCS10Wrapper extends net.osm.pki.pkcs.PKCS10
implements ValueFactory
{

    //================================================================
    // state
    //================================================================

    private sun.security.pkcs.PKCS10 pkcs10;

    //================================================================
    // constructor
    //================================================================

   /**
    *  Default null constructor
    */ 
    public PKCS10Wrapper()
    {
        this.data = new EncodedData( DEREncoding.value, new byte[0] );
    }

   /**
    * PKCS10Wrapper valuetype constructor using a supplied byte array.
    * @param bytes a PKCS10 DER encoded byte array
    */
    public PKCS10Wrapper( byte[] bytes ) 
    throws IOException, SignatureException, NoSuchAlgorithmException
    {
	  this( new sun.security.pkcs.PKCS10( bytes ) );
    }

   /**
    * PKCS10Wrapper valuetype constructor using a supplied PKCS10 request
    * with DER encoding.
    * <p>
    * @param pkcs10 = PKCS10 implementation
    */
    public PKCS10Wrapper( sun.security.pkcs.PKCS10 pkcs10 )
    {
	  this.pkcs10 = pkcs10;
        this.cert_request_type = PKCS10CertificateRequest.value;
        this.data = new EncodedData( DEREncoding.value, pkcs10.getEncoded() );
    }

   /**
    * Creation of a new DER encoded PKCS10 request given a supplied Subject.
    * @param subject - an authenticated Java security subject
    * @exception NullPointerException if the supplied subject is null
    */
    public PKCS10Wrapper( final Subject subject ) throws NullPointerException
    {
	  if( subject == null ) throw new NullPointerException(
		"Null subject supplied to a pkcs10 request");

	  //
	  // Get the first private credential that is a X500PrivateCredential
	  // WARNING: what to do if we have more than one private credential ?
	  //

        Set set = subject.getPrivateCredentials();
	  Iterator iterator = set.iterator();
        X500PrivateCredential credential = null;
	  while( iterator.hasNext() )
	  {
		Object object = iterator.next();
		if( object instanceof X500PrivateCredential )
		{
		    credential = (X500PrivateCredential) object;
		    break;
		}
	  }

        if( credential == null )
        {
            String warning = "Shell unable to locate a X500PrivateCredential";
		throw new RuntimeException( warning );
	  }

	  try
	  {
	      PrivateKey privateKey = credential.getPrivateKey();		

            X509Certificate certificate = credential.getCertificate();
	      pkcs10 = new sun.security.pkcs.PKCS10(certificate.getPublicKey());
		
		String s = privateKey.getAlgorithm();
		String signatureForm = null;
		if( s.equalsIgnoreCase("DSA") || s.equalsIgnoreCase("DSS") )
		{
                signatureForm = "SHA1WithDSA";
		}
		else if( s.equalsIgnoreCase("RSA") )
		{
                signatureForm = "MD5WithRSA";
		}
		else
		{
		    String error = "Cannot derive signature algorithm";
		    throw new RuntimeException( error );
	      }

		Signature signature = Signature.getInstance( signatureForm );
            signature.initSign( privateKey );
		sun.security.x509.X500Name x500name = 
			new sun.security.x509.X500Name( certificate.getSubjectDN().toString() );
            sun.security.x509.X500Signer x500signer = 
                  new sun.security.x509.X500Signer( signature, x500name );
            pkcs10.encodeAndSign( x500signer );
	  }	
	  catch( Exception e )
	  {
		throw new RuntimeException( "Unexpected error while preparing PKCS10", e );
	  }

        this.cert_request_type = PKCS10CertificateRequest.value;
        this.data = new EncodedData( DEREncoding.value, pkcs10.getEncoded() );

    }

    //===========================================================
    // net.osm.pkcs.PKCS10 implementation
    //===========================================================

   /**
    * Returns the subject's public key.
    */
    public java.security.PublicKey getSubjectPublicKeyInfo()
    {
        return getPkcs10().getSubjectPublicKeyInfo();
    }

   /**
    * Returns the subject's name.
    */
    public net.osm.pki.base.X500Name getPrincipalName()
    {
        return new net.osm.pki.base.X500NameWrapper( getPkcs10().getSubjectName() );
    }

   /**
    * Returns the additional attributes requested.
    */
    public sun.security.pkcs.PKCS10Attributes getAttributes()
    {
        return getPkcs10().getAttributes();
    }

   /**
    * Returns the encoded and signed certificate request as a DER-encoded byte array.
    */
    public byte[] getEncoded()
    {
        return getPkcs10().getEncoded();
    }

    //===========================================================
    // ValueFactory
    //===========================================================
    
    public Serializable read_value( InputStream is ) {
        return is.read_value( new PKCS10Wrapper( ) );
    }

    //===========================================================
    // internal
    //===========================================================

    private sun.security.pkcs.PKCS10 getPkcs10()
    {
        if( pkcs10 == null )
	  {
		try
	      {
	          pkcs10 = new sun.security.pkcs.PKCS10( data.data );
	      }
	      catch( Exception e )
	      {
	          throw new RuntimeException("Failed to decode PKCS10 data", e );
	      }
	  }
	  return pkcs10;
    }

}

