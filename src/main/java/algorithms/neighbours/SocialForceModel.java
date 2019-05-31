package algorithms.neighbours;

import models.Particle;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class SocialForceModel {

	private static double boxHeight = 20.0;
	private static double boxWidth = 20.0;
	private static double boxDiameter = 1.2;

	private static final double MAX_DIAMETER = 0.58;
	private static final double MAX_INTERACTION_RADIUS = MAX_DIAMETER / 2;

	private static double lengthDividedBy = 2;

	// Initial State
	private static double time = 0.0;

	// Each particle's integration method
	private static final Map<Particle, IntegrationMethodWithNeighbours> particleIntegrationMethods = new HashMap<>();

	public static void run(
			List<Particle> particles,
			BufferedWriter buffer,
			BufferedWriter flowFileBuffer,
			double dt,
			double printDeltaT,
			double length,
			double width,
			double diameter,
			double kN,
			double kT,
			double desiredSpeed,
			double A,
			double B,
			double τ,
			double LDividedBy) throws IOException {

		boxHeight = length;
		boxWidth = width;
		boxDiameter = diameter;
		lengthDividedBy = LDividedBy;

//		Particle p1 = particles.get(0);
//		Particle p2 = particles.get(1);
//		p1.setPosition(new Vector2D(0.25, 13));
//		p1.setRadius(0.25);
//		p2.setRadius(0.25);
//		p2.setPosition(new Vector2D(0.25, 12));
//		p1.setVelocity(new Vector2D(0, 0));
//		p2.setVelocity(new Vector2D(0, 0));
//		List<Particle> test2particles = new ArrayList<>();
//		test2particles.add(p1);
//		test2particles.add(p2);
//		particles = test2particles;

		// Write first frame to buffer
		printFrame(buffer, particles);

		// Print frame
		int currentFrame = 1;
		int printFrame = (int) Math.ceil(printDeltaT / dt);

		// Save N for 'Particles Left;
		int N = particles.size();

		// Particles out of room but still moving
		List<Particle> outOfRoom = new LinkedList<>();

		long startTime = System.currentTimeMillis();

		while (outOfRoom.size() < N) {
			time += dt;

			// Calculate neighbours
			CellIndexMethod.run(particles,
					(boxHeight * (1 + 1 / lengthDividedBy)),
					(int) Math.floor((boxHeight * (1 + 1 / lengthDividedBy)) / (2 * MAX_INTERACTION_RADIUS))
			);

			// Calculate sum of forces, including fake wall particles
			particles.stream().parallel().forEach(p -> {
				Set<Particle> neighboursCustom = new HashSet<>(p.getNeighbours());
				neighboursCustom = filterNeighbors(p, neighboursCustom);
				addFakeWallParticles(p, neighboursCustom);
				calculateForce(p, neighboursCustom, kN, kT, A, B, τ);
			});

			List<Particle> toRemove = new LinkedList<>();

			// Only at first frame, initialize previous position of Verlet with Euler
			if (time == dt) {
				particles.forEach(p -> {
					Vector2D currentForce = p.getForce();
					double posX = p.getPosition().getX() - dt * p.getVelocity().getX();
					double posY = p.getPosition().getY() - dt * p.getVelocity().getY();
					posX += Math.pow(dt, 2) * currentForce.getX() / (2 * p.getMass());
					posY += Math.pow(dt, 2) * currentForce.getY() / (2 * p.getMass());

					particleIntegrationMethods.put(p,
							new VerletWithNeighbours(new Vector2D(posX, posY)));

					// Remove Neighbours
					p.clearNeighbours();
				});
			} else {
				particles.stream().parallel().forEach(p -> {

					// Update position
					moveParticle(p, dt);

					// Relocate particles that go outside box a distance of L/10 and clear neighbours
					if (p.getPosition().getY() < boxHeight / lengthDividedBy
							&& !outOfRoom.contains(p)) {

						outOfRoom.add(p);

						// Write time for flow
						try {
							flowFileBuffer.write(String.valueOf(time));
							flowFileBuffer.newLine();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					// Delete particles that arrive to Y = 0
					if (p.getPosition().getY() <= 0)
						toRemove.add(p);

					// Remove Neighbours
					p.clearNeighbours();
				});
			}

			// Delete particles that arrive to Y = 0
			particles.removeAll(toRemove);

			// Print current frame
			if ((currentFrame % printFrame) == 0)
				printFrame(buffer, particles);

			System.out.println("Particles Left: " + (N - outOfRoom.size()));
			currentFrame++;
		}
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		long minutes = (elapsedTime / 1000) / 60;
		long seconds = (elapsedTime / 1000) % 60;
		System.out.format("Execution time: %d minutes and %d seconds.", minutes, seconds);
	}


	private static Set<Particle> filterNeighbors(Particle particle, Set<Particle> neighbors) {
		HashSet<Particle> set = new HashSet<>();
		neighbors.forEach(neighbor -> {
			if (particle.getPosition().distance(neighbor.getPosition()) <= (particle.getRadius() + neighbor.getRadius())) {
				set.add(neighbor);
			}
		});
		return set;
	}

	/**
	 * Calculate sum of forces
	 */
	private static void calculateForce(Particle particle, Set<Particle> neighbours, double kN, double kT, double A, double B, double τ) {

		// Particle normal force reset and accumulator
		particle.resetNormalForce();
		AtomicReference<Double> atomicNormalForce = new AtomicReference<>(0.0);

		// Particle force calculation
		Vector2D F = new Vector2D(0, 0);
		F = neighbours.stream().map(p2 -> {

			// Sum of forces
			Vector2D sum = new Vector2D(0, 0);

			// Calculate distance between centers
			double distance = particle.getPosition().distance(p2.getPosition());

			// Calculate x component of contact unit vector e
			double Enx = (p2.getPosition().getX() - particle.getPosition().getX()) / distance;

			// Calculate y component of contact unit vector e
			double Eny = (p2.getPosition().getY() - particle.getPosition().getY()) / distance;

			// Calculate epsilon
			double eps = particle.getRadius() + p2.getRadius() - distance;

			// Granular force
			if (eps >= 0.0) {
				// Calculate Fn
				double Fn = -kN * eps;

				// Calculate Ft
				Vector2D relativeVelocity = particle.getVelocity().subtract(p2.getVelocity());
				Vector2D tangentVector = new Vector2D(-Eny, Enx);
				double Ft = -kT * eps * (relativeVelocity.dotProduct(tangentVector));

				double Fx = Fn * Enx + Ft * (-Eny);
				double Fy = Fn * Eny + Ft * Enx;

				atomicNormalForce.accumulateAndGet(Fn, (x, y) -> x + y);

				sum = sum.add(new Vector2D(Fx, Fy));
			}

			// Social force: do not use fake wall particles for social force
			if (p2.getId() > 0) {
				double FnSocial = -A * Math.exp((eps) / B);

				double FxSocial = FnSocial * Enx;
				double FySocial = FnSocial * Eny;

				sum = sum.add(new Vector2D(FxSocial, FySocial));
				/* End Social force */
			}

			return sum;
		}).reduce(F, Vector2D::add);

		// Driving force
		// Calculate distance between centers
		double MARGIN = 0.1;
		double targetX = particle.getPosition().getX();
		if (particle.getPosition().getX() - particle.getRadius() <= boxWidth / 2 - boxDiameter / 2 + MARGIN)
			targetX = boxWidth / 2 - boxDiameter / 2 + particle.getRadius() + MARGIN;
		if (particle.getPosition().getX() + particle.getRadius() >= boxWidth / 2 + boxDiameter / 2 - MARGIN)
			targetX = boxWidth / 2 + boxDiameter / 2 - particle.getRadius() - MARGIN;
		double targetY = boxHeight / lengthDividedBy;
		if (particle.getPosition().getY() < targetY)
			targetY = 0;
		particle.setDesiredTarget(new Vector2D(targetX, targetY));

		Vector2D FnDriving = ((particle.getVectorToTarget().subtract(particle.getVelocity()))).scalarMultiply(particle.getMass() / τ);
		F = F.add(FnDriving);

		// Particle knows its force at THIS frame
		particle.setForce(F);

		// Set particle's normal force for pressure calculation later on
		particle.setNormalForce(atomicNormalForce.get());
	}

	private static void moveParticle(Particle particle, double dt) {
		IntegrationMethodWithNeighbours integrationMethod = particleIntegrationMethods.get(particle);
		integrationMethod.updatePosition(particle, dt);
	}

	/**
	 * For the ones that make contact, add a fake particle to the set of neighbours.
	 * TODO refactor codigo repetido y casos ifs q nunca entraria
	 */
	private static void addFakeWallParticles(Particle particle, Set<Particle> neighbours) {
		int fakeId = -1;

		// Analyse left wall
		if (particle.getPosition().getX() - particle.getRadius() <= 0) {
			Particle leftWallParticle = new Particle(fakeId--, particle.getRadius(), particle.getMass());
			leftWallParticle.setPosition(new Vector2D(-particle.getRadius(), particle.getPosition().getY()));
			leftWallParticle.setVelocity(Vector2D.ZERO);
			neighbours.add(leftWallParticle);
		}
		// Analyse right wall
		else if (particle.getPosition().getX() + particle.getRadius() >= boxWidth) {
			Particle rightWallParticle = new Particle(fakeId--, particle.getRadius(), particle.getMass());
			rightWallParticle.setPosition(new Vector2D(particle.getRadius() + boxWidth, particle.getPosition().getY()));
			rightWallParticle.setVelocity(Vector2D.ZERO);
			neighbours.add(rightWallParticle);
		}

		double diameterStart = (boxWidth / 2 - boxDiameter / 2);
		boolean outsideGap = particle.getPosition().getX() < diameterStart || particle.getPosition().getX() > (diameterStart + boxDiameter);

		double bottomWall = boxHeight / lengthDividedBy;
//		double upperWall = boxHeight * (1 + 1 / lengthDividedBy);

		// Analyse bottom wall
		if (particle.getPosition().getY() >= bottomWall
				&& particle.getPosition().getY() - particle.getRadius() <= bottomWall) {
			if (outsideGap) {
				Particle bottomWallParticle = new Particle(fakeId, particle.getRadius(), particle.getMass());
				bottomWallParticle.setPosition(new Vector2D(particle.getPosition().getX(), bottomWall - particle.getRadius()));
				bottomWallParticle.setVelocity(Vector2D.ZERO);
				neighbours.add(bottomWallParticle);
			} else {
				if (boxDiameter > 0.0) {
					if (particle.getPosition().getX() - particle.getRadius() <= diameterStart
							&& particle.getPosition().distance(new Vector2D(diameterStart, bottomWall)) < particle.getRadius()) {
						Particle leftDiameterStartParticle = new Particle(fakeId, 0.0, 0.0);
						leftDiameterStartParticle.setPosition(new Vector2D(diameterStart, bottomWall));
						leftDiameterStartParticle.setVelocity(Vector2D.ZERO);
						neighbours.add(leftDiameterStartParticle);
					} else if (particle.getPosition().getX() + particle.getRadius() >= diameterStart + boxDiameter
							&& particle.getPosition().distance(new Vector2D(diameterStart + boxDiameter, bottomWall)) < particle.getRadius()) {
						Particle rightDiameterStartParticle = new Particle(fakeId, 0.0, 0.0);
						rightDiameterStartParticle.setPosition(new Vector2D(diameterStart + boxDiameter, bottomWall));
						rightDiameterStartParticle.setVelocity(Vector2D.ZERO);
						neighbours.add(rightDiameterStartParticle);
					}
				} else {
					if (particle.getPosition().getY() - particle.getRadius() <= bottomWall) {
						Particle closedDiameterParticle = new Particle(fakeId, particle.getRadius(), particle.getMass());
						closedDiameterParticle.setPosition(new Vector2D(particle.getPosition().getX(), bottomWall - particle.getRadius()));
						closedDiameterParticle.setVelocity(Vector2D.ZERO);
						neighbours.add(closedDiameterParticle);
					}
				}
			}
		}
		// Analyse top wall
//		else if (particle.getPosition().getY() + particle.getRadius() >= upperWall) {
//			Particle topWallParticle = new Particle(fakeId, particle.getRadius(), particle.getMass());
//			topWallParticle.setPosition(new Vector2D(particle.getPosition().getX(), particle.getRadius() + upperWall));
//			topWallParticle.setVelocity(Vector2D.ZERO);
//			neighbours.add(topWallParticle);
//		}
	}

	private static void printFrame(BufferedWriter buffer, List<Particle> particles) throws IOException {
		buffer.write(String.valueOf(particles.size()));
		buffer.newLine();
		buffer.write("t=");
		buffer.write(String.valueOf(new DecimalFormat("#.###").format(time)));
		buffer.write("s");
		buffer.newLine();

		particles.stream().parallel().forEach(p -> {
			try {
				buffer.write(particleToString(p));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});
	}

	private static String particleToString(Particle p) {
		return p.getId() + " " +
				p.getRadius() + " " +
				p.getPosition().getX() + " " +
				p.getPosition().getY() + " " +
				p.getVelocity().getX() + " " +
				p.getVelocity().getY() + " " +
				p.calculatePressure() + " \n";
	}
}
