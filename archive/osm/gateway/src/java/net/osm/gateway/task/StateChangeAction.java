
package net.osm.gateway.task;

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

import net.osm.session.task.TaskAdapter;
import net.osm.session.task.TaskRuntimeException;
import net.osm.gateway.resource.AbstractResourceAction;

/**
 * Implementation of <strong>Action</strong> that sets the
 * name of an AbstractResource to a supplied value.
 */
public final class StateChangeAction extends AbstractResourceAction 
{
    public ActionForward perform( 
      final ActionMapping mapping, final ActionForm form,
      final HttpServletRequest request, final HttpServletResponse response ) 
    throws IOException, ServletException 
    {
        final StateChangeForm data = (StateChangeForm) form;
        final TaskAdapter adapter = (TaskAdapter) getAdapter( request, data );
        try
        {
            String state = data.getState();
            if( state.equals("start") )
            {
                adapter.start();
            }
            else if( state.equals("suspend") )
            {
                adapter.suspend();
            }
            else if( state.equals("resume") )
            {
                adapter.resume();
            }
            else if( state.equals("stop") )
            {
                adapter.stop();
            }
            else
            {
                final String error = "Invalid state property: " + state;
                throw new TaskRuntimeException( error );
            }
        }
        catch( Throwable e )
        {
        }
        finally
        {
            response.addHeader("Pragma", "no-cache");
            response.addHeader("Cashe-Control", "max-age=0, must-revalidate");
            return new ActionForward( "/" + adapter.getBase() 
              + "?resolve=" + adapter.getIdentity() 
              + "&view=state" );
        }
    }
}
