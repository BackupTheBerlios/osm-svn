
var xwebContentPanel = 'main';
var xwebHeaderPanel = 'header';

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
