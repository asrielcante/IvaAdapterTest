package main;

import util.ecb.file.IvaProcessECBController;

public class IvaProcessECB {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fileName = "CFDOPCASA20171016.TXT";
		
		IvaProcessECBController ecbUtil = new IvaProcessECBController();
		ecbUtil.processECBTxtFile(fileName);
	}
}
