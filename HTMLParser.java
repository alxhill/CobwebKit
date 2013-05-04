public class HTMLParser {

    public static Node parse(String html)
    {
        String tag = HTMLParser.getTag(html);
        Node node = new Node(tag, html);
        node.parse();
        return node;
    }

    public static String getTag(String data)
    {
        // return "" if this isn't a tag or is a closing tag.
        if (data.charAt(0) != '<' || data.charAt(1) == '/') return "";

        int spaceIndex = data.indexOf(' ');
        int bracketIndex = data.indexOf('>');
        int index = (spaceIndex > bracketIndex) || (spaceIndex < 0) ? bracketIndex : spaceIndex;
        return data.substring(1, index);
    }
}

