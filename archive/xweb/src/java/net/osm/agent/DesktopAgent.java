
package net.osm.agent;

import org.omg.CORBA.ORB;
import org.omg.Session.Desktop;
import org.omg.Session.DesktopHelper;
import org.apache.avalon.framework.CascadingRuntimeException;

/**
 * The <code>DesktopAgent</code> is a type WorkspaceAgent attached to a 
 * PrincipalAgent that serves as a private repository for AbractResourceAgent
 * instances.  A DesktopAgent is associated to exactly one PrincipalAgent.
 * A PrincipalAgent maintains exactly one DesktopAgent.
 */
public class DesktopAgent extends WorkspaceAgent
{

    //=========================================================================
    // Members
    //=========================================================================

    protected Desktop desktop;


    //=========================================================================
    // Constructor
    //=========================================================================

    public DesktopAgent( )
    {
	  super();
    }

    public DesktopAgent( ORB orb, Desktop reference )
    {
	  super( orb, reference );
	  this.desktop = reference;
    }

    //=========================================================================
    // Operations
    //=========================================================================

   /**
    * Set the resource that is to be presented.
    */
    public void setReference( Object value ) 
    {
	  super.setReference( value );
        try
        {
	      this.desktop = DesktopHelper.narrow( (org.omg.CORBA.Object) value );
        }
        catch( Exception local )
        {
            throw new CascadingRuntimeException( "Bad primary object reference.", local );
        }
    }

    public UserAgent getOwner()
    {
	  try
	  {
            return new UserAgent( orb, desktop.belongs_to() );
        }
        catch( Throwable e )
        {
            return null;
        }
    }
}
