/*
 * Group 51
 * Brian Kim-Lim (260636766)
 * Jason Dias (260617554)
 */

package navigation;

import org.freedesktop.DBus.Local;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class Lab3 {

	// instantiate static resources
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static Odometer odometer;
	private static Navigation navigation;
	private static final double pathOne[][] = {{60,30},{30,30},{30,60},{60,60}};
	private static final double pathTwo[][] = {{0,60},{60,0}};
	
	@SuppressWarnings("resource")							    // Because we don't bother to close this resource
	private static SensorModes usSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S1"));		// usSensor is the instance
	private static SampleProvider usDistance = usSensor.getMode("Distance");	// usDistance provides samples from this instance
	private static float[] usData = new float[usDistance.sampleSize()];
	
	// final variables
	public static final double TRACK = 15.8;
	public static final double WHEEL_RADIUS = 2.1;

	public static void main(String[] args) {
		
		int buttonChoice;

		// instantiate resources
		final TextLCD textLCD = LocalEV3.get().getTextLCD();
		odometer = new Odometer(leftMotor, rightMotor, WHEEL_RADIUS, TRACK);
		OdometryDisplay display = new OdometryDisplay(odometer, textLCD);
		UltrasonicPoller usPoller = null;
		

		do {
			// clear the display
			textLCD.clear();

			// ask the user whether the robot should take Path 1 (4 way points) or Path 2 (block avoidance)
			textLCD.drawString("< Left | Right >", 0, 0);
			textLCD.drawString("       |        ", 0, 1);
			textLCD.drawString("PathOne| PathTwo", 0, 2);

			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);
		
		odometer.start();
		display.start();

		if (buttonChoice == Button.ID_LEFT) { //pathOne
			navigation = new Navigation(odometer, leftMotor, rightMotor, pathOne, TRACK, WHEEL_RADIUS, false);
			
		}	

		else { //pathTwo
			navigation = new Navigation(odometer, leftMotor, rightMotor, pathTwo, TRACK, WHEEL_RADIUS, true);
			usPoller = new UltrasonicPoller(usDistance, usData, navigation);
			usPoller.start();
		}
		
		navigation.start();
		
		

		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
	
	

}
