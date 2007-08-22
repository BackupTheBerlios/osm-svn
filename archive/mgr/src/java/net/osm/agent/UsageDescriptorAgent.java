
package net.osm.agent;

import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.ORB;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CollaborationFramework.InputDescriptor;
import org.omg.CollaborationFramework.OutputDescriptor;
import org.omg.CollaborationFramework.UsageDescriptor;

import net.osm.entity.EntityService;
import net.osm.shell.StaticFeature;
import net.osm.shell.ScrollView;
import net.osm.shell.FeaturesPanel;
import net.osm.util.IconHelper;
import net.osm.util.ListEvent;
import net.osm.util.ListListener;

/**
 * The <code>UsageDescriptorAgent</code> class is a agent representing an instance of the
 * UsageDescriptor valuetype.
 */
public class UsageDescriptorAgent extends ValueAgent 
{
    //=========================================================================
    // static
    //=========================================================================

    private static final String inputPath = "net/osm/agent/image/input.gif";
    private static final String outputPath = "net/osm/agent/image/output.gif";
    private static final ImageIcon inputIcon = IconHelper.loadIcon( inputPath );
    private static final ImageIcon outputIcon = IconHelper.loadIcon( outputPath );

    //=========================================================================
    // state
    //=========================================================================

   /**
    * The entity resolver (used to create criteria types that may be included 
    * within the usage description).
    */
    EntityService resolver;

   /**
    * The UsageDescriptor that this agents represents.
    */
    protected UsageDescriptor usage;

    private CriteriaAgent criteria;

    private boolean isInput = true;

    private UsageAgent assignment;

    private List features;

    private List propertyPanelList;

    private ORB orb;

    //=========================================================================
    // Constructor
    //=========================================================================

    public UsageDescriptorAgent( Object object, ORB orb, EntityService resolver )
    {
	  super( object );
	  if( orb == null ) throw new NullPointerException("Null orb argument.");
	  if( resolver == null ) throw new NullPointerException("Null resolver argument.");
        try
        {
		this.orb = orb;
	      this.resolver = resolver;
            this.usage = (UsageDescriptor) object;
		this.isInput = ( object instanceof InputDescriptor );
        }
	  catch( Throwable t )
        {
		throw new RuntimeException(
		"UsageDescriptorAgent/setPrimary - bad type.");
        }
    }

    //=========================================================================
    // UsageDescriptorAgent
    //=========================================================================

   /**
    * Returns true if the usage agent represents an input usage instance.
    */
    public boolean isaInput( ) 
    {
        return this.isInput;
    }

   /**
    * Set the resource that is to be presented.
    */
    public void setPrimary( Object value ) 
    {
	  if( value == null ) throw new RuntimeException(
		"UsageDescriptorAgent/setPrimary - null value supplied.");
	  try
	  {
		this.usage = (UsageDescriptor) value;
        }
	  catch( Throwable t )
        {
		throw new RuntimeException(
		"UsageDescriptorAgent/setPrimary - bad type.");
        }
    }
 
    //=========================================================================
    // implementation
    //=========================================================================

    public Icon getDefaultIcon()
    {
	  if( isInput ) return inputIcon;
	  return outputIcon;
    }

   /**
    * Returns the name of the resource.
    */

    public String getTag()
    {
	  return usage.getTag();
    }

   /**
    * Returns the type code constraint from the usage descriptor.
    */
    public TypeCode getTypeCode()
    {
	  try
        {
            return usage.getType();
	  }
	  catch( Throwable e )
        {
            throw new RuntimeException( "UsageDescriptorAgent:getTypeCode", e );
        }
    }

   /**
    * Returns the IDL identifier of the type code constraint from the usage descriptor.
    */
    public String getIDLIdentifier()
    {
	  try
        {
            return usage.getID();
	  }
	  catch( Throwable e )
        {
            throw new RuntimeException( "UsageDescriptorAgent:getTypeCode", e );
        }
    }

   /**
    * Returns the required status of the usage descriptor - implementation returns false
    * for any instance of OutputDescriptor and returns the value decalred on InputDescriptor
    * instances.
    */
    public boolean getRequired()
    {
	  try
        {
            if( usage instanceof InputDescriptor ) return ((InputDescriptor) usage).required;
		return false;
	  }
	  catch( Throwable e )
        {
            throw new RuntimeException( "UsageDescriptorAgent:getRequired", e );
        }
    }

   /**
    * Returns the implied status of the usage descriptor - implementation returns false
    * for any instance of OutputDescriptor and returns the value decalred on InputDescriptor
    * instances.
    */
    public boolean getImplied()
    {
	  try
        {
            if( usage instanceof InputDescriptor ) return ((InputDescriptor) usage).implied;
		return false;
	  }
	  catch( Throwable e )
        {
            throw new RuntimeException( "UsageDescriptorAgent:getImplied", e );
        }
    }

   /**
    * Returns a possibly null criteria agent that can be used as an constructor
    * argument in the creation of a bound resource.
    */
    public CriteriaAgent getCriteria()
    {
        if( criteria != null ) return criteria;
        if( usage instanceof InputDescriptor ) if( ((InputDescriptor)usage).criteria != null )
	  {
		try
	      {
                criteria = (CriteriaAgent) resolver.resolve( ((InputDescriptor)usage).criteria );
		}
		catch( Exception e )
		{
		    final String error = "unable to resolve usage criteria";
		    throw new RuntimeException( error, e );
		}
	  }
        return criteria;
    }

   /**
    * Test if a supplied AbstractResourceAgent is a candidate for this usage 
    * specification.  The implementation check that the supplied agent is 
    * an instance of the IDL identifier exposed by the usage constraint.
    * @param agent the AbstractResourceAgent that is proposed as a candidate
    * @see AbstractResourceAgent#getAbstractResource
    * @see #getIDLIdentifier
    */
    public boolean isaCandidate( AbstractResourceAgent agent )
    {
	  if( agent == null ) return true;
        if( agent instanceof GenericResourceAgent )
        {

		//
		// get the valuefactory for the type declared as the usage
            // constraint
		//

		ValueFactory factory = ((org.omg.CORBA_2_3.ORB)orb).lookup_value_factory( getIDLIdentifier() );
            if( factory != null )
		{
                // the usage constraint is a valuetype
	 	    GenericResourceAgent generic = (GenericResourceAgent) agent;
		    if( generic.getValue() == null ) return false;
                return factory.getClass().isInstance( generic.getValue() );
		}
        }
	  return agent.getAbstractResource()._is_a( getIDLIdentifier() );
    }

   /**
    * Returns the assigned state of the usage descriptor.
    * @return boolean true if a value is assigned
    */
    public boolean isAssigned() 
    {
        return assignment != null;
    }

   /**
    * Returns the resource assigned under the usage descriptor.  The 
    * value returned may be null.
    * @return UsageAgent the UsageAgent instance (possibly null)
    */
    public UsageAgent getAssignment() 
    {
        return assignment;
    }

   /**
    * Declares an AbstractResource agent as the value assigned as the usage argument.
    * @param agent the resource agent to assign to the usage constraint
    */
    public void setAssignment( UsageAgent agent )
    {
	  assignment = agent;
	  putValue("assignment", assignment );
    }

    //=========================================================================
    // ListListener
    //=========================================================================

   /**
    * Method invoked when an object is added to a list of usage descriptions 
    * in which this descriptor is a member.  If the event references this instance
    * a local references to the assigned resource is held and made available through
    * the getAssignment method.
    * @param event the list event
    * @see #getAssignment
    */
    public void addObject( ListEvent event )
    {
	  UsageAgent link = (UsageAgent) event.getObject();
	  if( link.getTag().equals( getTag() ) ) 
	  {
		assignment = link;
		putValue( "assignment", assignment );
	  }
    }

   /**
    * Method invoked when an object is removed from the list.  The implementation 
    * monitors the retraction of usage links from the assigned task.  If the 
    * tag value matches the tag for this usage descriptor the assignment value 
    * is set to null and the listener is removed.
    */
    public void removeObject( ListEvent event )
    {
	  UsageAgent link = (UsageAgent) event.getObject();
	  if( link.getTag().equals( getTag() ) ) 
	  {
		assignment = null;
		putValue( "assignment", assignment );
	  }
    }

    //=========================================================================
    // Entity
    //=========================================================================

   /**
    * The <code>getPropertyPanels</code> operation returns a sequence of panels 
    * representing different views of the membership model.  The base 
    * <code>DefaultEntity/getView<code> operation returns <code>TabbedView</code>
    * which in-turn colects views from the type hierachy using the 
    * <code>getPropertyPanels</code> operation.
    */
    public List getPropertyPanels()
    {
	  if( getCriteria() == null ) return super.getPropertyPanels();
        if( propertyPanelList == null )
        {
		propertyPanelList = super.getPropertyPanels();
            propertyPanelList.add( 
		  new ScrollView( 
                new FeaturesPanel( getCriteria(), "Criteria" )
	        )
            );
        }
        return propertyPanelList;
    }

   /**
    * Returns a list of <code>Features</code> instances to be presented under 
    * the Properties dialog features panel.
    * @return List the list of <code>Feature</code> instances
    */
    public List getFeatures()
    {
        if( features != null ) return features;

        List list = super.getFeatures();
	  try
	  {
            list.add( new StaticFeature( "mode", getModeString() ));
            list.add( new StaticFeature( "tag", getTag() ));
            list.add( new StaticFeature( "kind", getIDLIdentifier() ));
            if( isInput ) list.add( new StaticFeature( "required", new Boolean( getRequired() )));
            if( isInput ) list.add( new StaticFeature( "implied", new Boolean( getImplied() )));
        }
	  catch( Exception e )
	  {
	      return list;
	  }
	  this.features = list;
	  return features;
    }

    private String getModeString()
    {
        if( isInput ) return "input";
        return "output";
    }

    //=========================================================================
    // Object (override)
    //=========================================================================

    public String toString()
    {
         return this.getClass().getName() + "[ id=" + System.identityHashCode( this ) +
	     " tag=" + getTag() + " idl=" + getIDLIdentifier() + 
	     " implied=" + getImplied() + " required=" + getRequired() + 
	     " ]";
    }

}
