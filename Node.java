import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.awt.*;
import java.awt.font.TextAttribute;

public class Node
{
    protected HashMap<String, String> attributes = new HashMap<String, String>();
    protected HashMap<String, String> rules = new HashMap<String, String>();

    class Rule {
        String name;
        String value;
        int specificity;

        Rule(String name, String value, int specificity)
        {
            this.name = name;
            this.value = value;
            this.specificity = specificity;
        }
    }

    // stores the name of the current tag for use in parsing
    protected String name;

    // raw html data to parse
    protected String data;

    // list of nodes to store as children
    ArrayList<Node> nodes = null;

    // or just store text
    String text = null;

    int margin[] = new int[4];

    public Node(String tag, String data)
    {
        //set blank defaults for applying styles against
        attributes.put("tag", tag);
        attributes.put("id", "");
        attributes.put("class", "");
        this.name = tag;
        this.data = data;
    }

    /**
     * uses parseAttributes and parseContent to process the html string.
     * Before: <div class="blue"><p>...</p></div><h2>...
     * Returns: <h2>...
     */
    public String parse()
    {
        parseAttributes();
        return parseContent();
    }

    /**
     * Parses out attributes and puts them into the attributes map
     * Before the call, data will be <div id="blue"><p>...
     * After the function call, data will have had the initial tag
     * removed: <p>... will be stored in data.
     */
    public void parseAttributes()
    {
        // get rid of the tag name and opening bracket
        data = data.substring(name.length()+1);

        if (data.indexOf('>') < 1)
        {
            data = data.substring(1);
            return;
        }

        String attributeString = data.substring(1, data.indexOf('>'));

        String[] attrs = attributeString.split(" ");

        for (String attr : attrs)
        {
            String[] props = attr.split("=");
            String key = props[0];
            String value = props[1];
            if (value.startsWith("\"") && value.endsWith("\""))
                value = value.substring(1, value.length()-1);

            attributes.put(key, value);
        }

        data = data.substring(attributeString.length()+2);
    }

    /**
     * takes a string and turns it into nodes
     * accepts the data with the start tag removed,
     * returns the string with any content it's parsed
     * removed.
     * Before: <p>...</p></div><h2>...
     * Returns: <h2>...
     */
    public String parseContent()
    {
        String currentText = data;
        if (currentText.charAt(0) == '<')
        {
            nodes = new ArrayList<Node>();
            String tag;

            while (!(tag = HTMLParser.getTag(currentText)).equals(""))
            {
                Node newNode = new Node(tag, currentText);
                currentText = newNode.parse();
                nodes.add(newNode);
            }
            return currentText.substring(name.length()+3);
        }
        else
        {
            String endTag = "</"+name+">";

            int endTagIndex = data.indexOf(endTag);

            // save the contents minus the tags into the text variable
            text = data.substring(0, endTagIndex);
            return data.substring(endTagIndex + endTag.length());
        }

    }

    /**
     * Find all nodes with given tag
     * @param  tag the tag to find
     * @return     A list of Nodes with the specified tag.
     */
    public ArrayList<Node> findNodesByTag(String tag)
    {
        if (nodes != null)
        {
            ArrayList<Node> found = new ArrayList<Node>();

            for (Node node : nodes)
            {
                if (node.name.equals(tag)) found.add(node);
                if (node.nodes != null) found.addAll(node.findNodesByTag(tag));
            }
            return found;
        }
        else return null;
    }

    /**
     * Applies CSS to this element and it's children,
     * or just passes the rule on depending on whether it matches
     * @param style The Style object to apply
     */
    public void applyCSS(Style style)
    {
        HashMap<Style.SelectorType, String> styleArray = style.getCurrentSelector();
        Boolean matchesThis = true;
        for (Map.Entry<Style.SelectorType, String> styleEntry : styleArray.entrySet())
        {
            String type = styleEntry.getKey().toString().toLowerCase();
            String value = styleEntry.getValue();

            // if any of the parts don't match, don't apply to this element
            if (!attributes.get(type).equals(value)) matchesThis = false;
        }

        if (style.shouldApplyStyle() && matchesThis)
        {
            addRules(style.rules);
        }
        else
        {
            if (nodes != null)
            {
                if (matchesThis) style.next();
                for (Node node : nodes)
                {
                    node.applyCSS(style);
                }
            }
        }
    }

    public void addRules(HashMap<String,String> rules)
    {
        this.rules.putAll(rules);
        if (nodes != null)
        {
            for (Node node : nodes)
            {
                node.addRules(rules);
            }
        }
    }

    /**
     * Render the current element, with the canvas moved to the right place for
     * the next element
     * @param g The graphics context to render to
     */
    public void render(Graphics2D g)
    {


        // render this if text or the nodes if not.
        if (nodes ==  null)
        {
            Graphics2D g2 = (Graphics2D) g.create();
            // apply the current CSS styles to the new graphics object
            for (Map.Entry<String, String> rule : rules.entrySet())
            {
                applyRule(g2, rule.getKey(), rule.getValue());
            }
            Dimension size = getSize(g2);
            g2.translate(margin[0], size.getHeight()+margin[1]);
            g2.drawString(text, 0, 0);
            g2.dispose();
            g.translate(margin[1] + margin[2], size.getHeight()+margin[3]);
        }
        else
        {
            for (Node node : nodes)
            {
                node.render(g);
            }
        }

    }

    /**
     * returns the size of this element, or of its children.
     * @param  g graphics context for measurement
     * @return   Dimension representing the size of this element
     *
     */
    public Dimension getSize(Graphics2D g)
    {
        Dimension d = new Dimension();
        if (nodes == null)
        {
            FontMetrics metrics = g.getFontMetrics();
            d.setSize(metrics.stringWidth(text), metrics.getHeight());
        }
        else
        {
            int totalHeight = 0;
            int maxWidth = 0;
            for (Node node : nodes)
            {
                Dimension nd = node.getSize(g);
                totalHeight += nd.getHeight();
                maxWidth = Math.max(maxWidth, (int) nd.getWidth());
            }
            d.setSize(maxWidth, totalHeight);
        }
        return d;
    }

    /* enum of all supported rules */
    private enum RuleType {
        COLOR,
        FONTSIZE, FONTFAMILY, FONTWEIGHT, FONTSTYLE,
        MARGINTOP, MARGINLEFT, MARGINRIGHT, MARGINBOTTOM,
        TEXTALIGN, TEXTDECORATION
    }

    /**
     * Apply the contents of a specific rule to the graphics context.
     * @param g        graphics context to apply rule to
     * @param property the name of the rule to apply - e.g color
     * @param value    the value of the rule - e.g blue or #BADA55
     */
    public void applyRule(Graphics2D g, String property, String value)
    {
        RuleType name;
        try {
            name = RuleType.valueOf(property.replace("-", "").toUpperCase());
        }
        catch (Exception e) {
            // rule was invalid, so nothing happens
            return;
        }
        switch (name)
        {
            case COLOR:
                Color c;
                try
                {
                    if (value.startsWith("#"))
                        c = Color.decode(value);
                    else
                        c = (Color) Class.forName("java.awt.Color").getField(value.toLowerCase()).get(null);
                }
                catch (Exception e)
                {
                    // if it's an invalid color, it gets ignored
                    c = g.getColor();
                }
                g.setColor(c);
                break;
            case FONTSIZE:
                float size;
                // value ends in px, because who uses anything else?
                size = Float.parseFloat(value.substring(0, value.indexOf("px")));
                g.setFont(g.getFont().deriveFont(size));
                break;
            case FONTFAMILY:
                HashMap<TextAttribute, String> newFont = new HashMap<TextAttribute, String>();
                if (value.equals("serif"))
                    newFont.put(TextAttribute.FAMILY, Font.SERIF);
                else
                    newFont.put(TextAttribute.FAMILY, Font.SANS_SERIF);
                g.setFont(g.getFont().deriveFont(newFont));
                break;
            case FONTWEIGHT:
                if (value.equals("bold"))
                    g.setFont(g.getFont().deriveFont(Font.BOLD));
                else
                    g.setFont(g.getFont().deriveFont(Font.PLAIN));
                break;
            case FONTSTYLE:
                if (value.equals("italic"))
                    g.setFont(g.getFont().deriveFont(Font.ITALIC));
                else if (value.equals("normal"))
                    g.setFont(g.getFont().deriveFont(Font.PLAIN));
                break;
            case MARGINLEFT:
                margin[0] = Integer.parseInt(value.substring(0, value.indexOf("px")));
                break;
            case MARGINTOP:
                margin[1] = Integer.parseInt(value.substring(0, value.indexOf("px")));
                break;
            case MARGINRIGHT:
                margin[2] = Integer.parseInt(value.substring(0, value.indexOf("px")));
                break;
            case MARGINBOTTOM:
                margin[3] = Integer.parseInt(value.substring(0, value.indexOf("px")));
                break;
            case TEXTALIGN:
                break;
            case TEXTDECORATION:
                if (value.equals("underline"))
                {
                    HashMap<TextAttribute, Integer> underlineFont = new HashMap<TextAttribute, Integer>();
                    underlineFont.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                    g.setFont(g.getFont().deriveFont(underlineFont));
                }
                break;
        }
    }

    public String toString()
    {
        String stringValue = "("+attributes;
        if (rules.size() > 0) stringValue += "["+rules+"]";
        stringValue += ":\n\t";
        stringValue += nodes == null ? text : String.valueOf(nodes);
        stringValue += "\n)";
        return stringValue;
    }

}
