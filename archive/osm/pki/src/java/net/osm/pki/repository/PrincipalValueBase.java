package net.osm.pki.repository;

import java.io.Serializable;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.PKIRepository.RepositoryProviderInfo;
import org.omg.PKIRepository.PrincipalValue;
import org.omg.PKI.Certificate;
import org.omg.PKI.CertificatePair;
import org.omg.PKI.CRL;

public class PrincipalValueBase extends PrincipalValue 
implements ValueFactory
{

   /** 
    *  Default null constructor
    */ 
    public PrincipalValueBase(){}

   /**
    * PrincipalValueBase constructor using the supplied initialization values.
    * <p>
    * @param name = name of the principal
    * @param certificates = sequence of certificates held by the principal
    * @param authorities = sequence of CA certificates held by the principal
    * @param authorities = sequence of certificate private and public pairs
    * @param crl certificate revocation list
    * @param delta delta certificate revocation list
    * @param arl arl list
    */
    public PrincipalValueBase( String name, Certificate[] certificates, Certificate[] authorities, 
	CertificatePair[] pairs, CRL crl, CRL delta, CRL arl )
    {
        this.name = name;
        this.certificates = certificates;
        this.authorities = authorities;
        this.pairs = pairs;
        this.crl = crl;
        this.delta = delta;
        this.arl = arl;
    }

    //===========================================================
    // ValueFactory
    //===========================================================
    
    public Serializable read_value( InputStream is ) {
        return is.read_value( new PrincipalValueBase( ) );
    }

}

