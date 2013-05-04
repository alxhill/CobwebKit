import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

class Style {

    public enum SelectorType {
        ID, CLASS, TAG
    }

    public String selector;
    // we split each selector by spaces to deal with heirarchy, storing an
    // array of selectortypes mapped to their name to allow rules like tag.class#id
    public ArrayList<HashMap<SelectorType,String>> selectorList;
    int currentSelector = 0;

    public HashMap<String, String> rules = new HashMap<String,String>();

    public Style(String selector)
    {
        this.selector = selector;
        selectorList = new ArrayList<HashMap<SelectorType,String>>();

        for (String singleSelector : selector.split(" "))
        {
            HashMap<SelectorType,String> ruleMap = new HashMap<SelectorType,String>();
            for (String singleTypeSelector : singleSelector.trim().split("(?=[.#])"))
            {
                switch (singleTypeSelector.charAt(0))
                {
                    case '.':
                        ruleMap.put(SelectorType.CLASS, singleTypeSelector.substring(1));
                        break;
                    case '#':
                        ruleMap.put(SelectorType.ID, singleTypeSelector.substring(1));
                        break;
                    default:
                        ruleMap.put(SelectorType.TAG, singleTypeSelector);
                }
            }
            selectorList.add(ruleMap);
        }
    }

    public void addRule(String rule)
    {
        String[] ruleSplit = rule.split(":");
        rules.put(ruleSplit[0].trim(), ruleSplit[1].trim());
    }

    public HashMap<SelectorType,String> getCurrentSelector()
    {
        return selectorList.get(currentSelector);
    }

    public Boolean shouldApplyStyle()
    {
        return currentSelector == selectorList.size()-1;
    }

    public void next()
    {
        currentSelector++;
    }

    public String toString()
    {
        String css = selector + " {\n";
        for (Map.Entry<String, String> rule : rules.entrySet())
        {
            css += "\t" + rule.getKey() +": " + rule.getValue()+";\n";
        }
        css += "}";
        return css;
    }
}
