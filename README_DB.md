MySQL connection and testing notes for the Identity Requests plugin

1) JDBC connection properties used by the plugin (IdentityRequestService):

   jdbc url: jdbc:mysql://localhost/identityiq?useServerPrepStmts=true&tinyInt1isBit=true&useSSL=false&characterEncoding=UTF-8&serverTimezone=UTC
   user: identityiq
   password: identityiq
   driver class: com.mysql.cj.jdbc.Driver

2) Place the MySQL Connector/J JAR (mysql-connector-java-X.X.X.jar) into your Tomcat/IIQ classpath (e.g., TOMCAT_HOME/lib or the IIQ lib folder) so DriverManager can load the driver.

3) Sample query to run in MySQL Workbench (file: db/sample_identity_request_query.sql):

   SELECT * FROM identityiq.spt_identity_request;

4) After updating source and packaging the plugin, rebuild the plugin zip and deploy to IIQ's plugins location (WEB-INF/plugins or via Admin UI), then restart Tomcat/IIQ.

5) Postman collection for testing is under postman/IdentityRequests.postman_collection.json — update collection variable base_url to your IIQ URL and ensure you have an authenticated session cookie.

If you want, I can add a build step (Ant) to package the plugin automatically and include the MySQL connector copy step.
