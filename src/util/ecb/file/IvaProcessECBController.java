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
	
	public static String inputFilePath = "C:\\Users\\ase\\Desktop\\ECB batch\\ejemplosdearchivosdeentradaedc\\";
	public static String outputFilePath = "C:\\Users\\ase\\Desktop\\ECB batch\\ejemplosdearchivosdeentradaedc\\output\\";
	
	BigDecimal val1;
	BigDecimal val2;
	BigDecimal totalMnOriginal;
	BigDecimal newTotalMn;
	
	StringBuilder fileBlockOne;
    StringBuilder fileProcessLine;
    StringBuilder fileBlockTwo;
    
    StringBuilder lineSixSb;
    
    String firstLine = null;
    String [] arrayFirstLine = null;
	List<String> lineSixList = null;
	List<String[]> lineElevenList = null;
	
	public void processECBTxtFile(String fileName) {
		FileInputStream fileToProcess = null;
		DataInputStream in = null;
		BufferedReader br = null;
		
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		Writer fileWriter = null;
		
		File outDir;
		File outputFile;
		
		try{
			fileToProcess = new FileInputStream(inputFilePath + fileName);
			in = new DataInputStream(fileToProcess);
			br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			outDir =  new File(outputFilePath);
			if (!outDir.exists()) {
				outDir.mkdirs();
			}
			String strLine;
			
			outputFile = new File(outputFilePath + "GENERATED" +fileName);
			fos = new FileOutputStream(outputFile);
            osw = new OutputStreamWriter(fos, "UTF-8");    
            fileWriter = new BufferedWriter(osw);
            
            fileBlockOne = new StringBuilder();
            fileProcessLine = new StringBuilder();
            fileBlockTwo = new StringBuilder();
            lineSixSb = new StringBuilder();
            
            val1 = BigDecimal.ZERO;
			val2 = BigDecimal.ZERO;
			newTotalMn = BigDecimal.ZERO;
			totalMnOriginal = BigDecimal.ZERO;
			
			firstLine = null;
			arrayFirstLine = null;
			lineSixList = new ArrayList<String>();
			
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
								//realizar cambios
							}else{
								fileWriter.write(firstLine + "\n"
										+ fileBlockOne.toString() 
										+ lineSixSb.toString() 
										+ fileBlockTwo.toString());
								ecbWritten++;
							}
							
							
							//fileProcessLine.append(generateProcessLine(val1, val2));
							//fileWriter.write(fileBlockOne.toString() + fileProcessLine.toString() + fileBlockTwo.toString());
							//ecbWritten++;
							
							resetECB();
						}
						
						firstLine = strLine;
						arrayFirstLine = arrayValues;
						totalMnOriginal = new BigDecimal(arrayFirstLine[5]);
						//fileBlockOne.append(strLine+"\n");
						
					}else if(lineNum > 1 && lineNum < 6){//lineas 2 a 5
						fileBlockOne.append(strLine+"\n");
					}else if(lineNum == 6){//linea 6
						//lineSixList.add(strLine);
						lineSixSb.append(strLine + "\n");
						newTotalMn = newTotalMn.add(new BigDecimal(arrayValues[2]));
					}else if(lineNum > 6 && lineNum < 11){//lineas 7 a 10
						fileBlockTwo.append(strLine+"\n");
					}else if(lineNum == 11){//linea 11
						fileBlockTwo.append(strLine+"\n");
						lineElevenList.add(arrayValues);
					}
				}
				firstLoop = false;
			}
//			if (ecbWritten < ecbCount ){
//				System.out.println("Escribiendo ultimo ECB");
//				
//				fileProcessLine.append(generateProcessLine(val1, val2));
//				fileWriter.write(fileBlockOne.toString() + fileProcessLine.toString() + fileBlockTwo.toString().trim());
//				
//				resetECB();
//			}
			
			fileWriter.close();
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private String generateProcessLine(BigDecimal value1, BigDecimal value2){
		String generatedLine = "";
		
		generatedLine = "07|" + value1.toString() + "|" + value2.toString() + "\n";
		return generatedLine;
	}
	
	private void resetECB(){
		fileBlockOne = new StringBuilder();
		fileProcessLine = new StringBuilder();
		fileBlockTwo = new StringBuilder();
		val1 = BigDecimal.ZERO;
		val2 = BigDecimal.ZERO;
		
		newTotalMn = BigDecimal.ZERO;
		totalMnOriginal = BigDecimal.ZERO;
		
		arrayFirstLine = null;
		lineSixSb = new StringBuilder();
		//lineSixList = new ArrayList<String>();
		lineElevenList = new ArrayList<String[]>();
	}
	
	private String strJoin(String[] aArr, String sSep) {
	    StringBuilder sbStr = new StringBuilder();
	    for (int i = 0, il = aArr.length; i < il; i++) {
	        if (i > 0)
	            sbStr.append(sSep);
	        sbStr.append(aArr[i]);
	    }
	    return sbStr.toString();
	}

}
