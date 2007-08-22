package net.osm.pki.base;

import java.io.Serializable;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.PKI.CRL;
import org.omg.PKI.CRLHelper;
import org.omg.PKI.EncodedData;

/**
 * Valuetype used to contain the information about a repository provider.
 */
public class CRLBase extends CRL
implements ValueFactory
{
   /** 
    *  Default null constructor
    */ 
    public CRLBase(){}

   /**
    * CRLBase constructor using the supplied initialization values.
    * <p>
    * @param crl_type = the type of CRL
    * @param data = encoded CRL datastructure
    */
    public CRLBase ( int crl_type, EncodedData data )
    {
        this.crl_type = crl_type;
        this.data = data;
    }

    //===========================================================
    // internals
    //===========================================================

    public String toString()
    {
        return "CRL:" + " type: " + crl_type 
		+ ", data: " + data;
    }

    //===========================================================
    // ValueFactory
    //===========================================================
    
    public Serializable read_value( InputStream is ) {
        return is.read_value( new CRLBase( ) );
    }
}

