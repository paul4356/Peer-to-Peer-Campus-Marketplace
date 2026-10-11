FROM php:8.3-apache

RUN docker-php-ext-install pdo_mysql \
    && for module in mpm_event mpm_worker; do \
        if [ -e "/etc/apache2/mods-enabled/${module}.load" ]; then a2dismod "$module"; fi; \
    done \
    && a2enmod mpm_prefork rewrite

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
    && a2enconf marketplace \
    && apachectl -t

ENV PORT=8080
EXPOSE 8080

CMD ["sh", "/usr/local/bin/railway-start"]
