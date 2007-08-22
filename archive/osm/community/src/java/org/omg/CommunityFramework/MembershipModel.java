
package org.omg.CommunityFramework;

import java.util.LinkedList;
import java.io.Serializable;

import org.apache.avalon.framework.configuration.Configuration;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

/**
 * MembershipModel is a valuetype that extends the Model valuetype through 
 * addition of fields containing a MembershipPolicy and a Role representing 
 * the root business role of a role hierarchy.
 */

public class MembershipModel 
extends Control
implements Model
{

    //==========================================================
    // static
    //==========================================================
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
        "IDL:omg.org/CommunityFramework/MembershipModel:1.0",
    };

    //==========================================================
    // state
    //==========================================================
    
    /**
    * Defines privacy and exclusivity policy of the containing Membership.
    */
    public MembershipPolicy policy;

   /**
    * The root role of a role hieriachy.
    */
    public Role role;

    //==========================================================
    // constructors
    //==========================================================
    
   /**
    * Default constructor used in valuetype internalization.
    */
    public MembershipModel(){}

   /**
    * Creation of a new MembershipModel based on a supplied root role and policy.
    */
    public MembershipModel ( String label, String note, Role role, MembershipPolicy policy ) 
    {
	  super( label, note );
        this.role = role;
        this.policy = policy;
    }

    /**
    * Creation of a model based on a supplied Configuration instance.
    */
    public MembershipModel( Configuration conf )
    {
	  super( conf );
	  try
	  {
            policy = new MembershipPolicy( conf.getChild( "mpolicy" ) );
            role = new Role( conf.getChild( "role" ) );
	  }
	  catch( Exception e )
	  {
		throw new RuntimeException("unable to create configured membership policy", e );
	  }
    }
    //==========================================================
    // implemenetation
    //==========================================================

   /**
    * Return the value TypeCode
    */
    public org.omg.CORBA.TypeCode _type()
    {
        return MembershipModelHelper.type();
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read(InputStream _is)
    {
        super._read(_is);
        policy = MembershipPolicyHelper.read(_is);
        role = RoleHelper.read(_is);
    }
    
   /**
    * Marshal the value into an OutputStream
    */
    public void _write(OutputStream _os)
    {
        super._write(_os);
        MembershipPolicyHelper.write(_os, policy);
        RoleHelper.write(_os, role);
    }
    
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }
    
   /**
    * MembershipModel factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new MembershipModel() );
    }

    // 
    // implementation of Model
    //

   /**
    * Returns a LinkedList of Component instaces, each representing a tab to be 
    * displayed within a JTabbedPane.
    * 
    * @return LinkedList of Component instances for addition in a containing
    * JTabbedPane.
    */
/*
    public LinkedList getViews( ) {
        if( views == null ) {
		views = new LinkedList();
		views.add( super.getControlView());
		if( role != null ) views.add( getRoleView());
        }
        // WARNING: need to add policy view
        return views;
    }
*/
   /**
    * Returns a JScrollPane containing a view of the role hieriachy.
    * 
    * @return JScrollPane view to present under the human interface.
    */
/*
    public JScrollPane getRoleView( ) {
	  if( roleView == null ) roleView = new RoleView();
	  return roleView;
    }

    private class RoleView extends JScrollPane {
        
        private RoleView( ) {

		super();
            setName("Roles");

		// create background
		JPanel p = new JPanel();
            p.setBackground(Color.white);
            p.setLayout( new FlowLayout(FlowLayout.LEFT) );

		// create role graph
		if( role != null) {
		    String title = role.getLabel();
		    if( role.is_abstract ) {
			  title = title + " (passive role)";
		    } else {
			  title = title + " (active role)";
		    }
		    DefaultMutableTreeNode top = new DefaultMutableTreeNode( title );
		    populate( top, role );
		    JTree tree = new JTree( top );
		    p.add( tree );
            }

		// associate background to scrolling pane
		getViewport().setView( p );

        }

	  private void populate( DefaultMutableTreeNode node, Role role ) {
		
	      RolePolicy p = role.getPolicy();
		node.add( new DefaultMutableTreeNode( "quorum: " + p.getQuorum()));
		int c = p.getCeiling();
		if( c > -1 ) node.add( new DefaultMutableTreeNode( "ceiling: " + c ));

	      if( p.getQuorumPolicy() == QuorumPolicy.SIMPLE ) {
		    node.add( new DefaultMutableTreeNode( "policy: SIMPLE" ));
		} else {
		    node.add( new DefaultMutableTreeNode( "policy: CONNECTED" ));
		}

	      if( p.getAssessmentPolicy() == QuorumAssessmentPolicy.STRICT ) {
		    node.add( new DefaultMutableTreeNode( "assessment: STRICT" ));
		} else {
		    node.add( new DefaultMutableTreeNode( "assessment: LAZY" ));
		}

	      if( role.roles != null ) {
		    int n = role.roles.length;
		    for( int i = 0; i < n; i++ ) {
			  Role child = role.roles[i];
		        String title = child.getLabel();
		        if( child.is_abstract ) {
			      title = title + " (passive role)";
		        } else {
			      title = title + " (active role)";
		        }

			  DefaultMutableTreeNode sibling = new DefaultMutableTreeNode( title ); 
		        node.add( sibling );
			  populate( sibling, child );
                }
		}
        }
    }
*/
}
