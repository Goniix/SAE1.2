#!/bin/sh
for entry in *
do
    if [ -f $entry ] && [ "${entry##*.}" = "class" ]
    then
        rm $entry
    fi
    if [ -f $entry ] && [ "${entry##*.}" = "java" ]
    then
        javac -cp ~/ijava/program.jar:. $entry
    fi
done