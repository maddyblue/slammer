/* This file is in the public domain. */

package slammer;

import slammer.gui.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class SRM
{
	public static void main(String[] args) throws Exception
	{
		try
		{
			System.setProperty("derby.system.home", "database");

			SplashScreen splash = new SplashScreen("Seismic-Record Manager");

			Utils.startDB();
			splash.advance();

			Utils.getDB().runUpdate("update data set select1=0, select2=0");
			splash.advance();

			// if the OS supports a native LF, use it
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			JFrame frame = new JFrame("Seismic-Record Manager");
			frame.setIconImage(new ImageIcon(
				frame.getClass().getResource("/slammer/images/icon.jpg")).getImage());
			splash.advance();

			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e)
				{
					try
					{
						e.getWindow().setVisible(false);
						Utils.closeDB();
					}
					catch(Exception ex)
					{
						Utils.catchException(ex);
					}
					System.exit(0);
				}
			});

			splash.advance();

			frame.getContentPane().add(new SlammerTabbedPane(frame, false));
			Dimension screen = frame.getToolkit().getScreenSize();
			frame.setSize((int)(screen.width * 0.8), (int)(screen.height * 0.8));
			frame.setLocationRelativeTo(null);
			splash.advance();

			frame.setVisible(true);
			splash.dispose();
		}
		catch(Exception ex)
		{
			Utils.catchException(ex);
			System.exit(1);
		}
	}
}
