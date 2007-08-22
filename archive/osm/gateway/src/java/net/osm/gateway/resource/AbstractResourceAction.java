
package net.osm.gateway.resource;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.util.MessageResources;

import net.osm.session.resource.AbstractResourceAdapter;
import net.osm.gateway.GatewayRuntimeException;
import net.osm.session.HomeAdapter;

/**
 * Implementation of <strong>Action</strong> that saves the top level
 * provider configuration that is common for all profiles.
 */

public abstract class AbstractResourceAction extends Action 
{

    protected final AbstractResourceAdapter getAdapter( HttpServletRequest request, 
      AbstractResourceForm data ) throws ServletException
    {
        try
        {
            return (AbstractResourceAdapter) getHome( request ).resolve( 
                data.getBase() + "=" + data.getIdentity() );
        }
        catch( Throwable e )
        {
            final String error = "Unexpected exception while resolving an adapter.";
            throw new GatewayRuntimeException( error, e );
        }
    }

    protected final HomeAdapter getHome( HttpServletRequest request )
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

    protected ActionForward getDefaultActionForward( AbstractResourceForm data )
    {
        return new ActionForward( "/" + data.getBase() 
          + "?resolve=" + data.getIdentity() 
          + "&view=default" );
    }
}
