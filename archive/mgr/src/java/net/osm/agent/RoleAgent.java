
package net.osm.agent;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.TableColumnModel;

import org.apache.avalon.framework.logger.Logger;

import org.omg.CommunityFramework.Control;
import org.omg.CommunityFramework.QuorumPolicy;
import org.omg.CommunityFramework.QuorumAssessmentPolicy;
import org.omg.CommunityFramework.Role;
import org.omg.CORBA.ORB;

import net.osm.agent.util.SequenceIterator;
import net.osm.shell.DefaultCellRenderer;
import net.osm.shell.StaticFeature;
import net.osm.shell.FeaturesPanel;
import net.osm.shell.ScrollView;
import net.osm.shell.TabbedView;
import net.osm.shell.View;
import net.osm.util.IconHelper;
import net.osm.util.ActiveList;

/**
 * Business roles, represented by instances of the class <code>RoleAgent</code>, provide
 * a structure that defines role poicy, notions of abstract versus concrete role types, 
 * and role heirachies.  Business roles are used extensively under the OSM Community and 
 * Collaboration frameworks as a mechanisms through which rights and privaliges can be 
 * granted within the scope of dynamically changing communites and business processes.
 */
public class RoleAgent extends ControlAgent
{

    //=====================================================================
    // static 
    //=====================================================================

    private static String path = "net/osm/agent/image/role.gif";
    private static ImageIcon icon = IconHelper.loadIcon( path );

    private static final String connectTrue = "required";
    private static final String connectFalse = "not-required";

    private static final String lazyPolicy = "on process completion";
    private static final String activePolicy = "real-time";

    //=====================================================================
    // state 
    //=====================================================================

   /**
    * The Role valuetype.
    */
    private Role role;

   /**
    * Cached reference to the sibling roles as RoleAgent instances
    */
    private RoleAgent[] roles;

   /**
    * List containing the set of subsidiary roles.
    */
    private List list;

   /**
    * The graphical representation of the role.
    */
    private View view;

    private LinkedList properties;
    private List propertyPanelList;
    private List features;
    private TableColumnModel featuresColumnModel;

    //=========================================================================
    // Constructor
    //=========================================================================

   /**
    * Creation of a new RoleAgent based on a supplied role valuetype.
    * @param value the <code>Role</code> valuetype that defines the agent state.
    * @param logger the logging channel supplied by the model containing this role.
    */
    public RoleAgent( Logger logger, Object value )
    {
	  super( logger, value );
	  setPrimary( value );
    }

    //=========================================================================
    // Atrribute setters
    //=========================================================================

   /**
    * Set the instance of Role maintained by this RoleAgent.
    * @param value an instance of org.omg.CommunityFramework.Role
    */
    public void setPrimary( Object value ) 
    {
	  if( value == null ) throw new RuntimeException(
		"Null value supplied under the setPrimary method.");
        try
        {
            this.role = (Role) value;
		super.setPrimary( value );
        }
	  catch( Throwable t )
        {
		throw new RuntimeException(
		"Supplied constructor value is not a role.", t );
        }
    }

    //=========================================================================
    // Getter methods
    //=========================================================================

   /**
    * Roles can be abstract or concrete - abstract roles cannot be assigned 
    * directly to members, however, abstract roles can hold sibling roles 
    * which infer the role of a parent.
    * @return boolean true if this is an abstract role.
    */

    public boolean getAbstract()
    {
	  return role.is_abstract;
    }

   /**
    * Roles can contain sibling roles, exposed by the <code>getRoles</code> method.
    * Simbling roles facilitate the establishment of role heirachies and the means 
    * through which members are implicitly associated to all parents of a given role.
    */

    public List getRoles()
    {
        if( list == null )
        {
	      list = new ActiveList( getLogger() );
	      if( role.roles != null ) 
		{
	          int n = role.roles.length;
	          for( int i=0; i<n; i++ )
                {
			  RoleAgent agent = new RoleAgent( getLogger(), role.roles[i] );
			  if( featuresColumnModel != null ) agent.setFeatureColumnModel( featuresColumnModel );
                    list.add( agent );
                }
            }
        }
	  return list;
    }

   /**
    * Returns true if the role can be considered as having reached quorum.
    * @return boolean true if quorum established
    */

    public boolean getQuorum()
    {
	  throw new RuntimeException("method not implemented");
    }

   /**
    * Returns the minimum number of members associated under this role for the role
    * to be considered as having reached quorum.
    * @return int minimum number of members required for quorum
    */

    public int getFloor()
    {
	  return role.getPolicy().getQuorum();
    }

   /**
    * Returns the maximum number of members that may be associated under this role.
    * @return int ceiling limit
    */

    public int getCeiling()
    {
	  return role.getPolicy().getCeiling();
    }

   /**
    * Returns the connection policy to apply during quorum calculation.
    * @return boolean true if members must be connected to be included in the quorum count
    */

    public boolean getConnectedPolicy()
    {
	  return ( role.getPolicy().getQuorumPolicy() == QuorumPolicy.CONNECTED );
    }
 
   /**
    * Returns the deferred count assessment policy.
    * @return true if quorum counting can be deferred.
    */

    public boolean getDeferralPolicy()
    {
	  return ( role.getPolicy().getAssessmentPolicy() == QuorumAssessmentPolicy.LAZY );
    }

   /**
    * Returns a role within this role heirachy given a role label.
    * @param label the role label to locate
    * @return RoleAgent
    * @exception NotFoundException
    */

    public RoleAgent getRole( String label ) throws NotFoundException
    {
        if( this.role.label.equals( label ) ) return this;
	  Iterator iterator = getRoles().iterator();
        while( iterator.hasNext() )
        {
            try
            {
                return ((RoleAgent)iterator.next()).getRole( label );
		}
	      catch( NotFoundException nf )
		{
		}
        }
        throw new NotFoundException( "Role '" + label + "' not found." );
    }

    //=========================================================================
    // Entity
    //=========================================================================

    protected void setFeatureColumnModel( TableColumnModel model )
    {
        featuresColumnModel = model;
    }

    public View getView()
    {
        if( featuresColumnModel == null ) throw new NullPointerException(
	    "features column model has not been set");
        return new ScrollView( 
            new FeaturesPanel( this, getLabel(), featuresColumnModel )
	  );
    }

   /**
    * The <code>getStandardViews</code> operation returns a sequence of panels 
    * representing different views of the content and/or associations maintained by
    * the agent.
    */
    public List getViews()
    {
	  System.out.println("getting panel for " + getLabel() );
        if( propertyPanelList == null )
        {
		propertyPanelList = super.getPropertyPanels();
            propertyPanelList.add( 
		  new ScrollView( 
                new FeaturesPanel( this, getLabel(), getShell().getDefaultFont() )
	        )
            );
        }
        return propertyPanelList;
    }

   /**
    * Returns a list of <code>Features</code> instances to be presented under 
    * the Model dialog features panel.
    * @return List the list of <code>Feature</code> instances
    */
    public List getFeatures()
    {
        if( features != null ) return features;
        List list = super.getFeatures();
	  try
	  {
		String ceiling = "unlimited";
		String floor = "no constraint";
		String connection = connectFalse;
		String policy = activePolicy ;

		if( getCeiling() > 0 ) ceiling = "no more than " + getCeiling();
		if( getFloor() > 0 ) floor = "at least " + getFloor();
            if( getConnectedPolicy() ) connection = connectTrue;
            if( getDeferralPolicy() ) policy = lazyPolicy;

	      list.add( new StaticFeature("abstract", "" + getAbstract() ));
	      list.add( new StaticFeature("assessment", policy ));
	      list.add( new StaticFeature("connection", connection ));
	      list.add( new StaticFeature("ceiling", ceiling ));
	      list.add( new StaticFeature("floor", floor ));
        }
	  catch( Exception e )
	  {
	      return list;
	  }
	  this.features = list;
	  return features;
    }

   /**
    * Returns the icon representing the entity.
    * @param size a value of LARGE or SMALL
    * @return Icon iconic representation of the entity
    * @osm.note only small icons are currently implemented
    */
    public Icon getIcon( int size )
    {
        return icon;
    }


   /**
    * Test if this entity if a leaf or a composite
    * @return boolean true if this is a leaf entity
    */
    public boolean isaLeaf( )
    {
        return false;
    }

   /**
    * Returns a list of entities that represents the navigatable content
    * of the workspace. 
    * @return List the navigatable content
    */
    public List getChildren( )
    {
        return getRoles();
    }

}
