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

package org.acme.test;

import java.io.File;
import java.net.URI;

import junit.framework.TestCase;

import net.dpml.lang.Part;

import org.acme.demo.Demo;

/**
 * Deployment of the demo component.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DemoTestCase extends TestCase
{
   /**
    * Test component deployment.
    * @exception Exception if an error occurs
    */
    public void testComponent() throws Exception
    {
        URI uri = getPartURI();
        Part part = Part.load( uri );
        Demo demo = (Demo) part.getContent();
    }
    
    private URI getPartURI() throws Exception
    {
        String path = System.getProperty( "project.deliverable.part.filename" );
        File file = new File( path );
        return file.toURI();
    }
}
