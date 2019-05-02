

/* First created by JCasGen Thu May 02 10:37:39 CEST 2019 */
package de.unidue.ltl.escrito.core.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Thu May 02 10:37:39 CEST 2019
 * XML source: /Users/andrea/git/escrito/de.unidue.ltl.escrito/de.unidue.ltl.escrito.core/src/main/resources/desc/type/Escrito.xml
 * @generated */
public class RutaAnnotation extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(RutaAnnotation.class);
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
  protected RutaAnnotation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public RutaAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public RutaAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public RutaAnnotation(JCas jcas, int begin, int end) {
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
  //* Feature: featureName

  /** getter for featureName - gets 
   * @generated
   * @return value of the feature 
   */
  public String getFeatureName() {
    if (RutaAnnotation_Type.featOkTst && ((RutaAnnotation_Type)jcasType).casFeat_featureName == null)
      jcasType.jcas.throwFeatMissing("featureName", "de.unidue.ltl.escrito.core.types.RutaAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((RutaAnnotation_Type)jcasType).casFeatCode_featureName);}
    
  /** setter for featureName - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setFeatureName(String v) {
    if (RutaAnnotation_Type.featOkTst && ((RutaAnnotation_Type)jcasType).casFeat_featureName == null)
      jcasType.jcas.throwFeatMissing("featureName", "de.unidue.ltl.escrito.core.types.RutaAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((RutaAnnotation_Type)jcasType).casFeatCode_featureName, v);}    
  }

    