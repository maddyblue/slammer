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

public class Coupled extends Analysis
{
	public static double Coupled(final DoubleList data, final double G, final double di, final double scale, final double uwgt, final double height, final double vs, final double damp, final double angle, final double caList[][])
	{
		double temp[];
		final double beta = 0.25;
		final double gamma = 0.5;
		double Mtot, M, L, omega;
		int qq;

		double disp[] = new double[caList.length];
		double mu[] = new double[caList.length];

		for(int ii = 0; ii < caList.length; ii++)
		{
			disp[ii] = caList[ii][0];
			mu[ii] = caList[ii][1];
		}

		double rho, time = 0;
		int i, k, j, kk;
		boolean slide;

		final double angleR = Math.toRadians(angle);
		final double angleC = Math.cos(angleR);
		final double angleS = Math.sin(angleR);

		setValueSize(di);

		data.reset();

		Double val;
		double cur = 0, prev;
		double curG = 0, prevG;

		// slide=0 no sliding, slide=1 sliding
		// variables that end in 1 are for previous time step
		// variables that end in 2 are for current time step

		double s1=0, sdot1=0, sdotdot1=0;
		double s2=0, sdot2=0, sdotdot2=0;
		double u1=0, udot1=0, udotdot1=0;
		double u2=0, udot2=0, udotdot2=0, baseacc=0;
		double basef=0, acc1=0, acc2=0, normalf1=0, normalf2=0;

		rho = uwgt / G;

		// calculate constants for Newmark algorithm

		Mtot = rho * height;
		slide = false;

		// qq indicates which mu is in effect
		qq = 0;

		omega = Math.PI * vs / (2.0 * height);
		L = 2.0 * rho * height / Math.PI;
		M = rho * height / 2.0;

		for(j = 0; (val = data.each()) != null; j++)
		{
			prev = cur;
			prevG = curG;
			cur = val.doubleValue() * scale;
			curG = cur * G;

			// setup state frm previous time step
			if(j == 0) // first time only
			{
				u1 = 0;
				udot1 = 0;
				udotdot1 = 0;
				s1 = 0;
				sdot1 = 0;
				sdotdot1 = 0;
				normalf1 = 0;
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

			// setup acceleration loading

			// normal force corrected for vertical component of accel
			normalf2 = Mtot * G * angleC + Mtot * curG * angleS;

			if(j == 0)
			{
				acc1 = 0;
				acc2 = curG * angleC;
			}
			else
			{
				if(!slide)
				{
					acc1 = prevG * angleC;
					acc2 = curG * angleC;
				}
				else
				{
					acc1 = G * angleS - mu[qq] * normalf1 / Mtot;
					acc2 = G * angleS - mu[qq] * normalf2 / Mtot;
				}
			}

			// solve for u, udot, udotdot at next time step

			temp = CoupledSolvu(u1, udot1, udotdot1, u2, udot2, udotdot2, acc1, acc2, slide, j, M, Mtot, L, omega, beta, gamma, di, damp, G);

			u1 = temp[0];
			udot1 = temp[1];
			udotdot1 = temp[2];
			u2 = temp[3];
			udot2 = temp[4];
			udotdot2 = temp[5];
			acc1 = temp[6];
			acc2 = temp[7];

			// calculate base force based on udotdot calculation

			basef = -Mtot * curG * angleC - L * udotdot2 + Mtot * G * angleS;

			// check if sliding has started

			if(!slide)
			{
				if(basef > mu[qq] * normalf2)
					slide = true;
			}


			// based oncalculated response:

			if(slide)
			{
				// update sliding acceleration
				sdotdot2 = -curG * angleC - mu[qq] * normalf2 / Mtot - L * udotdot2 / Mtot + G * angleS;

				// if sliding is occuring, integrate sdotdot using trapezoid rule to get sdot and s
				sdot2 = sdot1 + 0.5 * di * (sdotdot2 + sdotdot1);
				s2 = s1 + 0.5 * di * (sdot2 + sdot1);

				// check if sliding has stopped
				if(sdot2 <= 0.0)
				{
					temp = CoupledSlideStop(s1, sdot1, sdotdot1, sdotdot2, u1, udot1, udotdot1, s2, sdot2, u2, udot2, udotdot2, slide, normalf2, Mtot, M, j, L, omega, mu[qq], beta, gamma, di, curG, prevG, angleS, angleC, damp, G);
					s1 = temp[0];
					sdot1 = temp[1];
					sdotdot1 = temp[2];
					u1 = temp[3];
					udot1 = temp[4];
					udotdot1 = temp[5];
					s2 = temp[6];
					sdot2 = temp[7];
					sdotdot2 = temp[8];
					u2 = temp[9];
					udot2 = temp[10];
					udotdot2 = temp[11];
					normalf2 = temp[12];

					slide = false;
					sdot2 = 0;
					sdotdot2 = 0;
				}
			}

			baseacc = curG * angleC;

			// output sliding quantities

			store(s2);

			if(!slide && Math.abs(s2) >= disp[qq] && qq < (mu.length - 1))
			{
				qq++;
			}

			time += di;
		}

		end(s2);

		return s2;
	}

	public static double[] CoupledSolvu(double u1, double udot1, double udotdot1, double u2, double udot2, double udotdot2, double acc1, double acc2, boolean slide, final int j, final double M, final double Mtot, final double L, final double omega, final double beta, final double gamma, final double di, final double damp, final double G)
	{
		double khat, a, b, dip, diu, diudot;
		double d1;

		if(slide)
			d1 = 1.0 - (L * L) / (M * Mtot);
		else
			d1 = 1;

		khat = (omega * omega) + 2.0 * damp * omega * gamma / (beta * di) + d1 / (beta * (di * di));
		a = d1 / (beta * di) + 2 * damp * omega * gamma / beta;
		b = d1 / (2 * beta) + di * 2 * damp * omega * (gamma / (2 * beta) - 1);

		if(j == 0)
		{
			dip = -L / M * (acc2 - acc1);
			diu = dip / khat;
			diudot = gamma / (beta * di) * diu;
			u2 = diu;
			udot2 = diudot;
			udotdot2 = (-(L / M) * acc2 - 2.0 * damp * omega * udot2 - (omega * omega) * u2) / d1;
		}
		else
		{
			dip = -L / M * (acc2 - acc1) + a * udot1 + b * udotdot1;
			diu = dip / khat;
			diudot = gamma / (beta * di) * diu - gamma / beta * udot1 + di * (1.0 - gamma / (2.0 * beta)) * udotdot1;
			u2 = u1 + diu;
			udot2 = udot1 + diudot;
			udotdot2 = (-(L / M) * acc2 - 2.0 * damp * omega * udot2 - (omega * omega) * u2) / d1;
		}

		double ret[] = new double[8];
		ret[0] = u1;
		ret[1] = udot1;
		ret[2] = udotdot1;
		ret[3] = u2;
		ret[4] = udot2;
		ret[5] = udotdot2;
		ret[6] = acc1;
		ret[7] = acc2;

		return ret;
	}

	public static double[] CoupledSlideStop(double s1, double sdot1, double sdotdot1, double sdotdot2, double u1, double udot1, double udotdot1, double s2, double sdot2, double u2, double udot2, double udotdot2, boolean slide, double normalf2, final double Mtot, final double M, final int j, final double L, final double omega, final double mu, final double beta, final double gamma, final double di, final double curG, final double prevG, final double angleS, final double angleC, final double damp, final double G)
	{
		double ddt, acc1, acc2;
		double acc1b, dd;
		double khat, dip, a, b;

		// end of slide time is taken where sdot=0 from previous analysis
		// assumin sliding throughout the time step

		dd = -sdot1 / (sdot2 - sdot1);
		ddt = dd * di;
		acc1 = G * angleS - mu * (G * angleC + curG * angleS);
		acc1b = prevG + dd * (curG - prevG);
		acc2 = G * angleS - mu * (G * angleC + acc1b * angleS);


		 // if dd=0, sliding has already stopped: skip this solution

		if(dd !=  0)
		{
			slide  =  true;

			double temp[] = CoupledSolvu(u1, udot1, udotdot1, u2, udot2, udotdot2, acc1, acc2, slide, j, M, Mtot, L, omega, beta, gamma, di, damp, G);
			u1 = temp[0];
			udot1 = temp[1];
			udotdot1 = temp[2];
			u2 = temp[3];
			udot2 = temp[4];
			udotdot2 = temp[5];
			acc1 = temp[6];
			acc2 = temp[7];

			u1 = u2;
			udot1 = udot2;
			udotdot1 = udotdot2;
			normalf2 = Mtot * G * angleC + Mtot * acc1b * angleS;
			sdotdot2 =  - acc1b * angleC - mu * normalf2 / Mtot - L * udotdot2 / Mtot + G * angleS;
			sdot2 = sdot1 + 0.5 * ddt * (sdotdot2 + sdotdot1);
			s2 = s1 + 0.5 * ddt * (sdot1 + sdot2);

			 // solve for non sliding response during remaining part of di

			ddt = (1.0 - dd) * di;
			// slide = false; // this does nothing, afaik
			acc1 = acc2;
			acc2 = curG * angleC;

			khat = 1.0 + 2.0 * damp * omega * gamma * ddt + (omega * omega) * beta * (ddt * ddt);
			a = (1.0 - (L * L) / (Mtot * M)) + 2.0 * damp * omega * ddt * (gamma - 1.0) + (omega * omega) * (ddt * ddt) * (beta - 0.5);
			b = (omega * omega) * ddt;
			dip =  - L / M * (acc2 - acc1) + a * (udotdot1) - b * (udot1);
			udotdot2 = dip / khat;

			udot2 = udot1 + (1.0 - gamma) * ddt * (udotdot1) + gamma * ddt * (udotdot2);
			u2 = u1 + udot1 * ddt + (0.5 - beta) * (ddt * ddt) * (udotdot1) + beta * (ddt * ddt) * (udotdot2);
		}

		double ret[] = new double[13];
		ret[0] = s1;
		ret[1] = sdot1;
		ret[2] = sdotdot1;
		ret[3] = u1;
		ret[4] = udot1;
		ret[5] = udotdot1;
		ret[6] = s2;
		ret[7] = sdot2;
		ret[8] = sdotdot2;
		ret[9] = u2;
		ret[10] = udot2;
		ret[11] = udotdot2;
		ret[12] = normalf2;

		return ret;
	}
}
