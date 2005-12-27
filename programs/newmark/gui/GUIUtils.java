/*
 * GUIUtils.java - gui utilities
 *
 * Copyright (C) 2002 Matthew Jibson (dolmant@dolmant.net)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/* $Id$ */

package newmark.gui;

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
