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
 * ServletHolderTestCase.
 */
public class ServletHolderTestCase extends TestCase
{
    private static final String NAME = "test";
    private static final String CLASSNAME = ServletHolderTestCase.class.getName();
    
    private ServletHolder m_holder;
    
    /**
     * Setup a servlet holder.
     * @throws Exception if an error occurs during test execution
     */
    public void setUp() throws Exception
    {
        m_holder = new ServletHolder( NAME, CLASSNAME );
    }
    
    
   /**
    * Test name.
    * @throws Exception if an error occurs during test execution
    */
    public void testName() throws Exception
    {
        assertEquals( "name", NAME, m_holder.getName() );
    }

   /**
    * Test classname.
    * @throws Exception if an error occurs during test execution
    */
    public void testClassname() throws Exception
    {
        assertEquals( "classname", CLASSNAME, m_holder.getClassName() );
    }

}

