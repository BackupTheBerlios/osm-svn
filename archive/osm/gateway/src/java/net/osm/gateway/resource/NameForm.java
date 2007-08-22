
package net.osm.gateway.resource;

import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.avalon.framework.context.Context;

import org.omg.CORBA.ORB;

import net.osm.gateway.GatewayServlet;
import net.osm.gateway.GatewayRuntimeException;
import net.osm.session.HomeAdapter;
import net.osm.session.resource.AbstractResourceAdapter;
import net.osm.adapter.Adapter;

public class NameForm extends ActionForm 
{
    
    private String m_key = null;
    private Context m_context = null;
    private Adapter m_adapter = null;
    private String m_name = null;
    private String m_url = null;

   /**
    * Return the name.
    */
    public String getKey() 
    {
        return m_key;
    }
    
   /**
    * Set the name.
    * @param key The key.
    */
    public void setKey(String key)
    {    
        m_key = key;   
    }

   /**
    * Return the url.
    */
    public String getUrl() 
    {
        return m_url;
    }
    
   /**
    * Set the url.
    * @param url The url value
    */
    public void setUrl(String path )
    {    
        m_url = path;   
    }

   /**
    * Return the name.
    */
    public String getName() 
    {
        return m_name;
    }
    
   /**
    * Set the name.
    * @param name The new name
    */
    public void setName(String name)
    {    
        m_name = name;   
    }

   /**
    * Return the action context.
    * @return name context value
    */
    public Context getContext()
    {    
        return m_context;
    }

    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) 
    {
        request.getSession().removeAttribute( m_key );
        m_context = null;
        m_key = null;
        m_name = null;
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
        if( m_url != null )
        {
            try
            {
                m_adapter = resolveAdapter( request, m_url );
            }
            catch( Throwable e )
            {
                System.out.println("RESOLUTION ERROR: " + e.toString() );
            }
        } 
        if( m_key == null )
        {
            errors.add("name", new ActionError("error.action.key"));
        }
        else
        {
            m_context = (Context) request.getSession().getAttribute( m_key );
            if( m_context == null ) 
            {
                errors.add("newname", new ActionError("error.context.missing"));
            }
        }
        if(( m_name == null ) || ( m_name.length() < 1 ) )
        {
            errors.add("newname", new ActionError("error.name.required"));
        }
        return errors;
    }

    public Adapter resolveAdapter( HttpServletRequest request, String path ) throws Exception
    {
        System.out.println("PATH: " + path );
        System.out.println("CONTEXT: " + request.getContextPath() );
        System.out.println("URI: " + request.getRequestURI() );
        System.out.println("REQUESTED: " + request.getRequestURL() );
        System.out.println("SERVLET: " + request.getServletPath() );
        URL r = new URL( request.getRequestURL().toString() );
        URL url = new URL( r, path );
        String context = request.getContextPath();
        System.out.println("URL: " + url );
        String file = url.getPath();
        System.out.println("FILE: " + file );
        String base = file.substring( context.length(), file.length() );
        if( base.startsWith("/") )
        {
            base = base.substring(1,base.length());
        }
        System.out.println("BASE: " + base );
        String query = url.getQuery();
        int resolve = query.indexOf("resolve=");
        int id = 0;
        String value = "";
        System.out.println("RESOLVE: " + resolve );
        if( resolve > -1 )
        {
            value = query.substring( resolve + 8, query.length() );
            int amp = value.indexOf("&");
            System.out.println("AMP: " + amp );
            if( amp > -1 )
            {
                value = value.substring(0, amp );
            }
            try
            {
                id = Integer.parseInt( value );
            }
            catch( Throwable e )
            {
                id = -1;
            }
        }
        System.out.println("QUERY: " + query );
        System.out.println("VALUE: " + value );
        System.out.println("ID: " + id );

        //doSomePlayingAround( request );

        return null;
    }

    private void doSomePlayingAround( HttpServletRequest request )
    {
        //ORB orb = (ORB) request.getSession().getServletContext().getAttribute("net.osm.gateway.orb");
        //GatewayServlet gateway = (GatewayServlet) request.getSession().getServletContext().getAttribute("net.osm.session");
        //gateway.doTest( "corbaloc:iiop:1.2@home.osm.net:2056/GATEWAY" );
    }
}
