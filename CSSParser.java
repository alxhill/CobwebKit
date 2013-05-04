import java.util.ArrayList;

public class CSSParser {

    /**
     * Creates a list of Styles (which have a selector and a rule)
     * from a string of CSS
     * @param  css String of raw CSS
     * @return     List of all styles as in original order.
     */
    public static ArrayList<Style> parse(String css)
    {
        ArrayList<Style> styleList = new ArrayList<Style>();
        String[] styles = css.split("}");
        for (String style : styles)
        {
            String selector = style.substring(0, style.indexOf('{')).trim();
            String rules = style.substring(style.indexOf('{')+1).trim();

            for (String singleSelector : selector.split(","))
            {
                Style newStyle = new Style(singleSelector.trim());

                for (String rule : rules.split(";"))
                {
                    newStyle.addRule(rule.trim());
                }

                styleList.add(newStyle);
            }
        }
        return styleList;
    }

}
