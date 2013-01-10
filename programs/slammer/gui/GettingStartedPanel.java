/* This file is in the public domain. */

package slammer.gui;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.net.URL;
import slammer.*;

class GettingStartedPanel extends JPanel
{
	SlammerTabbedPane parent;

	JEditorPane startedPane = new JEditorPane();

	public GettingStartedPanel(SlammerTabbedPane parent) throws Exception
	{
		this.parent = parent;

		startedPane.setPage(Help.getUrl("program/gettingStarted.html"));
		startedPane.setEditable(false);

		Font font = UIManager.getFont("Label.font");
		String bodyRule = "body { font-family: " + font.getFamily() + "; " + "font-size: " + font.getSize() + "pt; }";
		((HTMLDocument)startedPane.getDocument()).getStyleSheet().addRule(bodyRule);

		setLayout(new BorderLayout());

		add(BorderLayout.CENTER, new JScrollPane(startedPane));
	}
}
