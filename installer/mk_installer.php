#!/usr/local/bin/php -q

<?php

$jarfile = "../newmarkinstall.jar";

echo "Compiling installer java files...";
passthru("javac *.java");
echo "done.\n";

echo "Creating initial jar file...";
passthru("cd .. && jar cfm $jarfile installer/install.mf \\"
	. "installer/install.props \\"
	. "installer/*.html \\"
	. "installer/*.class \\"
	. "installer/newmark-*");
echo "done.\n";

$list = array("newmark-program", "mckoi-fileset");

foreach($list as $file)
{
	$contents = contents($file);

	echo "Adding $file to $jarfile...";
	passthru("cd .. && jar uf $jarfile $contents");
	echo "done.\n";
}

$list = array("california", "otherus", "international", "chichi");

foreach($list as $file)
{
	$contents = contents("newmark-eq-$file");

	echo "Adding $file to $jarfile...";
	passthru("cd .. && zip -q $jarfile $contents");
	echo "done.\n";
}

echo "Done.\n";

function contents($filename)
{
	$fd = fopen ($filename, "r");
	$contents = str_replace(" ", "\\ ", fread ($fd, filesize ($filename)));
	$contents = str_replace("\n", "  ", $contents);
	fclose ($fd);

	return $contents;
}

?>
