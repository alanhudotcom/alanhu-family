#!/bin/sh
echo $1
echo $2
java -Xms512m -Xmx512m -Xmn400m -jar PMHelper.jar $1 $2
