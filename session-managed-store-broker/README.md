$ mvn clean package
$ cf push
$ cf create-service-broker session-managed-store-broker user passwd http://session-managed-store-broker.\<DOMAIN\>
$ cf enable-service-access session-managed-store -p \<PLAN\> -o \<ORG\>
$ cf create-service session-managed-store plan1 session-managed-store
