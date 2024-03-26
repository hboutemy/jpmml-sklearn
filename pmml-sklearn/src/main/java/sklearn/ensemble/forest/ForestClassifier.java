/*
 * Copyright (c) 2015 Villu Ruusmann
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
package sklearn.ensemble.forest;

import java.util.List;

import org.dmg.pmml.DataType;
import org.dmg.pmml.MiningFunction;
import org.dmg.pmml.mining.MiningModel;
import org.dmg.pmml.mining.Segmentation;
import org.jpmml.converter.CategoricalLabel;
import org.jpmml.converter.FieldNameUtil;
import org.jpmml.converter.FieldNames;
import org.jpmml.converter.Schema;
import sklearn.HasEstimatorEnsemble;
import sklearn.HasMultiApplyField;
import sklearn.SkLearnClassifier;
import sklearn.tree.HasTreeOptions;
import sklearn.tree.TreeClassifier;
import sklearn.tree.TreeUtil;

public class ForestClassifier extends SkLearnClassifier implements HasEstimatorEnsemble<TreeClassifier>, HasMultiApplyField, HasTreeOptions {

	public ForestClassifier(String module, String name){
		super(module, name);
	}

	@Override
	public DataType getDataType(){
		return DataType.FLOAT;
	}

	@Override
	public int getNumberOfApplyFields(){
		return ForestUtil.getNumberOfEstimators(this);
	}

	@Override
	public String getMultiApplyField(Object segmentId){
		return FieldNameUtil.create(FieldNames.NODE_ID, segmentId);
	}

	@Override
	public Schema configureSchema(Schema schema){
		return TreeUtil.configureSchema(this, schema);
	}

	@Override
	public MiningModel encodeModel(Schema schema){
		CategoricalLabel categoricalLabel = (CategoricalLabel)schema.getLabel();

		MiningModel miningModel = ForestUtil.encodeBaseForest(this, Segmentation.MultipleModelMethod.AVERAGE, MiningFunction.CLASSIFICATION, schema);

		encodePredictProbaOutput(miningModel, DataType.DOUBLE, categoricalLabel);

		return miningModel;
	}

	@Override
	public List<? extends TreeClassifier> getEstimators(){
		return getList("estimators_", TreeClassifier.class);
	}
}