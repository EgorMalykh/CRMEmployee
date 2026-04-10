FROM node

WORKDIR /app

COPY package.json ./

COPY vite.config.js ./

COPY package-lock.json ./

COPY eslint.config.js ./

COPY tailwind.config.js ./

COPY postcss.config.js ./

RUN npm i

COPY . .

EXPOSE 5173

CMD ["npm", "run", "dev"]