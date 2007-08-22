/**
 */

package net.osm.realm;

import java.util.MissingResourceException;

import org.omg.CORBA.LocalObject;
import org.omg.CosPersistentState.Session;
import org.omg.CosPersistentState.StorageObject;
import org.omg.CosPersistentState.NotFound;

import org.apache.orb.util.ExceptionHelper;

/**
 * The <code>AccessManager</code> is an implementation of the 
 * <code>AccessController</code> service that provides support for resolution
 * of resource access decisions.  The initial reference to the singleton 
 * instance of this class is resolved through  
 * <code>orb.resolve_initial_references("AccessController")</code>.
 */
public class AccessManager extends LocalObject implements AccessController
{

    //=================================================================
    // static 
    //=================================================================

    private static boolean trace = false;

    private static Session session;

    public static void setSession( Session pss )
    {
        if( session != null ) throw new RuntimeException(
          "Bad invocation order - session already set.");
        session = pss;
    }

    //=================================================================
    // constructor 
    //=================================================================

   /**
    * Creation of a new <code>AccessManager</code> instance.
    */
    public AccessManager( )
    {
    }

    //=================================================================
    // AccessController 
    //=================================================================

   /**
    * The <code>accessDecision</code> method returns a true or false
    * access decision given a principal, object identifier and operation
    * name. The implementation delegates the access control decision 
    * to an access controller from the resource home based on the 
    * supplied persitent object identifier.
    *
    * @param principal the pricipal invoking the operation 
    * @param oid object identifier corresponding to the target of the request
    * @param operation the name of the operation to be invoked on the target by the principal
    * @osm.warning current implementation needs to be upgraded to be configurable but this
    *   is pending resolution of passing a configuration to the ORB initialization class
    * @osm.warning if not access policy can be located for a particular operation, the default
    *   access policy is to enable access - this should be declared as a configurable property
    *   once configuration is put in place
    */
    public boolean accessDecision( final StandardPrincipal principal, final byte[] oid, final String operation )
    {
        try
	  {
            //
	      // Make sure we have the PSS session so that we can resolve the 
	      // home instance.
            //

            if( session == null ) throw new MissingResourceException(
              "AccessManager, Session has not been set.", "org.omg.CosPersistentState.Session", "");

            //
            // get the home for the object identified by the OID and get the
            // <code>StandardPrincipal</code> and <code>AccessPolicy</code> from the 
	      // home
            //

            StorageObject store = (StorageObject) session.find_by_pid( oid );
            Accessible home = (Accessible) store.get_storage_home();
            StandardPrincipal owner = home.getOwner( oid );
            AccessPolicy policy = home.getAccessPolicy( oid );
            boolean ownerPolicyApplicable = false;
		if( owner.equals( principal ) ) ownerPolicyApplicable = true;

            //
            // currently we don't have persistent association of roles
	      // to principals - as such, the currently implentation only 
            // supports the implicit roles of "owner" and "user" where 
            // user is not the owner.
            //

            boolean permission = false;
            if( ownerPolicyApplicable )
            {
		    //
		    // apply the owner policy
	          //
		    permission = policy.accessible( new String[]{"owner", "user"}, operation );
		    if( trace ) System.out.println("Applying OWNER policy for " + operation + " is " + permission );
            }
	      else
	      {
		    //
		    // apply user policy
		    //

		    permission = policy.accessible( new String[]{"user"}, operation );
		    if( trace ) System.out.println("Applying USER policy for " + operation + " is " + permission );
	      }
            return permission;   

        }
	  catch( Exception e )
	  {
		if( trace ) ExceptionHelper.printException(
              "Unable to establish an access policy for " + operation, e, this );
            return true;
	  }
    }

   /**
    * Returns a string representation of this instance.
    * @return String containing the class name and session status
    */
    public String toString()
    {
        return "[ " + this.getClass().getName() + ", session: " + session +  "]";
    }
}
