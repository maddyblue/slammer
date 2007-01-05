/*
 * Copyright (c) 2005 Matt Jibson
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *    - Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

/* $Id$ */

package newmark;

import newmark.*;
import newmark.analysis.*;
import java.util.*;
import junit.framework.*;

public class NewmarkTest extends TestCase
{
	private DoubleList dat, dat1;
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
		dat1 = new DoubleList("../records/Cape Mendocino 1992/PET-000");
	}

	public static Test suite()
	{
		Analysis.testing = true; // this lets the swing test suite work
		TestSuite suite = new TestSuite();

		suite.addTest(new NewmarkTest("RigorousDecoupledLinear"));
		suite.addTest(new NewmarkTest("RigorousDecoupledEquivalent"));
		suite.addTest(new NewmarkTest("RigorousCoupledLinear"));
		suite.addTest(new NewmarkTest("RigorousCoupledEquivalent"));

		suite.addTest(new NewmarkTest("RigorousRigidBlockDown"));
		suite.addTest(new NewmarkTest("RigorousRigidBlockDual"));

		return suite;
	}

	public void RigorousDecoupledLinear()
	{
		res = Decoupled.Decoupled(dat.getAsArray(), uwgt, height, vs, damp, dt, scal, g, vr, ca, false);
		Assert.assertEquals(0.14571, res, 1e-4);
	}

	public void RigorousDecoupledEquivalent()
	{
		res = Decoupled.Decoupled(dat.getAsArray(), uwgt, height, vs, damp, dt, scal, g, vr, ca, true);
		Assert.assertEquals(0.03486, res, 1e-4);
	}

	public void RigorousCoupledLinear()
	{
		res = Coupled.Coupled(dat.getAsArray(), uwgt, height, vs, damp, dt, scal, g, vr, ca, false);
		Assert.assertEquals(0.12784, res, 1e-4);
	}

	public void RigorousCoupledEquivalent()
	{
		res = Coupled.Coupled(dat.getAsArray(), uwgt, height, vs, damp, dt, scal, g, vr, ca, true);
		Assert.assertEquals(0.02225, res, 1e-4);
	}

	public void RigorousRigidBlockDown()
	{
		res = RigidBlock.NewmarkRigorous("", dat1, dt, ca, scal, false, 0, 1.0);
		Assert.assertEquals(35.40371, res, 1e-4);
	}

	public void RigorousRigidBlockDual()
	{
		res = RigidBlock.NewmarkRigorous("", dat1, dt, ca, scal, true, 20, 1.0);
		Assert.assertEquals(35.14587, res, 1e-4);
	}
}
