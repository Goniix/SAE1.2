#!/bin/bash
export SOURCES=src
export CLASSES=classes
export CLASSPATH=`find lib -name "*.jar" | tr '\n' ':'`

javac -cp ${CLASSPATH} -sourcepath ${SOURCES} -d ${CLASSES} $@ `find src -name "*.java"`
#si ça marche pas faut run
#sed -i -e 's/\r$//' compile.sh