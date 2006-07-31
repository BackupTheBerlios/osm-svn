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

package net.osm.http;

import junit.framework.TestCase;

/**
 * ServletEntryTestCase.
 */
public class ServletEntryTestCase extends TestCase
{
    private static final String NAME = "test";
    private static final String PATH = "abc";
    
    private ServletEntry m_entry;
    
    /**
     * Setup the servlet entry.
     * @throws Exception if an error occurs
     */
    public void setUp() throws Exception
    {
        m_entry = new ServletEntry( NAME, PATH );
    }
    
    
   /**
    * Test name.
    * @throws Exception if an error occurs during test execution
    */
    public void testName() throws Exception
    {
        assertEquals( "name", NAME, m_entry.getName() );
    }

   /**
    * Test path.
    * @throws Exception if an error occurs during test execution
    */
    public void testPath() throws Exception
    {
        assertEquals( "path", PATH, m_entry.getPath() );
    }

}

