

/* First created by JCasGen Wed Dec 14 17:16:31 CET 2016 */
package de.unidue.ltl.escrito.core.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



import org.apache.uima.jcas.cas.StringArray;


/** 
 * Updated by JCasGen Mon Jan 27 15:16:25 CET 2020
 * XML source: /Users/andrea/git/escrito/de.unidue.ltl.escrito/de.unidue.ltl.escrito.core/src/main/resources/desc/type/Escrito.xml
 * @generated */
public class LearnerAnswerWithReferenceAnswer extends LearnerAnswer {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(LearnerAnswerWithReferenceAnswer.class);
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
  protected LearnerAnswerWithReferenceAnswer() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public LearnerAnswerWithReferenceAnswer(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public LearnerAnswerWithReferenceAnswer(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public LearnerAnswerWithReferenceAnswer(JCas jcas, int begin, int end) {
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
  //* Feature: referenceAnswers

  /** getter for referenceAnswers - gets 
   * @generated
   * @return value of the feature 
   */
  public StringArray getReferenceAnswers() {
    if (LearnerAnswerWithReferenceAnswer_Type.featOkTst && ((LearnerAnswerWithReferenceAnswer_Type)jcasType).casFeat_referenceAnswers == null)
      jcasType.jcas.throwFeatMissing("referenceAnswers", "de.unidue.ltl.escrito.core.types.LearnerAnswerWithReferenceAnswer");
    return (StringArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((LearnerAnswerWithReferenceAnswer_Type)jcasType).casFeatCode_referenceAnswers)));}
    
  /** setter for referenceAnswers - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setReferenceAnswers(StringArray v) {
    if (LearnerAnswerWithReferenceAnswer_Type.featOkTst && ((LearnerAnswerWithReferenceAnswer_Type)jcasType).casFeat_referenceAnswers == null)
      jcasType.jcas.throwFeatMissing("referenceAnswers", "de.unidue.ltl.escrito.core.types.LearnerAnswerWithReferenceAnswer");
    jcasType.ll_cas.ll_setRefValue(addr, ((LearnerAnswerWithReferenceAnswer_Type)jcasType).casFeatCode_referenceAnswers, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for referenceAnswers - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public String getReferenceAnswers(int i) {
    if (LearnerAnswerWithReferenceAnswer_Type.featOkTst && ((LearnerAnswerWithReferenceAnswer_Type)jcasType).casFeat_referenceAnswers == null)
      jcasType.jcas.throwFeatMissing("referenceAnswers", "de.unidue.ltl.escrito.core.types.LearnerAnswerWithReferenceAnswer");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((LearnerAnswerWithReferenceAnswer_Type)jcasType).casFeatCode_referenceAnswers), i);
    return jcasType.ll_cas.ll_getStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((LearnerAnswerWithReferenceAnswer_Type)jcasType).casFeatCode_referenceAnswers), i);}

  /** indexed setter for referenceAnswers - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setReferenceAnswers(int i, String v) { 
    if (LearnerAnswerWithReferenceAnswer_Type.featOkTst && ((LearnerAnswerWithReferenceAnswer_Type)jcasType).casFeat_referenceAnswers == null)
      jcasType.jcas.throwFeatMissing("referenceAnswers", "de.unidue.ltl.escrito.core.types.LearnerAnswerWithReferenceAnswer");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((LearnerAnswerWithReferenceAnswer_Type)jcasType).casFeatCode_referenceAnswers), i);
    jcasType.ll_cas.ll_setStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((LearnerAnswerWithReferenceAnswer_Type)jcasType).casFeatCode_referenceAnswers), i, v);}
   
    
  //*--------------*
  //* Feature: referenceAnswerIds

  /** getter for referenceAnswerIds - gets 
   * @generated
   * @return value of the feature 
   */
  public StringArray getReferenceAnswerIds() {
    if (LearnerAnswerWithReferenceAnswer_Type.featOkTst && ((LearnerAnswerWithReferenceAnswer_Type)jcasType).casFeat_referenceAnswerIds == null)
      jcasType.jcas.throwFeatMissing("referenceAnswerIds", "de.unidue.ltl.escrito.core.types.LearnerAnswerWithReferenceAnswer");
    return (StringArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((LearnerAnswerWithReferenceAnswer_Type)jcasType).casFeatCode_referenceAnswerIds)));}
    
  /** setter for referenceAnswerIds - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setReferenceAnswerIds(StringArray v) {
    if (LearnerAnswerWithReferenceAnswer_Type.featOkTst && ((LearnerAnswerWithReferenceAnswer_Type)jcasType).casFeat_referenceAnswerIds == null)
      jcasType.jcas.throwFeatMissing("referenceAnswerIds", "de.unidue.ltl.escrito.core.types.LearnerAnswerWithReferenceAnswer");
    jcasType.ll_cas.ll_setRefValue(addr, ((LearnerAnswerWithReferenceAnswer_Type)jcasType).casFeatCode_referenceAnswerIds, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for referenceAnswerIds - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public String getReferenceAnswerIds(int i) {
    if (LearnerAnswerWithReferenceAnswer_Type.featOkTst && ((LearnerAnswerWithReferenceAnswer_Type)jcasType).casFeat_referenceAnswerIds == null)
      jcasType.jcas.throwFeatMissing("referenceAnswerIds", "de.unidue.ltl.escrito.core.types.LearnerAnswerWithReferenceAnswer");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((LearnerAnswerWithReferenceAnswer_Type)jcasType).casFeatCode_referenceAnswerIds), i);
    return jcasType.ll_cas.ll_getStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((LearnerAnswerWithReferenceAnswer_Type)jcasType).casFeatCode_referenceAnswerIds), i);}

  /** indexed setter for referenceAnswerIds - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setReferenceAnswerIds(int i, String v) { 
    if (LearnerAnswerWithReferenceAnswer_Type.featOkTst && ((LearnerAnswerWithReferenceAnswer_Type)jcasType).casFeat_referenceAnswerIds == null)
      jcasType.jcas.throwFeatMissing("referenceAnswerIds", "de.unidue.ltl.escrito.core.types.LearnerAnswerWithReferenceAnswer");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((LearnerAnswerWithReferenceAnswer_Type)jcasType).casFeatCode_referenceAnswerIds), i);
    jcasType.ll_cas.ll_setStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((LearnerAnswerWithReferenceAnswer_Type)jcasType).casFeatCode_referenceAnswerIds), i, v);}
  }

    
