package slammer.gui;

import javax.swing.JTextField;
import java.awt.Dimension;

class WideTextField extends JTextField
{
	public WideTextField(int cols)
	{
		super(cols);
	}

	public WideTextField(String s, int cols)
	{
		super(s, cols);
	}

	public Dimension getMinimumSize()
	{
		return getPreferredSize();
	}

	public Dimension getMaximumSize()
	{
		return getPreferredSize();
	}
}
