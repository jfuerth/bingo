import java.awt.*;
import java.awt.print.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Collections;
import java.text.BreakIterator;

/**
 * BingoCard is a lightweight (Swing) component that renders a Bingo
 * card.  This class includes a main() method so it can be used
 * stand-alone from the command line, but it can just as easily be
 * used programmatically from inside any Swing application or applet.
 *
 * @author Jonathan Fuerth
 */
public class BingoCard extends JPanel {

    private static int rows = 5;
    private static int cols = 5;
    private JPanel card;

    public BingoCard() {
	super(new BingoCardLayout(rows+1, cols));
    }

    /**
     * Reads the names for the squares from the file named as the
     * first command line argument, or from the default
     * <code>bingo_names.txt</code> if no arguments are present.
     */
    public static void main(String[] args) throws IOException {
	String namesFile = "bingo_names.txt";
	if (args.length >= 1) {
	    namesFile = args[0];
	}

	final List[] names = readNamesFile(namesFile);
	
	JComponent card = createCard(names);

	final JSpinner printPageCount = new JSpinner();

	JButton printButton = new JButton("Print...");
	printButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    PrinterJob pj = PrinterJob.getPrinterJob();
		    pj.setPrintable(new CardPrinter(names, ((Integer) printPageCount.getValue()).intValue()));
		    if (pj.printDialog()) {
			try {
			    pj.print();
			} catch (Exception ex) {
			    ex.printStackTrace();
			    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		    }
		}
	    });

	JPanel printPanel = new JPanel(new FlowLayout());
	printPanel.add(new JLabel("Number of Cards:"));
	printPanel.add(printPageCount);
	printPanel.add(printButton);

	JFrame f = new JFrame("Bingo!");	
	f.getContentPane().add(card, BorderLayout.CENTER);
	f.getContentPane().add(printPanel, BorderLayout.SOUTH);
	f.pack();
	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	f.setVisible(true);
    }

    public static JComponent createCard(List[] names) {
	for (int i = 0; i < names.length; i++) {
	    Collections.shuffle(names[i]);
	}

	String heading[] = {"B", "I", "N", "G", "O"};

	JPanel card = new BingoCard();

	Font squareFont = new Font("Serif", Font.PLAIN, 20);

	for (int col = 0; col < cols; col++) {
	    JLabel headingLabel = new JLabel(heading[col], JLabel.CENTER);
	    headingLabel.setFont(new Font("SansSerif", Font.PLAIN, 60));
	    card.add(headingLabel);
	    for (int row = 0; row < rows; row++) {
		if (col == 2 && row == 2) {
		    LineBreakLabel lab = new LineBreakLabel("FREE SPACE");
		    lab.setBorder(BorderFactory.createLineBorder(Color.black));
		    lab.setFont(squareFont);
		    card.add(lab);
		} else {
		    LineBreakLabel lab = new LineBreakLabel((String) names[col].get(row));
		    lab.setBorder(BorderFactory.createLineBorder(Color.black));
		    lab.setFont(squareFont);
		    card.add(lab);
		}
	    }
	}

	return card;
    }

    /**
     * Reads the words that can appear in each column from the given
     * file.  The file format is as follows:
     *
     * <pre>
     * [B]
     * My B Item
     * My Other B Item
     *
     * [I]
     * My I Item
     * My other I Item
     *
     * [N]
     * My N Item
     * My other N Item
     * </pre>
     *
     * <p>(and so on).
     *
     * <p>Blank lines (including lines containing only whitespace) are
     * skipped.  The section names are not important; any line that
     * starts with a '[' character starts a new section.  There is
     * currently no way to specify a bingo card content whose text
     * starts with '['.
     *
     * <p>Note that you need to have at least <code>getRows()</code>
     * items in each section of the words file, or the BingoCard will
     * not be able to fill the column.
     */
    public static List[] readNamesFile(String fileName) throws IOException {
	List[] names = new List[5];
	for (int i = 0; i < 5; i++) {
	    names[i] = new ArrayList();
	}
	BufferedReader in = new BufferedReader(new FileReader(new File(fileName)));
	int i = -1;
	String line;
	while ((line = in.readLine()) != null) {
	    if (line.startsWith("[")) {
		i++;
	    } else if (line.trim().length() == 0) {
		// skip
	    } else {
		names[i].add(line);
	    }
	}
	return names;
    }

//     public void paintChildren(Graphics g) {
// 	Graphics2D g2 = (Graphics2D) g;
// 	for (int i = 0; i < getComponentCount(); i++) {
// 	    System.out.println("Painting BingoCard subcomponent "+i);
// 	    JComponent c = (JComponent) getComponent(i);
// 	    g2.translate(c.getX(), c.getY());
// 	    c.paintBorder(g2);
// 	    c.paintComponent(g2);
// 	    g2.translate(-c.getX(), -c.getY());
// 	}
//     }
}

/**
 * The BingoCardLayout is much like the AWT GridLayout.  In fact, it
 * is currently functionally identical for our purposes.
 */
class BingoCardLayout implements LayoutManager {

    private int numRows;
    private int numCols;

    private int preferredRowWidth = 100;

    public BingoCardLayout(int rows, int cols) {
	this.numRows = rows;
	this.numCols = cols;
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
	return new Dimension(preferredRowWidth*numCols, preferredRowWidth*numRows);
    }

    public Dimension minimumLayoutSize(Container parent) {
	return new Dimension(preferredRowWidth*numCols, preferredRowWidth*numRows);
    }

    public void layoutContainer(Container parent) {
	System.out.println("Laying out component "+parent);
	Dimension size = parent.getSize();
	int childNum = 0;
	for (int col = 0; col < numCols; col++) {
	    int x = col * (size.width / numCols);
	    for (int row = 0; row < numRows; row++) {
		int y = row * (size.height / numRows);
		if (childNum >= parent.getComponentCount()) break;
		Component child = parent.getComponent(childNum);
		Rectangle pos = new Rectangle(x, y, size.width / numCols, size.height / numRows);
		child.setBounds(pos);
		System.out.println("Moved child "+childNum+" to "+pos);
		childNum++;
	    }
	}
    }
}

/**
 * The LineBreakLabel is similar to the Swing JLabel, but it can wrap
 * the text that it contains if the component is too narrow to display
 * it all on a single line.
 */
class LineBreakLabel extends JPanel {

    private String originalText;
    private List lines;

    /**
     * Creates a new LineBreakLabel with the given text (which will
     * wrap at word boundaries if necessary).
     */
    public LineBreakLabel(String text) {
	setText(text);
	addComponentListener(new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
		    lines = null;
		}
	    });
    }

    /**
     * Changes the text that this component tries to display.
     */
    public void setText(String v) {
	this.originalText = v;
	this.lines = null;
    }

    /**
     * Returns the original text (no line breaks).
     */
    public String getText() {
	return originalText;
    }

    /**
     * Creates the list of lines that this component will paint.
     * Tries to break lines so that none of them will be wider than
     * this component in the current font, but single words that are
     * wider than this component will overflow.
     */
    public List getLines() {
	if (lines == null) {

	    lines = new LinkedList();

	    Graphics2D g2 = (Graphics2D) getGraphics();
	    if (g2 == null) {
		lines.add("No Graphics Context Yet!");
		return lines;
	    }

	    FontMetrics fm = g2.getFontMetrics();
	    if (fm == null) {
		lines.add("No Font Metrics Yet!");
		return lines;
	    }

	    int availWidth = getWidth() - getInsets().left - getInsets().right;
	    String text = getText();
	    BreakIterator bi = BreakIterator.getWordInstance();
	    bi.setText(text);
	    int start = bi.first();
	    int prevEnd = 0;
	    for (int end = bi.next();
		 end != BreakIterator.DONE;
		 end = bi.next()) {

		if (fm.stringWidth(text.substring(start, end)) > availWidth) {
		    lines.add(text.substring(start, prevEnd));
		    start = prevEnd;
		}
		prevEnd = end;
	    }
	    if (start != text.length()) {
		lines.add(text.substring(start, text.length()));
	    }
	}
	return lines;
    }

    /**
     * Paints this component's lines of text centered horizontally and
     * vertically in the component.
     */
    public void paintComponent(Graphics g) {
	System.out.println("LineBreakLabel '"+getText()+"' is painting");
	Graphics2D g2 = (Graphics2D) g;
	FontMetrics fm = g2.getFontMetrics();
	Iterator it = getLines().iterator();
	int y = getHeight() / 2 - (fm.getHeight() * lines.size()) / 2 + fm.getAscent();
	while (it.hasNext()) {
	    String line = (String) it.next();
	    int x = getWidth() / 2 - fm.stringWidth(line) / 2;
	    g2.drawString(line, x, y);
	    y += fm.getHeight();
	}
    }
}

/**
 * CardPrinter is an AWT Printable which is designed to print
 * BingoCard components.
 */
class CardPrinter implements Printable {

    /**
     * The choices for each column (used when constructing bingo
     * cards).
     */
    private List[] names;

    /**
     * The number of distinct cards to print (this is also the number
     * of pages).
     */
    private int numCards;

    /**
     * Sets up a card printer for numCards cards.
     */
    public CardPrinter(List[] names, int numCards) {
	this.names = names;
	this.numCards = numCards;
	System.out.println("Created new CardPrinter!");
    }

    /**
     * Creates a random card and prints it.
     */
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
	throws PrinterException {
 
	Graphics2D g2 = (Graphics2D) graphics;

	if (pageIndex > numCards-1) {
	    System.out.println("No such page "+pageIndex);
	    return(NO_SUCH_PAGE);
	} else {
	    JComponent c = BingoCard.createCard(names);
	    JFrame f = new JFrame("Printing");
	    f.getContentPane().add(c);
	    c.setBackground(Color.white);
	    c.setForeground(Color.black);
	    Rectangle printDimensions = new Rectangle(0, 0,
						      (int) pageFormat.getImageableWidth()-1,
						      (int) pageFormat.getImageableHeight()-1);
	    f.setSize(printDimensions.width, printDimensions.height);
	    f.setVisible(true);
	    c.setPreferredSize(new Dimension(printDimensions.width, printDimensions.height));
	    c.setBounds(printDimensions);
	    c.setVisible(true);
	    g2.translate((int) pageFormat.getImageableX(), (int) pageFormat.getImageableY());
	    f.getContentPane().print(g2);
	    System.out.println("Painted page "+pageIndex);
	    f.dispose();
	    return(PAGE_EXISTS);
	}
   }
}
