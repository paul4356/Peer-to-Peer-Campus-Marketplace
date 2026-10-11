FROM php:8.3-apache

RUN docker-php-ext-install pdo_mysql \
    && a2enmod rewrite

COPY web-learning/ /var/www/html/
COPY backend-php/ /var/www/backend-php/
COPY database/ /var/www/database/
COPY railway-start.sh /usr/local/bin/railway-start

RUN chmod +x /usr/local/bin/railway-start \
    && printf '%s\n' \
        '<Directory /var/www/html>' \
        '    AllowOverride All' \
        '    Require all granted' \
        '</Directory>' \
        > /etc/apache2/conf-available/marketplace.conf \
    && a2enconf marketplace

ENV PORT=8080
EXPOSE 8080

CMD ["sh", "/usr/local/bin/railway-start"]
