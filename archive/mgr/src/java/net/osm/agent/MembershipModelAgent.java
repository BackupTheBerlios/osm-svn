
package net.osm.agent;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import javax.swing.JPanel;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.omg.CommunityFramework.Control;
import org.omg.CommunityFramework.PrivacyPolicyValue;
import org.omg.CommunityFramework.MembershipModel;
import org.omg.CommunityFramework.PrivacyPolicyValue;
import org.omg.CORBA.ORB;

import net.osm.shell.Panel;
import net.osm.shell.View;
import net.osm.shell.ScrollView;
import net.osm.shell.SplitPane;
import net.osm.shell.StaticFeature;
import net.osm.shell.FeaturesPanel;
import net.osm.shell.NavigatorPanel;
import net.osm.util.ActiveList;

/**
 * MembershipModel is an agent that wraps a MembershipModel valuetype 
 * and exposes information pertaining to MembershipPolicy and a role 
 * role representing the root business role of a role hierarchy.
 */

public class MembershipModelAgent extends ControlAgent
{

   /**
    * The object reference to the MembershipModel that this agents 
    * represents.
    */
    protected MembershipModel model;

   /**
    * Cached reference to the root role.
    */
    protected RoleAgent role;

   /**
    * A list contining this instance and the root role.
    */
    protected List structure;

   /**
    * A list of features exposed by this model.
    */  
    private List features;

    private List propertyPanelList;


    //=========================================================================
    // Constructor
    //=========================================================================

   /**
    * Default null constructor supporting creation of a new 
    * <code>MembershipModelAgent</code>.
    */
    public MembershipModelAgent(  ){}

    //=========================================================================
    // implementation
    //=========================================================================

   /**
    * Set the resource that is to be presented.
    */
    public void setPrimary( Object value ) 
    {
	  if( value == null ) throw new RuntimeException(
		"ControlAgent/setPrimary - null value supplied.");
        try
        {
            this.model = (MembershipModel) value;
		super.setPrimary( value );
        }
	  catch( Throwable t )
        {
		throw new RuntimeException(
		"MembershipModelAgent/setPrimary - bad type.");
        }
    }

   /**
    * Returns the privacy policy value.
    */

    public PrivacyPolicyValue getPrivacyPolicy()
    {
	  return model.policy.privacy;
    }

   /**
    * Returns the privacy policy value as a string.
    */

    public String getPrivacy()
    {
	  if( model.policy.privacy == PrivacyPolicyValue.PUBLIC_DISCLOSURE ) 
        {
            return "public";
        }
        else if( model.policy.privacy == PrivacyPolicyValue.RESTRICTED_DISCLOSURE )
        {
            return "protected";
        }
	  return "private";
    }

   /**
    * Returns the exclusive pricipal constraint.
    */
    public boolean getExclusive()
    {
	  return model.policy.exclusive;
    }

   /**
    * Returns the root role.
    */
    public RoleAgent getRole()
    {
	  if( this.role == null ) 
	  {
		this.role = new RoleAgent( getLogger(), model.role );
		this.role.setFeatureColumnModel( 
		  FeaturesPanel.newFeaturesColumnModel( 
		    getShell().getDefaultFont() 
		  ) 
		);
        }
	  return this.role;
    }

    //=========================================================================
    // Visuals
    //=========================================================================

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
	      list.add( new StaticFeature("privacy", "" + getPrivacy() ));
	      list.add( new StaticFeature("exclusive", "" + getExclusive() ));
        }
	  catch( Exception e )
	  {
	      return list;
	  }
	  this.features = list;
	  return features;
    }

   /**
    * The <code>getPropertyPanels</code> operation returns a sequence of panels 
    * representing different views of the membership model.  The base 
    * <code>DefaultEntity/getView<code> operation returns <code>TabbedView</code>
    * which in-turn colects views from the type hierachy using the 
    * <code>getPropertyPanels</code> operation.
    */
    public List getPropertyPanels()
    {
        if( propertyPanelList == null )
        {
		propertyPanelList = super.getPropertyPanels();
            propertyPanelList.add( 
		  new SplitPane( "Roles", new ScrollView( 
                new NavigatorPanel( getRole(), "Roles", true )
	        ))
            );
        }
        return propertyPanelList;
    }
}
