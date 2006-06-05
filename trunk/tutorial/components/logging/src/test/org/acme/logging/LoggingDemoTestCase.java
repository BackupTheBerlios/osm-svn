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

package org.acme.logging;

import java.io.File;
import java.net.URI;

import junit.framework.TestCase;

import net.dpml.logging.Logger;

import net.dpml.component.Controller;
import net.dpml.component.Component;
import net.dpml.component.Provider;

/**
 * Validation exception testcase verifies the ValidationException and 
 * ValidationException#Issue class implementations.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class LoggingDemoTestCase extends TestCase
{
    private static final File TARGET = new File( System.getProperty( "project.target.dir" ) );
    private static final File TEST = new File( TARGET, "test" );
    private static final File LOGGING = new File( TEST, "logging.properties" );

    static
    {
        System.setProperty( 
          "dpml.logging.config", " file:" + LOGGING.toString() );
        System.out.println( LOGGING.toString() );
        System.setProperty( 
          "java.util.logging.config.class", 
          "net.dpml.util.ConfigurationHandler" );
    }
    
    private static final Controller CONTROLLER = Controller.STANDARD;
    private static final File PARTS = new File( TARGET, "deliverables/parts" );
    private static final File PATH = new File( PARTS, "@PATH@" );
    
   /**
    * Test the logging channel.
    * @exception Exception if an error occurs
    */
    public void testLogger() throws Exception
    {
        URI uri = PATH.toURI();
        Component component = CONTROLLER.createComponent( uri );
        Provider provider = component.getProvider();
        Demo demo = (Demo) provider.getValue( false );
    }
}
