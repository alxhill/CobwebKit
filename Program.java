import java.io.*;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;

public class Program implements Runnable {

    String windowTitle;
    ArrayList<Style> styleList;

    Node html;
    Node head;
    Node body;

    PageView view = new PageView();

    // small internal class that renders the body element
    private class PageView extends JPanel {

        PageView()
        {
            setPreferredSize(new Dimension(800, 600));
        }

        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            body.render(g2);
        }
    }

    public static void main(String[] args)
    {
        String filename = args.length() == 0 ? "test.html" : args[0];
        Program program = new Program();
        String htmlString = program.readFile(filename);
        program.parseData(htmlString);
        SwingUtilities.invokeLater(program);
    }

    public void parseData(String htmlString)
    {
        html = HTMLParser.parse(htmlString);
        head = html.findNodesByTag("head").get(0);
        body = html.findNodesByTag("body").get(0);

        // get the title of the page.
        Node title = head.findNodesByTag("title").get(0);
        windowTitle = title.text;

        // get the CSS elements + the default file all in one string
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

        // parse the CSS and apply it to the html
        styleList = CSSParser.parse(stylesheet);
        for (Style style : styleList)
        {
            html.applyCSS(style);
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

    public void run()
    {
        JFrame w = new JFrame(windowTitle);
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        w.add(view);
        w.pack();
        w.setLocationByPlatform(true);
        w.setVisible(true);
    }



}
