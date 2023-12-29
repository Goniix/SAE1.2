#!/bin/sh
for entry in src/*
do
    if [ -f $entry ] && [ "${entry##*.}" = "class" ]
    then
        rm src/$entry
    fi
    if [ -f $entry ] && [ "${entry##*.}" = "java" ]
    then
        javac -cp ~/ijava/program.jar:. src/$entry
    fi
done