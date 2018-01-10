package util.ecb.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class FormateaECBPampaController {

	public static String PathECBEntrada = "/home/linuxlite/shell_scripts/ECBIVA/interfaces/";
	public static String PathECBSalida = "/home/linuxlite/shell_scripts/ECBIVA/CFDProcesados/";
	public static String PathECBCatalogos = "/home/linuxlite/shell_scripts/ECBIVA/interfaces/";

	// public static String PathECBEntrada = "C:\\Users\\ase\\Desktop\\ECB
	// batch\\ejemplosdearchivosdeentradaedc\\interfaces\\";
	// public static String PathECBSalida = "C:\\Users\\ase\\Desktop\\ECB
	// batch\\ejemplosdearchivosdeentradaedc\\CFDProcesados\\";
	// public static String PathECBCatalogos = "C:\\Users\\ase\\Desktop\\ECB
	// batch\\ejemplosdearchivosdeentradaedc\\Catalogos\\";

	public static String pampasConceptCatalog = "pampaConceptos.TXT";
	public static String filesExtension = ".TXT";

	BigDecimal subTotalOriginal;
	BigDecimal newSubTotalAllConcepts;
	BigDecimal newSubTotal;
	
	BigDecimal newTotal;

	BigDecimal ivaOriginal;
	BigDecimal newIva;

	BigDecimal tasa;

	StringBuilder fileBlockOne;
	StringBuilder fileBlockTwo;

	StringBuilder lineElevenSb;

	String firstLine = null;
	String lineTwo = null;
	String lineSeven = null;
	String lineEigth = null;
	String lineNine = null;
	String lineTen = null;
	String lineEleven = null;

	Map<String, String> pampasConceptList = null;

	public FormateaECBPampaController() {

	}

	public boolean processECBTxtFile(String fileName, String timeStamp) {
		System.out.println("Inicia Formatea PAMPA - " + fileName);
		boolean result = true;
		try {
			FileInputStream fileToProcess = null;
			DataInputStream in = null;
			BufferedReader br = null;

			FileOutputStream fos = null;
			OutputStreamWriter osw = null;
			Writer fileWriter = null;

			File outputFile;

			File inputFile = new File(PathECBEntrada + fileName + filesExtension);
			if (inputFile.exists()) {
				fileToProcess = new FileInputStream(PathECBEntrada + fileName + filesExtension);
				in = new DataInputStream(fileToProcess);
				br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
				String strLine;

				loadPampasConceptList();

				outputFile = new File(PathECBEntrada + "GENERATED_" + fileName + filesExtension);

				fos = new FileOutputStream(outputFile);
				osw = new OutputStreamWriter(fos, "UTF-8");
				fileWriter = new BufferedWriter(osw);

				fileBlockOne = new StringBuilder();
				fileBlockTwo = new StringBuilder();
				lineElevenSb = new StringBuilder();

				ivaOriginal = BigDecimal.ZERO;
				newIva = BigDecimal.ZERO;
				
				newSubTotal = BigDecimal.ZERO;
				newSubTotalAllConcepts = BigDecimal.ZERO;
				subTotalOriginal = BigDecimal.ZERO;
				newTotal = BigDecimal.ZERO;
				tasa = BigDecimal.ZERO;

				firstLine = "";
				lineTwo = "";
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
								String ecbBakup = firstLine + "\n" + lineTwo + "\n" + fileBlockOne.toString() + lineSeven
										+ "\n" + (lineEigth.isEmpty() ? "" : lineEigth + "\n")
										+ (lineNine.isEmpty() ? "" : lineNine + "\n") + lineTen + "\n"
										+ lineElevenSb.toString();
								try{
									firstLine = FormateaECBIvaController.truncateExcangeFromFirstLine(firstLine);
								}catch(Exception e){
									ecbError.append("-error:Error al convertir tipo de cambio a dos decimales\n");
								}
								
								if (ecbError.toString().isEmpty()) {
									if (tasa.compareTo(BigDecimal.ZERO) != 0) {
										try {
											// calcula iva
											newIva = newSubTotal.multiply(tasa).divide(new BigDecimal(100));
											newIva = newIva.setScale(2, BigDecimal.ROUND_HALF_EVEN);

											// generar linea 1
											firstLine = replaceTotalsFromFirstLine(firstLine, newSubTotalAllConcepts, newIva);
											// generar linea 2
											lineTwo = replaceTotalsFromLineTwo(lineTwo, newSubTotalAllConcepts, newIva);
											// generar linea 7
											lineSeven = replaceIvaFromLineSeven(lineSeven, newIva);
											// generar linea 9
											if (!lineNine.isEmpty()) {
												lineNine = replaceIvaFromLineNine(lineNine, newIva);
											}

										} catch (Exception e) {
											System.out.println(ecbCount.toString() + "---Excepcion al hacer calculos en ECB numero de cuenta: "
													+ numCta);
											e.printStackTrace();
											exception = true;
										}
									}
								} else {
									System.out.println(ecbCount.toString() + "---Errores en ECB numero de cuenta: " + numCta);
									System.out.println(ecbError.toString());
								}
								
								if(!exception){
									fileWriter.write(firstLine + "\n" + lineTwo + "\n" + fileBlockOne.toString() + lineSeven
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
								ivaOriginal = new BigDecimal(arrayValues[6].trim());
							} catch (Exception e) {
								ecbError.append("-error: no se pudo leer el iva informado en linea 1\n");
							}
							try{
								numCta = arrayValues[2].trim();
							}catch(Exception e){
								numCta = "NumeroDefault";
								ecbError.append("-error: no se pudo leer el numero de cuenta\n");
							}

						} else if (lineNum == 2) {//linea 2
							lineTwo = strLine;
							try {
								subTotalOriginal = new BigDecimal(arrayValues[6].trim());
							} catch (Exception e) {
								ecbError.append("-error: no se pudo leer el subtotal\n");
							}
						} else if (lineNum > 2 && lineNum < 6) {// lineas 3 a 5
							fileBlockOne.append(strLine + "\n");
						} else if (lineNum == 6) {// linea 6
							
							BigDecimal importeActual = new BigDecimal(0);
							try {
								importeActual = new BigDecimal(arrayValues[2].trim());
							} catch (Exception e) {
								ecbError.append("-error: no se pudo leer el importe de concepto\n");
							}
							if(conceptAplicaIva(arrayValues[1].trim())){//si el concepto aplica iva
								newSubTotal = newSubTotal.add(importeActual);
							}
							newSubTotalAllConcepts = newSubTotalAllConcepts.add(importeActual);
							fileBlockOne.append(strLine + "\n");
							
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
				if (ecbWritten.compareTo(ecbCount) != 0) {
					System.out.println("Escribiendo ultimo ECB - Formatea PAMPA");
//					
					boolean exception = false;
					String ecbBakup = firstLine + "\n" + lineTwo + "\n" + fileBlockOne.toString() + lineSeven
							+ "\n" + (lineEigth.isEmpty() ? "" : lineEigth + "\n")
							+ (lineNine.isEmpty() ? "" : lineNine + "\n") + lineTen + "\n"
							+ lineElevenSb.toString();
					try{
						firstLine = FormateaECBIvaController.truncateExcangeFromFirstLine(firstLine);
					}catch(Exception e){
						ecbError.append("-error:Error al convertir tipo de cambio a dos decimales\n");
					}
					
					if (ecbError.toString().isEmpty()) {
						if (tasa.compareTo(BigDecimal.ZERO) != 0) {
							try {
								// calcula iva
								newIva = newSubTotal.multiply(tasa).divide(new BigDecimal(100));
								newIva = newIva.setScale(2, BigDecimal.ROUND_HALF_EVEN);

								// generar linea 1
								firstLine = replaceTotalsFromFirstLine(firstLine, newSubTotalAllConcepts, newIva);
								// generar linea 2
								lineTwo = replaceTotalsFromLineTwo(lineTwo, newSubTotalAllConcepts, newIva);
								// generar linea 7
								lineSeven = replaceIvaFromLineSeven(lineSeven, newIva);
								// generar linea 9
								if (!lineNine.isEmpty()) {
									lineNine = replaceIvaFromLineNine(lineNine, newIva);
								}

							} catch (Exception e) {
								System.out.println(ecbCount.toString() + "---Excepcion al hacer calculos en ECB numero de cuenta: "
										+ numCta);
								e.printStackTrace();
								exception = true;
							}
						}
					} else {
						System.out.println(ecbCount.toString() + "---Errores en ECB numero de cuenta: " + numCta);
						System.out.println(ecbError.toString());
					}
					
					if(!exception){
						fileWriter.write(firstLine + "\n" + lineTwo + "\n" + fileBlockOne.toString() + lineSeven
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
				br.close();
				File movedFile = new File(PathECBSalida + fileName + "ORIGINAL_" + timeStamp + filesExtension);
				if (moveFile(inputFile, movedFile)) {// mover archivo original
					// renombrar archivo generado
					if (moveFile(outputFile, new File(PathECBEntrada + fileName + filesExtension))) {
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
			System.out.println("Exception formateaECBPampa:" + e.getMessage());
			return false;
		}
	}

	private void resetECB() {
		fileBlockOne = new StringBuilder();
		fileBlockTwo = new StringBuilder();

		newSubTotal = BigDecimal.ZERO;
		newSubTotalAllConcepts = BigDecimal.ZERO;
		subTotalOriginal = BigDecimal.ZERO;
		
		newTotal = BigDecimal.ZERO;

		ivaOriginal = BigDecimal.ZERO;
		newIva = BigDecimal.ZERO;

		tasa = BigDecimal.ZERO;

		lineElevenSb = new StringBuilder();

		firstLine = "";
		lineTwo = "";
		lineSeven = "";
		lineEigth = "";
		lineNine = "";
		lineTen = "";
		lineEleven = "";
	}

	private void loadPampasConceptList() throws Exception {
		FileInputStream fis = new FileInputStream(PathECBCatalogos + pampasConceptCatalog);
		DataInputStream dis = new DataInputStream(fis);
		BufferedReader bfr = new BufferedReader(new InputStreamReader(dis, "UTF-8"));
		String conceptLine = null;
		pampasConceptList = new HashMap<String, String>();

		while ((conceptLine = bfr.readLine()) != null) {
			if(conceptLine.trim() != ""){
				String[] conceptArray = conceptLine.replace("\uFEFF", "").split("\\|");
				pampasConceptList.put(conceptArray[1].trim().toUpperCase(), conceptArray[0].trim().toUpperCase());
				//System.out.println(pampasConceptList.containsKey(conceptArray[1].trim().toUpperCase()) + " - " + conceptArray[1].trim().toUpperCase());
			}
		}
		bfr.close();
	}

	private boolean conceptAplicaIva(String concept) {
		boolean result = false;
		if (pampasConceptList != null) {
			boolean hasConcept = pampasConceptList.containsKey(concept.trim().toUpperCase());
			if(hasConcept){
				String aplicaIva = (String)pampasConceptList.get(concept.trim().toUpperCase());
				if(aplicaIva.equalsIgnoreCase("+")){
					result = true;
				}
			}
		}
		//System.out.println("concepto: " + concept.trim().toUpperCase() + " "+ (String)pampasConceptList.get(concept.trim().toUpperCase()) + " aplicaIva: " + result);
		return result;
	}

	public static boolean moveFile(File afile, File bfile) {
		InputStream inStream = null;
		OutputStream outStream = null;

		try {

			inStream = new FileInputStream(afile);
			outStream = new FileOutputStream(bfile);

			byte[] buffer = new byte[1024];

			int length;
			// copy the file content in bytes
			while ((length = inStream.read(buffer)) > 0) {

				outStream.write(buffer, 0, length);

			}

			inStream.close();
			outStream.close();

			// delete the original file
			afile.delete();

			//System.out.println("Archivo movido con exito");
			return true;

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private String replaceTotalsFromFirstLine(String originalLine, BigDecimal newSubTotalValue,
			BigDecimal newIvaValue) {
		
		StringBuilder controlLineSb = new StringBuilder();
		String[] originalLineArray = originalLine.split("\\|");
		
		newIvaValue = newIvaValue.setScale(2, BigDecimal.ROUND_HALF_EVEN);
		newSubTotalValue = newSubTotalValue.setScale(2, BigDecimal.ROUND_HALF_EVEN);
		
		BigDecimal newTotal = newSubTotalValue.add(newIvaValue);
		newTotal = newTotal.setScale(2, BigDecimal.ROUND_HALF_EVEN);

		for (int i = 0; i < originalLineArray.length; i++) {
			if (i == 5) {
				controlLineSb.append(newTotal.toString() + "|");
			} else if (i == 6) {
				controlLineSb.append(newIvaValue.toString() + "|");
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
	
	private String replaceTotalsFromLineTwo(String originalLine, BigDecimal newSubTotalValue, BigDecimal newIvaValue) {
		StringBuilder controlLineSb = new StringBuilder();
		String[] originalLineArray = originalLine.split("\\|");
		newIvaValue = newIvaValue.setScale(2, BigDecimal.ROUND_HALF_EVEN);
		newSubTotalValue = newSubTotalValue.setScale(2, BigDecimal.ROUND_HALF_EVEN);

		BigDecimal newTotal = newSubTotalValue.add(newIvaValue);
		newTotal = newTotal.setScale(2, BigDecimal.ROUND_HALF_EVEN);

		for (int i = 0; i < originalLineArray.length; i++) {
			if (i == 6) {
				controlLineSb.append(newSubTotalValue.toString() + "|");
			} else if (i == 7) {
				controlLineSb.append(newTotal.toString() + "|");
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
	
	private String replaceIvaFromLineSeven(String originalLine, BigDecimal newIvaValue) {
		StringBuilder controlLineSb = new StringBuilder();
		String[] originalLineArray = originalLine.split("\\|");
		newIvaValue = newIvaValue.setScale(2, BigDecimal.ROUND_HALF_EVEN);

		for (int i = 0; i < originalLineArray.length; i++) {
			if (i == 2) {
				controlLineSb.append(newIvaValue.toString() + "|");
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
	
	private String replaceIvaFromLineNine(String originalLine, BigDecimal newIvaValue) {
		StringBuilder controlLineSb = new StringBuilder();
		String[] originalLineArray = originalLine.split("\\|");
		newIvaValue = newIvaValue.setScale(2, BigDecimal.ROUND_HALF_EVEN);

		for (int i = 0; i < originalLineArray.length; i++) {
			if (i == 3) {
				controlLineSb.append(newIvaValue.toString() + "|");
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
