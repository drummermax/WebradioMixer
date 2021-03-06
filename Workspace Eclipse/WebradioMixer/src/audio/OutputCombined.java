package audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;

public class OutputCombined {
	private Input[] inputs;
	private Output[] outputs;
	private int numberOfInputs;

	private int mixerInfosIndex = 0;

	private double volume = 1; // range 0..1

	public OutputCombined(AudioFormat audioFormat, int mixerInfosIndex, Input[] inputs, boolean[] activateLatencyCompensation) {
		this.inputs = inputs;
		this.setMixerInfosIndex(mixerInfosIndex);
		numberOfInputs = inputs.length;
		outputs = new Output[numberOfInputs];

		for (int i = 0; i < numberOfInputs; i++) {
			outputs[i] = new Output(audioFormat, mixerInfosIndex, activateLatencyCompensation[i]);
		}
	}

	public void updateLine(AudioFormat audioFormat) {
		for (int i = 0; i < numberOfInputs; i++) {
			outputs[i].updateLine(audioFormat);
		}
	}

	public void close() {
		for (int i = 0; i < numberOfInputs; i++) {
			outputs[i].close();
		}
	}

	public double getVolume() {
		return volume;
	}

	public void setVolume(double volume) {		
		this.volume = volume;

		for (int i = 0; i < numberOfInputs; i++) {
			outputs[i].setVolume(volume);
		}
	}

	public void setVolumeOfSingleInput(int input, double volume) {
		outputs[input].setVolume(volume);
	}
	
	public void mute() {
		for (int i = 0; i < numberOfInputs; i++) {
			outputs[i].mute();
		}
	}
	
	public void unmute() {
		for (int i = 0; i < numberOfInputs; i++) {
			outputs[i].unmute();
		}
	}

	public void muteSingle(int input) {
		outputs[input].mute();
	}

	public void unmuteSingle(int input) {
		outputs[input].unmute();
	}
	
	public void updateInputData() {
		for (int i = 0; i < numberOfInputs; i++) {
			outputs[i].setInputData(inputs[i].getOutputData());
		}
	}

	public int getMixerInfosIndex() {
		return mixerInfosIndex;
	}

	public void setMixerInfosIndex(int mixerInfosIndex) {
		this.mixerInfosIndex = mixerInfosIndex;

		for (int i = 0; i < numberOfInputs; i++) {
			outputs[i].setMixerInfosIndex(mixerInfosIndex);
		}
	}

	public SourceDataLine getSourceDataLine() {
		return outputs[0].getSourceDataLineOut();
	}
}
