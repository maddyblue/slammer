/*
 * Copyright (C) 2004 Matthew Jibson (dolmant@dolmant.net)
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

package newmark.analysis;

import newmark.*;

public class Decoupled extends Analysis
{
	// main function parameters
	private static double uwgt, height, vs, damp, dt, scal, g, vr, vs1;
	private static int dv2, dv3;

	// main function variables
	private static double rho, dampf;
	private static int j;
	private static boolean slide;

	private static double Mtot, M, L, omega, avgacc[], deltacc;
	private static double ain[], s[], sdot[], disp[], mu[];
	private static double u[], udot[], udotdot[];
	private static double u1, udot1, udotdot1;

	private static double beta, gamma, acc1, acc2;
	private static double mx, mx1, mmax, gameff1;
	private static double gamref;
	private static double n, o;
	private static int npts, nmu;

	private static double time;

	private static int qq;

	public static double Decoupled(double[] ain_p, double uwgt_p, double height_p, double vs_p, double damp_p, double dt_p, double scal_p, double g_p, double vr_p, double[][] ca, int dv2_p, int dv3_p)
	{
		// assign all passed parameters to the local data
		uwgt = uwgt_p;
		height = height_p;
		vs = vs_p;
		vs1 = vs;
		damp = damp_p;
		dt = dt_p;
		scal = scal_p;
		g = g_p;
		vr = vr_p;
		dv2 = dv2_p;
		dv3 = dv3_p;
		ain = ain_p;

		// init graphing
		setValueSize(dt);

		// copy ca into disp and mu
		disp = new double[ca.length];
		mu = new double[ca.length];
		for(int i = 0; i < ca.length; i++)
		{
			disp[i] = ca[i][0];
			mu[i] = ca[i][1];
		}

		nmu = ca.length;

		npts = ain.length;

		avgacc = new double[npts];
		s = new double[npts];
		sdot = new double[npts];
		u = new double[npts];
		udot = new double[npts];
		udotdot = new double[npts];

		deltacc = 0.0;
		u1 = 0.0;
		udot1 = 0.0;
		udotdot1 = 0.0;

		acc1 = 0.0;
		acc2 = 0.0;
		mx = 0.0;
		mx1 = 0.0;
		mmax = 0.0;
		gameff1 = 0.0;
		n = 100.0;
		o = 100.0;

		rho = uwgt / g;

		if(dv2 == 0)
			dampf = 0.0;
		else if(dv2  ==  1)
			dampf = 55.016 * Math.pow((vr / vs), -0.9904) / 100.0;

		/* Helpful debugging output
		int i;
		System.out.println("Density : " + rho);
		System.out.println("Height : " + height);

		if(nmu==1)
		{
			System.out.println("Yield Acceleration Coeff. : " + mu[0]);
		}
		if(!(nmu==1))
		{
			for(i=1;i<=nmu;i++)
			{
				System.out.println("Yield Acceleration Coeff.: " + mu[i-1] + "   over Displacement " + disp[i-1]);
			}
		}

		System.out.println("Dynamic Properties");
		System.out.println("Shear Wave Velocity" + "  " + "Damping Ratio");
		System.out.println("Soil" + "  " + "Rock" + "  " + "Soil" + "  " + "Foundation" + "  " + "Total");
		System.out.println("INITIAL" + "  " + vs + "  " + vr + "  " + damp + "  " + dampf + "  " + (damp+dampf));
		// */

		// for each mode calculate constants for Newmark algorithm
		//////////////////////////////////////////////////////////////////////// /

		beta = 0.25;
		gamma = 0.5;
		Mtot = rho * height;
		slide = true;
		qq = 1;

		omega = Math.PI * vs / (2.0 * height);
		L =  - 2.0 * rho * height / (Math.PI * Math.cos(Math.PI));
		M = rho * height / 2.0;

		damp += dampf;
		n = 100.0;
		o = 100.0;
		gamref = 0.13;

		// Loop for time steps in time histories

		// For Equivalent Linear
		if(dv3 == 1)
		{
			d_eq();
		}

		// For Linear Elastic
		for(j = 1; j <= npts; j++)
		{
			d_setupstate(j);
			d_response(j);
		}

		slide = false;
		time = 0.0;

		avg_acc();

		// Calculate decoupled displacements
		for(j = 1;j <= npts;j++)
		{
			d_sliding();

			if(scal > 0)
				store(Math.abs(s[j - 1]));

			//System.out.println((j * dt) + ":  " + s[j - 1]);

			residual_mu();
		}

		end(Math.abs(s[npts - 1]));
		return Math.abs(s[npts - 1]);
	}

	private static void d_response(int jj)
	{
		double khat, gamma, beta, a, b, L, M, Mtot;
		double deltp, deltu, deltudot, deltudotdot, u2, udot2, udotdot2;

		beta = 0.25;
		gamma = 0.5;
		Mtot = rho * height;

		omega = Math.PI * vs / (2.0 * height);
		L =  - 2.0 * rho * height / (Math.PI * Math.cos(Math.PI));
		M = rho * height / 2.0;

		khat = (omega * omega) + 2.0 * damp * omega * gamma / (beta * dt) + 1.0 / (beta * (dt * dt));
		a = 1.0 / (beta * dt) + 2.0 * damp * omega * gamma / beta;
		b = 1.0 / (2.0 * beta) + dt * 2.0 * damp * omega * (gamma / (2.0 * beta) - 1.0);

		if(jj == 1)
		{
			deltp =  - L / M * (acc2 - acc1);
			deltu = deltp / khat;
			deltudot = gamma / (beta + dt) * deltu;
			u2 = deltu;
			udot2 = deltudot;
			udotdot2 =  - (L / M) * acc2 - 2.0 * damp * omega * udot2 - (omega * omega) * u2;
		}
		else
		{
			deltp =  - L / M * (acc2 - acc1) + a * udot1 + b * udotdot1;
			deltu = deltp / khat;
			deltudot = gamma / (beta * dt) * deltu - gamma / beta * udot1 + dt * (1.0 - gamma / (2.0 * beta)) * udotdot1;
			deltudotdot = 1.0 / (beta * (dt * dt)) * dt - 1.0 / (beta * dt) * udot1 - 0.5 / beta * udotdot1;
			u2 = u1 + deltu;
			udot2 = udot1 + deltudot;
			udotdot2 = udotdot1 + deltudotdot;
			udotdot2 =  - (L / M) * acc2 - 2.0 * damp * omega * udot2 - (omega * omega) * u2;
		}

		avgacc[jj - 1] = acc2;

		u[jj - 1] = u2;
		udot[jj - 1] = udot2;
		udotdot[jj - 1] = udotdot2;
		avgacc[jj - 1] = avgacc[jj - 1] + L / Mtot * udotdot[jj - 1];
	}

	private static void d_setupstate(int jj)
	{
		//set up state from previous time step
		if(jj == 1)
		{
			u1 = 0.0;
			udot1 = 0.0;
			udotdot1 = 0.0;
		}
		else
		{
			u1 = u[jj - 2];
			udot1 = udot[jj - 2];
			udotdot1 = udotdot[jj - 2];
		}

		// Set up acceleration loading

		if(jj == 1)
		{
			acc1 = 0.0;
			acc2 = ain[jj - 1] * g * scal;
		}
		else if(!slide)
		{
			acc1 = ain[jj - 2] * g * scal;
			acc2 = ain[jj - 1] * g * scal;
			s[jj - 1] = s[jj - 2];
		}
		else
		{
			acc1 = ain[jj - 2] * g * scal;
			acc2 = ain[jj - 1] * g * scal;
		}
	}

	private static void d_sliding()
	{
		// Calculate decoupled displacements

		double deltacc;

		if(j == 1)
			deltacc = avgacc[j - 1];
		else
			deltacc = avgacc[j - 1] - avgacc[j - 2];

		if(!slide)
		{
			sdot[j - 1] = 0;

			if(j == 1)
				s[j - 1] = 0;
			else
				s[j - 1] = s[j - 2];
		}
		else
		{
			if(j == 1)
			{
				sdot[j - 1] = 0;
				s[j - 1] = 0;
			}
			else
			{
				sdot[j - 1] = sdot[j - 2] + (mu[qq - 1] * g - avgacc[j - 2]) * dt - 0.5 * deltacc * dt;
				s[j - 1] = s[j - 2] + sdot[j - 2] * dt + 0.5 * dt * dt * (mu[qq - 1] * g - avgacc[j - 2]) - deltacc * dt * dt / 6.0;
			}
		}

		if(!slide)
		{
			if(avgacc[j - 1] > mu[qq - 1] * g)
				slide = true;
		}
		else
		{
			if(sdot[j - 1] >= 0.0)
			{
				slide = false;

				if(j == 1)
					s[j - 1] = 0;
				else
					s[j - 1] = s[j - 2];

				sdot[j - 1] = 0.0;
			}
		}
	}

	private static void d_eq()
	{
		int jj, t = 0;

		while(n > 5 || o > 5)
		{
			for(jj = 1; jj <= npts; jj++)
			{
				d_setupstate(jj);
				d_response(jj);
			}

			for(jj = 1;jj <= npts;jj++)
				effstr();

			eq_property();
		}
	}

	private static void effstr()
	{
		//effective shear strain calculation

		double mx1 = 0.0, mx = 0.0, mmax;
		int j;

		for(j = 1; j <= npts; j++)
		{
			if (j == 1)
			{
				mx1 = u[j - 1];
				mx = u[j - 1];
			}
			else
			{
				if(u[j - 1] < 0)
				{
					if(u[j - 1] <= mx1)
						mx1 = u[j - 1];
					else
						mx1 = mx1;
				}
				else
				{
					if(u[j - 1] >= mx)
						mx = u[j - 1];
					else
						mx = mx;
				}
			}

			if(j == npts)
			{
				if(Math.abs(mx) > Math.abs(mx1))
				{
					mmax = mx;
					gameff1 = 0.65 * mmax / height;
				}
				else if(Math.abs(mx) < Math.abs(mx1))
				{
					mmax = mx1;
					gameff1 = 0.65 * mmax / height;
				}
				else
				{
					if(mx>0)
					{
						mmax = mx;
						gameff1 = 0.65 * mmax / height;
					}
					else
					{
						mmax = mx1;
						gameff1 = 0.65 * mmax / height;
					}
				}
			}
		}

		gameff1 = Math.abs(gameff1);
	}

	private static void eq_property()
	{
		double gameff2, vs2, com1, com2, damp2, G1, G2, l, m;

		gameff2 = Math.abs(gameff1) * 100.0;
		vs2 = vs1 / Math.sqrt(1 + (gameff2 / gamref));
		com1 = 1.0 / (1.0 + gameff2 / gamref);
		com2 = Math.pow(com1, 0.1);

		if(dv2 == 0)
			dampf = 0.0;
		else if(dv2 == 1)
			dampf = 55.016 * Math.pow((vr / vs2), -0.9904); // should this also be "/ 100.0" like it is in the main Coupled() function?

		damp2 = dampf + 0.62 * com2 * (100.0 / Math.PI * (4.0 * ((gameff2 - gamref * Math.log((gamref + gameff2) / gamref)) / (gameff2 * gameff2 / (gameff2 + gamref))) - 2.0)) + 1.0;

		G1 = (uwgt / g) * vs * vs;
		G2 = (uwgt / g) * vs2 * vs2;

		l = (G1 - G2) / G1;
		m = ((damp * 100.0) - damp2) / (damp * 100.0);

		n = Math.abs(l) * 100.0;
		o = Math.abs(m) * 100.0;

		vs = vs2;
		damp = damp2 * 0.01;
		dampf = dampf * 0.01;
	}

	private static void avg_acc()
	{
		//effective shear strain calculation

		double mx1 = 0.0, mx = 0.0;
		int jj;

		for(jj = 1; jj <= npts; jj++)
		{
			if (jj == 1)
			{
				mx1 = avgacc[jj - 1];
				mx = avgacc[jj - 1];
			}
			else
			{
				if(avgacc[jj - 1] < 0.0)
				{
					if(avgacc[jj - 1] <= mx1)
						mx1 = avgacc[jj - 1];
					else
						mx1 = mx1;
				}
				else
				{
					if(avgacc[jj - 1] >= mx)
						mx = avgacc[jj - 1];
					else
						mx = mx;
				}
			}

			if(jj == npts)
			{
				if(Math.abs(mx) > Math.abs(mx1))
					mmax = mx;
				else if(Math.abs(mx) < Math.abs(mx1))
					mmax = mx1;
				else
				{
					if(mx > 0.0)
						mmax = mx;
					else
						mmax = mx1;
				}
			}
		}
	}

	private static void residual_mu()
	{
		if(nmu > 1)
		{
			if(!slide && (Math.abs(s[j - 1]) >= disp[qq - 1]))
			{
				if(qq <= nmu - 1)
					qq++;
			}
		}
	}
}
