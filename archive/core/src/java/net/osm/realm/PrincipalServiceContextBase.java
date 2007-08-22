

package net.osm.realm;

import java.io.Serializable;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.security.cert.CertPath;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateEncodingException;

import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;


public class PrincipalServiceContextBase extends PrincipalServiceContext
implements ValueFactory
{

    private CertificateFactory factory;

    private CertPath path;

    private boolean trace = false;

    //===========================================================
    // constructors
    //===========================================================

    public PrincipalServiceContextBase( )
    {
	  if( trace ) System.out.println("PrincipalServiceContextBase, Constructor/0");
    }

    public PrincipalServiceContextBase( CertPath path ) throws IOException, CertificateEncodingException
    {
	  if( trace ) System.out.println("PrincipalServiceContextBase, Constructor/1");
        this.encoded = path.getEncoded();
    }

    //===========================================================
    // PrincipalServiceContext
    //===========================================================

    public byte[] getEncoded()
    {
        return encoded;
    }

    //===========================================================
    // ValueFactory
    //===========================================================
    
    public Serializable read_value( InputStream is ) {
        return is.read_value( new PrincipalServiceContextBase( ) );
    }

}

