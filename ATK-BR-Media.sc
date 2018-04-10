ATK_BSS_Media {

	var <>id;
	var <>type;
	var <>location;
	var <>loop;
	var <>gain;
	var <>channel;
	var <>timeOffset;

	var loop;

	init
	{
		this.type = "none";
		this.location = nil;
		this.channel = 1;
		this.timeOffset = 0.0;
		this.gain = 1.0;
		this.loop = ATK_BSS_Loop();
	}
}

