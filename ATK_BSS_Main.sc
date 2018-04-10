/*
Date: January 12th 2018                       | 12.01.2018
Author: Steffen Ebner                         |
University of Music Karlsruhe                 | Hochschule für Musik Karlsruhe
Institute of Musicology and Music-Informatics | Institut für Musikwissenschaft und Musikinformatik

ATK_BSS_Main.instance = nil;
a = ATK_BSS_Main();
a.add_SourceToList();
a.sourceObjects[0].set_SoundfilePath("C:/Users/admin/Music/TrajectoryEditor_Audio/mono_glassmarimbachime.wav");
a.sourceObjects[0].play_Source(0,true);
a.sourceObjects[0].set_PositionSpherical(1.5707 * 1.0, 0.2 * 1.5707, 1.5707* 1.3);
a.sourceObjects[0].init_Synth();
a.sourceObjects[0].pause_Source();
a.sourceObjects[0].synth.free;
a.free
*/

// =================================================================================
ATK_BSS_Main {

	classvar <>instance;

	var <>audioRate;
	var <>sourceObjects;
	var <>mediaList;
	var <>atkDecoder, <>atkEncoder, <>atkDecoderSynth;
	var <>mix, <>bus;
	var <>scServer;
	var <>window;
	var <>sourceCountLabel;
	var <>encoderOptionsDropdown;
	var <>samplingRateDropdown;
	var <>portTextfield, <>portApplyButton;

	var <>set_Path, <>set_Volume, <>play_Source, <>pause_Source, <>set_SourceLoop, <>set_SourcePosition, <>set_ClientStatus;
	var <>set_ClientName, <>add_Source, <>set_SourcePosCartesian, <>set_SourcePosSpherical;
	var <>testingDef;
	var <>positionUnit;

	var <>crtlTestSynth;

	var <>clientStatusLabel, <>clientNameLabel;

	var <>port;
	classvar <>oscPort;
	var <>oscReceiverFunc;

// =================================================================================

	*new
	{
		if (instance.isNil) {
			instance = super.new.init();
		}
		^instance;
	}

// =================================================================================

	prepareServer
	{
		Server.default.waitForBoot {

			this.initATKEncoder();
			this.initATKDecoder();
			{
				//Server.default.sync();
				this.add_SynthDefs();
				this.bus = Bus.audio(Server.default, 4);

				this.makeServerWindow();

				this.atkDecoderSynth = Synth(\atkDecode, [\out, 0,  \in, this.bus]);
			}.defer(1.5);
		};
	}

// =================================================================================

	init
	{
		this.port = 1337;
		this.sourceObjects = List.new();
		this.mediaList = List.new();
		//this.initOSCDefs();
		this.initOSCResponder();
		this.prepareServer();
		this.positionUnit = 0;

	}

// =================================================================================

	initATKDecoder
	{
		this.atkDecoder = FoaDecoderKernel.newCIPIC;
	}

// =================================================================================

	initATKEncoder
	{
		this.atkEncoder = FoaEncoderMatrix.newOmni;
	}

// =================================================================================

	initOSCDefs
	{
		"The function initOSCDefs is now deprecated. It should no longer be called. Use initOSCResponder instead.".postln;
		/*
		set_ClientStatus = OSCdef(\clientStatus, { |msg,time,addr,recvPort|
			{
				this.clientStatusLabel.string_(msg[1]).stringColor_(Color.green)
			}.defer;
		},'/client/status', recvPort:port);

		set_ClientName = OSCdef(\clientName, { |msg,time,addr,recvPort|
			{
				this.clientNameLabel.string_(msg[1]);
				this.update_GUI();
			}.defer;
		},'/client/name', recvPort:port);

		set_Path = OSCdef(\setPath, { |msg,time,addr,recvPort|
			{sourceObjects[msg[1]].set_SoundfilePath(msg[2])}.defer;
		}, '/source/path', recvPort:port);

		set_Volume = OSCdef(\setSourceVolume, { |msg,time,addr,recvPort|
			sourceObjects[msg[1]].setVolume(msg[2]);
		},'/source/volume', recvPort:port);

		play_Source = OSCdef(\playSource, { |msg,time,addr,recvPort|
			sourceObjects[msg[1]].play_Source(msg[2],msg[3]);
		},'/source/play', recvPort:port);

		pause_Source = OSCdef(\pauseSource, { |msg,time,addr,recvPort|
			sourceObjects[msg[1]].pause_Source();
		},'/source/pause', recvPort:port);

		set_SourceLoop = OSCdef(\loopSource, { |msg,time,addr,recvPort|
			sourceObjects[msg[1]].set_loop(msg[2]);
		},'/source/loop', recvPort:port);

		set_SourcePosition = OSCdef(\setSourcePosition, { |msg,time,addr,recvPort|
			sourceObjects[msg[1]].changePosition(msg[2]);
		},'/source/position', recvPort:port);

		add_Source = OSCdef(\addSource, { |msg,time,addr,recvPort|
			{
				this.add_SourceToList();
				this.update_GUI();
			}.defer;
		},'/addSource', recvPort:port);

		set_SourcePosCartesian = OSCdef(\sourcePositionCART, { |msg,time,addr,recvPort|
			{
				sourceObjects[msg[1]].set_PositionXYZ(msg[2],msg[3],msg[4]);
			}.defer;
		},'/source/positionCartesian', recvPort:port);


		set_SourcePosSpherical = OSCdef(\sourcePositionSPHE, { |msg,time,addr,recvPort|
			{
				sourceObjects[msg[1]].set_PositionSpherical(msg[2],msg[3],msg[4]);
			}.defer;
		},'/source/positionSpherical', recvPort:port);

		crtlTestSynth = OSCdef(\testSynth, { |msg, time, addr, recvPort |
				sourceObjects[msg[1]].set_PositionSpherical(msg[2], msg[3], msg[4]);
				(msg[2].asString + " " + msg[3].asString + " " + msg[4].asString).postln;
		}, '/testSynth', recvPort:port);
		*/
	}

// =================================================================================

	add_SourceToList
	{
		this.sourceObjects.add( ATK_BSS_SourceObject.new() );
		this.update_GUI;
	}

// =================================================================================

	update_GUI
	{
		sourceCountLabel.string_(this.sourceObjects.size.asString);
	}

// =================================================================================

	makeServerWindow
	{
		window = QWindow("ATK Server", Rect(80,80,450,100), false, true).onClose_({this.free});

		QView(window,Rect(200,4,1,92)).background_(Color.gray);

		QStaticText(window, Rect(10,10,150,20)).string_("Client name: ");
		clientNameLabel = QStaticText(window, Rect(80,10,150,20)).string_("N/A");

		QStaticText(window, Rect(10,40,150,20)).string_("Client status: ");
		clientStatusLabel = QStaticText(window, Rect(80,40,150,20)).string_("Offline").stringColor_(Color.grey);

		QStaticText(window, Rect(10,70,150,20)).string_("Number of Sources: ");
		sourceCountLabel = QStaticText(window, Rect(140,70,150,20)).string_(sourceObjects.size.asString);

		QStaticText(window, Rect(220,10,50,20)).string_("Port: ");
		portTextfield = QTextField(window, Rect(320,10,44,20)).string_("1337");
		portApplyButton = QButton(window, Rect(290,10,30,20)).string_("Set");
		portApplyButton.action_({this.set_Port()});

		QStaticText(window, Rect(220,70,80,20)).string_("Decoder: ");
		encoderOptionsDropdown = QPopUpMenu(window, Rect(290,70,150,20));
		encoderOptionsDropdown.items_(["CIPIC","Spherical","5.0"]);
		encoderOptionsDropdown.action = {this.changeATKDecoder()};

		QStaticText(window, Rect(220,40,80,20)).string_("Audio Rate:");
		samplingRateDropdown = QPopUpMenu(window, Rect(290,40,150,20));
		samplingRateDropdown.items_(["44100","44100","44100"]);
		samplingRateDropdown.action_({this.set_AudioRate();});

		window.front();
	}

// =================================================================================

	set_Port
	{
		this.freeOSCDefs();
		this.initOSCDefs();
		this.port = portTextfield.value.asInteger;
		("Port has been set to: " + this.port.asString).postln;
	}

// =================================================================================

	changeATKDecoder
	{
		switch (this.encoderOptionsDropdown.value,
			0,{ this.atkDecoder = FoaDecoderKernel.newCIPIC; },
			1,{ this.atkDecoder = FoaDecoderKernel.newSpherical;},
			2,{ this.atkDecoder = FoaDecoderMatrix.new5_0; "5.0 Decoder loaded".postln;}
		);
	}

// =================================================================================

	set_AudioRate
	{
		var ddVal = samplingRateDropdown.value;
		switch(ddVal,
			0, {this.audioRate = 44100}, // Server needs to reboot when changing SR,
			1, {this.audioRate = 44100}, // therefore everything needs to be re-initialized
			2, {this.audioRate = 44100}  // NOT IMPLEMENTED YET !!!
		);
		Server.default.options.sampleRate = this.audioRate; // Not working
		//Server.default.reboot;
		("Server Audio-Rate: " + Server.default.options.sampleRate.asString).postln;
	}

// =================================================================================

	freeOSCDefs
	{
		"Since OSCdefs are not used any more, this function goes unused.".postln;
		/*
		set_Path.free;
		set_Volume.free;
		play_Source.free;
		set_SourceLoop.free;
		set_SourcePosition.free;
		set_ClientStatus.free;
		set_ClientName.free;
		add_Source.free;
		set_SourcePosCartesian.free;
		set_SourcePosSpherical.free;
		testingDef.free;
		*/
	}

// =================================================================================

	free
	{
		this.freeOSCDefs;
		//this.atkEncoder.free;
		this.atkDecoder.free;
		this.atkDecoderSynth.free;
		thisProcess.removeOSCRecvFunc(oscReceiverFunc);
		thisProcess.closeUDPPort(this.port);
		//sourceObjects.do{|src| src.free;};
	}

// =================================================================================

	// Should be called when Encoder/Decoder gets changed

	add_SynthDefs
	{
		SynthDef(\atkSource,
			{ | out, rho, theta, phi, vol, mute, loop, sndBuffer, currentStartFrame |
			var signal = PlayBuf.ar(1, sndBuffer, 1, 1, currentStartFrame, loop, 2);
			var encoded = FoaEncode.ar(signal, this.atkEncoder);
			// encoded = FoaTransform.ar(encoded, 'pushX', posX);
			// encoded = FoaTransform.ar(encoded, 'pushY', posY);
			// encoded = FoaTransform.ar(encoded, 'pushZ', posZ);
			encoded = FoaTransform.ar(encoded, 'push', rho, theta, phi);
			Out.ar(out, encoded*vol);
		}).add;

		SynthDef(\atkDecode, { |out, in|
			Out.ar(out, FoaDecode.ar(In.ar(in, 4), this.atkDecoder));
		}).add;

		SynthDef(\myTestSynth,
			{ | out, rho, theta, phi |
			var signal = Decay.ar(Impulse.ar(1.7), 0.1) * WhiteNoise.ar;
			var encoded = FoaEncode.ar(signal, this.atkEncoder);
			encoded = FoaTransform.ar(encoded, 'push', rho, theta, phi);
			Out.ar(out, encoded*0.03);
		}).add;
	}
// =================================================================================

	initOSCResponder
	{
		thisProcess.openUDPPort(this.port);

		oscReceiverFunc = { |msg, time, addr|

			if(msg[0] != '/status.reply')
			{
				var a = msg[0].asString.split(separator: $/);
				a = a[1..];

				if(a[0] == "spatdif")
				{
					// ==================================

					if(a[1] == "source") // Entity: source
					{
						var id = a[2].asInteger; // the ID / Name of the source

						case

						{a[3] == "soundfile"} {
							{sourceObjects[id].set_SoundfilePath(msg[1])}.defer;
							"changing Soundfile".postln;
						}
						{a[3] == "position"} {
							//sourceObjects[id].set_SourcePosSpherical(msg[1],msg[2],msg[3]);

							if (this.positionUnit == 0)
							{
								sourceObjects[id].set_PositionCartesian(msg[1],msg[2],msg[3]);
							};
						}
						{a[3] == "present"} {
							sourceObjects[id].present(msg[1]);
							"Source presence".postln;
						}
						{a[3] == "volume"} {
							sourceObjects[id].setVolume(msg[1]);
							"set source volume".postln;
						}
						{a[3] == "loop"} {
							sourceObjects[id].set_loop(msg[1]);
							"loop source".postln;
						}
						{a[3] == "play"} {
							sourceObjects[id].play_Source(msg[1]);
							"playing source".postln;
						}
						{a[3] == "pause"} {
							sourceObjects[id].pause_Source(msg[1]);
							"Pausing source".postln;
						};
					};

					// ==================================

					if(a[1] == "media") // Entity: Media
					{
						var id = a[2].asInteger;

						case {a[3] == "type"} {
							/*
							stream
							file
							live
							none (default)
							*/
						}
						{a[3] == "location"} {this.mediaList[id] = msg[1];}
						{a[3] == "channel"} {}
						{a[3] == "time-offset"} {}
						{a[3] == "gain"} {};
					};

					// ==================================

					if(a[1] == "loop") // Entity: Loop
					{
						var id = a[2].asInteger;

						case {a[3] == "type"}
						{
							/*
							stream
							file
							live
							none (default)
							*/
							this.mediaList[id] = msg[1];
						}

						{a[3] == "location"}
						{
							this.mediaList[id] = msg[1];
						}

						{a[3] == "channel"} {"Not implemented yet.".postln;}
						{a[3] == "time-offset"} {"Not implemented yet.".postln;}
						{a[3] == "gain"} {"Not implemented yet.".postln;};
					};

					// ==================================

					if(a[1] == "info") // Entity: info
					{
						case
						{a[2] == "author"} {"[Info] Author: %".postf(msg[1]);}
						{a[2] == "host"}
						{
							{
								this.clientNameLabel.string_(msg[1]);
								this.update_GUI();
							}.defer;
						}
						{a[2] == "hoststatus"} {"[Info] Host: % - Status: %".postf(msg[1], msg[2]);};
					};

					// ==================================

					if(a[1] == "scene") // Entity: scene
					{
						if(a[2] == "addSource")
						{
							{
								this.add_SourceToList();
								this.update_GUI();
							}.defer;
						};
					};

					// ==================================

					if(a[1] == "settings") // Entity: Settings
					{
						if(a[2] == "position-unit")
						{
							if( (msg[1] == 0 || msg[1] == 1 || msg[1] == 2),
							{
								this.positionUnit = msg[1];
								if (msg[1]==2) {"WARNING: Unit 2 (openGL) not supported yet!".postln;};
							},
							{"Position unit invalid. You can choose between: 0 (Cartesian), 1 (Spherical) and 3 (openGL)".postln;};
							);
						}
					};

					// ==================================

					//
					// other spatdif entities?
					//

				};
			};
		}; // oscReceiverFunc
		thisProcess.addOSCRecvFunc(oscReceiverFunc);
	} // oscResponder

} // class

// =================================================================================


/*

ATK_BSS_Main.instance = nil;
a = ATK_BSS_Main();
a.add_SourceToList();
a.free
a.sourceObjects[0].change_PositionSpherical(1.5707* 1.0, 1.5707* 0.0 ,  1.5707* 0.0);

b = NetAddr.new("127.0.0.1", 1337);

b.sendMsg("/spatdif/scene/addSource", "true");
b.sendMsg("/spatdif/source/0/position", 3.141, 1.575, 0.654);
b.sendMsg("/spatdif/source/0/soundfile", "C:/sounds/first.wav");
b.sendMsg("/spatdif/info/host", "Trajectory-Editor");
b.sendMsg("/spatdif/info/author", "Steffen");

*/

