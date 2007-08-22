
var xwebContentPanel = 'main';
var xwebHeaderPanel = 'header';


 /*
  Set the url of the content panel to the supplied value.
  */

  function setContentLocation( url ) 
  {
     this.frames[xwebContentPanel].window.location=url;
  };

 /*
  Set the url of the content panel to the supplied value.
  */

  function setLocations( header, body ) 
  {
     this.frames[xwebContentPanel].window.location=body;
     this.frames[xwebHeaderPanel].window.location=header;
  };

 /*
  Set the url of the header panel to the supplied value.
  Somehow we need this to check if the requested document is different to 
  what has already been loaded (as it currently reloads even ifg its the 
  same page because I'm calling this from onLoad in the content page)
  */

  function setHeaderLocation( url ) 
  {
     this.frames[xwebHeaderPanel].window.location=url;
  };

 /*
  For a row element in a table, set the background color value to 
  a common value - needs to be updated so that it links to the style 
  sheet
  */

  function setFocus(el) {
	with (el.style) {
		background = "lightsteelblue";
	}
  }

 /*
  For a row element in a table, set the background color to the 
  same value as the background of the panel - needs to be updated
  to get a CSS value or current window background value
  */

  function releaseFocus(el) {
	with (el.style) {
		background = "white";
	}
  }
