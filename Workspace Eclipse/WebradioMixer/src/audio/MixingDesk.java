package audio;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;

import com.sun.javafx.tk.quantum.MasterTimer;

import main.Filemanager;

public class MixingDesk {
	private static MixingDesk instance;

	private static Input microphone1, microphone2, mairlistChannel1, mairlistChannel2, mairlistPFL, mairlistCartwall,
			stdOut, mairlistMasterRecord;

	private static OutputCombined monitor, phones, mairlistMaster;

	private static AudioFormat audioFormat;
	private static int samplerate = 44100, numberOfBits = 16;

	private static String nircmdPath;

	private static boolean setRecording_microphones = false, setRecording_manually = false,
			isRecording_microphones = false, isRecording_manually = false;
	private static boolean speakingActive1 = false, speakingActive2 = false, monitoringToPhonesActive1 = false,
			monitoringToPhonesActive2 = false, pflActive = false, cartwallActive = false, monitorMuted = false,
			phonesStdWiedergabe = false;
	private static AudioInputStream aircheck_microphones, aircheck_manually;
	private static File aircheckFile_microphones, aircheckFile_manually;
	private static AudioFileFormat.Type aircheckFileType = AudioFileFormat.Type.WAVE;

	private static Thread recorderThread_microphones, recorderThread_manually;

	public static MixingDesk getInstance() {
		if (instance == null) {
			instance = new MixingDesk();

			instance.setSpeakingAktive(1, false);
			instance.setSpeakingAktive(2, false);
			instance.setPflActive(false);
			instance.setCartwallActive(false);
			instance.setPhonesStdWiedergabe(false);
			instance.setMonitorMuted(false);
		}
		return instance;
	}

	private MixingDesk() {
		nircmdPath = System.getProperty("user.dir") + "\\nircmd-x64";

		if (nircmdPath.contains("\\workspace\\WebradioMixer"))
			nircmdPath = nircmdPath.replace("\\workspace\\WebradioMixer", "");

		initLines();
	}

	public void initLines() {
		audioFormat = new AudioFormat(samplerate, numberOfBits, 2, true, true);

		microphone1 = new Input(audioFormat, (int) Filemanager.getInstance().variables.get("microphone 1"));
		microphone2 = new Input(audioFormat, (int) Filemanager.getInstance().variables.get("microphone 2"));
		mairlistChannel1 = new Input(audioFormat, (int) Filemanager.getInstance().variables.get("mairlist channel 1"));
		mairlistChannel2 = new Input(audioFormat, (int) Filemanager.getInstance().variables.get("mairlist channel 2"));
		mairlistPFL = new Input(audioFormat, (int) Filemanager.getInstance().variables.get("mairlist pfl"));
		mairlistCartwall = new Input(audioFormat, (int) Filemanager.getInstance().variables.get("mairlist cartwall"));
		stdOut = new Input(audioFormat, (int) Filemanager.getInstance().variables.get("stdout"));
		mairlistMasterRecord = new Input(audioFormat,
				(int) Filemanager.getInstance().variables.get("mairlist master record"));

		// Input[] mairlistMasterInputs = { microphone1, mairlistChannel1,
		// mairlistChannel2 };
		Input[] mairlistMasterInputs = { microphone1, microphone2, mairlistChannel1, mairlistChannel2,
				mairlistCartwall };
		boolean[] mairlistMasterInputsLatencyCompensation = { false, false, true, true, true };
		Input[] monitorInputs = { mairlistChannel1, mairlistChannel2, mairlistCartwall };
		boolean[] monitorInputsLatencyCompensation = { false, false, false };
		Input[] phonesInputs = { mairlistChannel1, mairlistChannel2, mairlistPFL, mairlistCartwall, stdOut, microphone1, microphone2 };
		boolean[] phonesInputsLatencyCompensation = { false, false, false, false, false, false, false };

		mairlistMaster = new OutputCombined(audioFormat,
				(int) Filemanager.getInstance().variables.get("mairlist master"), mairlistMasterInputs,
				mairlistMasterInputsLatencyCompensation);
		mairlistMaster.setVolume(1);

		int masterInitialVolumeProzent = (int) Filemanager.getInstance().variables
				.get("mairlist master initial volume");

		Double masterInitialVolume = new Double(masterInitialVolumeProzent);
		masterInitialVolume = masterInitialVolume / 100;

		mairlistMasterRecord.setVolume(masterInitialVolume); // avoid clipping
																// in master
																// channel

		monitor = new OutputCombined(audioFormat, (int) Filemanager.getInstance().variables.get("monitor"),
				monitorInputs, monitorInputsLatencyCompensation);
		monitor.setVolume(1);

		phones = new OutputCombined(audioFormat, (int) Filemanager.getInstance().variables.get("phones"), phonesInputs,
				phonesInputsLatencyCompensation);
		phones.setVolume(1);
		phones.setVolumeOfSingleInput(5, 0); //monitoring
		phones.setVolumeOfSingleInput(6, 0); //monitoring
	}

	public void updateLines() {
		audioFormat = new AudioFormat(samplerate, numberOfBits, 2, true, true);

		microphone1.updateLine(audioFormat);
		microphone2.updateLine(audioFormat);
		mairlistChannel1.updateLine(audioFormat);
		mairlistChannel2.updateLine(audioFormat);
		mairlistPFL.updateLine(audioFormat);
		mairlistCartwall.updateLine(audioFormat);
		stdOut.updateLine(audioFormat);
		mairlistMasterRecord.updateLine(audioFormat);

		mairlistMaster.updateLine(audioFormat);
		monitor.updateLine(audioFormat);
		phones.updateLine(audioFormat);
	}

	public void updateMixingDesk() {
		microphone1.updateOutputData();
		microphone2.updateOutputData();
		mairlistChannel1.updateOutputData();
		mairlistChannel2.updateOutputData();
		mairlistPFL.updateOutputData();
		mairlistCartwall.updateOutputData();
		stdOut.updateOutputData();
		// mairlistMasterRecord.updateOutputData(); //no update needed, because
		// the stream is directed into the writer

		mairlistMaster.updateInputData();
		monitor.updateInputData();
		phones.updateInputData();

		if (setRecording_microphones) {
			if (!isRecording_microphones) {
				isRecording_microphones = true;

				if (setRecording_manually) {
					setRecording_manually = false;
					mairlistMasterRecord.close();
				}

				System.out.println("started recording microphones");

				mairlistMasterRecord.open();

				aircheck_microphones = new AudioInputStream(mairlistMasterRecord.getTargetDataLine());

				SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
				Date now = new Date();
				String strDate = sdfDate.format(now);

				String aircheckFilePath = System.getProperty("user.dir") + "\\aircheck\\aircheck_" + strDate + ".wav";

				if (aircheckFilePath.contains("\\workspace\\WebradioMixer"))
					aircheckFilePath = aircheckFilePath.replace("\\workspace\\WebradioMixer", "");

				aircheckFile_microphones = new File(aircheckFilePath);

				recorderThread_microphones = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							AudioSystem.write(aircheck_microphones, aircheckFileType, aircheckFile_microphones);
						} catch (IOException e) {
							e.printStackTrace();
						}

						recorderThread_microphones = null;
					}
				});
				recorderThread_microphones.start();
			}
		} else {
			if (isRecording_microphones) {
				if (!isRecording_manually)
					mairlistMasterRecord.close();

				isRecording_microphones = false;

				System.out.println("finished recording microphones");

				// try {
				// aircheck.close();
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
			}
		}

		if (setRecording_manually) {
			if (!isRecording_manually) {
				isRecording_manually = true;

				if (setRecording_microphones) {
					setRecording_microphones = false;
					mairlistMasterRecord.close();
				}

				System.out.println("started recording manually");

				mairlistMasterRecord.open();

				aircheck_manually = new AudioInputStream(mairlistMasterRecord.getTargetDataLine());

				SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
				Date now = new Date();
				String strDate = sdfDate.format(now);

				String aircheckFilePath = System.getProperty("user.dir") + "\\aircheck\\record_" + strDate + ".wav";

				if (aircheckFilePath.contains("\\workspace\\WebradioMixer"))
					aircheckFilePath = aircheckFilePath.replace("\\workspace\\WebradioMixer", "");

				aircheckFile_manually = new File(aircheckFilePath);

				recorderThread_manually = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							AudioSystem.write(aircheck_manually, aircheckFileType, aircheckFile_manually);
						} catch (IOException e) {
							e.printStackTrace();
						}

						recorderThread_manually = null;
					}
				});
				recorderThread_manually.start();
			}
		} else {
			if (isRecording_manually) {
				if (!isRecording_microphones)
					mairlistMasterRecord.close();

				isRecording_manually = false;

				System.out.println("finished recording manually");

				// try {
				// aircheck.close();
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
			}
		}
	}

	private void activatePhonesPFL() {
		phones.muteSingle(0);
		phones.muteSingle(1);
		phones.unmuteSingle(2);
		phones.muteSingle(3);
		phones.muteSingle(4);
	}

	private void deactivatePhonesPFL() {
		phones.unmuteSingle(0);
		phones.unmuteSingle(1);
		phones.muteSingle(2);
		phones.unmuteSingle(3);
		phones.muteSingle(4);
	}

	private void activateMicrophone(int microphoneNumber) {
		if (microphoneNumber == 1) {
			mairlistMaster.setVolumeOfSingleInput(0, 1);
		} else if (microphoneNumber == 2) {
			mairlistMaster.setVolumeOfSingleInput(1, 1);
		}

		setMonitorMuted(true);
	}

	private void deactivateMicrophone(int microphoneNumber) {
		if (microphoneNumber == 1) {
			mairlistMaster.setVolumeOfSingleInput(0, 0);
		} else if (microphoneNumber == 2) {
			mairlistMaster.setVolumeOfSingleInput(1, 0);
		}
	}

	private void activateCartwall() {
		mairlistMaster.unmuteSingle(4);
		monitor.unmuteSingle(2);
		phones.unmuteSingle(3);
	}

	private void deactivateCartwall() {
		mairlistMaster.muteSingle(4);
		monitor.muteSingle(2);
		phones.muteSingle(3);
	}

	public void setPhonesStdWiedergabe(boolean phonesStdWiedergabe) {
		if (phonesStdWiedergabe && !MixingDesk.phonesStdWiedergabe) {
			activatePhonesStdWiedergabe();
		} else if (!phonesStdWiedergabe && MixingDesk.phonesStdWiedergabe) {
			activatePhonesNormal();
		}

		MixingDesk.phonesStdWiedergabe = phonesStdWiedergabe;
	}

	private void activatePhonesStdWiedergabe() {
		phones.muteSingle(0);
		phones.muteSingle(1);
		phones.muteSingle(2);
		phones.muteSingle(3);
		phones.unmuteSingle(4);
	}

	private void activatePhonesNormal() {
		phones.unmuteSingle(0);
		phones.unmuteSingle(1);
		phones.unmuteSingle(2);
		phones.unmuteSingle(3);
		phones.muteSingle(4);
	}

	public void toggleSpeakingActive(int microphoneNumber) {
		if (microphoneNumber == 1) {
			setSpeakingAktive(microphoneNumber, !speakingActive1);
		} else if (microphoneNumber == 2) {
			setSpeakingAktive(microphoneNumber, !speakingActive2);
		}
	}

	public void setSpeakingAktive(int microphoneNumber, boolean speakingActive) {
		if (microphoneNumber == 1) {
			if (speakingActive && !speakingActive1) {
				activateMicrophone(microphoneNumber);
				setSetRecording_microphones(true);
			} else if (!speakingActive && speakingActive1) {
				deactivateMicrophone(microphoneNumber);
				if (!speakingActive2)
					setSetRecording_microphones(false);
			}

			speakingActive1 = speakingActive;
		} else if (microphoneNumber == 2) {
			if (speakingActive && !speakingActive2) {
				activateMicrophone(microphoneNumber);
				setSetRecording_microphones(true);
			} else if (!speakingActive && speakingActive2) {
				deactivateMicrophone(microphoneNumber);
				if (!speakingActive1)
					setSetRecording_microphones(false);
			}

			speakingActive2 = speakingActive;
		}

		if (!speakingActive1 && !speakingActive2)
			setMonitorMuted(false);
	}
	
	public void toggleMonitoringToPhonesActive(int microphoneNumber) {
		if (microphoneNumber == 1) {
			setMonitoringToPhonesActive(microphoneNumber, !monitoringToPhonesActive1);
		} else if (microphoneNumber == 2) {
			setMonitoringToPhonesActive(microphoneNumber, !monitoringToPhonesActive2);
		}
	}
	
	public void setMonitoringToPhonesActive(int microphoneNumber, boolean monitoringToPhonesActive) {
		if (microphoneNumber == 1) {
			if (monitoringToPhonesActive) {
				phones.setVolumeOfSingleInput(5, 1);
			} else {
				phones.setVolumeOfSingleInput(5, 0);
			}
			
			monitoringToPhonesActive1 = monitoringToPhonesActive;
		} else if (microphoneNumber == 2) {
			if (monitoringToPhonesActive) {
				phones.setVolumeOfSingleInput(6, 1);
			} else {
				phones.setVolumeOfSingleInput(6, 0);
			}
			
			monitoringToPhonesActive2 = monitoringToPhonesActive;
		}
	}

	public void togglePflActive() {
		setPflActive(!pflActive);
	}

	public void setPflActive(boolean pflActive) {
		if (pflActive && !MixingDesk.pflActive) {
			activatePhonesPFL();
			System.out.println("SET PFL: " + pflActive);
		} else if (!pflActive && MixingDesk.pflActive) {
			deactivatePhonesPFL();
			System.out.println("SET PFL: " + pflActive);
		}

		MixingDesk.pflActive = pflActive;
	}

	public void toggleCartwallActive() {
		setCartwallActive(!cartwallActive);
	}

	public void setCartwallActive(boolean cartwallActive) {
		if (cartwallActive && !MixingDesk.cartwallActive) {
			activateCartwall();
		} else if (!cartwallActive && MixingDesk.cartwallActive) {
			deactivateCartwall();
		}

		MixingDesk.cartwallActive = cartwallActive;
	}

	public void toggleMonitorMuted() {
		setMonitorMuted(!monitorMuted);
	}

	public void setMonitorMuted(boolean monitorMuted) {
		if (monitorMuted && !MixingDesk.monitorMuted) {
			monitor.mute();
		} else if (!monitorMuted && MixingDesk.monitorMuted) {
			monitor.unmute();
		}

		MixingDesk.monitorMuted = monitorMuted;
	}

	public AudioFormat getAudioFormat() {
		return audioFormat;
	}

	public int getSamplerate() {
		return samplerate;
	}

	public void setSamplerate(int samplerate) {
		MixingDesk.samplerate = samplerate;
	}

	public int getNumberOfBits() {
		return numberOfBits;
	}

	public void setNumberOfBits(int numberOfBits) {
		MixingDesk.numberOfBits = numberOfBits;
	}

	public String getNircmdPath() {
		return nircmdPath;
	}

	public Input getMicrophone1() {
		return microphone1;
	}

	public Input getMicrophone2() {
		return microphone2;
	}

	public Input getMairlistChannel1() {
		return mairlistChannel1;
	}

	public Input getMairlistChannel2() {
		return mairlistChannel2;
	}

	public Input getMairlistPFL() {
		return mairlistPFL;
	}

	public Input getMairlistCartwall() {
		return mairlistCartwall;
	}

	public Input getStdOut() {
		return stdOut;
	}

	public Input getMairlistMasterRecord() {
		return mairlistMasterRecord;
	}

	public OutputCombined getMonitor() {
		return monitor;
	}

	public OutputCombined getPhone() {
		return phones;
	}

	public OutputCombined getMairlistMaster() {
		return mairlistMaster;
	}

	public void setSetRecording_microphones(boolean setRecording_microphones) {
		MixingDesk.setRecording_microphones = setRecording_microphones;
	}

	public boolean isSetRecording_microphones() {
		return setRecording_microphones;
	}

	public void setSetRecording_manually(boolean setRecording_manually) {
		MixingDesk.setRecording_manually = setRecording_manually;
	}

	public boolean isSetRecording_manually() {
		return setRecording_manually;
	}

	public boolean isSpeakingActive1() {
		return speakingActive1;
	}

	public boolean isSpeakingActive2() {
		return speakingActive2;
	}

	public boolean isMonitoringToPhonesActive1() {
		return monitoringToPhonesActive1;
	}

	public boolean isMonitoringToPhonesActive2() {
		return monitoringToPhonesActive2;
	}

	public boolean isMonitorMuted() {
		return monitorMuted;
	}

	public boolean isPhonesStdWiedergabe() {
		return phonesStdWiedergabe;
	}

	public boolean isCartwallActive() {
		return cartwallActive;
	}
}
