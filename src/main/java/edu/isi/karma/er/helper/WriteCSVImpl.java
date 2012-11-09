package edu.isi.karma.er.helper;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class WriteCSVImpl {
	private FileWriter fw;
	private BufferedWriter bw;

	public void WriteCSVImpl() {
		try {
			this.fw = new FileWriter(
			// "/Users/yingzhang/Downloads/BuildingMatchedResult.csv");
					"/Users/yzhang/Downloads/BuildingMatchedResult.csv");
			this.bw = new BufferedWriter(fw);
			String header = "Source,Source_NAME,Source_X,Source_Y,Source_POLYGON,Matched,Matched_Building_Name,"
					+ "Matched_Building_X,Matched_Building_Y,Matched_Building_Polygon,Distance,Similarity\r\n";
			this.fw.write(header);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void WriteOneLine(String oneLine) {
		try {
			this.bw.newLine();
			this.bw.write(oneLine);
			this.bw.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void Close() {
		try {
			this.bw.close();
			this.fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
