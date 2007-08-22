package net.osm.pki.repository;

import java.io.Serializable;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.PKIRepository.RepositoryProviderInfo;
import org.omg.PKI.CertificateInfo;
import org.omg.PKI.CRLInfo;

/**
 * Valuetype used to contain the information about a repository provider.
 */
public class RepositoryProviderInfoBase extends RepositoryProviderInfo 
implements ValueFactory
{
   /** 
    *  Default null constructor
    */ 
    public RepositoryProviderInfoBase(){}

   /**
    * RepositoryProviderInfoBase constructor using the supplied initialization values.
    * <p>
    * @param standardDescription = standard description
    * @param standardVersion = version number of the specification
    * @param productDescription = product sescription
    * @param productVersion = product version
    * @param productVendor = product vendor
    * @param supportedCertificates = supported certificates
    * @param supportedCRLs = supported CRL info list
    * @param supportedCrossCertificates = supported cross certificates;
    */
    public RepositoryProviderInfoBase ( 
	String standardDescription, String standardVersion, 
	String productDescription, String productVersion,
	String productVendor, CertificateInfo[] supportedCertificates,
	CRLInfo[] supportedCRLs, CertificateInfo[] supportedCrossCertificates )
    {
        this.standardDescription = standardDescription;
        this.standardVersion = standardVersion;
        this.productDescription = productDescription;
        this.productVersion = productVersion;
        this.productVendor = productVendor;
        this.supportedCertificates = supportedCertificates;
        this.supportedCRLs = supportedCRLs;
        this.supportedCrossCertificates = supportedCrossCertificates;
    }

    //===========================================================
    // internals
    //===========================================================

    public String toString()
    {
        String result = "" 
	  	+ "\tRepository Provider Information\n\n" 
		+ "\tclass: " + getClass().getName() + "\n"
		+ "\tdescription: " + standardDescription + "\n"
		+ "\tstandard version: " + standardVersion + "\n"
		+ "\tvendor: " + productVendor + "\n"	
		+ "\tproduct description: \n\n" + packageString( productDescription, 12, 40 ) + "\n\n"
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

        result = result + "\tsupported cross-certificates:\n";
        for( int i=0; i<supportedCrossCertificates.length; i++ )
        {
		result = result + "\t\t" + supportedCrossCertificates[i] + "\n";
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
        return is.read_value( new RepositoryProviderInfoBase( ) );
    }
}

