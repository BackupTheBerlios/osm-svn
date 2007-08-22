
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
import org.apache.avalon.framework.context.Context;

import net.osm.session.resource.AbstractResourceAdapter;

/**
 * Implementation of <strong>Action</strong> that sets the
 * name of an AbstractResource to a supplied value.
 */
public final class RenameAction extends Action 
{

    public ActionForward perform( 
      final ActionMapping mapping, final ActionForm form,
      final HttpServletRequest request, final HttpServletResponse response ) 
    throws IOException, ServletException 
    {

        NameForm data = null;
        try
        {
            data = (NameForm) form;
        }
        catch( Throwable e )
        {
            final String error = "Unable to resolve a NameForm from the supplied form.";
            throw new ServletException( error, e );
        }

        //
        // get the resource against which this action is being applied
        //

        AbstractResourceAdapter adapter = null;
        try
        {
            adapter = (AbstractResourceAdapter) data.getContext().get("resource");
        }
        catch( Throwable e )
        {
            final String error = "Unable to resolve the target resource.";
            throw new ServletException( error, e );
        }

        //
        // apply the rename request
        //

        try
        {
            adapter.setName( data.getName() );
        }
        catch( Throwable e )
        {
            final String error = "Unexpected exception while renaming the resource.";
            throw new ServletException( error, e );
        }

        //
        // return
        //

        return new ActionForward( "/" + adapter.getBase() 
          + "?resolve=" + adapter.getIdentity() );
    }
}
