/**
 */

package net.osm.adapter;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.CascadingRuntimeException;

/**
 * The <code>DefaultServiceContext</code> class is Context class that 
 * holds the set of information required to establish a service.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class DefaultServiceContext extends DefaultContext
implements ServiceContext
{

    //=================================================================
    // constructor
    //=================================================================
    
   /**
    * Creation of a new DefaultChooserContext.
    * @param path parent identifiers
    * @param key the name to assign to the factory POA
    * @param parent the parent context
    */
    public DefaultServiceContext( String[] path, String key, Context parent )
    {
        super( parent );
        put( ServiceContext.SERVICE_KEY, key );

        String[] base = new String[ path.length + 1 ];
        for( int i=0; i<path.length; i++ )
        {
            base[i] = path[i];
        }
        base[ path.length ] = key;

        put( ServiceContext.SERVICE_PATH, base );
    }

    //=================================================================
    // ServiceContext
    //=================================================================

   /**
    * Returns the base identifier.
    * @return <code>String[]</code> the set of parent identifiers.
    */
    public String[] getPath( ) throws ContextException
    {
        return (String[]) get( ServiceContext.SERVICE_PATH );
    }

   /**
    * Returns the factory POA key.
    * @return <code>String</code> the name to assign to the factory POA
    * @exception ContextException if the name has not been set
    */
    public String getKey( ) throws ContextException
    {    
        return (String) get( ServiceContext.SERVICE_KEY );
    }
}
