
package net.osm.realm;

/**
 * The <code>Accessible</code> interface defines operations 
 * that enable a client to access an object's owner and applicable 
 * access policies.
 *
 * @author  Stephen McConnell
 * @version 1.0 20 JUN 2001
 */
public interface Accessible
{
   /**
    * Return the owner for a given persistent identifier.
    *
    * @return StandardPrincipal - the object's owner
    */
    public StandardPrincipal getOwner( byte[] oid );

   /**
    * Return the access policy for a given persistent identifier.
    *
    * @return AccessPolicy - the policy model to apply.
    */
    public AccessPolicy getAccessPolicy( byte[] oid );

}
