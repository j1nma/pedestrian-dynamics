package algorithms.neighbours;

import models.Particle;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class SocialForceModel {

	private static double boxHeight = 20.0;
	private static double boxWidth = 20.0;
	private static double boxDiameter = 1.2;

	private static final double MAX_DIAMETER = 0.58;
	private static final double MAX_INTERACTION_RADIUS = MAX_DIAMETER / 2;

	// Initial State
	private static double time = 0.0;

	// Each particle's integration method
	private static final Map<Particle, IntegrationMethodWithNeighbours> particleIntegrationMethods = new HashMap<>();

	public static void run(
			List<Particle> particles,
			BufferedWriter buffer,
			BufferedWriter flowFileBuffer,
			double limitTime,
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
			double τ) throws IOException {

		boxHeight = length;
		boxWidth = width;
//		boxDiameter = 0.0;
		boxDiameter = diameter;

//		Particle p1 = particles.get(0);
//		Particle p2 = particles.get(1);
//		p1.setPosition(new Vector2D(2, 2.5));
//		p1.setRadius(0.25);
//		p2.setRadius(0.25);
//		p2.setPosition(new Vector2D(11, 14));
//		p1.setVelocity(new Vector2D(0, 0));
//		p2.setVelocity(new Vector2D(0, 0));
//		List<Particle> test2particles = new ArrayList<>();
//		test2particles.add(p1);
//		test2particles.add(p2);
//		particles = test2particles;

		// Print to buffer
		printFirstFrame(buffer, particles);

		// Print frame
		int currentFrame = 1;
		int printFrame = (int) Math.ceil(printDeltaT / dt);
		AtomicReference<Integer> leftInBox = new AtomicReference<>(1);

		while (leftInBox.get() != 0) {
			leftInBox.set(0);
			time += dt;

			// Calculate neighbours
			CellIndexMethod.run(particles,
					(boxHeight * 1.1),
					(int) Math.floor((boxHeight * 1.1) / (2 * MAX_INTERACTION_RADIUS))
			);

			// Calculate sum of forces, including fake wall particles
			particles.stream().parallel().forEach(p -> {
				Set<Particle> neighboursCustom = new HashSet<>(p.getNeighbours());
				neighboursCustom = filterNeighbors(p, neighboursCustom);
				addFakeWallParticles(p, neighboursCustom);
				calculateForce(p, neighboursCustom, kN, kT, A, B, τ);
			});


			// Only at first frame, initialize previous position of Verlet with Euler
			if (time == dt) {
				particles.forEach(p -> {
					if (time == dt) {
						Vector2D currentForce = p.getForce();
						double posX = p.getPosition().getX() - dt * p.getVelocity().getX();
						double posY = p.getPosition().getY() - dt * p.getVelocity().getY();
						posX += Math.pow(dt, 2) * currentForce.getX() / (2 * p.getMass());
						posY += Math.pow(dt, 2) * currentForce.getY() / (2 * p.getMass());

						particleIntegrationMethods.put(p,
								new VerletWithNeighbours(new Vector2D(posX, posY)));
					}
				});
				leftInBox.set(particles.size());
			} else {
				// Update position
				particles.stream().parallel().forEach(p -> {
					moveParticle(p, dt);
					if (p.getPosition().getY() > boxHeight / 10
							&& p.getPosition().getY() <= boxHeight * 1.1
							&& p.getPosition().getX() > 0
							&& p.getPosition().getX() <= boxWidth)
						leftInBox.accumulateAndGet(1, (x, y) -> x + y);
				});
			}

			// Delete particles that arrive to Y=0
			particles.removeIf(particle -> particle.getPosition().getY() <= 0);

			// Remove Neighbours
			particles.forEach(Particle::clearNeighbours);

			// print current frame if need to
			if ((currentFrame % printFrame) == 0) {
				buffer.write(String.valueOf(particles.size()));
				buffer.newLine();
				buffer.write(String.valueOf(currentFrame));
				buffer.newLine();

				particles.stream().parallel().forEach(p -> {
					try {
						buffer.write(particleToString(p));
					} catch (IOException e1) {
						e1.printStackTrace();
					}

				});

			}

			System.out.println("Particles Left: " + leftInBox.get());
			currentFrame++;
		}
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

		// todo esto hace la sumatoria de Fuerzas de Fgranular. lo que habria que hacer es hacer las Fsocial y la F deseo y luego sumarlas
		//  o bien en una sola iteracion que es la de abajo en la sumatoria agregar tmb Fdeseo y Fsocial

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

				sum = sum.add(new Vector2D(Fx, Fy));
			}


			// Do not use fake wall particles for social
			if (p2.getId() > 0) {
				/* Start Social force */
				double FnSocial = -A * Math.exp((eps) / B);

				double FxSocial = FnSocial * Enx;
				double FySocial = FnSocial * Eny;

				sum = sum.add(new Vector2D(FxSocial, FySocial));
				/* End Social force */
			}

			return sum;
		}).reduce(F, Vector2D::add);

		/* Start Driving force */
		// Calculate distance between centers
		double MARGIN = 0.0;
		double targetX = particle.getPosition().getX();
		if (particle.getPosition().getX() - particle.getRadius() <= boxWidth / 2 - boxDiameter / 2 + MARGIN)
			targetX = boxWidth / 2 - boxDiameter / 2 + particle.getRadius() + MARGIN;
		if (particle.getPosition().getX() + particle.getRadius() >= boxWidth / 2 + boxDiameter / 2 - MARGIN)
			targetX = boxWidth / 2 + boxDiameter / 2 - particle.getRadius() - MARGIN;
		double targetY = boxHeight / 10;
		if (particle.getPosition().getY() < targetY)
			targetY = 0;
		particle.setDesiredTarget(new Vector2D(targetX, targetY));

		Vector2D vectorToTarget = particle.getVectorToTarget();

		Vector2D FnDriving = ((vectorToTarget.subtract(particle.getVelocity()))).scalarMultiply(particle.getMass() / τ); // TODO revisar qué es tau!
		F = F.add(FnDriving);
		/* End Driving force */

		// Particle knows its force at THIS frame
		particle.setForce(F);

		// Set particle's normal force for pressure calculation later on
//		particle.setNormalForce(atomicNormalForce.get());
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

		double bottomWall = boxHeight / 10;
		double upperWall = boxHeight * 1.1;

		// Analyse bottom wall
		if (particle.getPosition().getY() >= bottomWall
				&& particle.getPosition().getY() - particle.getRadius() <= bottomWall) {
//				&& particle.getVelocity().getY() < 0) {
			if (outsideGap) {
				Particle bottomWallParticle = new Particle(fakeId--, particle.getRadius(), particle.getMass());
				bottomWallParticle.setPosition(new Vector2D(particle.getPosition().getX(), bottomWall - particle.getRadius()));
				bottomWallParticle.setVelocity(Vector2D.ZERO);
				neighbours.add(bottomWallParticle);
			} else {
				if (boxDiameter > 0.0) {
					if (particle.getPosition().getX() - particle.getRadius() <= diameterStart
							&& particle.getPosition().distance(new Vector2D(diameterStart, bottomWall)) < particle.getRadius()) {
						Particle leftDiameterStartParticle = new Particle(fakeId--, 0.0, 0.0); //todo: re ask about mass
						leftDiameterStartParticle.setPosition(new Vector2D(diameterStart, bottomWall));
						leftDiameterStartParticle.setVelocity(Vector2D.ZERO);
						neighbours.add(leftDiameterStartParticle);
					} else if (particle.getPosition().getX() + particle.getRadius() >= diameterStart + boxDiameter
							&& particle.getPosition().distance(new Vector2D(diameterStart + boxDiameter, bottomWall)) < particle.getRadius()) {
						Particle rightDiameterStartParticle = new Particle(fakeId--, 0.0, 0.0);
						rightDiameterStartParticle.setPosition(new Vector2D(diameterStart + boxDiameter, bottomWall));
						rightDiameterStartParticle.setVelocity(Vector2D.ZERO);
						neighbours.add(rightDiameterStartParticle);
					}
				} else {
					if (particle.getPosition().getY() - particle.getRadius() <= bottomWall) {
						Particle closedDiameterParticle = new Particle(fakeId--, particle.getRadius(), particle.getMass());
						closedDiameterParticle.setPosition(new Vector2D(particle.getPosition().getX(), bottomWall - particle.getRadius()));
						closedDiameterParticle.setVelocity(Vector2D.ZERO);
						neighbours.add(closedDiameterParticle);
					}
				}
			}
		}
		// Analyse top wall
		else if (particle.getPosition().getY() + particle.getRadius() >= upperWall) {
			Particle topWallParticle = new Particle(fakeId--, particle.getRadius(), particle.getMass());
			topWallParticle.setPosition(new Vector2D(particle.getPosition().getX(), particle.getRadius() + upperWall));
			topWallParticle.setVelocity(Vector2D.ZERO);
			neighbours.add(topWallParticle);
		}
	}

	private static void printFirstFrame(BufferedWriter buff, List<Particle> particles) throws IOException {
		buff.write(String.valueOf(particles.size()));
		buff.newLine();
		buff.write("0");
		buff.newLine();

		// Print remaining particles
		particles.forEach(particle -> {
			try {
				buff.write(particleToString(particle));
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
				p.getVelocity().getY() + " \n";
	}
}
