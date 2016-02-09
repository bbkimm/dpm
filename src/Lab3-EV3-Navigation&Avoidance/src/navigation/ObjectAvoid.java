package navigation;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class ObjectAvoid extends Thread{
	private final int THRESHOLD = 10;
	private static double xDest;
	private static double yDest;
	
	private Odometer odometer;
	private EV3UltrasonicSensor usSensor;
	private Navigation nav;
	
	public ObjectAvoid(Odometer odo, EV3UltrasonicSensor us, Navigation nav){
		this.odometer = odo;
		this.usSensor = us;
		this.nav = nav;
	}
	
	//may not need this entire class. Was trying to see if using this as a thread is more efficient
	public void run(){
		/*while(true){
			
		}*/
	}
}
