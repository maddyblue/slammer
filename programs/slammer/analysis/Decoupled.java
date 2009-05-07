/*
 * Originally written by Yong-Woo Lee and Ellen Rathje for the SLAMMER project.
 * This work has been placed into the public domain. You may use this work in
 * any way and for any purpose you wish.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */

package slammer.analysis;

import slammer.*;

public class Decoupled extends DeCoupledCommon
{
	private static double avgacc[], deltacc;
	private static double sdot[];
	private static double udot[];
	private static double u1, udot1, udotdot1;

	private static double acc1, acc2;

	private static double time;

	public static double Decoupled(double[] ain_p, double uwgt_p, double height_p, double vs_p, double damp1_p, double dt_p, double scal_p, double g_p, double vr_p, double[][] ca, boolean dv3_p)
	{
		// assign all passed parameters to the local data
		uwgt = uwgt_p;
		height = height_p;
		vs = vs_p;
		vs1 = vs;
		damp1 = damp1_p;
		damp = damp1;
		dt = dt_p;
		scal = scal_p;
		g = g_p;
		vr = vr_p;
		dv3 = dv3_p;
		ain = ain_p;

		if ((g < 33.) && (g > 32.))
		{
			uwgt = 120.;
		}
		else
		{
			uwgt = 20.;
		}

		//System.out.println("INIT VALUES " + vs + "  " + vr + "  " + uwgt + "     " + damp1 + "  " + damp);


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

		dampf = 55.016 * Math.pow((vr / vs), -0.9904) / 100.0;
		if(dampf > 0.2)
			dampf = 0.2;

		/* The following block appears in the original code. We will do the same
		 * thing by using a scaling factor multiplier.
		for(j = 1; j <= npts; j++)
			ain[j-1] *= -1.0;
		*/
		scal *= -1.0;

		// for each mode calculate constants for Slammer algorithm
		//////////////////////////////////////////////////////////////////////// /

		beta = 0.25;
		gamma = 0.5;
		Mtot = rho * height;
		slide = true;
		qq = 1;

		omega = Math.PI * vs / (2.0 * height);
		L =  - 2.0 * rho * height / Math.PI * Math.cos(Math.PI);
		M = rho * height / 2.0;

		n = 100.0;
		o = 100.0;
		gamref = 0.05;
		damp = damp1 + dampf;

		// Loop for time steps in time histories

		// For Equivalent Linear
		if(dv3)
			d_eq();

		/* Helpful debugging output
		int i;
		System.out.println("Density : " + rho);
		System.out.println("Height : " + height);

		if(nmu==1)
		{
			System.out.println("Yield Acceleration Coeff. : " + mu[0]);
		}
		else
		{
			for(i = 1; i <= nmu; i++)
			{
				System.out.println("Yield Acceleration Coeff.: " + mu[i-1] + "   over Displacement " + disp[i-1]);
			}
		}

		System.out.println("Dynamic Properties");
		System.out.println("Shear Wave Velocity" + "  " + "Damping Ratio");
		System.out.println("Soil" + "  " + "Rock" + "  " + "Soil" + "  " + "Foundation" + "  " + "Total");
		System.out.println("INITIAL" + "  " + vs + "  " + vr + "  " + damp + "  " + dampf + "  " + (damp+dampf));
		// */

		omega = Math.PI * vs / (2.0 * height);

	

		//if(!dv2)
		//	dampf = 0.0;
		//else
		//	dampf = 55.016 * Math.pow((vr / vs), -0.9904) / 100.0;

		// Calculate final dynamic response using original prop's for LE analysis and EQL properties for EQL analysis
			for (j = 1; j <= npts; j++)
			{
				d_setupstate();
				d_response();
			}
		  

		slide = false;
		time = 0.0;

		System.out.println("DEC FINAL" + "  " + vs + "  " + damp + "  " + dampf + "  " + gameff1);


		avg_acc();

		// Calculate decoupled displacements
		for(j = 1; j <= npts; j++)
		{
			d_sliding();

			store(Math.abs(s[j - 1]));

			//System.out.println((j * dt) + ": " + s[j - 1]);

			residual_mu();
		}

		end(Math.abs(s[npts - 1]));
		return Math.abs(s[npts - 1]);
	}

	private static void d_response()
	{
		double khat, omega, a, b;
		double deltp, deltu, deltudot, deltudotdot, u2, udot2, udotdot2;

		omega = Math.PI * vs / (2.0 * height);

		khat = (omega * omega) + 2.0 * damp * omega * gamma / (beta * dt) + 1.0 / (beta * (dt * dt));
		a = 1.0 / (beta * dt) + 2.0 * damp * omega * gamma / beta;
		b = 1.0 / (2.0 * beta) + dt * 2.0 * damp * omega * (gamma / (2.0 * beta) - 1.0);

		if(j == 1)
		{
			deltp =  - L / M * (acc2 - acc1);
			deltu = deltp / khat;
			deltudot = gamma / (beta * dt) * deltu;
			u2 = deltu;
			udot2 = deltudot;
			udotdot2 =  - (L / M) * acc2 - 2.0 * damp * omega * udot2 - (omega * omega) * u2;
		}
		else
		{
			deltp =  - L / M * (acc2 - acc1) + a * udot1 + b * udotdot1;
			deltu = deltp / khat;
			deltudot = gamma / (beta * dt) * deltu - gamma / beta * udot1 + dt * (1.0 - gamma / (2.0 * beta)) * udotdot1;
			deltudotdot = 1.0 / (beta * (dt * dt)) * deltu - 1.0 / (beta * dt) * udot1 - 0.5 / beta * udotdot1;
			u2 = u1 + deltu;
			udot2 = udot1 + deltudot;
			udotdot2 = udotdot1 + deltudotdot;
		}

		avgacc[j - 1] = acc2;
		u[j - 1] = u2;
		udot[j - 1] = udot2;
		udotdot[j - 1] = udotdot2;
		avgacc[j - 1] = avgacc[j - 1] + L / Mtot * udotdot[j - 1];
	}

	private static void d_setupstate()
	{
		//set up state from previous time step
		if(j == 1)
		{
			u1 = 0.0;
			udot1 = 0.0;
			udotdot1 = 0.0;
		}
		else
		{
			u1 = u[j - 2];
			udot1 = udot[j - 2];
			udotdot1 = udotdot[j - 2];
		}

		// Set up acceleration loading

		if(j == 1)
		{
			acc1 = 0.0;
			acc2 = ain[j - 1] * g * scal;
		}
		else if(!slide)
		{
			acc1 = ain[j - 2] * g * scal;
			acc2 = ain[j - 1] * g * scal;
			s[j - 1] = s[j - 2];
		}
		else
		{
			acc1 = ain[j - 2] * g * scal;
			acc2 = ain[j - 1] * g * scal;
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

		if(j == 1) // ADDED
		{
			sdot[j - 1] = 0;
			s[j - 1] = 0;
		}
		else if(!slide)
		{
			sdot[j - 1] = 0;
			s[j - 1] = s[j - 2];
		}
		else
		{
			sdot[j - 1] = sdot[j - 2] + (mu[qq - 1] * g - avgacc[j - 2]) * dt - 0.5 * deltacc * dt;
			s[j - 1] = s[j - 2] - sdot[j - 2] * dt - 0.5 * dt * dt * (mu[qq - 1] * g - avgacc[j - 2]) + deltacc * dt * dt / 6.0;
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
		int t = 0;
	
		while(n > 5.0 || o > 5.0)
		{
			for(j = 1; j <= npts; j++)
			{
				d_setupstate();
				d_response();
			}
	
			for(j = 1; j <= npts; j++)
				effstr();
	
			eq_property();
	
			//* Helpful debugging output
	
			t++;
			System.out.println("DEC ITERATION" + "  " + t + "  " + vs + "  " + damp + "  " + dampf + "  " + gameff1);
			//*/
		}
	}

	private static void avg_acc()
	{
		//Calculate Kmax

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
				}
				else
				{
					if(avgacc[jj - 1] >= mx)
						mx = avgacc[jj - 1];
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

		System.out.println("DEC Kmax: " + mmax);
	}
}
