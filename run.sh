#!/bin/bash
export CLASSPATH=`find ./lib -name "*.jar" | tr '\n' ':'`
export MAINCLASS='Splask' # <- à remplacer par le nom de votre programme
java -cp ${CLASSPATH}:classes $MAINCLASS
# Le programme s'exécute depuis la racine de l'archive

#si ça marche pas faut run
#sed -i -e 's/\r$//' run.sh