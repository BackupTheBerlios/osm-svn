package net.osm.pki.authority;

import java.io.Serializable;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.PKIAuthority.AuthorityProviderInfo;
import org.omg.PKI.CertificateRequestInfo;
import org.omg.PKI.CertificateRevocationInfo;
import org.omg.PKI.CertificateInfo;
import org.omg.PKI.CRLInfo;
import org.omg.PKI.KeyRecoveryInfo;
import org.omg.PKI.Certificate;

/**
 * Valuetype used to contain the information about a authority provider.
 */
public class AuthorityProviderInfoBase extends AuthorityProviderInfo 
implements ValueFactory
{
   /** 
    * Default null constructor
    */ 
    public AuthorityProviderInfoBase(){}

   /**
    * AuthorityProviderInfoBase constructor using the supplied initialization values.
    * <p>
    * @param standardDescription = standard description
    * @param standardVersion = version number of the specification
    * @param productDescription = product sescription
    * @param productVersion = product version
    * @param productVendor = product vendor
    * @param supportedCertificates = supported certificates
    * @param supportedCRLs = supported CRL info list
    * @param supportedCertRequestTypes = supported certificate request types;
    * @param supportedCertRevocationTypes = supported certificate recovation types;
    * @param supportedKeyRecoveryTypes = supported key revocery types;
    *
    * @param publicKeyURL = URL of a out-of-band public key (e.g. HTML page)
    * @param policyURL = URL of a policy description page
    * @param certificates = sequence of public certificates
    */
    public AuthorityProviderInfoBase ( 
	String standardDescription, String standardVersion, 
	String productDescription, String productVersion,
	String productVendor, CertificateInfo[] supportedCertificates,
	CRLInfo[] supportedCRLs, 
	CertificateRequestInfo[] supportedCertRequestTypes,
      CertificateRevocationInfo[] supportedCertRevocationTypes, 
	KeyRecoveryInfo[] supportedKeyRecoveryTypes,
	Certificate[] publicKeys,
	String publicKeyURL,
	String policyURL
    )
    {
        this.standardDescription = standardDescription;
        this.standardVersion = standardVersion;
        this.productDescription = productDescription;
        this.productVersion = productVersion;
        this.productVendor = productVendor;
        this.supportedCertificates = supportedCertificates;
        this.supportedCRLs = supportedCRLs;
        this.supportedCertRequestTypes = supportedCertRequestTypes;
        this.supportedCertRevocationTypes = supportedCertRevocationTypes;
        this.supportedKeyRecoveryTypes = supportedKeyRecoveryTypes;
        this.publicKeys = publicKeys;
        this.publicKeyURL = publicKeyURL;
        this.policyURL = policyURL;
    }

    //===========================================================
    // internals
    //===========================================================

    public String toString()
    {
        String result = "" 
	  	+ "\tAuthority Provider Information\n\n" 
		+ "\tclass: " + getClass().getName() + "\n"
		+ "\tdescription: " + standardDescription + "\n"
		+ "\tstandard version: " + standardVersion + "\n"
		+ "\tvendor: " + productVendor + "\n"	
		+ "\tproduct description: \n\n" + 
			packageString( productDescription, 4, 50 ) + "\n\n"
		+ "\tversion: " + productVersion + "\n";	

        result = result + "\tsupported certificates:\n";
        for( int i=0; i<supportedCertificates.length; i++ )
        {
		result = result + "\t\t" + supportedCertificates[i] + "\n";
        }

        result = result + "\tsupported CRL:\n";
        for( int i=0; i<supportedCRLs.length; i++ )
        {
		result = result + "\t\t" + supportedCRLs[i] + "\n";
        }

        result = result + "\tsupported certificate request types:\n";
        for( int i=0; i<supportedCertRequestTypes.length; i++ )
        {
		result = result + "\t\t" + supportedCertRequestTypes[i] + "\n";
        }

        result = result + "\tsupported certificate recovation types:\n";
        for( int i=0; i<supportedCertRevocationTypes.length; i++ )
        {
		result = result + "\t\t" + supportedCertRevocationTypes[i] + "\n";
        }

        result = result + "\tsupported certificate recovation types:\n";
        for( int i=0; i<supportedKeyRecoveryTypes.length; i++ )
        {
		result = result + "\t\t" + supportedKeyRecoveryTypes[i] + "\n";
        }

        return result;
    }

    private String packageString( String ss, int left, int width )
    {
	  String s = ss.toString().trim();
	  String string = "";
	  String padding = "";
	  for( int j=0; j<left; j++) 
	  {
		padding = padding + " "; 
	  }
        string = padding + s.substring( 0, width );
	  String remainder = s.substring( width );
	  if( remainder.length() > 0 ) if( remainder.length() > width )
	  {
		int k = 0;
		while( !remainder.startsWith( " " ) )
            {
		    string = string + remainder.substring(0,1);
		    remainder = remainder.substring(1);
	      }
		remainder.trim();
	      if( remainder.length() > width )
		{ 
	          string = string + "\n" + packageString( remainder, left, width );
	      }
	      else
	      {
	          string = string + "\n" + padding + remainder.trim();
	      }
	  }
	  else
	  {
	      string = string + "\n" + padding + remainder.trim();
        }
        return string;
    }


    //===========================================================
    // ValueFactory
    //===========================================================
    
    public Serializable read_value( InputStream is ) {
        return is.read_value( new AuthorityProviderInfoBase( ) );
    }
}

