
package net.osm.shell;

import javax.swing.Action;

/**
 * The <code>Simulator</code> interface provides access to a 
 * view of a model that an entity simulates.
 *
 * @author  Stephen McConnell
 * @version 1.0 20 JUN 2001
 */
public interface Simulator
{

   /**
    * Returns an Entity representing the model associated to the primary entity.
    * @return Entity the entities model.
    */
    public Entity getModel();

}
