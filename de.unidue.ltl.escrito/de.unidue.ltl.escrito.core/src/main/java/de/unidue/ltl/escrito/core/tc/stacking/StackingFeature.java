package de.unidue.ltl.escrito.core.tc.stacking;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureType;

public class StackingFeature extends Feature{

	private int stackingGroupId;
	
	public int getStackingGroupId(){
		return stackingGroupId;
	}
	
	@Override
	public String getName(){
		return "stackingGroup:"+this.stackingGroupId+"_"+super.getName();
	}
	
	
	public StackingFeature(String name, Object value, FeatureType type, int id) throws TextClassificationException {
		super(name, value, type);
		this.stackingGroupId = id;
	}

}
