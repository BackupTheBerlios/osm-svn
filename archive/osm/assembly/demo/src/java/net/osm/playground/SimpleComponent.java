

package net.osm.playground;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceManager;

/**
 * This is a minimal demonstration component that a dependency on 
 * BasicService and provides SimpleService.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class SimpleComponent extends AbstractLogEnabled implements Serviceable, SimpleService
{

    //=======================================================================
    // PrimaryService
    //=======================================================================

    public void doObjective()
    {
        getLogger().info("hello from SimpleComponent");
    }

    //=======================================================================
    // Serviceable
    //=======================================================================

    public void service( ServiceManager manager )
    {
        getLogger().debug("I'm being serviced!");
    }
}
