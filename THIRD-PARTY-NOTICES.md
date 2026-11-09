# Third-party references and licenses

The application domain code is an original educational implementation.

Spring Petclinic (https://github.com/spring-projects/spring-petclinic) was
reviewed for its owner/pet/visit model, layered Spring structure and Maven
Wrapper usage. No Petclinic Java application code, templates, images or data
were copied. Petclinic is licensed under Apache-2.0.

`mvnw` and `mvnw.cmd` are standard Apache Maven Wrapper 3.3.4 scripts generated
using `mvn wrapper:wrapper -Dmaven=3.9.11 -Dtype=only-script`.
Their original Apache Software Foundation license headers are retained.
Source: https://github.com/apache/maven-wrapper/tree/maven-wrapper-3.3.4
License text: `licenses/Apache-2.0.txt`.

Spring Boot, Spring Framework, Spring Data and Hibernate Validator are used
under their respective open source licenses. Hibernate ORM is LGPL-2.1,
H2 is dual licensed MPL-2.0/EPL-1.0, and dependencies remain externally
resolved by Maven. See each dependency's published POM and license metadata.
