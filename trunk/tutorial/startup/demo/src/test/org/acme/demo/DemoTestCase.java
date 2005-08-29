
package org.acme.demo;

import junit.framework.TestCase;

/**
 * Test the demo class..
 */
public class DemoTestCase extends TestCase
{
   /**
    * Test the demo class.
    */
    public void testDemo() throws Exception
    {
        float a = 123;
        float b = 0.5f;
        Demo demo = new Demo();
        float result = demo.divide( a, b, Demo.REGULAR );
        System.out.println( "result: " + result );
    }
}
