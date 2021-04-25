from common import *

from category_encoders import BaseNEncoder, BinaryEncoder, CatBoostEncoder, CountEncoder, LeaveOneOutEncoder, OneHotEncoder, OrdinalEncoder, TargetEncoder, WOEEncoder
from pandas import DataFrame
from sklearn.base import clone
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import RandomForestClassifier
from sklearn.impute import SimpleImputer
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder as SkLearnOneHotEncoder
from sklearn2pmml.pipeline import PMMLPipeline
from xgboost import XGBClassifier

cat_cols = ["Employment", "Education", "Marital", "Occupation", "Gender"]
cont_cols = ["Age", "Income", "Hours"]

def build_audit(cat_encoder, cont_encoder, classifier, name, **pmml_options):
	if name.endswith("Audit"):
		audit_X, audit_y = load_audit("Audit")
	elif name.endswith("AuditNA"):
		audit_X, audit_y = load_audit("AuditNA")
	else:
		raise ValueError()

	pipeline = PMMLPipeline([
		("mapper", ColumnTransformer([
			("cat", cat_encoder, cat_cols),
			("cont", cont_encoder, cont_cols)
		])),
		("classifier", classifier)
	])
	pipeline.fit(audit_X, audit_y)
	pipeline.configure(**pmml_options)
	store_pkl(pipeline, name)
	adjusted = DataFrame(pipeline.predict(audit_X), columns = ["Adjusted"])
	adjusted_proba = DataFrame(pipeline.predict_proba(audit_X), columns = ["probability(0)", "probability(1)"])
	adjusted = pandas.concat((adjusted, adjusted_proba), axis = 1)
	store_csv(adjusted, name)

classifier = LogisticRegression()

build_audit(OneHotEncoder(handle_missing = "error", handle_unknown = "error"), "passthrough", clone(classifier), "OneHotEncoderAudit")
build_audit(OneHotEncoder(handle_missing = "value", handle_unknown = "error"), SimpleImputer(), clone(classifier), "OneHotEncoderAuditNA")
build_audit(Pipeline([("ordinal", OrdinalEncoder(handle_missing = "error", handle_unknown = "value")), ("ohe", SkLearnOneHotEncoder())]), "passthrough", clone(classifier), "OrdinalEncoderAudit")
build_audit(Pipeline([("ordinal", OrdinalEncoder(handle_missing = "value", handle_unknown = "error")), ("ohe", SkLearnOneHotEncoder())]), SimpleImputer(), clone(classifier), "OrdinalEncoderAuditNA")
build_audit(BaseNEncoder(base = 2, drop_invariant = True, handle_missing = "error", handle_unknown = "error"), "passthrough", clone(classifier), "Base2EncoderAudit")
build_audit(BaseNEncoder(base = 2, handle_missing = "value", handle_unknown = "error"), SimpleImputer(), clone(classifier), "Base2EncoderAuditNA")
build_audit(Pipeline([("basen", BaseNEncoder(base = 3, drop_invariant = True, handle_missing = "error", handle_unknown = "error")), ("ohe", SkLearnOneHotEncoder())]), "passthrough", clone(classifier), "Base3EncoderAudit")
build_audit(Pipeline([("basen", BaseNEncoder(base = 3, handle_missing = "value", handle_unknown = "error")), ("ohe", SkLearnOneHotEncoder())]), SimpleImputer(), clone(classifier), "Base3EncoderAuditNA")
build_audit(BaseNEncoder(base = 4, drop_invariant = True, handle_missing = "error", handle_unknown = "error"), "passthrough", clone(classifier), "Base4EncoderAudit")
build_audit(BaseNEncoder(base = 4, handle_missing = "value", handle_unknown = "error"), SimpleImputer(), clone(classifier), "Base4EncoderAuditNA")

classifier = RandomForestClassifier(n_estimators = 71, random_state = 13)

build_audit(BinaryEncoder(handle_missing = "error", handle_unknown = "error"), "passthrough", clone(classifier), "BinaryEncoderAudit", compact = False)
build_audit(CatBoostEncoder(a = 0.5, handle_missing = "error", handle_unknown = "error"), "passthrough", clone(classifier), "CatBoostEncoderAudit", compact = False, numeric = True)
build_audit(CountEncoder(normalize = True, min_group_size = 0.05, handle_missing = "error", handle_unknown = "error"), "passthrough", clone(classifier), "CountEncoderAudit", compact = False)
build_audit(LeaveOneOutEncoder(handle_missing = "error", handle_unknown = "error"), "passthrough", clone(classifier), "LeaveOneOutEncoderAudit", compact = False, numeric = True)
build_audit(TargetEncoder(handle_missing = "error", handle_unknown = "error"), "passthrough", clone(classifier), "TargetEncoderAudit", compact = False)
build_audit(WOEEncoder(handle_missing = "error", handle_unknown = "error"), "passthrough", clone(classifier), "WOEEncoderAudit", compact = False)

classifier = LogisticRegression()

build_audit(BinaryEncoder(handle_missing = "value", handle_unknown = "error"), SimpleImputer(), clone(classifier), "BinaryEncoderAuditNA")
build_audit(CatBoostEncoder(a = 0.5, handle_missing = "value", handle_unknown = "error"), SimpleImputer(), clone(classifier), "CatBoostEncoderAuditNA")
build_audit(CountEncoder(min_group_size = 10, handle_missing = "count", handle_unknown = "error"), SimpleImputer(), clone(classifier), "CountEncoderAuditNA")
build_audit(LeaveOneOutEncoder(handle_missing = "value", handle_unknown = "error"), SimpleImputer(), clone(classifier), "LeaveOneOutEncoderAuditNA")
build_audit(TargetEncoder(handle_missing = "value", handle_unknown = "error"), SimpleImputer(), clone(classifier), "TargetEncoderAuditNA")
build_audit(WOEEncoder(handle_missing = "value", handle_unknown = "error"), SimpleImputer(), clone(classifier), "WOEEncoderAuditNA")