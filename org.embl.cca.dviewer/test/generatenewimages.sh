#!/bin/sh
if [[ "$1" =~ ^[0-9]+([.][0-9]+)?$ ]] ; then
  timeI=$1
  file=$2
else
  file=$1
  timeI=$2
fi
if [ "$file" == "" ]; then
  file=`ls *.cbf *.img *.mccd 2> /dev/null | head -1`
fi
if [ "$file" == "" ] ; then
  echo "No dataset given."
  exit 2
fi
echo "Dataset is based on "$file.

if [ "$timeI" == "" ]; then
  timeI=2
elif [ "$timeI" == "0" ]; then
  echo Removing previously generated files.
  files=`cat imagestoremove.txt`
  for f in $files; do
    rm -f $f
  done
  rm -f imagestoremove.txt
  exit 2
fi
echo Sleeping time will be ${timeI}s.


pref=`echo $file | awk -F "_" '{ txt=$1;for(i=2;i<NF;i++) txt=sprintf("%s_%s",txt,$i); print txt}'`
#echo $pref
ext=`echo $file | awk -F "." '{print $NF}'`
#echo $ext
numdig=`echo $file | awk -F "." '{print $(NF-1)}' | awk -F "_" '{ print length($NF) }'`
#echo $numdig
origfiles=`ls $pref*.$ext | awk -F "." '{print $(NF-1)}' | awk -F "_" '{ print $NF }' | sort -n `
last=`ls $pref*.$ext | awk -F "." '{print $(NF-1)}' | awk -F "_" '{ print $NF }' | sort -n | tail -1 | awk '{ print 1*$1 }'`
echo Last image number is $(( $last ))
if [ "$last" == "" ]; then
  echo Could not find last image.
  exit 3
fi
nextI=$(( ($last/1000 +1)*1000 ))
echo Next Image number is $nextI.

while [ "1"=="1" ]; do
  for ofile in $origfiles; do
    new=${pref}_`echo $nextI | awk '{ print sprintf("%0'${numdig}'d",$1)}'`.$ext
    echo $new
    echo $new >> imagestoremove.txt
    ln -s ${pref}_$ofile.$ext $new
    t=$(( nextI++ ))
    sleep $timeI
  done
done
