#!/bin/bash

#Clean reset pour les packages
sudo dpkg --configure -a
sudo apt-get install -f

# reponses au questions de mysql-server-5.5 et de phpmyadmin en utilisant les outils fournis dans le paquage debconf-utils
# -----------------------------------

#Fourni initialement dans le script de base
echo "Include /etc/phpmyadmin/apache.conf" | sudo tee --append /etc/apache2/apache2.conf
sudo service apache2 restart

# installation de mysql-server, mysql-client, apache2,  php5, libapache2-mod-php5, php5-mysql, phpmyadmin. Dans le meme ordre

#Necessaire pour mysql-server-5.5 et phpmyadmin
sudo apt-get install -y debconf-utils

#Installation de mysql-server-5.5
echo "========================================="
echo "Installation de mysql-server-5.5..."
echo "========================================="

#https://serversforhackers.com/video/installing-mysql-with-debconf

#permet d'eviter une fenetre interactive
export DEBIAN_FRONTEND="noninteractive"

sudo debconf-set-selections <<< "mysql-server mysql-server/root_password password toor"
sudo debconf-set-selections <<< "mysql-server mysql-server/root_password_again password toor"

sudo apt-get install -y mysql-server-5.5

service mysql-server-5.5 restart

#Installation de mysql-client, apache2, php5, libapache2-mod-php5, php5-mysql
echo "======================================================================================"
echo "Installation de mysql-client, apache2, php5, libapache2-mod-php5, php5-mysql..."
echo "======================================================================================"

sudo apt-get install -y mysql-client apache2 php5 libapache2-mod-php5 php5-mysql

#http://stackoverflow.com/questions/22440298/preseeding-phpmyadmin-skip-multiselect-skip-password
#Installation de phpmyadmin

echo "=================================="
echo "Installation de phpmyadmin..."
echo "=================================="

sudo debconf-set-selections <<< "phpmyadmin phpmyadmin/reconfigure-webserver multiselect apache2"
sudo debconf-set-selections <<< "phpmyadmin phpmyadmin/dbconfig-install boolean true"
sudo debconf-set-selections <<< "phpmyadmin phpmyadmin/mysql/admin-user string root"
sudo debconf-set-selections <<< "phpmyadmin phpmyadmin/mysql/admin-pass password toor"
sudo debconf-set-selections <<< "phpmyadmin phpmyadmin/mysql/app-pass password pwd"
sudo debconf-set-selections <<< "phpmyadmin phpmyadmin/app-password-confirm password pwd"

sudo apt-get install -y phpmyadmin

#Enleve les dependances de packages qui ne sont plus necessaires
sudo apt-get autoremove

#Indique le status du serveur Apache, devrait normalement etre operationnel
/etc/init.d/apache2 status
