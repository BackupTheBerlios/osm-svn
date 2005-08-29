
package org.acme.demo;

public class Demo
{
    public static final Shape REGULAR = new Shape( 0 );
    public static final Shape IRREGULAR = new Shape( 1 );

    public float divide( float a, float b, Shape shape )
    {
        return a/b;
    }

    public static class Shape
    {
        private int m_value;

        private Shape( int value )
        {
            m_value = value;
        }

        public boolean equals( Object other )
        {
            if( null == other )
            {
                return false;
            }
            else if( other instanceof Shape )
            {
                Shape shape = (Shape) other;
                return shape.m_value == m_value;
            }
            else
            {
                return false;
            }
        }
    }

}
