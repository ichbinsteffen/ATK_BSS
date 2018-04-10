/*
Date: January 12th 2018                       | 12.01.2018
Author: Steffen Ebner                         |
University of Music Karlsruhe                 | Hochschule für Musik Karlsruhe
Institute of Musicology and Music-Informatics | Institut für Musikwissenschaft und Musikinformatik

ATKServer.instance = nil;
a = ATKServer();
a.add_SourceToList();
a.sourceObjects[0].set_SoundfilePath("C:/Users/admin/Music/TrajectoryEditor_Audio/mono_glassmarimbachime.wav");
a.sourceObjects[0].play_Source(0,true);
a.sourceObjects[0].set_PositionSpherical(1.5707 * 1.0, 0.2 * 1.5707, 1.5707* 1.3);
a.sourceObjects[0].init_Synth();
a.sourceObjects[0].pause_Source();
a.sourceObjects[1].synth.free;
a.free
*/


ATK_BSS_SourceObject {

	var <>id;
	var <>sndBuffer, <>sndFilePath, <>numFrames;
	var <>posCartesian, <>mute, <>volume, <>loop, <>currentStartFrame;
	var <>atkEncoderMatrix, <>atkEncoder;
	var <>synthDef, <>synth, <>out;

	classvar currentID;
	classvar sampleRate;

// =================================================================================

	*initClass
	{
		currentID = -1;
		sampleRate = 44100;
	}

// =================================================================================

	*new
	{
		currentID = currentID + 1;
		^super.new.init()
	}

// =================================================================================

	init
	{
		this.atkEncoder = FoaEncoderMatrix.newOmni;
		this.sndBuffer = Buffer.new();
		this.currentStartFrame = 0;
		this.loop = 0;
		this.volume = 0.8;
		//this.init_Synth();
		this.play_TEST_SYNTH(0.0,0.3,1.0);
	}

// =================================================================================

	play_TEST_SYNTH
	{
		| r, t, p |

		this.synth = Synth.new(\myTestSynth,
			[
				\out, ATK_BSS_Main.instance.bus,
				\rho, r,
				\theta, t,
				\phi, p
			]
		);
	}

// =================================================================================

	set_StartFrame
	{
		| startSec |

		this.currentStartFrame = (this.numFrames / Server.default.sampleRate) * startSec;
	}

// =================================================================================

	set_SoundfilePath
	{
		| sndPath |

		this.sndFilePath = sndPath;
		this.sndFilePath.postln;
		this.update_Buffer();
	}

// =================================================================================

	update_Buffer
	{
		//this.sndBuffer = Buffer.cueSoundFile(Server.default, this.sndFilePath, 0, 1, 44100*4);
		this.sndBuffer = Buffer.read(Server.default, this.sndFilePath);
		this.synth.set(\buffer, this.sndBuffer);
	}

// =================================================================================

	set_PositionCartesian
	{
		| x, y, z |

		var spherical = Cartesian.new(x,y,z).asSpherical;
		this.synth.set(\rho, spherical.rho);
		this.synth.set(\theta, spherical.theta);
		this.synth.set(\phi, spherical.phi);
	}

// =================================================================================

	set_PositionSpherical
	{
		| rho, theta, phi |

		// 1.rho = Distance,
		// 2.theta = azimut,
		// 3.phi = elevation
		this.synth.set(\rho, rho);
		this.synth.set(\theta, theta);
		this.synth.set(\phi, phi);
	}

// =================================================================================

	play_Source
	{
		| startSec, loop |

		var startFrame = startSec * 44100;
		this.numFrames = this.sndBuffer.numFrames;
		("Soundfile has: " + this.sndBuffer.numFrames + "Frames").postln;
		("Playing From frame: "+ startFrame).postln;
		this.synth.set(\currentStartFrame, startFrame);
		this.synth.set(\loop, loop);
		this.synth.set(\sndBuffer, this.sndBuffer);
		this.synth.run(true);
	}

// =================================================================================

	pause_Source
	{
		this.synth.run(false);
	}

// =================================================================================

	set_loop
	{
		| loop | // a boolean: true = loop it

		this.loop = loop;
		this.synth.set(\loop, loop)
	}

// =================================================================================

	init_Synth
	{
		this.synth = Synth.newPaused(\atkSource,
			[
				\out, ATK_BSS.instance.bus,
				\sndBuffer, this.sndBuffer,
				\vol, this.volume,
				\currentStartFrame, this.currentStartFrame
			]
		);
	}

// =================================================================================

}