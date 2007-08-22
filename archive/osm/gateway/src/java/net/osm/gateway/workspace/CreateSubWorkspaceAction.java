
package net.osm.gateway.workspace;

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

import net.osm.session.workspace.WorkspaceAdapter;
import net.osm.gateway.resource.NameForm;

/**
 * Implementation of <strong>Action</strong> that sets the
 * name of an AbstractResource to a supplied value.
 */
public final class CreateSubWorkspaceAction extends Action 
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
        // get the workspace against which this action is being applied
        //

        WorkspaceAdapter adapter = null;
        try
        {
            adapter = (WorkspaceAdapter) data.getContext().get("workspace");
        }
        catch( Throwable e )
        {
            final String error = "Unable to resolve the target workspace.";
            throw new ServletException( error, e );
        }

        //
        // apply the sub-workspace creation request
        //

        WorkspaceAdapter workspace = null;
        try
        {
            workspace = adapter.createSubWorkspace( data.getName() );
        }
        catch( Throwable e )
        {
            final String error = "Unexpected exception creating new subworkspace.";
            throw new ServletException( error, e );
        }

        //
        // return
        //

        return new ActionForward( "/" + workspace.getBase() 
          + "?resolve=" + workspace.getIdentity() );
    }
}
