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

	@Option(
			name = "limitTime",
			abbrev = 'm',
			help = "Maximum time of simulation (s).",
			category = "startup",
			defaultValue = "5.0"
	)
	public double limitTime;

	@Option(
			name = "numberOfPedestrians",
			abbrev = 'n',
			help = "Number of pedestrians.",
			category = "startup",
			defaultValue = "10"
	)
	public int N;

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
			help = "Door width.",
			category = "startup",
			defaultValue = "1.2"
	)
	public double diameter;

	@Option(
			name = "normalK",
			abbrev = 'k',
			help = "Normal elastic constant (N/m).",
			category = "startup",
			defaultValue = "120000"
	)
	public double kN;

	@Option(
			name = "tangentK",
			abbrev = 'i',
			help = "Tangent elastic constant (kg/m/s).",
			category = "startup",
			defaultValue = "240000"
	)
	public double kT;

	@Option(
			name = "speed",
			abbrev = 's',
			help = "Desired speed [0.8 - 6.0 m/s].",
			category = "startup",
			defaultValue = "2.6"
	)
	public double desiredSpeed;

	@Option(
			name = "A",
			abbrev = 'a',
			help = "Social force parameter multiplying exponential term (N).",
			category = "startup",
			defaultValue = "2000"
	)
	public double A;

	@Option(
			name = "B",
			abbrev = 'b',
			help = "Social force parameter dividing inside exponential term (m).",
			category = "startup",
			defaultValue = "0.08"
	)
	public double B;

	@Option(
			name = "tau",
			abbrev = 'u',
			help = "Driving force denominator (s).",
			category = "startup",
			defaultValue = "0.5"
	)
	public double τ;
}
