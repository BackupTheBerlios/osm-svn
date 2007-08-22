
package net.osm.agent;

import java.util.LinkedList;
import java.util.Iterator;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import org.omg.CollaborationFramework.CollaborationModel;
import org.omg.CollaborationFramework.ProcessorCriteria;
import org.omg.CollaborationFramework.ProcessorModel;
import org.omg.CosLifeCycle.NVP;

import net.osm.agent.util.SequenceIterator;
import net.osm.shell.View;

/**
 * The <code>ProcessorCriteriaAgent</code> class is a base class for all
 * agents hosting valuetypes based on the <code>org.omg.CommunityFramework.ProcessorCriteria</code>
 * specification.  A processor criteria extends Control with the addition of named value pairs that
 * constitute factory arguments.
 */
public class ProcessorCriteriaAgent extends CriteriaAgent
{

   /**
    * The object reference to the ProcessorCriteria that this agents 
    * represents.
    */
    protected ProcessorCriteria criteria;

    private ProcessorModelAgent model;

   /**
    * Linked list of views including supertype views.
    */
    private LinkedList list;

   /**
    * Internal reference to the criteria view.
    */
    //private TabbedView view;

    //=========================================================================
    // Constructor
    //=========================================================================

   /**
    * Default constructor.
    */
    public ProcessorCriteriaAgent(  )
    {
        super();
    }

    //=========================================================================
    // Atrribute setters
    //=========================================================================

   /**
    * Set the criteria instance this agent is wrapping.
    */
    public void setPrimary( Object value ) 
    {
        super.setPrimary( value );
	  try
	  {
		this.criteria = (ProcessorCriteria) value;
        }
	  catch( Throwable t )
        {
		throw new RuntimeException(
			"Bad argument type in setPrimary.");
        }
    }

    //=========================================================================
    // Getter methods
    //=========================================================================

   /**
    * Returns an processor model from the processor criteria.
    */

    public ProcessorModelAgent getModel()
    {
	  if( model != null ) return model;
	  try
	  {
            model = (ProcessorModelAgent) getResolver().resolve( criteria.model );
	      return model;
	  }
	  catch( Exception e )
	  {
	      final String error = "unable to resolve processor model";
	      throw new RuntimeException( error, e );
	  }
    }

    //=========================================================================
    // Entity implementation
    //=========================================================================

/*
    public View getView()
    {
        if( view == null )
	  {
		TabbedView panel = new TabbedView( "Processor Criteria", this );
		view = panel;
	  }
	  return view;
    }
*/
   /**
    * The <code>getStandardViews</code> operation returns a sequence of panels 
    * representing different views of the content and/or associations maintained by
    * the agent.
    */
/*
    public LinkedList getStandardViews()
    {
	  if( list == null )
	  {
	      //list = super.getStandardViews();
		//list.add( getModel().getUsageView() );
            return getModel().getStandardViews();
        }
	  return list;
    }
*/
}
