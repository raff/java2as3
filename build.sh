#!/bin/sh
SYSTEM=`uname`
if [ `expr ${SYSTEM} : "^CYG"` -ne 0 ]; then
  ASC="`cygpath -d -m 'c:/Program Files/Adobe/Flex Builder 3/sdks/3.0.0/bin/compc.exe'`"
else
  ASC="/Applications/Adobe Flex Builder 3/Flex SDK 3/bin/compc"
fi

#cp -r jas as3

l=`find as3 -name "*.as" | sed -e 's/^as3.//' -e 's/.as$//' -e 's/\//./g'`
"${ASC}" -compiler.incremental -compiler.source-path=as3 -include-sources=jas -output=as3.swc -include-classes $l
#"${ASC}" $*
