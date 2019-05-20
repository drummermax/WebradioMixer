package audio;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

import main.GlobalVariables;

public class Input {
	private int mixerInfosIndex = 0;
	private String inputSettingsName = "Mikrofon";

	private AudioFormat audioFormat;
	private Mixer.Info[] mixerInfos;
	private Mixer mixerIn;
	private Line.Info[] lineInfosIn;
	private TargetDataLine targetDataLineIn;

	private byte[] outputData;
	private int outputDataLength;

	private double setVolume = 0, volume = 0; // range 0..1

	private boolean opened = false;

	public Input(AudioFormat audioFormat, int mixerInfosIndex) {
		this.audioFormat = audioFormat;
		this.mixerInfosIndex = mixerInfosIndex;

		updateLine(audioFormat);

		inputSettingsName = mixerInfos[mixerInfosIndex].toString().split("\\s+\\(")[0];

		float sampleRate = audioFormat.getSampleRate();
		int sampleSizeInBits = audioFormat.getSampleSizeInBits();
		int channels = audioFormat.getChannels();

		outputDataLength = GlobalVariables.getInstance().getBufferSize() * channels * sampleSizeInBits / 8; // 32
		outputData = new byte[outputDataLength];
	}

	public void updateLine(AudioFormat audioFormat) {
		this.audioFormat = audioFormat;

		if (opened)
			close();

		mixerInfos = AudioSystem.getMixerInfo();
		mixerIn = AudioSystem.getMixer(mixerInfos[mixerInfosIndex]);
		lineInfosIn = mixerIn.getTargetLineInfo();

		try {
			targetDataLineIn = (TargetDataLine) mixerIn.getLine(lineInfosIn[0]);
		} catch (LineUnavailableException e) {
			System.out.println("Input Line Number " + mixerInfosIndex + " not available!");
			e.printStackTrace();
		}

		open();
	}

	public void open() {
		try {
			targetDataLineIn.open(audioFormat);
		} catch (LineUnavailableException e) {
			System.out.println("Input Line Number " + mixerInfosIndex + " not available (open)!");
			e.printStackTrace();
		}
		targetDataLineIn.start();

		opened = true;
	}

	public void close() {
		targetDataLineIn.close();

		opened = false;
	}

	public double getVolume() {
		return volume;
	}

	public double getSetVolume() {
		return setVolume;
	}

	public void setVolume(double volume) {
		this.setVolume = volume;
		
		this.volume = volume;
		
//		System.out.println("cd " + MixingDesk.getInstance().getNircmdPath() + " && NIRCMD setsysvolume "
//				+ (int) (volume * 65535) + " \"" + inputSettingsName + "\"");

		//ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "cd " + MixingDesk.getInstance().getNircmdPath()
		//		+ " && NIRCMD setsysvolume " + (int) (volume * 65535) + " \"" + inputSettingsName + "\"");

		ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "cd " + System.getProperty("user.dir") + "\\nircmd-x64"
				+ " && NIRCMD setsysvolume " + (int) (volume * 65535) + " \"" + inputSettingsName + "\"");
		
		builder.redirectErrorStream(true);
		
		try {
			builder.start();
		} catch (IOException e) {
			System.out.println("NIRCMD didn't work!");
			e.printStackTrace();
		}

		this.volume = volume;
	}

	public void updateOutputData() {
		targetDataLineIn.read(outputData, 0, outputDataLength);
	}

	public byte[] getOutputData() {
		return outputData;
	}

	public int getOutputDataLength() {
		return outputDataLength;
	}

	public int getMixerInfosIndex() {
		return mixerInfosIndex;
	}

	public void setMixerInfosIndex(int mixerInfosIndex) {
		this.mixerInfosIndex = mixerInfosIndex;
	}

	public TargetDataLine getTargetDataLine() {
		return targetDataLineIn;
	}

}
