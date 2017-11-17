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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

	StringBuilder fileBlockOne;
	StringBuilder fileBlockTwo;

	StringBuilder lineSixSb;

	List<String[]> pampasConceptList = null;

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
				lineSixSb = new StringBuilder();

				boolean firstLoop = true;
				BigInteger ecbCount = BigInteger.ZERO;
				BigInteger ecbWritten = BigInteger.ZERO;
				BigInteger ecbOmitted = BigInteger.ZERO;
				
				while ((strLine = br.readLine()) != null) {
					strLine = strLine.trim();
					if (!strLine.equals("")) {
						String[] arrayValues = strLine.split("\\|");
						int lineNum = Integer.parseInt(arrayValues[0]);

						if (lineNum == 1) {// linea 1

							if (!firstLoop) {
								if (!lineSixSb.toString().isEmpty()) {
									fileWriter.write(
											fileBlockOne.toString() + lineSixSb.toString() + fileBlockTwo.toString());
									ecbOmitted = ecbOmitted.add(BigInteger.ONE);
								}
								ecbWritten = ecbWritten.add(BigInteger.ONE);
								resetECB();
							}
							ecbCount = ecbCount.add(BigInteger.ONE);
							fileBlockOne.append(strLine + "\n");

						} else if (lineNum > 1 && lineNum < 6) {// lineas 2 a 5
							fileBlockOne.append(strLine + "\n");
						} else if (lineNum == 6) {// linea 6
							// quitar los conceptos "-" del catalogo
							if (!removeIsNeeded(arrayValues[1])) {
								lineSixSb.append(strLine + "\n");
							}

						} else if (lineNum > 6) {// lineas 7 a 11
							fileBlockTwo.append(strLine + "\n");
						}
					}
					firstLoop = false;
				}
				if (ecbWritten.compareTo(ecbCount) != 0) {
					System.out.println("Escribiendo ultimo ECB - Formatea PAMPA");
					if (!lineSixSb.toString().isEmpty()) {
						fileWriter.write(fileBlockOne.toString() + lineSixSb.toString() + fileBlockTwo.toString());
						ecbOmitted = ecbOmitted.add(BigInteger.ONE);
					}
					ecbWritten = ecbWritten.add(BigInteger.ONE);
					resetECB();
				}

				fileWriter.close();
				br.close();
				File movedFile = new File(PathECBSalida + fileName + "ORIGINAL_PAMPA_" + timeStamp + filesExtension);
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
		lineSixSb = new StringBuilder();
	}

	private void loadPampasConceptList() throws Exception {
		FileInputStream fis = new FileInputStream(PathECBCatalogos + pampasConceptCatalog);
		DataInputStream dis = new DataInputStream(fis);
		BufferedReader bfr = new BufferedReader(new InputStreamReader(dis, "UTF-8"));
		String conceptLine = null;
		pampasConceptList = new ArrayList<String[]>();

		while ((conceptLine = bfr.readLine()) != null) {
			String[] conceptArray = conceptLine.split("\\|");
			pampasConceptList.add(conceptArray);
		}
		bfr.close();
	}

	private boolean removeIsNeeded(String concept) {
		boolean result = false;
		if (pampasConceptList != null) {
			for (String[] row : pampasConceptList) {
				if (row.length == 2) {
					if (row[0].equals("-") && row[1].equalsIgnoreCase(concept)) {
						result = true;
						break;
					}
				}
			}
		}
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

}
