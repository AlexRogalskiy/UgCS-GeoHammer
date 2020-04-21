package com.ugcs.gprvisualizer.app.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.math.HorizontalProfile;

/**
 * find ground level 2 (from HorizontalProfile) 
 *
 */
public class LevelScanHP implements Command {

	@Override
	public void execute(SgyFile file) {

		
		// find edges
		new EdgeFinder().execute(file);
		
		// find HPs
		new HorizontalGroupScan().execute(file);
		
		// lines  
		List<HorizontalProfile> lines = findStraightLines(file);
		
		if(lines.isEmpty()) {
			System.out.println("no top line :(");
			return;
		}
		// select top straight line !
		HorizontalProfile top = HorizontalGroupFilter.getBrightest(lines);
		
		// copy sgyfile
		SgyFile file2 = file.copy(); 
		
		// remove noise
		new BackgroundNoiseRemover().execute(file2);

		// find edges
		new EdgeFinder().execute(file2);
		
		// find HPs
		new HorizontalGroupScan().execute(file2);
		
		// select curve lines
		List<HorizontalProfile> lines2 = findCurveLines(file2);
		if(lines2.isEmpty()) {
			System.out.println("no grnd line2 :(");
			return;
		}
		
		// select best curve !
		HorizontalProfile grnd = HorizontalGroupFilter.getBrightest(lines2);
		grnd.finish(file.getTraces());
		grnd.color = new Color(200, 100, 100);
		
		// create sum HP
		//HorizontalProfile mirr = HorizontalGroupFilter.createMirroredLine(file, top, grnd);
		
		// copy to HP to original
		List<HorizontalProfile> result = new ArrayList<>();
		result.add(top);
		result.add(grnd);
		//result.add(mirr);
		file.profiles= result;
		file.groundProfile = grnd;
		
	}

	public List<HorizontalProfile> findStraightLines(SgyFile file) {
		List<HorizontalProfile> tmpStraight = new ArrayList<>();
		for(HorizontalProfile hp : file.profiles) {
			if(hp.height <= 3) {
				tmpStraight.add(hp);
			}
		}
		
		return tmpStraight;
	}

	public List<HorizontalProfile> findCurveLines(SgyFile file) {
		List<HorizontalProfile> tmpStraight = new ArrayList<>();
		for(HorizontalProfile hp : file.profiles) {
			if(hp.height > 4) {
				tmpStraight.add(hp);
			}
		}
		
		return tmpStraight;
	}

	@Override
	public String getButtonText() {
		
		return "Ground level scan v.2";
	}

	@Override
	public Change getChange() {

		return Change.justdraw;
	}
	
	
	

}