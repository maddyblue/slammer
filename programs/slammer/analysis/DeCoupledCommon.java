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


public class DeCoupledCommon extends Analysis
{
	// main function parameters
	protected static double uwgt, height, vs, damp, damp1, dt, scal, g, vr, vs1;
	protected static boolean dv2, dv3;

	// main function variables
	protected static double Mtot, M, L, omega, beta, gamma, angle;
	protected static int qq, nmu, npts;

	protected static double rho, delt, dampf;
	protected static int j;
	protected static boolean slide;

	//slide=0 no sliding, slide=1 sliding
	//variable that end in 1 are for previous time step
	//variable that end in 2 are for current time step

	protected static double mx, mx1, mmax, gameff1, gamref, n, o;
	protected static double s[], u[], disp[], mu[], udotdot[];

	protected static double ain[];

	protected static void eq_property()
	{
		double gameff2, vs2, com1, com2, damp2, G1, G2, l, m;

		gameff2 = Math.abs(gameff1) * 100.0;
		vs2 = vs1 / Math.sqrt(1.0 + (gameff2 / gamref));
		com1 = 1.0 / (1.0 + gameff2 / gamref);
		com2 = Math.pow(com1, 0.1);

		if(!dv2)
			dampf = 0.0;
		else
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

	protected static void residual_mu()
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

	protected static void effstr()
	{
		//effective shear strain calculation

		double mx1 = 0.0, mx = 0.0, mmax;
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
					gameff1 = 0.65 * mmax / height;
				}
				else if(Math.abs(mx) < Math.abs(mx1))
				{
					mmax = mx1;
					gameff1 = 0.65 * mmax / height;
				}
				else
				{
					if(mx > 0)
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
}
