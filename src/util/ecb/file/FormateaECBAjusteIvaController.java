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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormateaECBAjusteIvaController {

	public static String PathECBEntrada = "/home/linuxlite/shell_scripts/ECBIVA/interfaces/";
	public static String PathECBSalida = "/home/linuxlite/shell_scripts/ECBIVA/CFDProcesados/";
	public static String PathECBCatalogos = "/home/linuxlite/shell_scripts/ECBIVA/interfaces/";

	public static String ajusteIvaConceptsFileName = "ajusteIvaConceptos.TXT";
	//List<String[]> ajusteIvaConceptList = null;
	Map<String, String> ajusteIvaConceptList = null;
	
	
	public static String filesExtension = ".TXT";

	BigDecimal ivaMnOriginal;
	BigDecimal newIvaMn;

	BigDecimal tasa;

	StringBuilder fileBlockOne;
	StringBuilder fileBlockTwo;

	StringBuilder lineSixSb;
	StringBuilder lineElevenSb;

	String firstLine = null;
	String lineSeven = null;
	String lineEigth = null;
	String lineNine = null;
	String lineTen = null;
	String lineEleven = null;
	
	private boolean isCarter = false;
	
	List<String> sixListOriginal = new ArrayList<String>();
	List<String> sixList = new ArrayList<String>();

	public FormateaECBAjusteIvaController() {

	}

	public boolean processECBTxtFile(String fileName, String timeStamp) {
		System.out.println("Inicia ajuste IVA - " + fileName);
		
		if(fileName.toUpperCase().contains("CFDPTCARTER") || fileName.toUpperCase().contains("CFDPTSOFOMC")){
			isCarter = true;
		}else{
			isCarter = false;
		}
		
		boolean result = true;
		try {
			//mapCatalogos = Util.readXLSFile(properties.getUrlArchivoCatalogs());
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
			if (inputFile.exists()) {
				fileToProcess = new FileInputStream(PathECBEntrada + fileName + filesExtension);
				in = new DataInputStream(fileToProcess);
				br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
				String strLine;
				
				loadAjusteIvaConceptList();

				outputFile = new File(PathECBEntrada + "GENERATED_" + fileName + filesExtension);
				outputControlFile = new File(PathECBSalida + fileName + "_CONTROL_AJUSTE_" + timeStamp + filesExtension);

				fos = new FileOutputStream(outputFile);
				osw = new OutputStreamWriter(fos, "UTF-8");
				fileWriter = new BufferedWriter(osw);

				fosControl = new FileOutputStream(outputControlFile);
				oswControl = new OutputStreamWriter(fosControl, "UTF-8");
				fileWriterControl = new BufferedWriter(oswControl);

				ivaMnOriginal = BigDecimal.ZERO;
				newIvaMn = BigDecimal.ZERO;
				
				fileBlockOne = new StringBuilder();
				fileBlockTwo = new StringBuilder();
				lineSixSb = new StringBuilder();
				lineElevenSb = new StringBuilder();

				tasa = BigDecimal.ZERO;

				firstLine = "";
				lineSeven = "";
				lineEigth = "";
				lineNine = "";
				lineTen = "";
				lineEleven = "";

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
								String ecbBakup = firstLine + "\n" + fileBlockOne.toString() + lineSixSb.toString() 
										+ lineSeven + "\n" + (lineEigth.isEmpty() ? "" : lineEigth + "\n")
										+ (lineNine.isEmpty() ? "" : lineNine + "\n") + lineTen + "\n"
										+ lineElevenSb.toString();
								
								if (ecbError.toString().isEmpty()) {
									if (tasa.compareTo(BigDecimal.ZERO) != 0) {
										try {
											if(ivaMnOriginal.compareTo(BigDecimal.ZERO) != 0){
												lineSixSb = adjustIvaFromLinesSix(lineSixSb);
											
												if(newIvaMn.compareTo(BigDecimal.ZERO) > 0 && newIvaMn.compareTo(ivaMnOriginal) == 0){
													// guardar en control file
													String[] lineOne = firstLine.split("\\|");
													String controlLine = generateControlLine(numCta, lineOne[4], "iva ajustado", newIvaMn);
													fileWriterControl.write(controlLine);
												}
											}
										} catch (Exception e) {
											System.out.println(ecbCount.toString() + "---Excepcion al ajustar iva en ECB numero de cuenta: "
													+ numCta);
											exception = true;
											e.printStackTrace();
										}
									}
								} else {
									System.out.println(ecbCount.toString() + "---Errores en ECB numero de cuenta: " + numCta);
									System.out.println(ecbError.toString());
								}
								
								if(!exception){
									fileWriter.write(firstLine + "\n" + fileBlockOne.toString() + lineSixSb.toString() 
											+ lineSeven + "\n" + (lineEigth.isEmpty() ? "" : lineEigth + "\n")
											+ (lineNine.isEmpty() ? "" : lineNine + "\n") + lineTen + "\n"
											+ lineElevenSb.toString());
								}else{
									fileWriter.write(ecbBakup);
								}

								ecbWritten = ecbWritten.add(BigInteger.ONE);
								String writeTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
								System.out.println(ecbWritten.toString() + " - numCta: " + numCta + " - escrito: " + writeTimeStamp);
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

						} else if (lineNum > 1 && lineNum < 6) {// lineas 2 a 5
							fileBlockOne.append(strLine + "\n");
						} else if (lineNum == 6) {// linea 6
							//guardar filas 6
							lineSixSb.append(strLine + "\n");
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
				if (ecbWritten.compareTo(ecbCount) != 0) {// escribir ultimo ecb
					System.out.println("Escribiendo ultimo ECB - Ajuste IVA");

					boolean exception = false;
					String ecbBakup = firstLine + "\n" + fileBlockOne.toString() + lineSixSb.toString() 
							+ lineSeven + "\n" + (lineEigth.isEmpty() ? "" : lineEigth + "\n")
							+ (lineNine.isEmpty() ? "" : lineNine + "\n") + lineTen + "\n"
							+ lineElevenSb.toString();
					
					if (ecbError.toString().isEmpty()) {
						if (tasa.compareTo(BigDecimal.ZERO) != 0) {
							try {
								if(ivaMnOriginal.compareTo(BigDecimal.ZERO) != 0){
									lineSixSb = adjustIvaFromLinesSix(lineSixSb);
								
									if(newIvaMn.compareTo(BigDecimal.ZERO) > 0 && newIvaMn.compareTo(ivaMnOriginal) == 0){
										// guardar en control file
										String[] lineOne = firstLine.split("\\|");
										String controlLine = generateControlLine(numCta, lineOne[4], "iva ajustado", newIvaMn);
										fileWriterControl.write(controlLine);
									}
								}
							} catch (Exception e) {
								System.out.println(ecbCount.toString() + "---Excepcion al ajustar iva en ECB numero de cuenta: "
										+ numCta);
								exception = true;
								e.printStackTrace();
							}
						}
					} else {
						System.out.println(ecbCount.toString() + "---Errores en ECB numero de cuenta: " + numCta);
						System.out.println(ecbError.toString());
					}
					
					if(!exception){
						fileWriter.write(firstLine + "\n" + fileBlockOne.toString() + lineSixSb.toString() 
								+ lineSeven + "\n" + (lineEigth.isEmpty() ? "" : lineEigth + "\n")
								+ (lineNine.isEmpty() ? "" : lineNine + "\n") + lineTen + "\n"
								+ lineElevenSb.toString());
					}else{
						fileWriter.write(ecbBakup);
					}

					ecbWritten = ecbWritten.add(BigInteger.ONE);
					String writeTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
					System.out.println(ecbWritten.toString() + " - numCta: " + numCta + " - escrito: " + writeTimeStamp);
					resetECB();
				}

				fileWriter.close();
				fileWriterControl.close();
				br.close();
				//File movedFile = new File(PathECBSalida + fileName + "ORIGINAL_AJUSTE_" + timeStamp + filesExtension);
				if (inputFile.delete()) {// eliminar archivo original
					// renombrar archivo generado
					if (FormateaECBPampaController.moveFile(outputFile,
							new File(PathECBEntrada + fileName + filesExtension))) {
						result = true;
					} else {
						System.out.println("No se pudo renombrar el archivo generado");
						result = false;
					}
				} else {
					System.out.println("No se pudo eliminar el archivo original");
					result = false;
				}

			} else {
				System.out
						.println("No se encontro el archivo de entrada: " + PathECBEntrada + fileName + filesExtension);
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
			System.out.println("Exception formateaECBIva - " + fileName + ": " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	private String generateControlLine(String NumCuenta, String NumTarjeta, String descripcion, BigDecimal newIvaMnVal) {

		StringBuilder controlLineSb = new StringBuilder();

		newIvaMnVal = newIvaMnVal.setScale(2, BigDecimal.ROUND_HALF_EVEN);

		controlLineSb.append(NumCuenta + "|");
		controlLineSb.append(NumTarjeta + "|");
		controlLineSb.append(descripcion + "|");
		controlLineSb.append(newIvaMnVal.toString() + "\n");

		return controlLineSb.toString();
	}

	private void resetECB() {
		fileBlockOne = new StringBuilder();
		fileBlockTwo = new StringBuilder();

		ivaMnOriginal = BigDecimal.ZERO;
		newIvaMn = BigDecimal.ZERO;

		tasa = BigDecimal.ZERO;

		lineSixSb = new StringBuilder();
		lineElevenSb = new StringBuilder();

		firstLine = "";
		lineSeven = "";
		lineEigth = "";
		lineNine = "";
		lineTen = "";
		lineEleven = "";
	}
	
	private StringBuilder adjustIvaFromLinesSix(StringBuilder lines){
		StringBuilder result = new StringBuilder();
		String[] sixArray = lines.toString().split("\\n");
		//List<String[]> sixList = new ArrayList<String[]>();
		String lastChar = "";
		boolean entraAjuste = false;
		boolean substract = false;
		if(sixArray.length > 1){
			
			lastChar = sixArray[0].substring(sixArray[0].length() - 1);
			if (!lastChar.equals("|")) {
				lastChar = "";
			}
			
			BigDecimal ajuste = BigDecimal.ZERO;
			BigDecimal totalIvaInicial = new BigDecimal("0.00");
			//for(String line : sixArray){
			for(int i = 0; i < sixArray.length; i++){
				String line = sixArray[i];
				String[] lineArray = line.split("\\|");
				//String[] lineArrayOriginal = line.split("\\|");
				if(conceptRequiresIva(lineArray[1].trim())){
					//System.out.println("Concepto incluido en calculo: " + line);
					BigDecimal importe = new BigDecimal(lineArray[2].trim());
					BigDecimal iva = (importe.multiply(tasa)).divide(new BigDecimal(100));
					iva = iva.setScale(2, BigDecimal.ROUND_HALF_EVEN);
					totalIvaInicial = totalIvaInicial.add(iva);
				}
				sixList.add(line);
				//System.arraycopy( lineArray, 0, lineArrayOriginal, 0, lineArray.length );
				String lineOriginal = line;
				sixListOriginal.add(lineOriginal);
			}
			int conceptsCount = sixList.size();
			//System.out.println("iva original: " + ivaMnOriginal.toString()); 
			//System.out.println("iva inicial: " + totalIvaInicial.toString()); 
			BigDecimal diferencia = BigDecimal.ZERO;
			if(totalIvaInicial.compareTo(ivaMnOriginal) > 0){
				diferencia = totalIvaInicial.subtract(ivaMnOriginal);
				substract = true;
			}else{
				diferencia = ivaMnOriginal.subtract(totalIvaInicial);
			}
			//System.out.println("Diferencia iva: "+ diferencia.toString());
			diferencia = diferencia.setScale(2, BigDecimal.ROUND_HALF_EVEN); 
			BigDecimal totalIva = BigDecimal.ZERO;
			if(diferencia.compareTo(new BigDecimal("0.5")) < 0){
				//System.out.println("Entro ajuste < 0.5" + (diferencia.compareTo(new BigDecimal("0.5")) < 0));
				int loops = 1;
				boolean stopped = false;
				do{
					ajuste = ajuste.add(new BigDecimal("0.01"));
					
					for(int c = 1; c < conceptsCount; c++){
						totalIva = BigDecimal.ZERO;
						for(String line : sixList){
							//System.out.println("Concepto: "+ lineArray[1].trim());
							String[] lineArray = line.split("\\|");
							if(conceptRequiresIva(lineArray[1].trim())){
								//System.out.println("-Concepto tomado en cuenta-");
								BigDecimal importe = new BigDecimal(lineArray[2].trim());
								BigDecimal iva = (importe.multiply(tasa)).divide(new BigDecimal(100));
								iva = iva.setScale(2, BigDecimal.ROUND_HALF_EVEN);
								totalIva = totalIva.add(iva);
							}
						}
						totalIva = totalIva.setScale(2, BigDecimal.ROUND_HALF_EVEN);
						
						if(totalIva.compareTo(ivaMnOriginal) != 0){
							//System.out.println("Iva actual calculado: " + totalIva.toString());
							//System.out.println("Iva informado: " + ivaMnOriginal.toString());
							//System.out.println("---valor ajuste: " + ajuste.toString()); 
							entraAjuste=true;
							
//							sixList = new ArrayList<String[]>();
//							for(int i = 0; i < sixArray.length; i++){
//								String line = sixArray[i];
//								String[] lineArray = line.split("\\|");
//								sixList.add(lineArray);
//							}
							sixList = new ArrayList<String>(sixListOriginal);
							String[] incrementArray = sixList.get(0).split("\\|");
							String[] decrementArray = sixList.get(c).split("\\|");
							//System.out.println("Increment concept val: " + incrementArray[2]);
							//System.out.println("decrement concept val: " + decrementArray[2]);
							BigDecimal increment = BigDecimal.ZERO;
							BigDecimal decrement = BigDecimal.ZERO;
							if(!substract){
								//System.out.println("Entra add");
								increment = new BigDecimal(incrementArray[2]).add(ajuste)
										.setScale(2, BigDecimal.ROUND_HALF_EVEN);
								decrement = new BigDecimal(decrementArray[2]).subtract(ajuste)
										.setScale(2, BigDecimal.ROUND_HALF_EVEN);
							}else{
								//System.out.println("Entra substract");
								increment = new BigDecimal(incrementArray[2]).subtract(ajuste)
										.setScale(2, BigDecimal.ROUND_HALF_EVEN);
								decrement = new BigDecimal(decrementArray[2]).add(ajuste)
										.setScale(2, BigDecimal.ROUND_HALF_EVEN);
							}
		
							//String[] incrementConcept = sixList.get(0);
							//String[] decrementConcept = sixList.get(c);
							
							incrementArray[2] = increment.toString();
							decrementArray[2] = decrement.toString();
							
							sixList.set(0, generateSixLineFromArray(incrementArray, lastChar));
							sixList.set(c, generateSixLineFromArray(decrementArray, lastChar));
							
						}else{
							if(entraAjuste){
								newIvaMn = totalIva;
								//System.out.println("---Si ajusto iva---");
							}
							stopped = true;
							break;
						}
	//					if(loops == (sixList.size()-1)){
	//						loops = 1;
	//					}else{
	//						loops++;
	//					}
					}
					if(stopped){
						break;
					}
				} while(ajuste.compareTo(new BigDecimal("0.05")) < 0 );
			}else{
				result = lines;
			}
		}else{
			result = lines;
		}
		
		if(newIvaMn.compareTo(BigDecimal.ZERO) > 0 && newIvaMn.compareTo(ivaMnOriginal) == 0){
			//concatenar nuevas lineas 6
			for(String line : sixList){
//				result.append(line[0].trim());
//				result.append("|");
//				result.append(line[1].trim());
//				result.append("|");
//				result.append(line[2].trim());
//				result.append(lastChar);
				//String[] lineArray = line.split("\\|");
				result.append(line);
				result.append("\n");
			}
			
		}else{
			result = lines;
		}
		
		return result;
	}
	
	public String generateSixLineFromArray(String[] sixArray, String lastChar){
		StringBuilder result = new StringBuilder();
		
		result.append(sixArray[0].trim());
		result.append("|");
		result.append(sixArray[1].trim());
		result.append("|");
		result.append(sixArray[2].trim());
		result.append(lastChar);
		
		return result.toString();
	}
	
	private boolean conceptRequiresIva(String concept) {
		boolean result = false;
		if (ajusteIvaConceptList != null) {
//			for (String[] row : ajusteIvaConceptList) {
//				if (row.length == 5) {
//					if (row[0].equals("002") && !row[1].equalsIgnoreCase("Exento") && row[4].equalsIgnoreCase(concept)) {
//						result = true;
//						break;
//					}
//				}
//			}
			//System.out.println(ajusteIvaConceptList.get(concept.trim().toUpperCase()));
			result = ajusteIvaConceptList.containsKey(concept.trim().toUpperCase());
		}
		return result;
	}
	
	
	private void loadAjusteIvaConceptList() throws Exception {
		FileInputStream fis = new FileInputStream(PathECBCatalogos + ajusteIvaConceptsFileName);
		DataInputStream dis = new DataInputStream(fis);
		BufferedReader bfr = new BufferedReader(new InputStreamReader(dis, "UTF-8"));
		String conceptLine = null;
		//ajusteIvaConceptList = new ArrayList<String[]>();
		ajusteIvaConceptList = new HashMap<String, String>();

		while ((conceptLine = bfr.readLine()) != null) {
			if(conceptLine.trim() != ""){
				//System.out.println(conceptLine);
				String[] conceptArray = conceptLine.replace("\uFEFF", "").split("\\|");
				//System.out.println("antes agrega: " + conceptArray[4].trim().toUpperCase());
				//System.out.println(conceptArray[0]+ " - "+(conceptArray[0].equalsIgnoreCase("002") && !conceptArray[1].trim().equalsIgnoreCase("Exento")));
				if(conceptArray[0].equalsIgnoreCase("002") && !conceptArray[1].trim().equalsIgnoreCase("Exento")){
					//ajusteIvaConceptList.add(conceptArray);
					ajusteIvaConceptList.put(conceptArray[4].trim().toUpperCase(), conceptArray[4].trim().toUpperCase());
					//System.out.println("despues agrega: "+ajusteIvaConceptList.get(conceptArray[4].trim().toUpperCase()));
				}
			}
		}
		bfr.close();
	}
}
