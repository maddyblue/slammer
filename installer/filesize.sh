#!/bin/bash

function print_size {
	echo -n $1 "(" `cat $1 | wc -l` "): "
	cat $1 | sed 's/ /\\ /g' | xargs ls -l | awk 'BEGIN { size=0 } { disk_size+=(int($5/4096+1)*4)/1024; size+=$5/1024 } END { print disk_size " " size }'
}

function mk_contents {
	file=$1.tar
	tar tf $file > $1 2> /dev/null
	print_size $1
}

function process_eq {
	name=installer/eq-$1
	out=records/eq-$1.sql
	rm -f $out
	mk_contents $name
	for i in `cat $name | awk -F '/' '{ print $2 }' | uniq | sed 's/ /_/g'`
	do
		j=`echo $i | sed 's/_/ /g'`
		grep "$j" records/eq.sql >> $out
	done
}

mk_contents installer/program-slammer
mk_contents installer/program-srm

for file in california chichi international otherus
do
	process_eq $file
done

for file in installer/*.tar
do
	if [ $file -nt $file.bz2 ]; then
		echo bzip2ing $file
		bzip2 -kf $file
	fi
done
