// Thu Nov 23 07:22:02 CET 2000

package org.omg.CollaborationFramework;

import java.util.LinkedList;
import java.io.Serializable;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.OverlayLayout;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.avalon.framework.configuration.Configuration;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CommunityFramework.Role;
import org.omg.CommunityFramework.RoleHelper;
import org.omg.CommunityFramework.RolePolicy;
import org.omg.CommunityFramework.QuorumPolicy;
import org.omg.CommunityFramework.QuorumAssessmentPolicy;


/**
CollaborationModel is the valuetype that defines the bulk of the semantics behind an instance of
CollaborationProcessor. CollaborationModel extends ProcessorModel though addition of a role
hierarchy, and, State hierarchy. The entire collaboration model is structurally centered on a state
hierarchy, the root of which is defined by the State instance exposed under the state field. The
root-state and sub-states contain the declaration of available triggers (transitions holders) that can
be referenced by clients through apply operations on the Collaboration interface. The state field
named role contains a Role valuetype that represents the root of a role hierarchy that can be
referenced by Trigger instances (contained by State instances) as preconditions to activation. For
example, a transition (exposed as Trigger) may reference a role as a guard, which in turn
introduces a constraint on the invoking client to be associated with the Encounter membership
under an equivalent role.
<p>As a valuetype, a CollaborationModel can be passed between different domains and treated as a
self-contained structure that can be readily re-used by trading partners. The structural
information contained in the inherited ProcessorModel defines the logical wiring of a processor
towards its coordinating task, while the extensions introduced under CollaborationModel define
the semantics of collaborative interaction.
*/

public class CollaborationModel extends ProcessorModel
implements StreamableValue, ValueFactory
{
    
    //
    //  state members
    //
    
    /**
    * A Role valuetype (refer CommunityFramework) that defines a
    * hierarchy of business roles that may be referenced by other control
    * structures within a CollaborationModel (refer Trigger) for the purpose of
    * establishing membership and quorum preconditions towards an invoking
    * client. This value may be null if all Trigger guard value are also null.
    */
    public Role role;

    /**
    * A non-null value defining the root state of the collaboration model. A
    * State is itself a container of other states within which Triggers are contained.
    * Triggers act as constraint guards relative to the Actions they contain.
    */
    public State state;


    // private fields

    //private LinkedList views;
    //private StateView stateView;
    //private RoleView roleView;

    //
    // constructors
    //

    /**
    * Null argument constructor used during stream internalization.
    */
    public CollaborationModel( ) {}

   /**
    * Creation of a collaboration model based on a supplied 
    * Configuration instance.
    */
    public CollaborationModel( Configuration conf )
    {
	  super( conf );
	  try
	  {
            role = new Role( conf.getChild("role") );
            state = new State( conf.getChild("state") );
	      state.initalizeTriggers( this );
		state.initalizeReferrals( this );
	  }
	  catch( Exception e )
	  {
		throw new RuntimeException("unable to create configure collaboration model", e );
	  }
    }
    
    //
    // implementation of Streamable
    //
    
    public TypeCode _type()
    {
        return CollaborationModelHelper.type();
    }
    
    public void _read(InputStream _is)
    {
        super._read(_is);
        role = RoleHelper.read(_is);
        state = StateHelper.read(_is);
    }
    
    public void _write(OutputStream _os)
    {
        super._write(_os);
        RoleHelper.write(_os, role);
        StateHelper.write(_os, state);
    }
    
    //
    // implementation of ValueBase
    //
    
    static final String[] _ids_list =
    {
        "IDL:omg.org/CollaborationFramework/CollaborationModel:1.0",
    };
    
    public String[] _truncatable_ids() { return _ids_list; }

    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new CollaborationModel() );
    }

    
    /**
     *Return a role corresponding to the supplied role label.  Returns null if
     *no matching role.
     */
    public Role lookupRole( String label ) 
    {
        if( role != null ) return role.lookupRole( label );
        return null;
    }
    
   /**
    *Return a State corresponding to the supplied label.  Returns null if
    *no matching state.
    */
    public State lookupState( String label ) 
    {
        if( state != null ) return state.lookupState( label );
        return null;
    }

   /**
    *Return a Action corresponding to the supplied label.  Returns null if
    *no matching action.
    */
    public Action lookupAction( String label ) 
    {
        if( state != null ) return state.lookupAction( label );
        return null;
    }


    /**
    * A CollaborationModel is a concrete criteria that is 
    * componsed of a set of usage declarations from the inherited
    * ProcessorModel.  CollaborationModel extends this with a 
    * Role and State hierachy.  The getViews operation returns
    * a LinkedList of Components suitable for addition to a 
    * JTabbedPane by the invoking client.
    *
    * @return LinkedList of Components
    */
/*
    public LinkedList getViews( ) {
        if( views == null ) {
		views = super.getViews();
		views.add( getStateView() );
		if( role != null ) views.add( getRoleView());
        }
        return views;
    }
*/
   /**
    * Return the view of a the root state within this CollaborationModel.
    *
    * @return JScrollPane of root state
    */
/*
    public JScrollPane getStateView( ) {
	  if( stateView == null ) stateView = new StateView();
	  return stateView;
    }
*/
   /**
    * Return the view of a the root state within this CollaborationModel.
    *
    * @return JScrollPane of root role
    */
/*
    public JScrollPane getRoleView( ) {
	  if( roleView == null ) roleView = new RoleView();
	  return roleView;
    }

    private class StateView extends JScrollPane{
        
        protected StateView( ) {

		super();
            setName("States");

		// create background
		JPanel p = new JPanel();
		p.setLayout( new FlowLayout( FlowLayout.CENTER ));
            p.setBorder( new EmptyBorder( 20, 30, 10, 30 ));
            p.setBackground(Color.white);

		// add root state
		Component c = state.getStateView();
		p.add( c );

		// associate background to scrolling pane
		getViewport().setView( p );
        }
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
