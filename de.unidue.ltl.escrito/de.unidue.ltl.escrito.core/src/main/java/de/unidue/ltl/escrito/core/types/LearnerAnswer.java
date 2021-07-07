

/* First created by JCasGen Wed Dec 14 16:15:38 CET 2016 */
package de.unidue.ltl.escrito.core.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Mon Jan 27 15:16:25 CET 2020
 * XML source: /Users/andrea/git/escrito/de.unidue.ltl.escrito/de.unidue.ltl.escrito.core/src/main/resources/desc/type/Escrito.xml
 * @generated */
public class LearnerAnswer extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(LearnerAnswer.class);
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
  protected LearnerAnswer() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public LearnerAnswer(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public LearnerAnswer(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public LearnerAnswer(JCas jcas, int begin, int end) {
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
  //* Feature: promptId

  /** getter for promptId - gets 
   * @generated
   * @return value of the feature 
   */
  public String getPromptId() {
    if (LearnerAnswer_Type.featOkTst && ((LearnerAnswer_Type)jcasType).casFeat_promptId == null)
      jcasType.jcas.throwFeatMissing("promptId", "de.unidue.ltl.escrito.core.types.LearnerAnswer");
    return jcasType.ll_cas.ll_getStringValue(addr, ((LearnerAnswer_Type)jcasType).casFeatCode_promptId);}
    
  /** setter for promptId - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPromptId(String v) {
    if (LearnerAnswer_Type.featOkTst && ((LearnerAnswer_Type)jcasType).casFeat_promptId == null)
      jcasType.jcas.throwFeatMissing("promptId", "de.unidue.ltl.escrito.core.types.LearnerAnswer");
    jcasType.ll_cas.ll_setStringValue(addr, ((LearnerAnswer_Type)jcasType).casFeatCode_promptId, v);}    
  }

    
