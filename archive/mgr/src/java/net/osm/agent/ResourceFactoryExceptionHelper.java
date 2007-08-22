/*
 * @(#)ResourceFactoryExceptionHelper.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 24/02/2001
 */

package net.osm.agent;

import net.osm.util.ExceptionHelper;
import org.omg.CommunityFramework.ResourceFactoryProblem;
import org.omg.CommunityFramework.Problem;

/**
 * General utilities supporting the listing of exception.
 */

public class ResourceFactoryExceptionHelper extends ExceptionHelper
{

   /**
    * Prints the exception to the standard error out together with
    * cause statements.
    * @param e the exception to print
    */
    public static void printException( ResourceFactoryProblem e )
    {
	  printException( null, e );
    }

   /**
    * Prints the exception to the standard error out together with
    * cause statements.
    * @param label label identifying the error
    * @param e the exception to print
    */
    public static void printException( String label, ResourceFactoryProblem e )
    {
        printException( label, e, null );
    }

   /**
    * Prints the exception to the standard error out together with
    * source and cause statements.
    * @param source the source of the request
    * @param label label identifying the error
    * @param e the exception to print
    */
    public static void printException( String label, ResourceFactoryProblem e, Object source )
    {
        printException( label, e, source, false );
    }

   /**
    * Prints the exception to the standard error out together with
    * source and cause statements.
    * @param source the source of the request
    * @param label label identifying the error
    * @param e the exception to print
    */
    public static void printException( String label, ResourceFactoryProblem e, Object source, boolean trace )
    {

        java.io.PrintStream out = System.err;
        synchronized( out )
	  {
	      out.println( "=================================================" );
	      if( label != null ) out.println( "Message: " + label );
	      if( source != null ) out.println( "Source: " + source );
	      out.println( "Exception: " + e.toString() );
	      if( e.problem != null ) printCause( out, e.problem );
	      out.println( "=================================================" );
            if( trace ) e.printStackTrace();
	      out.println( "=================================================" );
        }
    }

    private static void printCause( java.io.PrintStream out, Problem problem )
    {
	  out.println( "Problem: " + problem.toString() );
    }
}
