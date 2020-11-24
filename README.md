# The iCRF Generator
Semantic interoperability of clinical data requires the use of a common vocabulary, such as SNOMED-CT. Unfortunately, mapping data to such a terminology is time-consuming and requires expert knowledge of both the dataset and the terminology. A viable alternative can be the reuse of codebooks - published dataset definitions which, in some cases, have already been mapped to a terminology. We designed the eCRF Generator, a tool which makes it easy to generate interoperable electronic Case Report Forms for (currently) three major EDCs: OpenClinica 3, Castor and REDCap. The tool currently provides access to several codebooks stored online in Art-Decor, such as: 
* The Basic Health Data Set (Basisgegevensset Zorg), which is the national standard hospitals will use to exchange electronic healthcare record data
* RIVM’s population screening colorectal cancer and cervical cancer
* The pathology Colon biopsy and Colonrectum carcinoma protocols from PALGA, the Dutch national pathology registry. 

By using the definitions from such codebooks, a user ensures interoperability with all datasets using these definitions, thus increasing the FAIRness of the data. 

***Learn more about the iCRF Generator in the [publication](https://f1000research.com/articles/9-81). If you use the iCRF Generator for your project, please cite the paper!*** 

## News
The 1.2 release includes the following changes:
* **Fix for the Mac Catalina gatekeeper issue** See the Running the program chapter below for updated Mac instructions. Let us know if it doesn't work!
* Support for EMX model output, allowing you to use the iCRF Generator for e.g. Molgenis Catalogue
* Updated the codelist selection panel, which had some performance issues
* Added new codebooks, such as the 2020 ZIBs
* Increased the number of seconds before a timeout occurs. Issue here is that the current version of the FAIRGenomes codebook is just very big, taking ART-DECOR about 1.5 minutes to generate. After the first time it will of course be cached locally.  

## Supported EDCs
Currently the program can create a core file for:
* Castor EDC: creates an XML file which you can import in Castor, after which you can manually tweak it
* OpenClinica 3: creates an OpenClinica 3 Excel template with the items selected in it. The user can add additional details to the Excel and upload it to OpenClinica 3 afterwards
* REDCap: creates a csv file, which can be further edited and uploaded to REDCap
  
Additional EDCs may be added at some point if there is sufficient demand and I know the import definitions (Alea, OC4, Research Manager, etc).

## Available Codebooks
At this moment the following ART-DECOR codebooks are available:
1.	The Clinical Building Blocks (Zorginformatiebouwstenen), which are information models of minimal clinical concepts
2.	The Basic Health Data Set (Basisgegevensset Zorg). This codebook is used for the standardised exchange of patient data between e.g. healthcare providers  
3.	The National Institute for Public Health and the Environment’s national screening codebook of bowel cancer and cervical cancer (RIVM bevolkingsonderzoeken) 
4.	Cancer Core Europe 
5.	The PALGA Colon biopsy protocol
6.	The PALGA Colorectum carcinoma protocol
7.  The VASCA codebook. A codebook for rare diseases registries based on the JRC common data elements set and the EJPRD semantic model applied to the Registry of Vascular Anomalies (VASCA) use case.
8.  **NEW: FAIRGenomes codebook**
9.  **NEW: The Clinical Building Blocks 2020 release**

### Custom Codebooks
If you wish to use the iCRF Generator for ART-DECOR codebooks that are not currently available in the standard set of available codebooks, please do the following:
*  Go to the cache directory
    *  If this directory does not yet exist, start the iCRF Generator and close it again - starting it will create the directory 
*  Open the file "codebooks.txt" in your favorite editor
*  Add the codebook following the format described in the codebooks.txt file
*  Start the iCRF Generator
*  Page 2, the page that allows you to select codebooks, should now show your newly added codebook
    * If it does not, ask for help
    
Please be aware that I cannot guarantee that iCRF Generator is compatible with every codebook available in ART-DECOR!  

## Running the program
If you downloaded the distribution ZIP file, it comes provided with its own JRE. The next sections show how to start the application for Windows and Mac.  
*Please be aware that the first time a codebook is selected, it has to be downloaded from ART-DECOR, which can take a while! The program stores
the downloaded XML file locally in the cache directory, so subsequent loading of that codebook will be very fast.*

### Windows
Unpack the file and double-click on runme.bat

### Mac
First time: 
Unpack the file and ***right-click*** the runme.command file and select ***open***

Subsequent runs:
Double-click the runme.command file.


## Some limitations
* The iCRF Generator aims at generating items, so additional details (such as sections, repeating groups, etc) will have to be added manually.
* The iCRF Generator currently allows access to six codebooks. We may add support for more codebooks in the future. If you have a great codebook available, which is unique and not overlapping too much with existing codebooks, feel free to contact us and we'll see whether it can be added.

## Creating a distributable zip file with JRE
JavaFX went from being integrated in Oracle's Java 8 to being a separate package, which is compatible with Java 11 and 12. Whereas in Java 8 it was possible to create a fat-jar which would then run on any computer with a Java 8 JRE, this is no longer an option as there is no JRE for these distributions. So, the fat-jar no longer being an option, the way to go is to generate my own JRE for Java 12 and provide that as part of my distribution. The nice thing is that you only include the modules from Java that are necessary, meaning the size stays manageable and that the windows/mac distribution will run on any windows/mac machine since I'm providing the JRE myself, so no more issues on e.g. a mac that does not have a JRE installed. However, I'm still confused on how to properly handle JavaFX in this modular approach. JavaFX has jmods files and jar files. The jmods I need to be able to generate the JRE; the jar files in my IDE. I use Maven to fetch the jar files, but there does not yet seem to be a mature plugin for jlink. So I ended up with some Maven stuff in which, for a distribution, I:
* Create a Jar file for GenerateCRF
* Copy all the dependencies, except the JavaFX jar files to a directory
* Download the JavaFX jmods and unpack these
* Generate the JRE, using the jmods
* Create a zip file from the jar file, the dependencies and the JRE

I created a profile for both Mac and Windows. So basically, if you're on one of these platforms and you call Maven's package, it will create a nice distributable zip file, including a script file which the user can double-click to run the program.

TODO: find out whether it is possible to change this, especially the JavaFX bit.

## Roadmap
Find our plans for future improvements [here](docs/roadmap.md)! 

## About
The iCRF Generator was designed and created by **Sander de Ridder** (VUmc 2019) and Jeroen Beliën (VUmc).\
Testers & Consultants: 
* Gerben Rienk Visser (Trial Data Solutions)
* REDCap testing - Wessel Sloof (UMCG)
This project was sponsored by KWF project TraIT2Health-RI (WP: Registry-in-a-Box)
---
iCRFGenerator is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

iCRFGenerator is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

---


<div style="text-align:center">
<img src="docs/images/rib.png" height="100">&nbsp;<img src="docs/images/healthri_white.png" height="100">&nbsp;<img src="docs/images/kwf_white.png" height="100">&nbsp;<img src="docs/images/vumc_white.png" height="100"><br>
<img src="docs/images/aumc_white.png" height="60">
</div>