/* This file is in the public domain. */

package slammer.gui;

public interface SlammerTableInterface
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
	public static final Integer RSBOTHO  = new Integer(RSBOTH);

	public static final Integer IMTBLO   = new Integer(IMTBL);
	public static final Integer IMCMBO   = new Integer(IMCMB);
	public static final Integer IMBOTHO  = new Integer(IMBOTH);

	public static final int SELECT  = 1;
	public static final int MANAGER = 2;

	public static final Boolean TRUE  = Boolean.TRUE;
	public static final Boolean FALSE = Boolean.FALSE;

	public static final Object[][] fieldArray = {
		//                                                                          sort          select    manager
		// Field name                           units   abbrev         DB name      field  search display   display   import
		{ "Import",                             "",     "Import",      "",          FALSE, FALSE, NONEO,    NONEO,    IMTBLO,  "Import"                },
		{ "File",                               "",     "File",        "",          FALSE, FALSE, NONEO,    NONEO,    IMTBLO,  "File"                  },
		{ "Earthquake",                         "",     "Earthquake",  "EQ",        TRUE,  FALSE, RSBOTHO,  RSBOTHO,  IMBOTHO, "Earthquake"            },
		{ "Record",                             "",     "Record",      "RECORD",    TRUE,  FALSE, RSBOTHO,  RSBOTHO,  IMTBLO,  "Record"                },
		{ "Digitization Interval",              "s",    "Dig. Int.",   "DIGI_INT",  FALSE, FALSE, NONEO,    RECORDO,  IMBOTHO, "Digitization Interval" },
		{ "Moment Magnitude",                   "",     "Magnitude",   "MOM_MAG",   TRUE,  TRUE,  RECORDO,  RECORDO,  IMBOTHO, "Moment Magnitude"      },
		{ "Arias Intensity",                    "m/s",  "Arias Int.",  "ARIAS",     TRUE,  TRUE,  RECORDO,  RECORDO,  NONEO,   "Arias Intensity"       },
		{ "Duration<sub>5-95%</sub>",           "s",    "Duration",    "DOBRY",     TRUE,  TRUE,  RECORDO,  RECORDO,  NONEO,   "Duration 5-95%"        },
		{ "Peak Acceleration",                  "g",    "PGA",         "PGA",       TRUE,  TRUE,  RECORDO,  RECORDO,  NONEO,   "Peak Acceleration"     },
		{ "Peak Velocity",                      "cm/s", "PGV",         "PGV",       TRUE,  TRUE,  RECORDO,  RECORDO,  NONEO,   "Peak Velocity"         },
		{ "Mean Period",                        "s",    "Mean Per.",   "MEAN_PER",  TRUE,  TRUE,  RECORDO,  RECORDO,  NONEO,   "Mean Period"           },
		{ "Epicentral Distance",                "km",   "Epi. Dist.",  "EPI_DIST",  TRUE,  TRUE,  RECORDO,  RECORDO,  IMBOTHO, "Epicentral Distance"   },
		{ "Focal Distance",                     "km",   "Focal Dist.", "FOC_DIST",  TRUE,  TRUE,  RECORDO,  RECORDO,  IMBOTHO, "Focal Distance"        },
		{ "Rupture Distance",                   "km",   "Rup. Dist",   "RUP_DIST",  TRUE,  TRUE,  RECORDO,  RECORDO,  IMBOTHO, "Rupture Distance"      },
		{ "V<sub>S</sub><sup>30</sup>",         "m/s",  "Vs30",        "VS30",      TRUE,  TRUE,  STATIONO, STATIONO, IMBOTHO, "Vs30"                  },
		{ "Site Classification (Geomatrix C3)", "",     "Site Class.", "CLASS",     TRUE,  FALSE, STATIONO, STATIONO, IMBOTHO, "Site Classification"   },
		{ "Focal Mechanism",                    "",     "Foc. Mech.",  "FOC_MECH",  TRUE,  FALSE, RECORDO,  RECORDO,  IMBOTHO, "Focal Mechanism"       },
		{ "Location",                           "",     "Location",    "LOCATION",  TRUE,  FALSE, STATIONO, STATIONO, IMBOTHO, "Location"              },
		{ "Owner",                              "",     "Owner",       "OWNER",     TRUE,  FALSE, STATIONO, STATIONO, IMBOTHO, "Owner"                 },
		{ "Latitude",                           "",     "Lat.",        "LATITUDE",  TRUE,  FALSE, STATIONO, STATIONO, IMBOTHO, "Latitude"              },
		{ "Longitude",                          "",     "Long.",       "LONGITUDE", TRUE,  FALSE, STATIONO, STATIONO, IMBOTHO, "Longitude"             },
		{ "Analyze",                            "",     "Analyze",     "ANALYZE",   FALSE, FALSE, RSBOTHO,  NONEO,    NONEO,   "Analyze"               }
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
	public static final int colDispName       = 9;

	public static final int rowImport     = 0;
	public static final int rowFile       = 1;
	public static final int rowEarthquake = 2;
	public static final int rowRecord     = 3;
	public static final int rowDigInt     = 4;
	public static final int rowMagnitude  = 5;
	public static final int rowAriasInt   = 6;
	public static final int rowDuration   = 7;
	public static final int rowPGA        = 8;
	public static final int rowPGV        = 9;
	public static final int rowMeanPer    = 10;
	public static final int rowEpiDist    = 11;
	public static final int rowFocalDist  = 12;
	public static final int rowRupDist    = 13;
	public static final int rowVS30       = 14;
	public static final int rowSiteClass  = 15;
	public static final int rowFocMech    = 16;
	public static final int rowLocation   = 17;
	public static final int rowOwner      = 18;
	public static final int rowLat        = 19;
	public static final int rowLng        = 20;
	public static final int rowAnalyze    = 21;

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

	public static final String[] FocMechShort = {
		"",
		"SS",
		"N",
		"R",
		"NO",
		"RO"
	};

	// Site Class codes
	public static final int SCA = 1;
	public static final int SCB = 2;
	public static final int SCC = 3;
	public static final int SCD = 4;
	public static final int SCE = 5;

	public static final String[] SiteClassArray = {
		"",
		"A",
		"B",
		"C",
		"D",
		"E"
	};
}
