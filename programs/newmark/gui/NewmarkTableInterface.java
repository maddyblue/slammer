/*
 * NewmarkTableInterface.java - interface containing a bunch of constants for the table
 *
 * Copyright (C) 2002 Matthew Jibson (dolmant@dolmant.net)
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

/* $Id: NewmarkTableInterface.java,v 1.1 2003/06/15 01:58:11 dolmant Exp $ */

package newmark.gui;

public interface NewmarkTableInterface
{
	public static final int NONE    = 0;

	public static final int RECORD  = 1 << 0;
	public static final int STATION = 1 << 1;
	public static final int RSBOTH  = RECORD | STATION;

	public static final int IMTBL   = 1 << 2;
	public static final int IMCMB   = 1 << 3;
	public static final int IMBOTH  = IMTBL | IMCMB;

	public static final int SWAP    = 1 << 4;
	public static final int REFRESH = 1 << 5;

	public static final Integer NONEO    = new Integer(NONE);

	public static final Integer RECORDO  = new Integer(RECORD);
	public static final Integer STATIONO = new Integer(STATION);
	public static final Integer RSBOTHO    = new Integer(RSBOTH);

	public static final Integer IMTBLO   = new Integer(IMTBL);
	public static final Integer IMCMBO   = new Integer(IMCMB);
	public static final Integer IMBOTHO    = new Integer(IMBOTH);

	public static final int SELECT  = 1;
	public static final int MANAGER = 2;

	public static final Boolean TRUE  = Boolean.TRUE;
	public static final Boolean FALSE = Boolean.FALSE;

	public static final Object[][] fieldArray = {
	//                                                            sort          select    manager
	//Field name               units  abbrev         DB name      field  search display   display   import
	{ "Import",                "",    "Import",      "",          FALSE, FALSE, NONEO,    NONEO,    IMTBLO  },
	{ "File",                  "",    "File",        "",          FALSE, FALSE, NONEO,    NONEO,    IMTBLO  },
	{ "Earthquake",            "",    "Earthquake",  "eq",        TRUE,  FALSE, RSBOTHO,  RSBOTHO,  IMBOTHO },
	{ "Record",                "",    "Record",      "record",    TRUE,  FALSE, RSBOTHO,  RSBOTHO,  IMTBLO },
	{ "Digitization Interval", "s",   "Dig. Int.",   "digi_int",  FALSE, FALSE, NONEO,    RECORDO,  IMBOTHO },
	{ "Moment Magnitude",      "",    "Magnitude",   "mom_mag",   TRUE,  TRUE,  RECORDO,  RECORDO,  IMBOTHO },
	{ "Arias Intensity",       "m/s", "Arias Int.",  "arias",     TRUE,  TRUE,  RECORDO,  RECORDO,  NONEO   },
	{ "Duration (5-95%)",      "s",   "Duration",    "dobry",     TRUE,  TRUE,  RECORDO,  RECORDO,  NONEO   },
	{ "Peak Acceleration",     "g",   "PGA",         "pga",       TRUE,  TRUE,  RECORDO,  RECORDO,  NONEO   },
	{ "Mean Period",           "s",   "Mean Per.",   "mean_per",  TRUE,  TRUE,  RECORDO,  RECORDO,  NONEO   },
	{ "Epicentral Distance",   "km",  "Epi. Dist.",  "epi_dist",  TRUE,  TRUE,  RECORDO,  RECORDO,  IMBOTHO },
	{ "Focal Distance",        "km",  "Focal Dist.", "foc_dist",  TRUE,  TRUE,  RECORDO,  RECORDO,  IMBOTHO },
	{ "Rupture Distance",      "km",  "Rup. Dist",   "rup_dist",  TRUE,  TRUE,  RECORDO,  RECORDO,  IMBOTHO },
	{ "Focal Mechanism",       "",    "Foc. Mech.",  "foc_mech",  TRUE,  FALSE, RECORDO,  RECORDO,  IMBOTHO },
	{ "Location",              "",    "Location",    "location",  FALSE, FALSE, STATIONO, STATIONO, IMBOTHO },
	{ "Owner",                 "",    "Owner",       "owner",     TRUE,  FALSE, STATIONO, STATIONO, IMBOTHO },
	{ "Latitude",              "",    "Lat.",        "latitude",  FALSE, FALSE, STATIONO, STATIONO, IMBOTHO },
	{ "Longitude",             "",    "Long.",       "longitude", FALSE, FALSE, STATIONO, STATIONO, IMBOTHO },
	{ "Site Classification",   "",    "Site Class.", "class",     TRUE,  FALSE, STATIONO, STATIONO, IMBOTHO },
	{ "Analyze",               "",    "Analyze",     "analyze",   FALSE, FALSE, RSBOTHO,  NONEO,    NONEO   }
	};

	public static final int colFieldName      = 0;
	public static final int colUnits          = 1;
	public static final int colAbbrev         = 2;
	public static final int colDBName         = 3;
	public static final int colSortField      = 4;
	public static final int colSearchable     = 5;
	public static final int colSelectDisplay  = 6;
	public static final int colManagerDisplay = 7;
	public static final int colImport         = 8;

	public static final int rowImport     = 0;
	public static final int rowFile       = 1;
	public static final int rowEarthquake = 2;
	public static final int rowRecord     = 3;
	public static final int rowDigInt     = 4;
	public static final int rowMagnitude  = 5;
	public static final int rowAriasInt   = 6;
	public static final int rowDuration   = 7;
	public static final int rowPGA        = 8;
	public static final int rowMeanPer    = 9;
	public static final int rowEpiDist    = 10;
	public static final int rowFocalDist  = 11;
	public static final int rowRupDist    = 12;
	public static final int rowFocMech    = 13;
	public static final int rowLocation   = 14;
	public static final int rowOwner      = 15;
	public static final int rowLat        = 16;
	public static final int rowLng        = 17;
	public static final int rowSiteClass  = 18;
	public static final int rowAnalyze    = 19;

	// Focal Mech codes
	public static final int FMStrikeSlip     = 1;
	public static final int FMNormal         = 2;
	public static final int FMReverse        = 3;
	public static final int FMObliqueNormal  = 4;
	public static final int FMObliqueReverse = 5;

	public static final String FMObliqueNormalLong  = "Oblique normal";
	public static final String FMObliqueReverseLong = "Oblique reverse";

	public static final String[] FocMechArray = {
		"",
		"Strike-slip",
		"Normal",
		"Reverse",
		"Obl. normal",
		"Obl. reverse"
	};

	// Site Class codes
	public static final int SCHardRock  = 1;
	public static final int SCSoftRock  = 2;
	public static final int SCStiffSoil = 3;
	public static final int SCSoftSoil  = 4;

	public static final String[] SiteClassArray = {
		"",
		"Hard rock",
		"Soft rock",
		"Stiff soil",
		"Soft soil",
	};
}
