# -------- Stage 1: Build (SSR) --------
FROM node:20 AS build
WORKDIR /app

COPY /paperlessWebUI/webUI/package*.json ./
RUN npm ci

COPY /paperlessWebUI/webUI ./
ARG configuration=production
RUN npm run build -- --configuration=${configuration}

# -------- Stage 2: Runtime (Node SSR) --------
FROM node:20-alpine
WORKDIR /app

# Kopiere nur das gebaute Artefakt
COPY --from=build /app/dist/web-ui /app/dist/web-ui

ENV NODE_ENV=production
ENV PORT=80
EXPOSE 80

# Startet den Angular-SSR-Server
CMD ["node", "dist/web-ui/server/server.mjs"]
