#!/bin/bash

echo "Iniciando el procesamiento de las tareas"
timeStart=$(date +%F-%l-%M-%S-)"M"$(($(date +%s%N)/1000000))
echo "TIMESTARTJAVA:"$timeStart > massive.log

if [ -z "$1" ];
then
	echo "ERROR: No se encuentra el parametro nombre"
	exit -1
fi
nombre=$1
echo "Iniciando tareas iva process para ecb $nombre" >> massive.log 
java -classpath /home/linuxlite/shell_scripts/ECBIVA/javaBin/ main.IvaProcessECB $nombre >> massive.log 2> massive.err 

timeEnd=$(date +%F-%l-%M-%S-)"M"$(($(date +%s%N)/1000000))
echo "TIMEENDJAVA:"$timeEnd >> massive.log
