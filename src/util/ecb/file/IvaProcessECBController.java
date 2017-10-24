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

public class IvaProcessECBController {
	
	public IvaProcessECBController() {
	
	}
	
	public static String inputFilePath = "/home/linuxlite/shell_scripts/ECBIVA/input/";
	public static String outputFilePath = "/home/linuxlite/shell_scripts/ECBIVA/output/";
	public static String catalogsPath = "/home/linuxlite/shell_scripts/ECBIVA/catalogs/";
	
	public static String ingresoConceptsFileName = "conceptosIngresos.TXT";
	public static String egresoConceptsFileName = "conceptosEgresos.TXT";
	public static String filesExtension = ".TXT";
	
	BigDecimal val1;
	BigDecimal val2;
	BigDecimal totalMnOriginal;
	BigDecimal newTotalMn;
	
	StringBuilder fileBlockOne;
    StringBuilder fileProcessLine;
    StringBuilder fileBlockTwo;
    
    StringBuilder lineSixSb;
    
    String firstLine = null;
	List<String[]> lineElevenList = null;
	
	List<String> ingresoConceptList = null;
	List<String> egresoConceptList = null;
	
	String documentType = null;
	
	public void processECBTxtFile(String fileName) {
		FileInputStream fileToProcess = null;
		DataInputStream in = null;
		BufferedReader br = null;
		
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		Writer fileWriter = null;
		
		FileOutputStream fosControl = null;
		OutputStreamWriter oswControl = null;
		Writer fileWriterControl = null;
		
		File outDir;
		File outputFile;
		File outputControlFile;
		
		try{
			File inputFile = new File(inputFilePath + fileName + filesExtension);
			if(inputFile.exists()){
				fileToProcess = new FileInputStream(inputFilePath + fileName + filesExtension);
				in = new DataInputStream(fileToProcess);
				br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
				String outPath = outputFilePath + fileName + "/";
				outDir =  new File(outPath);
				if (!outDir.exists()) {
					outDir.mkdirs();
				}
				String strLine;
				
				loadIngresoConceptList();
				loadEgresoConceptList();
				
				outputFile = new File(outPath + "GENERATED_" +fileName + filesExtension);
				outputControlFile = new File(outPath + "CONTROL_" +fileName + filesExtension);
				
				fos = new FileOutputStream(outputFile);
	            osw = new OutputStreamWriter(fos, "UTF-8");    
	            fileWriter = new BufferedWriter(osw);
	            
	            fosControl = new FileOutputStream(outputControlFile);
	            oswControl = new OutputStreamWriter(fosControl, "UTF-8");    
	            fileWriterControl = new BufferedWriter(oswControl);
	            
	            fileBlockOne = new StringBuilder();
	            fileProcessLine = new StringBuilder();
	            fileBlockTwo = new StringBuilder();
	            lineSixSb = new StringBuilder();
	            
	            val1 = BigDecimal.ZERO;
				val2 = BigDecimal.ZERO;
				newTotalMn = BigDecimal.ZERO;
				totalMnOriginal = BigDecimal.ZERO;
				
				firstLine = null;
				
				lineElevenList = new ArrayList<String[]>();
				
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
								
								if(totalMnOriginal.compareTo(newTotalMn) != 0){
									String [] lineOne = firstLine.split("\\|");
									//guardar NumTarjeta y TotalMn en control file
									fileWriterControl.write(lineOne[4] + "|" + lineOne[5] + "|" + newTotalMn.toString() + "\n");
									//generar linea 1
									firstLine = replaceTotalFromFirstLine(firstLine, newTotalMn);
									//generar lineas 6
									lineSixSb = generateSixLinesBlock(lineElevenList);
									
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
							
						}else if(lineNum > 1 && lineNum < 6){//lineas 2 a 5
							if(lineNum == 2){
								documentType = arrayValues[1];
							}
							fileBlockOne.append(strLine+"\n");
						}else if(lineNum == 6){//linea 6
							lineSixSb.append(strLine + "\n");
							newTotalMn = newTotalMn.add(new BigDecimal(arrayValues[2]));
						}else if(lineNum > 6 && lineNum < 11){//lineas 7 a 10
							fileBlockTwo.append(strLine+"\n");
						}else if(lineNum == 11){//linea 11
							fileBlockTwo.append(strLine+"\n");
							
							//solo se toman en cuenta lineas 11 que esten en los archivos de config ingreso/egreso
							if(documentType.equalsIgnoreCase("I") 
									&& ingresoConceptList.contains(arrayValues[3])){
								lineElevenList.add(arrayValues);
							}else if(documentType.equalsIgnoreCase("E")
									&& egresoConceptList.contains(arrayValues[3])){
								lineElevenList.add(arrayValues);
							}
							
						}
					}
					firstLoop = false;
				}
				if (ecbWritten < ecbCount ){
					System.out.println("Escribiendo ultimo ECB");
					
					if(totalMnOriginal.compareTo(newTotalMn) != 0){
						String [] lineOne = firstLine.split("\\|");
						//guardar NumTarjeta y TotalMn en control file
						fileWriterControl.write(lineOne[4] + "|" + lineOne[5] + "|" + newTotalMn.toString());
						//generar linea 1
						firstLine = replaceTotalFromFirstLine(firstLine, newTotalMn);
						//generar lineas 6
						lineSixSb = generateSixLinesBlock(lineElevenList);
						
					}
					
					fileWriter.write(firstLine + "\n"
							+ fileBlockOne.toString() 
							+ lineSixSb.toString() 
							+ fileBlockTwo.toString().trim());
					ecbWritten++;
					
					resetECB();
				}
				
				fileWriter.close();
				fileWriterControl.close();
				br.close();
			}else{
				System.out.println("No se encontro el archivo de entrada: "+inputFilePath + fileName + filesExtension);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private String replaceTotalFromFirstLine(String originalLine, BigDecimal newTotalMnValue){
		StringBuilder controlLineSb = new StringBuilder();
		String [] originalLineArray = originalLine.split("\\|");
		
		for(int i = 0; i < originalLineArray.length; i++){
			if( i == 5){
				controlLineSb.append(newTotalMnValue.toString() + "|");
			}else{
				controlLineSb.append(originalLineArray[i] + "|");
			}
		}
		controlLineSb.setLength(controlLineSb.length() - 1);//remove last pipe
		return controlLineSb.toString();
	}
	
	private StringBuilder generateSixLinesBlock(List<String[]> ecbMovList){
		StringBuilder result = new StringBuilder();
		
		for(String[] mov : ecbMovList){
			result.append("06|");
			result.append(mov[3]);
			result.append("|");
			result.append(mov[5]);
			result.append("\n");
		}
		
		return result;
	}
	
	private void resetECB(){
		fileBlockOne = new StringBuilder();
		fileProcessLine = new StringBuilder();
		fileBlockTwo = new StringBuilder();
		val1 = BigDecimal.ZERO;
		val2 = BigDecimal.ZERO;
		
		newTotalMn = BigDecimal.ZERO;
		totalMnOriginal = BigDecimal.ZERO;
		
		lineSixSb = new StringBuilder();
		lineElevenList = new ArrayList<String[]>();
		
		documentType = null;
	}
	
	private void loadIngresoConceptList() throws Exception{
		FileInputStream fis = new FileInputStream(catalogsPath + ingresoConceptsFileName);
		DataInputStream dis  = new DataInputStream(fis);
		BufferedReader bfr = new BufferedReader(new InputStreamReader(dis, "UTF-8"));
		String ingresoLine = null;
		ingresoConceptList = new ArrayList<String>();
		
		while((ingresoLine = bfr.readLine()) != null){
			ingresoConceptList.add(ingresoLine);
		}
		bfr.close();
	}
	
	private void loadEgresoConceptList() throws Exception{
		FileInputStream fis = new FileInputStream(catalogsPath + egresoConceptsFileName);
		DataInputStream dis  = new DataInputStream(fis);
		BufferedReader bfr = new BufferedReader(new InputStreamReader(dis, "UTF-8"));
		String egresoLine = null;
		egresoConceptList = new ArrayList<String>();
		
		while((egresoLine = bfr.readLine()) != null){
			egresoConceptList.add(egresoLine);
		}
		bfr.close();
	}

}
