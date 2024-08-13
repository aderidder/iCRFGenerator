# Adding new codebooks to the iCRF Generator
After starting the iCRF Generator for the first time, your cache directory will contains a iCRFSettings.xlsx file. Head to the "Codebooks" tab.

## Adding an OpenEHR codebook
- Go to the [OpenEHR](https://ckm.openehr.org/ckm/) website
- Search for a codebook, e.g. blood:
  <img src="images/add_codebooks/openehr_search.png" height="300">
- Double-click on the codebook - this opens it.
- The URL now shows something like https://ckm.openehr.org/ckm/archetypes/1013.1.3574
- The 1013.1.3574 is the prefix you need to add to the codebooks tab
- For this example, the entries would become:

| type    | name           | prefix      | server                       | group_is_item | tags  | skip_language |
|---------|----------------|-------------|------------------------------|---------------|-------|---------------|
| OPENEHR | Blood Pressure | 1013.1.3574 | https://ckm.openehr.org/ckm/ | FALSE         | blood |               |

Please be aware that we cannot guarantee that iCRF Generator is compatible with every codebook available in OpenEHR!

## Adding an ART-DECOR codebook
- Go to ART-DECOR's [project index](https://decor.nictiz.nl/services/ProjectIndex)
- Select the project, e.g. CBS doodsoorzakenstatistiek
- The URL now shows something like https://decor.nictiz.nl/services/ProjectIndex?prefix=cbs-dstat-&format=html&language=&ui=nl-NL
- Take the prefix from this URL, cbs-dstat-
- Add this to the codebooks tab
- For this example, the entries would become:

| type     | name                        | prefix     | server                            | group_is_item | tags  | skip_language |
|----------|-----------------------------|------------|-----------------------------------|---------------|-------|---------------|
| ARTDECOR | CBS doodsoorzakenstatistiek | cbs-dstat- | https://decor.nictiz.nl/services/ | FALSE         | death |

Please be aware that we cannot guarantee that iCRF Generator is compatible with every codebook available in ART-DECOR!

## Concerning tags
The tags are used to filter codebooks in the Search Codebooks part of the iCRF Generator.
