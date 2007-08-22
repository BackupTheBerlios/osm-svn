// Thu Nov 23 07:22:01 CET 2000

package org.omg.CommunityFramework;

import java.io.Serializable;

import org.apache.avalon.framework.configuration.Configuration;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

/**
* RolePolicy is a valuetype that defines ceiling limits and quorum policy for a 
* particular role. The value of the quorum filed defines the minimum number of 
* Members that must be associated with the role that the policy is associated 
* with before the role can be considered to have reached quorum.  The ceiling 
* field defines the maximum number of Members that may be associated under the 
* role.  The policy field exposes a RolePolicy value that details the mechanism 
* to quorum calculations.  In the case of a null value for policy or assessment, 
* the value shall be inferred by the parent policy.  In the case of no parent 
* policy declaration, quorum policy shall be SIMPLE and assessment policy shall 
* be LAZY (representing the least restrictive case).  The absence of a ceiling 
* value shall indicate no limit on the number of associated members.  The 
* absence of a quorum value shall imply a quorum of 0.
*/

public class RolePolicy
implements StreamableValue, ValueFactory
{
    
    //==========================================================
    // static
    //==========================================================
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
        "IDL:omg.org/CommunityFramework/RolePolicy:1.0"
    };

    //==========================================================
    // state
    //==========================================================
        
   /**
    * The minimum number of Members that must be associated with the role 
    * before the role can be considered to have achieved quorum.   
    */
    public int quorum = -1;

   /**
    * The maximum number of Member instances that may be associated to this role.
    */
    public int ceiling = -1;

   /**
    * An emanation of SIMPLE or CONNECTED.  When the value is SIMPLE, quorum 
    * calculation is based on number of Member instances.  When the quorum policy 
    * is CONNECTED, the quorum calculation is based on the number of Member 
    * instances that reference a User that is in a connected state.
    */
    public QuorumPolicy policy = QuorumPolicy.SIMPLE;

   /**
    * An enumeration used to determine the mechanism to be applied to quorum 
    * assessment.  The enumeration describes STRICT and LAZY assessment policies.  
    * Under STRICT assessment, the establishment of a quorum is required before the 
    * membership is considered valid.  Under LAZY assessment, the determination of 
    * quorum is based on the accumulative count of members during the lifetime of 
    * the membership.  LAZY assessment introduces the possibility for the execution 
    * of optimistic processes that depend on valid quorums for finalization and 
    * commitment of results.
    */
    public QuorumAssessmentPolicy assessment = QuorumAssessmentPolicy.STRICT;

   /**
    * Internal reference to the role containing this policy.
    */
    private Role role;

    //==========================================================
    // constructors
    //==========================================================
    
   /**
    * Default constructor used during internalization of a RolePolicy instance from a 
    * valuetype stream.
    */
    public RolePolicy( ){}

   /**
    * Configuration of the role policy based on a supplied Configuration instance.
    */
    public RolePolicy( Configuration conf )
    {
        this.quorum = conf.getAttributeAsInteger("quorum",0);
        this.ceiling = conf.getAttributeAsInteger("ceiling",0);

	  try
	  {
	      String policy = conf.getAttribute( "policy" );
		if( policy.toLowerCase().equals("simple") ) 
		{
		    this.policy = QuorumPolicy.SIMPLE;
		}
		else
		{
		    this.policy = QuorumPolicy.CONNECTED;
		}
        }
        catch( Exception e )
        {
        }

	  try
	  {
	      String assessment = conf.getAttribute( "assessment" );
		if( assessment.toLowerCase().equals("lazy") ) 
		{
		    this.assessment = QuorumAssessmentPolicy.LAZY;
		}
		else
		{
		    this.assessment = QuorumAssessmentPolicy.STRICT;
		}
        }
        catch( Exception e )
        {
        }
    }

    //==========================================================
    // implementation
    //==========================================================

   /**
    * Declare the containing role.
    */
    protected void setRole( Role role )
    {
        this.role = role;
    }

   /**
    * Return the quorum value.
    */
    public int getQuorum() 
    {
        if( quorum > -1 ) return quorum;
	  return 0;
    }

   /**
    * Return the ceiling value where a 0 ceiling is equivilent 
    * to no limit and a negative ceiling value implies that the 
    * ceiling defaults to the containing roles parent policy 
    * celing value.
    */
    public int getCeiling() 
    {
        if( ceiling > -1 ) return ceiling;
        return 0;
    }

   /**
    * Return the quorum policy value.
    */
    public QuorumPolicy getQuorumPolicy() 
    {
	  if( policy != null ) return policy;
	  return QuorumPolicy.SIMPLE;
    }

   /**
    * Return the assessment policy value.
    */
    public QuorumAssessmentPolicy getAssessmentPolicy() 
    {
	  if( assessment != null ) return assessment;
	  return QuorumAssessmentPolicy.LAZY;
    }

   /**
    * Static method that returns a default role policy in the event that
    * the a root role is constructed with a policy declaration.
    */
    public static RolePolicy getDefaultPolicy() 
    {
	   RolePolicy p = new RolePolicy();
	   p.policy = QuorumPolicy.SIMPLE;
	   p.assessment = QuorumAssessmentPolicy.LAZY;
	   p.ceiling = 0;
	   p.quorum = 0;
	   return p;
    }

   /**
    * Return the value TypeCode
    */    
    public TypeCode _type()
    {
        return RolePolicyHelper.type();
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read(InputStream is)
    {
        quorum = is.read_long();
        ceiling = is.read_long();
        policy = QuorumPolicyHelper.read(is);
        assessment = QuorumAssessmentPolicyHelper.read(is);
    }
    
   /**
    * Marshal the value into an OutputStream
    */
    public void _write(org.omg.CORBA.portable.OutputStream os)
    {
        os.write_long(quorum);
        os.write_long(ceiling);
        QuorumPolicyHelper.write(os, policy);
        QuorumAssessmentPolicyHelper.write(os, assessment);
    }
    
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }

   /**
    * RolePolicy factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new RolePolicy() );
    }
}
