# => Build container
FROM node:alpine as builder
WORKDIR /app
COPY ui/package.json .
COPY ui/yarn.lock .
RUN yarn
COPY ui/ .
RUN yarn build


FROM nginx:1.21.6-alpine

# Nginx config
RUN rm -rf /etc/nginx/conf.d
COPY ui/conf /etc/nginx

# Static build
COPY --from=builder /app/build /usr/share/nginx/html/

# Default port exposure
EXPOSE 80
EXPOSE 443

# Copy .env file and shell script to container
WORKDIR /usr/share/nginx/html
COPY ui/env.sh .
COPY ui/.env .

# Add bash
RUN apk add --no-cache bash

# Make our shell script executable
RUN chmod +x env.sh

# Start Nginx server
CMD ["/bin/bash", "-c", "/usr/share/nginx/html/env.sh && nginx -g \"daemon off;\""]
