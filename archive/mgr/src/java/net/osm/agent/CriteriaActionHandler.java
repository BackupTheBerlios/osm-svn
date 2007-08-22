
package net.osm.agent;

import java.awt.event.ActionEvent;
import org.omg.CommunityFramework.ResourceFactoryProblem;

/**
 * The <code>CriteriaActionHandler</code> interface defines the 
 * methods supported by an object that can handle an action
 * event applied to a criteria instance.
 *
 * @see CriteriaAgent
 */
public interface CriteriaActionHandler
{

   /**
    * Method invoked by a <code>CriteriaAgent</code> when the 
    * criteria has been activated via an <code>ActionEvent</code>.
    * @param event the <code>ActionEvent</code> received by the <code>CriteriaAgent</code>
    * @param criteria the <code>CriteriaAgent</code> that received the action event.
    */
    public void handleCriteriaCallback( ActionEvent event, CriteriaAgent criteria ) 
    throws ResourceFactoryProblem;


}
