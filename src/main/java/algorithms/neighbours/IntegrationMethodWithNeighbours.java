package algorithms.neighbours;

import models.Particle;

interface IntegrationMethodWithNeighbours {

	void updatePosition(Particle particle, double dt);
}
