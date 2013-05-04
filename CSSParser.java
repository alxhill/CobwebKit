import java.util.ArrayList;

public class CSSParser {

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
