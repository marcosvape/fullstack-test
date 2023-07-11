FROM node:15.0-slim

COPY ./ /project

WORKDIR project/codebase/rdicidr-0.1.0/dist

RUN npm install -g serve
RUN serve -s build