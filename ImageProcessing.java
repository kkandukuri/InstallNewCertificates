package com.expressfashion.itemskupopulation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * 
 * 
 * @author kkandukuri
 *checks if image is available or not.
 */
public class ImageProcessing {

	public void processData() throws Exception {

		try  {
			File file = new File("/Users/kkandukuri/Images.csv");

			BufferedReader br = new BufferedReader(new FileReader(file));

			String newLine;
			boolean isFirst = true;
			ExecutorService executorSer = Executors.newFixedThreadPool(10);
			BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/kkandukuri/ImagesValidation.csv"));
			
			while ((newLine = br.readLine()) != null) {
				if (isFirst) {
					isFirst = false;
					continue;

				}
				System.out.println(newLine);

				String[] splitString = newLine.split(",");
				//System.out.println(splitString[splitString.length - 1]);
				try {
				if (splitString[14] != null) {
					ImageThread thread = new ImageThread(splitString, newLine,writer);
					executorSer.execute(thread);
				} else {
					writer.write(newLine + "," + "Not Available");
				}
				}catch(Exception e) {
					System.out.println("Error:"+e.getMessage());
				}
			}
			executorSer.shutdown();
			try {
				executorSer.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			} catch (InterruptedException e) {
				System.out.println("Error in Executor thread.");
			}
			

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	public class ImageThread implements Runnable {
		String urlI;
		BufferedWriter out;
		String data;
		public ImageThread(String[] splitString, String dataI,BufferedWriter writer) {
			urlI = "https://images.express.com/is/image/expressfashion/" + splitString[splitString.length - 1].replace("\"", "");
			out=writer;
			data = dataI;
		}

		public void run() {
			try {
				try {
				URL url = new URL(urlI);
				HttpURLConnection http = (HttpURLConnection) url.openConnection();
				int statusCode = http.getResponseCode();
				System.out.println(data+"," + "Available");
				if (statusCode == 200) {
					out.write(data + "," + "Available");
					out.write("\n");
				}
				else {
					out.write(data + "," + "Not Available");// +","+"https://images.express.com/is/image/expressfashion/"+splitString[splitString.length-1]
					out.write("\n");
				}
				}catch(Exception e) {
					System.out.println(e);
					out.write(data + "," + "Not Available");
					out.write("\n");
				}
			} catch (Exception ex) {
				System.out.println(" Exception in Thread:" + ex.getMessage());
			}
		}
	}
}
