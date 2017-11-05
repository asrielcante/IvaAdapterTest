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

	public boolean processECBTxtFile(String fileName) {
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
				outputControlFile = new File(PathECBSalida + fileName + "_CONTROL" + filesExtension);

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
				int ecbCount = 0;
				int ecbWritten = 0;
				while ((strLine = br.readLine()) != null) {

					if (!strLine.equals("")) {
						String[] arrayValues = strLine.split("\\|");
						int lineNum = Integer.parseInt(arrayValues[0]);

						if (lineNum == 1) {// linea 1
							ecbCount++;

							if (!firstLoop) {
								// calcula iva
								newIvaMn = newTotalMn.multiply(tasa).divide(new BigDecimal(100));
								newIvaMn = newIvaMn.setScale(2, BigDecimal.ROUND_HALF_EVEN);
								if (ivaMnOriginal.compareTo(newIvaMn) != 0) {
									String[] lineOne = firstLine.split("\\|");
									// guardar NumTarjeta, TotalMn e ivaMn en
									// control file
									String controlLine = generateControlLine(lineOne[4], lineOne[5], newTotalMn,
											lineOne[6], newIvaMn);
									fileWriterControl.write(controlLine);
									// generar linea 1
									firstLine = replaceTotalsFromFirstLine(firstLine, newTotalMn, newIvaMn);
									//generar linea 2
									lineTwo = replaceTotalsFromLineTwo(lineTwo, newTotalMn, newIvaMn);
									// generar linea 7
									lineSeven = replaceIvaFromLineSeven(lineSeven, newIvaMn);
									// generar linea 9
									if (!lineNine.isEmpty()) {
										lineNine = replaceIvaFromLineNine(lineNine, newIvaMn);
									}
								}

								fileWriter.write(firstLine + "\n" + lineTwo + "\n"
										+ fileBlockOne.toString() + lineSeven + "\n"
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

						}else if(lineNum == 2){
							lineTwo = strLine;
							documentType = arrayValues[1];
						} else if (lineNum > 2 && lineNum < 6) {// lineas 3 a 5
							fileBlockOne.append(strLine + "\n");
						} else if (lineNum == 6) {// linea 6
							newTotalMn = newTotalMn.add(new BigDecimal(arrayValues[2]));
							fileBlockOne.append(strLine + "\n");
						} else if (lineNum == 7) {// linea 7
							lineSeven = strLine;
						} else if (lineNum == 8) {// linea 8
							lineEigth = strLine;
						} else if (lineNum == 9) {// linea 9
							lineNine = strLine;
							if (arrayValues[1].equalsIgnoreCase("IVA")) {
								tasa = new BigDecimal(arrayValues[2]);
							}
						} else if (lineNum == 10) {// linea 10
							if(lineNine.isEmpty()){//si no hay linea 9 cerrar streams y lanzar excepcion
								fileWriter.close();
								fileWriterControl.close();
								br.close();
								throw new Exception("Error Formatea IVA - No se encontro fila 9 - ECB " + ecbCount);
							}
							lineTen = strLine;
						} else if (lineNum == 11) {// linea 11
							lineElevenSb.append(strLine + "\n");
						}
					}
					firstLoop = false;
				}
				if (ecbWritten < ecbCount) {// escribir ultimo ecb
					System.out.println("Escribiendo ultimo ECB - Formatea IVA");
					// calcula iva
					newIvaMn = newTotalMn.multiply(tasa).divide(new BigDecimal(100));
					newIvaMn = newIvaMn.setScale(2, BigDecimal.ROUND_HALF_EVEN);
					if (ivaMnOriginal.compareTo(newIvaMn) != 0) {
						String[] lineOne = firstLine.split("\\|");
						// guardar NumTarjeta, TotalMn e ivaMn en control file
						String controlLine = generateControlLine(lineOne[4], lineOne[5], newTotalMn, lineOne[6],
								newIvaMn);
						fileWriterControl.write(controlLine);
						// generar linea 1
						firstLine = replaceTotalsFromFirstLine(firstLine, newTotalMn, newIvaMn);
						//generar linea 2
						lineTwo = replaceTotalsFromLineTwo(lineTwo, newTotalMn, newIvaMn);
						// generar linea 7
						lineSeven = replaceIvaFromLineSeven(lineSeven, newIvaMn);
						// generar linea 9
						if (!lineNine.isEmpty()) {
							lineNine = replaceIvaFromLineNine(lineNine, newIvaMn);
						}
					}

					fileWriter.write(firstLine + "\n" + lineTwo + "\n"
							+ fileBlockOne.toString() + lineSeven + "\n"
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
				String timeStamp = new SimpleDateFormat("HHmmss").format(Calendar.getInstance().getTime());
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
			System.out.println("Exception formateaECBIva: " + e.getMessage());
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
		// controlLineSb.setLength(controlLineSb.length() - 1);//remove last pipe
		return controlLineSb.toString();
	}
	private String replaceTotalsFromLineTwo(String originalLine, BigDecimal newTotalMnValue,
			BigDecimal newIvaMnValue) {
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
		// controlLineSb.setLength(controlLineSb.length() - 1);//remove last pipe
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
		// controlLineSb.setLength(controlLineSb.length() - 1);//remove last pipe
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
		// controlLineSb.setLength(controlLineSb.length() - 1);//remove last pipe
		return controlLineSb.toString();
	}

	private String generateControlLine(String NumTarjeta, String totalMnOriginalVal, BigDecimal newTotalMnVal,
			String ivaMnOriginalVal, BigDecimal newIvaMnVal) {

		StringBuilder controlLineSb = new StringBuilder();

		newTotalMnVal = newTotalMnVal.setScale(2, BigDecimal.ROUND_HALF_EVEN);
		newIvaMnVal = newIvaMnVal.setScale(2, BigDecimal.ROUND_HALF_EVEN);

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
