/*
 * WizardException.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 28/08/2001
 */

package net.osm.shell.control.wizard;

import net.osm.shell.ShellException;

/**
 * General exception type for exceptions raised by a wizard.
 * @version 1.0
 */
public class WizardException extends ShellException 
{

    /**
     * Constructs an <code>WizardException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public WizardException( String msg ) 
    {
        super(msg);
    }

    /**
     * Constructs an <code>WizardException</code> with the specified detail message
     * and causal exception.
     * @param msg the detail message.
     * @param cause the exception causing the problem.
     */
    public WizardException( String msg, Throwable ex ) 
    {
        super(msg, ex);
    }
}

