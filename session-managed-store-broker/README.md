$ mvn clean package</br>
$ cf push</br>
$ cf create-service-broker session-managed-store-broker user passwd http://session-managed-store-broker.\<DOMAIN\></br>
$ cf enable-service-access session-managed-store -p \<PLAN\> -o \<ORG\></br>
$ cf create-service session-managed-store plan1 session-managed-store
