/*
 * Group 51
 * Brian Kim-Lim (260636766)
 * Jason Dias (260617554)
 */

package navigation;

public interface UltrasonicController {
	
	public void processUSData(int distance);
	
	public int readUSDistance();
}
