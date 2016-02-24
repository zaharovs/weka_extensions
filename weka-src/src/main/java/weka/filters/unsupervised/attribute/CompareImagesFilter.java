package weka.filters.unsupervised.attribute;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.SparseInstance;
import weka.core.UnsupportedAttributeTypeException;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.StreamableFilter;
import weka.filters.UnsupervisedFilter;
import weka.filters.unsupervised.helpers.ImageFiles;

/**
 * <!-- globalinfo-start -->
 * Add additional attribute, identifying unique numeric identity of image. 
 * Given identity is exactly same for identical images in attribute specifying image name to belonging to instances.
 * At the moment only JPG, PNG & GIF images are accepted.
 * <!-- globalinfo-end -->
 * 
 * <!-- options-start -->
 * 
 * <pre> -D &lt;col&gt
 * 	Set directory to be read from. </pre>
 * <pre> -C &lt;col&gt
 *  Set attribute (column) num, by which images to be identified. </pre>
 * <pre> -A &lt;col&gt
 * 	Set new attribute's name. Optional - default is <imageComparison>. </pre>
 * <!-- options-end -->
 * 
 * @author Germans Zaharovs (germans@germans.me.uk)
 * @version $Revision: 1.0 $
 *
 */
public class CompareImagesFilter 
			extends Filter 
			implements UnsupervisedFilter, StreamableFilter, OptionHandler {

	/** for serialization */
	static final long serialVersionUID = 6969696969696971L;
	
	/** The attribute's index setting */
	private Range m_attributePosition = new Range("last");
	
	/** Directory, from which images to be read from */
	private String m_attributeReadDirectory = "";
	
	/** Images in directory */
	private ImageFiles images;
	
	/** Name of the new attribute. */
	private String m_attributeName  = "imageComparison";
	
	
	/**
	 * Returns a string describing this filter
	 * 
	 * @return		a description of the filter suitable for 
	 * 			displaying in the explorer & experimenter gui
	 */
	public String globalInfo(){
		return "This filter to be used for identifying identical images."
				+ " Images are identified by existing attribute values against images existing in provided"
				+ " directory. If image isn't found in directory '-1' is set as a value";
		}
	
	/**
	  * Returns an enumeration describing the available options.
	  * 
	  * @return			an enumeration of all available options
	  */
	  @Override
	public Enumeration listOptions() {
		  
		  Vector newVector = new Vector(3);
		  
		  newVector.addElement(new Option(
				  "\tSpecify directory in which images to be read from. Images must be of type: JPG, PNG or GIF.",
				  "D", 1, "-E <directory>"
		  ));
		  newVector.addElement(new Option(
				  "\tSpecify new comparison attribute's name.",
				  "A", 1, "-A <name>"
				  ));
		  newVector.addElement(new Option(
				  "\tSpecify attribute position, containing names of images to be examined.",
				  "P", 1, "-P <position>"
				  ));
		  return newVector.elements();
	}
	  
	/**
	 * Parses a given list of options. </p>
	 * 
	 * <!-- options-start -->
	 * Valid options are: <p/>
	 * 
	 * <pre> -D &lt;directory&gt;
	 *  Specify the directory, in which images are located. </pre>
	 *  
	 * <pre> -A &lt;Attribute name&gt;
	 *  Specify new attribute's name </pre>
	 * 
	 * <pre> -P &lt;Attribute to read from&gt;
	 *  Specify attribute position, from which images to be set for dataset; </pre>
	 *  
	 * <!-- options-end -->
	 * 
	 * @param options the list of options as an array of strings
	 * @throws Exception if an option is not supported, or required option isn't provided
	 */
	@Override
    public void setOptions(String[] options) throws Exception {
			String tmpStr;
			
			tmpStr = Utils.getOption("D", options);
			if(tmpStr.length() != 0){
				setReadDirectory(tmpStr);
			} else{
				throw new Exception("Directory of images must to be specified");
			}
			
			tmpStr = Utils.getOption("A", options);
			if(tmpStr.length() != 0){
				this.setName(tmpStr);
			}else{
				//set pre-defined name here
				this.setName("EvaluatedImages");
			}
			
			tmpStr = Utils.getOption("P", options);
			if(tmpStr.length() != 0){
				setPosition(tmpStr);
			}else{
				throw new Exception("Attribute position must be set");
			}

    }
	
   /**
   * Gets the current settings of the filter.
   *
   * @return 		an array of strings suitable for passing to setOptions
   */
	@Override
	public String[] getOptions() {
		    String[] options = new String[6];
		    int current = 0;
		    
		    options[current++] = "-D"; options[current++] = this.getReadDirectory();
		    options[current++] = "-P"; options[current++] = this.getPosition();
		    options[current++] = "-A"; options[current++] = this.getName();
		    
		    while(current < options.length){
		    	options[current++] = "";
		    }
		    return options;
	}
		
	/**
	 * Return the tip text for this property
	 * 
	 * @return tip text for this property suitable for 
	 * displaying in the explorer/experimenter gui
	 */
    public String indexTipPosition() {
    	return "Set attribute position, corresponding to image names";
    }
    
    /**
     * Set the position of image names in dataset. This property must correspond
     * to names of the images in directory specified.
     * 
     * @param position Image name attribute
     */
    public void setPosition(String position){
    	this.m_attributePosition.setRanges(position);
    }
	
    /**
     * Returns the position of image corresponding attribute
     * @return position of image corresponding attribute
     */
	public String getPosition(){
		return this.m_attributePosition.getRanges();
	}
    
	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String readDirectoryTiptext(){
		return "Set directory, containing images for comparison";
	}
    
	/**
	 * Set directory of the images to be compared for equality
	 * @param directory of the images to be compared
	 */
	public void setReadDirectory(String directory){
		this.m_attributeReadDirectory = directory;
	}
    
	/**
	 * Returns the directory, in which images have been compared
	 * @return directory, in which images have been compared
	 */
	public String getReadDirectory(){
		return this.m_attributeReadDirectory;
	}
	
	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for 
	 * displaying in the explorer/experimenter gui
	 */
	public String nameTipText(){
		return "Name of new attribute, created for comparison of images";
	}
	
	/**
	 * Set the name for new attribute to be created for comparison
	 * @param name of new attribute
	 */
	public void setName(String name){
		this.m_attributeName = name;
	}
	
	/**
	 * Get the name of the new attribute to be created
	 * @return name of the attribute to be created
	 */
	public String getName(){
		return this.m_attributeName;
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
		this.m_attributePosition.setUpper(instanceInfo.numAttributes()-1);
		//load images from director in here
	    this.images = new ImageFiles(this.m_attributeReadDirectory);
	    //create all nominal values for new attribute
	    FastVector nominalAttribute = new FastVector();
	    for(String value: images.getNamesOfImages()){
	    	nominalAttribute.addElement(value);
	    }
	    //add value, if image is not appearing to be in folder
	    nominalAttribute.addElement(""+-1);
	    //set new attribute to dataset
		Attribute newAttribute = new Attribute(this.getName(), nominalAttribute);
		Instances outputFormat = new Instances(instanceInfo, 0);
		outputFormat.insertAttributeAt(newAttribute, instanceInfo.numAttributes());
		setOutputFormat(outputFormat);
		return true;
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
			//first copy string values from input to output
			copyValues(newInstance, true, newInstance.dataset(), getOutputFormat());
			
			//insert new attribute and reassign to output
			newInstance.setDataset(null);
			newInstance.insertAttributeAt(getOutputFormat().numAttributes()-1);
			newInstance.setDataset(getOutputFormat());
			//set new value in here
			newInstance.setValue(newInstance.attribute(newInstance.numAttributes()-1),
					""+this.images.getValueImage(newInstance.stringValue(this.m_attributePosition.getSelection()[0])));
			push(newInstance);
			return true;
		}
		
		double[] vals = new double[instance.numAttributes()+1];
		for(int i = 0; i < instance.numAttributes(); i++){
			if(instance.isMissing(i)){
				vals[i] = Instance.missingValue();
			}else{
				vals[i] = instance.value(i);
			}
		}
		
		Instance inst = null;
		if(instance instanceof SparseInstance){
			inst = new SparseInstance(instance.weight(), vals);
		}else{
			inst = new Instance(instance.weight(), vals);
		}
		
		inst.setDataset(getInputFormat());
		copyValues(inst, false, instance.dataset(), getOutputFormat());
		inst.setDataset(getOutputFormat());
		push(instance);
		return false;
	}
	
	/**
	 * Revision of the code
	 */
	public String getRevision() {
		return RevisionUtils.extract("$Revision: 1.0 $");
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
	   * Set the output format. Takes the current average class values
	   * and m_InputFormat and calls setOutputFormat(Instances) 
	   * appropriately.
	   */
	private void setOutputFormat() {
	    Instances 	newData = getInputFormat();
	   
	    Attribute att = getInputFormat().attribute(getInputFormat().numAttributes()-1);
	    Attribute attValues = getInputFormat().attribute(this.m_attributePosition.getSelection()[0]);
	    for(int i = 0; i < att.numValues(); i++){
	    	String imageName = attValues.value(i);
	    	int value = this.images.getValueImage(imageName);
	    	//put this value back to attribute
	    	  newData.renameAttributeValue(att, att.value(i), ""+value);
	    }
	    
	    // save new data into dataset
	    setOutputFormat(newData);
	  }
	  
	  /**
	   * Main method for testing this class.
	   *
	   * @param args 	should contain arguments to the filter: use -h for help
	   */
	public static void main(String [] args) {
	    runFilter(new CompareImagesFilter(), args);
	  }
}
