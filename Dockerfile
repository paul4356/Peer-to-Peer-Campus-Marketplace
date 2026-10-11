FROM php:8.3-apache

RUN apt-get update \
    && apt-get install -y --no-install-recommends libpq-dev \
    && docker-php-ext-install pdo_mysql pdo_pgsql \
    && rm -rf /var/lib/apt/lists/* \
    && for module in mpm_event mpm_worker; do \
        if [ -e "/etc/apache2/mods-enabled/${module}.load" ]; then a2dismod "$module"; fi; \
    done \
    && a2enmod mpm_prefork rewrite

COPY web-learning/ /var/www/html/
COPY backend-php/ /var/www/backend-php/
COPY database/ /var/www/database/
COPY render-start.sh /usr/local/bin/render-start

RUN printf '%s\n' 'upload_max_filesize = 5M' 'post_max_size = 6M' \
        > /usr/local/etc/php/conf.d/marketplace-uploads.ini

RUN chmod +x /usr/local/bin/render-start \
    && printf '%s\n' \
        '<Directory /var/www/html>' \
        '    AllowOverride All' \
        '    Require all granted' \
        '</Directory>' \
        > /etc/apache2/conf-available/marketplace.conf \
    && mkdir -p /var/www/html/uploads/items \
    && chown -R www-data:www-data /var/www/html/uploads \
    && a2enconf marketplace \
    && apachectl -t

ENV PORT=8080
EXPOSE 8080

CMD ["sh", "/usr/local/bin/render-start"]
