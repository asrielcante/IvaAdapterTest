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

public class IvaProcessECBController {
	
	public IvaProcessECBController() {
	
	}
	
	public static String inputFilePath = "C:\\Users\\ase\\Desktop\\ECB batch\\ejemplosdearchivosdeentradaedc\\";
	public static String outputFilePath = "C:\\Users\\ase\\Desktop\\ECB batch\\ejemplosdearchivosdeentradaedc\\output\\";
	
	BigDecimal val1;
	BigDecimal val2;
	
	StringBuilder fileBlockOne;
    StringBuilder fileProcessLine;
    StringBuilder fileBlockTwo;
	
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
            
            val1 = BigDecimal.ZERO;
			val2 = BigDecimal.ZERO;
			boolean firstLoop = true;
			int ecbCount = 0;
			int ecbWritten = 0;
			while((strLine = br.readLine()) != null){
				
				if(!strLine.equals("")){
					String [] arrayValues = strLine.split("\\|");
					int lineNum = Integer.parseInt(arrayValues[0]);
					if(lineNum == 1){
						ecbCount++;
						
						if(!firstLoop){
							
							fileProcessLine.append(generateProcessLine(val1, val2));
							fileWriter.write(fileBlockOne.toString() + fileProcessLine.toString() + fileBlockTwo.toString());
							ecbWritten++;
							
							resetECB();
						}
						
						fileBlockOne.append(strLine+"\n");
						
					}else if(lineNum > 1 && lineNum < 7){
						fileBlockOne.append(strLine+"\n");
					}else if(lineNum > 7 && lineNum < 11){
						fileBlockTwo.append(strLine+"\n");
					}else if(lineNum == 11){
						fileBlockTwo.append(strLine+"\n");
						//calcular
						val1 = val1.add(new BigDecimal(arrayValues[5]));
						val2 = val2.add(new BigDecimal(arrayValues[5]));
					}
				}
				firstLoop = false;
			}
			if (ecbWritten < ecbCount ){
				System.out.println("Escribiendo ultimo ECB");
				
				fileProcessLine.append(generateProcessLine(val1, val2));
				fileWriter.write(fileBlockOne.toString() + fileProcessLine.toString() + fileBlockTwo.toString().trim());
				
				resetECB();
			}
			
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
