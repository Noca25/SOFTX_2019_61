package isula.aco.algorithms.maxmin;

import isula.aco.Ant;
import isula.aco.ConfigurationProvider;
import isula.aco.DaemonAction;
import isula.aco.DaemonActionType;

import java.util.logging.Level;
import java.util.logging.Logger;

//TODO(cgavidia): Generics can be used on Configuration Provider types.
public abstract class UpdatePheromoneMatrixForMaxMin<E> extends DaemonAction<E> {

  private static Logger logger = Logger
      .getLogger(UpdatePheromoneMatrixForMaxMin.class.getName());

  /**
   * Instantiates the Update Pheromone Matrix Policy.
   * 
   * @param configurationProvider
   *          Configuration provider.
   */
  public UpdatePheromoneMatrixForMaxMin() {
    super(DaemonActionType.AFTER_ITERATION_CONSTRUCTION);

  }

  @Override
  public void applyDaemonAction(ConfigurationProvider provider) {

    MaxMinConfigurationProvider configurationProvider = (MaxMinConfigurationProvider) provider;
    logger.log(Level.FINE, "UPDATING PHEROMONE TRAILS");

    logger.log(Level.FINE, "Performing evaporation on all edges");
    logger.log(Level.FINE,
        "Evaporation ratio: " + configurationProvider.getEvaporationRatio());

    double[][] pheromoneMatrix = getEnvironment().getPheromoneMatrix();
    int matrixRows = pheromoneMatrix.length;
    int matrixColumns = pheromoneMatrix[0].length;

    for (int i = 0; i < matrixRows; i++) {
      for (int j = 0; j < matrixColumns; j++) {
        double newValue = pheromoneMatrix[i][j]
            * configurationProvider.getEvaporationRatio();

        if (newValue >= configurationProvider.getMinimumPheromoneValue()) {
          pheromoneMatrix[i][j] = newValue;
        } else {
          pheromoneMatrix[i][j] = configurationProvider
              .getMinimumPheromoneValue();
        }
      }
    }

    logger.log(Level.FINE, "Depositing pheromone on Best Ant trail.");

    Ant<E> bestAnt = getAntColony().getBestPerformingAnt(getEnvironment());

    E[] bestSolution = bestAnt.getSolution();
    int positionInSolution = 0;

    // TODO(cgavidia): From here, we can factor the policy of only best ant
    // deposits pheromone.
    for (E solutionComponent : bestSolution) {
      // TODO(cgavidia): This makes me think a solution type is necessary...
      double newValue = getNewPheromoneValue(bestAnt, positionInSolution,
          solutionComponent, configurationProvider);

      if (newValue <= configurationProvider.getMaximumPheromoneValue()) {
        bestAnt.setPheromoneTrailValue(solutionComponent, getEnvironment(),
            newValue);
      } else {
        bestAnt.setPheromoneTrailValue(solutionComponent, getEnvironment(),
            configurationProvider.getMaximumPheromoneValue());
      }

      positionInSolution += 1;
    }
  }

  protected abstract double getNewPheromoneValue(Ant<E> bestAnt,
      int positionInSolution, E solutionComponent,
      MaxMinConfigurationProvider configurationProvider);

}