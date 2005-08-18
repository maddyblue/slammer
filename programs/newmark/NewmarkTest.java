/*
 * Copyright (C) 2005 Matthew Jibson (dolmant@dolmant.net)
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

package newmark;

import newmark.*;
import newmark.analysis.*;
import java.util.*;
import junit.framework.*;

public class NewmarkTest extends TestCase
{
	private DoubleList dat;
	private static final double uwgt = 18.85;
	private static final double height = 30.48;
	private static final double vs = 175.53;
	private static final double damp = 0.0898;
	private static final double dt = 0.02;
	private static final double scal = 1.0;
	private static final double g = 9.8;
	private static final double vr = 762.0;
	private static final double[][] ca = { { 0, 0.1 } };
	private double res;

	public NewmarkTest(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		dat = new DoubleList("../sd2/pac8-new.txt");
	}

	public static Test suite()
	{
		Analysis.testing = true; // this lets the swing test suite work
		TestSuite suite = new TestSuite();
		suite.addTest(new NewmarkTest("RigorousDecoupledRigidLinear"));
		suite.addTest(new NewmarkTest("RigorousDecoupledElasticLinear"));
		suite.addTest(new NewmarkTest("RigorousDecoupledRigidEquivalent"));
		suite.addTest(new NewmarkTest("RigorousDecoupledElasticEquivalent"));
		suite.addTest(new NewmarkTest("RigorousCoupledRigidLinear"));
		suite.addTest(new NewmarkTest("RigorousCoupledElasticLinear"));
		suite.addTest(new NewmarkTest("RigorousCoupledRigidEquivalent"));
		suite.addTest(new NewmarkTest("RigorousCoupledElasticEquivalent"));
		return suite;
	}

	public void RigorousDecoupledRigidLinear()
	{
		res = Decoupled.Decoupled(dat.getAsArray(), uwgt, height, vs, damp, dt, scal, g, vr, ca, false, false);
		Assert.assertEquals(0.14571, res, 1e-5);
	}

	public void RigorousDecoupledElasticLinear()
	{
		res = Decoupled.Decoupled(dat.getAsArray(), uwgt, height, vs, damp, dt, scal, g, vr, ca, true, false);
		Assert.assertEquals(0.08827, res, 1e-5);
	}

	public void RigorousDecoupledRigidEquivalent()
	{
		res = Decoupled.Decoupled(dat.getAsArray(), uwgt, height, vs, damp, dt, scal, g, vr, ca, false, true);
		Assert.assertEquals(0.03486, res, 1e-5);
	}

	public void RigorousDecoupledElasticEquivalent()
	{
		res = Decoupled.Decoupled(dat.getAsArray(), uwgt, height, vs, damp, dt, scal, g, vr, ca, true, true);
		Assert.assertEquals(0.00489, res, 1e-5);
	}

	public void RigorousCoupledRigidLinear()
	{
		res = Coupled.Coupled(dat.getAsArray(), uwgt, height, vs, damp, dt, scal, g, vr, ca, false, false);
		Assert.assertEquals(0.12784, res, 1e-5);
	}

	public void RigorousCoupledElasticLinear()
	{
		res = Coupled.Coupled(dat.getAsArray(), uwgt, height, vs, damp, dt, scal, g, vr, ca, true, false);
		Assert.assertEquals(0.07615, res, 1e-5);
	}

	public void RigorousCoupledRigidEquivalent()
	{
		res = Coupled.Coupled(dat.getAsArray(), uwgt, height, vs, damp, dt, scal, g, vr, ca, false, true);
		Assert.assertEquals(0.02225, res, 1e-5);
	}

	public void RigorousCoupledElasticEquivalent()
	{
		res = Coupled.Coupled(dat.getAsArray(), uwgt, height, vs, damp, dt, scal, g, vr, ca, true, true);
		Assert.assertEquals(0.00859, res, 1e-5);
	}
}
