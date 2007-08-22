/*
 * @(#)DialogCallbackHandler.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 6/02/2001
 */

package net.osm.vault;

import java.awt.Font;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * <p>
 * Dialog window to query the user for answers to authentication questions.
 * @see javax.security.auth.callback
 */
final class DialogCallbackHandler 
implements CallbackHandler
{

    //======================================================
    // static
    //======================================================

    private static final int JPasswordFieldLen = 8 ;
    public static Font font = new Font("Dialog", 0, 11);


    /* 
    The parent window, or null if using the default parent 
    */
    private Component parentComponent;


    //======================================================
    // Constructor
    //======================================================

   /**
    * Creates a callback dialog with the default parent window.
    */
    public DialogCallbackHandler() 
    { 
    }

   /**
    * Creates a callback dialog and specify the parent window.
    *
    * @param parentComponent the parent window
    * (a null value implies the default parent)
    */
    public DialogCallbackHandler(Component parentComponent) 
    {
        this.parentComponent = parentComponent;
    }
    
    //======================================================
    // CallbackHandler
    //======================================================

   /*
    * An interface for recording actions to carry out if the user
    * clicks OK for the dialog.
    */
    private static interface Action {
     void perform();
    }

    /**
     * Handles the specified set of callbacks.
     *
     * @param callbacks the callbacks to handle
     * @exception UnsupportedCallbackException if the callback is not an
     * instance  of NameCallback or PasswordCallback
     */

    public void handle(Callback[] callbacks ) throws UnsupportedCallbackException
    {

        //
        // Collect messages to display in the dialog 
        //
        final List messages = new ArrayList(3);
        String banner = "Please login";

        // 
        // Collection actions to perform if the user clicks OK
        //

        final List okActions = new ArrayList(2);

        ConfirmationInfo confirmation = new ConfirmationInfo();

        for (int i = 0; i < callbacks.length; i++) 
        {
            if (callbacks[i] instanceof TextOutputCallback) 
            {
                TextOutputCallback tc = (TextOutputCallback) callbacks[i];

                switch (tc.getMessageType()) 
                {
                  case TextOutputCallback.INFORMATION:
                    break;
                  case TextOutputCallback.WARNING:
                    confirmation.messageType = JOptionPane.WARNING_MESSAGE;
                    break;
                  case TextOutputCallback.ERROR:
                    confirmation.messageType = JOptionPane.ERROR_MESSAGE;
                    break;
                  default:
                    throw new UnsupportedCallbackException(
                    callbacks[i], "Unrecognized message type");
                }

                String message = tc.getMessage();
                if (message != null) 
                {
                    banner = message;
                }

            }
            else if (callbacks[i] instanceof NameCallback) 
            {
                final NameCallback nc = (NameCallback) callbacks[i];

                JLabel prompt = new JLabel(nc.getPrompt());
                prompt.setFont( font );

                final JTextField name = new JTextField( );
                name.setFont( font );
                String defaultName = nc.getDefaultName();
                if (defaultName != null) 
                {
                    name.setText(defaultName);
                }
                else
                {
                    name.setText( System.getProperty("user.name"));
                }
        
                //
                // Put the prompt and name in a horizontal box, 
                // and add that to the set of messages.
                //

                Box namePanel = Box.createHorizontalBox();
                namePanel.add(prompt);
                namePanel.add(name);
                messages.add(namePanel);

                // Store the name back into the callback if OK

                okActions.add(
                  new Action() {
                      public void perform() {
                        nc.setName(name.getText());
                      }
                  }
                );
      
            } 
            else if (callbacks[i] instanceof PasswordCallback) 
            {
                final PasswordCallback pc = (PasswordCallback) callbacks[i];

                JLabel prompt = new JLabel(pc.getPrompt());
                prompt.setFont( font );

                final JPasswordField password = new JPasswordField(JPasswordFieldLen);

                if (!pc.isEchoOn()) 
                {
                    password.setEchoChar('*');
                }

                Box passwordPanel = Box.createHorizontalBox();
                passwordPanel.add(prompt);
                passwordPanel.add(password);
                messages.add(passwordPanel);
  
                okActions.add( 
                  new Action() 
                  {
                      public void perform() 
                      {
                          pc.setPassword(password.getPassword());
                      }
                  }
                );

            } 
            else if (callbacks[i] instanceof ConfirmationCallback) 
            {
                confirmation.setCallback
                ((ConfirmationCallback) callbacks[i]);

            }
            else 
            {
                throw new UnsupportedCallbackException(
                callbacks[i], "Unrecognized Callback");
            }
        }

        messages.add(0, banner);

        // Display the dialog 

        int result = JOptionPane.showOptionDialog(
          parentComponent,
          messages.toArray(),
          "Login",
          confirmation.optionType,
          confirmation.messageType,
          null,                // icon
          confirmation.options,  // options
          confirmation.initialValue); // initialValue

        // Perform the OK actions 

        if (result == JOptionPane.OK_OPTION || result == JOptionPane.YES_OPTION )
        {
            Iterator iterator = okActions.iterator();
            while (iterator.hasNext()) 
            {
                ((Action) iterator.next()).perform();
            }
        }
        confirmation.handleResult(result);
    }

    /*
     * Provides assistance with translating between JAAS and Swing
     * confirmation dialogs.
     */
    private static class ConfirmationInfo 
    {

        private int[] translations;

        int optionType = JOptionPane.OK_CANCEL_OPTION;
        Object[] options = null;
        Object initialValue = null;

        int messageType = JOptionPane.QUESTION_MESSAGE;

        private ConfirmationCallback callback;

        /* Set the confirmation callback handler */
        void setCallback(ConfirmationCallback callback)
          throws UnsupportedCallbackException
        {
            this.callback = callback;

            int confirmationOptionType = callback.getOptionType();
            switch (confirmationOptionType) 
            {
              case ConfirmationCallback.YES_NO_OPTION:
                optionType = JOptionPane.YES_NO_OPTION;
                translations = new int[] {
                    JOptionPane.YES_OPTION, ConfirmationCallback.YES,
                    JOptionPane.NO_OPTION, ConfirmationCallback.NO,
                    JOptionPane.CLOSED_OPTION, ConfirmationCallback.NO
                };
                break;
              case ConfirmationCallback.YES_NO_CANCEL_OPTION:
                optionType = JOptionPane.YES_NO_CANCEL_OPTION;
                translations = new int[] {
                    JOptionPane.YES_OPTION, ConfirmationCallback.YES,
                    JOptionPane.NO_OPTION, ConfirmationCallback.NO,
                    JOptionPane.CANCEL_OPTION, ConfirmationCallback.CANCEL,
                    JOptionPane.CLOSED_OPTION, ConfirmationCallback.CANCEL
                };
                break;
              case ConfirmationCallback.OK_CANCEL_OPTION:
                optionType = JOptionPane.OK_CANCEL_OPTION;
                translations = new int[] {
                    JOptionPane.OK_OPTION, ConfirmationCallback.OK,
                    JOptionPane.CANCEL_OPTION, ConfirmationCallback.CANCEL,
                    JOptionPane.CLOSED_OPTION, ConfirmationCallback.CANCEL
                };
                break;
              case ConfirmationCallback.UNSPECIFIED_OPTION:
                options = callback.getOptions();

                translations = new int[] {
                    JOptionPane.CLOSED_OPTION, callback.getDefaultOption()
                };
                break;
              default:
                throw new UnsupportedCallbackException(
                    callback,
                    "Unrecognized option type: " + confirmationOptionType);
            }

            int confirmationMessageType = callback.getMessageType();
            switch (confirmationMessageType) {
              case ConfirmationCallback.WARNING:
                messageType = JOptionPane.WARNING_MESSAGE;
                break;
              case ConfirmationCallback.ERROR:
                messageType = JOptionPane.ERROR_MESSAGE;
                break;
              case ConfirmationCallback.INFORMATION:
                messageType = JOptionPane.INFORMATION_MESSAGE;
                break;
              default:
                throw new UnsupportedCallbackException(
                    callback,
                    "Unrecognized message type: " + confirmationMessageType);
            }
        }

       /**
        * Process the result returned by the Swing dialog 
        */
        void handleResult(int result) 
        {
            if (callback == null) 
            {
                return;
            }

            for (int i = 0; i < translations.length; i += 2) 
            {
                if (translations[i] == result) 
                {
                    result = translations[i + 1];
                    break;
                }
            }
            callback.setSelectedIndex(result);
        }
    }
}
