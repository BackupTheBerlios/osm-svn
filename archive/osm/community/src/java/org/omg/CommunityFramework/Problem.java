// Mon Dec 18 07:03:03 CET 2000

package org.omg.CommunityFramework;

import java.io.Serializable;
import java.util.Properties;
import java.util.Date;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA.StringValueHelper;
import org.omg.Session.TimestampHelper;
import org.omg.TimeBase.UtcT;
import org.apache.time.TimeUtils;


/**
 * Problem is a utility valuetype that is exposed under the 
 * ResourceFactoryProblem exception within the CommunityFramework 
 * module, and is used to describe configuration and runtime 
 * problems within the CollaborationFramework that are not readily 
 * exposed as formal exceptions.  Examples of the application of 
 * Problem instances include the description of the cause of a 
 * failure arising during a factory creation operation.  Other 
 * examples from the CollaborationFramework include description 
 * of non-fulfillment of a constraints and documentation of 
 * non-critical problem encountered during the execution of a 
 * collaborative process.
 * <p>
 * The Problem valuetype contains a timestamp, a problem identifier, 
 * message and description, and a possibly empty sequence of 
 * contributing Problem declarations. 
 * @osm.warning implementation needs to be rebuilt using i18n
 */

public class Problem
implements StreamableValue, ValueFactory
{
    
    //==========================================================
    // static
    //==========================================================
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
        "IDL:omg.org/CommunityFramework/Problem:1.0"
    };

    //==========================================================
    // state
    //==========================================================
    
   /**
    * Date and time that the problem identification occurred.
    */
    public org.omg.TimeBase.UtcT timestamp;
    
   /**
    *Identifier of a labeled control that the problem is related to..
    */
    public String identifier;
 
   /**
    *Short human readable message describing the problem.
    */
    public String message;

   /**
    *Descriptive text detailing the problem, suitable for presentation under a human interface.
    */
    public String description;
    
   /**
    *A sequence of Problem instances representing the problem cause.
    */
    public Problem[] cause;
    
    //==========================================================
    // constructors
    //==========================================================

   /**
    * Default constructor for stream internalization.
    */
    public Problem() 
    {
    }

   /**
    * Creation of a new Problem with a supplied problem identifier.
    */
    public Problem( String identifier ) 
    {
	  this( identifier, new Problem[0] );
    }

   /**
    * Creation of a new Problem with a supplied problem identifier and 
    * properties.
    */
    public Problem( String identifier, Properties props ) 
    {
	  this( identifier, props, new Problem[0] );
    }

   /**
    * Creation of a new Problem with a supplied problem identifier,  
    * properties and cause.
    */
    public Problem( String identifier, Properties props, Problem[] cause )
    {
	  if( identifier.equals("REQUIRED_INPUT_IS_MISSING") )
	  {
		message = "A required input resource has not been established on the Task.";
		description = "The Processor that this Task is coordinating requires " +
		"the association of a tagged input resource.  This condition must be fulfilled " +
		"before the process can be started.\n" +
		"\ttag: " + props.getProperty( "tag" ) + "\n" +
		"\trequired: " + props.getProperty( "required" );
        }
	  else if( identifier.equals("REQUIRED_INPUT_TYPE_MISMATCH") )
	  {
		message = "A required input resource has an invalid type";
		description = "The type of resource required by a input constraint is " +
		"different to the type of resource currently bound to a usage association " +
		"of the same name.\n" +
		"\ttag: " + props.getProperty( "tag" ) + "\n" +
		"\tfound: " + props.getProperty( "supplied" ) + "\n" +
		"\trequired: " + props.getProperty( "required" );
        }
	  else
	  {
		message = "Unqualified problem";
		description = "A problem has been raised with an unknown problem identifier.";
        }
	  Date date = new Date();
	  this.timestamp = TimeUtils.getCurrentTime();
	  this.identifier = identifier;
	  this.cause = cause;
    }

   /**
    * Creation of a new Problem with a supplied problem identifier,  
    * and cause.
    */
    public Problem( String identifier, Problem[] cause ) 
    {
	  message = "";
	  description = "";

	  if( identifier.equals("NO_COORDINATOR") )
	  {
		message = "A Processor must be associated with a coordinating Task";
		description = "A Processor is coordinated by a single Task that is " +
               "responsible for the inital establishment of process " +
               "preconditions (such as consumption and production usage " +
               "associations). Until a Task is associated as the coordinator, " +
               "this processor cannot be executed.";
	  }
	  else if( identifier.equals("PROCESSOR_COORDINATION_ERROR") )
	  {
		message = "Internal error while attempting to locate the coordianting Task";
		description = "An unexpected exception occured while attempting to locate " +
	  	    "Task respoonsible for coordination of this Processor.  See log for details";
	  }
	  else if( identifier.equals("INPUT_PRECONDITION_ASSESSMENT_ERROR") )
	  {
		message = "Internal error while attempting to assess usage preconditions";
		description = "An unexpected exception occured while attempting to validate " +
		  " preconditions associated with this process.  See log for details";
	  }
	  else if( identifier.equals("INPUT_PRECONDITION_VALIDATION_ERROR") )
	  {
		message = "Internal error while attempting to validate a usage precondition";
		description = "An unexpected exception occured while attempting to validate " +
		  " a input preconditions associated with this process.  See log for details";
	  }
	  else
	  {
		identifier = "UNKNOWN";
		message = "Unqualified problem";
		description = "A problem has been raised with an unknown problem identifier.";
        }
	  Date date = new Date();
	  this.timestamp = TimeUtils.getCurrentTime();
	  this.identifier = identifier;
	  this.message = message;
	  this.description = description;
	  this.cause = cause;
    }

   /**
    * Creation of a new Problem with a supplied problem identifier,  
    * message and description.
    */
    public Problem( String identifier, String message, String description ) 
    {
	  this( identifier, message, description, new Problem[0] );
    }

   /**
    * Creation of a new Problem with a supplied problem identifier,  
    * message, description and cause.
    */
    public Problem( String identifier, String message, String description, Problem[] cause ) 
    {
	  Date date = new Date();
	  this.timestamp = TimeUtils.getCurrentTime();
	  this.identifier = identifier;
	  this.message = message;
	  this.description = description;
	  this.cause = cause;
    }

    public Problem( String identifier, String message, Throwable cause )
    {
	  this.timestamp = TimeUtils.getCurrentTime();
	  this.identifier = identifier;       
	  this.message = message;
	  this.description = "";
	  this.cause = new Problem[]{ new Problem( cause ) };
    }

    public Problem( Throwable e )
    {
	  this.timestamp = TimeUtils.getCurrentTime();
	  this.identifier = e.getClass().getName();
	  this.message = e.getMessage();
	  this.description = "";
	  if( e.getCause() != null ) 
	  {
		this.cause = new Problem[]{ new Problem( e.getCause() ) };
	  }
	  else
	  {
	      this.cause = new Problem[0];
	  }
    }

    //==========================================================
    // Problem
    //==========================================================
    
    public void print()
    {
	  String lead = "\t";
	  System.out.println( lead + "identifier: " + identifier );
	  System.out.println( lead + "message: " + message );
	  System.out.println( lead + "description: " + description );
	  System.out.println( lead + "cause: " + cause.length );
        for( int i = 0; i<cause.length; i++ )
        {
		Problem p = cause[i];
		p.print();
        }
    }

   /**
    * Return the value TypeCode
    */
    public TypeCode _type()
    {
        return ProblemHelper.type();
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read(InputStream is)
    {
	  timestamp = TimestampHelper.read(is);
	  identifier = LabelHelper.read(is);
	  message = StringValueHelper.read(is);
	  description = StringValueHelper.read(is);
	  cause = ProblemsHelper.read(is);
    }
        
   /**
    * Marshal the value into an OutputStream
    */
    public void _write( org.omg.CORBA.portable.OutputStream os )
    {
	  TimestampHelper.write(os,timestamp);
	  LabelHelper.write(os,identifier);
	  StringValueHelper.write(os,message);
	  StringValueHelper.write(os,description);
	  ProblemsHelper.write(os,cause);
    }

   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }
    
   /**
    * Problem factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Problem() );
    }

    public String toString()
    {
        return toString( this );
    }

    public String toString( Problem problem )
    {
	  return toString( "PROBLEM: ", problem );
    }

    public String toString( String lead, Problem problem )
    {
	  String s = lead + problem.identifier + "\n"
	    + "\tmessage: " + problem.message + "\n"
	    + "\tdescription: " + problem.description + "\n"
	    + "\tcause: " + problem.cause.length;

        for( int i = 0; i<problem.cause.length; i++ )
        {
		Problem p = problem.cause[i];
		s = s + "\n\n" + toString( "CAUSE/" + (i+1) + ": ", p );
        }
	  return s;
    }

}
