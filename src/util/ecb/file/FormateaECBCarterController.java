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

	BigDecimal totalMnOriginal;
	BigDecimal newTotalMn;
	
	BigDecimal totalConceptsA;
	BigDecimal ivaA;
	BigDecimal tasa;
	BigDecimal ivaMnOriginal;
	BigDecimal ivaB;
	BigDecimal montoConceptosGrav;

	StringBuilder fileBlockOne;
	StringBuilder fileBlockTwo;

	StringBuilder lineSixSb;

	String firstLine = null;
	List<String> lineSixList = null;

	List<String> carterConceptList = null;

	String documentType = null;

	public FormateaECBCarterController() {

	}

	public boolean processECBTxtFile(String fileName) {
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
				lineSixSb = new StringBuilder();

				newTotalMn = BigDecimal.ZERO;
				totalMnOriginal = BigDecimal.ZERO;
				
				totalConceptsA = BigDecimal.ZERO;
				ivaA = BigDecimal.ZERO;
				tasa = BigDecimal.ZERO;
				ivaMnOriginal = BigDecimal.ZERO;
				ivaB = BigDecimal.ZERO;
				montoConceptosGrav = BigDecimal.ZERO;
				

				firstLine = null;

				lineSixList = new ArrayList<String>();

				boolean firstLoop = true;
				int ecbCount = 0;
				int ecbWritten = 0;
				while ((strLine = br.readLine()) != null) {
					strLine = strLine.trim();
					
					if (!strLine.equals("")) {
						String[] arrayValues = strLine.split("\\|");
						int lineNum = Integer.parseInt(arrayValues[0]);

						if (lineNum == 1) {// linea 1
							ecbCount++;

							if (!firstLoop) {
								if(tasa.compareTo(BigDecimal.ZERO) != 0){
									//calcular iva conceptos fuera de la lista
									BigDecimal ivaPaso0 = (totalMnOriginal.multiply(tasa)).divide(new BigDecimal(100));
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
										
										if (totalMnOriginal.compareTo(newTotal) != 0) {
											//cambiar montos de conceptos informados
											lineSixSb = processSixLines(lineSixList, totalMnOriginal, montoConceptosGrav);
										}
									}
								}								

								fileWriter.write(firstLine + "\n" 
								+ fileBlockOne.toString() 
								+ lineSixSb.toString()
								+ fileBlockTwo.toString());
								ecbWritten++;

								resetECB();
							}

							firstLine = strLine;
							totalMnOriginal = new BigDecimal(arrayValues[5]);
							ivaMnOriginal = new BigDecimal(arrayValues[6]);

						} else if (lineNum > 1 && lineNum < 6) {// lineas 2 a 5
							if (lineNum == 2) {
								documentType = arrayValues[1];
							}
							fileBlockOne.append(strLine + "\n");
						} else if (lineNum == 6) {// linea 6
							lineSixSb.append(strLine + "\n");
							lineSixList.add(strLine);
							if(!listContains(carterConceptList, arrayValues[1].trim())){
								totalConceptsA = totalConceptsA.add(new BigDecimal(arrayValues[2].trim()));
							}
						} else if (lineNum > 6) {// lineas 7 a 11
							if(lineNum == 9){
								if (arrayValues[1].equalsIgnoreCase("IVA")) {
									tasa = new BigDecimal(arrayValues[2]);
								}
							}
							fileBlockTwo.append(strLine + "\n");
						}
					}
					firstLoop = false;
				}
				if (ecbWritten < ecbCount) {
					System.out.println("Escribiendo ultimo ECB");
					if(tasa.compareTo(BigDecimal.ZERO) != 0){
						//calcular iva conceptos fuera de la lista
						BigDecimal ivaPaso0 = (totalMnOriginal.multiply(tasa)).divide(new BigDecimal(100));
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
							
							if (totalMnOriginal.compareTo(newTotal) != 0) {
								//cambiar montos de conceptos informados
								lineSixSb = processSixLines(lineSixList, totalMnOriginal, montoConceptosGrav);
							}
						}
					}

					fileWriter.write(firstLine + "\n" 
					+ fileBlockOne.toString() 
					+ lineSixSb.toString()
					+ fileBlockTwo.toString());
					ecbWritten++;

					resetECB();
				}

				fileWriter.close();
				//fileWriterControl.close();
				br.close();
				
				String timeStamp = new SimpleDateFormat("HHmmss").format(Calendar.getInstance().getTime());
				File movedFile = new File(PathECBSalida + fileName + "ORIGINAL_CARTER_" + timeStamp + filesExtension);
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
				
				newLine = "06|"
						+ lineArray[1] + "|"
						+ nuevoImporte.toString() + "\n"
						+"06|"
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

		newTotalMn = BigDecimal.ZERO;
		totalMnOriginal = BigDecimal.ZERO;
		
		totalConceptsA = BigDecimal.ZERO;
		ivaA = BigDecimal.ZERO;
		tasa = BigDecimal.ZERO;
		ivaMnOriginal = BigDecimal.ZERO;
		ivaB = BigDecimal.ZERO;
		montoConceptosGrav = BigDecimal.ZERO;

		lineSixSb = new StringBuilder();
		lineSixList = new ArrayList<String>();

		documentType = null;
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
}
