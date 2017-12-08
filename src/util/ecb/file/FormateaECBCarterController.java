package util.ecb.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FormateaECBCarterController {

	public static String PathECBEntrada = "/home/linuxlite/shell_scripts/ECBIVA/interfaces/";
	public static String PathECBSalida = "/home/linuxlite/shell_scripts/ECBIVA/CFDProcesados/";
	public static String PathECBCatalogos = "/home/linuxlite/shell_scripts/ECBIVA/interfaces/";

	public static String carterConceptsFileName = "carterConceptos.TXT";
	public static String filesExtension = ".TXT";

	BigDecimal subTotalMnOriginal;
	BigDecimal newTotalMn;
	
	BigDecimal totalConceptsA;
	BigDecimal ivaA;
	BigDecimal tasa;
	BigDecimal ivaMnOriginal;
	BigDecimal ivaB;
	BigDecimal montoConceptosGrav;
	
	BigDecimal subTotalResult = BigDecimal.ZERO;
	BigDecimal totalResult = BigDecimal.ZERO;
	BigDecimal ivaResult = BigDecimal.ZERO;


	StringBuilder fileBlockOne;
	StringBuilder fileBlockTwo;

	String firstLine = null;
	String lineTwo = null;
	String lineSeven = null;
	String lineEigth = null;
	String lineNine = null;
	String lineTen = null;
	String lineEleven = null;

	StringBuilder lineSixSb;
	StringBuilder lineElevenSb;

	List<String> lineSixList = null;

	List<String> carterConceptList = null;
	
	//rodolform
        BigDecimal UDIVal = BigDecimal.ZERO;

	public FormateaECBCarterController() {

	}

	public boolean processECBTxtFile(String fileName, String timeStamp) {
            System.out.println("Inicia Formatea CARTER - " + fileName);
            boolean result = true;
           
            try {
                FileInputStream fileToProcess = null;
                DataInputStream in = null;
                BufferedReader br = null;

                FileOutputStream fos = null;
                OutputStreamWriter osw = null;
                Writer fileWriter = null;

    //			FileOutputStream fosControl = null;
    //			OutputStreamWriter oswControl = null;
    //			Writer fileWriterControl = null;

                File outputFile;
                //File outputControlFile;   
                
                //cambio rodolfor
                File inputFileUDI = new File(PathECBEntrada + "UDIPTCARTER" + fileName.substring(fileName.length() - 8) + filesExtension );
                FileInputStream fileToProcessUDI;
                DataInputStream inUDI;
                BufferedReader brUDI;               

                //leer el archivo de UDI
                if (inputFileUDI.exists()) {
                    fileToProcessUDI = new FileInputStream(inputFileUDI);
                    inUDI = new DataInputStream(fileToProcessUDI);
                    brUDI = new BufferedReader(new InputStreamReader(inUDI, "UTF-8"));
                    String strLineUDI;

                    while ((strLineUDI = brUDI.readLine()) != null) {
                        strLineUDI = strLineUDI.trim();         
                        BigDecimal newValue = new BigDecimal(strLineUDI).setScale(2, java.math.RoundingMode.DOWN);                  
                        UDIVal = newValue;
                    }
                    //System.out.println("Valor UDI - " + UDIVal);
                   
                    fileToProcessUDI.close();
                    inUDI.close();
                    brUDI.close();                    
                }

                File inputFile = new File(PathECBEntrada + fileName + filesExtension);
                if (inputFile.exists()) {
                    fileToProcess = new FileInputStream(PathECBEntrada + fileName + filesExtension);
                    in = new DataInputStream(fileToProcess);
                    br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    String strLine;

                    loadCarterConceptList();

                    outputFile = new File(PathECBEntrada + "GENERATED_" + fileName + filesExtension);
                    //outputControlFile = new File(PathECBSalida + "CONTROL_" + fileName + filesExtension);

                    fos = new FileOutputStream(outputFile);
                    osw = new OutputStreamWriter(fos, "UTF-8");
                    fileWriter = new BufferedWriter(osw);

    //				fosControl = new FileOutputStream(outputControlFile);
    //				oswControl = new OutputStreamWriter(fosControl, "UTF-8");
    //				fileWriterControl = new BufferedWriter(oswControl);

                    fileBlockOne = new StringBuilder();
    				fileBlockTwo = new StringBuilder();
    				lineElevenSb = new StringBuilder();
                    lineSixSb = new StringBuilder();

                    newTotalMn = BigDecimal.ZERO;
                    subTotalMnOriginal = BigDecimal.ZERO;
                    
                    totalConceptsA = BigDecimal.ZERO;
                    ivaA = BigDecimal.ZERO;
                    tasa = BigDecimal.ZERO;
                    ivaMnOriginal = BigDecimal.ZERO;
                    ivaB = BigDecimal.ZERO;
                    montoConceptosGrav = BigDecimal.ZERO;
                    

                    firstLine = "";
    				lineTwo = "";
    				lineSeven = "";
    				lineEigth = "";
    				lineNine = "";
    				lineTen = "";
    				lineEleven = "";

                    lineSixList = new ArrayList<String>();

                    boolean firstLoop = true;
                    BigInteger ecbCount = BigInteger.ZERO;
                    BigInteger ecbWritten = BigInteger.ZERO;
                    StringBuilder ecbError = new StringBuilder();
                    String numCta = "NumeroDefault";
                    while ((strLine = br.readLine()) != null) {
                        strLine = strLine.trim();
                        
                        if (!strLine.equals("")) {
                            String[] arrayValues = strLine.split("\\|");
                            int lineNum = Integer.parseInt(arrayValues[0]);

                            if (lineNum == 1) {// linea 1

                                if (!firstLoop) {
                                    boolean exception = false;
                                    String ecbBakup = firstLine + "\n" + lineTwo + "\n" + fileBlockOne.toString() 
		                                    + lineSixSb.toString()
		                                    + lineSeven
    										+ "\n" + (lineEigth.isEmpty() ? "" : lineEigth + "\n")
    										+ (lineNine.isEmpty() ? "" : lineNine + "\n") + lineTen + "\n"
    										+ lineElevenSb.toString();
                                    
                                    try{
                                        firstLine = FormateaECBIvaController.truncateExcangeFromFirstLine(firstLine);
                                    }catch(Exception e){
                                        ecbError.append("-error:Error al convertir tipo de cambio a dos decimales\n");
                                    }
                                    
                                    if (ecbError.toString().isEmpty()) {
                                        if(tasa.compareTo(BigDecimal.ZERO) != 0){
                                            try{
                                                //calcular iva conceptos fuera de la lista
                                                BigDecimal ivaPaso0 = (subTotalMnOriginal.multiply(tasa)).divide(new BigDecimal(100));
                                                ivaPaso0  = ivaPaso0.setScale(2, BigDecimal.ROUND_HALF_EVEN);
                                                
                                                if(ivaPaso0.compareTo(ivaMnOriginal) != 0){
                                                    ivaA = totalConceptsA.multiply(tasa).divide(new BigDecimal(100));
                                                    ivaA = ivaA.setScale(2, BigDecimal.ROUND_HALF_EVEN);
                                                    //calcular ivaB
                                                    ivaB = ivaMnOriginal.subtract(ivaA);
                                                    //calcular monto de conceptos gravados
                                                    montoConceptosGrav = (ivaB.multiply(new BigDecimal(100))).divide(tasa);
                                                    
                                                    BigDecimal montoConceptosGravRounded = montoConceptosGrav
                                                            .setScale(2, BigDecimal.ROUND_HALF_EVEN);
                                                    BigDecimal newTotal = (montoConceptosGravRounded.add(totalConceptsA))
                                                            .setScale(2, BigDecimal.ROUND_HALF_EVEN);
                                                    
                                                    
                                                    if (subTotalMnOriginal.compareTo(newTotal) != 0) {
                                                        //cambiar montos de conceptos informados
                                                        lineSixSb = processSixLines(lineSixList, subTotalMnOriginal, montoConceptosGrav);
                                                        //calcular nuevos totales
                                                        calculateNewTotals(lineSixSb);
                                                        //reemplazar totales
                                                        firstLine = replaceTotalsFromFirstLine(firstLine, subTotalResult, totalResult, ivaResult);
        												// generar linea 2
        												lineTwo = replaceTotalsFromLineTwo(lineTwo, subTotalResult, totalResult, ivaResult);
        												// generar linea 7
        												lineSeven = replaceIvaFromLineSeven(lineSeven, ivaResult);
        												// generar linea 9
        												if (!lineNine.isEmpty()) {
        													lineNine = replaceIvaFromLineNine(lineNine, ivaResult);
        												}
                                                        
                                                    }
                                                }
                                            }catch(Exception e){
                                                System.out.println(ecbCount.toString() + "---Excepcion al hacer calculos en ECB numero de cuenta: "
                                                        + numCta);
                                                exception = true;
                                            }
                                        }
                                    } else {
                                        System.out.println(ecbCount.toString() + "---Errores en ECB numero de cuenta: " + numCta);
                                        System.out.println(ecbError.toString());
                                    }
                                    
                                    if(!exception){

                                        //rodolform
                                        //System.out.println("firstLine - " + firstLine);
                                        //System.out.println("fileBlockOne - " + fileBlockOne);
                                        //System.out.println("lineSixSb - " + lineSixSb);
                                        //System.out.println("fileBlockTwo - " + fileBlockTwo);                                      

                                        String txtUDI = "|UDI|";
                                        String txtMXV = "|MXV|";
                                        if(lineTwo.contains(txtUDI) )
                                        {
                                        	StringBuilder lineTwoSb = new StringBuilder(lineTwo);
                                            //reemplazar UDI por MVX
                                            int indexOfUDI = lineTwoSb.indexOf(txtUDI);
                                            //System.out.println("indexOfUDI - " + indexOfUDI);
                                            lineTwoSb.replace(indexOfUDI, indexOfUDI+5,txtMXV);
                                            //System.out.println("fileBlockOne reemplazo - " + fileBlockOne);
                                            lineTwo = lineTwoSb.toString();
                                            //reemplazar el 0.00 por el tipo de cambio traido del archivo de UDIYYYYMMDD
                                            //donde la frcha debe ser igual a la del archivo fuente                                           
                                            String[] arrayFirstLine = firstLine.split("\\|");                                         
                                            firstLine  = "01|"
                                                + arrayFirstLine[1].toString() + "|"
                                                + arrayFirstLine[2].toString() + "|"
                                                + arrayFirstLine[3].toString() + "|"
                                                + arrayFirstLine[4].toString() + "|"
                                                + arrayFirstLine[5].toString() + "|"
                                                + arrayFirstLine[6].toString() + "|"
                                                + arrayFirstLine[7].toString() + "|"  
                                                + arrayFirstLine[8].toString() + "|"               
                                                + UDIVal;

                                                //System.out.println("firstLine reemplazo - " + firstLine);
                                        }
                                        //fin rodolform

                                        fileWriter.write(firstLine + "\n" + lineTwo + "\n" + fileBlockOne.toString() 
		                                    + lineSixSb.toString()
		                                    + lineSeven
											+ "\n" + (lineEigth.isEmpty() ? "" : lineEigth + "\n")
											+ (lineNine.isEmpty() ? "" : lineNine + "\n") + lineTen + "\n"
											+ lineElevenSb.toString());
                                    }else{
                                        fileWriter.write(ecbBakup);
                                    }

                                    ecbWritten = ecbWritten.add(BigInteger.ONE);
                                    resetECB();
                                }

                                ecbCount = ecbCount.add(BigInteger.ONE);
                                ecbError = new StringBuilder();
                                firstLine = strLine;
                                
                                try {
                                    ivaMnOriginal = new BigDecimal(arrayValues[6].trim());
                                } catch (Exception e) {
                                    ecbError.append("-error: no se pudo leer el iva informado en linea 1\n");
                                }
                                try{
                                    numCta = arrayValues[2].trim();
                                }catch(Exception e){
                                    numCta = "NumeroDefault";
                                    ecbError.append("-error: no se pudo leer el numero de cuenta\n");
                                }

                            } else if (lineNum == 2) {// lineas
                            	try {
                                    subTotalMnOriginal = new BigDecimal(arrayValues[6].trim());
                                } catch (Exception e) {
                                    ecbError.append("-error: no se pudo leer el subtotal\n");
                                }
                            	lineTwo = strLine;
                            } else if (lineNum > 2 && lineNum < 6) {// lineas 3 a 5
    							fileBlockOne.append(strLine + "\n");
    						} else if (lineNum == 6) {// linea 6
                                lineSixSb.append(strLine + "\n");
                                lineSixList.add(strLine);
                                try{
                                    if(!listContains(carterConceptList, arrayValues[1].trim())){
                                        totalConceptsA = totalConceptsA.add(new BigDecimal(arrayValues[2].trim()));
                                    }
                                }catch(Exception e){
                                    ecbError.append("-error: no se pudo hacer la suma de importes de conceptos\n");
                                }
                            } else if (lineNum == 7) {// linea 7
    							lineSeven = strLine;
    						} else if (lineNum == 8) {// linea 8
    							lineEigth = strLine;
    						} else if (lineNum == 9) {// linea 9
    							lineNine = strLine;
    							try {
    								if (arrayValues[1].equalsIgnoreCase("IVA")) {
    									tasa = new BigDecimal(arrayValues[2].trim());
    								}
    							} catch (Exception e) {
    								ecbError.append("-error: No se pudo leer el valor de tasa\n");
    							}

    						} else if (lineNum == 10) {// linea 10
    							lineTen = strLine;
    						} else if (lineNum == 11) {// linea 11
    							lineElevenSb.append(strLine + "\n");
    						}
                        }
                        firstLoop = false;
                    }
                    if (ecbWritten.compareTo(ecbCount) != 0) {//escribir ultimo ecb
                        System.out.println("Escribiendo ultimo ECB");


                        boolean exception = false;
                        String ecbBakup = firstLine + "\n" + lineTwo + "\n" + fileBlockOne.toString() 
                                + lineSixSb.toString()
                                + lineSeven
								+ "\n" + (lineEigth.isEmpty() ? "" : lineEigth + "\n")
								+ (lineNine.isEmpty() ? "" : lineNine + "\n") + lineTen + "\n"
								+ lineElevenSb.toString();
                        
                        try{
                            firstLine = FormateaECBIvaController.truncateExcangeFromFirstLine(firstLine);
                        }catch(Exception e){
                            ecbError.append("-error:Error al convertir tipo de cambio a dos decimales\n");
                        }
                        
                        if (ecbError.toString().isEmpty()) {
                            if(tasa.compareTo(BigDecimal.ZERO) != 0){
                                try{
                                    //calcular iva conceptos fuera de la lista
                                    BigDecimal ivaPaso0 = (subTotalMnOriginal.multiply(tasa)).divide(new BigDecimal(100));
                                    ivaPaso0  = ivaPaso0.setScale(2, BigDecimal.ROUND_HALF_EVEN);
                                    
                                    if(ivaPaso0.compareTo(ivaMnOriginal) != 0){
                                        ivaA = totalConceptsA.multiply(tasa).divide(new BigDecimal(100));
                                        ivaA = ivaA.setScale(2, BigDecimal.ROUND_HALF_EVEN);
                                        //calcular ivaB
                                        ivaB = ivaMnOriginal.subtract(ivaA);
                                        //calcular monto de conceptos gravados
                                        montoConceptosGrav = (ivaB.multiply(new BigDecimal(100))).divide(tasa);
                                        
                                        BigDecimal montoConceptosGravRounded = montoConceptosGrav
                                                .setScale(2, BigDecimal.ROUND_HALF_EVEN);
                                        BigDecimal newTotal = (montoConceptosGravRounded.add(totalConceptsA))
                                                .setScale(2, BigDecimal.ROUND_HALF_EVEN);
                                        
                                        
                                        if (subTotalMnOriginal.compareTo(newTotal) != 0) {
                                            //cambiar montos de conceptos informados
                                            lineSixSb = processSixLines(lineSixList, subTotalMnOriginal, montoConceptosGrav);
                                            //calcular nuevos totales
                                            calculateNewTotals(lineSixSb);
                                            //reemplazar totales
                                            firstLine = replaceTotalsFromFirstLine(firstLine, subTotalResult, totalResult, ivaResult);
											// generar linea 2
											lineTwo = replaceTotalsFromLineTwo(lineTwo, subTotalResult, totalResult, ivaResult);
											// generar linea 7
											lineSeven = replaceIvaFromLineSeven(lineSeven, ivaResult);
											// generar linea 9
											if (!lineNine.isEmpty()) {
												lineNine = replaceIvaFromLineNine(lineNine, ivaResult);
											}
                                            
                                        }
                                    }
                                }catch(Exception e){
                                    System.out.println(ecbCount.toString() + "---Excepcion al hacer calculos en ECB numero de cuenta: "
                                            + numCta);
                                    exception = true;
                                }
                            }
                        } else {
                            System.out.println(ecbCount.toString() + "---Errores en ECB numero de cuenta: " + numCta);
                            System.out.println(ecbError.toString());
                        }
                        
                        if(!exception){

                            //rodolform
                            //System.out.println("firstLine - " + firstLine);
                            //System.out.println("fileBlockOne - " + fileBlockOne);
                            //System.out.println("lineSixSb - " + lineSixSb);
                            //System.out.println("fileBlockTwo - " + fileBlockTwo);                                      

                            String txtUDI = "|UDI|";
                            String txtMXV = "|MXV|";
                            if(lineTwo.contains(txtUDI) )
                            {
                            	StringBuilder lineTwoSb = new StringBuilder(lineTwo);
                                //reemplazar UDI por MVX
                                int indexOfUDI = lineTwoSb.indexOf(txtUDI);
                                //System.out.println("indexOfUDI - " + indexOfUDI);
                                lineTwoSb.replace(indexOfUDI, indexOfUDI+5,txtMXV);
                                //System.out.println("fileBlockOne reemplazo - " + fileBlockOne);
                                lineTwo = lineTwoSb.toString();
                                //reemplazar el 0.00 por el tipo de cambio traido del archivo de UDIYYYYMMDD
                                //donde la frcha debe ser igual a la del archivo fuente                                           
                                String[] arrayFirstLine = firstLine.split("\\|");                                         
                                firstLine  = "01|"
                                    + arrayFirstLine[1].toString() + "|"
                                    + arrayFirstLine[2].toString() + "|"
                                    + arrayFirstLine[3].toString() + "|"
                                    + arrayFirstLine[4].toString() + "|"
                                    + arrayFirstLine[5].toString() + "|"
                                    + arrayFirstLine[6].toString() + "|"
                                    + arrayFirstLine[7].toString() + "|"  
                                    + arrayFirstLine[8].toString() + "|"               
                                    + UDIVal;

                                    //System.out.println("firstLine reemplazo - " + firstLine);
                            }
                            //fin rodolform

                            fileWriter.write(firstLine + "\n" + lineTwo + "\n" + fileBlockOne.toString() 
                                + lineSixSb.toString()
                                + lineSeven
								+ "\n" + (lineEigth.isEmpty() ? "" : lineEigth + "\n")
								+ (lineNine.isEmpty() ? "" : lineNine + "\n") + lineTen + "\n"
								+ lineElevenSb.toString());
                        }else{
                            fileWriter.write(ecbBakup);
                        }

                        ecbWritten = ecbWritten.add(BigInteger.ONE);
                        resetECB();
                        
                    }                   

                    fileWriter.close();
                    //fileWriterControl.close();
                    br.close();
                    
                    File movedFile = new File(PathECBSalida + fileName + "ORIGINAL_" + timeStamp + filesExtension);
                    if (FormateaECBPampaController.moveFile(inputFile, movedFile)) {// mover archivo original
                        // renombrar archivo generado
                        if (FormateaECBPampaController.moveFile(outputFile,
                                new File(PathECBEntrada + fileName + filesExtension))) {
                            result = true;
                        } else {
                            System.out.println("No se pudo renombrar el archivo generado");
                            result = false;
                        }
                    } else {
                        System.out.println("No se pudo mover el archivo original");
                        result = false;
                    }

                } else {
                    System.out.println("No se encontro el archivo de entrada: " + PathECBEntrada + fileName + filesExtension);
                    result = false;
                }
                if (!result) {
                    File delete = new File(PathECBEntrada + "GENERATED_" + fileName + filesExtension);
                    if (delete.exists()) {
                        delete.delete();
                    }
                }
                return result;
            } catch (Exception e) {
                File delete = new File(PathECBEntrada + "GENERATED_" + fileName + filesExtension);
                if (delete.exists()) {
                    delete.delete();
                }
                e.printStackTrace();
                System.out.println("Exception formateaECBCarter:" + e.getMessage());
                return false;
            }
        }
	
	private StringBuilder processSixLines(List<String> sixLines, BigDecimal subTotalOriginal, BigDecimal subTotalNuevo){
		StringBuilder result = new StringBuilder();
		
		for(String line : sixLines){
			String[] lineArray = line.split("\\|");
			String newLine = line;
			if(listContains(carterConceptList, lineArray[1])){
				
				BigDecimal importeOriginal = new BigDecimal(lineArray[2]);
				
				
				MathContext mathC = MathContext.DECIMAL64;
				BigDecimal division = importeOriginal.divide(subTotalOriginal, mathC);
				
				BigDecimal nuevoImporte = subTotalNuevo.multiply(division);
				
				nuevoImporte = 	nuevoImporte.setScale(2, BigDecimal.ROUND_HALF_EVEN);
				
				BigDecimal montoExento = importeOriginal.subtract(nuevoImporte);
				montoExento=montoExento.setScale(2, BigDecimal.ROUND_HALF_EVEN);
				
				newLine = "";
				if(nuevoImporte.compareTo(BigDecimal.ZERO) != 0){
					newLine = newLine + "06|"
							+ lineArray[1] + "|"
							+ nuevoImporte.toString() + "\n";
				}
				newLine = newLine	+"06|"
						+ lineArray[1] + " EXENTO" + "|"
						+ montoExento.toString();
			}
			result.append(newLine);
			result.append("\n");
		}
		
		return result;
	}

	private void resetECB() {
		fileBlockOne = new StringBuilder();
		fileBlockTwo = new StringBuilder();

		lineSixSb = new StringBuilder();
		lineElevenSb = new StringBuilder();

		firstLine = "";
		lineSeven = "";
		lineEigth = "";
		lineNine = "";
		lineTen = "";
		lineEleven = "";
		lineSixList = new ArrayList<String>();
		
		subTotalResult = BigDecimal.ZERO;
		totalResult = BigDecimal.ZERO;
		ivaResult = BigDecimal.ZERO;
	}

	private void loadCarterConceptList() throws Exception {
		FileInputStream fis = new FileInputStream(PathECBCatalogos + carterConceptsFileName);
		DataInputStream dis = new DataInputStream(fis);
		BufferedReader bfr = new BufferedReader(new InputStreamReader(dis, "UTF-8"));
		String carterConceptLine = null;
		carterConceptList = new ArrayList<String>();

		while ((carterConceptLine = bfr.readLine()) != null) {
			if(!carterConceptLine.trim().isEmpty())
				carterConceptList.add(carterConceptLine.trim());
		}
		bfr.close();
	}
	
	private boolean listContains(List<String> list, String value){
		for(String val : list){
			if (val.equalsIgnoreCase(value)) return true;
		}
		return false;
	}
	
	private void calculateNewTotals(StringBuilder lines){
		String[] sixArray = lines.toString().split("\\n");
		
		BigDecimal newIvaCalculated = BigDecimal.ZERO;
		BigDecimal newTotalCalculated = BigDecimal.ZERO;
		BigDecimal newSubtotalCalculated = BigDecimal.ZERO;
		
		if(sixArray.length > 0){
			for(int i = 0; i < sixArray.length; i++){
				String line = sixArray[i];
				String[] lineArray = line.split("\\|");
				BigDecimal importe = new BigDecimal(lineArray[2].trim());
				if(listContains(carterConceptList, lineArray[1].trim())){
					//System.out.println("Carter concepto si aplica iva:" + lineArray[1].trim());
					//con redondeo
					BigDecimal iva = (importe.multiply(tasa)).divide(new BigDecimal(100));
					iva = iva.setScale(2, BigDecimal.ROUND_HALF_EVEN);
					newIvaCalculated = newIvaCalculated.add(iva);
					
				}
				newSubtotalCalculated = newSubtotalCalculated = newSubtotalCalculated.add(importe);
			}
			
			newTotalCalculated = newSubtotalCalculated.add(newIvaCalculated);
			
			//System.out.println("Nuevo total iva calculado:" + newIvaCalculated.toString());
			newIvaCalculated = newIvaCalculated.setScale(2, BigDecimal.ROUND_HALF_EVEN);
			//System.out.println("Nuevo total iva calculado redondeado:" + newIvaCalculated.toString());
			
			//System.out.println("Nuevo subTotal calculado redondeado:" + newSubtotalCalculated.toString());
			newSubtotalCalculated = newSubtotalCalculated.setScale(2, BigDecimal.ROUND_HALF_EVEN);
			//System.out.println("Nuevo subTotal calculado redondeado:" + newSubtotalCalculated.toString());
			
			//System.out.println("Nuevo total calculado redondeado:" + newTotalCalculated.toString());
			newTotalCalculated = newTotalCalculated.setScale(2, BigDecimal.ROUND_HALF_EVEN);
			//System.out.println("Nuevo total calculado redondeado:" + newTotalCalculated.toString());

			subTotalResult = newSubtotalCalculated;
			totalResult = newTotalCalculated;
			ivaResult = newIvaCalculated;
		}
	}
	
	private String replaceTotalsFromFirstLine(String originalLine, BigDecimal subtotalRes,
			BigDecimal totalRes, BigDecimal ivaRes) {
		StringBuilder controlLineSb = new StringBuilder();
		String[] originalLineArray = originalLine.split("\\|");

		for (int i = 0; i < originalLineArray.length; i++) {
			if (i == 5) {
				controlLineSb.append(subtotalRes.toString() + "|");
			} else if (i == 6) {
				controlLineSb.append(ivaRes.toString() + "|");
			} else {
				controlLineSb.append(originalLineArray[i] + "|");
			}
		}
		String lastChar = originalLine.substring(originalLine.length() - 1);
		if (!lastChar.equals("|")) {
			controlLineSb.setLength(controlLineSb.length() - 1);// remove last pipe
		}

		return controlLineSb.toString();
	}
	private String replaceTotalsFromLineTwo(String originalLine, BigDecimal subtotalRes,
			BigDecimal totalRes, BigDecimal ivaRes) {
		
		StringBuilder controlLineSb = new StringBuilder();
		String[] originalLineArray = originalLine.split("\\|");

		for (int i = 0; i < originalLineArray.length; i++) {
			if (i == 6) {
				controlLineSb.append(subtotalRes.toString() + "|");
			} else if (i == 7) {
				controlLineSb.append(totalRes.toString() + "|");
			} else {
				controlLineSb.append(originalLineArray[i] + "|");
			}
		}

		String lastChar = originalLine.substring(originalLine.length() - 1);
		if (!lastChar.equals("|")) {
			controlLineSb.setLength(controlLineSb.length() - 1);// remove last pipe
		}

		return controlLineSb.toString();
	}
	private String replaceIvaFromLineSeven(String originalLine, BigDecimal ivaRes) {
		StringBuilder controlLineSb = new StringBuilder();
		String[] originalLineArray = originalLine.split("\\|");

		for (int i = 0; i < originalLineArray.length; i++) {
			if (i == 2) {
				controlLineSb.append(ivaRes.toString() + "|");
			} else {
				controlLineSb.append(originalLineArray[i] + "|");
			}
		}

		String lastChar = originalLine.substring(originalLine.length() - 1);
		if (!lastChar.equals("|")) {
			controlLineSb.setLength(controlLineSb.length() - 1);// remove last pipe
		}

		return controlLineSb.toString();
	}
	private String replaceIvaFromLineNine(String originalLine, BigDecimal ivaRes) {
		StringBuilder controlLineSb = new StringBuilder();
		String[] originalLineArray = originalLine.split("\\|");

		for (int i = 0; i < originalLineArray.length; i++) {
			if (i == 3) {
				controlLineSb.append(ivaRes.toString() + "|");
			} else {
				controlLineSb.append(originalLineArray[i] + "|");
			}
		}

		String lastChar = originalLine.substring(originalLine.length() - 1);
		if (!lastChar.equals("|")) {
			controlLineSb.setLength(controlLineSb.length() - 1);// remove last pipe
		}

		return controlLineSb.toString();
	}
}
