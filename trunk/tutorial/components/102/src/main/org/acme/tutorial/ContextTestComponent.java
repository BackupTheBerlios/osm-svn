/*
 * Copyright 2005 Stephen J. McConnell.
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

package org.acme.tutorial;

import java.awt.Color;
import java.io.File;
import java.net.URI;

/**
 * Component used for context entry testing. The main purpose of this 
 * class is to exercise the spectrum of content access operations.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ContextTestComponent
{
    //------------------------------------------------------------------
    // concerns
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
        * Return the assigned color.
        * @param color the default color value
        * @return the color value 
        */
        Color getOptionalColor( Color color );
        
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
        File getFile( File value );
        
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
    }

    private final Context m_context;
    
    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------

   /**
    * Creation of a new context test component.
    * @param context the component direven context criteria
    */
    public ContextTestComponent( final Context context )
    {
        m_context = context;
    }
    
   /**
    * Return the container assigned context instance.
    * @return the assigned context 
    */
    public Context getContext() // for testcase
    {
        return m_context;
    }
}
