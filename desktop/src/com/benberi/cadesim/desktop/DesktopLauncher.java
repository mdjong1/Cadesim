package com.benberi.cadesim.desktop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;
import java.util.Scanner;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.benberi.cadesim.BlockadeSimulator;
import com.benberi.cadesim.Constants;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Thread updateThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					BufferedReader sc = new BufferedReader(new FileReader("getdown.txt"));
					sc.readLine();
					//get server url from getDown.txt
					String cadesimUrl = sc.readLine();
					String[] url = cadesimUrl.split("=");
					//read version from getDown.txt
					String cadeSimVersion = sc.readLine();
					String[] version = cadeSimVersion.split("=");
					String txtVersion = version[1].replaceAll("\\s+","");
					URL cadesimServer = new URL(url[1] + "version.txt");
					//read version from server
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(cadesimServer.openStream()));
					String serverVersion = reader.readLine().replaceAll("\\s+","");
					boolean updateBool = serverVersion.equals(txtVersion);
					if(!updateBool) {
						Constants.SERVER_VERSION_BOOL = false;
					}else {
						Constants.SERVER_VERSION_BOOL = true;
					}
				}
				 catch (IOException e) {
					e.printStackTrace();
				}
			}
		
		});
		updateThread.start();
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		int[] res = new int[2];
		BlockadeSimulator cadesim = new BlockadeSimulator();
		res = loadresolution();
		config.resizable = false;
		config.width = res[0];
		config.height = res[1];
		//config.backgroundFPS = 20;    // bugfix high CPU
		config.vSyncEnabled = false; // "
		config.title = Constants.name + " (version " + Constants.VERSION + ")";
		new LwjglApplication(cadesim, config);
	}

	private static int[] loadresolution() {
		Properties prop = new Properties();
		String fileName = "user.config";
		InputStream is = null;
		try {
		    is = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		}
	
		try {
		    prop.load(is);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    int[] resolution = new int[2];
		resolution[0] = Integer.parseInt(prop.getProperty("user.width"));
		resolution[1] = Integer.parseInt(prop.getProperty("user.height"));
		return resolution;
	}
}