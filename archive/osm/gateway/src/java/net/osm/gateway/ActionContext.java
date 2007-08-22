

package net.osm.gateway;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.DefaultContext;


/**
 * ActionContext is a context object used to hold references
 * that are relative to a particular action that may be passed
 * through several requests.
 */
public class ActionContext extends DefaultContext
{

    private String m_id;

    //=======================================================================
    // Constructor
    //=======================================================================

    public ActionContext()
    {
        this( null );
    }

    public ActionContext( Context parent )
    {
        super( parent );
        m_id = "" + System.identityHashCode( this );
    }

    //=======================================================================
    // ActionContext
    //=======================================================================

   /**
    * Clean up state members.
    */ 
    public String getId()
    {
        return m_id;
    }

}
