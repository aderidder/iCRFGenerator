# The iCRF Generator

**The NWO-Funded Extended iCRF Generator (version 2.0) has just been released! See the News section for all the changes!**<br>

## Breaking news!!
ART-DECOR moved stuff, breaking the iCRF Generator. The fix is easy:
* Start the iCRF Generator. Exit again
* Go to the cache directory - probably iCRFGenerator\cache 
* Edit iCRFSettings.xlsx
  * In the column Server, replace https://decor.nictiz.nl/services/ with https://decor.nictiz.nl/decor/services/
* Save the file
* Done!
This will be fixed in the next release.

Semantic interoperability of clinical data requires the use of a common vocabulary, such as SNOMED-CT. Unfortunately, mapping data to such a terminology is time-consuming and requires expert knowledge of both the dataset and the terminology. A viable alternative can be the reuse of codebooks - published dataset definitions which, in some cases, have already been mapped to a terminology. We designed the iCRF Generator, a tool which makes it easy to generate interoperable Case Report Forms (iCRFs) for several major EDCs, including Castor and REDCap. The tool currently provides access to multiple codebooks stored online in ART-DECOR and OpenEHR, such as: 
* The Basic Health Data Set (Basisgegevensset Zorg), which is the national standard hospitals will use to exchange electronic healthcare record data
* RIVM’s population screening colorectal cancer and cervical cancer
* The pathology Colon biopsy and Colonrectum carcinoma protocols from PALGA, the Dutch national pathology registry. 

By using the definitions from such codebooks, a user ensures interoperability with all datasets using these definitions, thus increasing the FAIRness of the data. 

***Learn more about the iCRF Generator in the [publication](https://f1000research.com/articles/9-81). If you use the iCRF Generator for your project, please cite the paper!***

## News
New in 2.0 - This is major release and includes many new features:
* Support for OpenEHR - Specifically, we've added codebooks from ckm.openehr.org
* Support for CDISC's ODM-XML
* Improvements to the User Interface:
  * Change in the iCRF Generator's flow. In the original version, when you selected languages, you were given the codebooks in these languages. In the new version, you select a main language and alternative languages. You are presented the codebook in the main language and the CRFs are also generated in the alternative languages using these same items.
  * Improved selection of codebooks. You can now search for codebooks based on multiple search parameters
* Easier to customise some aspects (see the Customising the iCRF Generator section):
  * Originally, the iCRF Generator had a codebooks.txt where you could add codebooks. This has been moved to an Excel file, iCRFSettings.xlsx
  * Additionally, this file contains languages and validation messages for some languages. You may want to add validation messages for your particular language here, since it currently contains only Dutch and English.
    * Note: these messages are currently used by ODM-XML and Castor. We aim to add them to other EDCs as well when appropriate.
* Updated the OpenClinica 3 functionality, which was outdated compared to the other EDCs.
* Added LibreClinica to the list of EDCs - this uses the OpenClinica 3 functionality.

New in 1.2.6:
* ART-DECOR made some changes which breaks the download of codebooks; instead of http, https has to be used. The file containing the default codebooks is updated with this change. If you don't feel like grabbing the new distribution, you can also just update the codebooks.txt file in the cache directory - change any http to https and you should be fine. 

New in 1.2.4 / 1.2.5:
* Log4j updated to newest version for security reasons

New in 1.2.3:
* Updated to new Apache POI library to fix an issue an OpenClinica CRF and EMX model refused to show the save dialog
* New RIB logo
* Cosmetic change to the hyperlink layout on page 3 of the wizard

New in 1.2:
* **Fix for the Mac Catalina gatekeeper issue** See the Running the program chapter below for updated Mac instructions. Let us know if it doesn't work!
* Support for EMX model output, allowing you to use the iCRF Generator for e.g. Molgenis Catalogue
* Updated the codelist selection panel, which had some performance issues
* Added new codebooks, such as the 2020 ZIBs
* Added a customisable timeout in the Settings. Default is 2 minutes, but there may still be a timeout, as ART-DECOR can be very slow (they're working on it...). After the first time it will of course be cached locally.  
* Added a small hyperlink "i" next to all the codebooks on page 1, which links to the codebook's ART-DECOR page


## Supported Outputs
Currently the program can create a file for:
* Castor EDC: creates an XML file which you can import in Castor, after which you can manually tweak it
* OpenClinica 3 / LibreClinica: creates an Excel template with the items selected in it. The user can add additional details to the Excel and upload it to OpenClinica 3 afterwards
* REDCap: creates a csv file, which can be further edited and uploaded to REDCap
* Molgenis EMX: generated files are compatible. Molgenis team are creating a 2.0 version of their model, which is not included here yet.
* ODM-XML: generated files validate. Depending on where you want to use the file, specific ODM-extensions may be necessary
  
Additional EDCs may be added at some point if there is sufficient demand and I know the import definitions (Alea, OC4, Research Manager, etc).

## Available Codebooks
At this moment the following codebooks are immediately available:
1. ART-DECOR: The Clinical Building Blocks (Zorginformatiebouwstenen), which are information models of minimal clinical concepts
2. ART-DECOR: The Basic Health Data Set (Basisgegevensset Zorg). This codebook is used for the standardised exchange of patient data between e.g. healthcare providers  
3. ART-DECOR: The National Institute for Public Health and the Environment’s national screening codebook of bowel cancer and cervical cancer (RIVM bevolkingsonderzoeken) 
4. ART-DECOR: The PALGA Colon biopsy protocol
5. ART-DECOR: The PALGA Colorectum carcinoma protocol
6. ART-DECOR: The VASCA codebook. A codebook for rare diseases registries based on the JRC common data elements set and the EJPRD semantic model applied to the Registry of Vascular Anomalies (VASCA) use case.
7. ART-DECOR: FAIRGenomes codebook
8. ART-DECOR: The Clinical Building Blocks 2020 release
9. OpenEHR: Blood Pressure
10. OpenEHR: Glasgow Coma Scale (GCS)
11. OpenEHR: Hip arthroplasty component

This may change in the future. Please note that if you want to use a different codebook from ART-DECOR or OpenEHR, you can easily add it to the iCRFSettings.xlsx file (Codebooks tab). See [adding new codebooks](docs/add_codebooks.md)! for more information.

### Customising the iCRF Generator
The iCRF Generator now comes with a new settings file, called iCRFSettings.xlsx, which is available in the cache directory after the first run. This file contains the following sheets:
1. Info - explains what the file does and what the other sheets are for
2. Codebooks - contains the default iCRF Generator codebooks. You can add additional codebooks from OpenEHR or ART-DECOR here. Once you've done so, (re)start the iCRF Generator and the codebooks should be visible in the Codebook Selection page.
3. ValidationMessages - contains messages for a specific language. These are used when validations are created in that language. When no message in a specific language is available this defaults to English. This functionality is currently used by ODM-XML and Castor.
4. Languages - maps an extended language to a simple language. E.g. en-US and en-UK are both mapped to en. This is necessary to be able to determine overlapping languages between codebooks. You can alter this if you feel languages should not be considered the same.

## Running the program
If you downloaded the distribution ZIP file, it comes provided with its own JRE. The next sections show how to start the application for Windows and Mac.  
*Please be aware that the first time a codebook is selected, it has to be downloaded, which can take a while! The program stores
the downloaded codebooks locally in the cache directory, so subsequent loading of that codebook will be very fast.*

### Windows
Unpack the file and double-click on runme.bat

### Mac
First time: 
Unpack the file and ***right-click*** the runme.command file and select ***open***

Subsequent runs:
Double-click the runme.command file.

## Some limitations
* The iCRF Generator aims at generating items, so additional details (such as sections, repeating groups, etc) will have to be added manually. We may add some of this functionality in the future if there's demand for it and funding can be found.
* The iCRF Generator currently allows access to multiple codebooks out of the box. You can easily add more (ART-DECOR/OPENEHR) codebooks to the iCRFSettings file, although we cannot guarantee they all work

## Roadmap
Find our plans for future improvements [here](docs/roadmap.md)! 

## About
The iCRF Generator was designed and created by **Sander de Ridder** (Amsterdam UMC) and **Jeroen Beliën** (Amsterdam UMC).\
Testers & Consultants: 
* Gerben Rienk Visser (Trial Data Solutions)
* REDCap testing - Wessel Sloof (UMCG)

This project was sponsored by:
* NWO Open Science Fund 2021
* KWF project TraIT2Health-RI - Registry-in-a-Box
---
iCRFGenerator is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

iCRFGenerator is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

---


<div style="text-align:center">
<img src="docs/images/NWO.jpg" height="100"> <img src="docs/images/rib_new.png" height="100">&nbsp;<img src="docs/images/healthri_white.png" height="100">&nbsp;<img src="docs/images/kwf_white.png" height="100"><br>
<img src="docs/images/aumc_white.png" height="60">
</div>