FROM nginx

WORKDIR /usr/share/react

RUN curl -fsSL https://deb.nodesource.com/setup_17.x | bash -
RUN apt-get install -y nodejs
RUN npm install -g yarn

COPY package.json yarn.lock ./

RUN yarn install

COPY . .

RUN npm run build

RUN rm -r /usr/share/nginx/html/*

RUN cp -a build/. /usr/share/nginx/html








