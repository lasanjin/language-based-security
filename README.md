# Language-Based Security
Project from Language-Based Security course at Chalmers. Runs with Java 11 and Python 3.6

## How to run
- Execute script
```
$ ./run run all 
```

- DIY
  1. Run Spring Boot application [SecureLogin](SecureLogin)
     - `cd SecureLogin/ && mvn clean package && cd ..`
     - `mvn -f SecureLogin/ spring-boot:start`
  2. Execute brute-force attack(s)
     - `./attacks/bruteforce.py`
     - `./attacks/multithreaded_bruteforce.py`
  3. Kill Spring application
     - `ps aux | grep SpringApplication | awk 'NR==1{print $2}' | xargs kill -9`


## Demo
<img src="demo.gif">
