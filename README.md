# weka_extensions
Extending Weka's features for essential data mining tasks

#PRE-WORDS: 

This repo is aiming to fill the gaps in pre-processing in Weka mining tool. By making various mining techniques, discovered that Weka is not flexible (or at least not easy to find required API) to make required basic techniques, like removing part of the text, counting appearance of value(/s) in text, image comparison into attribute (as nominal) and more. 

#IMPLEMENTATION:

The software was downloaded in official Waikato University Website <http://www.cs.waikato.ac.nz/ml/weka/downloading.html>. Then weka-src.jar was unpacked and installed for using with Eclipse. For complete installation details, please refer to: <http://weka.wikispaces.com/Eclipse>. 

HOW TO USE REPO:

All the implementation, once pulled from the repo will be available within runtime of Eclipse (or any other IDE) on GUIChooser.java. 

##Filters:

### RemoveSubString 
  - *attribute.unsupervised*
Given filter is responsible for deleting part of the string from specified attribute(s). Pretty simple, functionality, however was not easy to figure out in Weka original package. 
Usage features: 
  * can count num of characters (string) from backward, therefore removing only required portion (specify -999 as start position)

### CompareImagesFilter
 - *attribute.unsupervised*
This filter is useful for comparing images and finding exactly same ones. Problem was that, for some mining was collected directory of images corresponding to instances.Some of the images are identical and therefore could give potential information between instances. This filter produces additional column with nominal numeric values and identical images would equal to same number and therefore could be identified. If there is no given image (name from attribute), '-1' value is given. 

### NominalToNumeric
 - *attrbute.unsupervised*
 Self explanatory, used for converting nominal values into numeric in Weka environment (**not implemented at the moment**)

