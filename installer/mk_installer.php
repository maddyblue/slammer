#!/usr/local/bin/php -q

<?php

$jarfile = "/usr/dolmant/newmarkinstall.jar";

passthru("cd .. && jar cfm0 $jarfile installer/install.mf \\"
	. "installer/install.props \\"
	. "installer/*.html \\"
	. "installer/*.class \\"
	. "installer/newmark-*");

$list = array("newmark-program", "mckoi-fileset");

foreach($list as $file)
{
	$contents = contents($file);

	passthru("cd .. && jar uf0 $jarfile $contents");
}

$list = array("california", "otherus", "international", "chichi");

foreach($list as $file)
{
	$contents = contents("newmark-eq-$file");

	passthru("cd .. && zip -q0 $jarfile $contents");
}

function contents($filename)
{
	$fd = fopen ($filename, "r");
	$contents = str_replace(" ", "\\ ", fread ($fd, filesize ($filename)));
	$contents = str_replace("\n", "  ", $contents);
	fclose ($fd);

	return $contents;
}

?>
