package com.ugcs.gprvisualizer.app.commands;

import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.math.HalfHyperDst;
import com.ugcs.gprvisualizer.math.HorizontalProfile;
import com.ugcs.gprvisualizer.math.ScanProfile;

public class AlgorithmicScan implements AsinqCommand {

	private static final double X_FACTOR_FROM = 0.90;
	private static final double X_FACTOR_TO = 1.71;
	private static final double X_FACTOR_STEP = 0.1;
	
	@Override
	public void execute(SgyFile file) {
		
		//clear
		for(Trace t: file.getTraces()) {
			
			t.good = new int[t.getNormValues().length];			
		}
		
		
		//double kf = AppContext.model.getSettings().hyperkfc/100.0;
		
		processSgyFile(file);			
		
	}

	private void processSgyFile(SgyFile sf) {
		List<Trace> traces = sf.getTraces();
		int height = traces.get(0).getNormValues().length;
		int good[][] = new int[traces.size()][height];
		
		if(sf.groundProfile == null) {
			System.out.println("!!!!groundProfile == null");
			return;
		}
		
		for(int i=0; i<traces.size(); i++) {
			processTrace(sf, i, good); 
		}
		
		sf.algoScan = saveResultToTraces(traces, good);
	}

	private ScanProfile saveResultToTraces(List<Trace> traces, int[][] good) {
		
		ScanProfile hp = new ScanProfile(traces.size()); 
		
		for(int i=0; i<traces.size(); i++) {
			hp.intensity[i] = cleversumdst(good, i);// Math.max(hp.deep[i], cleversumdst(good, i));
			
			
			//put to trace.good
			if(traces.get(i).good == null) {
				traces.get(i).good = new int[good[i].length];
			}
			for(int z=0;z<good[i].length; z++) {
				traces.get(i).good[z] = good[i][z];
			}
			
		}
		hp.finish();
		
		return hp;
	}

	private int cleversumdst(int[][] good, int tr) {
		//TODO: use real distance in meters 
		int margin = 10;
		
		
		double sum = 0;		
		//boolean bothside = false;
		//boolean bothsidemax;
		double maxsum = 0;
		int emptycount =0;
		int both = 0; 
		for(int i=0; i<good[tr].length; i++) {
			
			// 0 1 2 3
			int val = getAtLeastOneGood(good, tr, margin, i);
			both = both | val;
			if(val != 0) {				
				sum += (val < 3 ? 1.0 : 2.0);
			}else {
				emptycount++;
				if(emptycount > 5) {
					maxsum = Math.max(maxsum, sum * (both == 3 ? 10 : 1));
					both = 0;
					sum = 0;
					emptycount = 0;					
				}
			}			
		}
		
		maxsum = Math.max(maxsum, sum * (both == 3 ? 10 : 1));
		return (int)(maxsum);//
	}

	
	
	private int getAtLeastOneGood(int[][] good, int tr, int margin, int smp) {
		int r = 0;
		
		for(int chtr = Math.max(0, tr-margin); chtr < Math.min(good.length, tr+margin+1); chtr++) {
			r = r | good[chtr][smp]; //0 1 2 3				
		}
		
		return r;
	}

	private double processTrace(SgyFile sgyFile, int tr, int[][] good) {
		int goodSmpCnt = 0;
		int maxSmp =
				Math.min(
						AppContext.model.getSettings().layer + AppContext.model.getSettings().hpage,
						sgyFile.getTraces().get(tr).getNormValues().length-2
				);
		
		// test all samples to fit hyperbola
		
		for(int smp = AppContext.model.getSettings().layer;				
			smp< maxSmp ; smp++) {
			
			// reduce x distance for hyperbola calculation
			for(double x_factor = X_FACTOR_FROM; x_factor <= X_FACTOR_TO; x_factor += X_FACTOR_STEP) {
				processHyper3(sgyFile, tr, smp, x_factor, good);
			}
		}
		
		return goodSmpCnt;
	}

	public double getThreshold() {
		double thr = (double)AppContext.model.getSettings().hyperSensitivity.intValue() / 100.0;
		return thr;
	}
	
	private void processHyper3(SgyFile sgyFile, int tr, int smp, double x_factor, int[][] good) {
		double thr = getThreshold();
		HalfHyperDst left = HalfHyperDst.getHalfHyper(sgyFile, tr, smp, -1, x_factor);		
		
		HalfHyperDst right = HalfHyperDst.getHalfHyper(sgyFile, tr, smp, +1, x_factor);
		
		double left100 = left.analize(100); 
		double left20 = left.analize(20);
		double right100 = right.analize(100);
		double right20 = right.analize(20);
		 		
		
		good[tr][smp] =
			good[tr][smp] 
				|
			(left100 > thr && right20 > thr ? 1 : 0) 
				| 
			(right100 > thr && left20 > thr ? 2 : 0); 
		
	}

	
	
	
	@Override
	public String getButtonText() {
	
		return "internal algorithmic scan";
	}

	public Change getChange() {
		return Change.traceValues;
	}
	
}
