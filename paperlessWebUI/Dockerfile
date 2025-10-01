# --------------------
# Stage 1: Build Angular
# --------------------
# Stage 1: Build an Angular Docker Image
FROM node AS build

WORKDIR /app

COPY /paperlessWebUI/webUI/package*.json ./

RUN npm install

COPY /paperlessWebUI/webUI ./

ARG configuration=production

RUN npm run build -- --output-path=dist/out --configuration $configuration

# --------------------
# Stage 2:
# --------------------
# Stage 2, use the compiled app, ready for production with Nginx

FROM nginx

COPY --from=build /app/dist/out/browser/ /usr/share/nginx/html

COPY /paperlessWebUI/nginx-custom.conf /etc/nginx/conf.d/default.conf