/* This file is in the public domain. */

package slammer.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
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
	JRadioButton SaygiliRathje2008CARPAPVAI = new JRadioButton("<html>Saygili and Rathje (2008) Critical acceleration ratio,<br/>peak acceleration, peak velocity, Arias intensity</html>");
	JRadioButton SaygiliRathje2009CARPAM = new JRadioButton("<html>Saygili and Rathje (2009) Critical acceleration ratio,<br/>peak acceleration, and magnitude</html>");
	ButtonGroup group = new ButtonGroup();

	JTextField fieldAc = new WideTextField(7);
	JTextField fieldAmax = new WideTextField(7);
	JTextField fieldVmax = new WideTextField(7);
	JTextField fieldIa = new WideTextField(7);
	JTextField fieldM = new WideTextField(7);
	JTextField fieldResCm = new WideTextField(7);
	JTextField fieldResIn = new WideTextField(7);
	JEditorPane ta = new JEditorPane();
	JScrollPane sta = new JScrollPane(ta);
	JButton button = new JButton("Compute");

	String Jibson1993Str = "This program estimates rigid-block Newmark displacement as a function of Arias shaking intensity and critical acceleration as explained in Jibson (1993). The estimate is made using the following regression equation:"
	+ "<p>log <i>D<sub>n</sub></i> = 1.460 log <i>I<sub>a</sub></i> - 6.642 <i>a<sub>c</sub></i> + 1.546"
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
	+ "<p>where <i>D<sub>n</sub></i> is Newmark displacement in centimeters, <i>a<sub>c</sub></i> is critical acceleration in g's, and <i>a<sub>max</sub></i> is peak ground acceleration in g's. The equation was developed by conducting rigorous Newmark integrations on 2383 strong-motion records for critical acceleration values between 0.05 and 0.30 g.  The regression model has standard deviation of 1.13.";

	String SaygiliRathje2008CARPAPVStr = "This program estimates rigid-block Newmark displacement as a function of critical acceleration, peak ground acceleration (<i>a<sub>max</sub></i>), and peak ground velocity (<i>v<sub>max</sub></i>) as explained in Saygili and Rathje (2008).  The estimate is made using the following regression equation:"
	+ "<p>ln <i>D<sub>n</sub></i> = -1.56 - 4.58 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> ) - 20.84 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> )<sup>2</sup> + 44.75 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> )<sup>3</sup> - 30.50 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> )<sup>4</sup> - 0.64 ln <i>a<sub>max</sub></i> + 1.55 ln <i>v<sub>max</sub></i>"
	+ "<p>where <i>D<sub>n</sub></i> is Newmark displacement in centimeters, <i>a<sub>c</sub></i> is critical acceleration in g's, <i>a<sub>max</sub></i> is peak ground acceleration in g's, and <i>v<sub>max</sub></i> is peak ground velocity in centimeters per second. The equation was developed by conducting rigorous Newmark integrations on 2383 strong-motion records for critical acceleration values between 0.05 and 0.30 g.  The regression model has a standard deviation of 0.41 + 0.52(<i>a<sub>c</sub></i> / <i>a<sub>max</sub></i>).";

	String SaygiliRathje2008CARPAPVAIStr = "This program estimates rigid-block Newmark displacement as a function of critical acceleration, peak ground acceleration (<i>a<sub>max</sub</i>), peak ground velocity (<i>v<sub>max</sub></i>), and Arias intensity (<i>I<sub>a</sub></i>), as explained in Saygili and Rathje (2008).  The estimate is made using the following regression equation:"
	+ "<p>ln <i>D<sub>n</sub></i> = -0.74 - 4.93 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> ) - 19.91 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> )<sup>2</sup> + 43.75 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> )<sup>3</sup> - 30.12 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> )<sup>4</sup> - 1.30 ln <i>a<sub>max</sub></i> + 1.04 ln <i>v<sub>max</sub></i> + 0.67 ln <i>I<sub>a</sub></i>"
	+ "<p>where <i>D<sub>n</sub></i> is Newmark displacement in centimeters, <i>a<sub>c</sub></i> is critical acceleration in g's, <i>a<sub>max</sub></i> is peak ground acceleration in g's, <i>v<sub>max</sub></i> is peak ground velocity in centimeters per second, and <i>I<sub>a</sub</i> is Arias intensity in meters per second. The equation was developed by conducting rigorous Newmark integrations on 2383 strong-motion records for critical acceleration values between 0.05 and 0.30 g.  The regression model has standard deviation of 0.20 + 0.79 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> ).";

	String SaygiliRathje2009CARPAMStr = "This program estimates rigid-block Newmark displacement as a function of critical acceleration ratio, peak acceleration, and moment magnitude as explained in Rathje and Saygili (2009).  The estimate is made using the following regression equation:"
	+ "<p>ln <i>D<sub>n</sub></i> = 4.89 - 4.85 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> ) - 19.64 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> )<sup>2</sup> + 42.49 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> )<sup>3</sup> - 29.06 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> )<sup>4</sup> + 0.72 ln <i>a<sub>max</sub></i> + 0.89 + 0.424 ( <b>M</b> - 6 )"
	+ "<p>where <i>D<sub>n</sub></i> is Newmark displacement in centimeters, <i>a<sub>c</sub></i> is critical acceleration in g's, <i>a<sub>max</sub></i> is horizontal peak ground acceleration (PGA) in g's, and <b>M</b> is moment magnitude.  This equation was developed by conducting rigorous Newmark integrations on more than 2000 single-component strong-motion records for several discrete values of critical acceleration.  The standard deviation of the model is 0.95.";

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
		group.add(SaygiliRathje2009CARPAM);

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
		SaygiliRathje2009CARPAM.setActionCommand("change");
		SaygiliRathje2009CARPAM.addActionListener(this);

		fieldAc.setEditable(false);
		fieldAmax.setEditable(false);
		fieldVmax.setEditable(false);
		fieldIa.setEditable(false);
		fieldM.setEditable(false);
		fieldResCm.setEditable(false);
		fieldResIn.setEditable(false);

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
		JLabel label;

		Box sidepanel = new Box(BoxLayout.Y_AXIS);

		label = new JLabel("Select analysis:");
		label.setFont(GUIUtils.headerFont);
		sidepanel.add(label);
		sidepanel.add(SaygiliRathje2009CARPAM);
		sidepanel.add(SaygiliRathje2008CARPA);
		sidepanel.add(SaygiliRathje2008CARPAPV);
		sidepanel.add(SaygiliRathje2008CARPAPVAI);
		sidepanel.add(Jibson2007CA);
		sidepanel.add(Jibson2007CAM);
		sidepanel.add(Jibson2007AICA);
		sidepanel.add(Jibson2007AICAR);
		sidepanel.add(JibsonAndOthers1998);
		sidepanel.add(Jibson1993);
		sidepanel.add(Ambraseys);

		c.gridx = x++;
		c.gridy = y++;
		c.gridheight = 11;
		c.anchor = GridBagConstraints.NORTHWEST;
		gridbag.setConstraints(sidepanel, c);
		panel.add(sidepanel);

		c.gridx = x++;
		c.fill = GridBagConstraints.VERTICAL;
		label = new JLabel(" ");
		label.setBorder(b);
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridheight = 1;
		c.gridwidth = 2;
		c.gridx = x++;
		c.gridy = y++;
		c.fill = GridBagConstraints.NONE;
		label = new JLabel("Input parameters:");
		label.setFont(GUIUtils.headerFont);
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		c.gridwidth = 1;
		label = new JLabel("Critical (yield) acceleration (g):");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldAc, c);
		panel.add(fieldAc);

		c.gridx = x++;
		c.gridy = y++;
		label = new JLabel("Peak ground acceleration (g):");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldAmax, c);
		panel.add(fieldAmax);

		c.gridx = x++;
		c.gridy = y++;
		label = new JLabel("Peak ground velocity (g):");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldVmax, c);
		panel.add(fieldVmax);

		c.gridx = x++;
		c.gridy = y++;
		label = new JLabel("Arias intensity (m/s):");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldIa, c);
		panel.add(fieldIa);

		c.gridx = x++;
		c.gridy = y++;
		label = new JLabel("Magnitude:");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldM, c);
		panel.add(fieldM);

		c.gridx = x++;
		c.gridy = y++;
		c.insets = top;
		c.gridwidth = 2;
		gridbag.setConstraints(button, c);
		panel.add(button);

		c.gridy = y++;
		label = new JLabel("Results:");
		label.setFont(GUIUtils.headerFont);
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		c.gridwidth = 1;
		c.insets = none;
		label = new JLabel("Estimated Newmark displacement (cm):");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldResCm, c);
		panel.add(fieldResCm);

		c.gridy = y++;
		c.gridx = x++;
		label = new JLabel("Estimated Newmark displacement (in.):");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x;
		gridbag.setConstraints(fieldResIn, c);
		panel.add(fieldResIn);

		c.gridx = 0;
		c.gridy = 11;
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
				ta.setText("");

				fieldAc.setEditable(false);
				fieldAmax.setEditable(false);
				fieldVmax.setEditable(false);
				fieldIa.setEditable(false);
				fieldM.setEditable(false);

				if(Jibson1993.isSelected())
				{
					fieldAc.setEditable(true);
					fieldIa.setEditable(true);
					ta.setText(Jibson1993Str);
				}
				else if(JibsonAndOthers1998.isSelected())
				{
					fieldAc.setEditable(true);
					fieldIa.setEditable(true);
					ta.setText(JibsonAndOthers1998Str);
				}
				else if(Jibson2007CA.isSelected())
				{
					fieldAc.setEditable(true);
					fieldAmax.setEditable(true);
					ta.setText(Jibson2007CAStr);
				}
				else if(Jibson2007CAM.isSelected())
				{
					fieldAc.setEditable(true);
					fieldAmax.setEditable(true);
					fieldM.setEditable(true);
					ta.setText(Jibson2007CAMStr);
				}
				else if(Jibson2007AICA.isSelected())
				{
					fieldAc.setEditable(true);
					fieldIa.setEditable(true);
					ta.setText(Jibson2007AICAStr);
				}
				else if(Jibson2007AICAR.isSelected())
				{
					fieldAc.setEditable(true);
					fieldAmax.setEditable(true);
					fieldIa.setEditable(true);
					ta.setText(Jibson2007AICARStr);
				}
				else if(Ambraseys.isSelected())
				{
					fieldAc.setEditable(true);
					fieldAmax.setEditable(true);
					ta.setText(AmbraseysStr);
				}
				else if(SaygiliRathje2008CARPA.isSelected())
				{
					fieldAc.setEditable(true);
					fieldAmax.setEditable(true);
					ta.setText(SaygiliRathje2008CARPAStr);
				}
				else if(SaygiliRathje2008CARPAPV.isSelected())
				{
					fieldAc.setEditable(true);
					fieldAmax.setEditable(true);
					fieldVmax.setEditable(true);
					ta.setText(SaygiliRathje2008CARPAPVStr);
				}
				else if(SaygiliRathje2008CARPAPVAI.isSelected())
				{
					fieldAc.setEditable(true);
					fieldAmax.setEditable(true);
					fieldVmax.setEditable(true);
					fieldIa.setEditable(true);
					ta.setText(SaygiliRathje2008CARPAPVAIStr);
				}
				else if(SaygiliRathje2009CARPAM.isSelected())
				{
					fieldAc.setEditable(true);
					fieldAmax.setEditable(true);
					fieldM.setEditable(true);
					ta.setText(SaygiliRathje2009CARPAMStr);
				}
			}
			else if(command.equals("do"))
			{
				boolean analysis = true;

				double Ac = 0, Amax = 0, Vmax = 0, Ia = 0, M = 0;
				Double d;

				if(fieldAc.isEditable())
				{
					d = (Double)Utils.checkNum(fieldAc.getText(), "critical acceleration field", null, false, null, new Double(0), true, null, false);
					if(d == null) return;
					Ac = d.doubleValue();
				}

				if(fieldAmax.isEditable())
				{
					d = (Double)Utils.checkNum(fieldAmax.getText(), "peak ground acceleration field", null, false, null, new Double(0), true, null, false);
					if(d == null) return;
					Amax = d.doubleValue();
				}

				if(fieldVmax.isEditable())
				{
					d = (Double)Utils.checkNum(fieldVmax.getText(), "peak ground velocity field", null, false, null, new Double(0), true, null, false);
					if(d == null) return;
					Vmax = d.doubleValue();
				}

				if(fieldIa.isEditable())
				{
					d = (Double)Utils.checkNum(fieldIa.getText(), "Arias intensity field", null, false, null, new Double(0), true, null, false);
					if(d == null) return;
					Ia = d.doubleValue();
				}

				if(fieldM.isEditable())
				{
					d = (Double)Utils.checkNum(fieldM.getText(), "magnitude field", null, false, null, new Double(0), true, null, false);
					if(d == null) return;
					M = d.doubleValue();
				}

				if(Jibson1993.isSelected())
					fieldResCm.setText(RigidBlockSimplified.Jibson1993(Ia, Ac));
				else if(JibsonAndOthers1998.isSelected())
					fieldResCm.setText(RigidBlockSimplified.JibsonAndOthers1998(Ia, Ac));
				else if(Jibson2007CA.isSelected())
					fieldResCm.setText(RigidBlockSimplified.Jibson2007CA(Ac, Amax));
				else if(Jibson2007CAM.isSelected())
					fieldResCm.setText(RigidBlockSimplified.Jibson2007CAM(Ac, Amax, M));
				else if(Jibson2007AICA.isSelected())
					fieldResCm.setText(RigidBlockSimplified.Jibson2007AICA(Ia, Ac));
				else if(Jibson2007AICAR.isSelected())
					fieldResCm.setText(RigidBlockSimplified.Jibson2007AICAR(Ia, Ac, Amax));
				else if(Ambraseys.isSelected())
					fieldResCm.setText(RigidBlockSimplified.AmbraseysAndMenu(Amax, Ac));
				else if(SaygiliRathje2008CARPA.isSelected())
					fieldResCm.setText(RigidBlockSimplified.SaygiliRathje2008CARPA(Ac, Amax));
				else if(SaygiliRathje2008CARPAPV.isSelected())
					fieldResCm.setText(RigidBlockSimplified.SaygiliRathje2008CARPAPV(Ac, Amax, Vmax));
				else if(SaygiliRathje2008CARPAPVAI.isSelected())
					fieldResCm.setText(RigidBlockSimplified.SaygiliRathje2008CARPAPVAI(Ac, Amax, Vmax, Ia));
				else if(SaygiliRathje2009CARPAM.isSelected())
					fieldResCm.setText(RigidBlockSimplified.SaygiliRathje2009CARPAM(Ac, Amax, M));
				else
				{
					analysis = false;
					GUIUtils.popupError("No function selected.");
				}

				if(analysis)
					fieldResIn.setText(Analysis.fmtOne.format(Double.parseDouble(fieldResCm.getText()) / 2.54));
			}
		}
		catch (Exception ex)
		{
			Utils.catchException(ex);
		}
	}
}
