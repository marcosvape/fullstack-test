FROM node:latest

COPY ./ /project

WORKDIR project/codebase/rdicidr-0.1.0/dist

RUN npm install -g serve

EXPOSE 3000
ENTRYPOINT ["serve", "-s", "build"]