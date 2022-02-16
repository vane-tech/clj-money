FROM clojure:openjdk-11-tools-deps-slim-bullseye

RUN apt-get update && apt-get install -y --no-install-recommends nodejs npm
RUN npm install --global yarn

ENV SRC /money/
RUN mkdir $SRC
WORKDIR $SRC
ADD . $SRC
RUN yarn global add shadow-cljs
