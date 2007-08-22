
package net.osm.agent;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.BoxLayout;
import javax.swing.Action;
import javax.swing.border.EmptyBorder;

import org.omg.CosLifeCycle.NVP;
import org.omg.CommunityFramework.Criteria;
import org.omg.CommunityFramework.ResourceFactoryProblem;

import net.osm.shell.View;
import net.osm.shell.StaticFeature;
import net.osm.agent.util.SequenceIterator;
import net.osm.agent.WorkspaceAgent;
import net.osm.util.IconHelper;

/**
 * The <code>CriteriaAgent</code> class is a base class for all
 * agents hosting valuetypes based on the <code>org.omg.CommunityFramework.Criteria</code>
 * specification.  A criteria extends Control with the addition of named value pairs that
 * constitute factory arguments.
 */
public class CriteriaAgent extends ControlAgent
{

    //=========================================================================
    // static
    //=========================================================================

    private static String path = "net/osm/agent/image/control.gif";
    private static ImageIcon icon = IconHelper.loadIcon( path );

    //=========================================================================
    // state
    //=========================================================================

   /**
    * The CriteriaActionHandler that established the criteria and knows
    * about the execution context.
    * @see #setCallback
    */
    protected CriteriaActionHandler callback;

   /**
    * The primary Criteria valuetype.
    */
    protected Criteria criteria;

    private NVPAgent[] context;

   /**
    * Linked list of features exposed by this criteria.
    */
    private List features;


    //=========================================================================
    // constructor
    //=========================================================================

   /**
    * Default constructor.
    */
    public CriteriaAgent(  )
    {
        super();
    }

   /**
    * Set the criteria instance this agent is wrapping.
    */
    public void setPrimary( Object value ) 
    {
        super.setPrimary( value );
	  try
	  {
		this.criteria = (Criteria) value;
		putValue( Action.NAME, getName() );
        }
	  catch( Throwable t )
        {
		throw new RuntimeException(
			"Bad argument type in setPrimary.");
        }
    }

   /**
    * When criteria agents are established by another agent
    * we need to set the creating agent as a callback so that 
    * the actionPerformed method can delegate execution back to 
    * the initator.
    */
    public void setCallback( CriteriaActionHandler callback )
    {
	  if( this.callback != null ) throw new IllegalStateException( "attempt to reset callback");
        if( callback == null )
	  {
		final String error = "null callback argument";
	      throw new NullPointerException( error );
	  }
        this.callback = callback;
    }

   /**
    * Returns the primary Criteria instance.
    */
    public Criteria getCriteria()
    {
        return this.criteria;
    }

    //==========================================================
    // ActionListener
    //==========================================================

   /**
    * Invokes the criteria as an argument against the factory.
    */
    public void actionPerformed( ActionEvent event )
    {
        if( callback != null )
	  {
		try
		{
		    callback.handleCriteriaCallback( event, this );
		}
		catch( ResourceFactoryProblem e )
		{
		    if( getLogger().isErrorEnabled() ) 
		    {
			  String problem = e.problem.toString();
		        getLogger().error( problem );
		        System.out.println( problem );
		    }
		}
		catch( Throwable e )
		{
		    final String error = "unexpected error while invoking criteria callback handler";
		    throw new RuntimeException( error, e );
		}
	  }
        else
	  {
		final String error = "callback handler has set";
		throw new NullPointerException( error );
	  }
    }

    //=========================================================================
    // Agent
    //=========================================================================

   /**
    * Returns the label of the control.
    * @see #getLabel
    */
    public String getName()
    {
	  return super.getNote();
    }

    //=========================================================================
    // Entity implementation
    //=========================================================================

   /**
    * Returns a list of <code>Features</code> instances to be presented under 
    * the Properties dialog features panel.
    * @return List the list of <code>Feature</code> instances
    */
    public List getFeatures()
    {
        if( features != null ) return features;
	  features = super.getFeatures();
	  NVP[] values = criteria.values;
        for( int i=0; i<values.length; i++ )
	  {
	      NVPAgent nvp = new NVPAgent( values[i] );
		features.add( new StaticFeature( nvp.getName(), nvp.getValue() ));
	  }
	  return features;
    }
 }
