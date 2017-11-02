package main;

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
		
		FormateaECBPampaController ecbPampaUtil = new FormateaECBPampaController();
		
		for(int i = 0; i < filenames.length; i ++){
			if(filenames[i].equalsIgnoreCase("CFDLMPAMPAS")
					|| filenames[i].equalsIgnoreCase("CFDLMPAMPAA")){
				if(!ecbPampaUtil.processECBTxtFile(filenames[i].trim()+date)){
					System.out.println("Error al procesar: " + filenames[i].trim());
				}
			}
		}
		
		System.out.println("Fin del procesamiento Formatea ECB Pampa");
		System.exit(0);
	}
}
