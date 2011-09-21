/* This file is in the public domain. */

package slammer.analysis;

public abstract class DeCoupledCommon extends Analysis
{
	// main function parameters
	protected double uwgt, height, vs, damp, damp1, dt, scal, g, vr, vs1;
	protected boolean dv2 = true, dv3;

	// main function variables
	protected double Mtot, M, L, omega, beta, gamma, angle;
	protected int qq, nmu, npts;

	protected double rho, delt, dampf, damps, damps_prev;
	protected int j;

	/*
	 * slide=0 no sliding, slide=1 sliding
	 * variable that end in 1 are for previous time step
	 * variable that end in 2 are for current time step
	 */

	protected boolean slide;

	protected double mx, mx1, gameff1, gamref, n, o, acc1, acc2, u1, udot1, udotdot1;
	protected double s[], u[], udot[], udotdot[], disp[], mu[], avgacc[];

	protected double ain[];

	protected void eq_property()
	{
		double gameff2, vs2, com1, com2, damp2, G1, G2, l, m;

		gameff2 = Math.abs(gameff1) * 100.0;
		vs2 = vs1 / Math.sqrt(1.0 + (gameff2 / gamref));
		com1 = 1.0 / (1.0 + gameff2 / gamref);
		com2 = Math.pow(com1, 0.1);

		damps = 0.0;

		if(!dv2)
			dampf = 0.0;
		else
		{
			dampf = 55.016 * Math.pow((vr / vs2), -0.9904);
			if(dampf > 20.0)
				dampf = 20.0;
		}

		damps = 0.62 * com2 * (100.0 / Math.PI * (4.0 * ((gameff2 - gamref * Math.log((gamref + gameff2) / gamref)) / (gameff2 * gameff2 / (gameff2 + gamref))) - 2.0)) + 1.0;
		damp2 = dampf + damps;

		G1 = (uwgt / g) * vs * vs;
		G2 = (uwgt / g) * vs2 * vs2;

		l = (G1 - G2) / G1;
		m = ((damps_prev - damps) / damps_prev);

		n = Math.abs(l) * 100.0;
		o = Math.abs(m) * 100.0;

		vs = vs2;

		damps_prev = damps;
		damp = damp2 * 0.01;
		dampf = dampf * 0.01;
	}

	protected void residual_mu()
	{
		if(nmu > 1)
		{
			if(!slide && (Math.abs(s[j - 1]) >= disp[qq - 1]))
			{
				if(qq <= (nmu - 1))
					qq++;
			}
		}
	}

	protected void effstr()
	{
		// effective shear strain calculation

		double mx1 = 0.0, mx = 0.0, mmax = 0.0;
		int jj;

		for(jj = 1; jj <= npts; jj++)
		{
			if (jj == 1)
			{
				mx1 = u[jj - 1];
				mx = u[jj - 1];
			}
			else
			{
				if(u[jj - 1] < 0)
				{
					if(u[jj - 1] <= mx1)
						mx1 = u[jj - 1];
				}
				else
				{
					if(u[jj - 1] >= mx)
						mx = u[jj - 1];
				}
			}

			if(jj == npts)
			{
				if(Math.abs(mx) > Math.abs(mx1))
				{
					mmax = mx;
					gameff1 = 0.65 * 1.57 * mmax / height;
				}
				else if(Math.abs(mx) < Math.abs(mx1))
				{
					mmax = mx1;
					gameff1 = 0.65 * 1.57 * mmax / height;
				}
				else
				{
					if(mx > 0)
					{
						mmax = mx;
						gameff1 = 0.65 * 1.57 * mmax / height;
					}
					else
					{
						mmax = mx1;
						gameff1 = 0.65 * 1.57 * mmax / height;
					}
				}
			}
		}

		gameff1 = Math.abs(gameff1);
	}

	// calculate Kmax
	protected void avg_acc()
	{
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
				else if(mx > 0.0)
					mmax = mx;
				else
					mmax = mx1;
			}
		}
	}

	protected void d_response()
	{
		double khat, omega, a, b;
		double deltp, deltu, deltudot, deltudotdot, u2, udot2, udotdot2;

		omega = Math.PI * vs / (2.0 * height);

		khat = (omega * omega) + 2.0 * damp * omega * gamma / (beta * dt) + 1.0 / (beta * (dt * dt));
		a = 1.0 / (beta * dt) + 2.0 * damp * omega * gamma / beta;
		b = 1.0 / (2.0 * beta) + dt * 2.0 * damp * omega * (gamma / (2.0 * beta) - 1.0);

		if(j == 1)
		{
			deltp = - L / M * (acc2 - acc1);
			deltu = deltp / khat;
			deltudot = gamma / (beta * dt) * deltu;
			u2 = deltu;
			udot2 = deltudot;
			udotdot2 = - (L / M) * acc2 - 2.0 * damp * omega * udot2 - (omega * omega) * u2;
		}
		else
		{
			deltp = - L / M * (acc2 - acc1) + a * udot1 + b * udotdot1;
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

	protected void d_setupstate()
	{
		// set up state from previous time step
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

		// set up acceleration loading

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

	protected void eq()
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
		}
	}
}
