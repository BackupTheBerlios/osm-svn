

package net.osm.playground;

import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 * This is a minimal demonstration component that provides BasicService 
 * and has no dependencies
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class TerminalComponent extends AbstractLogEnabled
implements BasicService
{

    //=======================================================================
    // BasicService
    //=======================================================================

    public void doPrimeObjective()
    {
        getLogger().info("hello from TerminalComponent");
    }
}
