#!/bin/bash

echo "Iniciando el procesamiento de las tareas" >> massive.log
timeStart=$(date +%F-%l-%M-%S-)"M"$(($(date +%s%N)/1000000))
echo "TIMESTARTJAVA:"$timeStart > massive.log

if [ -z "$1" ];
then
	echo "ERROR: No se encuentra el parametro interfaces" >> massive.log
	exit -1
fi
if [ -z "$2" ];
then
	echo "ERROR: No se encuentra el parametro fecha" >> massive.log
	exit -1
fi
nombres=$1
echo $nombres
fecha=$2
echo $fecha
timestamp=$(date +"%H%M%S");
echo $timestamp

#rutas formatea
pathECBSalida="/home/linuxlite/shell_scripts/ECBIVA/CFDProcesados/";
DIR_INTERFACES="/home/linuxlite/shell_scripts/ECBIVA/interfaces/";

#funciones
validaSalidas(){
	
	nombreA="$1"
	fechaA="$2"
	timeStampA="$3"
	returnVal=0
	if ! [ "$nombreA" == "CFDPTCARTER" ] && ! [ "$nombreA" == "CFDPTSOFOMC" ] ; then	
		#valida salida control
		if [ ! -e "${pathECBSalida}${nombreA}${fechaA}_CONTROL_${timeStampA}.TXT" ] ; then
			echo "Error:falta archivo de salida _CONTROL_ para ${nombreA}${fechaA}" >> massive.log
			returnVal=1
		fi
	fi
	
	#valida salida original
	if [ ! -e "${pathECBSalida}${nombreA}${fechaA}ORIGINAL_${timeStampA}.TXT" ] ; then
		echo "Error:falta archivo de salida ORIGINAL_ para ${nombreA}${fechaA}" >> massive.log
		returnVal=1
	fi
	
	#valida salida control ajuste
	if [ ! -e "${pathECBSalida}${nombreA}${fechaA}_CONTROL_AJUSTE_${timeStampA}.TXT" ] ; then
		echo "Error:falta archivo de salida _CONTROL_AJUSTE_ para ${nombreA}${fechaA}" >> massive.log
		returnVal=1
	fi
	
	return $returnVal
}

#valida catalogos
if [ ! -e "${DIR_INTERFACES}/pampaConceptos.TXT" ] || [ ! -e "${DIR_INTERFACES}/carterConceptos.TXT" ] ;
then
	echo "ERROR: Falta alguno de los archivos de catalogos, se detiene el proceso" >> massive.log
	exit 1
fi

echo "Iniciando tareas iva process para ecb $nombre" >> massive.log 
java -classpath /home/linuxlite/shell_scripts/ECBIVA/javaBin/ main.FormateaECB $nombres $fecha $timestamp >> massive.log 2> massive.err 

#valida salidas de formatea
interfaces=(`echo $nombres | tr ',' ' '`)
for ARG in ${interfaces[@]}
do
	validaSalidas ${ARG} $fecha $timestamp
	valNumResult=$?
	if [[ $valNumResult -eq 1 ]] ; then
		echo "Error:error en validacion de salidas, se detiene el proceso" >> massive.log
		exit 1
	fi
done


timeEnd=$(date +%F-%l-%M-%S-)"M"$(($(date +%s%N)/1000000))
echo "TIMEENDJAVA:"$timeEnd
echo "Terminando tareas iva process para ecb $nombre" >> massive.log
