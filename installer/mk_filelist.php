#!/usr/local/bin/php -q

<?php

function addfile($name, $file)
{
	passthru("cd .. && echo $name >> $file");
}

function adddir($dir, $file)
{
	$dir = str_replace(" ", "\\ ", $dir);
	passthru("cd .. && find $dir -type f | grep -v .svn | sort >> $file");
}

function clear($file)
{
	passthru("cd .. && echo -n > $file");
}

function contents($filename)
{
	$fd = fopen ($filename, "r");
	$contents = str_replace(" ", "\\ ", fread ($fd, filesize ($filename)));
	$contents = str_replace("\n", "  ", $contents);
	fclose ($fd);

	return $contents;
}

function getsize($file)
{
	echo "$file: ";

	$contents = str_replace(" ", "\\ ", contents("../$file"));
	passthru("cd .. && echo -n $contents | xargs ls -l | awk 'BEGIN { size=0 } { disk_size+=(int(\$5/4096+1)*4); size+=\$5/1024 } END { print disk_size \" \" size }'");
}

function addeqs($eqs, $file)
{
	$pos = strrpos($file, "-");
	$short = substr($file, $pos + 1);
	clear("installer/$file");
	clear("records/EQdata-$short.txt");
	clear("records/eq-$short.sql");
	foreach($eqs as $eq)
	{
		adddir("records/$eq", "installer/$file");
		passthru("cd .. && grep \"$eq\" records/EQdata.txt >> records/EQdata-$short.txt");
		passthru("cd .. && grep \"$eq\" records/eq.sql >> records/eq-$short.sql");
	}
	addfile("records/EQdata-$short.txt", "installer/$file");
	addfile("records/eq-$short.sql", "installer/$file");
	getsize("installer/$file");
}

// program
$file = "installer/newmark-program";
clear($file);
addfile("programs/newmark.jar", $file);
adddir("programs/jars", $file);
getsize($file);

// database fileset
passthru("cd .. && find org -type f | grep -v \".svn\" | sort | sed 's/" . '\\' . '$/' . '\\' . '\\' . '$/g' . "' > installer/database-fileset");

// newmark-eq-california
$eqs = array(
	"Cape Mendocino 1992",
	"Coalinga 1983",
	"Coyote Lake 1979",
	"Daly City 1957",
	"El Centro 1940",
	"Imperial Valley 1979",
	"Kern County 1952",
	"Landers 1992",
	"Loma Prieta 1989",
	"Mammoth Lakes-1 1980",
	"Mammoth Lakes-2 1980",
	"Morgan Hill 1984",
	"N. Palm Springs 1986",
	"Northridge 1994",
	"Parkfield 1966",
	"San Fernando 1971",
	"Santa Barbara 1978",
	"Superstition Hills 1987",
	"Westmorland 1981",
	"Whittier Narrows 1987"
);
$file = "newmark-eq-california";
addeqs($eqs, $file);

// newmark-eq-otherus
$eqs = array(
	"Hilo 1975",
	"Nisqually 2001"
);
$file = "newmark-eq-otherus";
addeqs($eqs, $file);

// newmark-eq-international
$eqs = array(
	"Duzce, Turkey 1999",
	"Friuli, Italy 1976",
	"Kobe, Japan 1995",
	"Kocaeli, Turkey 1999",
	"Nahanni, Canada 1985",
	"Tabas, Iran 1978"
);
$file = "newmark-eq-international";
addeqs($eqs, $file);

// newmark-eq-chichi
$eqs = array(
	"Chi-Chi, Taiwan 1999"
);
$file = "newmark-eq-chichi";
addeqs($eqs, $file);

?>
