# CSRF Attack Demo

This demo application shows how a CSRF attack works
and how to defend against this attack type.

The demo provides a simple Rest API dealing with customers:

* [localhost:8080/api](http://localhost:8080/api): Gets the list of customers
* [localhost:8080/api/create?firstname=A&lastname=BadGuy](http://localhost:8080/api/create?firstname=A&lastname=BadGuy): Creates a customer via a GET request 
  (Just for the demo to show why it is a __BAD__ idea to use GET requests to create resources in Rest APIs)
* localhost:8080/api/create: Creates a customer via a POST request
* [localhost:8080/web/form](http://localhost:8080/web/form): Shows a web form (Spring MVC/Thymeleaf) to create
  a customer via a POST request (using CSRF protection)
  
To try the CSRF attack: 

1. please start the application by running _CsrfAttackDemoApplication_
2. clean up the logs from the console log
3. open the file _attack/index.html_ with a web browser (e.g. in IntelliJ by using a right click on this file)
4. Try the two attack types using a GET, and a POST request via the corresponding links  
4. look into the console log: You should see 2 log statements telling that 2 customers have been created
   one using a GET request and another one using a POST request.
   This has been the CSRF attack that succeeded on both the GET and POST request.
  
No comment the line ```.csrf().disable()``` in the _WebSecurityConfiguration_ class to re-enable
the CSRF protection.

No try the CSRF attack again:

1. please re-start the application by running _CsrfAttackDemoApplication_
2. clean up the logs from the console log
3. open the file _attack/index.html_ with a web browser again
4. look into the console log: You should still see 1 log statement telling that 
   1 customer has been created using a GET request.
   This has been again the CSRF attack that this time only succeeded on the GET request.
   
So we re-enabled CSRF protection but why did the attack still work for GET request?
This is because the CSRF protection only works for POST, PUT, PATCH and DELETE requests by default
and NOT for GET requests. This is basically why it is a __BAD__ idea to use GET requests to create resources!

So keep CSRF protection enabled. You can safely switch it off for two reasons:

1. You are using stateless authentication using bearer tokens (so no session cookies needed anymore)
2. You are just doing it for demo purposes via postman or curl in a non-productive environment
