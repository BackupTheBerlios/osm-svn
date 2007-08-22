
package net.osm.gateway.task;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import net.osm.gateway.GatewayRuntimeException;
import net.osm.session.resource.AbstractResourceAdapter;
import net.osm.gateway.resource.AbstractResourceForm;


public class StateChangeForm extends AbstractResourceForm 
{

    private String m_state;

   /**
    * Return the state value.
    */
    public String getState() 
    {
        return m_state;
    }
    
   /**
    * Set the state value
    * @param state a <code>String</code> containing the integer value of the 
    *   desired task state.
    */
    public void setState(String state)
    {    
        m_state = state;   
    }
    
    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) 
    {
        m_state = null;
    }
    
    /**
     * Validate the properties that have been set from this HTTP request,
     * and return an <code>ActionErrors</code> object that encapsulates any
     * validation errors that have been found.  If no errors are found, return
     * an <code>ActionErrors</code> object with no recorded error messages.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public ActionErrors validate( ActionMapping mapping, HttpServletRequest request ) 
    {
        ActionErrors errors = super.validate( mapping, request );
        if(( m_state == null ) || ( m_state.length() < 1 ) )
        {
            errors.add("state", new ActionError("error.state.required"));
        }
        return errors;
    }
}
