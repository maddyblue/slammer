/*
 * Copyright (c) 2002 Matt Jibson
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *    - Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
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
