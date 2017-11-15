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

public class FormateaECBIvaController {

	public static String PathECBEntrada = "/home/linuxlite/shell_scripts/ECBIVA/interfaces/";
	public static String PathECBSalida = "/home/linuxlite/shell_scripts/ECBIVA/CFDProcesados/";
	public static String PathECBCatalogos = "/home/linuxlite/shell_scripts/ECBIVA/interfaces/";

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
	String lineTwo = null;
	String lineSeven = null;
	String lineEigth = null;
	String lineNine = null;
	String lineTen = null;
	String lineEleven = null;

	String documentType = null;

	public FormateaECBIvaController() {

	}

	public boolean processECBTxtFile(String fileName, String timeStamp) {
		System.out.println("Inicia Formatea IVA - " + fileName);
		boolean result = true;
		try {
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

				outputFile = new File(PathECBEntrada + "GENERATED_" + fileName + filesExtension);
				outputControlFile = new File(PathECBSalida + fileName + "_CONTROL_" + timeStamp + filesExtension);

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
									firstLine = truncateExcangeFromFirstLine(firstLine);
								}catch(Exception e){
									ecbError.append("-error:Error al convertir tipo de cambio a dos decimales\n");
								}
								
								if (ecbError.toString().isEmpty()) {
									if (tasa.compareTo(BigDecimal.ZERO) != 0) {
										try {
											// calcula iva
											newIvaMn = newTotalMn.multiply(tasa).divide(new BigDecimal(100));
											newIvaMn = newIvaMn.setScale(2, BigDecimal.ROUND_HALF_EVEN);

											if (ivaMnOriginal.compareTo(newIvaMn) != 0) {
												String[] lineOne = firstLine.split("\\|");
												// guardar NumTarjeta, TotalMn e
												// ivaMn en control file
												String controlLine = generateControlLine(numCta, lineOne[4], lineOne[5],
														newTotalMn, lineOne[6], newIvaMn);
												fileWriterControl.write(controlLine);
												// generar linea 1
												firstLine = replaceTotalsFromFirstLine(firstLine, newTotalMn, newIvaMn);
												// generar linea 2
												lineTwo = replaceTotalsFromLineTwo(lineTwo, newTotalMn, newIvaMn);
												// generar linea 7
												lineSeven = replaceIvaFromLineSeven(lineSeven, newIvaMn);
												// generar linea 9
												if (!lineNine.isEmpty()) {
													lineNine = replaceIvaFromLineNine(lineNine, newIvaMn);
												}
											}
										} catch (Exception e) {
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
								totalMnOriginal = new BigDecimal(arrayValues[5].trim());
							} catch (Exception e) {
								ecbError.append("-error: no se pudo leer el subtotal\n");
							}
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

						} else if (lineNum == 2) {
							lineTwo = strLine;
							// documentType = arrayValues[1];
						} else if (lineNum > 2 && lineNum < 6) {// lineas 3 a 5
							fileBlockOne.append(strLine + "\n");
						} else if (lineNum == 6) {// linea 6
							BigDecimal importeActual = new BigDecimal(0);
							try {
								importeActual = new BigDecimal(arrayValues[2].trim());
							} catch (Exception e) {
								ecbError.append("-error: no se pudo leer el importe de concepto\n");
							}
							newTotalMn = newTotalMn.add(importeActual);
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
				if (ecbWritten.compareTo(ecbCount) != 0) {// escribir ultimo ecb
					System.out.println("Escribiendo ultimo ECB - Formatea IVA");

					boolean exception = false;
					String ecbBakup = firstLine + "\n" + lineTwo + "\n" + fileBlockOne.toString() + lineSeven
							+ "\n" + (lineEigth.isEmpty() ? "" : lineEigth + "\n")
							+ (lineNine.isEmpty() ? "" : lineNine + "\n") + lineTen + "\n"
							+ lineElevenSb.toString();
					try{
						firstLine = truncateExcangeFromFirstLine(firstLine);
					}catch(Exception e){
						ecbError.append("-error:Error al convertir tipo de cambio a dos decimales\n");
					}
					
					if (ecbError.toString().isEmpty()) {
						if (tasa.compareTo(BigDecimal.ZERO) != 0) {
							try {
								// calcula iva
								newIvaMn = newTotalMn.multiply(tasa).divide(new BigDecimal(100));
								newIvaMn = newIvaMn.setScale(2, BigDecimal.ROUND_HALF_EVEN);

								if (ivaMnOriginal.compareTo(newIvaMn) != 0) {
									String[] lineOne = firstLine.split("\\|");
									// guardar NumTarjeta, TotalMn e
									// ivaMn en control file
									String controlLine = generateControlLine(numCta, lineOne[4], lineOne[5],
											newTotalMn, lineOne[6], newIvaMn);
									fileWriterControl.write(controlLine);
									// generar linea 1
									firstLine = replaceTotalsFromFirstLine(firstLine, newTotalMn, newIvaMn);
									// generar linea 2
									lineTwo = replaceTotalsFromLineTwo(lineTwo, newTotalMn, newIvaMn);
									// generar linea 7
									lineSeven = replaceIvaFromLineSeven(lineSeven, newIvaMn);
									// generar linea 9
									if (!lineNine.isEmpty()) {
										lineNine = replaceIvaFromLineNine(lineNine, newIvaMn);
									}
								}
							} catch (Exception e) {
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
				fileWriterControl.close();
				br.close();
				File movedFile = new File(PathECBSalida + fileName + "ORIGINAL_IVA_" + timeStamp + filesExtension);
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

	private String replaceTotalsFromFirstLine(String originalLine, BigDecimal newTotalMnValue,
			BigDecimal newIvaMnValue) {
		StringBuilder controlLineSb = new StringBuilder();
		String[] originalLineArray = originalLine.split("\\|");
		newIvaMnValue = newIvaMnValue.setScale(2, BigDecimal.ROUND_HALF_EVEN);
		newTotalMnValue = newTotalMnValue.setScale(2, BigDecimal.ROUND_HALF_EVEN);

		for (int i = 0; i < originalLineArray.length; i++) {
			if (i == 5) {
				controlLineSb.append(newTotalMnValue.toString() + "|");
			} else if (i == 6) {
				controlLineSb.append(newIvaMnValue.toString() + "|");
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

	public static String truncateExcangeFromFirstLine(String originalLine) {
		StringBuilder controlLineSb = new StringBuilder();
		String[] originalLineArray = originalLine.split("\\|");

		for (int i = 0; i < originalLineArray.length; i++) {
			if (i == 9) {
				if (originalLineArray[i] != null && !originalLineArray[i].trim().isEmpty()) {
					String tipoCambioS = originalLineArray[i].trim();
					DecimalFormat df = new DecimalFormat("0.00");
					BigDecimal tipoCambio = new BigDecimal(tipoCambioS);
					tipoCambioS = df.format(tipoCambio);
					controlLineSb.append(tipoCambioS + "|");
				}
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

	private String replaceTotalsFromLineTwo(String originalLine, BigDecimal newTotalMnValue, BigDecimal newIvaMnValue) {
		StringBuilder controlLineSb = new StringBuilder();
		String[] originalLineArray = originalLine.split("\\|");
		newIvaMnValue = newIvaMnValue.setScale(2, BigDecimal.ROUND_HALF_EVEN);
		newTotalMnValue = newTotalMnValue.setScale(2, BigDecimal.ROUND_HALF_EVEN);

		BigDecimal newTotal = newTotalMnValue.add(newIvaMnValue);
		newTotal = newTotal.setScale(2, BigDecimal.ROUND_HALF_EVEN);

		for (int i = 0; i < originalLineArray.length; i++) {
			if (i == 6) {
				controlLineSb.append(newTotalMnValue.toString() + "|");
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

	private String replaceIvaFromLineSeven(String originalLine, BigDecimal newIvaMnValue) {
		StringBuilder controlLineSb = new StringBuilder();
		String[] originalLineArray = originalLine.split("\\|");
		newIvaMnValue = newIvaMnValue.setScale(2, BigDecimal.ROUND_HALF_EVEN);

		for (int i = 0; i < originalLineArray.length; i++) {
			if (i == 2) {
				controlLineSb.append(newIvaMnValue.toString() + "|");
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

	private String replaceIvaFromLineNine(String originalLine, BigDecimal newIvaMnValue) {
		StringBuilder controlLineSb = new StringBuilder();
		String[] originalLineArray = originalLine.split("\\|");
		newIvaMnValue = newIvaMnValue.setScale(2, BigDecimal.ROUND_HALF_EVEN);

		for (int i = 0; i < originalLineArray.length; i++) {
			if (i == 3) {
				controlLineSb.append(newIvaMnValue.toString() + "|");
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

	private String generateControlLine(String NumCuenta, String NumTarjeta, String totalMnOriginalVal, BigDecimal newTotalMnVal,
			String ivaMnOriginalVal, BigDecimal newIvaMnVal) {

		StringBuilder controlLineSb = new StringBuilder();

		newTotalMnVal = newTotalMnVal.setScale(2, BigDecimal.ROUND_HALF_EVEN);
		newIvaMnVal = newIvaMnVal.setScale(2, BigDecimal.ROUND_HALF_EVEN);

		controlLineSb.append(NumCuenta + "|");
		controlLineSb.append(NumTarjeta + "|");
		controlLineSb.append(totalMnOriginalVal + "|");
		controlLineSb.append(newTotalMnVal.toString() + "|");
		controlLineSb.append(ivaMnOriginalVal + "|");
		controlLineSb.append(newIvaMnVal.toString() + "\n");

		return controlLineSb.toString();
	}

	private void resetECB() {
		fileBlockOne = new StringBuilder();
		fileBlockTwo = new StringBuilder();

		newTotalMn = BigDecimal.ZERO;
		totalMnOriginal = BigDecimal.ZERO;

		ivaMnOriginal = BigDecimal.ZERO;
		newIvaMn = BigDecimal.ZERO;

		tasa = BigDecimal.ZERO;

		lineElevenSb = new StringBuilder();

		documentType = null;

		firstLine = "";
		lineTwo = "";
		lineSeven = "";
		lineEigth = "";
		lineNine = "";
		lineTen = "";
		lineEleven = "";
	}
}
