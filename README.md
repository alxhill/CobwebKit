CobwebKit
=========

The worlds worst browser engine.

Currently it is able to:
* Read in and parse a HTML file
* Access any element by tag name
* Get styles from style tags on the page
* Parse styles into a selector and rules
* Apply styles through the HTML tree
* Renders text only nodes
* Renders with correct CSS styles

To do:
* Implement support for more rules
    * font stuff
        font-weight, font-family, font-style
    * margins - v.simple in this system
    * text-{align:center, decoration: underline}
    * display: block vs inline?
* Create a set of default styles

* Add support for wrapped text (for the p tag)
* Selector specificity
* Add support for img tags
* Make a distinction between block and inline elements

* Make the parser able to have text and nodes alongside each other
* Add support for inline styles
* Add support for link tags
* Add support for a tags/links?
