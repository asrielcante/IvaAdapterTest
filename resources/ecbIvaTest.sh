#!/bin/bash

echo "Iniciando el procesamiento de las tareas"
timeStart=$(date +%F-%l-%M-%S-)"M"$(($(date +%s%N)/1000000))
echo "TIMESTARTJAVA:"$timeStart > massive.log

if [ -z "$1" ];
then
	echo "ERROR: No se encuentra el parametro interfaces"
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
timestamp=$(date +"%H%M%S");
echo $timestamp

#rutas formatea
pathECBEntrada="/home/linuxlite/shell_scripts/ECBIVA/interfaces/";
pathECBSalida="/home/linuxlite/shell_scripts/ECBIVA/CFDProcesados/";
pathECBCatalogos="/home/linuxlite/shell_scripts/ECBIVA/interfaces/";

#funciones
validaSalidas(){
	#. ${DIR_UTIL}/encab_ini.sh valida_salidas.sh_$1_$2_$3
	
	echo "entra a validaSalidas: "_$1_$2_$3
	nombreA="$1"
	fechaA="$2"
	timeStampA="$3"
	returnVal=0
	if [ "$nombreA" == "CFDPTCARTER" ] || [ "$nombreA" == "CFDPTSOFOMC" ] ; then
		##valida salida carter
		if [ ! -e "${pathECBSalida}${nombreA}${fechaA}ORIGINAL_CARTER_${timeStampA}.TXT" ] ; then
			echo "Error:falta archivo de salida ORIGINAL_CARTER_ para ${nombreA}${fechaA}";
			returnVal=1
		fi
	else
		if [ "$nombreA" == "CFDLMPAMPAS" ] || [ "$nombreA" == "CFDLMPAMPAA" ] ; then
			#valida salida pampa
			if [ ! -e "${pathECBSalida}${nombreA}${fechaA}ORIGINAL_PAMPA_${timeStampA}.TXT" ] ; then
				echo "Error:falta archivo de salida ORIGINAL_PAMPA_ para ${nombreA}${fechaA}";
				returnVal=1
			fi
		fi
		
		#valida salida control
		if [ ! -e "${pathECBSalida}${nombreA}${fechaA}_CONTROL_${timeStampA}.TXT" ] ; then
			echo "Error:falta archivo de salida _CONTROL_ para ${nombreA}${fechaA}";
			returnVal=1
		fi
		#valida salida original iva
		if [ ! -e "${pathECBSalida}${nombreA}${fechaA}ORIGINAL_IVA_${timeStampA}.TXT" ] ; then
			echo "Error:falta archivo de salida ORIGINAL_IVA_ para ${nombreA}${fechaA}";
			returnVal=1
		fi
	fi
	return $returnVal
}

#valida catalogos
if [ ! -e "${pathECBCatalogos}pampaConceptos.TXT" ] || [ ! -e "${pathECBCatalogos}carterConceptos.TXT" ] ;
then
	echo "ERROR: Falta alguno de los archivos de catalogos, se detiene el proceso"
	exit 1
fi

echo "Iniciando tareas iva process para ecb $nombre" >> massive.log 
#java -classpath /home/linuxlite/shell_scripts/ECBIVA/javaBin/ main.FormateaECB $nombres $fecha $timestamp >> massive.log 2> massive.err 

#valida salidas de formatea
interfaces=(`echo $nombres | tr ',' ' '`)
for ARG in ${interfaces[@]}
do
	validaSalidas ${ARG} $fecha $timestamp
	valNumResult=$?
	if [[ $valNumResult -eq 1 ]] ; then
		echo "Error:error en validacion de salidas, se detiene el proceso"
		exit 1
	fi
done


timeEnd=$(date +%F-%l-%M-%S-)"M"$(($(date +%s%N)/1000000))
echo "TIMEENDJAVA:"$timeEnd
echo "Terminando tareas iva process para ecb $nombre" 
