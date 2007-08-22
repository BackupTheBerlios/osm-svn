
package net.osm.gateway.resource;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import net.osm.session.HomeAdapter;
import net.osm.gateway.GatewayRuntimeException;
import net.osm.session.resource.AbstractResourceAdapter;
import net.osm.adapter.Adapter;


public class AbstractResourceForm extends ActionForm 
{
    
    private String m_base = null;
    private String m_identity = null;
    private String m_view = null;

   /**
    * Return the view.
    */
    public String getView() 
    {
        return m_view;
    }
    
   /**
    * Set the adapter.
    * @param adapter the current adapter
    */
    public void setView( String view )
    {    
        m_view = view;   
    }
    
   /**
    * Return the base page.
    */
    public String getBase() 
    {
        return m_base;
    }
    
   /**
    * Set the base page.
    * @param page the base page
    */
    public void setBase(String base)
    {    
        m_base = base;   
    }

   /**
    * Return the adapter identity.
    */
    public String getIdentity() 
    {
        return m_identity;
    }
    
   /**
    * Set the adapter identity value.
    * @param id the adapter identity
    */
    public void setIdentity(String id )
    {    
        m_identity = id;   
    }
    
    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) 
    {
        m_base = null;
        m_identity = null;
        m_view = null;
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
        ActionErrors errors = new ActionErrors();

        if(( m_identity == null ) || ( m_identity.length() < 1 ) )
        {
            errors.add("identity", new ActionError("error.identity.required"));
        }

        if(( m_base == null ) || ( m_base.length() < 1 ) )
        {
            errors.add("base", new ActionError("error.base.required"));
        }

        if(( m_view == null ) ) m_view = "default";

        return errors;
    }
}
