import java.util.ArrayList;
import java.io.*;

public class Program {

    Node htmlNodes;
    String windowTitle;
    ArrayList<Style> styleList;

    public static void main(String[] args)
    {
        String filename = args[0];
        Program program = new Program();
        Node n = program.loadData(filename);
        program.getHTMLData();
        program.setStyles();
        System.out.println(program.htmlNodes);
    }

    public Node loadData(String filename)
    {
        return htmlNodes = HTMLParser.parse(readFile(filename));
    }

    public void getHTMLData()
    {
        // get the title of the page.
        Node title = htmlNodes.findNodesByTag("title").get(0);
        windowTitle = title.text;

        String stylesheet = readFile("default.css");
        ArrayList<Node> styles = htmlNodes.findNodesByTag("style");
        for (Node style : styles)
        {
            String css = style.text.replace("\t", "").replace("\r", "");
            for (String line : css.split("\n"))
            {
                stylesheet += line.trim();
            }
        }

        styleList = CSSParser.parse(stylesheet);
    }

    public void setStyles()
    {
        for (Style style : styleList)
        {
            htmlNodes.applyCSS(style);
        }
    }

    public String readFile(String filename)
    {
        String data = "";
        try
        {
            File file = new File(filename);
            if (!file.exists()) throw new Error("No file called " + filename);
            BufferedReader bufferedreader = new BufferedReader(new FileReader(file));
            String line;
            while((line = bufferedreader.readLine())!=null)
            {
                if (line != null && !line.equals(""))
                {
                    data += line.trim();
                }
                data = data.replace("\t", "").replace("\r", "").replace("\n", "");
            }
        }
        catch (IOException e)
        {
            e.getStackTrace();
        }
        return data;
    }

}
