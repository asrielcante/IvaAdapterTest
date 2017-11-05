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
import java.util.ArrayList;
import java.util.List;

public class FormateaECBIvaController {
	
	public static String PathECBEntrada = "/home/linuxlite/shell_scripts/ECBIVA/interfaces/";
	public static String PathECBSalida = "/home/linuxlite/shell_scripts/ECBIVA/CFDProcesados/";
	public static String PathECBCatalogos = "/home/linuxlite/shell_scripts/ECBIVA/interfaces/";
	
	public static String ingresoConceptsFileName = "conceptosIngresos.TXT";
	public static String egresoConceptsFileName = "conceptosEgresos.TXT";
	public static String filesExtension = ".TXT";
	
	BigDecimal totalMnOriginal;
	BigDecimal newTotalMn;
	
	BigDecimal ivaMnOriginal;
	BigDecimal newIvaMn;
	
	BigDecimal tasa;
	
	StringBuilder fileBlockOne;
    StringBuilder fileBlockTwo;
    
    StringBuilder lineElevenSb;
    
    String firstLine = null;
    String lineSeven = null;
    String lineEigth = null;
    String lineNine = null;
    String lineTen = null;
    String lineEleven = null;
	
	String documentType = null;
	
	public FormateaECBIvaController(){
		
	}
	
	public void processECBTxtFile(String fileName){			
		try{
			FileInputStream fileToProcess = null;
			DataInputStream in = null;
			BufferedReader br = null;
			
			FileOutputStream fos = null;
			OutputStreamWriter osw = null;
			Writer fileWriter = null;
			
			FileOutputStream fosControl = null;
			OutputStreamWriter oswControl = null;
			Writer fileWriterControl = null;
			
			File outputFile;
			File outputControlFile;

				File inputFile = new File(PathECBEntrada + fileName + filesExtension);
				if(inputFile.exists()){
					fileToProcess = new FileInputStream(PathECBEntrada + fileName + filesExtension);
					in = new DataInputStream(fileToProcess);
					br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
					String strLine;
					
					outputFile = new File(PathECBEntrada + "GENERATED_" +fileName + filesExtension);
					outputControlFile = new File(PathECBSalida +fileName + "_CONTROL" + filesExtension);
					
					fos = new FileOutputStream(outputFile);
		            osw = new OutputStreamWriter(fos, "UTF-8");    
		            fileWriter = new BufferedWriter(osw);
		            
		            fosControl = new FileOutputStream(outputControlFile);
		            oswControl = new OutputStreamWriter(fosControl, "UTF-8");    
		            fileWriterControl = new BufferedWriter(oswControl);
		            
		            fileBlockOne = new StringBuilder();
		            fileBlockTwo = new StringBuilder();
		            lineElevenSb = new StringBuilder();
		            
					newTotalMn = BigDecimal.ZERO;
					totalMnOriginal = BigDecimal.ZERO;
					tasa = BigDecimal.ZERO;
					
					firstLine = "";
					lineSeven = "";
					lineEigth = "";
				    lineNine = "";
				    lineTen = "";
				    lineEleven = "";
					
					boolean firstLoop = true;
					int ecbCount = 0;
					int ecbWritten = 0;
					while((strLine = br.readLine()) != null){
						
						if(!strLine.equals("")){
							String [] arrayValues = strLine.split("\\|");
							int lineNum = Integer.parseInt(arrayValues[0]);
							
							if(lineNum == 1){//linea 1
								ecbCount++;
								
								if(!firstLoop){
									//calcula iva
									newIvaMn = newTotalMn.multiply(tasa).divide(new BigDecimal(100));
									if(ivaMnOriginal.compareTo(newIvaMn) != 0){
										String [] lineOne = firstLine.split("\\|");
										//guardar NumTarjeta, TotalMn e ivaMn en control file
										fileWriterControl.write(lineOne[4] + "|" 
										+ lineOne[5] + "|" + newTotalMn.toString() + "|" 
										+ lineOne[6] + "|" + newIvaMn.toString() + "\n");
										//generar linea 1
										firstLine = replaceTotalsFromFirstLine(firstLine, newTotalMn, newIvaMn);
										//generar linea 7
										lineSeven = replaceIvaFromLineSeven(lineSeven, newIvaMn);
										//generar linea 9
										if(!lineNine.isEmpty()){
											lineNine = replaceIvaFromLineNine(lineNine, newIvaMn);
										}
										
									}
									
									fileWriter.write(firstLine + "\n"
											+ fileBlockOne.toString()
											+ lineSeven + "\n"
											+ (lineEigth.isEmpty() ? "" : lineEigth + "\n")
											+ (lineNine.isEmpty() ? "" : lineNine + "\n")
											+ lineTen + "\n"
											+ lineElevenSb.toString());
									
									ecbWritten++;
									resetECB();
								}
								
								firstLine = strLine;
								totalMnOriginal = new BigDecimal(arrayValues[5]);
								ivaMnOriginal = new BigDecimal(arrayValues[6]);
								
							}else if(lineNum > 1 && lineNum < 6){//lineas 2 a 5
								if(lineNum == 2){
									documentType = arrayValues[1];
								}
								fileBlockOne.append(strLine+"\n");
							}else if(lineNum == 6){//linea 6
								newTotalMn = newTotalMn.add(new BigDecimal(arrayValues[2]));
								fileBlockOne.append(strLine+"\n");
							}else if(lineNum == 7){//linea 7
								lineSeven = strLine;
							}else if(lineNum == 8){//linea 8
								lineEigth = strLine;
							}else if(lineNum == 9){//linea 9
								lineNine = strLine;
								if(arrayValues[1].equalsIgnoreCase("IVA")){
									tasa = new BigDecimal(arrayValues[2]);
								}
							}else if(lineNum == 10){//linea 10
								lineTen = strLine;
							}else if(lineNum == 11){//linea 11
								lineElevenSb.append(strLine+"\n");
							}
						}
						firstLoop = false;
					}
					if (ecbWritten < ecbCount ){//escribir ultimo ecb
						System.out.println("Escribiendo ultimo ECB");
						//calcula iva
						newIvaMn = newTotalMn.multiply(tasa).divide(new BigDecimal(100));
						if(ivaMnOriginal.compareTo(newIvaMn) != 0){
							String [] lineOne = firstLine.split("\\|");
							//guardar NumTarjeta, TotalMn e ivaMn en control file
							fileWriterControl.write(lineOne[4] + "|" 
							+ lineOne[5] + "|" + newTotalMn.toString() + "|" 
							+ lineOne[6] + "|" + newIvaMn.toString() + "\n");
							//generar linea 1
							firstLine = replaceTotalsFromFirstLine(firstLine, newTotalMn, newIvaMn);
							//generar linea 7
							lineSeven = replaceIvaFromLineSeven(lineSeven, newIvaMn);
							//generar linea 9
							if(!lineNine.isEmpty()){
								lineNine = replaceIvaFromLineNine(lineNine, newIvaMn);
							}
						}
						
						fileWriter.write(firstLine + "\n"
								+ fileBlockOne.toString()
								+ lineSeven + "\n"
								+ (lineEigth.isEmpty() ? "" : lineEigth + "\n")
								+ (lineNine.isEmpty() ? "" : lineNine + "\n")
								+ lineTen + "\n"
								+ lineElevenSb.toString());
						
						ecbWritten++;
						resetECB();
					}
					
					fileWriter.close();
					fileWriterControl.close();
					br.close();
				}else{
					System.out.println("No se encontro el archivo de entrada: "+PathECBEntrada + fileName + filesExtension);
				}
		}
		catch(Exception e){
			File delete = new File(PathECBEntrada + "GENERATED_" +fileName + filesExtension);
			if(delete.exists()){
				delete.delete();
			}
			e.printStackTrace();
			System.out.println("Exception formateaECBIvaPampa:" + e.getMessage());		
		}		
	}
	
	private String replaceTotalsFromFirstLine(String originalLine, BigDecimal newTotalMnValue, BigDecimal newIvaMnValue){
		StringBuilder controlLineSb = new StringBuilder();
		String [] originalLineArray = originalLine.split("\\|");
		newIvaMnValue = newIvaMnValue.setScale(2, BigDecimal.ROUND_HALF_EVEN);
		newTotalMnValue = newTotalMnValue.setScale(2, BigDecimal.ROUND_HALF_EVEN);
		
		for(int i = 0; i < originalLineArray.length; i++){
			if( i == 5){
				controlLineSb.append(newTotalMnValue.toString() + "|");
			}else if(i == 6){
				controlLineSb.append(newIvaMnValue.toString() + "|");
			}else{
				controlLineSb.append(originalLineArray[i] + "|");
			}
		}
		//controlLineSb.setLength(controlLineSb.length() - 1);//remove last pipe
		return controlLineSb.toString();
	}
	
	private String replaceIvaFromLineSeven(String originalLine, BigDecimal newIvaMnValue){
		StringBuilder controlLineSb = new StringBuilder();
		String [] originalLineArray = originalLine.split("\\|");
		newIvaMnValue = newIvaMnValue.setScale(2, BigDecimal.ROUND_HALF_EVEN);
		
		for(int i = 0; i < originalLineArray.length; i++){
			if(i == 2){
				controlLineSb.append(newIvaMnValue.toString() + "|");
			}else{
				controlLineSb.append(originalLineArray[i] + "|");
			}
		}
		//controlLineSb.setLength(controlLineSb.length() - 1);//remove last pipe
		return controlLineSb.toString();
	}
	
	private String replaceIvaFromLineNine(String originalLine, BigDecimal newIvaMnValue){
		StringBuilder controlLineSb = new StringBuilder();
		String [] originalLineArray = originalLine.split("\\|");
		newIvaMnValue = newIvaMnValue.setScale(2, BigDecimal.ROUND_HALF_EVEN);
		
		for(int i = 0; i < originalLineArray.length; i++){
			if(i == 3){
				controlLineSb.append(newIvaMnValue.toString() + "|");
			}else{
				controlLineSb.append(originalLineArray[i] + "|");
			}
		}
		//controlLineSb.setLength(controlLineSb.length() - 1);//remove last pipe
		return controlLineSb.toString();
	}
	
	private void resetECB(){
		fileBlockOne = new StringBuilder();
		//fileProcessLine = new StringBuilder();
		fileBlockTwo = new StringBuilder();
		
		newTotalMn = BigDecimal.ZERO;
		totalMnOriginal = BigDecimal.ZERO;
		
		ivaMnOriginal = BigDecimal.ZERO;
		newIvaMn = BigDecimal.ZERO;
		
		tasa = BigDecimal.ZERO;
		
		lineElevenSb = new StringBuilder();
		//lineElevenList = new ArrayList<String[]>();
		
		documentType = null;
		
		firstLine = "";
		lineSeven = "";
		lineEigth = "";
	    lineNine = "";
	    lineTen = "";
	    lineEleven = "";
	}
}
