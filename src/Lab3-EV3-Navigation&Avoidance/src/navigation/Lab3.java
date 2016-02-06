package navigation;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3UltrasonicSensor;

public class Lab3 {

	// instantiate static resources
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	//private static final EV3UltrasonicSensor uSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S1"));
	private static Odometer odometer;
	private static Navigation navigation;
	private static final double pathOne[][] = {{60,30},{30,30},{30,60},{60,60}};
	private static final double pathTwo[][] = {{0,60},{60,0}};
	
	
	// final variables
	public static final double TRACK = 15.8;
	public static final double WHEEL_RADIUS = 2.1;

	public static void main(String[] args) {

		int buttonChoice;

		// instantiate resources

		final TextLCD textLCD = LocalEV3.get().getTextLCD();
		odometer = new Odometer(leftMotor, rightMotor, WHEEL_RADIUS, TRACK);
		OdometryDisplay display = new OdometryDisplay(odometer, textLCD);
		

		do {
			// clear the display
			textLCD.clear();

			// ask the user whether the motors should drive in a square or float
			textLCD.drawString("< Left | Right >", 0, 0);
			textLCD.drawString("       |        ", 0, 1);
			textLCD.drawString("PathOne| PathTwo", 0, 2);

			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);
		
		odometer.start();
		display.start();

		if (buttonChoice == Button.ID_LEFT) { //pathOne
			navigation = new Navigation(odometer, leftMotor, rightMotor, pathOne, TRACK, WHEEL_RADIUS);
			
		}	

		else { //pathTwo
			navigation = new Navigation(odometer, leftMotor, rightMotor, pathTwo, TRACK, WHEEL_RADIUS);
		}
		
		navigation.start();

		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
	
	

}
