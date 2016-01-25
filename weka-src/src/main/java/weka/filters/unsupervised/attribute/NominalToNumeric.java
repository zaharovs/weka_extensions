package weka.filters.unsupervised.attribute;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.plaf.basic.BasicComboBoxUI.PropertyChangeHandler;

import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.FastVector;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.ProtectedProperties;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.UnsupportedAttributeTypeException;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.StreamableFilter;
import weka.filters.UnsupervisedFilter;

/**
 * <!-- globalinfo-start -->
 * Converts a nominal attribute (i.e. specific number of value) to numeric.
 * You should ensure that all nominal values that will appear will be of type numeric.
 * <p/>
 * <!-- globalinfo-end -->
 * 
 * <!-- options-start -->
 * Valid options are:
 * <p/>
 * 
 * <pre>
 * -R &lt;col&gt;
 *  Sets the range of attributes indices (default last)
 * </pre>
 * 
 * <!-- options-end -->
 * 
 * 
 * @author Germans Zaharovs (germans@germans.me.uk)
 * @version $Revision: 1.0 $
 */
public class NominalToNumeric extends Filter implements UnsupervisedFilter, OptionHandler, StreamableFilter {

	/** for serialization */
	private static final long serialVersionUID = 6969696969696970L;
	
	/** The attribute's range indices setting. */
	private Range m_AttIndices = new Range("last");
	
	/** TODO make points later */
	private Map<Integer, Attribute> attributes = new HashMap<>();
	
	/**
	 * Returns a string describing this filter
	 * 
	 * @return a description of the filter suitable for displaying in the 
	 * 				explorer/experimenter gui
	 */
	public String globalInfo(){
		return "Converts a range of nominal attributes (specified number of values) to numeric"
				+ ". You should ensure that all values in attribute are numeric to successfully "
				+ "apply this filter.";
	}
	
	/**
	 * Returns the Capabilities of this filter.
	 * 
	 * @return the capabilities of this object
	 * @see Capabilities
	 */
	@Override
	public Capabilities getCapabilities(){
		Capabilities result = super.getCapabilities();
		result.disableAll();
		
		//enable only nominal attributes
		
		//attributes
		result.enableAll();
		result.enable(Capability.NOMINAL_ATTRIBUTES);
		
		// class
		result.enableAllClasses();
		result.enable(Capability.MISSING_CLASS_VALUES);
		result.enable(Capability.NO_CLASS);
		
		return result;
	}
	
	/**
	   * Sets the format of the input instances.
	   * 
	   * @param instanceInfo an Instances object containing the input instance
	   *          structure (any instances contained in the object are ignored -
	   *          only the structure is required).
	   * @return true if the outputFormat may be collected immediately.
	   * @throws UnsupportedAttributeTypeException if the selected attribute a
	   *           string attribute.
	   * @throws Exception if the input format can't be set successfully.
	   */
	  @Override
	  public boolean setInputFormat(Instances instanceInfo) throws Exception {

	    super.setInputFormat(instanceInfo);
	    m_AttIndices.setUpper(instanceInfo.numAttributes() - 1);
	    return false;
	  }
	  
	  
	/**
	   * Input an instance for filtering. The instance is processed and made
	   * available for output immediately.
	   * 
	   * @param instance the input instance.
	   * @return true if the filtered instance may now be collected with output().
	   * @throws IllegalStateException if no input structure has been defined.
	   */
	@Override
	public boolean input(Instance instance) throws Exception {
		if(getInputFormat() == null){
			throw new IllegalStateException("No input instance format defined");
		}
		if(m_NewBatch){
			resetQueue();
			m_NewBatch = false;
		}
		
		if(isOutputFormatDefined()){
			Instance newInstance = (Instance) instance.copy();
			
			//make sure that we process only the data specified in isInrange
			for(int i = 0; i <newInstance.numAttributes(); i++){
				if(newInstance.attribute(i).isNominal() && !newInstance.isMissing(i) 
						&& m_AttIndices.isInRange(i)){
					Attribute outAtt = getOutputFormat().attribute(newInstance.attribute(i).name());
					String inVal = newInstance.stringValue(i);
					int outIndex = outAtt.indexOfValue(inVal);
					if(outIndex < 0) {
						newInstance.setMissing(i);
					}else{
						/*TODO make sure to re-think this implementation later,
						 as at the moment, if attribute's value can't be converted
						 to numeric, implementation will set it to missing */
						try{
							double value = Double.parseDouble(inVal);
							newInstance.setValue(i, value);
						}catch(NumberFormatException e){
							System.err.println(e);
						}
					}
					
				}
			}
			push(newInstance);
			return true;
		}
		
		
		bufferInput(instance);
		return false;
	}
	
	/**
	 * Signifies that this batch of input to the filter is finished. If the filter
     * requires all instances prior to filtering, output() may now be called to
     * retrieve the filtered instances. 
     * 
     * @return true if there are instances pending output.
     * @throws IllegalStateException if no input structure has been defined.
	 */
	@Override
	public boolean batchFinished(){
		
		if(getInputFormat() == null){
			throw new IllegalStateException("No input instance format defined");
		}
		if(!isOutputFormatDefined()){
			
			setOutputFormat();
			
			// Convert pending input instances
			for (int i = 0; i < getInputFormat().numInstances(); i++){
				Instance temp = (Instance) getInputFormat().instance(i).copy();
				for(int j = 0; j < temp.numAttributes(); j++){
					if(temp.attribute(j).isString() && !temp.isMissing(j) 
							&& m_AttIndices.isInRange(j)){
						
						// adjust indexes for the removal of the nominal value
						temp.setValue(j, temp.value(j)-1);
						
					}
				}
				push(temp);
			}
		}
		
		//TODO process all the instances here
		for(int i = 0; i < getInputFormat().numInstances(); i++){
			//we know what attributes to be accessed in here
			Set<Integer> keys = this.attributes.keySet();
			for (Integer key : keys) {
				//change instances in here
				Instances newInstances = new Instances(getInputFormat());
				Instance newInstance = newInstances.instance(i);
				newInstance.setValue(key, this.attributes.get(key).value(i));
				//push instance to newInstances 
				
			}
			
		}
		
		flushInput();
		m_NewBatch = true;
		return (numPendingOutput() != 0);
	}
	
	
	/**
	 * Returns an enumeration describing the available options.
	 * 
	 * @return an enumeration of all the available options.
	 */
	@Override
	public Enumeration<Option> listOptions() {
		
		Vector<Option> newVector = new Vector<Option>(1);
		
		newVector.addElement(new Option(
				"\tSets the range of attribute indices (default last).", "R", 1,
				"-R <col>"
				));
		
		newVector.addElement(new Option("\tInvert the range specified by -R.", "V", 1, 
				"-V <col>"));
		
		return newVector.elements();
	}

	/**
	   * Parses a given list of options.
	   * <p/>
	   * 
	   * <!-- options-start --> Valid options are:
	   * <p/>
	   * 
	   * <pre>
	   * -R &lt;col&gt;
	   *  Sets the range of attribute indices (default last).
	   * </pre>
	   * 
	   * <pre>
	   * -V &lt;col&gt;
	   *  Inverts the selection specified by -R.
	   * </pre>
	   * 
	   * <!-- options-end -->
	   * 
	   * @param options the list of options as an array of strings
	   * @throws Exception if an option is not supported
	   */
	@Override
	public void setOptions(String[] options) throws Exception {
		
		 String attIndices = Utils.getOption('R', options);
		    if (attIndices.length() != 0) {
		      setAttributeRange(attIndices);
		    } else {
		      setAttributeRange("last");
		    }

		    String invertSelection = Utils.getOption('V', options);
		    if (invertSelection.length() != 0) {
		      m_AttIndices.setInvert(true);
		    } else {
		      m_AttIndices.setInvert(false);
		    }

		    if (getInputFormat() != null) {
		      setInputFormat(getInputFormat());
		    }

	}

	 /**
	   * Gets the current settings of the filter.
	   * 
	   * @return an array of strings suitable for passing to setOptions
	   */
	  @Override
	  public String[] getOptions() {

	    String[] options = new String[this.m_AttIndices.getInvert() ? 7 : 6];
	    int current = 0;

	    options[current++] = "-R";
	    options[current++] = "" + (getAttributeRange());

	    while (current < options.length) {
	      options[current++] = "";
	    }

	    if (this.m_AttIndices.getInvert()) {
	      options[current++] = "-V";
	    }

	    return options;
	  }

	  /**
	   * @return tip text for this property suitable for displaying in the
	   *         explorer/experimenter gui
	   */
	  public String attributeRangeTipText() {

	    return "Sets which attributes to process. This attributes "
	        + "must be string attributes (\"first\" and \"last\" are valid values "
	        + "as well as ranges and lists)";
	  }
	  
	  /**
	   * Get the range of indices of the attributes used.
	   * 
	   * @return the index of the attribute
	   */
	  public String getAttributeRange() {

	    return m_AttIndices.getRanges();
	  }
	  
	  /**
	   * Sets range of indices of the attributes used.
	   * 
	   * @param attIndex the index of the attribute
	   */
	  public void setAttributeRange(String rangeList) {

	    m_AttIndices.setRanges(rangeList);
	  }
	  
	  /**
	   * Set the output format. Takes the current average class values and
	   * m_InputFormat and calls setOutputFormat(Instances) appropriately.
	   */
	  private void setOutputFormat() {

	    Instances newData;
	    FastVector newAtts, newVals;
	    
	    // Compute new attributes

	    newAtts = new FastVector(getInputFormat().numAttributes());
	    for (int j = 0; j < getInputFormat().numAttributes(); j++) {
	      Attribute att = getInputFormat().attribute(j);
	      if (!m_AttIndices.isInRange(j) || !att.isNominal()) {

	        // We don't have to copy the attribute because the
	        // attribute index remains unchanged.
	        newAtts.addElement(att);
	      } else {
	    	  //add attributes to be changed
	    	this.attributes.put(j, att);
	    	Attribute attr = new Attribute (att.name());
	    	attr.setWeight(att.weight());
        	newAtts.addElement(attr);
	      }
	    }

	    // Construct new header
	    newData = new Instances(getInputFormat().relationName(), newAtts, 0);
	    newData.setClassIndex(getInputFormat().classIndex());
	    
	    setOutputFormat(newData);
	  }
	  
	  /**
	   * Returns the revision string.
	   * 
	   * @return the revision
	   */
	  @Override
	  public String getRevision() {
	    return RevisionUtils.extract("$Revision: 1.0 $");
	  }
	  
	  /**
	   * Main method for testing this class.
	   * 
	   * @param argv should contain arguments to the filter: use -h for help
	   */
	  public static void main(String[] argv) {
	    runFilter(new NominalToNumeric(), argv);
	  }
}
