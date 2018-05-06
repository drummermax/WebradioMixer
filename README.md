# WebradioMixer

**Configs:**

- WebradioMixer.jar, nircmd-x64, folder "aircheck", WebradioMixerPackageTransmitter.jar and config.txt have to be in one folder
- Microphones must be set as playback devices with the phones as output
- mAirList Fernsteuerung: TCP/IP-Server with 127.0.0.1:9001
- mAirlist Hintergrundscript: activate "Hintergrundscript/WebradioMixerHintergrundscript.mls" in mAirList
  - Change path of "WebradioMixerPackageTransmitter.jar" in all procedures (HDD partition and path)

**Starting WebradioMixer:**

"java -jar WebradioMixer.jar BUFFERSIZE_SOUNDCARDS LATENCY_COMPENSATION"

- BUFFERSIZE_SOUNDCARDS typ. 64
- LATENCY_COMPENSATION in milliseconds typ. 650

After that start mAirList.