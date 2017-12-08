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
import java.util.List;

public class FormateaECBAjusteIvaController {

	public static String PathECBEntrada = "/home/linuxlite/shell_scripts/ECBIVA/interfaces/";
	public static String PathECBSalida = "/home/linuxlite/shell_scripts/ECBIVA/CFDProcesados/";
	public static String PathECBCatalogos = "/home/linuxlite/shell_scripts/ECBIVA/interfaces/";

	public static String ajusteIvaConceptsFileName = "ajusteIvaConceptos.TXT";
	List<String[]> ajusteIvaConceptList = null;
	
	
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
		List<String[]> sixList = new ArrayList<String[]>();
		String lastChar = "";
		boolean entraAjuste = false;
		if(sixArray.length > 1){
			
			lastChar = sixArray[0].substring(sixArray[0].length() - 1);
			if (!lastChar.equals("|")) {
				lastChar = "";
			}
			
			BigDecimal ajuste = new BigDecimal("0.00");
			BigDecimal totalIvaInicial = new BigDecimal("0.00");
			//for(String line : sixArray){
			for(int i = 0; i < sixArray.length; i++){
				String line = sixArray[i];
				String[] lineArray = line.split("\\|");
				if(conceptRequiresIva(lineArray[1].trim()) 
						&& !(isCarter && lineArray[1].trim().toUpperCase().contains("EXENTO"))){
					//System.out.println("Concepto incluido en calculo: " + line);
					BigDecimal importe = new BigDecimal(lineArray[2].trim());
					BigDecimal iva = (importe.multiply(tasa)).divide(new BigDecimal(100));
					iva = iva.setScale(2, BigDecimal.ROUND_HALF_EVEN);
					totalIvaInicial = totalIvaInicial.add(iva);
				}
				sixList.add(lineArray);
			}
			
			System.out.println("iva original: " + ivaMnOriginal.toString());
			System.out.println("iva inicial: " + totalIvaInicial.toString());
			
			if(totalIvaInicial.compareTo(ivaMnOriginal) > 0){
				ajuste = totalIvaInicial.subtract(ivaMnOriginal);
			}else{
				ajuste = ivaMnOriginal.subtract(totalIvaInicial);
			}
			System.out.println("valor ajuste: " + ajuste.toString());
			BigDecimal totalIva = BigDecimal.ZERO;
			
			int loops = 1;
			do{
				
				totalIva = BigDecimal.ZERO;
				for(String[] lineArray : sixList){
					//System.out.println("Concepto: "+ lineArray[1].trim());
					if(conceptRequiresIva(lineArray[1].trim()) 
							&& !(isCarter && lineArray[1].trim().toUpperCase().contains("EXENTO"))){
						//System.out.println("-Concepto tomado en cuenta-");
						BigDecimal importe = new BigDecimal(lineArray[2].trim());
						BigDecimal iva = (importe.multiply(tasa)).divide(new BigDecimal(100));
						iva = iva.setScale(2, BigDecimal.ROUND_HALF_EVEN);
						totalIva = totalIva.add(iva);
					}
				}
				totalIva = totalIva.setScale(2, BigDecimal.ROUND_HALF_EVEN);
				//System.out.println("Iva actual calculado: " + totalIva.toString());
				//System.out.println("Iva informado: " + ivaMnOriginal.toString());
				if(totalIva.compareTo(ivaMnOriginal) != 0){
					entraAjuste=true;
					
					sixList = new ArrayList<String[]>();
					for(int i = 0; i < sixArray.length; i++){
						String line = sixArray[i];
						String[] lineArray = line.split("\\|");
						sixList.add(lineArray);
					}
					
					//System.out.println("Increment concept val: " + sixList.get(0)[2]);
					//System.out.println("decrement concept val: " + sixList.get(loops)[2]);
					
					BigDecimal increment = new BigDecimal(sixList.get(0)[2]).add(ajuste)
							.setScale(2, BigDecimal.ROUND_HALF_EVEN);
					BigDecimal decrement = new BigDecimal(sixList.get(loops)[2]).subtract(ajuste)
							.setScale(2, BigDecimal.ROUND_HALF_EVEN);

					String[] incrementConcept = sixList.get(0);
					String[] decrementConcept = sixList.get(loops);
					
					incrementConcept[2] = increment.toString();
					decrementConcept[2] = decrement.toString();
					
					sixList.set(0, incrementConcept);
					sixList.set(loops, decrementConcept);
					
				}else{
					if(entraAjuste){
						newIvaMn = totalIva;
					}
					break;
				}
				loops++;
			} while(loops < sixList.size());
			System.out.println("loops totales: " + (loops-1));
		}else{
			result = lines;
		}
		
		if(newIvaMn.compareTo(BigDecimal.ZERO) > 0 && newIvaMn.compareTo(ivaMnOriginal) == 0){
			//generar nuevas lineas 6
			for(String[] line : sixList){
				result.append(line[0].trim());
				result.append("|");
				result.append(line[1].trim());
				result.append("|");
				result.append(line[2].trim());
				result.append(lastChar);
				result.append("\n");
			}
			
		}else{
			result = lines;
		}
		
		return result;
	}
	
	private boolean conceptRequiresIva(String concept) {
		boolean result = false;
		if (ajusteIvaConceptList != null) {
			for (String[] row : ajusteIvaConceptList) {
				if (row.length == 5) {
					if (row[0].equals("002") && !row[1].equalsIgnoreCase("Exento") && row[4].equalsIgnoreCase(concept)) {
						result = true;
						break;
					}
				}
			}
		}
		return result;
	}
	
	
	private void loadAjusteIvaConceptList() throws Exception {
		FileInputStream fis = new FileInputStream(PathECBCatalogos + ajusteIvaConceptsFileName);
		DataInputStream dis = new DataInputStream(fis);
		BufferedReader bfr = new BufferedReader(new InputStreamReader(dis, "UTF-8"));
		String conceptLine = null;
		ajusteIvaConceptList = new ArrayList<String[]>();

		while ((conceptLine = bfr.readLine()) != null) {
			String[] conceptArray = conceptLine.split("\\|");
			if(conceptArray[0].trim().equals("002") && !conceptArray[1].trim().equalsIgnoreCase("Exento")){
				ajusteIvaConceptList.add(conceptArray);
			}
		}
		bfr.close();
	}
}
