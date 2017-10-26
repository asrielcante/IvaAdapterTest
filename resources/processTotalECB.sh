#!/bin/bash
######################################################################################
# Nombre del shell: processTotalECB.sh
# Tipo de proceso (BATCH u ONLINE):BATCH
# Periodicidad: Diaria
# Equipo de produccion:<>
# Tiempo de ejecucion del proceso: El tiempo lo determina el numero de ECB en el archivo de ebtrada a atender
# Descripcion de funcionalidad general del proceso: Toma el archivo ECB indicadicado y calcula el totalMn 
# Base de Datos: N/A
# Tablas que accede: N/A
# Reprocesable (SI o NO): SI
# Lista de Variables que utiliza: 
# Fecha de creacion: 26 de octubre del 2017
# Autor ultima modificacion: Asriel Cante
# Motivo de ultima modificacion: Creacion
# Fecha de Actualizacion: 
# Autor ultima modificacion: 
# Motivo de ultima modificacion:

## Exporta directorio para ejecucion de encab_ini.sh
export DIR_UTIL=/planCFD/procesos/soptec/util
## Valida existencia y permisos de ejecucin del shell encab_ini.sh
if [ ! -x ${DIR_UTIL}/encab_ini.sh ] ; then
	echo "NO existe encab_ini.sh, el proceso NO puede continuar"
	exit 1
fi
. ${DIR_UTIL}/encab_ini.sh $0_$1
ValidaInicio
echo "Iniciando el procesamiento de las tareas"

timeStart=$(date +%F-%l-%M-%S-)"M"$(($(date +%s%N)/1000000))
echo "TIMESTARTJAVA:"$timeStart > massive.log


echo "Iniciando tareas process total ECB para ECB" >> massive.log 
$JAVA_HOME_MASIVO $JAVA_ARGS_VI_MASIVO -classpath $CLASSPATH_LIB3_3 com.interfactura.firmalocal.main.ProcessTotalECB >> massive.log 2> massive.err 



timeEnd=$(date +%F-%l-%M-%S-)"M"$(($(date +%s%N)/1000000))
echo "TIMEENDJAVA:"$timeEnd >> massive.log

echo "Fin del procesamiento de las tareas"


ValidaEstatus
