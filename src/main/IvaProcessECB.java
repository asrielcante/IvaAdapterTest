package main;

import util.ecb.file.ProcessTotalECBController;

public class IvaProcessECB {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//String fileName = "CFDLMPAMPAS20171016T07_03_14";
		String fileName = args[0].trim();
				
		ProcessTotalECBController ecbUtil = new ProcessTotalECBController();
		ecbUtil.processECBTxtFile(fileName);
	}
}
