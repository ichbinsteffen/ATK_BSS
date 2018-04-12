/*
Date: January 12th 2018                       | 12.01.2018
Author: Steffen Ebner                         |
University of Music Karlsruhe                 | Hochschule für Musik Karlsruhe
Institute of Musicology and Music-Informatics | Institut für Musikwissenschaft und Musikinformatik
*/

ATK_BSS_SourceObject {

	var <>id;
	var <>sndBuffer, <>sndFilePath, <>numFrames;
	var <>posCartesian, <>mute, <>volume, <>loop, <>currentStartFrame;
	var <>atkEncoderMatrix, <>atkEncoder;
	var <>synthDef, <>synth, <>out;
	var <>associated_media;
	var <>present;

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
	{ |withID|
		currentID = currentID + 1;
		^super.new.init(withID)
	}

// =================================================================================

	init
	{ |withID|
		this.id = withID;
		this.atkEncoder = FoaEncoderMatrix.newOmni;
		this.sndBuffer = Buffer.new();
		this.currentStartFrame = 0;
		this.loop = 0;
		this.volume = 0.8;
		this.init_Synth();
		//this.play_TEST_SYNTH(0.0,0.3,1.0);
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

	set_SoundfilePath //////// DEPRECATED
	{
		| sndPath |

		this.sndFilePath = sndPath;
		this.sndFilePath.postln;
		this.update_Buffer();
	}

	// USE set_media INSTEAD:
	set_media
	{
		| mediaObject |

		this.associated_media = mediaObject;
		this.sndFilePath = associated_media.location;
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
		"Playing Source-Object: % from sec: %; Frame: % \n".postf(this.id, startSec, startFrame);
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
				\out, ATK_BSS_Main.instance.bus,
				\sndBuffer, this.sndBuffer,
				\vol, this.volume,
				\currentStartFrame, this.currentStartFrame
			]
		);
	}

// =================================================================================

}