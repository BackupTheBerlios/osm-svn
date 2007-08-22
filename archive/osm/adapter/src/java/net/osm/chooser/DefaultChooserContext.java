/**
 */

package net.osm.chooser;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.CascadingRuntimeException;

import net.osm.adapter.ServiceContext;
import net.osm.adapter.DefaultServiceContext;


/**
 * The <code>DefaultChooserContext</code> class is Context class that 
 * holds the set of information required to establish a chooser.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class DefaultChooserContext extends DefaultServiceContext
implements ChooserContext
{

    //=================================================================
    // constructor
    //=================================================================
    
   /**
    * Creation of a new DefaultChooserContext.
    * @param path parent identifier
    * @param key the name to assign to the factory POA
    * @param parent the parent context
    */
    public DefaultChooserContext( String[] path, String key, Context parent )
    {
        super( path, key, parent );
    }
}
