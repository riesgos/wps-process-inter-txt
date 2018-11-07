# wps-process-inter-txt

Wrapper process for the [CIGIDEN](http://cigiden.cl/en/) inter-txt python script to integrate it into the [52North WPS Server](https://github.com/52North/WPS).

The inter-txt python script takes as input a csv file of infrastructure restoration time series after an earthquake (e.g. column 1 could be titled "water_supply" and the entries below would be percentages describing the level of service of the water supply infrastructure, and further columns would contain additional infrastructures). The script was used in the paper "Data-driven estimation of interdependencies and restoration of infrastructure systems" (DOI: https://doi.org/10.1016/j.ress.2018.10.005).

The InterTxt java class in org.52n.wps.project.riesgos is a wrapper of the inter-txt.py script placed in /usr/share/riesgos. The wrapper serves as a webapp for the 52North WPS Server implementation, making it a process that runs in the server. The input for the process is a csv file ("input-data"), of type GenericFileDataBinding within the WPS implementation. The output is a LiteralStringBinding of the standard output of the python script, captured by the JavaProcessStreamReader class in org.52n.wps.project.riesgos.util.

The process is deployed here:

https://riesgos.52north.org/wps/WebProcessingService?service=WPS&request=DescribeProcess&version=2.0.0&identifier=org.n52.wps.project.riesgos.InterTxt

A test client can be found here:

https://riesgos.52north.org/wps-js/
