
package net.osm.gateway.user;

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

import net.osm.adapter.Adapter;
import net.osm.factory.Argument;
import net.osm.factory.Parameter;
import net.osm.factory.FactoryAdapter;
import net.osm.session.resource.AbstractResourceAdapter;
import net.osm.session.processor.ProcessorAdapter;
import net.osm.session.task.TaskAdapter;
import net.osm.session.task.TaskRuntimeException;
import net.osm.session.user.PrincipalAdapter;

import net.osm.gateway.resource.NameForm;

/**
 * Implementation of <strong>Action</strong> that sets the
 * name of an AbstractResource to a supplied value.
 */
public final class CreateTaskAction extends Action 
{

    public ActionForward perform( 
      final ActionMapping mapping, final ActionForm form,
      final HttpServletRequest request, final HttpServletResponse response ) 
    throws IOException, ServletException 
    {
        System.out.println("CREATE TASK");
        TaskAdapter task = null;

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
        // get the principal
        //

        PrincipalAdapter principal = null;
        try
        {
            principal = (PrincipalAdapter) data.getContext().get("principal");
        }
        catch( Throwable e )
        {
            final String error = "Unable to resolve the principal.";
            throw new ServletException( error, e );
        }

        //
        // get the factory
        //

        FactoryAdapter factory = null;
        try
        {
            factory = (FactoryAdapter) data.getContext().get("factory");
        }
        catch( Throwable e )
        {
            final String error = "Unable to resolve the the target factory.";
            throw new ServletException( error, e );
        }

        try
        {

            Parameter[] params = factory.getParameters();
            if( params.length == 0 )
            {
                //
                // no parameters so we can go ahead with adapter creation
                //

                Adapter adapter = factory.create( new Argument[0] );
                task = principal.createTask( (ProcessorAdapter) adapter );
                task.setName( data.getName() );
            }
            else
            {
                //
                // we need to return to the client to get the parameters 
                // filled in before completing adapter creation
                //

                throw new TaskRuntimeException(
                  "Argument preparation not implemented.");
            }
        }
        catch( Throwable e )
        {
            final String error = "Unexpected exception while creating task.";
            throw new ServletException( error, e );
        }

        return new ActionForward( "/" + task.getBase() 
          + "?resolve=" + task.getIdentity() 
          + "&view=state" );
    }
}
