
package net.osm.agent;

import org.omg.CORBA.ORB;
import org.omg.Session.Desktop;
import org.omg.Session.DesktopHelper;

import net.osm.agent.WorkspaceAgent;

/**
 * The <code>DesktopAgent</code> is a type WorkspaceAgent attached to a 
 * PrincipalAgent that serves as a private repository for AbractResourceAgent
 * instances.  A DesktopAgent is associated to exactly one PrincipalAgent.
 * A PrincipalAgent maintains exactly one DesktopAgent.
 */
public class DesktopAgent extends WorkspaceAgent
{

    //=========================================================================
    // state
    //=========================================================================

    protected Desktop desktop;

    private UserAgent userAgent;

    //=========================================================================
    // Agent
    //=========================================================================

   /**
    * Set the resource that is to be presented.
    */
    public void setPrimary( Object value ) 
    {
	  super.setPrimary( value );
        try
        {
	      this.desktop = DesktopHelper.narrow( (org.omg.CORBA.Object) value );
        }
        catch( Exception local )
        {
            throw new RuntimeException( "Bad primary object reference.", local );
        }
    }

   /**
    * Returns the removable state of the Entity.
    * @return boolean true if this entity is removable.
    */
    public boolean removable()
    {
        return false;
    }

    //=========================================================================
    // DesktopAgent
    //=========================================================================

   /**
    * The <code>getType</code> method returns a human-friendly name of the entity.
    */
    public String getType( )
    {
	  return "Desktop";
    }

    public UserAgent getOwner()
    {
	  if( userAgent != null ) return userAgent;
	  try
	  {
		return (UserAgent) getResolver().resolve( desktop.belongs_to() );
        }
        catch( Throwable e )
        {
            throw new RuntimeException("DesktopAgent. Unable to resolve desktop owner.", e );
        }
    }
}
