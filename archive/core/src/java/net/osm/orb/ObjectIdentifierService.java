/**
 */

package net.osm.orb;

import org.omg.CORBA.LocalObject;


/**
 * Service that provides access to the current thread object identifier.
 */
public class ObjectIdentifierService extends LocalObject
{

    //=================================================================
    // static
    //=================================================================
    
    private static InheritableThreadLocal holder = new InheritableThreadLocal();

    //=================================================================
    // implementation
    //=================================================================

   /**
    * Static operation that returns the current threads request object identifier
    * @return byte[] the current object identifier
    */
    public byte[] getObjectIdentifier()
    {
        return (byte[]) holder.get();
    }

   /**
    * Static operation to set the thead local object idenifier variable
    * @param array the current object identifier
    */
    public void setObjectIdentifier( byte[] array )
    {
        holder.set( array );
    }


}
