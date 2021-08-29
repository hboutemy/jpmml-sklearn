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
package sklearn;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dmg.pmml.DataType;
import org.dmg.pmml.MiningFunction;
import org.dmg.pmml.Model;
import org.dmg.pmml.OpType;
import org.jpmml.converter.Feature;
import org.jpmml.converter.Label;
import org.jpmml.converter.ModelEncoder;
import org.jpmml.converter.Schema;
import org.jpmml.python.ClassDictUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract
public class Estimator extends Step {

	public Estimator(String module, String name){
		super(module, name);
	}

	abstract
	public MiningFunction getMiningFunction();

	abstract
	public Model encodeModel(Schema schema);

	@Override
	public int getNumberOfFeatures(){

		if(containsKey("n_features_")){
			return getInteger("n_features_");
		}

		return HasNumberOfFeatures.UNKNOWN;
	}

	@Override
	public OpType getOpType(){
		return OpType.CONTINUOUS;
	}

	@Override
	public DataType getDataType(){
		return DataType.DOUBLE;
	}

	public boolean isSupervised(){
		MiningFunction miningFunction = getMiningFunction();

		switch(miningFunction){
			case CLASSIFICATION:
			case REGRESSION:
				return true;
			case CLUSTERING:
				return false;
			default:
				throw new IllegalArgumentException();
		}
	}

	public Model encode(Schema schema){
		checkLabel(schema.getLabel());
		checkFeatures(schema.getFeatures());

		Model model = encodeModel(schema);

		String modelName = model.getModelName();
		if(modelName == null){
			String pmmlName = getPMMLName();

			if(pmmlName != null){
				model.setModelName(pmmlName);
			}
		}

		String algorithmName = model.getAlgorithmName();
		if(algorithmName == null){
			String pyClassName = getClassName();

			model.setAlgorithmName(pyClassName);
		}

		addFeatureImportances(model, schema);

		return model;
	}

	public void checkLabel(Label label){
		boolean supervised = isSupervised();

		if(supervised){

			if(label == null){
				throw new IllegalArgumentException("Expected a label, got no label");
			}
		} else

		{
			if(label != null){
				throw new IllegalArgumentException("Expected no label, got " + label);
			}
		}
	}

	public void checkFeatures(List<? extends Feature> features){
		StepUtil.checkNumberOfFeatures(this, features);
	}

	public void addFeatureImportances(Model model, Schema schema){
		List<? extends Number> featureImportances = getPMMLFeatureImportances();
		if(featureImportances == null){
			featureImportances = getFeatureImportances();
		}

		ModelEncoder encoder = (ModelEncoder)schema.getEncoder();
		List<? extends Feature> features = schema.getFeatures();

		if(featureImportances != null){
			ClassDictUtil.checkSize(features, featureImportances);

			for(int i = 0; i < features.size(); i++){
				Feature feature = features.get(i);
				Number featureImportance = featureImportances.get(i);

				encoder.addFeatureImportance(model, feature, featureImportance);
			}
		}
	}

	public Object getOption(String key, Object defaultValue){
		Map<String, ?> pmmlOptions = getPMMLOptions();

		if(pmmlOptions != null && pmmlOptions.containsKey(key)){
			return pmmlOptions.get(key);
		} // End if

		// XXX
		if(containsKey(key)){
			logger.warn("Attribute \'" + ClassDictUtil.formatMember(this, "pmml_options_") + "\' is not set. Falling back to the surrogate attribute \'" + ClassDictUtil.formatMember(this, key) + "\'");

			return get(key);
		}

		return defaultValue;
	}

	public void putOption(String key, Object value){
		putOptions(Collections.singletonMap(key, value));
	}

	public void putOptions(Map<String, ?> options){
		Map<String, ?> pmmlOptions = getPMMLOptions();

		if(pmmlOptions == null){
			pmmlOptions = new LinkedHashMap<>();

			setPMMLOptions(pmmlOptions);
		}

		// XXX
		pmmlOptions.putAll((Map)options);
	}

	public boolean hasFeatureImportances(){
		return containsKey("feature_importances_") || containsKey("pmml_feature_importances_");
	}

	public List<? extends Number> getFeatureImportances(){

		if(!containsKey("feature_importances_")){
			return null;
		}

		return getNumberArray("feature_importances_");
	}

	public List<? extends Number> getPMMLFeatureImportances(){

		if(!containsKey("pmml_feature_importances_")){
			return null;
		}

		return getNumberArray("pmml_feature_importances_");
	}

	public Estimator setPMMLFeatureImportances(List<? extends Number> pmmlFeatureImportances){
		put("pmml_feature_importances_", toArray(pmmlFeatureImportances));

		return this;
	}

	public Map<String, ?> getPMMLOptions(){
		Object value = get("pmml_options_");

		if(value == null){
			return null;
		}

		return getDict("pmml_options_");
	}

	public Estimator setPMMLOptions(Map<String, ?> pmmlOptions){
		put("pmml_options_", pmmlOptions);

		return this;
	}

	public String getSkLearnVersion(){
		return getOptionalString("_sklearn_version");
	}

	public static final String FIELD_APPLY = "apply";
	public static final String FIELD_DECISION_FUNCTION = "decisionFunction";
	public static final String FIELD_PREDICT = "predict";

	private static final Logger logger = LoggerFactory.getLogger(Estimator.class);
}