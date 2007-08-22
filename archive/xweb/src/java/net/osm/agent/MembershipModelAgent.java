
package net.osm.agent;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.omg.CommunityFramework.Control;
import org.omg.CommunityFramework.PrivacyPolicyValue;
import org.omg.CommunityFramework.MembershipModel;
import org.omg.CommunityFramework.PrivacyPolicyValue;
import org.omg.CORBA.ORB;

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


    //=========================================================================
    // Constructor
    //=========================================================================

    public MembershipModelAgent(  )
    {
    }

    public MembershipModelAgent( Object value )
    {
	  super( value );
        try
        {
            this.model = (MembershipModel) value;
        }
	  catch( Throwable t )
        {
		throw new RuntimeException(
		"MembershipModelAgent/setReference - bad type.");
        }
    }


    //=========================================================================
    // Atrribute setters
    //=========================================================================

   /**
    * Set the resource that is to be presented.
    */
    public void setReference( Object value ) 
    {
	  if( value == null ) throw new RuntimeException(
		"ControlAgent/setReference - null value supplied.");
        try
        {
            this.model = (MembershipModel) value;
		super.setReference( value );
        }
	  catch( Throwable t )
        {
		throw new RuntimeException(
		"MembershipModelAgent/setReference - bad type.");
        }
    }

    //=========================================================================
    // Getter methods
    //=========================================================================

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
	  if( this.role == null ) this.role = new RoleAgent( model.role );
	  return this.role;
    }
}
