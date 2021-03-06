/* 
 * OdometryCorrection.java

 * Group 51
 * Brian Kim - Lim 260636766
 * Jason Dias 260617554
 */
package ev3Odometer;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.Color;
import lejos.robotics.SampleProvider;

public class OdometryCorrection extends Thread {
	private static final long CORRECTION_PERIOD = 10;
	private Odometer odometer;
	
	//resources
	private EV3ColorSensor colorSensor;

	//data from light sensor
	private float[] sampleData;
	private int buzzCounter = 0;
	private double[] currentPosition = new double[3];
	private boolean[] getValues = new boolean[]{true,true,true};
			
	// constructor
	public OdometryCorrection(Odometer odometer, EV3ColorSensor colorSensor) {
		this.odometer = odometer;
		this.colorSensor = colorSensor;
	}
	

	// run method (required for Thread)
	public void run() {
		long correctionStart, correctionEnd;
		SampleProvider sampleProvider = colorSensor.getMode("ColorID");
		int sampleSize = sampleProvider.sampleSize();
		sampleData = new float[sampleSize];
		
		while (true) {
			correctionStart = System.currentTimeMillis();
			colorSensor.fetchSample(sampleData,0); //fetch sample from sensor
			if(sampleData[0] == 13.0) { //sampledata only contains one value per thread cycle
				Sound.buzz(); //indicate black line for testing
				odometer.getPosition(currentPosition, getValues);
				if(currentPosition[1] <= 20 && currentPosition[1] >= 10 && // line 1
						currentPosition[0] <= 5 && currentPosition[0] >= -5) {
					odometer.setX(0);
					odometer.setY(15);
					odometer.setTheta(0);
				}
				else if(currentPosition[1] <= 50 && currentPosition[1] >= 40 && // line 2
						currentPosition[0] <= 5 && currentPosition[0] >= -5) {
					odometer.setX(0);
					odometer.setY(45);
					odometer.setTheta(0);
					
					
				}
				else if(currentPosition[1] <= 65 && currentPosition[1] >= 55 && // line 3
						currentPosition[0] <= 20 && currentPosition[0] >= 10) {
					odometer.setX(15.5);	
					odometer.setY(60);
					odometer.setTheta(90);
				
				}
				else if(currentPosition[1] <= 65 && currentPosition[1] >= 55 && //line 4
						currentPosition[0] <= 50 && currentPosition[0] >= 40) {
					odometer.setX(45);
					odometer.setY(60);
					odometer.setTheta(90);
					
				}
				else if(currentPosition[1] <= 50 && currentPosition[1] >= 40 && //line 5
						currentPosition[0] <= 65 && currentPosition[0] >= 55) {
					odometer.setX(60);
					odometer.setY(45);
					odometer.setTheta(180);
				
					
				}
				else if(currentPosition[1] <= 20 && currentPosition[1] >= 10 && //line 6
						currentPosition[0] <= 65 && currentPosition[0] >= 55) {
					odometer.setX(60);
					odometer.setY(15);
					odometer.setTheta(180);
					
				}
				else if(currentPosition[1] <= 5 && currentPosition[1] >= -5 && //line 7
						currentPosition[0] <= 50 && currentPosition[0] >= 40) {
					odometer.setTheta(270);
					
				}
				else if(currentPosition[1] <= 5 && currentPosition[1] >= -5 && //line 8
						currentPosition[0] <= 20 && currentPosition[0] >= 10) {
					odometer.setX(14);
					odometer.setTheta(270);
					
				}
				
			
				
				
			}
				
			
			
			// this ensure the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD
							- (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometry correction will be
					// interrupted by another thread
				}
			}
		}
	}
}
