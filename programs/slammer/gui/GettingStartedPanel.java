/*
 * Originally written by Matt Jibson for the SLAMMER project. This work has been
 * placed into the public domain. You may use this work in any way and for any
 * purpose you wish.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */

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

		startedPane.setPage(new URL(Help.prefix + "program/gettingStarted.html"));
		startedPane.setEditable(false);

		setLayout(new BorderLayout());

		add(BorderLayout.CENTER, new JScrollPane(startedPane));
	}
}
