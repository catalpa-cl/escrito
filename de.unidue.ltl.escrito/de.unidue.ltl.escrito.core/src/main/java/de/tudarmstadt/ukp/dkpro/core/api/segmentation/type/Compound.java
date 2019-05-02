

/* First created by JCasGen Wed Feb 20 15:07:53 CET 2019 */
package de.tudarmstadt.ukp.dkpro.core.api.segmentation.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;


/** This type represents a decompounding word, i.e.: flowerpot. Each Compound one have at least two Splits.
 * Updated by JCasGen Thu May 02 10:37:39 CEST 2019
 * XML source: /Users/andrea/git/escrito/de.unidue.ltl.escrito/de.unidue.ltl.escrito.core/src/main/resources/desc/type/Escrito.xml
 * @generated */
public class Compound extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Compound.class);
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
  protected Compound() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Compound(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Compound(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Compound(JCas jcas, int begin, int end) {
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
  //* Feature: splits

  /** getter for splits - gets A word that can be decomposed into different parts.
   * @generated
   * @return value of the feature 
   */
  public FSArray getSplits() {
    if (Compound_Type.featOkTst && ((Compound_Type)jcasType).casFeat_splits == null)
      jcasType.jcas.throwFeatMissing("splits", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Compound_Type)jcasType).casFeatCode_splits)));}
    
  /** setter for splits - sets A word that can be decomposed into different parts. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setSplits(FSArray v) {
    if (Compound_Type.featOkTst && ((Compound_Type)jcasType).casFeat_splits == null)
      jcasType.jcas.throwFeatMissing("splits", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound");
    jcasType.ll_cas.ll_setRefValue(addr, ((Compound_Type)jcasType).casFeatCode_splits, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for splits - gets an indexed value - A word that can be decomposed into different parts.
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public Split getSplits(int i) {
    if (Compound_Type.featOkTst && ((Compound_Type)jcasType).casFeat_splits == null)
      jcasType.jcas.throwFeatMissing("splits", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Compound_Type)jcasType).casFeatCode_splits), i);
    return (Split)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Compound_Type)jcasType).casFeatCode_splits), i)));}

  /** indexed setter for splits - sets an indexed value - A word that can be decomposed into different parts.
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setSplits(int i, Split v) { 
    if (Compound_Type.featOkTst && ((Compound_Type)jcasType).casFeat_splits == null)
      jcasType.jcas.throwFeatMissing("splits", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Compound_Type)jcasType).casFeatCode_splits), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Compound_Type)jcasType).casFeatCode_splits), i, jcasType.ll_cas.ll_getFSRef(v));}
  }

    