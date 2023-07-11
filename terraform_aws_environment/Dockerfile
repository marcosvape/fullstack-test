FROM marcosvap/terraform-cdk-base:latest

ARG TERRAFORM_CREDENTIALS
ENV TERRAFORM_CREDENTIALS $TERRAFORM_CREDENTIALS

COPY ./ /project

WORKDIR project

RUN mkdir .gen

RUN cdktf synth

RUN mv cdktf.out/stacks/stack/* .

RUN sed -i s/#TOKEN_PLACE_HOLDER#/${TERRAFORM_CREDENTIALS}/ credentials.tfrc.json

RUN cp credentials.tfrc.json $HOME/.terraform.d/credentials.tfrc.json
RUN terraform init
RUN terraform apply -auto-approve