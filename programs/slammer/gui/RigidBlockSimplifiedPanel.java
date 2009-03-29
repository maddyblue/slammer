/* This file is in the public domain. */

package slammer.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.Vector;
import slammer.*;
import slammer.analysis.*;

class RigidBlockSimplifiedPanel extends JPanel implements ActionListener
{
	SlammerTabbedPane parent;

	JRadioButton Jibson1993 = new JRadioButton("Jibson (1993)");
	JRadioButton JibsonAndOthers1998 = new JRadioButton("Jibson and others (1998, 2000)");
	JRadioButton Jibson2007CA = new JRadioButton("Jibson (2007) Critical acceleration ratio");
	JRadioButton Jibson2007CAM = new JRadioButton("Jibson (2007) Critical acceleration ratio and magnitude");
	JRadioButton Jibson2007AICA = new JRadioButton("Jibson (2007) Arias intensity and critical acceleration");
	JRadioButton Jibson2007AICAR = new JRadioButton("Jibson (2007) Arias intensity and critical acceleration ratio");
	JRadioButton Ambraseys = new JRadioButton("Ambraseys and Menu (1988)");
	JRadioButton SaygiliRathje2008CARPA = new JRadioButton("<html>Saygili and Rathje (2008) Critical acceleration ratio<br/>and peak acceleration</html>");
	JRadioButton SaygiliRathje2008CARPAPV = new JRadioButton("<html>Saygili and Rathje (2008) Critical acceleration ratio,<br/>peak acceleration, peak velocity</html>");
	JRadioButton SaygiliRathje2008CARPAPVAI = new JRadioButton("<html>Saygili and Rathje (2008) Critical acceleration ratio,<br/>peak acceleration, peak velocity, and Arias intensity</html>");
	ButtonGroup group = new ButtonGroup();

	JLabel labelOne = new JLabel(" ");
	JLabel labelTwo = new JLabel(" ");
	JLabel labelThree = new JLabel(" ");
	JLabel labelFour = new JLabel(" ");
	JLabel labelRes = new JLabel("Estimated Newmark Displacement (cm):");
	JTextField labelOnef = new JTextField(7);
	JTextField labelTwof = new JTextField(7);
	JTextField labelThreef = new JTextField(7);
	JTextField labelFourf = new JTextField(7);
	JTextField labelResf = new JTextField(7);
	JEditorPane ta = new JEditorPane();
	JScrollPane sta = new JScrollPane(ta);
	JButton button = new JButton("Compute");

	String Jibson1993Str = "This program estimates rigid-block Newmark displacement as a function of Arias shaking intensity and critical acceleration as explained in Jibson (1993). The estimate is made using the following regression equation:"
	+ "<p>log <i>D<sub>n</sub></i> = 1.460 log <i>I<sub>a</sub></i> - 6.642 log <i>a<sub>c</sub></i> + 1.546"
	+ "<p>where <i>D<sub>n</sub></i> is Newmark displacement in centimeters, <i>I<sub>a</sub></i> is Arias intensity in meters per second, and <i>a<sub>c</sub></i> is critical acceleration in g's. This equation was developed by conducting rigorous Newmark integrations on 11 single-component strong-motion records for several discrete values of critical acceleration. The regression model has an R<sup>2</sup> value of 87% and a model standard deviation of 0.409.</p>";

	String JibsonAndOthers1998Str = "This program estimates rigid-block Newmark displacement as a function of Arias shaking intensity and critical acceleration as explained in Jibson and others (1998, 2000). The estimate is made using the following regression equation:"
	+ "<p>log <i>D<sub>n</sub></i> = 1.521 log <i>I<sub>a</sub></i> - 1.993 log <i>a<sub>c</sub></i> - 1.546"
	+ "<p>where <i>D<sub>n</sub></i> is Newmark displacement in centimeters, <i>I<sub>a</sub></i> is Arias intensity in meters per second, and <i>a<sub>c</sub></i> is critical acceleration in g's. This equation was developed by conducting rigorous Newmark integrations on 555 single-component strong-motion records from 13 earthquakes for several discrete values of critical acceleration. The regression model has an R<sup>2</sup> value of 87% and a model standard deviation of 0.375.</p>";

	String Jibson2007CAStr = "This program estimates rigid-block Newmark displacement as a function of critical acceleration ratio as explained in Jibson (2007). The estimate is made using the following regression equation:"
	+ "<p>log <i>D<sub>n</sub></i> = 0.215 + log [ ( 1 - <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> ) <sup>2.341</sup> ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> ) <sup>-1.438</sup> ]"
	+ "<p>where <i>D<sub>n</sub></i> is Newmark displacement in centimeters, <i>a<sub>c</sub></i> is critical acceleration in g's, and <i>a<sub>max</sub></i> is horizontal peak ground acceleration (PGA) in g's. This equation was developed by conducting rigorous Newmark integrations on 2270 single-component strong-motion records from 30 earthquakes for several discrete values of critical acceleration. The regression model has an R<sup>2</sup> value of 84% and a model standard deviation of 0.510.</p>";

	String Jibson2007CAMStr = "This program estimates rigid-block Newmark displacement as a function of critical acceleration ratio and magnitude as explained in Jibson (2007). The estimate is made using the following regression equation:"
	+ "<p>log <i>D<sub>n</sub></i> = -2.710 + log [ ( 1 - <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> ) <sup>2.335</sup> ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> ) <sup>-1.478</sup> ] + 0.424 <b>M</b>"
	+ "<p>where <i>D<sub>n</sub></i> is Newmark displacement in centimeters, <i>a<sub>c</sub></i> is critical acceleration in g's, <i>a<sub>max</sub></i> is horizontal peak ground acceleration (PGA) in g's, and <b>M</b> is moment magnitude. This equation was developed by conducting rigorous Newmark integrations on 2270 single-component strong-motion records from 30 earthquakes for several discrete values of critical acceleration. The regression model has an R<sup>2</sup> value of 87% and a model standard deviation of 0.454.</p>";

	String Jibson2007AICAStr = "This program estimates rigid-block Newmark displacement as a function of Arias intensity and critical acceleration as explained in Jibson (2007). The estimate is made using the following regression equation:"
	+ "<p>log <i>D<sub>n</sub></i> = 2.401 log <i>I<sub>a</sub></i> - 3.481 log <i>a<sub>c</sub></i> - 3.320"
	+ "<p>where <i>D<sub>n</sub></i> is Newmark displacement in centimeters, <i>I<sub>a</sub></i> is Arias intensity in meters per second, and <i>a<sub>c</sub></i> is critical acceleration in g's. This equation was developed by conducting rigorous Newmark integrations on 875 single-component strong-motion records from 30 earthquakes for several discrete values of critical acceleration. The regression model has an R<sup>2</sup> value of 71% and a model standard deviation of 0.656.</p>";

	String Jibson2007AICARStr = "This program estimates rigid-block Newmark displacement as a function of Arias intensity and critical acceleration ratio as explained in Jibson (2007). The estimate is made using the following regression equation:"
	+ "<p>log <i>D<sub>n</sub></i> = 0.561 log <i>I<sub>a</sub></i> - 3.833 log ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> ) - 1.474"
	+ "<p>where <i>D<sub>n</sub></i> is Newmark displacement in centimeters, <i>I<sub>a</sub></i> is Arias intensity in meters per second, <i>a<sub>c</sub></i> is critical acceleration in g's, and <i>a<sub>max</sub></i> is horizontal peak ground acceleration (PGA) in g's. This equation was developed by conducting rigorous Newmark integrations on 875 single-component strong-motion records from 30 earthquakes for several discrete values of critical acceleration. The regression model has an R<sup>2</sup> value of 75% and a model standard deviation of 0.616.</p>";

	String AmbraseysStr = "This program estimates rigid-block Newmark displacement as a function of the critical acceleration and peak ground acceleration using the following equation as explained in Ambraseys and Menu (1988):"
	+ "<p>log <i>D<sub>n</sub></i> = 0.90 + log[ (1 - a<sub><i>c</i></sub> / a<sub><i>max</i></sub>)<sup>2.53</sup> (a<sub><i>c</i></sub> / a<sub><i>max</i></sub>)<sup>-1.09</sup> ]"
	+ "<p>where <i>D<sub>n</sub></i> is Newmark displacement in centimeters, <i>a<sub>c</sub></i> is critical (yield) acceleration in g's, and <i>a<sub>max</sub></i> is the peak horizontal ground acceleration in g's.";

	String SaygiliRathje2008CARPAStr = "This program estimates rigid-block Newmark displacement as a function of critical acceleration and peak ground acceleration (<i>a<sub>max</sub></i>) as explained in Saygili and Rathje (2008).  The estimate is made using the following regression equation:"
	+ "<p>ln <i>D<sub>n</sub></i> = 5.52 - 4.43 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> ) - 20.39 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> )<sup>2</sup> + 42.61 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> )<sup>3</sup> - 28.74 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> )<sup>4</sup> + 0.72 ln <i>a<sub>max</sub></i>"
	+ "<p>where <i>D</sub>n</sub></i> is Newmark displacement in centimeters, <i>a</sub>c</sub></i> is critical acceleration in g's, and <i>a<sub>max</sub></i> is peak ground acceleration in g's. The equation was developed by conducting rigorous Newmark integrations on 2383 strong-motion records for critical acceleration values between 0.05 and 0.30 g.  The regression model has standard deviation of 1.13.";

	String SaygiliRathje2008CARPAPVStr = "This program estimates rigid-block Newmark displacement as a function of critical acceleration, peak ground acceleration (<i>a<sub>max</sub></i>), and peak ground velocity (<i>v<sub>max</sub></i>) as explained in Saygili and Rathje (2008).  The estimate is made using the following regression equation:"
	+ "<p>ln <i>D<sub>n</sub></i> = -1.56 - 4.58 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> ) - 20.84 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> )<sup>2</sup> + 44.75 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> )<sup>3</sup> - 30.50 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> )<sup>4</sup> - 0.64 ln <i>a<sub>max</sub></i> + 1.55 ln <i>v<sub>max</sub></i>"
	+ "<p>where <i>D</sub>n</sub></i> is Newmark displacement in centimeters, <i>a</sub>c</sub></i> is critical acceleration in g's, <i>a<sub>max</sub></i> is peak ground acceleration in g's, and <i>v<sub>max</sub></i> is peak ground velocity in centimeters per second. The equation was developed by conducting rigorous Newmark integrations on 2383 strong-motion records for critical acceleration values between 0.05 and 0.30 g.  The regression model has standard deviation of 1.13.";

	String SaygiliRathje2008CARPAPVAIStr = "This program estimates rigid-block Newmark displacement as a function of critical acceleration, peak ground acceleration (<i>a<sub>max</sub</i>), peak ground velocity (<i>v<sub>max</sub></i>), and Arias intensity (<i>I<sub>a</sub></i>), as explained in Saygili and Rathje (2008).  The estimate is made using the following regression equation:"
	+ "<p>ln <i>D<sub>n</sub></i> = -0.74 - 4.93 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> ) - 19.91 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> )<sup>2</sup> + 43.75 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> )<sup>3</sup> - 30.12 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> )<sup>4</sup> - 1.30 ln <i>a<sub>max</sub></i> + 1.04 ln <i>v<sub>max</sub></i> + 0.67 ln <i>I<sub>a</sub></i>"
	+ "<p>where <i>D</sub>n</sub></i> is Newmark displacement in centimeters, <i>a</sub>c</sub></i> is critical acceleration in g's, <i>a<sub>max</sub></i> is peak ground acceleration in g's, <i>v<sub>max</sub></i> is peak ground velocity in centimeters per second, and <i>I<sub>a</sub</i> is Arias intensity in meters per second. The equation was developed by conducting rigorous Newmark integrations on 2383 strong-motion records for critical acceleration values between 0.05 and 0.30 g.  The regression model has standard deviation of 0.20 + 0.79 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> ).";

	public RigidBlockSimplifiedPanel(SlammerTabbedPane parent) throws Exception
	{
		this.parent = parent;

		group.add(Jibson1993);
		group.add(JibsonAndOthers1998);
		group.add(Jibson2007CA);
		group.add(Jibson2007CAM);
		group.add(Jibson2007AICA);
		group.add(Jibson2007AICAR);
		group.add(Ambraseys);
		group.add(SaygiliRathje2008CARPA);
		group.add(SaygiliRathje2008CARPAPV);
		group.add(SaygiliRathje2008CARPAPVAI);

		Jibson1993.setActionCommand("change");
		Jibson1993.addActionListener(this);
		JibsonAndOthers1998.setActionCommand("change");
		JibsonAndOthers1998.addActionListener(this);
		Jibson2007CA.setActionCommand("change");
		Jibson2007CA.addActionListener(this);
		Jibson2007CAM.setActionCommand("change");
		Jibson2007CAM.addActionListener(this);
		Jibson2007AICA.setActionCommand("change");
		Jibson2007AICA.addActionListener(this);
		Jibson2007AICAR.setActionCommand("change");
		Jibson2007AICAR.addActionListener(this);
		Ambraseys.setActionCommand("change");
		Ambraseys.addActionListener(this);
		SaygiliRathje2008CARPA.setActionCommand("change");
		SaygiliRathje2008CARPA.addActionListener(this);
		SaygiliRathje2008CARPAPV.setActionCommand("change");
		SaygiliRathje2008CARPAPV.addActionListener(this);
		SaygiliRathje2008CARPAPVAI.setActionCommand("change");
		SaygiliRathje2008CARPAPVAI.addActionListener(this);

		labelOnef.setEditable(false);
		labelTwof.setEditable(false);
		labelThreef.setEditable(false);
		labelFourf.setEditable(false);
		labelResf.setEditable(false);

		button.setActionCommand("do");
		button.addActionListener(this);

		ta.setEditable(false);
		ta.setContentType("text/html");

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		Insets top = new Insets(10, 0, 0, 0);
		Insets none = new Insets(0, 0, 0, 0);

		Border b = BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(0, 0, 0, 5),
			BorderFactory.createMatteBorder(0, 0, 0, 1, Color.BLACK)
		);

		JPanel panel = this;
		panel.setLayout(gridbag);

		int x = 0;
		int y = 0;

		Box sidepanel = new Box(BoxLayout.Y_AXIS);

		sidepanel.add(Jibson2007CA);
		sidepanel.add(Jibson2007CAM);
		sidepanel.add(Jibson2007AICA);
		sidepanel.add(Jibson2007AICAR);
		sidepanel.add(JibsonAndOthers1998);
		sidepanel.add(Jibson1993);
		sidepanel.add(Ambraseys);
		sidepanel.add(SaygiliRathje2008CARPA);
		sidepanel.add(SaygiliRathje2008CARPAPV);
		sidepanel.add(SaygiliRathje2008CARPAPVAI);

		c.gridx = x++;
		c.gridy = y++;
		c.gridheight = 9;
		c.anchor = GridBagConstraints.NORTHWEST;
		gridbag.setConstraints(sidepanel, c);
		panel.add(sidepanel);

		c.gridx = x++;
		c.fill = GridBagConstraints.BOTH;
		JLabel label = new JLabel(" ");
		label.setBorder(b);
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridheight = 1;
		c.gridwidth = 2;
		c.gridx = x++;
		c.gridy = y++;
		c.fill = GridBagConstraints.HORIZONTAL;
		label = new JLabel("Parameters:");
		label.setFont(GUIUtils.headerFont);
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		c.gridwidth = 1;
		gridbag.setConstraints(labelOne, c);
		panel.add(labelOne);

		c.gridx = x--;
		gridbag.setConstraints(labelOnef, c);
		panel.add(labelOnef);

		c.gridx = x++;
		c.gridy = y++;
		gridbag.setConstraints(labelTwo, c);
		panel.add(labelTwo);

		c.gridx = x--;
		gridbag.setConstraints(labelTwof, c);
		panel.add(labelTwof);

		c.gridx = x++;
		c.gridy = y++;
		gridbag.setConstraints(labelThree, c);
		panel.add(labelThree);

		c.gridx = x--;
		gridbag.setConstraints(labelThreef, c);
		panel.add(labelThreef);

		c.gridx = x++;
		c.gridy = y++;
		gridbag.setConstraints(labelFour, c);
		panel.add(labelFour);

		c.gridx = x--;
		gridbag.setConstraints(labelFourf, c);
		panel.add(labelFourf);

		c.gridx = x++;
		c.gridy = y++;
		c.insets = top;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(button, c);
		panel.add(button);

		c.gridy = y++;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(labelRes, c);
		panel.add(labelRes);

		c.gridx = x++;
		gridbag.setConstraints(labelResf, c);
		panel.add(labelResf);

		c.gridx = 0;
		c.gridy = 10;
		c.insets = none;
		c.gridwidth = 4;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		gridbag.setConstraints(sta, c);
		panel.add(sta);
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		try
		{
			String command = e.getActionCommand();
			if(command.equals("change"))
			{
				labelOne.setText("");
				labelTwo.setText("");
				labelThree.setText("");
				labelFour.setText("");
				labelOnef.setText("");
				labelTwof.setText("");
				labelThreef.setText("");
				labelFourf.setText("");
				ta.setText("");

				labelOnef.setEditable(false);
				labelTwof.setEditable(false);
				labelThreef.setEditable(false);
				labelFourf.setEditable(false);

				if(Jibson1993.isSelected())
				{
					labelOne.setText("Critical (yield) acceleration (g's):");
					labelTwo.setText("Arias intensity (m/s):");
					labelOnef.setEditable(true);
					labelTwof.setEditable(true);
					ta.setText(Jibson1993Str);
				}
				else if(JibsonAndOthers1998.isSelected())
				{
					labelOne.setText("Critical (yield) acceleration (g's):");
					labelTwo.setText("Arias intensity (m/s):");
					labelOnef.setEditable(true);
					labelTwof.setEditable(true);
					ta.setText(JibsonAndOthers1998Str);
				}
				else if(Jibson2007CA.isSelected())
				{
					labelOne.setText("Critical (yield) acceleration (g's):");
					labelTwo.setText("Peak ground acceleration (g's):");
					labelOnef.setEditable(true);
					labelTwof.setEditable(true);
					ta.setText(Jibson2007CAStr);
				}
				else if(Jibson2007CAM.isSelected())
				{
					labelOne.setText("Critical (yield) acceleration (g's):");
					labelTwo.setText("Peak ground acceleration (g's):");
					labelThree.setText("Magnitude:");
					labelOnef.setEditable(true);
					labelTwof.setEditable(true);
					labelThreef.setEditable(true);
					ta.setText(Jibson2007CAMStr);
				}
				else if(Jibson2007AICA.isSelected())
				{
					labelOne.setText("Critical (yield) acceleration (g's):");
					labelTwo.setText("Arias intensity (m/s):");
					labelOnef.setEditable(true);
					labelTwof.setEditable(true);
					ta.setText(Jibson2007AICAStr);
				}
				else if(Jibson2007AICAR.isSelected())
				{
					labelOne.setText("Critical (yield) acceleration (g's):");
					labelTwo.setText("Peak ground acceleration (g's):");
					labelThree.setText("Arias intensity (m/s):");
					labelOnef.setEditable(true);
					labelTwof.setEditable(true);
					labelThreef.setEditable(true);
					ta.setText(Jibson2007AICARStr);
				}
				else if(Ambraseys.isSelected())
				{
					labelOne.setText("Critical (yield) acceleration (g's):");
					labelTwo.setText("Peak ground acceleration (g's):");
					labelOnef.setEditable(true);
					labelTwof.setEditable(true);
					ta.setText(AmbraseysStr);
				}
				else if(SaygiliRathje2008CARPA.isSelected())
				{
					labelOne.setText("Critical (yield) acceleration (g's):");
					labelTwo.setText("Peak ground acceleration (g's):");
					labelOnef.setEditable(true);
					labelTwof.setEditable(true);
					ta.setText(SaygiliRathje2008CARPAStr);
				}
				else if(SaygiliRathje2008CARPAPV.isSelected())
				{
					labelOne.setText("Critical (yield) acceleration (g's):");
					labelTwo.setText("Peak ground acceleration (g's):");
					labelThree.setText("Peak ground velocity (cm/s):");
					labelOnef.setEditable(true);
					labelTwof.setEditable(true);
					labelThreef.setEditable(true);
					ta.setText(SaygiliRathje2008CARPAPVStr);
				}
				else if(SaygiliRathje2008CARPAPVAI.isSelected())
				{
					labelOne.setText("Critical (yield) acceleration (g's):");
					labelTwo.setText("Peak ground acceleration (g's):");
					labelThree.setText("Peak ground velocity (cm/s):");
					labelFour.setText("Arias intensity (m/s):");
					labelOnef.setEditable(true);
					labelTwof.setEditable(true);
					labelThreef.setEditable(true);
					labelFourf.setEditable(true);
					ta.setText(SaygiliRathje2008CARPAPVAIStr);
				}
			}
			else if(command.equals("do"))
			{
				if(Jibson1993.isSelected())
				{
					Double d1 = (Double)Utils.checkNum(labelOnef.getText(), "critical acceleration field", null, false, null, new Double(0), true, null, false);
					if(d1 == null) return;

					Double d2 = (Double)Utils.checkNum(labelTwof.getText(), "Arias intensity field", null, false, null, new Double(0), false, null, false);
					if(d2 == null) return;

					labelResf.setText(RigidBlockSimplified.Jibson1993(d2.doubleValue(), d1.doubleValue()));
				}
				else if(JibsonAndOthers1998.isSelected())
				{
					Double d1 = (Double)Utils.checkNum(labelOnef.getText(), "critical acceleration field", null, false, null, new Double(0), true, null, false);
					if(d1 == null) return;

					Double d2 = (Double)Utils.checkNum(labelTwof.getText(), "Arias intensity field", null, false, null, new Double(0), false, null, false);
					if(d2 == null) return;

					labelResf.setText(RigidBlockSimplified.JibsonAndOthers1998(d2.doubleValue(), d1.doubleValue()));
				}
				else if(Jibson2007CA.isSelected())
				{
					Double d1 = (Double)Utils.checkNum(labelOnef.getText(), "critical acceleration field", null, false, null, new Double(0), true, null, false);
					if(d1 == null) return;

					Double d2 = (Double)Utils.checkNum(labelTwof.getText(), "peak ground acceleration field", null, false, null, new Double(0), false, null, false);
					if(d2 == null) return;

					labelResf.setText(RigidBlockSimplified.Jibson2007CA(d1.doubleValue(), d2.doubleValue()));
				}
				else if(Jibson2007CAM.isSelected())
				{
					Double d1 = (Double)Utils.checkNum(labelOnef.getText(), "critical acceleration field", null, false, null, new Double(0), true, null, false);
					if(d1 == null) return;

					Double d2 = (Double)Utils.checkNum(labelTwof.getText(), "peak ground acceleration field", null, false, null, new Double(0), false, null, false);
					if(d2 == null) return;

					Double d3 = (Double)Utils.checkNum(labelThreef.getText(), "magnitude field", null, false, null, new Double(0), false, null, false);
					if(d3 == null) return;

					labelResf.setText(RigidBlockSimplified.Jibson2007CAM(d1.doubleValue(), d2.doubleValue(), d3.doubleValue()));
				}
				else if(Jibson2007AICA.isSelected())
				{
					Double d1 = (Double)Utils.checkNum(labelOnef.getText(), "critical acceleration field", null, false, null, new Double(0), true, null, false);
					if(d1 == null) return;

					Double d2 = (Double)Utils.checkNum(labelTwof.getText(), "Arias intensity field", null, false, null, new Double(0), false, null, false);
					if(d2 == null) return;

					labelResf.setText(RigidBlockSimplified.Jibson2007AICA(d2.doubleValue(), d1.doubleValue()));
				}
				else if(Jibson2007AICAR.isSelected())
				{
					Double d1 = (Double)Utils.checkNum(labelOnef.getText(), "critical acceleration field", null, false, null, new Double(0), true, null, false);
					if(d1 == null) return;

					Double d2 = (Double)Utils.checkNum(labelOnef.getText(), "peak ground acceleration field", null, false, null, new Double(0), true, null, false);
					if(d2 == null) return;

					Double d3 = (Double)Utils.checkNum(labelThreef.getText(), "Arias intensity field", null, false, null, new Double(0), false, null, false);
					if(d3 == null) return;

					labelResf.setText(RigidBlockSimplified.Jibson2007AICAR(d3.doubleValue(), d1.doubleValue(), d2.doubleValue()));
				}
				else if(Ambraseys.isSelected())
				{
					Double d1 = (Double)Utils.checkNum(labelOnef.getText(), "critical acceleration field", null, false, null, new Double(0), true, null, false);
					if(d1 == null) return;

					Double d2 = (Double)Utils.checkNum(labelTwof.getText(), "peak ground acceleration field", null, false, null, new Double(0), false, null, false);
					if(d2 == null) return;

					labelResf.setText(RigidBlockSimplified.AmbraseysAndMenu(d2.doubleValue(), d1.doubleValue()));
				}
				else if(SaygiliRathje2008CARPA.isSelected())
				{
					Double d1 = (Double)Utils.checkNum(labelOnef.getText(), "critical acceleration field", null, false, null, new Double(0), true, null, false);
					if(d1 == null) return;

					Double d2 = (Double)Utils.checkNum(labelTwof.getText(), "peak ground acceleration field", null, false, null, new Double(0), true, null, false);
					if(d2 == null) return;

					labelResf.setText(RigidBlockSimplified.SaygiliRathje2008CARPA(d1.doubleValue(), d2.doubleValue()));
				}
				else if(SaygiliRathje2008CARPAPV.isSelected())
				{
					Double d1 = (Double)Utils.checkNum(labelOnef.getText(), "critical acceleration field", null, false, null, new Double(0), true, null, false);
					if(d1 == null) return;

					Double d2 = (Double)Utils.checkNum(labelTwof.getText(), "peak ground acceleration field", null, false, null, new Double(0), true, null, false);
					if(d2 == null) return;

					Double d3 = (Double)Utils.checkNum(labelThreef.getText(), "peak ground velocity field", null, false, null, new Double(0), true, null, false);
					if(d3 == null) return;

					labelResf.setText(RigidBlockSimplified.SaygiliRathje2008CARPAPV(d1.doubleValue(), d2.doubleValue(), d3.doubleValue()));
				}
				else if(SaygiliRathje2008CARPAPVAI.isSelected())
				{
					Double d1 = (Double)Utils.checkNum(labelOnef.getText(), "critical acceleration field", null, false, null, new Double(0), true, null, false);
					if(d1 == null) return;

					Double d2 = (Double)Utils.checkNum(labelTwof.getText(), "peak ground acceleration field", null, false, null, new Double(0), true, null, false);
					if(d2 == null) return;

					Double d3 = (Double)Utils.checkNum(labelThreef.getText(), "peak ground velocity field", null, false, null, new Double(0), true, null, false);
					if(d3 == null) return;

					Double d4 = (Double)Utils.checkNum(labelFourf.getText(), "Arias intensity field", null, false, null, new Double(0), true, null, false);
					if(d4 == null) return;

					labelResf.setText(RigidBlockSimplified.SaygiliRathje2008CARPAPVAI(d1.doubleValue(), d2.doubleValue(), d3.doubleValue(), d4.doubleValue()));
				}
				else
				{
					GUIUtils.popupError("No function selected.");
				}

			}
		}
		catch (Exception ex)
		{
			Utils.catchException(ex);
		}
	}
}
