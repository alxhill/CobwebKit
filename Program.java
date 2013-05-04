import java.io.*;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.event.*;

public class Program implements Runnable {

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
        program.render();
        System.out.println(program.htmlNodes);
    }

    public Node loadData(String filename)
    {
        return htmlNodes = HTMLParser.parse(readFile(filename));
    }

    // extract information from the parsed HTML data.
    // at the moment this is only the title of the page and
    // the contents of any style tags.
    public void getHTMLData()
    {
        Node head = htmlNodes.findNodesByTag("head").get(0);

        // get the title of the page.
        Node title = head.findNodesByTag("title").get(0);
        windowTitle = title.text;

        String stylesheet = readFile("default.css");
        ArrayList<Node> styles = head.findNodesByTag("style");
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
