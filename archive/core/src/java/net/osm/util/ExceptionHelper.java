/*
 * @(#)ExceptionHelper.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 24/02/2001
 */

package net.osm.util;

/**
 * General utilities supporting the listing of exception.
 */

public class ExceptionHelper
{

   /**
    * Prints the exception to the standard error out together with
    * cause statements.
    * @param e the exception to print
    */
    public static void printException( Throwable e )
    {
	  printException( null, e );
    }

   /**
    * Prints the exception to the standard error out together with
    * cause statements.
    * @param label label identifying the error
    * @param e the exception to print
    */
    public static void printException( String label, Throwable e )
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
    public static void printException( String label, Throwable e, Object source )
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
    public static void printException( String label, Throwable e, Object source, boolean trace )
    {

        java.io.PrintStream out = System.err;
        synchronized( out )
	  {
	      out.println( "=================================================" );
	      if( label != null ) out.println( "Message: " + label );
	      if( source != null ) out.println( "Source: " + source );
	      out.println( "Exception: " + e.toString() );
	      if( e.getCause() != null ) printCause( out, e );
	      out.println( "=================================================" );
            if( trace ) e.printStackTrace();
	      out.println( "=================================================" );
        }
    }

    private static void printCause( java.io.PrintStream out, Throwable e )
    {
	  Throwable cause = e.getCause();
	  out.println( "Cause: " + cause.toString() );
	  if( cause.getCause() != null ) printCause( out, cause );
    }

    public static String packException( final String message, final Throwable e )
    {
	 String error = "Message: " + message;
	 if( e == null ) return error;
       error = error + "\nException: " + e.toString();
	 return packCause( error, e.getCause() );
    }

    private static String packCause( String s, Throwable cause )
    {
        if( cause == null ) return s;
        s = s + "\nCause: " + cause.toString();
        return packCause( s, cause.getCause() );
    }

    public static void printMessage( String message )
    {
        java.io.PrintStream out = System.out;
        synchronized( out )
	  {
            out.println( message );
        }
    }

    public static String packExceptionAsHTML( final String message, final Throwable e )
    {
       String header = "<html><body><table>";
       String footer = "</table></body></html>";
	 String error = header + "<tr bgcolor='#ccccff'><td valign=top>Message</td><td>" + message + "</td></tr>";
	 if( e == null ) return error + footer;
       error = error + "<tr><td valign='top'>Exception</td><td>" 
		+ e.getClass().getName() + " <br>" + e.getMessage() + "</td></tr>";
	 return packCauseInTable( error, e.getCause() ) + footer;
    }

    private static String packCauseInTable( String s, Throwable cause )
    {
        if( cause == null ) return s;
        s = s + "<tr><td valign='top'>Cause:</td><td>" 
		+ cause.getClass().getName() + " <br>" + cause.getMessage() + "</td></tr>";
        return packCauseInTable( s, cause.getCause() );
    }


}
