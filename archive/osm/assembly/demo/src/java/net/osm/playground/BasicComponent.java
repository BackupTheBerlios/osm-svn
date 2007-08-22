

package net.osm.playground;

import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 * This is a minimal demonstration component that implements the 
 * <code>BasicService</code> interface and has no dependencies.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */
public class BasicComponent extends AbstractLogEnabled
implements BasicService
{

    //=======================================================================
    // BasicService
    //=======================================================================

    public void doPrimeObjective()
    {
        getLogger().info("hello from BasicComponent");
    }

}
