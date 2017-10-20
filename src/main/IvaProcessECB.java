package main;

import util.ecb.file.IvaProcessECBController;

public class IvaProcessECB {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fileName = "CFDOPBANCO20171016.TXT";
		
		IvaProcessECBController ecbUtil = new IvaProcessECBController();
		ecbUtil.processTxtFile(fileName);
	}
}
