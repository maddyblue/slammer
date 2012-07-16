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

	public static final int REFRESH = 1 << 4;

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
		//                                                                       display      search       sort          select    manager
		// Field name                           units   abbrev                   DB name      DB name      field  search display   display   import   display name
		{ "Import",                             "",     "Import",                "",          "",          FALSE, FALSE, NONEO,    NONEO,    IMTBLO,  "Import"                },
		{ "File",                               "",     "File",                  "",          "",          FALSE, FALSE, NONEO,    NONEO,    IMTBLO,  "File"                  },
		{ "Earthquake",                         "",     "Earthquake",            "EQ",        "EQ",        TRUE,  FALSE, RSBOTHO,  RSBOTHO,  IMBOTHO, "Earthquake"            },
		{ "Record",                             "",     "Record",                "RECORD",    "RECORD",    TRUE,  FALSE, RSBOTHO,  RSBOTHO,  IMTBLO,  "Record"                },
		{ "Digitization interval",              "s",    "Digitization interval", "DIGI_INT",  "DIGI_INT",  FALSE, FALSE, NONEO,    RECORDO,  IMBOTHO, "Digitization interval" },
		{ "Moment magnitude",                   "",     "Magnitude",             "MOM_MAG",   "MAG_SRCH",  TRUE,  TRUE,  RECORDO,  RECORDO,  IMBOTHO, "Moment magnitude"      },
		{ "Arias intensity",                    "m/s",  "Arias intensity",       "ARIAS",     "ARIAS",     TRUE,  TRUE,  RECORDO,  RECORDO,  NONEO,   "Arias intensity"       },
		{ "Duration<sub>5-95%</sub>",           "s",    "<html>Duration<sub>5-95%</sub>",              "DOBRY",     "DOBRY",     TRUE,  TRUE,  RECORDO,  RECORDO,  NONEO,   "Duration 5-95%"        },
		{ "Peak acceleration",                  "g",    "PGA",                   "PGA",       "PGA",       TRUE,  TRUE,  RECORDO,  RECORDO,  NONEO,   "Peak acceleration"     },
		{ "Peak velocity",                      "cm/s", "PGV",                   "PGV",       "PGV",       TRUE,  TRUE,  RECORDO,  RECORDO,  NONEO,   "Peak velocity"         },
		{ "Mean period",                        "s",    "Mean period",           "MEAN_PER",  "MEAN_PER",  TRUE,  TRUE,  RECORDO,  RECORDO,  NONEO,   "Mean period"           },
		{ "Epicentral distance",                "km",   "Epicentral distance",   "EPI_DIST",  "EPI_SRCH",  TRUE,  TRUE,  RECORDO,  RECORDO,  IMBOTHO, "Epicentral distance"   },
		{ "Focal distance",                     "km",   "Focal distance",        "FOC_DIST",  "FOC_SRCH",  TRUE,  TRUE,  RECORDO,  RECORDO,  IMBOTHO, "Focal distance"        },
		{ "Rupture distance",                   "km",   "Rupture distance",      "RUP_DIST",  "RUP_SRCH",  TRUE,  TRUE,  RECORDO,  RECORDO,  IMBOTHO, "Rupture distance"      },
		{ "Focal mechanism",                    "",     "Focal mechanism",       "FOC_MECH",  "FOC_MECH",  TRUE,  FALSE, RECORDO,  RECORDO,  IMBOTHO, "Focal mechanism"       },
		{ "Location",                           "",     "Location",              "LOCATION",  "LOCATION",  TRUE,  FALSE, STATIONO, STATIONO, IMBOTHO, "Location"              },
		{ "Owner",                              "",     "Owner",                 "OWNER",     "OWNER",     TRUE,  FALSE, STATIONO, STATIONO, IMBOTHO, "Owner"                 },
		{ "V<sub>s</sub><sup>30</sup>",         "m/s",  "<html>V<sub>s</sub><sup>30</sup>",                  "VS30",      "VS30_SRCH", TRUE,  TRUE,  STATIONO, STATIONO, IMBOTHO, "Vs30"                  },
		{ "Site classification (Geomatrix C3)", "",     "Site classification",   "CLASS",     "CLASS",     TRUE,  FALSE, STATIONO, STATIONO, IMBOTHO, "Site classification"   },
		{ "Latitude",                           "",     "Latitude",              "LATITUDE",  "LAT_SRCH",  TRUE,  FALSE, STATIONO, STATIONO, IMBOTHO, "Latitude"              },
		{ "Longitude",                          "",     "Longitude",             "LONGITUDE", "LNG_SRCH",  TRUE,  FALSE, STATIONO, STATIONO, IMBOTHO, "Longitude"             },
		{ "Analyze",                            "",     "Analyze",               "ANALYZE",   "ANALYZE",   FALSE, FALSE, RSBOTHO,  NONEO,    NONEO,   "Analyze"               }
	};

	public static final int colFieldName      = 0;
	public static final int colUnits          = 1;
	public static final int colAbbrev         = 2;
	public static final int colDBName         = 3;
	public static final int colDBSearch       = 4;
	public static final int colSortField      = 5;
	public static final int colSearchable     = 6;
	public static final int colSelectDisplay  = 7;
	public static final int colManagerDisplay = 8;
	public static final int colImport         = 9;
	public static final int colDispName       = 10;

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
	public static final int rowFocMech    = 14;
	public static final int rowLocation   = 15;
	public static final int rowOwner      = 16;
	public static final int rowVs30       = 17;
	public static final int rowSiteClass  = 18;
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
