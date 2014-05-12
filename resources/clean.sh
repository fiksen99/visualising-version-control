#!/bin/bash
rm **/.* 2> /dev/null
rm -rf **/target 2> /dev/null
if [[ ! -z $1 ]]
then
  rm -rf **/$1 2> /dev/null
fi
rm **/pom.xml 2> /dev/null
rm pom.xml 2> /dev/null
