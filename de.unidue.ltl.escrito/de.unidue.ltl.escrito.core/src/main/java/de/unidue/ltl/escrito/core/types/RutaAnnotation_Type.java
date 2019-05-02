
/* First created by JCasGen Thu May 02 10:37:39 CEST 2019 */
package de.unidue.ltl.escrito.core.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Thu May 02 10:37:39 CEST 2019
 * @generated */
public class RutaAnnotation_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = RutaAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.unidue.ltl.escrito.core.types.RutaAnnotation");
 
  /** @generated */
  final Feature casFeat_featureName;
  /** @generated */
  final int     casFeatCode_featureName;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getFeatureName(int addr) {
        if (featOkTst && casFeat_featureName == null)
      jcas.throwFeatMissing("featureName", "de.unidue.ltl.escrito.core.types.RutaAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_featureName);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setFeatureName(int addr, String v) {
        if (featOkTst && casFeat_featureName == null)
      jcas.throwFeatMissing("featureName", "de.unidue.ltl.escrito.core.types.RutaAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_featureName, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public RutaAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_featureName = jcas.getRequiredFeatureDE(casType, "featureName", "uima.cas.String", featOkTst);
    casFeatCode_featureName  = (null == casFeat_featureName) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_featureName).getCode();

  }
}



    