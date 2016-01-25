package weka.filters.unsupervised.attribute;

import java.util.Enumeration;
import java.util.Vector;

import jdk.nashorn.internal.runtime.NumberToString;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.FastVector;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.UnsupportedAttributeTypeException;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.UnsupervisedFilter;

/**
 * <!-- globlinfo-start -->
 * Cut part of the data, specified by user.
 * <!-- globalinfo-end -->
 * <!-- options-start -->
 * Valid options are: <p/>
 * 
 * <pre> -L &lt;col&gt
 * 	 Set length of string to be removed from data.</pre>
 * <!-- options-end -->
 * @author Germans Zaharovs (germans@germans.me.uk)
 * @version $Revision: 1.0 $
 */
public class RemoveSubString extends Filter implements UnsupervisedFilter, OptionHandler {

	/** for serialization */
	static final long serialVersionUID = 6969696969696969L;
	
	/** The attribute's index setting */
	private Range m_AttIndex = new Range("last");
	
	/** Starting position of substring option */
	private int startPosition = 0;
	
	/** Ending position of substring option */
	private int lastPosition = -999;
	
	/**
	 * Returns a string describing this filter
	 * 
	 * @return		a description of the filter suitable for 
	 * 			displaying in the explorer & experimenter gui
	 */
	public String globalInfo(){
		return
				"Delete from <String, Nominal, Numeric> data types specified substring";
	}
	
	/**
	 * Returns the Capabilities of this filter.
	 * 
	 * @return		the capabilities of this object
	 * @see			Capabilities
	 */
	public Capabilities getCapabilities(){
		Capabilities result = super.getCapabilities();
		result.disableAll();
		
		//attributes
		result.enableAllAttributes();
		result.enable(Capability.MISSING_VALUES);
		
		//class
		result.enableAllClasses();
		result.enable(Capability.MISSING_CLASS_VALUES);
		result.enable(Capability.NO_CLASS);
		
		return result;
	}
	
	 /**
	   * Sets the format of the input instances.
	   *
	   * @param instanceInfo 	an Instances object containing the input 
	   * 				instance structure (any instances contained 
	   * 				in the object are ignored - only the 
	   * 				structure is required).
	   * @return 			true if the outputFormat may be collected 
	   * 				immediately.
	   * @throws UnsupportedAttributeTypeException 	if the selected attribute
	   * 						a string attribute.
	   * @throws Exception 		if the input format can't be set 
	   * 				successfully.
	   */
	public boolean setInputFormat(Instances instanceInfo) throws Exception{
		super.setInputFormat(instanceInfo);
		
		m_AttIndex.setUpper(instanceInfo.numAttributes()-1);
		
		return false;
	}
	
	  /**
	   * Input an instance for filtering. The instance is processed
	   * and made available for output immediately.
	   *
	   * @param instance 		the input instance.
	   * @return 			true if the filtered instance may now be
	   * 				collected with output().
	   * @throws IllegalStateException 	if no input structure has been defined.
	   */
	public boolean input(Instance instance){
		if(getInputFormat() == null){
			throw new IllegalStateException("No input instance format defined");
		}
		
		if(m_NewBatch){
			resetQueue();
			m_NewBatch = false;
		}
		
		if(isOutputFormatDefined()){
			Instance newInstance = (Instance) instance.copy();
			push(newInstance);
			return true;
		}
		
		bufferInput(instance);
		return false;
	}
	
	  /**
	   * Signifies that this batch of input to the filter is finished. If the 
	   * filter requires all instances prior to filtering, output() may now 
	   * be called to retrieve the filtered instances.
	   *
	   * @return 		true if there are instances pending output.
	   * @throws IllegalStateException 	if no input structure has been defined.
	   */
	  public boolean batchFinished() {
	    if (getInputFormat() == null)
	      throw new IllegalStateException("No input instance format defined");

	    if (!isOutputFormatDefined()) {
	      setOutputFormat();

	      // Convert pending input instances
	      for(int i = 0; i < getInputFormat().numInstances(); i++)
		push((Instance) getInputFormat().instance(i).copy());
	    } 

	    flushInput();
	    m_NewBatch = true;

	    return (numPendingOutput() != 0);
	  }
	  
	 /**
	  * Returns an enumeration describing the available options.
	  * 
	  * @return			an enumeration of all available options
	  */
	  @Override
	public Enumeration listOptions() {
		Vector result = new Vector();
		
		result.addElement(new Option(
				"\tSets the range of attributes to be manipulated (default last)", 
				"C", 1, "-C <col>"));
		
		result.addElement(new Option(
				"\tSpecify the last position of the string to be removed (default is last)",
				"L", 1, "-L <position>"
				));
		
		result.addElement(new Option(
				"\tSpecify the start position of the string to be removed (default is first)",
				"S", 1, "-S <position>"
				));
		return result.elements();
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		String tmpStr;
		
		tmpStr = Utils.getOption("C", options);
		if(tmpStr.length() != 0){
			setAttributeIndexes(tmpStr);
		} else{
			setAttributeIndexes("last");
		}
		
		tmpStr = Utils.getOption("L", options);
		if(tmpStr.length() != 0){
			setLastPosition(tmpStr);
		} 
		
		tmpStr = Utils.getOption("S", options);
		if(tmpStr.length() != 0){
			setStartPosition(tmpStr);
		}

	}

	/**
	   * Gets the current settings of the filter.
	   *
	   * @return 		an array of strings suitable for passing to setOptions
	   */
		@Override
	  public String[] getOptions() {
	    Vector    result;

	    result = new Vector();

	    result.add("-C");
	    result.add("" + (getAttributeIndexes()));

	    result.add("-L");
	    result.add("" + (getLastPosition()));
	    
	    result.add("-S");
	    result.add("" + (getStartPosition()));
	    
	    return (String[]) result.toArray(new String[result.size()]);	  
	  }
	  
	  /**
	   * Returns the tip text for this property
	   * 
	   * @return 		tip text for this property suitable for
	   * 			displaying in the explorer/experimenter gui
	   */
	  public String attributeIndexesTipText() {
	    return "Sets a range attributes to process. Any data type "
	      + "attributes in the range are left untouched (\"first\" and \"last\" are valid values)";
	  }
	  
	  /**
	   * Get the index of the attribute used.
	   *
	   * @return 		the index of the attribute
	   */
	  public String getAttributeIndexes() {
	    //    return m_AttIndex.getSingleIndex();
	    return m_AttIndex.getRanges();
	  }
	  
	  /**
	   * Get the index of the last index
	   * 
	   * @return	position of the last index
	   */
	  public String getLastPosition(){
		  return Integer.toString(this.lastPosition);
	  }
	  
	  /**
	   * Get the index of the first index
	   * 
	   * @return	position of the first index
	   */
	  public String getStartPosition(){
		  return Integer.toString(this.startPosition);
	  }
	  
	  /**
	   * Sets index of the attribute used.
	   *
	   * @param attIndex 	the index of the attribute
	   */
	  public void setAttributeIndexes(String attIndex) {
	    //    m_AttIndex.setSingleIndex(attIndex);
	    m_AttIndex.setRanges(attIndex);
	  }
	  
	  /**
	   * Sets last position of the String
	   * 
	   * @param lastPosition Index position of the last element
	   * @throws Exception if parameter is not of type integer
	   */
	  public void setLastPosition(String lastPosition)throws Exception{
		  try{
			  this.lastPosition = Integer.parseInt(lastPosition);
		  }catch (NumberFormatException e){
			  throw new Exception ("-L must be of type integer");
		  }
	  }
	  
	  /**
	   * Sets first position of the String
	   * 
	   * @param lastPosition Index position of the first element
	   * @throws Exception if parameter is not of type integer
	   */
	  public void setStartPosition(String firstPosition) throws Exception{
		  try{
			  this.startPosition = Integer.parseInt(firstPosition);
		  }catch (NumberFormatException e){
			  throw new Exception("-S must be of type integer");
		  }
	  }
	  
	  /**
	   * Set the output format. Takes the current average class values
	   * and m_InputFormat and calls setOutputFormat(Instances) 
	   * appropriately.
	   */
	  private void setOutputFormat() {
	    Instances 	newData = getInputFormat();
	    FastVector 	newAtts, newVals;
	    int typeOfAttr = 0;
	    
	     
	    //compute new attributes
	    newAtts = new FastVector(getInputFormat().numAttributes());
	    
	  
	    for (int j=0; j< m_AttIndex.getSelection().length; j++) {
	    	 Attribute att = getInputFormat().attribute(m_AttIndex.getSelection()[j]);
		      //determine what format is datatype
		      if(att.isNominal()){
		    	  typeOfAttr = Attribute.NOMINAL;
		      } else if(att.isNumeric()){
		    	  typeOfAttr = Attribute.NUMERIC;
		      } else if (att.isString()){
		    	  typeOfAttr = Attribute.STRING;
		      }
		     
		      
		      //if datatype is different than string, than change format
		      switch (typeOfAttr){
		      case Attribute.NOMINAL:
		    	 try{		    	 
			    	 newData = this.nominalToString(getInputFormat(), new NominalToString());
		    	 }catch(Exception e){
		    		 //do nothing in here?
		    		 System.out.println(e.getMessage());
		    	 }
			    break;
		      case Attribute.NUMERIC:
		    	  try {    		  
					  newData = this.nominalToString(getInputFormat(), new NumericToNominal());
					  newData = this.nominalToString(newData, new NominalToString());
				} catch (Exception e) {
					// do nothing in here?
					System.out.println(e.getMessage());
				}
		    	 break;
		      }
		      
		      
		      att = newData.attribute(m_AttIndex.getSelection()[j]);
		     
	    		  
		      //loop through all the values 
		      for(int i = 0; i < att.numValues(); i++){
		    	  //get value of each of the indexes
		    	  String temp = att.value(i);
		    	  String toRemove = "";
		    	  int startPositionLocal = 0;
		    	  //make logic of substring in here
	    		  if(this.startPosition<0){
	    			  //calculate correct position in here
	    			  startPositionLocal = temp.length() - Math.abs(this.startPosition);
	    		  }else{
	    			  startPositionLocal = this.startPosition;
	    		  }
		    	  if(this.lastPosition !=-999){
		    		  toRemove = temp.substring(startPositionLocal, this.lastPosition); 
		    		  temp = temp.substring(0, temp.indexOf(toRemove));
		    	  }else{
		    		  toRemove = temp.substring(startPositionLocal, temp.length());
		    		  temp = temp.substring(0, temp.indexOf(toRemove));
		    	  }
		    	  //put this value back to attribute
		    	  newData.renameAttributeValue(att, att.value(i), temp);
		      }
		    }
	    // save new data into dataset
	    setOutputFormat(newData);
	  }
	  
	  /**
	   * Returns the revision string.
	   * 
	   * @return		the revision
	   */
	  public String getRevision() {
	    return RevisionUtils.extract("$Revision: 1.0 $");
	  }
	  
	  /**
	   * Main method for testing this class.
	   *
	   * @param args 	should contain arguments to the filter: use -h for help
	   */
	  public static void main(String [] args) {
	    runFilter(new RemoveSubString(), args);
	  }
	  
	  /**
	   * 
	   * @param dataIn Dataset to be processed
	   * @param filterIn Filter to be applied for dataset
	   * @return Dataset with applied filter
	   * @throws Exception if filter is not defined in this implementation
	   */
	  private Instances nominalToString(Instances dataIn, Filter filterIn) throws Exception{
		  if(filterIn instanceof NominalToString){
			//make required filter of nominal to string
		      NominalToString nomToString = new NominalToString();
		      nomToString.setInputFormat(dataIn);
		      return Filter.useFilter(getInputFormat(), nomToString);
		  } else if(filterIn instanceof NumericToNominal){
			  NumericToNominal numToNominal = new NumericToNominal();
			  numToNominal.setInputFormat(dataIn);
			  return Filter.useFilter(dataIn, numToNominal);
		  }else{
			  throw new Exception("No filter implemented");
		  }
	  }
	  
}
