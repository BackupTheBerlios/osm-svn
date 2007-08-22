/**
 */

package net.osm.adapter;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;

/**
 * The <code>ServiceContext</code> interface is a context supplied to a 
 * provider that declares service key and path
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public interface ServiceContext extends Context
{
    public String SERVICE_KEY = "SERVICE_KEY";
    public String SERVICE_PATH = "SERVICE_PATH";

    //=================================================================
    // ChooserContext
    //=================================================================

   /**
    * Returns the base path.
    * @return <code>String[]</code> the set of parent identifiers.
    */
    public String[] getPath( ) throws ContextException;

   /**
    * Returns the service key.
    * @return <code>String</code> the name to assign to the factory
    * @exception ContextException if the name has not been set
    */
    public String getKey( ) throws ContextException;

}
