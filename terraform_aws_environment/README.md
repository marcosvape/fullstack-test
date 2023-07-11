This is a tool that uses terraform CDK to create the terraform files that generates infrastructure as code. Please refer
to the templates package to see the implemented templates. Use terraform cloud as a remote backend provider when
applying the terraform files to keep track of the actual state of the cloud environment

Requires terraform cli installed on machine.

Instalation of terraform cdk:


_npm install -g cdktf-cli cdktf get_


After installing all the stuff required, update your pom file to point to the desired template.

Then just run the following commands.

_mvn -e -q compile exec:java_

OR

_cdktf synth_

after generating the terraform files, remember to add the remote backend configuration and then just run the following:

_terraform login terraform init terraform plan terraform apply_