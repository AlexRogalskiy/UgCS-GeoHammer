package com.ugcs.gprvisualizer.app.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.math.HorizontalProfile;


/**
 * find ground profile from file.profiles 
 *
 */
public class HorizontalGroupFilter implements Command {

	@Override
	public void execute(SgyFile file) {
		System.out.println("  -- -HorizontalGroupFilter- " + file.getFile().getName());
		
		List<HorizontalProfile> tmpStraight = new ArrayList<>();
		List<HorizontalProfile> tmpCurve = new ArrayList<>();
		for(HorizontalProfile hp : file.profiles) {
			if(hp.height <= 4) {
				tmpStraight.add(hp);
			}else {
				tmpCurve.add(hp);
			}
		}
		
		if(tmpStraight.isEmpty() || tmpCurve.isEmpty()) {
			return;
		}
		System.out.println("  -top- ");
		HorizontalProfile brightestTop = getBrightest(tmpStraight);
		brightestTop.color = Color.RED;
		
		System.out.println("  -grnd- ");
		HorizontalProfile brightestGrn = getBrightest(tmpCurve);
		brightestGrn.color = Color.RED;
		
		file.groundProfile = brightestGrn; 
		
//		HorizontalProfile bott = createMirroredLine(file, brightestTop, brightestGrn);
		
		
//		List<HorizontalProfile> result = new ArrayList<>();
//		result.add(brightestTop);
//		result.add(brightestGrn);
//		result.add(bott);
//		file.profiles = result;
		
		
	}

	public static HorizontalProfile createMirroredLine(SgyFile file, HorizontalProfile brightestTop,
			HorizontalProfile brightestGrn) {
		
		int maxsmp = file.getMaxSamples()-1;
		HorizontalProfile bott = new HorizontalProfile(brightestTop.deep.length);
		for(int i=0; i<brightestTop.deep.length; i++ ) {
			
			bott.deep[i] = 
					Math.min(maxsmp,
					Math.max(0, brightestGrn.deep[i] + (brightestGrn.deep[i] - (int)brightestTop.avgdeep)));// deep[i]
			
		}
		bott.finish(file.getTraces());
		bott.color = new Color(150, 70, 70);
		return bott;
	}

	@Override
	public String getButtonText() {

		return "Ground from profiles";
	}

	@Override
	public Change getChange() {

		return Change.justdraw;
	}

	public static HorizontalProfile getBrightest(List<HorizontalProfile> tmpStraight) {
		int maxi = 0;
		int mini = 0;
		for(int i=0; i< tmpStraight.size(); i++) {
			
			HorizontalProfile hpi = tmpStraight.get(i);
			System.out.println("  avg of curve " + hpi.avgval);
			
			if(hpi.avgval > tmpStraight.get(maxi).avgval) {
				maxi = i;
			}
			if(hpi.avgval < tmpStraight.get(mini).avgval) {
				mini = i;
			}
		}
		int mid = (maxi+mini)/2;
		
		System.out.println(mini + " <-min " + mid + " max->  " + maxi);
		
		HorizontalProfile brightest = tmpStraight.get(mid);
		return brightest;
	}

	
}
