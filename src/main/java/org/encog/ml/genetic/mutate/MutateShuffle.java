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
package org.encog.ml.genetic.mutate;

import java.util.Random;

import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.opp.EvolutionaryOperator;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.ml.genetic.genome.ArrayGenome;

/**
 * A simple mutation where genes are shuffled.
 * This mutation will not produce repeated genes.
 */
public class MutateShuffle implements EvolutionaryOperator {

	private EvolutionaryAlgorithm owner;
	
	/**
	 * Perform a shuffle mutation.
	 * @param chromosome The chromosome to mutate.
	 */
	@Override
	public void performOperation(Random rnd, Genome[] parents, int parentIndex,
			Genome[] offspring, int offspringIndex) {
		ArrayGenome parent = (ArrayGenome)parents[parentIndex];
		offspring[offspringIndex] = this.owner.getPopulation().getGenomeFactory().factor();
		ArrayGenome child = (ArrayGenome)offspring[offspringIndex];
		
		child.copy(parent);
		
		final int length = parent.size();
		int iswap1 = (int) (Math.random() * length);
		int iswap2 = (int) (Math.random() * length);

		// can't be equal
		if (iswap1 == iswap2) {
			// move to the next, but
			// don't go out of bounds
			if (iswap1 > 0) {
				iswap1--;
			} else {
				iswap1++;
			}

		}

		// make sure they are in the right order
		if (iswap1 > iswap2) {
			final int temp = iswap1;
			iswap1 = iswap2;
			iswap2 = temp;
		}
		
		child.swap(iswap1,iswap2);
	}
	
	/**
	 * @return The number of offspring produced, which is 1 for this mutation.
	 */
	@Override
	public int offspringProduced() {
		return 1;
	}

	@Override
	public int parentsNeeded() {
		return 1;
	}

	@Override
	public void init(EvolutionaryAlgorithm theOwner) {
		this.owner = theOwner;
	}

}
