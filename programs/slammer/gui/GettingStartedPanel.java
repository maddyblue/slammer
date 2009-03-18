/* This file is in the public domain. */

package slammer.gui;

import javax.swing.*;
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

		startedPane.setEditable(false);

		startedPane.setPage(new URL(Help.prefix + "program/gettingStarted.html"));
		startedPane.setEditable(false);

		setLayout(new BorderLayout());

		add(BorderLayout.CENTER, new JScrollPane(startedPane));
	}
}
