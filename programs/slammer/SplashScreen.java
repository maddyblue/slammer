/* This file is in the public domain. */

package slammer;

import javax.swing.*;
import java.awt.*;

/**
 * The splash screen displayed on startup.
 */
public class SplashScreen extends JComponent
{
	String name;

	public SplashScreen(String name)
	{
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		setBackground(Color.WHITE);

		Font font = new Font("Dialog", Font.PLAIN, 10);
		setFont(font);
		fm = getFontMetrics(font);

		String images[] = {
			"Anchorage",
			"Black Rapids",
			"Georgia",
			"Huascaran",
			"Madison Canyon",
			"McGinnis",
			"Niigata 1",
			"Northridge 1",
			"Northridge 2",
			"Springdale",
			"Turnagain Heights",
			"West Fork"
		};

		image = getToolkit().getImage(
			getClass().getResource("/slammer/images/" +
				images[(int)(images.length * Math.random())]
				+ ".jpg"));
		MediaTracker tracker = new MediaTracker(this);
		tracker.addImage(image,0);

		try
		{
			tracker.waitForAll();
		}
		catch(Exception e)
		{
			Utils.catchException(e);
		}

		win = new JWindow();

		Dimension screen = getToolkit().getScreenSize();
		Dimension size = new Dimension(image.getWidth(this) + 2,
			image.getHeight(this) + 2 + PROGRESS_HEIGHT);
		win.setSize(size);

		win.getContentPane().add(BorderLayout.CENTER,this);

		win.setLocationRelativeTo(null);
		win.validate();
		win.setVisible(true);

		this.name = name;
	}

	public void dispose()
	{
		win.dispose();
	}

	public synchronized void advance()
	{
		progress++;
		repaint();

		// wait for it to be painted to ensure progress is updated
		// continuously
		try
		{
			wait();
		}
		catch(InterruptedException ie)
		{
			Utils.catchException(ie);
		}
	}

	public synchronized void paintComponent(Graphics g)
	{
		Dimension size = getSize();

		g.setColor(Color.black);
		g.drawRect(0,0,size.width - 1,size.height - 1);

		g.drawImage(image,1,1,this);

		// XXX: This should not be hardcoded
		g.setColor(Color.white);
		g.fillRect(1,image.getHeight(this) + 1,
			((win.getWidth() - 2) * progress) / 5,PROGRESS_HEIGHT);

		g.setColor(Color.black);

		String str = "Starting " + name;

		g.drawString(str,
			(getWidth() - fm.stringWidth(str)) / 2,
			image.getHeight(this) + (PROGRESS_HEIGHT
			+ fm.getAscent() + fm.getDescent()) / 2);

		notify();
	}

	// private members
	private FontMetrics fm;
	private JWindow win;
	private Image image;
	private int progress;
	private static final int PROGRESS_HEIGHT = 20;
}
