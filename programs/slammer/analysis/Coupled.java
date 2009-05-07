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

public class Coupled extends DeCoupledCommon
{
	//slide=0 no sliding, slide=1 sliding
	//variable that end in 1 are for previous time step
	//variable that end in 2 are for current time step

	private static double s1, sdot1, sdotdot1;
	private static double s2, sdot2, sdotdot2;
	private static double u1, udot1, udotdot1,avgacc[];
	private static double u2, udot2, udotdot2, baseacc;
	private static double basef, acc11, acc22, normalf1, normalf2;

	private static double COS, SIN, gSIN, gCOS;
	
	private static double acc1, acc2;
	private static double udot[];

	public static double Coupled(double[] ain_p, double uwgt_p, double height_p, double vs_p, double damp1_p, double dt_p, double scal_p, double g_p, double vr_p, double[][] ca, boolean dv3_p)
	{
		// assign all passed parameters to the local data
		uwgt = uwgt_p;
		height = height_p;
		vs = vs_p;
		vs1 = vs_p;
		damp1 = damp1_p;
		damp = damp1;
		dt = dt_p;
		scal = scal_p;
		g = g_p;
		vr = vr_p;
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

		npts = ain.length;

		// precompute some numbers
		COS = Math.cos(angle * Math.PI / 180.0);
		SIN = Math.sin(angle * Math.PI / 180.0);

		gCOS = g * COS;
		gSIN = g * SIN;

		delt = 0.0;
		j = 0;

		s1 = 0.0;
		sdot1 = 0.0;
		sdotdot1 = 0.0;
		s2 = 0.0;
		sdot2 = 0.0;
		sdotdot2 = 0.0;
		u1 = 0.0;
		udot1 = 0.0;
		udotdot1 = 0.0;
		u2 = 0.0;
		udot2 = 0.0;
		udotdot2 = 0.0;
		baseacc = 0.0;
		basef = 0.0;
		acc11 = 0.0;
		acc22 = 0.0;
		normalf1 = 0.0;
		normalf2 = 0.0;
		mx = 0.0;
		mx1 = 0.0;
		mmax = 0.0;
		gameff1 = 0.0;
		s = new double[ain.length];
		u = new double[ain.length];
		udotdot = new double[ain.length];
		avgacc = new double[npts];
		udot = new double[npts];


		//These are previous iteration value

		rho = uwgt / g;
		nmu = ca.length;

		dampf = 55.016 * Math.pow((vr / vs), -0.9904) / 100.0;
		if(dampf > 0.2)
			dampf = 0.2;

		// for each mode calculate constants for Slammer algorithm
		/////////////////////////////////////////////////////////////////////////

		beta = 0.25;
		gamma = 0.5;
		Mtot = rho * height;
		slide = false;
		normalf2 = 0.0;
		angle = 0.0;
		//qq indicates which mu is in effect
		qq = 1;
		omega = Math.PI * vs / (2.0 * height);
		L = 2.0 * rho * height / Math.PI;
		M = rho * height / 2.0;

		damp = damp1 + dampf;
		n = 100.0;
		o = 100.0;
		gamref = 0.05;

		// Finding Equivalent Linear Properties of Soil

		if(dv3)
			c_eq();

		// Calculate decoupled dynamic response and Kmax using LE properties or final EQL properties
		//   These values not used to calculate coupled displacements but provide information on general dynamic response

		for (j = 1; j <= npts; j++)
		{
			d_setupstate();
			d_response();
		}

		avg_acc();
		


		// Loop for time steps in time histories

		s1 = 0.0;
		sdot1 = 0.0;
		sdotdot1 = 0.0;
		s2 = 0.0;
		sdot2 = 0.0;
		sdotdot2 = 0.0;
		u1 = 0.0;
		udot1 = 0.0;
		udotdot1 = 0.0;
		u2 = 0.0;
		udot2 = 0.0;
		udotdot2 = 0.0;
		baseacc = 0.0;
		basef = 0.0;
		acc11 = 0.0;
		acc22 = 0.0;
		normalf1 = 0.0;
		normalf2 = 0.0;
		gameff1 = 0.0;
		omega = Math.PI * vs / (2.0 * height);

		for(j = 0; j < npts; j++)
		{
			s[j] = 0;
			u[j] = 0;
	//		avgacc[j] = 0;
		}

		for(j = 1; j <= npts; j++)
		{
			coupled_setupstate(j);

			// Solve for u, udot, udotdot at next time step
			////////////////////////////////////////////////

			solvu(j);

			udotdot[j - 1] = udotdot2;

			///// Update sliding acceleration based on calc'd response
			c_slideacc();

			/// Check if sliding has started
			c_slidingcheck();

			s[j - 1] = s2;

			store(Math.abs(s2));

			//System.out.println((j * dt) + ":  " + s[j - 1] + ", " + slide);

			residual_mu();
		}

		end(Math.abs(s2));
		return Math.abs(s2);
	}

	// Subroutine for the end of sliding
	private static void slidestop()
	{
		double ddt, acc11, acc22;
		double acc1b, delt, dd;
		double khat, deltp, a, b;

		delt = dt;

		//// Time of end of sliding is taken as where sdot=0 from previous
		//// analysis assuming sliding thruoughout the time step
		///////////////////////////////////////////////////////////////////
		dd = -sdot1 / (sdot2 - sdot1);
		ddt = dd * delt;
		acc11 = gSIN - mu[qq - 1] * (gCOS + ain[j - 1] * scal * gSIN);
		acc1b = ain[j - 2] * g * scal + dd * (ain[j - 1] - ain[j - 2]) * g * scal;
		acc22 = gSIN - mu[qq - 1] * (gCOS + acc1b * SIN);

		//if dd=0, sliding has already stopped and skip this solution

		if(dd == 0)
			return;

		solvu(j);
		u1 = u2;
		udot1 = udot2;
		udotdot1 = udotdot2;
		normalf2 = Mtot * gCOS + Mtot * acc1b * SIN;
		sdotdot2 =  - acc1b * COS - mu[qq - 1] * normalf2 / Mtot - L * udotdot2 / Mtot + gSIN;
		sdot2 = sdot1 + 0.5 * ddt * (sdotdot2 + sdotdot1);
		s2 = s1 + 0.5 * ddt * (sdot1 + sdot2);

		// Solve for non sliding response during remaining part of dt
		////////////////////////////////////////////////////////////////

		ddt = (1.0 - dd) * delt;
		slide = false;
		acc11 = acc22;
		acc22 = ain[j - 1] * gCOS * scal;

		khat = 1.0 + 2.0 * damp * omega * gamma * ddt + (omega * omega) * beta * (ddt * ddt);
		a = (1.0 - (L * L) / (Mtot * M)) + 2.0 * damp * omega * ddt * (gamma - 1.0) + (omega * omega) * (ddt * ddt) * (beta - 0.5);
		b = (omega * omega) * ddt;
		deltp =  - L / M * (acc22 - acc11) + a * (udotdot1) - b * (udot1);
		udotdot2 = deltp / khat;

		udot2 = udot1 + (1.0 - gamma) * ddt * (udotdot1) + gamma * ddt * (udotdot2);
		u2 = u1 + udot1 * ddt + (0.5 - beta) * (ddt * ddt) * (udotdot1) + beta * (ddt * ddt) * (udotdot2);
	}

	//solves for u, udot, and udotdot at next time step
	private static void solvu(int jj)
	{
		double khat, a, b, deltp, deltu, deltudot;
		double d1;

		delt = dt;

		if(slide)
			d1 = 1.0 - (L * L) / (M * Mtot);
		else
			d1 = 1.0;

		khat = (omega * omega) + 2.0 * damp * omega * gamma / (beta * delt) + d1 / (beta * (delt * delt));
		a = d1 / (beta * delt) + 2.0 * damp * omega * gamma / beta;
		b = d1 / (2.0 * beta) + delt * 2.0 * damp * omega * (gamma / (2.0 * beta) - 1.0);

		if(jj == 1)
		{
			deltp =  - L / M * (acc22 - acc11);
			deltu = deltp / khat;
			deltudot = gamma / (beta * delt) * deltu;
			u2 = deltu;
			udot2 = deltudot;
			udotdot2 = ( - (L / M) * acc22 - 2.0 * damp * omega * udot2 - (omega * omega) * u2) / d1;
		}
		else
		{
			deltp =  - L / M * (acc22 - acc11) + a * udot1 + b * udotdot1;
			deltu = deltp / khat;
			deltudot = gamma / (beta * delt) * deltu - gamma / beta * udot1 + delt * (1.0 - gamma / (2.0 * beta)) * udotdot1;
			u2 = u1 + deltu;
			udot2 = udot1 + deltudot;
			udotdot2 = ( - (L / M) * acc22 - 2.0 * damp * omega * udot2 - (omega * omega) * u2) / d1;
		}
	//	System.out.println("COUP avgagg" + "  " + jj + "  " + acc22 + "  " + L + "  " + M + "  " + udotdot2); 
	//	avgacc[jj - 1] = acc22 + (L / M) * udotdot2;
		u[jj - 1] = u2;
	}


	private static void coupled_setupstate(int jj)
	{
		// set up state from previous time step
		if(jj == 1)
		{
			u1 = 0.0;
			udot1 = 0.0;
			udotdot1 = 0.0;
			s1 = 0.0;
			sdot1 = 0.0;
			sdotdot1 = 0.0;
			normalf1 = 0.0;
		}
		else
		{
			u1 = u2;
			udot1 = udot2;
			udotdot1 = udotdot2;
			s1 = s2;
			sdot1 = sdot2;
			sdotdot1 = sdotdot2;
			normalf1 = normalf2;
		}

		// Set up acceleration loading
		///////////////////////////////////////

		// Normal force corrected for vertical component of accel
		/////////////////////////////////////////////////////////

		normalf2 = Mtot * gCOS + Mtot * ain[jj - 1] * scal * gSIN;

		if(jj == 1)
		{
			acc11 = 0.0;
			acc22 = ain[jj - 1] * gCOS * scal;
		}
		else if(!slide)
		{
			acc11 = ain[jj - 2] * gCOS * scal;
			acc22 = ain[jj - 1] * gCOS * scal;
		}
		else
		{
			acc11 = gSIN - mu[qq - 1] * normalf1 / Mtot;
			acc22 = gSIN - mu[qq - 1] * normalf2 / Mtot;
		}
	}

	private static void c_slidingcheck()
	{
		/// Check if sliding has started
		if(!slide)
		{
			if(basef > mu[qq - 1] * normalf2)
				slide = true;
		}
		else if(slide)
		{
			if(sdot2 <= 0.0)
			{
				slidestop();

				slide = false;
				sdot2 = 0.0;
				sdotdot2 = 0.0;
			}
		}
	}

	private static void c_slideacc()
	{
				
		///// Update sliding acceleration based on calc'd response

		if(slide)
			sdotdot2 =  - ain[j - 1] * gCOS * scal - mu[qq - 1] * normalf2 / Mtot - L * udotdot2 / Mtot + gSIN;

		//////  Calc. base force based on udotdot calc

		
		basef =  - Mtot * ain[j - 1] * gCOS * scal - L * udotdot2 + Mtot * gSIN;

		/////  If sliding is occuring, integrate sdotdot,
		/////  using trapezoid rule, to get sdot and s
		//////////////////////////////////////////////////

		if(slide)
		{
			sdot2 = sdot1 + 0.5 * dt * (sdotdot2 + sdotdot1);
			s2 = s1 + 0.5 * dt * (sdot2 + sdot1);
		}
	}

	private static void c_eq()
	{
		int t = 0, jj;

		while(n > 5.0 || o > 5.0)
		{
			acc1 = 0.0;
			acc2 = 0.0;

			for (j = 1; j <= npts; j++)
			{
				d_setupstate();
				d_response();
			}

			for (j = 1; j <= npts; j++)
				effstr();

			eq_property();

			//* Helpful debugging output

			t++;
			System.out.println("EQL ITERATION" + "  " + t + "  " + vs + "  " + damp + "  " + dampf + "  " + gameff1);
			//*/
		}
	}


	private static void d_response()
	{
		double khat, omega, a, b;
		double deltp, deltu, deltudot, deltudotdot, u2, udot2, udotdot2;

		omega = Math.PI * vs / (2.0 * height);

		khat = (omega * omega) + 2.0 * damp * omega * gamma / (beta * dt) + 1.0 / (beta * (dt * dt));
		a = 1.0 / (beta * dt) + 2.0 * damp * omega * gamma / beta;
		b = 1.0 / (2.0 * beta) + dt * 2.0 * damp * omega * (gamma / (2.0 * beta) - 1.0);

		if (j == 1)
		{
			deltp = -L / M * (acc2 - acc1);
			deltu = deltp / khat;
			deltudot = gamma / (beta * dt) * deltu;
			u2 = deltu;
			udot2 = deltudot;
			udotdot2 = -(L / M) * acc2 - 2.0 * damp * omega * udot2 - (omega * omega) * u2;
		}
		else
		{
			deltp = -L / M * (acc2 - acc1) + a * udot1 + b * udotdot1;
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
		if (j == 1)
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

		if (j == 1)
		{
			acc1 = 0.0;
			acc2 = ain[j - 1] * g * scal;
		}
		else if (!slide)
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

		System.out.println("Kmax: " + mmax);
	}

}
