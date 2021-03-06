package main;

import audio.MixingDesk;
import midi.MidiController;

public class Main {

	public static void main(String[] args) {		
	
		Filemanager.getInstance();
	
		if (args.length == 2) {
			GlobalVariables.getInstance().setBufferSize(Integer.parseInt(args[0]));
			GlobalVariables.getInstance().setLatencyCompensation(Integer.parseInt(args[1]));
		} else {
			GlobalVariables.getInstance().setBufferSize((int) Filemanager.getInstance().variables.get("buffersize_soundcards"));
			GlobalVariables.getInstance().setLatencyCompensation((int) Filemanager.getInstance().variables.get("latency_compensation"));
		}
		
		Verkehrsampel.getInstance();
		
		MixingDesk.getInstance();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		MidiController.getInstance();
		
		GUI gui = new GUI();

		Thread mixingDeskUpdater = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					MixingDesk.getInstance().updateMixingDesk();
				}
			}
		});

		mixingDeskUpdater.start();
		
		 /*
		while (true) {
			// MixingDesk.getInstance().getMairlistMaster().setVolume(0.6);
			// MixingDesk.getInstance().getMicrophone1().setPhonesActivated(true);
			
			MixingDesk.getInstance().getMairlistChannel1().setVolume(1);
			MixingDesk.getInstance().getMairlistChannel2().setVolume(0.1);

			MixingDesk.getInstance().setSetRecording(true);
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

//			MixingDesk.getInstance().updateLines();

			// MixingDesk.getInstance().getMairlistMaster().setVolume(0.1);
			// MixingDesk.getInstance().getMicrophone1().setPhonesActivated(false);
			
			MixingDesk.getInstance().getMairlistChannel1().setVolume(0.1);
			MixingDesk.getInstance().getMairlistChannel2().setVolume(1);
			
			MixingDesk.getInstance().setSetRecording(false);
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		*/

	}

}
