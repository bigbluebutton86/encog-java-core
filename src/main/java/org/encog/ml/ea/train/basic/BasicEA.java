/*
 * Encog(tm) Core v3.2 - Java Version
 * http://www.heatonresearch.com/encog/
 * http://code.google.com/p/encog-java/
 
 * Copyright 2008-2012 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *   
 * For more information on Heaton Research copyrights, licenses 
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */
package org.encog.ml.ea.train.basic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.encog.Encog;
import org.encog.EncogError;
import org.encog.mathutil.randomize.factory.RandomFactory;
import org.encog.ml.CalculateScore;
import org.encog.ml.MLContext;
import org.encog.ml.MLMethod;
import org.encog.ml.ea.codec.GeneticCODEC;
import org.encog.ml.ea.codec.GenomeAsPhenomeCODEC;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.opp.EvolutionaryOperator;
import org.encog.ml.ea.opp.OperationList;
import org.encog.ml.ea.opp.selection.SelectionOperator;
import org.encog.ml.ea.opp.selection.TournamentSelection;
import org.encog.ml.ea.population.Population;
import org.encog.ml.ea.score.AdjustScore;
import org.encog.ml.ea.score.parallel.ParallelScore;
import org.encog.ml.ea.sort.GenomeComparator;
import org.encog.ml.ea.sort.MaximizeAdjustedScoreComp;
import org.encog.ml.ea.sort.MaximizeScoreComp;
import org.encog.ml.ea.sort.MinimizeAdjustedScoreComp;
import org.encog.ml.ea.sort.MinimizeScoreComp;
import org.encog.ml.ea.species.SingleSpeciation;
import org.encog.ml.ea.species.Speciation;
import org.encog.ml.ea.species.Species;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.ml.genetic.GeneticError;
import org.encog.ml.prg.train.GeneticTrainingParams;
import org.encog.util.concurrency.MultiThreadable;

/**
 * Provides a basic implementation of a multi-threaded Evolutionary Algorithm.
 * The EA works from a score function.
 */
public class BasicEA implements EvolutionaryAlgorithm, MultiThreadable,
		Serializable {

	/**
	 * Calculate the score adjustment, based on adjusters.
	 * 
	 * @param genome
	 *            The genome to adjust.
	 * @param adjusters
	 *            The score adjusters.
	 */
	public static void calculateScoreAdjustment(final Genome genome,
			final List<AdjustScore> adjusters) {
		final double score = genome.getScore();
		double delta = 0;

		for (final AdjustScore a : adjusters) {
			delta += a.calculateAdjustment(genome);
		}

		genome.setAdjustedScore(score + delta);
	}

	/**
	 * Should exceptions be ignored.
	 */
	private boolean ignoreExceptions;

	/**
	 * Params.
	 */
	private GeneticTrainingParams params = new GeneticTrainingParams();

	/**
	 * The genome comparator.
	 */
	private GenomeComparator bestComparator;

	/**
	 * The genome comparator.
	 */
	private GenomeComparator selectionComparator;

	/**
	 * The population.
	 */
	private Population population;

	/**
	 * The score calculation function.
	 */
	private final CalculateScore scoreFunction;

	/**
	 * The selection operator.
	 */
	private SelectionOperator selection;

	/**
	 * The score adjusters.
	 */
	private final List<AdjustScore> adjusters = new ArrayList<AdjustScore>();

	/**
	 * The operators. to use.
	 */
	private final OperationList operators = new OperationList();

	/**
	 * The CODEC to use to convert between genome and phenome.
	 */
	private GeneticCODEC codec = new GenomeAsPhenomeCODEC();

	/**
	 * Random number factory.
	 */
	private RandomFactory randomNumberFactory = Encog.getInstance()
			.getRandomFactory().factorFactory();

	/**
	 * The validation mode.
	 */
	private boolean validationMode;

	/**
	 * The iteration number.
	 */
	private int iteration;

	/**
	 * The desired thread count.
	 */
	private int threadCount;

	/**
	 * The actual thread count.
	 */
	private int actualThreadCount = -1;

	/**
	 * The speciation method.
	 */
	private Speciation speciation = new SingleSpeciation();

	/**
	 * This property stores any error that might be reported by a thread.
	 */
	private Throwable reportedError;

	/**
	 * The best genome from the last iteration.
	 */
	private Genome oldBestGenome;

	/**
	 * The population for the next iteration.
	 */
	private final List<Genome> newPopulation = new ArrayList<Genome>();

	/**
	 * The mutation to be used on the top genome. We want to only modify its
	 * weights.
	 */
	private EvolutionaryOperator champMutation;

	/**
	 * The percentage of a species that is "elite" and is passed on directly.
	 */
	private double eliteRate = 0.3;

	/**
	 * The number of times to try certian operations, so an endless loop does
	 * not occur.
	 */
	private int maxTries = 5;

	/**
	 * The best ever genome.
	 */
	private Genome bestGenome;

	/**
	 * Construct an EA.
	 * 
	 * @param thePopulation
	 *            The population.
	 * @param theScoreFunction
	 *            The score function.
	 */
	public BasicEA(final Population thePopulation,
			final CalculateScore theScoreFunction) {

		this.population = thePopulation;
		this.scoreFunction = theScoreFunction;
		this.selection = new TournamentSelection(this, 4);

		// set the score compare method
		if (theScoreFunction.shouldMinimize()) {
			this.selectionComparator = new MinimizeAdjustedScoreComp();
			this.bestComparator = new MinimizeScoreComp();
		} else {
			this.selectionComparator = new MaximizeAdjustedScoreComp();
			this.bestComparator = new MaximizeScoreComp();
		}

		// set the iteration
		for (final Species species : thePopulation.getSpecies()) {
			for (final Genome genome : species.getMembers()) {
				setIteration(Math.max(getIteration(),
						genome.getBirthGeneration()));
			}
		}
	}

	/**
	 * Add a child to the next iteration.
	 * 
	 * @param genome
	 *            The child.
	 * @return True, if the child was added successfully.
	 */
	public boolean addChild(final Genome genome) {
		synchronized (this.newPopulation) {
			if (this.newPopulation.size() < getPopulation().getPopulationSize()) {
				// don't readd the old best genome, it was already added
				if (genome != this.oldBestGenome) {

					if (isValidationMode()) {
						if (this.newPopulation.contains(genome)) {
							throw new EncogError(
									"Genome already added to population: "
											+ genome.toString());
						}
					}

					this.newPopulation.add(genome);
				}

				if (getBestComparator().isBetterThan(genome, this.bestGenome)) {
					this.bestGenome = genome;
					getPopulation().setBestGenome(this.bestGenome);
				}
				return true;
			} else {
				if (isValidationMode()) {
					// throw new EncogError("Population overflow");
				}
				return false;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addOperation(final double probability,
			final EvolutionaryOperator opp) {
		getOperators().add(probability, opp);
		opp.init(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addScoreAdjuster(final AdjustScore scoreAdjust) {
		this.adjusters.add(scoreAdjust);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void calculateScore(final Genome g) {

		// try rewrite
		getPopulation().rewrite(g);

		// decode
		final MLMethod phenotype = getCODEC().decode(g);
		double score;

		// deal with invalid decode
		if (phenotype == null) {
			if (getBestComparator().shouldMinimize()) {
				score = Double.POSITIVE_INFINITY;
			} else {
				score = Double.NEGATIVE_INFINITY;
			}
		} else {
			if (phenotype instanceof MLContext) {
				((MLContext) phenotype).clearContext();
			}
			score = getScoreFunction().calculateScore(phenotype);
		}

		// now set the scores
		g.setScore(score);
		g.setAdjustedScore(score);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void finishTraining() {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GenomeComparator getBestComparator() {
		return this.bestComparator;
	}

	/**
	 * @return the bestGenome
	 */
	@Override
	public Genome getBestGenome() {
		return this.bestGenome;
	}

	/**
	 * @return the champMutation
	 */
	public EvolutionaryOperator getChampMutation() {
		return this.champMutation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GeneticCODEC getCODEC() {
		return this.codec;
	}

	/**
	 * @return the eliteRate
	 */
	public double getEliteRate() {
		return this.eliteRate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getError() {
		if (this.bestGenome != null) {
			return this.bestGenome.getScore();
		} else {
			if (getScoreFunction().shouldMinimize()) {
				return Double.POSITIVE_INFINITY;
			} else {
				return Double.NEGATIVE_INFINITY;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getIteration() {
		return this.iteration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getMaxIndividualSize() {
		return this.population.getMaxIndividualSize();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getMaxTries() {
		return this.maxTries;
	}

	/**
	 * @return the oldBestGenome
	 */
	public Genome getOldBestGenome() {
		return this.oldBestGenome;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OperationList getOperators() {
		return this.operators;
	}

	/**
	 * @return the params
	 */
	@Override
	public GeneticTrainingParams getParams() {
		return this.params;
	}

	/**
	 * @return The population.
	 */
	@Override
	public Population getPopulation() {
		return this.population;
	}

	/**
	 * @return the randomNumberFactory
	 */
	public RandomFactory getRandomNumberFactory() {
		return this.randomNumberFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<AdjustScore> getScoreAdjusters() {
		return this.adjusters;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CalculateScore getScoreFunction() {
		return this.scoreFunction;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SelectionOperator getSelection() {
		return this.selection;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GenomeComparator getSelectionComparator() {
		return this.selectionComparator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean getShouldIgnoreExceptions() {
		return this.ignoreExceptions;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Speciation getSpeciation() {
		return this.speciation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getThreadCount() {
		return this.threadCount;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isValidationMode() {
		return this.validationMode;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void iteration() {
		if (this.actualThreadCount == -1) {
			preIteration();
		}

		this.iteration++;

		ExecutorService taskExecutor = null;

		if (this.actualThreadCount == 1) {
			taskExecutor = Executors.newSingleThreadScheduledExecutor();
		} else {
			taskExecutor = Executors.newFixedThreadPool(this.actualThreadCount);
		}

		// Clear new population to just best genome.
		this.newPopulation.clear();
		this.newPopulation.add(this.bestGenome);
		this.oldBestGenome = this.bestGenome;

		// execute species in parallel
		for (final Species species : getPopulation().getSpecies()) {
			int numToSpawn = species.getOffspringCount();

			// Add elite genomes directly
			if (species.getMembers().size() > 5) {
				final int idealEliteCount = (int) (species.getMembers().size() * getEliteRate());
				final int eliteCount = Math.min(numToSpawn, idealEliteCount);
				for (int i = 0; i < eliteCount; i++) {
					final Genome eliteGenome = species.getMembers().get(i);
					if (getOldBestGenome() != eliteGenome) {
						numToSpawn--;
						if (!addChild(eliteGenome)) {
							break;
						}
					}
				}
			}

			while (numToSpawn-- > 0) {
				final EAWorker worker = new EAWorker(this, species);
				taskExecutor.execute(worker);
			}
		}

		// wait for threadpool to shutdown
		taskExecutor.shutdown();
		try {
			taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
		} catch (final InterruptedException e) {
			throw new GeneticError(e);
		}

		if (this.reportedError != null && !getShouldIgnoreExceptions()) {
			throw new GeneticError(this.reportedError);
		}

		if (isValidationMode()) {
			final int currentPopSize = this.newPopulation.size();
			final int targetPopSize = getPopulation().getPopulationSize();
			if (currentPopSize != targetPopSize) {
				throw new EncogError("Population size of " + currentPopSize
						+ " is outside of the target size of " + targetPopSize);
			}

			if (this.oldBestGenome != null
					&& !this.newPopulation.contains(this.oldBestGenome)) {
				throw new EncogError(
						"The top genome died, this should never happen!!");
			}

			if (this.bestGenome != null
					&& this.oldBestGenome != null
					&& getBestComparator().isBetterThan(this.oldBestGenome,
							this.bestGenome)) {
				throw new EncogError(
						"The best genome's score got worse, this should never happen!! Went from "
								+ this.oldBestGenome.getScore() + " to "
								+ this.bestGenome.getScore());
			}
		}

		this.speciation.performSpeciation(this.newPopulation);
	}

	/**
	 * Called before the first iteration. Determine the number of threads to
	 * use.
	 */
	private void preIteration() {

		this.speciation.init(this);

		// find out how many threads to use
		if (this.threadCount == 0) {
			this.actualThreadCount = Runtime.getRuntime().availableProcessors();
		} else {
			this.actualThreadCount = this.threadCount;
		}

		// score the initial population
		final ParallelScore pscore = new ParallelScore(getPopulation(),
				getCODEC(), new ArrayList<AdjustScore>(), getScoreFunction(),
				this.actualThreadCount);
		pscore.setThreadCount(this.actualThreadCount);
		pscore.process();
		this.actualThreadCount = pscore.getThreadCount();

		// just pick the first genome as best, it will be updated later.
		// also most populations are sorted this way after training finishes
		// (for reload)
		// if there is an empty population, the constructor would have blow
		this.bestGenome = getPopulation().getSpecies().get(0).getMembers()
				.get(0);
		getPopulation().setBestGenome(this.bestGenome);

		// speciate
		final List<Genome> genomes = getPopulation().flatten();
		this.speciation.performSpeciation(genomes);

	}

	/**
	 * Called by a thread to report an error.
	 * 
	 * @param t
	 *            The error reported.
	 */
	public void reportError(final Throwable t) {
		synchronized (this) {
			if (this.reportedError == null) {
				this.reportedError = t;
			}
		}
	}

	/**
	 * Set the comparator.
	 * 
	 * @param theComparator
	 *            The comparator.
	 */
	@Override
	public void setBestComparator(final GenomeComparator theComparator) {
		this.bestComparator = theComparator;
	}

	/**
	 * @param champMutation
	 *            the champMutation to set
	 */
	public void setChampMutation(final EvolutionaryOperator champMutation) {
		this.champMutation = champMutation;
	}

	/**
	 * Set the CODEC to use.
	 * 
	 * @param theCodec
	 *            The CODEC to use.
	 */
	public void setCODEC(final GeneticCODEC theCodec) {
		this.codec = theCodec;
	}

	/**
	 * @param eliteRate
	 *            the eliteRate to set
	 */
	public void setEliteRate(final double eliteRate) {
		this.eliteRate = eliteRate;
	}

	/**
	 * Set the current iteration number.
	 * 
	 * @param iteration
	 *            The iteration number.
	 */
	public void setIteration(final int iteration) {
		this.iteration = iteration;
	}

	/**
	 * @param maxTries
	 *            the maxTries to set
	 */
	public void setMaxTries(final int maxTries) {
		this.maxTries = maxTries;
	}

	/**
	 * @param params
	 *            the params to set
	 */
	public void setParams(final GeneticTrainingParams params) {
		this.params = params;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPopulation(final Population thePopulation) {
		this.population = thePopulation;
	}

	/**
	 * @param randomNumberFactory
	 *            the randomNumberFactory to set
	 */
	public void setRandomNumberFactory(final RandomFactory randomNumberFactory) {
		this.randomNumberFactory = randomNumberFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSelection(final SelectionOperator selection) {
		this.selection = selection;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSelectionComparator(final GenomeComparator theComparator) {
		this.selectionComparator = theComparator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setShouldIgnoreExceptions(final boolean b) {
		this.ignoreExceptions = b;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSpeciation(final Speciation speciation) {
		this.speciation = speciation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setThreadCount(final int numThreads) {
		this.threadCount = numThreads;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setValidationMode(final boolean validationMode) {
		this.validationMode = validationMode;
	}

}
