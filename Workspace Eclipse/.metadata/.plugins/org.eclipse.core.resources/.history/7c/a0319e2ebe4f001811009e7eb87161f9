package main;

public class GlobalVariables {
	private static GlobalVariables instance;
	
	private int bufferSize = 64;
	private double latencyCompensation = 0;
	
	public static GlobalVariables getInstance() {
		if (instance == null) {
			instance = new GlobalVariables();
		}
		return instance;
	}
	
	private GlobalVariables() {
		
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public double getLatencyCompensation() {
		return latencyCompensation;
	}

	public void setLatencyCompensation(double latencyCompensation) {
		this.latencyCompensation = latencyCompensation;
	}
	
	
}
