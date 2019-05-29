package io;

import com.google.devtools.common.options.Option;
import com.google.devtools.common.options.OptionsBase;

/**
 * Command-line options.
 */
public class SimulationOptions extends OptionsBase {

	@Option(
			name = "help",
			abbrev = 'h',
			help = "Prints usage info.",
			defaultValue = "false"
	)
	public boolean help;

//	@Option(
//			name = "limitTime",
//			abbrev = 'm',
//			help = "Maximum time of simulation (s).",
//			category = "startup",
//			defaultValue = "5.0"
//	)
//	public double limitTime;

	@Option(
			name = "deltaT",
			abbrev = 't',
			help = "Simulation delta time (s).",
			category = "startup",
			defaultValue = "0.00001"
	)
	public double deltaT;

	@Option(
			name = "printDeltaT",
			abbrev = 'p',
			help = "Simulation print delta time (s).",
			category = "startup",
			defaultValue = "0.5"
	)
	public double printDeltaT;

	@Option(
			name = "length",
			abbrev = 'l',
			help = "Length of room.",
			category = "startup",
			defaultValue = "20.0"
	)
	public double length;

	@Option(
			name = "width",
			abbrev = 'w',
			help = "Width of room.",
			category = "startup",
			defaultValue = "20.0"
	)
	public double width;

	@Option(
			name = "diameter",
			abbrev = 'd',
			help = "Diameter of outlet.",
			category = "startup",
			defaultValue = "1.2"
	)
	public double diameter;

//	@Option(
//			name = "normalK",
//			abbrev = 'k',
//			help = "Normal elastic constant (N/m).",
//			category = "startup",
//			defaultValue = "100000"
//	)
//	public double kN;
//
//	@Option(
//			name = "tangentK",
//			abbrev = 'i',
//			help = "Tangent elastic constant (N/m).",
//			category = "startup",
//			defaultValue = "200000"
//	)
//	public double kT;
}
