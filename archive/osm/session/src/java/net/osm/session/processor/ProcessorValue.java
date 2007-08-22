
package net.osm.session.processor;

import net.osm.session.task.Task;
import net.osm.session.task.TaskAdapter;
import net.osm.session.resource.AbstractResourceValue;
import org.omg.Session.task_state;

/**
 * An adapter providing EJB style access to a <code>Processor</code>.
 */
public class ProcessorValue extends AbstractResourceValue
implements ProcessorAdapter
{
    //=============================================================
    // static
    //=============================================================

    public static final String BASE_KEYWORD = "processor";

    /**
     * truncatable ids
     */
    static final String[] _ids_list =
    {
        "IDL:osm.net/session/processor/ProcessorAdapter:1.0",
    };

    //=============================================================
    // state
    //=============================================================

   /**
    * Internal reference to the processor backing the adapter.
    */
    private Processor m_processor;
    
    //=============================================================
    // constructors
    //=============================================================
    
   /**
    * Default constructor.
    */
    public ProcessorValue( ) 
    {
    }

   /**
    * Creation of a new DefaultProcessorAdapter.
    * @param primary the <code>Processor</code> object reference 
    *   backing the adapter
    */
    public ProcessorValue( Processor primary ) 
    {
	  super( primary );
    }

    //=============================================================
    // DefaultProcessorAdapter
    //=============================================================

    /**
     * Returns the primary object reference.
     * @return Processor the primary processor object reference
     */
    public Processor getPrimaryProcessor()
    {
        if( m_processor != null ) return m_processor;
        m_processor = ProcessorHelper.narrow( getPrimary() );
        return m_processor;
    }

    //=============================================================
    // ProcessorAdapter
    //=============================================================

    /**
     * Returns an adapter to the <code>Task</code> coordinting this procesor.
     * @return TaskAdapter the task coordinating the process or NULL if a 
     *   coordinator has not been assigned.
     */
    public TaskAdapter getTask()
    {
        Task task = getPrimaryProcessor().get_task();
        if( task != null ) return (TaskAdapter) task.get_adapter();
        return null;
    }

   /**
    * Returns the current process state.
    * @return int the process state value
    * @see org.omg.Session.task_state
    */
    public int getProcessState()
    {
        return getPrimaryProcessor().get_process_state().value();
    }

    //=============================================================
    // Startable
    //=============================================================

    public void start() throws Exception
    {
        getPrimaryProcessor().start();
    }
    
    public void stop() throws Exception
    {
        getPrimaryProcessor().stop();
    }
    
    public void suspend()
    {
        try
        {
            getPrimaryProcessor().suspend();
        }
        catch( Throwable e )
        {
            throw new ProcessorRuntimeException( "Cannot suspend process.", e );
        }
    }
    
    public void resume()
    {
        try
        {
            getPrimaryProcessor().start();
        }
        catch( Throwable e )
        {
            throw new ProcessorRuntimeException( "Cannot resume process.", e );
        }
    }

    //=============================================================
    // Adapter
    //=============================================================

    /**
     * Suppliments the supplied <code>StringBuffer</code> with a short 
     * description of the adapted object.
     * @param  buffer the string buffer to append the description to
     * @param  lead a <code>String</code> that is prepended to content
     */
    public void report( StringBuffer buffer, String lead )
    {
        super.report( buffer, lead );
        TaskAdapter task = this.getTask();
        if( task == null )
        {
            buffer.append( "\n" + lead + "Coordinator: " + task );
        }
        else
        {
            buffer.append( "\n" + lead + "Coordinator: " + getTask().getName());
        }
        buffer.append( "\n" + lead + "State: " 
          + task_state.from_int( getProcessState() ).toString() );
    }

    /**
     * Return a URL for the adapter.
     */
     public String getURL()
     {
         return "processor?resolve=" + getIdentity();
     }

   /**
    * Returns the static base keyword for the entity.
    */
    public String getBase()
    {
        return BASE_KEYWORD;
    }


    //=============================================================
    // ValueBase
    //=============================================================

   /**
    * Returns the truncatable ids identifying this valuetype.
    * @return String[] truncatable ids
    */
    public String [] _truncatable_ids()
    {
        return _ids_list;
    }

}
