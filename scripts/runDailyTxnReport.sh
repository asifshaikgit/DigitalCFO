#!/bin/bash

THE_CLASSPATH=
for i in `ls /home/vmuser/idos-1.0-SNAPSHOT/lib/*.jar`
do
  THE_CLASSPATH=${THE_CLASSPATH}:${i}
  echo ${i}
done

export CLASSPATH=$CLASSPATH:${THE_CLASSPATH}:

java controllers.standalone.DailyTxnCreationAlert



