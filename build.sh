#!/bin/sh
SYSTEM=`uname`
if [ `expr ${SYSTEM} : "^CYG"` -ne 0 ]; then
  ASC="c:/Progra~1/Adobe/Flex Builder 2/Flex SDK 2/bin/compc"
else
  ASC="/Applications/Adobe Flex Builder 2/Flex SDK 2/bin/compc"
fi

l=`find as3 -name "*.as" | sed -e 's/^as3.//' -e 's/.as$//' -e 's/\//./g'`
"${ASC}" -compiler.incremental -compiler.source-path=as3 -output=as3.swc -include-classes $l
#"${ASC}" $*
