/**
 */

package net.osm.chooser;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;

import net.osm.adapter.ServiceContext;

/**
 * The <code>ChooserContext</code> interface is a context supplied to a 
 * <code>ChooserProvider</code> that declares a POA key.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public interface ChooserContext extends ServiceContext
{
    public String CHOOSER_KEY = ServiceContext.SERVICE_KEY;
    public String CHOOSER_PATH = ServiceContext.SERVICE_PATH;
}
