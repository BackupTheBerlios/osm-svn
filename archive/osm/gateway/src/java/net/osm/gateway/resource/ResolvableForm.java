
package net.osm.gateway.resource;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import net.osm.session.HomeAdapter;
import net.osm.gateway.GatewayRuntimeException;
import net.osm.adapter.Adapter;


public class ResolvableForm extends ActionForm 
{
    
    private String m_base = null;
    private String m_identity = null;
    private Adapter m_adapter = null;
    
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
    * Return an adapter relative to the base and identity values.
    * @return Adapter the adapter resolved from the base and identity values
    */
    public Adapter getAdapter()
    {
        return m_adapter;
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
        m_adapter = null;
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
        boolean ok = true;
        ActionErrors errors = new ActionErrors();

        if(( m_identity == null ) || ( m_identity.length() < 1 ) )
        {
            ok = false;
            errors.add("identity", new ActionError("error.identity.required"));
        }

        if(( m_base == null ) || ( m_base.length() < 1 ) )
        {
            ok = false;
            errors.add("base", new ActionError("error.base.required"));
        }

        if( ok )
        {
            try
            {
                m_adapter = (Adapter) getHome( request ).resolve( 
                  getBase() + "=" + getIdentity() );
            }
            catch( Throwable e )
            {
                final String error = "Unexpected exception while resolving an adapter.";
                throw new GatewayRuntimeException( error, e );
            }
        }
 
        return errors;
    }

   /**
    * Internal utility to get the home adapter.
    * @param request the current HTTP servlet request
    * @return HomeAdapter the gateway home adapter
    */
    private final HomeAdapter getHome( HttpServletRequest request )
    {
        try
        {
            return (HomeAdapter) request.getSession().getServletContext().getAttribute("net.osm.session");
        }
        catch( Throwable e )
        {
            throw new GatewayRuntimeException( "Unable to locate gateway adapter.", e );
        }
    }
}
