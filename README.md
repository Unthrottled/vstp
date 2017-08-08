# VARIABLE SWEG TARGET PROXY

ADDS COOKIES AND BUTT TOKENS FOR CSRF FILTER.
ADDS CORS ACCEPT HEADERS TO PROXIED RESPONSES.

       gradle clean build

       java -jar VSTP-1.0.0.jar 6969

       curl -i "http://localhost:6969/?target=http%3A%2F%2Fhailhydra.dtie%3A8280%2Fcache-web-service-3.1%2Fswagger.json&_=1502216228294"
