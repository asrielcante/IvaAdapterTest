package main;

import util.ecb.file.FormateaECBIvaController;
import util.ecb.file.FormateaECBPampaController;
import util.ecb.file.ProcessTotalECBController;

public class FormateaECB {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//String fileName = "CFDLMPAMPAS20171101";
//		args = new String[2];
//		args[0] = "CFDLMPAMPAS,CFDLMPAMPAA";
//		args[1] = "20171101";
		
		String[] filenames = args[0].split(",");
		String date = args[1].trim();
		
		System.out.println("names: "+args[0]);
		System.out.println("date: "+args[1]);
		
		FormateaECBPampaController ecbPampaUtil = new FormateaECBPampaController();
		FormateaECBIvaController ecbIvaUtil = new FormateaECBIvaController();
		
		for(int i = 0; i < filenames.length; i ++){
			
			boolean continua = true;
			
			if(filenames[i].equalsIgnoreCase("CFDLMPAMPAS")
					|| filenames[i].equalsIgnoreCase("CFDLMPAMPAA")){//ajuste lineas 6 para pampa
				if(!ecbPampaUtil.processECBTxtFile(filenames[i].trim()+date)){
					continua = false;
					System.out.println("Error al procesar: " + filenames[i].trim());
				}
			}else{
				//reglas faltantes carter...
			}
			
			if(continua){//ajuste iva para todas las interfaces
				ecbIvaUtil.processECBTxtFile(filenames[i].trim()+date);
			}
			
		}
		
		System.out.println("Fin del procesamiento Formatea ECB Pampa");
		System.exit(0);
	}
}
