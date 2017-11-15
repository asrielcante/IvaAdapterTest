#!/bin/bash

echo "Iniciando el procesamiento de las tareas"
timeStart=$(date +%F-%l-%M-%S-)"M"$(($(date +%s%N)/1000000))
echo "TIMESTARTJAVA:"$timeStart > massive.log

if [ -z "$1" ];
then
	echo "ERROR: No se encuentra el parametro nombres"
	exit -1
fi
if [ -z "$2" ];
then
	echo "ERROR: No se encuentra el parametro fecha"
	exit -1
fi
nombres=$1
echo $nombres
fecha=$2
echo $fecha
echo "Iniciando tareas Formatea ECB $nombres" >> massive.log 
java -classpath /home/linuxlite/shell_scripts/ECBIVA/javaBin/ main.FormateaECB $nombres $fecha >> massive.log 2> massive.err 

timeEnd=$(date +%F-%l-%M-%S-)"M"$(($(date +%s%N)/1000000))
echo "TIMEENDJAVA:"$timeEnd >> massive.log
echo "Terminando tareas iva process para ecb $nombre" >> massive.log 
