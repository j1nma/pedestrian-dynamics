import algorithms.neighbours.SocialForceModel;
import com.google.devtools.common.options.OptionsParser;
import io.SimulationOptions;
import models.Particle;
import models.ParticleGenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class App {

	private static final String OUTPUT_DIRECTORY = "./output";
	private static final String OVITO_FILE = OUTPUT_DIRECTORY + "/ovito_file";
	private static final String FLOW_FILE_NAME = OUTPUT_DIRECTORY + "/flow_file";

	private static final double MIN_PARTICLE_DIAMETER = 0.5;
	private static final double MAX_PARTICLE_DIAMETER = 0.58;
	private static final double PARTICLE_MASS = 80;

	private static final double LENGTH_DIVIDED_BY = 2;

	private static ParticleGenerator particleGenerator = new ParticleGenerator();

	public static void main(String[] args) throws IOException {

		// Create output directory
		boolean createdOutputDirectory = new File(OUTPUT_DIRECTORY).mkdirs();
		if (!createdOutputDirectory) {
			System.out.println("Could not creating output directory.");
			System.exit(1);
		}

		// Parse command line options
		OptionsParser parser = OptionsParser.newOptionsParser(SimulationOptions.class);
		parser.parseAndExitUponError(args);
		SimulationOptions options = parser.getOptions(SimulationOptions.class);
		assert options != null;
		if (options.N <= 0
				|| options.deltaT <= 0
				|| options.printDeltaT <= 0
				|| options.length <= 0
				|| options.width <= 0
				|| options.diameter < 0
				|| options.kN <= 0
				|| options.kT <= 0) {
			printUsage(parser);
		}

		if (!parser.containsExplicitOption("deltaT")) {
			options.deltaT = 0.01 * Math.sqrt(PARTICLE_MASS / options.kN);
			System.out.println("Delta t: " + options.deltaT);
		}

		runAlgorithm(
				particleGenerator.generate(options.N, options.length, options.width,
						MIN_PARTICLE_DIAMETER, MAX_PARTICLE_DIAMETER, PARTICLE_MASS,
						options.desiredSpeed, LENGTH_DIVIDED_BY),
				options.deltaT,
				options.printDeltaT,
				options.length,
				options.width,
				options.diameter,
				options.kN,
				options.kT,
				options.desiredSpeed,
				options.A,
				options.B,
				options.τ
		);
	}

	private static void runAlgorithm(List<Particle> particles,
	                                 double deltaT,
	                                 double printDeltaT,
	                                 double length,
	                                 double width,
	                                 double diameter,
	                                 double kN,
	                                 double kT,
	                                 double desiredSpeed,
	                                 double A,
	                                 double B,
	                                 double τ) throws IOException {

		FileWriter fw = new FileWriter(String.valueOf(Paths.get(OVITO_FILE + "_DS=" + desiredSpeed + ".txt")));
		BufferedWriter writeFileBuffer = new BufferedWriter(fw);

		FileWriter fw3 = new FileWriter(String.valueOf(Paths.get(FLOW_FILE_NAME + "_DS=" + desiredSpeed + ".txt")));
		BufferedWriter flowFileBuffer = new BufferedWriter(fw3);

		SocialForceModel.run(
				particles,
				writeFileBuffer,
				flowFileBuffer,
				deltaT,
				printDeltaT,
				length,
				width,
				diameter,
				kN,
				kT,
				desiredSpeed,
				A,
				B,
				τ,
				App.LENGTH_DIVIDED_BY
		);

		writeFileBuffer.close();
		flowFileBuffer.close();
	}

	private static void printUsage(OptionsParser parser) {
		System.out.println("Usage: java -jar pedestrian-dynamics-1.0-SNAPSHOT.jar OPTIONS");
		System.out.println(parser.describeOptions(Collections.emptyMap(),
				OptionsParser.HelpVerbosity.LONG));
	}

}
