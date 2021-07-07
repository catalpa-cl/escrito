
/* First created by JCasGen Fri Jan 13 14:17:05 CET 2017 */
package de.unidue.ltl.escrito.core.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token_Type;

/** 
 * Updated by JCasGen Mon Jan 27 15:16:25 CET 2020
 * @generated */
public class LearnerAnswerToken_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = LearnerAnswerToken.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.unidue.ltl.escrito.core.types.LearnerAnswerToken");
 
  /** @generated */
  final Feature casFeat_isQuestionMaterial;
  /** @generated */
  final int     casFeatCode_isQuestionMaterial;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public boolean getIsQuestionMaterial(int addr) {
        if (featOkTst && casFeat_isQuestionMaterial == null)
      jcas.throwFeatMissing("isQuestionMaterial", "de.unidue.ltl.escrito.core.types.LearnerAnswerToken");
    return ll_cas.ll_getBooleanValue(addr, casFeatCode_isQuestionMaterial);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setIsQuestionMaterial(int addr, boolean v) {
        if (featOkTst && casFeat_isQuestionMaterial == null)
      jcas.throwFeatMissing("isQuestionMaterial", "de.unidue.ltl.escrito.core.types.LearnerAnswerToken");
    ll_cas.ll_setBooleanValue(addr, casFeatCode_isQuestionMaterial, v);}
    
  
 
  /** @generated */
  final Feature casFeat_token;
  /** @generated */
  final int     casFeatCode_token;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getToken(int addr) {
        if (featOkTst && casFeat_token == null)
      jcas.throwFeatMissing("token", "de.unidue.ltl.escrito.core.types.LearnerAnswerToken");
    return ll_cas.ll_getRefValue(addr, casFeatCode_token);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setToken(int addr, int v) {
        if (featOkTst && casFeat_token == null)
      jcas.throwFeatMissing("token", "de.unidue.ltl.escrito.core.types.LearnerAnswerToken");
    ll_cas.ll_setRefValue(addr, casFeatCode_token, v);}
    
  
 
  /** @generated */
  final Feature casFeat_isStopWord;
  /** @generated */
  final int     casFeatCode_isStopWord;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public boolean getIsStopWord(int addr) {
        if (featOkTst && casFeat_isStopWord == null)
      jcas.throwFeatMissing("isStopWord", "de.unidue.ltl.escrito.core.types.LearnerAnswerToken");
    return ll_cas.ll_getBooleanValue(addr, casFeatCode_isStopWord);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setIsStopWord(int addr, boolean v) {
        if (featOkTst && casFeat_isStopWord == null)
      jcas.throwFeatMissing("isStopWord", "de.unidue.ltl.escrito.core.types.LearnerAnswerToken");
    ll_cas.ll_setBooleanValue(addr, casFeatCode_isStopWord, v);}
    
  
 
  /** @generated */
  final Feature casFeat_isPunctuation;
  /** @generated */
  final int     casFeatCode_isPunctuation;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public boolean getIsPunctuation(int addr) {
        if (featOkTst && casFeat_isPunctuation == null)
      jcas.throwFeatMissing("isPunctuation", "de.unidue.ltl.escrito.core.types.LearnerAnswerToken");
    return ll_cas.ll_getBooleanValue(addr, casFeatCode_isPunctuation);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setIsPunctuation(int addr, boolean v) {
        if (featOkTst && casFeat_isPunctuation == null)
      jcas.throwFeatMissing("isPunctuation", "de.unidue.ltl.escrito.core.types.LearnerAnswerToken");
    ll_cas.ll_setBooleanValue(addr, casFeatCode_isPunctuation, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public LearnerAnswerToken_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_isQuestionMaterial = jcas.getRequiredFeatureDE(casType, "isQuestionMaterial", "uima.cas.Boolean", featOkTst);
    casFeatCode_isQuestionMaterial  = (null == casFeat_isQuestionMaterial) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_isQuestionMaterial).getCode();

 
    casFeat_token = jcas.getRequiredFeatureDE(casType, "token", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token", featOkTst);
    casFeatCode_token  = (null == casFeat_token) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_token).getCode();

 
    casFeat_isStopWord = jcas.getRequiredFeatureDE(casType, "isStopWord", "uima.cas.Boolean", featOkTst);
    casFeatCode_isStopWord  = (null == casFeat_isStopWord) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_isStopWord).getCode();

 
    casFeat_isPunctuation = jcas.getRequiredFeatureDE(casType, "isPunctuation", "uima.cas.Boolean", featOkTst);
    casFeatCode_isPunctuation  = (null == casFeat_isPunctuation) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_isPunctuation).getCode();

  }
}



    
