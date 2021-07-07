
/* First created by JCasGen Wed Dec 14 16:15:38 CET 2016 */
package de.unidue.ltl.escrito.core.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Mon Jan 27 15:16:25 CET 2020
 * @generated */
public class LearnerAnswer_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = LearnerAnswer.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.unidue.ltl.escrito.core.types.LearnerAnswer");
 
  /** @generated */
  final Feature casFeat_promptId;
  /** @generated */
  final int     casFeatCode_promptId;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getPromptId(int addr) {
        if (featOkTst && casFeat_promptId == null)
      jcas.throwFeatMissing("promptId", "de.unidue.ltl.escrito.core.types.LearnerAnswer");
    return ll_cas.ll_getStringValue(addr, casFeatCode_promptId);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setPromptId(int addr, String v) {
        if (featOkTst && casFeat_promptId == null)
      jcas.throwFeatMissing("promptId", "de.unidue.ltl.escrito.core.types.LearnerAnswer");
    ll_cas.ll_setStringValue(addr, casFeatCode_promptId, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public LearnerAnswer_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_promptId = jcas.getRequiredFeatureDE(casType, "promptId", "uima.cas.String", featOkTst);
    casFeatCode_promptId  = (null == casFeat_promptId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_promptId).getCode();

  }
}



    
