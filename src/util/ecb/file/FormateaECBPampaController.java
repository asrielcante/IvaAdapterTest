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

public class FormateaECBPampaController {
	
	public static String PathECBEntrada = "/home/linuxlite/shell_scripts/ECBIVA/interfaces/";
	public static String PathECBSalida = "/home/linuxlite/shell_scripts/ECBIVA/CFDProcesados/";
	public static String PathECBProceso = "/home/linuxlite/shell_scripts/ECBIVA/Proceso/";
	public static String PathECBCatalogos = "/home/linuxlite/shell_scripts/ECBIVA/Catalogos/";
	
//	public static String PathECBEntrada = "C:\\Users\\ase\\Desktop\\ECB batch\\ejemplosdearchivosdeentradaedc\\interfaces\\";
//	public static String PathECBSalida = "C:\\Users\\ase\\Desktop\\ECB batch\\ejemplosdearchivosdeentradaedc\\CFDProcesados\\";
//	public static String PathECBProceso = "C:\\Users\\ase\\Desktop\\ECB batch\\ejemplosdearchivosdeentradaedc\\Proceso\\";
//	public static String PathECBCatalogos = "C:\\Users\\ase\\Desktop\\ECB batch\\ejemplosdearchivosdeentradaedc\\Catalogos\\";
	
	public static String pampasConceptCatalog = "pampaConceptos.TXT";
	public static String filesExtension = ".TXT";

	StringBuilder fileBlockOne;
    StringBuilder fileBlockTwo;
    
    StringBuilder lineSixSb;
	
	List<String[]> pampasConceptList = null;
	
	String documentType = null;
	
	public FormateaECBPampaController(){
		
	}
	
	public boolean processECBTxtFile(String fileName){
		boolean result = true;
		try{
			FileInputStream fileToProcess = null;
			DataInputStream in = null;
			BufferedReader br = null;
			
			FileOutputStream fos = null;
			OutputStreamWriter osw = null;
			Writer fileWriter = null;
			
			File outputFile;

				File inputFile = new File(PathECBEntrada + fileName + filesExtension);
				if(inputFile.exists()){
					fileToProcess = new FileInputStream(PathECBEntrada + fileName + filesExtension);
					in = new DataInputStream(fileToProcess);
					br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
					String strLine;
					
					loadPampasConceptList();
					
					outputFile = new File(PathECBEntrada + "GENERATED_" +fileName + filesExtension);
					
					fos = new FileOutputStream(outputFile);
		            osw = new OutputStreamWriter(fos, "UTF-8");    
		            fileWriter = new BufferedWriter(osw);
		            
		            //outputControlFile = new File(outPath + "CONTROL_" +fileName + filesExtension);
		            //fosControl = new FileOutputStream(outputControlFile);
		            //oswControl = new OutputStreamWriter(fosControl, "UTF-8");    
		            //fileWriterControl = new BufferedWriter(oswControl);
		            
		            fileBlockOne = new StringBuilder();
		            fileBlockTwo = new StringBuilder();
		            lineSixSb = new StringBuilder();
					
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
									fileWriter.write(fileBlockOne.toString() 
											+ lineSixSb.toString() 
											+ fileBlockTwo.toString());
									ecbWritten++;
									
									resetECB();
								}
								fileBlockOne.append(strLine+"\n");
								
							}else if(lineNum > 1 && lineNum < 6){//lineas 2 a 5
								if(lineNum == 2){
									documentType = arrayValues[1];
								}
								fileBlockOne.append(strLine+"\n");
							}else if(lineNum == 6){//linea 6
								
								//quitar los conceptos "-" del catalogo
								if(!removeIsNeeded(arrayValues[1])){
									lineSixSb.append(strLine + "\n");
								}
								
							}else if(lineNum > 6){//lineas 7 a 11
								fileBlockTwo.append(strLine+"\n");
							}
						}
						firstLoop = false;
					}
					if (ecbWritten < ecbCount ){
						System.out.println("Escribiendo ultimo ECB");
						fileWriter.write(fileBlockOne.toString() 
								+ lineSixSb.toString() 
								+ fileBlockTwo.toString());

						ecbWritten++;
						resetECB();
					}
					
					fileWriter.close();
					//fileWriterControl.close();
					br.close();
					File movedFile = new File(PathECBSalida + fileName + "ORIGINAL" + filesExtension);
					if(!movedFile.exists()){
						if(inputFile.renameTo(movedFile)){
				    		//renombrar archivo generado
							if(outputFile.renameTo(new File(PathECBEntrada + fileName + filesExtension))){
								System.out.println("Los archivos procesados se han ubicado correctamente");
								result = true;
							}else{
								System.out.println("No se pudo renombrar el archivo generado");
								result = false;
							}
				    	}else{
				    		System.out.println("No se pudo mover el archivo original");
				    		result = false;
				    	}
					}else{
						System.out.println("El archivo: " + PathECBSalida + fileName + "ORIGINAL" + filesExtension
								+" ya existe en la ruta de salida");
						outputFile.delete();
						result = false;
					}
					
				}else{
					System.out.println("No se encontro el archivo de entrada: "+PathECBEntrada + fileName + filesExtension);
					result = false;
				}
				return result;
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Exception processTotalECB:" + e.getMessage());
			try {
				FileOutputStream fileError = new FileOutputStream(PathECBProceso + "formateaECBPampaError.txt");
				fileError.write(e.getMessage().getBytes());
				fileError.close();
			} catch (Exception e1) {
				e1.printStackTrace();
				System.out.println("Exception al crear formateaECBPampaError.txt:" + e.getMessage());
			}
			return false;
		}		
	}
	

	
	private void resetECB(){
		fileBlockOne = new StringBuilder();
		fileBlockTwo = new StringBuilder();
		lineSixSb = new StringBuilder();
		documentType = null;
	}
	
	private void loadPampasConceptList() throws Exception{
		FileInputStream fis = new FileInputStream(PathECBCatalogos + pampasConceptCatalog);
		DataInputStream dis  = new DataInputStream(fis);
		BufferedReader bfr = new BufferedReader(new InputStreamReader(dis, "UTF-8"));
		String conceptLine = null;
		pampasConceptList= new ArrayList<String[]>();
		
		while((conceptLine = bfr.readLine()) != null){
			String[] conceptArray = conceptLine.split("\\|");
			pampasConceptList.add(conceptArray);
		}
		bfr.close();
	}
	
	private boolean removeIsNeeded(String concept){
		boolean result = false;
		if(pampasConceptList != null){
			for(String[] row : pampasConceptList){
				if(row.length == 2){
					if(row[0].equals("-") && row[1].equalsIgnoreCase(concept)){
						result = true;
						break;
					}
				}
			}
		}
		return result;
	}
	
}
