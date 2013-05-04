import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class Node
{
    protected HashMap<String, String> attributes = new HashMap<String, String>();
    protected HashMap<String, String> rules = new HashMap<String, String>();

    // stores the name of the current tag for use in parsing
    protected String name;

    // raw html data to parse
    protected String data;

    // list of nodes to store as children
    ArrayList<Node> nodes = null;

    // or just store text
    String text = null;

    public Node(String tag, String data)
    {
        //set always invalid defaults for applying styles against
        attributes.put("tag", tag);
        attributes.put("id", "");
        attributes.put("class", "");
        this.name = tag;
        this.data = data;
    }

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

    public String parse()
    {
        parseAttributes();
        return parseContent();
    }

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

    public void applyCSS(Style style)
    {
        HashMap<Style.SelectorType, String> styleArray = style.getCurrentSelector();
        Boolean applyToThis = true;
        for (Map.Entry<Style.SelectorType, String> styleEntry : styleArray.entrySet())
        {
            String type = styleEntry.getKey().toString().toLowerCase();
            String value = styleEntry.getValue();

            // if any of the parts don't match, don't apply to this element
            if (!attributes.get(type).equals(value)) applyToThis = false;
        }

        if (style.shouldApplyStyle() && applyToThis)
        {
            addRules(style.rules);
            if (nodes != null)
            {
                for (Node node : nodes)
                {
                    node.addRules(style.rules);
                }
            }
        }
        else
        {
            if (nodes != null)
            {
                if (applyToThis) style.next();
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
