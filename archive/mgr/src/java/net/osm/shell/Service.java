/* 
 * Service.java
 */

package net.osm.shell;

import java.util.List;
import javax.swing.Action;


/**
 * The <code>Service</code> interface declares a component that requires 
 * human interface support from a <code>Shell</code>.
 *
 * @author Stephen McConnell
 */

public interface Service
{

   /**
    * Returns a list of Action instances to be installed as tool
    * menu items within the desktop for the lifetime of the service.
    */
    public List getTools( );

   /**
    * Returns an Action instances to be installed as 
    * menu items within the desktop preferences menu group.
    */
    public Action getPreferencesAction( );


}
