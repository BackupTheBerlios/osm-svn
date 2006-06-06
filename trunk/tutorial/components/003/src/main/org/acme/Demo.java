/*
 * Copyright 2006 Stephen J. McConnell.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acme;

import java.awt.Color;
import java.io.File;
import java.net.URI;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * A component with a context
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class Demo
{
    //------------------------------------------------------------------
    // context
    //------------------------------------------------------------------
    
   /**
    * Component driven context criteria.
    */
    public interface Context
    {
       /**
        * Return the assigned color.
        * @return the required color value
        */
        Color getColor();
        
       /**
        * Return the optional color.
        * @param color the default color value
        * @return the resolved color value 
        */
        Color getOptionalColor( Color color );
        
       /**
        * Return a constructed color.
        * @param color another color value
        * @return the color value 
        */
        Color getAnotherColor();
        
       /**
        * Return as assigned non-optional integer value.
        * @return the integer value
        */
        int getInteger();
        
       /**
        * Return as resolved optional integer value.
        * @param value the default value
        * @return the integer value
        */
        int getOptionalInteger( int value );
        
       /**
        * Return as assigned non-optional short value.
        * @return the short value
        */
        short getShort();
        
       /**
        * Return as resolved optional short value.
        * @param value the default value
        * @return the short value
        */
        short getOptionalShort( short value );
        
       /**
        * Return as assigned non-optional long value.
        * @return the long value
        */
        long getLong();
        
       /**
        * Return as resolved optional long value.
        * @param value the default value
        * @return the long value
        */
        long getOptionalLong( long value );
        
       /**
        * Return as assigned non-optional byte value.
        * @return the byte value
        */
        byte getByte();
        
       /**
        * Return as resolved optional byte value.
        * @param value the default value
        * @return the byte value
        */
        byte getOptionalByte( byte value );
        
       /**
        * Return as assigned non-optional double value.
        * @return the double value
        */
        double getDouble();
        
       /**
        * Return as resolved optional double value.
        * @param value the default value
        * @return the double value
        */
        double getOptionalDouble( double value );
        
       /**
        * Return as assigned non-optional float value.
        * @return the float value
        */
        float getFloat();
        
       /**
        * Return as resolved optional float value.
        * @param value the default value
        * @return the float value
        */
        float getOptionalFloat( float value );
        
       /**
        * Return as assigned non-optional char value.
        * @return the char value
        */
        char getChar();
        
       /**
        * Return as resolved optional char value.
        * @param value the default value
        * @return the char value
        */
        char getOptionalChar( char value );
        
       /**
        * Return as assigned non-optional boolean value.
        * @return the boolean value
        */
        boolean getBoolean();
        
       /**
        * Return as resolved optional boolean value.
        * @param flag the default value
        * @return the boolean value
        */
        boolean getOptionalBoolean( boolean flag );
        
       /**
        * Return as assigned non-optional file value.
        * @return the file value
        */
        File getFile();
        
       /**
        * Return as resolved optional file value.
        * @param value the default value
        * @return the file value
        */
        File getOptionalFile( File value );
        
       /**
        * Return a non-optional temporary file value.
        * @return the temp file value
        */
        File getTempFile();
        
       /**
        * Return as assigned non-optional uri value.
        * @return the uri value
        */
        URI getURI();
        
       /**
        * Return a optional uri value.
        * @param value the default value
        * @return the uri value
        */
        URI getOptionalURI( URI value );
        
       /**
        * Return as assigned non-optional name.
        * @return the name
        */
        String getName();
        
       /**
        * Return as assigned non-optional path.
        * @return the path
        */
        String getPath();
        
       /**
        * Return as assigned non-optional array of names.
        * @return the name array
        */
        String[] getNames();
        
    }
    
    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------
    
   /**
    * Creation of a new object using a supplied logging channel.
    * @param logger the logging channel
    * @param context the deployment context
    */
    public Demo( final Logger logger, final Context context ) throws Exception
    {
        short s = 9;
        long l = 21;
        byte b = 3;
        double d = 0.001;
        float f = 3.142f;
        char c = '#';
        File file = new File( System.getProperty( "user.dir" ) );
        File tmp = new File( System.getProperty( "java.io.tmpdir" ) );
        URI uri = file.toURI();
        
        if( logger.isLoggable( Level.INFO ) )
        {
            logger.info( "color: " + context.getColor() );
            logger.info( "anotherColor: " + context.getAnotherColor() );
            logger.info( "integer: " + context.getInteger() );
            logger.info( "short: " + context.getShort() );
            logger.info( "long: " + context.getLong() );
            logger.info( "byte: " + context.getByte() );
            logger.info( "double: " + context.getDouble() );
            logger.info( "float: " + context.getFloat() );
            logger.info( "char: " + context.getChar() );
            logger.info( "boolean: " + context.getBoolean() );
            logger.info( "file: " + context.getFile() );
            logger.info( "temp: " + context.getTempFile() );
            logger.info( "uri: " + context.getURI() );
            logger.info( "name: " + context.getName() );
            logger.info( "path: " + context.getPath() );
            logger.info( "names: " + context.getNames().length );
            logger.info( "optionalColor: " + context.getOptionalColor( Color.BLUE ) );
            logger.info( "optionalInteger: " + context.getOptionalInteger( 42 ) );
            logger.info( "optionalShort: " + context.getOptionalLong( s ) );
            logger.info( "optionalLong: " + context.getOptionalLong( l ) );
            logger.info( "optionalByte: " + context.getOptionalByte( b ) );
            logger.info( "optionalDouble: " + context.getOptionalDouble( d ) );
            logger.info( "optionalFloat: " + context.getOptionalFloat( f ) );
            logger.info( "optionalChar: " + context.getOptionalChar( c ) );
            logger.info( "optionalBoolean: " + context.getOptionalBoolean( false ) );
            logger.info( "optionalFile: " + context.getOptionalFile( file ) );
            logger.info( "optionalURI: " + context.getOptionalURI( uri ) );
        }
    }
}
