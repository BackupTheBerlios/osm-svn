/* 
 * Service.java
 */

package net.osm.shell;

import java.util.List;
import javax.swing.Action;


/**
 * The <code>Proxy</code> interface is exposed by services that 
 * need access to the Desktop.  Enabling access to the desktop 
 * should be considered as a privaliged action and should be 
 * considered carefully.
 *
 * @author Stephen McConnell
 */

public interface Proxy
{

   /**
    * Set the desktop that is handling the user interface.  This
    */
    public void setDesktop( Desktop desktop );

}
