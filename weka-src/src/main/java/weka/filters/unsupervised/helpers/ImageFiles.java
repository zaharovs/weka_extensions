package weka.filters.unsupervised.helpers;

import java.awt.Toolkit;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for reading images from directory, comparing them
 * to identify for same images used.
 * 
 * @author germans zaharovs <germans@germans.me.uk>
 * @version 1.0
 *
 */
public class ImageFiles {
	/** Location of the image directory*/
	private String imageSource;
	
	/** All images in this directory */
	public Set<Image> images = new HashSet<>();
	
	/** Collection of all hashcodes of images */
	private Set<Integer> hashCodes = new HashSet<>();
	
	/** Keep unique identifier for each image */
	int lastNumber=0;
	
	/**
	 * Constructor 
	 * 
	 * @param imageSource Location of the image directory to be read
	 * @throws FileNotFoundException Occurs if no given directory is found
	 * @throws InterruptedException Occurs if thread is occupied, and can't be accessed
	 */
	public ImageFiles(String imageSource) throws FileNotFoundException, InterruptedException{
		this.imageSource = imageSource;
		File folder = new File(imageSource);
		File[] listOfImages = folder.listFiles();
		for(int i = 0; i < listOfImages.length; i++){
			Image temp = new Image(listOfImages[i].getName(), lastNumber++);
			if(!hashCodes.contains(temp.getHashCode())){
				hashCodes.add(temp.getHashCode());
			}else{
				//we know that image exist in here
				Image[] contImages = images.toArray(new Image[images.size()]);
				for(int i1 = 0; i1 < contImages.length; i1++){
					if(contImages[i1].getHashCode() == temp.getHashCode()){
						temp.correspondingNumber = contImages[i1].correspondingNumber;
					}
				}
			}
			images.add(temp);
		}
	}
	
	/**Nested class for image holding, number is used for determining whether same*/
	class Image {
		/** This number to be unique for each unique image */
		int correspondingNumber;
		/** This name is unique for each image */
		String correspondingName;
		/**Hashcode representation of image*/
		int hashCode;

		/**
		 * Constructor
		 * @param number Number representing image
		 * @param name Name representing image
		 * @throws InterruptedException if image can't be read
		 * */
		public Image(String name, int number) throws InterruptedException{
			this.correspondingName = name;
			this.correspondingNumber = number;
			readHashCode();
			
		}
		
		/** Get a unique hashcode for image in here*/
		private void readHashCode() throws InterruptedException{
			java.awt.Image image = Toolkit.getDefaultToolkit().getImage(imageSource + File.separator + this.correspondingName);
			PixelGrabber grabber = new PixelGrabber(image, 0, 0, -1, -1, true);
			if(grabber.grabPixels()){
				int width = grabber.getWidth();
				int height = grabber.getHeight();
				int[] hashArray = new int[width*height];
				hashArray = (int[])grabber.getPixels();
				this.hashCode = Arrays.hashCode(hashArray);
			}else{
				this.hashCode = -1;
			}
		}
		/** Getter for hashcode of the image*/
		public int getHashCode(){
			return this.hashCode;
		}
		
		@Override
		public boolean equals(Object obj) {
			Image tempImage = (Image)obj;
			return ((this.correspondingName.equals(tempImage.correspondingName))
					&& (this.correspondingNumber == tempImage.correspondingNumber));
		}
	}
	
	public int getValueImage(String imageName){
		Image[] imNumber = this.images.toArray(new Image[this.images.size()]);
		for(int i = 0; i < imNumber.length; i++){
			if(imNumber[i].correspondingName.equals(imageName)){
				return imNumber[i].correspondingNumber;
			}
		}
		//not found
		return -1;	
	}
	
	/**
	 * Return corresponding numbers of all images in directory
	 * @return corresponding numbers
	 */
	public Set<String> getNamesOfImages(){
		Set<String> nameOfImages = new HashSet<>();
		for(Image temp: this.images){
			nameOfImages.add(""+temp.correspondingNumber);
		}
		return nameOfImages;
	}
	
	public static void main(String main[]) throws InterruptedException, IOException{
	     ImageFiles images = new ImageFiles(main[1]);
	     java.util.Iterator<Image> imagefiles = images.images.iterator();
	     FileOutputStream file = new FileOutputStream(new File("C:\\Users\\germans\\Desktop\\imageComparison.txt"));
	     while(imagefiles.hasNext()){
	    	 Image temp = imagefiles.next();
	    	 String sTemp = "Image name:\t "+temp.correspondingName+"\t Number:\t"+temp.correspondingNumber+"\n";
	    	 
	    	 file.write(sTemp.getBytes());
	    	 //System.out.printf("Product name:\t %s\t\t\tProduct Number:\t\t %d\n",temp.correspondingName,temp.correspondingNumber);
	     }
	     file.flush();
	     file.close();
	}
}
