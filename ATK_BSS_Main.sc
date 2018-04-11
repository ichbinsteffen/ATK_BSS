/*
Date: January 12th 2018                       | 12.01.2018
Author: Steffen Ebner                         |
University of Music Karlsruhe                 | Hochschule für Musik Karlsruhe
Institute of Musicology and Music-Informatics | Institut für Musikwissenschaft und Musikinformatik
*/


ATK_BSS_Main {

	classvar <>instance;

	var <>audioRate;
	var <>sourceDictionary;
	var <>mediaDictionary;
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

		this.sourceDictionary = Dictionary.new();
		this.mediaDictionary = Dictionary.new();

		this.initOSCResponder();
		this.prepareServer();
		this.positionUnit = "xyz";
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

	add_SourceToDictionary
	{
		| id |
		if (sourceDictionary[id.asSymbol] == nil,
			{
				this.sourceDictionary.put(id, ATK_BSS_SourceObject.new());
				this.update_GUI;
			},
			{
				"The Source id % already exists. Creation aborted.".postf(id);
		});
	}

// =================================================================================

	add_MediaToDictionary
	{
		| id |
		if (mediaDictionary[id.asSymbol] == nil,
			{
				this.mediaDictionary.put(id, ATK_BSS_Media.new());
			},
			{
				"The Media id % already exists. Creation aborted.".postf(id);
			}
		);
	}

// =================================================================================

	update_GUI
	{
		sourceCountLabel.string_(this.sourceDictionary.size.asString);
	}
// =================================================================================

	more_info
	{
		"Coordinate Unit: %".postf(this.positionUnit);
		// and so on...
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
		sourceCountLabel = QStaticText(window, Rect(140,70,150,20)).string_(sourceDictionary.size.asString);

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

	free
	{
		this.atkDecoder.free;
		this.atkDecoderSynth.free;
		thisProcess.removeOSCRecvFunc(oscReceiverFunc);
	}

// =================================================================================

	// Should be called when Encoder/Decoder gets changed

	add_SynthDefs
	{
		SynthDef(\atkSource,
			{
				| out, rho, theta, phi, vol, mute, loop, sndBuffer, currentStartFrame |

				var signal = PlayBuf.ar(1, sndBuffer, 1, 1, currentStartFrame, loop, 2);
				var encoded = FoaEncode.ar(signal, this.atkEncoder);
				// encoded = FoaTransform.ar(encoded, 'pushX', posX);
				// encoded = FoaTransform.ar(encoded, 'pushY', posY);
				// encoded = FoaTransform.ar(encoded, 'pushZ', posZ);
				encoded = FoaTransform.ar(encoded, 'push', rho, theta, phi);
				Out.ar(out, encoded*vol);
			}
		).add;

		SynthDef(\atkDecode,
			{
				| out, in |

				Out.ar(out, FoaDecode.ar(In.ar(in, 4), this.atkDecoder));
			}
		).add;

		SynthDef(\myTestSynth,
			{
				| out, rho, theta, phi |

				var signal = Decay.ar(Impulse.ar(1.7), 0.1) * WhiteNoise.ar;
				var encoded = FoaEncode.ar(signal, this.atkEncoder);
				encoded = FoaTransform.ar(encoded, 'push', rho, theta, phi);
				Out.ar(out, encoded*0.03);
			}
		).add;
	}

// =================================================================================

	initOSCResponder
	{
		thisProcess.openUDPPort(this.port);

		oscReceiverFunc = {

			| msg, time, addr |

			if(msg[0] != '/status.reply')
			{
				var a = msg[0].asString.split(separator: $/);
				a = a[1..];

				if(a[0] == "spatdif")
				{
					// ==================================

					if(a[1] == "source") // Entity: source
					{
						var addressed_so = nil;

						if(sourceDictionary[a[2].asSymbol] == nil,
							{
								"Source Object % not found".postf(a[2]);
							},
							{
								addressed_so = sourceDictionary[a[2].asSymbol];

								case

								{a[3] == "associate-media"}
								{
									if (mediaDictionary[msg[1]] == nil,
										{
											"Media ID: % - Not found. Association aborted.".postf(msg[1]);
										},
										{
											addressed_so.set_media(mediaDictionary[msg[1]]);
										}
									);
								}

								{a[3] == "position"}
								{
									if (this.positionUnit == "xyz")
									{
										addressed_so.set_PositionCartesian(msg[1],msg[2],msg[3]);
									};

									if (this.positionUnit == "aed")
									{
										addressed_so.set_PositionSpherical(msg[1],msg[2],msg[3]);
									};
								}

								{a[3] == "present"} {addressed_so.present = msg[1];}
								{a[3] == "volume"}  {addressed_so.volume = msg[1];}
								{a[3] == "play"}    {addressed_so.play_Source(msg[1], msg[2])}
								{a[3] == "loop"}    {addressed_so.loop = msg[1];}
								{a[3] == "media"}   {addressed_so.set_SoundfilePath(msg[1]);};
							}
						);
					}; // end of source entity


					// ==================================

					if(a[1] == "media") // Entity: Media
					{
						if(this.mediaDictionary[a[2].asSymbol] == nil,
							{
								"Media Object % not found".postf(a[2]);
							},
							{
								var adressed_m = this.mediaDictionary[a[2].asSymbol];

								case
								{a[3] == "type"}        {adressed_m.type = msg[1];}
								{a[3] == "location"}    {adressed_m.location = msg[1];}
								{a[3] == "channel"}     {adressed_m.channel = msg[1];}
								{a[3] == "time-offset"} {adressed_m.timeOffset = msg[1];}
								{a[3] == "gain"}        {adressed_m.gain = msg[1];};
							}
						);
					};

					// ==================================

					if(a[1] == "loop") // Entity: Loop
					{

						"Loop handling not defied yet.".postln;

						/*
						var id = a[2].asInteger;

						case {a[3] == "type"}
						{
							this.mediaDictionary[id] = msg[1];
						}

						{a[3] == "location"}
						{
							this.mediaList[id] = msg[1];
						}

						{a[3] == "channel"} {"Not implemented yet.".postln;}
						{a[3] == "time-offset"} {"Not implemented yet.".postln;}
						{a[3] == "gain"} {"Not implemented yet.".postln;};
						*/
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
						{a[2] == "host-status"} {"[Info] Host: % - Status: %".postf(msg[1], msg[2]);}
						{a[2] == "client-status"}
						{
							if (msg[1] == \true, {
								{this.clientStatusLabel.string_("ONLINE").stringColor_(Color.green);}.defer;
							});

							if (msg[1] == \false, {
								{this.clientStatusLabel.string_("OFFLINE").stringColor_(Color.red);}.defer;
							});

						};
					};

					// ==================================

					if(a[1] == "scene") // Entity: scene
					{
						if(a[2] == "add-source") // Descriptor: add-source
						{
							{
								this.add_SourceToDictionary(msg[1]);
								// this.add_SourceToList(); // Deprecated
							}.defer;
						};

						if(a[2] == "add-media")
						{
							this.add_MediaToDictionary(msg[1]);
						};

					};

					// ==================================

					if(a[1] == "settings") // Entity: Settings
					{
						if(a[2] == "position-unit") // Descriptor: position-unit
						{
							if( (msg[1] == "xyz" || msg[1] == "aed" || msg[1] == "openGL"),
							{
								this.positionUnit = msg[1];
								if (msg[1]=="openGL") {"WARNING: Unit 2 (openGL) not supported yet!".postln;};
							},
							{
									"Position unit invalid. Choose between: xyz (Cartesian), aed (Spherical) and openGL".postln;};
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
Basic testing:

ATK_BSS_Main.instance = nil;
a = ATK_BSS_Main();

b = NetAddr.new("127.0.0.1", 1337);

b.sendMsg("/spatdif/scene/add-source", "Sound001");
b.sendMsg("/spatdif/scene/add-media", "Media001");
b.sendMsg("/spatdif/media/Media001/location", "C:/sounds/mono_glassmarimbachime.wav");

b.sendMsg("/spatdif/source/Sound001/associate-media", "Media001");
a.mediaDictionary["Media001".asSymbol].location

b.sendMsg("/spatdif/source/Sound001/position", 3.141, 1.575, 0.654);

b.sendMsg("/spatdif/info/host", "3D-HASE");
b.sendMsg("/spatdif/info/client-status", "true");
b.sendMsg("/spatdif/info/author", "Steffen");
*/