# WebradioMixer

Configs:

- .JAR, nircmd-x64, folder "aircheck", WebradioMixerShortkey.jar and config.txt have to be in one folder
- Microphones must be set as playback devices with the phones as output
- mAirList Fernsteuerung: TCP/IP-Server with 127.0.0.1:9001

Starting WebradioMixer:

"java -jar WebradioMixer.jar BUFFERSIZE_SOUNDCARDS LATENCY_COMPENSATION"

- BUFFERSIZE_SOUNDCARDS typ. 64
- LATENCY_COMPENSATION in milliseconds typ. 650