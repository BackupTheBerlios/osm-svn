/*
 * License: etc/LICENSE.TXT
 */

package net.osm.gateway.tag;

import java.io.StringWriter;
import java.util.StringTokenizer;
import java.io.PrintWriter;

/**
 * Thrown by an Pipeline as a result of an unexpected runtime error 
 * during execution.
 *
 * @author  mcconnell
 * @version 1.0
 */

public class ExceptionUtil
{

    public static String[] captureStackTrace( final Throwable throwable )
    {
        final StringWriter sw = new StringWriter();
        throwable.printStackTrace( new PrintWriter( sw, true ) );
        return splitString( sw.toString(), "\n" );
    }

    private static String[] splitString( final String string, final String onToken )
    {
        final StringTokenizer tokenizer = new StringTokenizer( string, onToken );
        final String[] result = new String[ tokenizer.countTokens() ];

        for( int i = 0; i < result.length; i++ )
        {
            result[ i ] = tokenizer.nextToken();
        }

        return result;
    }
}

