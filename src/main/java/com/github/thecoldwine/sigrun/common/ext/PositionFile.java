package com.github.thecoldwine.sigrun.common.ext;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.opencsv.CSVReader;
import com.ugcs.gprvisualizer.app.MessageBoxHelper;
import com.ugcs.gprvisualizer.math.HorizontalProfile;

public class PositionFile {

	public void load(SgyFile sgyFile, File posfile) throws Exception {
		
		
		File file = sgyFile.getFile();
		//File mkupfile = getPositionFileBySgy(file);
		if (!posfile.exists()) {
			System.out.println(" not exists " + posfile.getAbsolutePath());
			return;
		}
		
		
		//[Elapsed, Date, Time, Pitch, Roll, Yaw, Latitude, Longitude, Altitude, Velocity, RTK Status, Latitude RTK, Longitude RTK, Altitude RTK, ALT:Altitude, ALT:Filtered Altitude, GPR:Trace]
		//[309793, 2021/05/12, 07:48:58.574, -6.03, -0.73, 137.52, 56.86301828, 24.11194153, 3.60, 5.20, OFF, , , , 2.91, 2.91, 999]

		try (CSVReader csvReader = new CSVReader(new FileReader(posfile));) {
			
			String[] header = csvReader.readNext();
			
			//точная высота относительно земли высотометр
			int altAltIndex = ArrayUtils.indexOf(header, "ALT:Altitude");
			
			//барометрическая относительно точки взлета
			int altIndex = ArrayUtils.indexOf(header, "Altitude");
			
			//эллипсоидная относительно уровня моря
			int altRtkIndex = ArrayUtils.indexOf(header, "Altitude RTK");
			
		    String[] values = null;
		    
		    HorizontalProfile hp = new HorizontalProfile(sgyFile.getTraces().size());
		    HorizontalProfile hp2 = new HorizontalProfile(sgyFile.getTraces().size());
		    
		    double hair =  100 / sgyFile.getSamplesToCmAir();
		    
   		    int posCount = 0;
		    while ((values = csvReader.readNext()) != null) {
		    	if (posCount > 63640) {
		    		System.out.println(Arrays.toString(values));
		    	}
		    	
	    	    //skip empty row or traces less than positions
		    	if(values.length >= 3 && posCount < sgyFile.getTraces().size()) {
		    		hp.deep[posCount] = (int)(Double.valueOf(values[altAltIndex]) * hair);
		    		hp2.deep[posCount] = (int)(Double.valueOf(values[altIndex]) * hair);
		    	}
		    	posCount++;		    	
		    }
		    		    
		    hp.finish(sgyFile.getTraces());			
			hp.color = Color.red;
			
			sgyFile.groundProfile = hp;
			
			
			hp2.finish(sgyFile.getTraces());			
			hp2.color = Color.green;
			
			System.out.println(posCount + " <> " + sgyFile.getTraces().size());
			if (posCount != sgyFile.getTraces().size()) {
				
				MessageBoxHelper.showError(
						"Warning", 
						"Count of traces in GPR file is " + sgyFile.getTraces().size() + " and count of traces in position file is " + posCount);
			}
			
			//sgyFile.profiles = new ArrayList<>();
			//sgyFile.profiles.add(hp2);
		}

	}
	
	
	
	
	private File getPositionFileBySgy(File file) {
		
		String mrkupName = null; 
		
		if (file.getName().toLowerCase().endsWith("gpr.sgy")) {
			mrkupName = StringUtils.replaceIgnoreCase(
					file.getAbsolutePath(), "gpr.sgy", "position.csv");
		} else if (file.getName().toLowerCase().endsWith(".dzt")) {
			mrkupName = StringUtils.replaceIgnoreCase(
					file.getAbsolutePath(), ".dzt", ".mrkup");
		}
		
		
		File mkupfile = new File(mrkupName);
		return mkupfile;
	}
}
