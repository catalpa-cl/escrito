

/* First created by JCasGen Fri Jan 13 14:17:05 CET 2017 */
package de.unidue.ltl.escrito.core.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;


import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Mon Jan 27 15:16:25 CET 2020
 * XML source: /Users/andrea/git/escrito/de.unidue.ltl.escrito/de.unidue.ltl.escrito.core/src/main/resources/desc/type/Escrito.xml
 * @generated */
public class LearnerAnswerToken extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(LearnerAnswerToken.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected LearnerAnswerToken() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public LearnerAnswerToken(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public LearnerAnswerToken(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public LearnerAnswerToken(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: isQuestionMaterial

  /** getter for isQuestionMaterial - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getIsQuestionMaterial() {
    if (LearnerAnswerToken_Type.featOkTst && ((LearnerAnswerToken_Type)jcasType).casFeat_isQuestionMaterial == null)
      jcasType.jcas.throwFeatMissing("isQuestionMaterial", "de.unidue.ltl.escrito.core.types.LearnerAnswerToken");
    return jcasType.ll_cas.ll_getBooleanValue(addr, ((LearnerAnswerToken_Type)jcasType).casFeatCode_isQuestionMaterial);}
    
  /** setter for isQuestionMaterial - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setIsQuestionMaterial(boolean v) {
    if (LearnerAnswerToken_Type.featOkTst && ((LearnerAnswerToken_Type)jcasType).casFeat_isQuestionMaterial == null)
      jcasType.jcas.throwFeatMissing("isQuestionMaterial", "de.unidue.ltl.escrito.core.types.LearnerAnswerToken");
    jcasType.ll_cas.ll_setBooleanValue(addr, ((LearnerAnswerToken_Type)jcasType).casFeatCode_isQuestionMaterial, v);}    
   
    
  //*--------------*
  //* Feature: token

  /** getter for token - gets 
   * @generated
   * @return value of the feature 
   */
  public Token getToken() {
    if (LearnerAnswerToken_Type.featOkTst && ((LearnerAnswerToken_Type)jcasType).casFeat_token == null)
      jcasType.jcas.throwFeatMissing("token", "de.unidue.ltl.escrito.core.types.LearnerAnswerToken");
    return (Token)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((LearnerAnswerToken_Type)jcasType).casFeatCode_token)));}
    
  /** setter for token - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setToken(Token v) {
    if (LearnerAnswerToken_Type.featOkTst && ((LearnerAnswerToken_Type)jcasType).casFeat_token == null)
      jcasType.jcas.throwFeatMissing("token", "de.unidue.ltl.escrito.core.types.LearnerAnswerToken");
    jcasType.ll_cas.ll_setRefValue(addr, ((LearnerAnswerToken_Type)jcasType).casFeatCode_token, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: isStopWord

  /** getter for isStopWord - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getIsStopWord() {
    if (LearnerAnswerToken_Type.featOkTst && ((LearnerAnswerToken_Type)jcasType).casFeat_isStopWord == null)
      jcasType.jcas.throwFeatMissing("isStopWord", "de.unidue.ltl.escrito.core.types.LearnerAnswerToken");
    return jcasType.ll_cas.ll_getBooleanValue(addr, ((LearnerAnswerToken_Type)jcasType).casFeatCode_isStopWord);}
    
  /** setter for isStopWord - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setIsStopWord(boolean v) {
    if (LearnerAnswerToken_Type.featOkTst && ((LearnerAnswerToken_Type)jcasType).casFeat_isStopWord == null)
      jcasType.jcas.throwFeatMissing("isStopWord", "de.unidue.ltl.escrito.core.types.LearnerAnswerToken");
    jcasType.ll_cas.ll_setBooleanValue(addr, ((LearnerAnswerToken_Type)jcasType).casFeatCode_isStopWord, v);}    
   
    
  //*--------------*
  //* Feature: isPunctuation

  /** getter for isPunctuation - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getIsPunctuation() {
    if (LearnerAnswerToken_Type.featOkTst && ((LearnerAnswerToken_Type)jcasType).casFeat_isPunctuation == null)
      jcasType.jcas.throwFeatMissing("isPunctuation", "de.unidue.ltl.escrito.core.types.LearnerAnswerToken");
    return jcasType.ll_cas.ll_getBooleanValue(addr, ((LearnerAnswerToken_Type)jcasType).casFeatCode_isPunctuation);}
    
  /** setter for isPunctuation - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setIsPunctuation(boolean v) {
    if (LearnerAnswerToken_Type.featOkTst && ((LearnerAnswerToken_Type)jcasType).casFeat_isPunctuation == null)
      jcasType.jcas.throwFeatMissing("isPunctuation", "de.unidue.ltl.escrito.core.types.LearnerAnswerToken");
    jcasType.ll_cas.ll_setBooleanValue(addr, ((LearnerAnswerToken_Type)jcasType).casFeatCode_isPunctuation, v);}    
  }

    
