/*
 * Copyright 2005 Stephen McConnell
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dpml.depot.desktop;

import javax.swing.LookAndFeel;

import com.jgoodies.looks.BorderStyle;
import com.jgoodies.looks.FontSizeHints;
import com.jgoodies.looks.HeaderStyle;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticTheme;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.Silver;

/** 
 * Look and feel settings.
 */
public final class Settings
{
    private LookAndFeel m_selectedLookAndFeel;
    private PlasticTheme m_selectedTheme;
    private Boolean m_useSystemFonts;
    private FontSizeHints m_fontSizeHints;
    private boolean m_useNarrowButtons;
    private boolean m_tabIconsEnabled;
    private Boolean m_popupDropShadowEnabled;
    private String m_plasticTabStyle;
    private boolean m_plasticHighContrastFocusEnabled;
    private HeaderStyle m_menuBarHeaderStyle;
    private BorderStyle m_menuBarPlasticBorderStyle;
    private BorderStyle m_menuBarWindowsBorderStyle;
    private Boolean m_menuBar3DHint;
    private HeaderStyle m_toolBarHeaderStyle;
    private BorderStyle m_toolBarPlasticBorderStyle;
    private BorderStyle m_toolBarWindowsBorderStyle;
    private Boolean m_toolBar3DHint;

    // Instance Creation ******************************************************

    private Settings() 
    {
    }

    public static Settings createDefault()
    {
        Settings settings = new Settings();
        settings.setSelectedLookAndFeel(new PlasticXPLookAndFeel());
        //settings.setSelectedTheme(PlasticLookAndFeel.createMyDefaultTheme());
        settings.setSelectedTheme( new Silver() );
        settings.setUseSystemFonts(Boolean.TRUE);
        settings.setFontSizeHints(FontSizeHints.MIXED);
        settings.setUseNarrowButtons(false);
        settings.setTabIconsEnabled(true);
        settings.setPlasticTabStyle(PlasticLookAndFeel.TAB_STYLE_DEFAULT_VALUE);
        settings.setPlasticHighContrastFocusEnabled(false);
        settings.setMenuBarHeaderStyle(null);
        settings.setMenuBarPlasticBorderStyle(null);
        settings.setMenuBarWindowsBorderStyle(null);
        settings.setMenuBar3DHint(null);
        settings.setToolBarHeaderStyle(null);
        settings.setToolBarPlasticBorderStyle(null);
        settings.setToolBarWindowsBorderStyle(null);
        settings.setToolBar3DHint(null);
        return settings;
    }

    public FontSizeHints getFontSizeHints() 
    {
        return m_fontSizeHints;
    }

    public void setFontSizeHints( FontSizeHints fontSizeHints ) 
    {
        m_fontSizeHints = fontSizeHints;
    }

    public Boolean getMenuBar3DHint() 
    {
        return m_menuBar3DHint;
    }

    public void setMenuBar3DHint( Boolean menuBar3DHint ) 
    {
        m_menuBar3DHint = menuBar3DHint;
    }

    public HeaderStyle getMenuBarHeaderStyle() 
    {
        return m_menuBarHeaderStyle;
    }

    public void setMenuBarHeaderStyle(HeaderStyle menuBarHeaderStyle) 
    {
        m_menuBarHeaderStyle = menuBarHeaderStyle;
    }

    public BorderStyle getMenuBarPlasticBorderStyle()
    {
        return m_menuBarPlasticBorderStyle;
    }

    public void setMenuBarPlasticBorderStyle(BorderStyle menuBarPlasticBorderStyle) 
    {
        m_menuBarPlasticBorderStyle = menuBarPlasticBorderStyle;
    }

    public BorderStyle getMenuBarWindowsBorderStyle() 
    {
        return m_menuBarWindowsBorderStyle;
    }

    public void setMenuBarWindowsBorderStyle(BorderStyle menuBarWindowsBorderStyle) 
    {
        m_menuBarWindowsBorderStyle = menuBarWindowsBorderStyle;
    }

    public Boolean isPopupDropShadowEnabled() 
    {
        return m_popupDropShadowEnabled;
    }

    public void setPopupDropShadowEnabled( Boolean popupDropShadowEnabled ) 
    {
        m_popupDropShadowEnabled = popupDropShadowEnabled;
    }

    public boolean isPlasticHighContrastFocusEnabled() 
    {
        return m_plasticHighContrastFocusEnabled;
    }

    public void setPlasticHighContrastFocusEnabled( boolean plasticHighContrastFocusEnabled ) 
    {
        m_plasticHighContrastFocusEnabled = plasticHighContrastFocusEnabled;
    }

    public String getPlasticTabStyle() 
    {
        return m_plasticTabStyle;
    }

    public void setPlasticTabStyle( String plasticTabStyle ) 
    {
        m_plasticTabStyle = plasticTabStyle;
    }

    public LookAndFeel getSelectedLookAndFeel() 
    {
        return m_selectedLookAndFeel;
    }

    public void setSelectedLookAndFeel( LookAndFeel selectedLookAndFeel ) 
    {
        m_selectedLookAndFeel = selectedLookAndFeel;
    }

    public PlasticTheme getSelectedTheme() 
    {
        return m_selectedTheme;
    }

    public void setSelectedTheme(PlasticTheme selectedTheme) 
    {
        m_selectedTheme = selectedTheme;
    }

    public boolean isTabIconsEnabled() 
    {
        return m_tabIconsEnabled;
    }

    public void setTabIconsEnabled(boolean tabIconsEnabled) 
    {
        m_tabIconsEnabled = tabIconsEnabled;
    }

    public Boolean getToolBar3DHint() 
    {
        return m_toolBar3DHint;
    }

    public void setToolBar3DHint(Boolean toolBar3DHint) 
    {
        m_toolBar3DHint = toolBar3DHint;
    }

    public HeaderStyle getToolBarHeaderStyle() 
    {
        return m_toolBarHeaderStyle;
    }

    public void setToolBarHeaderStyle(HeaderStyle toolBarHeaderStyle) 
    {
        m_toolBarHeaderStyle = toolBarHeaderStyle;
    }

    public BorderStyle getToolBarPlasticBorderStyle() 
    {
        return m_toolBarPlasticBorderStyle;
    }

    public void setToolBarPlasticBorderStyle(BorderStyle toolBarPlasticBorderStyle) 
    {
        m_toolBarPlasticBorderStyle = toolBarPlasticBorderStyle;
    }

    public BorderStyle getToolBarWindowsBorderStyle() 
    {
        return m_toolBarWindowsBorderStyle;
    }

    public void setToolBarWindowsBorderStyle(BorderStyle toolBarWindowsBorderStyle) 
    {
        m_toolBarWindowsBorderStyle = toolBarWindowsBorderStyle;
    }

    public boolean isUseNarrowButtons()
    {
        return m_useNarrowButtons;
    }

    public void setUseNarrowButtons( boolean useNarrowButtons ) 
    {
        m_useNarrowButtons = useNarrowButtons;
    }

    public Boolean isUseSystemFonts()
    {
        return m_useSystemFonts;
    }

    public void setUseSystemFonts( Boolean useSystemFonts ) 
    {
        m_useSystemFonts = useSystemFonts;
    }

}