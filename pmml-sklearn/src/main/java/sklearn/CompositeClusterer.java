/*
 * Copyright (c) 2023 Villu Ruusmann
 *
 * This file is part of JPMML-SkLearn
 *
 * JPMML-SkLearn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPMML-SkLearn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JPMML-SkLearn.  If not, see <http://www.gnu.org/licenses/>.
 */
package sklearn;

import java.util.List;

import org.dmg.pmml.DataType;
import org.dmg.pmml.Model;
import org.dmg.pmml.OpType;
import org.jpmml.converter.Schema;

public class CompositeClusterer extends Clusterer implements HasFeatureNamesIn, HasHead, Proxy {

	private Composite composite = null;


	public CompositeClusterer(Composite composite){
		super(composite.getPythonModule(), composite.getPythonName());

		setComposite(composite);
	}

	@Override
	public String getPredictField(){
		Clusterer clusterer = getFinalClusterer();

		return clusterer.getPredictField();
	}

	@Override
	public List<String> getFeatureNamesIn(){
		Composite composite = getComposite();

		return composite.getFeatureNamesIn();
	}

	@Override
	public int getNumberOfFeatures(){
		Composite composite = getComposite();

		return composite.getNumberOfFeatures();
	}

	@Override
	public int getNumberOfOutputs(){
		Clusterer clusterer = getFinalClusterer();

		return clusterer.getNumberOfOutputs();
	}

	@Override
	public OpType getOpType(){
		Composite composite = getComposite();

		return composite.getOpType();
	}

	@Override
	public DataType getDataType(){
		Composite composite = getComposite();

		return composite.getDataType();
	}

	@Override
	public boolean isSupervised(){
		Clusterer clusterer = getFinalClusterer();

		return clusterer.isSupervised();
	}

	@Override
	public String getAlgorithmName(){
		Clusterer clusterer = getFinalClusterer();

		return clusterer.getAlgorithmName();
	}

	@Override
	public Model encodeModel(Schema schema){
		Composite composite = getComposite();

		return composite.encodeModel(schema);
	}

	@Override
	public Step getHead(){
		Composite composite = getComposite();

		return composite.getHead();
	}

	public Clusterer getFinalClusterer(){
		Composite composite = getComposite();

		return composite.getFinalEstimator(Clusterer.class);
	}

	public Composite getComposite(){
		return this.composite;
	}

	private void setComposite(Composite composite){
		this.composite = composite;
	}
}