#!/usr/local/bin/php -q

<?php

$jarfile = "/media/newmark/newmarkinstall.jar";

passthru("cd .. && jar cfm $jarfile installer/install.mf \\"
	. "installer/install.props \\"
	. "installer/*.html \\"
	. "installer/*.class \\"
	. "installer/newmark-*");

$list = array("newmark-program", "mckoi-fileset");

foreach($list as $file)
{
	$contents = contents($file);

	passthru("cd .. && jar uf $jarfile $contents");
}

$list = array("california", "otherus", "international", "chichi");

foreach($list as $file)
{
	$contents = contents("newmark-eq-$file");

	passthru("cd .. && zip -q $jarfile $contents");
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
