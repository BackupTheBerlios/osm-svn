/**
 */

package net.osm.realm;

import java.io.IOException;
import java.security.cert.CertPath;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;

import net.osm.realm.PrincipalContext;

/**
 * PrincipalContextBase class.
 */
public class PrincipalContextBase extends LocalObject implements PrincipalContext
{

    //===========================================================
    // state
    //===========================================================

    private CertPath path;

    private Any any = ORB.init().create_any();

    private boolean trace = false;

   /**
    * Container of a X500Principal.
    */
    public PrincipalContextBase( CertPath path ) throws IOException
    {
	  if( trace ) System.out.println("PrincipalContextBase: " + path.getCertificates().size() );
        this.path = path;
        PrincipalContextHelper.insert( any, this );
	  if( trace ) System.out.println("PrincipalContextBase: done" );
    }

   /**
    * Return the certificate path for this principal.
    */
    public CertPath getCertificatePath()
    {
        return path;
    }

   /**
    * Returns an any containing this <code>PrincipalContextBase</code> instance.
    */
    public Any getAny()
    {
        return any;
    }
}
