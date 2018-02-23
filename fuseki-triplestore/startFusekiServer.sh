export FUSEKI_HOME=/home/ubuntu/fuseki-triplestore

su ubuntu -c 'nohup java -Xmx1000M -jar $FUSEKI_HOME/fuseki-server.jar --mem --update --port=3030 /ds  >> /home/ubuntu/logs/fuseki.log & '
