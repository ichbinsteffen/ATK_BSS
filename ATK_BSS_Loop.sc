ATK_BSS_Loop {

	var <>type;
	var <>points;
	var <>waitTime;

	init
	{
		this.type = "none";
		this.points = Array.new(maxSize: 2);
		this.points[0] = 0.0;
		// this.points[1] = "eof";
		this.waitTime = 0;
	}

	set_type
	{
		| t |

		if (t == "none" || t = "repeat" || t = "palindrome")
		{
			this.type = t
		}
	}

	set_points
	{
		| startPoint, endPoint |

		this.points[0] = startPoint;
		this.points[1] = endPoint;
	}

	set_waitTime
	{
		|wt|

		this.waitTime = wt;
	}
}