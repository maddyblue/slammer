/* This file is in the public domain. */

package slammer;

import slammer.*;
import slammer.analysis.*;
import java.util.*;
import junit.framework.*;

public class SlammerTest extends TestCase
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

	public SlammerTest(String name)
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

		suite.addTest(new SlammerTest("RigorousDecoupledLinear"));
		suite.addTest(new SlammerTest("RigorousDecoupledEquivalent"));
		suite.addTest(new SlammerTest("RigorousCoupledLinear"));
		suite.addTest(new SlammerTest("RigorousCoupledEquivalent"));

		suite.addTest(new SlammerTest("RigorousRigidBlockDown"));
		suite.addTest(new SlammerTest("RigorousRigidBlockDual"));

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
		res = RigidBlock.SlammerRigorous("", dat1, dt, ca, scal, false, 0, 1.0);
		Assert.assertEquals(35.40371, res, 1e-4);
	}

	public void RigorousRigidBlockDual()
	{
		res = RigidBlock.SlammerRigorous("", dat1, dt, ca, scal, true, 20, 1.0);
		Assert.assertEquals(35.14587, res, 1e-4);
	}
}
