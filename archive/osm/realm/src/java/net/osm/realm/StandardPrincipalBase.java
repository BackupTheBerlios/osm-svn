package net.osm.realm;

import java.io.Serializable;
import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.math.BigInteger;
import java.security.Principal;
import java.security.cert.CertPath;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;


/**
 * The <code>StandardPrincipal</code> interface contains a X509 certificate path
 * encoded as a byte array, together with convinience operations enableding access
 * to a principal, native X509 certificate path, issuing principal, and principal
 * serial number.
 */
public class StandardPrincipalBase extends StandardPrincipal
implements ValueFactory
{

    //=================================================================
    // state
    //=================================================================

   /**
    * The certificate factory from which the certificate chain will
    * be created.
    */ 
    private CertificateFactory factory;

   /**
    * The certificate chain in its native form.
    */
    private CertPath path;

   /**
    * The root certificate in the certificate chain.
    */
    private X509Certificate root;

   /**
    * The principal derived from the root certificate in the certificate 
    * chain.
    */
    private Principal principal;

   /**
    * The principal's certificate serial number.
    */
    private BigInteger serial;

   /**
    * The issuing principal.
    */
    private Principal issuer;

    private boolean trace = false;

    //=================================================================
    // constructor
    //=================================================================

    public StandardPrincipalBase( )
    {
    }

    public StandardPrincipalBase( PrincipalContext context )
    {
        this( context.getCertificatePath() );
    }

    public StandardPrincipalBase( CertPath path )
    {
	  this.path = path;
	  try
	  {
            this.encoded = path.getEncoded();
            this.principal = getPrincipal();
            this.serial = getSerialNumber();
            this.issuer = getIssuingPrincipal();
	  }
	  catch( Exception e )
	  {
		throw new RealmRuntimeException(
              "Unexpected exception while creating new StandardPrincipal.", e );
	  }
    }

    //=================================================================
    // StandardPrincipal
    //=================================================================

   /**
    * Return the principal's certificate path its its native form.
    */
    public CertPath getCertificatePath()
    {
        if( path != null ) return path;

	  //
	  // build the certificate path from its encoded form
	  //

	  try
	  {
	      if( trace ) System.out.println("StandardPrincipalBase, creating path");
  	      ByteArrayInputStream inputstream = new ByteArrayInputStream( getEncoded() );
	      if( factory == null ) factory = CertificateFactory.getInstance("X.509");
            path = factory.generateCertPath( inputstream );
            if( trace ) System.out.println("PrincipalServiceContextBase, path: " + 
  		  path.getCertificates().size());
        }
	  catch( Exception e )
	  {
	      String error = "Unable to internalize the certificate path.";
	      throw new RuntimeException( error, e );
	  }
        return path;
    }

   /**
    * Returns the principal its its native form.
    */
    public Principal getPrincipal()
    {
        if( principal != null ) return principal;
        if( root == null ) root = getRootCertificate();
        principal = root.getSubjectX500Principal();
	  return principal;
    }

   /**
    * Return the principal's certificate path as a byte array.
    */
    public byte[] getEncoded()
    {
        return super.encoded;
    }

   /**
    * Returns the Principal representing the issuer of the 
    * root certificate in the principal's certificate path.
    */
    public Principal getIssuingPrincipal()
    {
        if( issuer != null ) return issuer;
        if( root == null ) root = getRootCertificate();
        issuer = root.getIssuerX500Principal();
	  return issuer;
    }

   /**
    * Returns the serial number of the principal's root certificate.
    */
    public BigInteger getSerialNumber()
    {
        if( serial != null ) return serial;
        if( root == null ) root = getRootCertificate();
        serial = root.getSerialNumber();
	  return serial;
    }

    //===========================================================
    // internals
    //===========================================================

   /**
    * Returns the fist certificate in a supplied certificate path.
    * @param path a certificate path
    * @return X509Certificate the first certificate in the path
    */
    private X509Certificate getRootCertificate( )
    {
        if( root != null ) return root;
        if( path == null ) getCertificatePath();
        try
	  {
		Iterator certificates = path.getCertificates().iterator();
		while( certificates.hasNext() )
		{
		    Object object = certificates.next();
		    if( object instanceof X509Certificate )
		    {
		        root = (X509Certificate) object;
			  break;
		    }
		}
		
		if( root == null ) throw new RealmException( 
		  "Could not resolve root certificate (path size: " + 
		  path.getCertificates().size() + 
		  ")." );
        }
	  catch( Exception e )
	  {
		throw new RuntimeException(
		  "PrincipalManager, Unexpected exception while resolving first certificate.", e );
	  }
        return root;
    }

    //===========================================================
    // ValueFactory
    //===========================================================
    
    public Serializable read_value( InputStream is ) {
        return is.read_value( new StandardPrincipalBase( ) );
    }

    //===========================================================
    // Object
    //===========================================================

    public boolean equals( Object other )
    {
	  if( !( other instanceof StandardPrincipal )) return false;
	  StandardPrincipal p = (StandardPrincipal) other;
        return getCertificatePath().equals( p.getCertificatePath() );
    }
}

