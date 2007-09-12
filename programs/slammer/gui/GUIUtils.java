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
import javax.swing.border.*;
import java.awt.*;
import java.util.Vector;

public class GUIUtils
{
	public static final Font headerFont = new Font("Dialoug", Font.BOLD, 16);
	public static Color bg = new Color(204,204,204);

	public static void popupError(String er)
	{
		if(er.indexOf("\n") == -1)
		{
			JOptionPane.showMessageDialog(null, er, "Error", JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			JTextArea textArea = new JTextArea(er);
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);

			JOptionPane op = new JOptionPane(new JScrollPane(textArea), JOptionPane.ERROR_MESSAGE);
			op.setPreferredSize(new Dimension(600, 400));
			JDialog dialog = op.createDialog(op, "Error");
			dialog.setVisible(true);
		}
	}

	public static JPanel makeRecursiveLayoutDown(Vector list)
	{
		return makeRecursiveLayout(list, BorderLayout.NORTH, BorderLayout.WEST);
	}

	public static JPanel makeRecursiveLayoutRight(Vector list)
	{
		return makeRecursiveLayout(list, BorderLayout.WEST, BorderLayout.CENTER);
	}

	private static JPanel makeRecursiveLayout(Vector list, String container, String recursive)
	{
		JPanel ret = new JPanel(new BorderLayout());

		if(list.size() == 0)
			return ret;

		ret.add(container, (Container)list.remove(0));

		if(list.size() == 0)
			return ret;

		ret.add(recursive, makeRecursiveLayout(list, container, recursive));

		return ret;
	}

	public static Border makeCompoundBorder(int top, int left, int bottom, int right)
	{
		return makeCompoundBorder(top, left, bottom, right, 5);
	}

	public static Border makeCompoundBorder(int top, int left, int bottom, int right, int emptyWidth)
	{
		int realTop = getBorderSize(top, emptyWidth);
		int realLeft = getBorderSize(left, emptyWidth);
		int realBottom = getBorderSize(bottom, emptyWidth);
		int realRight = getBorderSize(right, emptyWidth);

		return BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(realTop, realLeft, realBottom, realRight),
			BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(top, left, bottom, right, Color.black),
				BorderFactory.createEmptyBorder(realTop, realLeft, realBottom, realRight)
			)
		);
	}

	private static int getBorderSize(int size, int width)
	{
		if(size != 0) return width;
		else return 0;
	}

	public static void setLocationMiddle(JFrame f)
	{
		Dimension screen = f.getToolkit().getScreenSize();
		Dimension size = f.getSize();
		f.setLocation((screen.width - size.width) / 2,	(screen.height - size.height) / 2);
	}
}
