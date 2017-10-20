package main;

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

import util.ecb.file.EcbTxtFileUtil;

public class IvaProcessECB {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		EcbTxtFileUtil ecbUtil = new EcbTxtFileUtil();
		ecbUtil.processTxtFile();
	}
}
