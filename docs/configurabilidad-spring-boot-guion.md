# Guion didactico: configurabilidad, registro, administracion y Docker en Spring Boot

Duracion: 3 sesiones de 1 hora y 50 minutos.

Estructura de cada sesion:

- 50 minutos de clase y demo.
- 10 minutos de descanso.
- 50 minutos de practica guiada, lectura critica y cierre.

La historia del tema es esta:

```text
1. Una aplicacion arranca con properties locales.
2. Sacamos la configuracion fuera del binario con Spring Cloud Config.
3. Registramos servicios con Eureka para no depender de IPs fijas.
4. Los observamos desde Spring Boot Admin.
5. Lo empaquetamos en Docker y levantamos todo con Docker Compose.
```

## Objetivos de aprendizaje

Al terminar, el alumno debe poder explicar y demostrar:

- Que diferencia hay entre configuracion local, variables de entorno, perfiles y configuracion externa.
- Como funciona un Config Server y como lo consume un cliente Spring Boot.
- Para que sirve Eureka y que significa registrar una instancia.
- Que aporta Spring Boot Admin encima de Actuator.
- Como Docker cambia el significado de `localhost`.
- Por que `depends_on` no garantiza que un servicio este listo.
- Como levantar dos instancias del mismo servicio y verlas en Eureka.

## Mapa de la demo

Servicios preparados:

| Pieza | Ruta | Puerto | Funcion |
|---|---|---:|---|
| Eureka Registry | `microservices/registry` | `8761` | Registro de servicios |
| Config Server | `microservices/configuration-server` | `8889` | Configuracion centralizada |
| Spring Boot Admin | `microservices/admin-server` | `9090` | Panel de administracion |
| Bills | `microservices/billsmicroservice` | `8040` | Microservicio de ejemplo |
| Bills 2 | `bills-microservice-2` en Compose | interno `8040` | Segunda instancia para Eureka |

Nota: Spring Cloud Config suele verse en el puerto `8888`. En este repo se usa `8889` para evitar conflictos frecuentes en Windows.

## Brujula conceptual

Usa esta tabla cuando notes que la clase esta mezclando piezas. Es normal: todas aparecen juntas, pero no hacen lo mismo.

| Pieza | Que es | Que problema resuelve | Que no es |
|---|---|---|---|
| Spring Boot | Framework de aplicacion | Crear una app Java arrancable con servidor embebido | Un sistema de microservicios por si solo |
| Actuator | Endpoints tecnicos dentro de cada app | Saber salud, metricas, propiedades y logs de una app | Un panel centralizado |
| Config Server | Servidor de propiedades | Centralizar configuracion fuera del jar | Un registro de servicios |
| Eureka Server | Registro de servicios | Saber que instancias estan vivas y donde estan | Un proxy, gateway o balanceador HTTP completo |
| Eureka Client | Cliente que se registra en Eureka | Anunciar "estoy vivo en esta URL" | Una interfaz de usuario |
| Spring Boot Admin | Aplicacion web de administracion | Ver muchas apps Spring Boot desde un unico panel | Un sistema completo de observabilidad |
| Docker Compose | Orquestador local sencillo | Arrancar varias piezas juntas en una red local | Kubernetes |

Frase para repetir durante las sesiones:

> Config Server responde "con que configuracion arranco", Eureka responde "donde estan las instancias" y Admin responde "como estan ahora".

## Preparacion antes de clase

Comprueba que todo compila:

```powershell
.\mvnw.cmd -pl microservices/registry,microservices/configuration-server,microservices/admin-server,microservices/billsmicroservice -am package -DskipTests
```

Si vas a usar Docker:

```powershell
docker version
docker compose config
```

Puertos que deben estar libres:

```text
8761  Eureka
8889  Config Server
9090  Spring Boot Admin
8040  Bills principal
8041  Bills local secundario, solo para alternativa sin Docker
3306  MySQL del compose, aunque Bills usa H2 en esta demo
```

Comandos rapidos de validacion:

```powershell
curl http://localhost:8889/bills-microservice/development
curl http://localhost:8040/api/v1/bills/whoami/alumno
curl -H "Accept: application/json" http://localhost:8761/eureka/apps
curl http://localhost:9090/instances
```

## Sesion 1: configuracion externa con Spring Cloud Config

### Mensaje de la sesion

Un jar no deberia cambiar porque cambie el entorno. Lo que cambia entre local, Docker, test o produccion es la configuracion que rodea al binario.

### 0-8 min: situar el problema

Pregunta inicial:

- "Que cosas cambian cuando pasamos de local a Docker?"
- "Que propiedad no querriais recompilar para cambiar?"
- "Que ocurre si una URL o una clave esta escrita directamente en el codigo?"

Muestra:

```text
microservices/billsmicroservice/src/main/resources/application.properties
```

Explica tres familias de propiedades:

| Tipo | Ejemplos | Pregunta didactica |
|---|---|---|
| Identidad | `spring.application.name`, `server.port` | Como se llama y donde escucha |
| Infraestructura | `EUREKA_URI`, `CONFIG_SERVER_URI` | Con quien habla |
| Negocio/demo | `user.role` | Que comportamiento cambia |

Frase util:

> Hoy vamos a perseguir una propiedad, `user.role`, desde el fichero local hasta Config Server, Eureka y Admin.

### 8-25 min: configuracion de Spring Boot sin magia

Explica la idea, no todos los detalles de precedencia:

```text
application.properties -> perfiles -> variables de entorno -> argumentos -> Config Server
```

Demo con valor local:

```powershell
.\mvnw.cmd -pl microservices/billsmicroservice spring-boot:run
curl http://localhost:8040/api/v1/bills/whoami/alumno
```

Resultado esperado si no esta Config Server:

```text
Hello! You're alumno and you'll become a(n) admin...
```

Demo con argumento:

```powershell
.\mvnw.cmd -pl microservices/billsmicroservice spring-boot:run "-Dspring-boot.run.arguments=--user.role=Teacher"
```

Idea para remarcar:

- No he tocado codigo.
- No he recompilado por cambiar el rol.
- La configuracion esta entrando desde fuera.

### 25-40 min: Actuator como lupa

Explica:

> Actuator no es el sistema de monitorizacion completo. Es la manera estandar de preguntarle a una aplicacion Spring Boot como esta y con que esta arrancando.

Comandos:

```powershell
curl http://localhost:8040/actuator/health
curl http://localhost:8040/actuator/info
curl http://localhost:8040/actuator/env/user.role
```

Pregunta al grupo:

- "Que endpoint os daria miedo exponer sin seguridad?"

Respuesta esperada:

- `env`
- `configprops`

Mensaje:

> Actuator es potente precisamente porque puede revelar demasiado.

### 40-55 min: crear el servidor de configuracion

Muestra:

```text
microservices/configuration-server/src/main/java/.../ConfigServerApplication.java
microservices/configuration-server/src/main/resources/application.properties
microservices/configuration-server/src/main/resources/config-repo/bills-microservice-development.properties
```

Explica cada pieza:

| Pieza | Significado |
|---|---|
| `@EnableConfigServer` | Activa el servidor de configuracion |
| `spring.profiles.active=native` | Usa ficheros locales del classpath para la demo |
| `config-repo` | Simula el repositorio de configuracion |
| `bills-microservice-development.properties` | Configuracion de Bills con perfil `development` |

Demo:

```powershell
.\mvnw.cmd -pl microservices/configuration-server spring-boot:run
curl http://localhost:8889/bills-microservice/development
```

Resultado esperado:

```text
user.role=Developer from Spring Cloud Config
```

### Variante: servir configuracion desde un repositorio Git

La demo anterior usa `native` porque es la forma mas rapida de entender el mecanismo: el Config Server lee ficheros que ya van dentro del proyecto. En un escenario mas realista, esos ficheros no deberian vivir dentro del jar del Config Server, sino en un repositorio externo.

Idea:

```text
Git repo de configuracion -> Config Server -> microservicios clientes
```

Por que es interesante:

- Permite cambiar configuracion sin recompilar ni redistribuir los microservicios.
- Da historial, diff, autoria y rollback con Git.
- Separa el binario de la aplicacion de las decisiones de entorno.
- Evita copiar el mismo `application.properties` en muchos servicios.
- Facilita tener configuracion distinta por aplicacion y perfil: `bills-microservice-development.properties`, `bills-microservice-classroom.properties`, etc.

En este repo hay un script preparado para crear un repositorio Git local con los ficheros del ejemplo:

```text
microservices/configuration-server/src/main/resources/repo-initialization-script.txt
```

Ejecutalo en una terminal de Windows. Creara:

```text
C:\config-repo
```

con estos ficheros:

```text
application.properties
bills-microservice.properties
bills-microservice-development.properties
bills-microservice-classroom.properties
```

Para que el Config Server lea desde ese repositorio, cambia en:

```text
microservices/configuration-server/src/main/resources/application.properties
```

de:

```properties
spring.profiles.active=native
spring.cloud.config.server.native.search-locations=classpath:/config-repo
```

a:

```properties
spring.profiles.active=git
# spring.cloud.config.server.native.search-locations=classpath:/config-repo
spring.cloud.config.server.git.uri=file:///c:/config-repo
spring.cloud.config.server.git.clone-on-start=true
spring.cloud.config.server.git.default-label=main
```

Si se usa Docker, recuerda que `docker-compose.yml` puede pisar el perfil activo con una variable de entorno. Para probar esta variante, cambia el servicio `config-server`:

```yaml
environment:
  EUREKA_URI: http://registry:8761/eureka
  SPRING_PROFILES_ACTIVE: git
```

Comprobacion:

```powershell
.\mvnw.cmd -pl microservices/configuration-server spring-boot:run
curl http://localhost:8889/bills-microservice/development
curl http://localhost:8889/bills-microservice/classroom
```

Resultado esperado:

```text
development -> user.role=Developer from Spring Cloud Config
classroom   -> user.role=Student from classroom profile
```

Mensaje didactico:

> El Config Server no es interesante porque "tenga properties". Es interesante porque convierte la configuracion en una fuente centralizada, versionada y consultable por todos los servicios al arrancar.

### 55-65 min: descanso

Deja visible el JSON del Config Server. Al volver, empieza preguntando:

- "Si este valor esta en Config Server, quien tiene que pedirlo?"

### 65-85 min: hacer que Bills consuma Config Server

Muestra en Bills:

```properties
spring.config.import=optional:configserver:
spring.cloud.config.uri=${CONFIG_SERVER_URI:http://localhost:8889}
```

Explica:

- `spring.config.import`: integra Config Server en el arranque de Spring Boot.
- `optional`: si Config Server no esta, la app puede arrancar con configuracion local.
- `CONFIG_SERVER_URI`: permite cambiar la URL sin tocar el jar.

Arranque recomendado:

1. Config Server.
2. Bills.

Comprobacion:

```powershell
curl http://localhost:8040/api/v1/bills/whoami/alumno
```

Resultado esperado:

```text
Hello! You're alumno and you'll become a(n) Developer from Spring Cloud Config...
```

Punto didactico:

> Si responde `admin`, Bills no ha leido Config Server. No es un fallo misterioso: es una pista para mirar orden de arranque, URL o disponibilidad.

### 85-96 min: ejercicio guiado

Ejercicio:

1. Cambiar `user.role` en:

```text
microservices/configuration-server/src/main/resources/config-repo/bills-microservice-development.properties
```

2. Reiniciar Config Server y Bills.
3. Comprobar:

```powershell
curl http://localhost:8040/api/v1/bills/whoami/alumno
```

Variante si van rapidos:

1. Anadir:

```properties
info.lesson=configuration
```

2. Verlo en:

```powershell
curl http://localhost:8040/actuator/info
```

### 96-108 min: perfiles en Spring Boot

Este bloque encaja al final de la sesion 1, justo despues de explicar Config Server. Si la sesion va justa, muestra solo la comprobacion contra Config Server y deja el arranque de Bills como ejercicio.

Idea didactica:

Un perfil permite tener variantes de configuracion para la misma aplicacion:

```text
bills-microservice + development -> configuracion de desarrollo
bills-microservice + classroom   -> configuracion para clase
bills-microservice + prod        -> configuracion de produccion
```

La aplicacion sigue siendo la misma. Lo que cambia es el conjunto de propiedades activo.

Ejemplo preparado en el repo:

Hay dos ficheros remotos para Bills dentro del Config Server:

```text
microservices/configuration-server/src/main/resources/config-repo/bills-microservice-development.properties
microservices/configuration-server/src/main/resources/config-repo/bills-microservice-classroom.properties
```

El perfil `development` devuelve:

```properties
user.role=Developer from Spring Cloud Config
```

El perfil `classroom` devuelve:

```properties
user.role=Student from classroom profile
```

Probar los perfiles directamente en Config Server:

Con Config Server arrancado:

```powershell
curl http://localhost:8889/bills-microservice/development
curl http://localhost:8889/bills-microservice/classroom
```

Pregunta al grupo:

- "Que parte de la URL cambia?"

Respuesta esperada:

- Cambia el perfil: `development` frente a `classroom`.

Arrancar Bills con otro perfil sin Docker:

Primero arranca Eureka y Config Server. Luego arranca Bills con el perfil `classroom`:

```powershell
java -jar microservices/billsmicroservice/target/billsmicroservice-0.0.1-SNAPSHOT.jar --spring.profiles.active=classroom
```

Comprueba:

```powershell
curl http://localhost:8040/api/v1/bills/whoami/alumno
```

Resultado esperado:

```text
Hello! You're alumno and you'll become a(n) Student from classroom profile...
```

Arrancar Bills con otro perfil en Docker Compose:

Puedes sobreescribir el perfil del contenedor desde la linea de comandos:

```powershell
docker compose up --build -d registry config-server admin-server
docker compose run --rm -p 8042:8040 -e SPRING_PROFILES_ACTIVE=classroom bills-microservice
```

Comprueba:

```powershell
curl http://localhost:8042/api/v1/bills/whoami/alumno
```

Resultado esperado:

```text
Hello! You're alumno and you'll become a(n) Student from classroom profile...
```

Mensaje clave:

```text
Perfil = seleccion de configuracion.
Config Server = lugar desde donde se sirve esa configuracion.
Variable de entorno o argumento = forma de elegir el perfil en despliegue.
```

No uses perfiles para cambiar codigo de negocio. Usalos para cambiar configuracion de entorno: URLs, credenciales externas, limites, flags, niveles de log o parametros operativos.

### 108-110 min: cierre

Tres ideas para cerrar:

- Configurar no es "tener muchos properties"; es separar decisiones del binario.
- Config Server permite centralizar, auditar y versionar configuracion.
- Actuator permite comprobar la configuracion real con la que la app ha arrancado.

Pregunta final:

- "Que configuracion meteriais en Config Server y que configuracion no meteriais nunca ahi?"

## Sesion 2: Eureka y Spring Boot Admin

### Mensaje de la sesion

Cuando hay varios servicios, no basta con que arranquen: tienen que encontrarse y tenemos que poder observarlos.

### 0-10 min: conectar con la sesion anterior

Recupera la historia:

```text
Bills ya puede pedir configuracion a Config Server.
Ahora queremos que los servicios se anuncien.
Despues queremos verlos desde un panel.
```

Dibujo para pizarra:

```text
Config Server ----\
Admin Server ------> Eureka Registry
Bills ------------/

Admin Server -> Actuator de cada servicio
```

Explica:

- Eureka responde a "quien existe y donde esta".
- Admin responde a "como esta cada aplicacion".

Vocabulario minimo antes de tocar codigo:

| Palabra | Significado en la demo |
|---|---|
| Servicio | Tipo de aplicacion, por ejemplo `BILLS-MICROSERVICE` |
| Instancia | Un proceso concreto de ese servicio, por ejemplo Bills en `8040` |
| Registro | Lista viva de servicios e instancias |
| Cliente Eureka | Aplicacion que se anuncia al registro |
| Heartbeat | Senal periodica de "sigo vivo" |
| Actuator | Endpoints que Admin consulta dentro de cada app |

Ejemplo con personas:

```text
Servicio: "profesor"
Instancias: "profesor en aula 1", "profesor en aula 2"
Registro: conserjeria que sabe en que aula esta cada uno
Admin: panel que mira el estado de cada aula sin entrar manualmente
```

### 10-30 min: Eureka Server

Muestra:

```text
microservices/registry
```

Codigo clave:

```java
@EnableEurekaServer
```

Properties clave:

```properties
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

Explicacion:

- El registry es el punto de encuentro.
- No necesita registrarse en si mismo.
- No necesita descargar una lista de servicios de otro registry.

Cuenta el flujo sin Spring primero:

```text
1. Arranca Eureka.
2. Arranca Bills.
3. Bills envia a Eureka: me llamo BILLS-MICROSERVICE y estoy en esta URL.
4. Eureka guarda esa entrada durante un tiempo.
5. Bills renueva la entrada periodicamente con heartbeats.
6. Si Bills deja de renovar, Eureka acaba quitandolo del registro.
```

Idea importante:

> Eureka no llama a los endpoints de negocio de Bills. Solo mantiene informacion de localizacion y estado de registro.

Demo:

```powershell
.\mvnw.cmd -pl microservices/registry spring-boot:run
```

Abrir:

```text
http://localhost:8761
```

Pregunta:

- "Que deberia aparecer ahora?"

Respuesta:

- Nada relevante todavia. El registry esta vivo, pero nadie se ha registrado.

### 30-50 min: registrar servicios

Muestra en Bills:

```properties
spring.application.name=bills-microservice
eureka.client.serviceUrl.defaultZone=${EUREKA_URI:http://localhost:8761/eureka}
eureka.client.fetch-registry=true
eureka.client.register-with-eureka=true
eureka.instance.prefer-ip-address=true
```

Explica:

| Propiedad | Lectura humana |
|---|---|
| `spring.application.name` | Nombre con el que aparezco agrupado |
| `defaultZone` | Donde esta Eureka |
| `register-with-eureka` | Me anuncio |
| `fetch-registry` | Puedo consultar otros servicios |
| `prefer-ip-address` | Publico IP en lugar de hostname |

Aclara la diferencia entre aplicacion e instancia:

```text
BILLS-MICROSERVICE
  instancia 1 -> http://192.168.x.x:8040
  instancia 2 -> http://192.168.x.x:8041
```

Esto prepara la sesion 3: cuando lancemos dos Bills, Eureka no mostrara dos aplicaciones distintas, sino dos instancias bajo el mismo nombre.

Arranca:

1. Config Server.
2. Admin Server.
3. Bills.

Vuelve a:

```text
http://localhost:8761
```

Busca:

- `CONFIG-SERVER`
- `ADMIN-SERVER`
- `BILLS-MICROSERVICE`

Comprobacion por API:

```powershell
curl -H "Accept: application/json" http://localhost:8761/eureka/apps
```

Lectura guiada de la respuesta:

- Busca el nombre de aplicacion.
- Busca `instanceId`.
- Busca `status`.
- Busca `homePageUrl`.

Pregunta:

- "Que dato usaria otro servicio para saber donde llamar?"

### 50-60 min: descanso

Deja abierta la UI de Eureka con los servicios registrados.

### 60-80 min: Spring Boot Admin

Muestra:

```text
microservices/admin-server
```

Codigo clave:

```java
@EnableAdminServer
@EnableDiscoveryClient
```

Explica:

- Admin Server se registra en Eureka.
- Admin Server tambien consulta Eureka.
- No hace falta poner `spring-boot-admin-starter-client` en cada app si Admin descubre por Eureka.

Define Spring Boot Admin con una frase clara:

> Spring Boot Admin es una aplicacion Spring Boot que descubre otras aplicaciones Spring Boot y muestra sus endpoints Actuator en una interfaz web.

Flujo interno:

```text
1. Admin pregunta a Eureka que aplicaciones existen.
2. Eureka devuelve nombre, URL e instancia de cada app.
3. Admin llama a los endpoints Actuator de esas apps.
4. Admin pinta salud, entorno, metricas, logs y detalles tecnicos.
```

Por eso hacen falta dos cosas:

| Necesidad | Donde se configura |
|---|---|
| Que Admin encuentre la app | Eureka |
| Que Admin pueda leer informacion de la app | Actuator expuesto en cada servicio |

En Bills, ensena:

```properties
management.endpoints.web.exposure.include=health,info,metrics,env,loggers,configprops
management.endpoint.health.show-details=always
```

Advertencia didactica:

> En clase exponemos `env` y `configprops` para aprender. En un entorno real esto exige seguridad, porque puede revelar URLs, tokens o secretos mal gestionados.

Demo:

```powershell
.\mvnw.cmd -pl microservices/admin-server spring-boot:run
```

Abrir:

```text
http://localhost:9090
```

### 80-98 min: recorrido por Admin

Ruta sugerida:

1. Abrir `BILLS-MICROSERVICE`.
2. Ver `Health`.
3. Ver `Info` y buscar `config.source=config-server-native-repository`.
4. Ver `Environment` y buscar `user.role`.
5. Ver `Metrics` y abrir alguna metrica HTTP.
6. Ver `Loggers`, cambiar temporalmente un logger y volverlo a dejar como estaba.

Preguntas durante el recorrido:

- "Que pantalla mirarias si una app esta caida?"
- "Que pantalla mirarias si una app arranca con una propiedad rara?"
- "Que pantalla mirarias para cambiar logging sin reiniciar?"

Mini reto de 3 minutos:

1. En Admin, abre `Environment` de Bills.
2. Busca `user.role`.
3. Decide si el valor viene de Config Server o del fichero local.

Respuesta esperada:

- Debe venir de Config Server.
- Si aparece `admin`, Bills no esta leyendo la configuracion remota.

### 98-110 min: cierre

Tabla de cierre:

| Herramienta | Pregunta que responde |
|---|---|
| Config Server | Con que configuracion arranca |
| Eureka | Que servicios existen y donde estan |
| Actuator | Que sabe contar una app sobre si misma |
| Admin Server | Donde veo todo sin entrar servicio por servicio |

Ejercicio si sobra tiempo:

- Cambiar `info.app.description` de Bills.
- Reiniciar Bills.
- Ver el cambio en Admin.

## Sesion 3: Docker, Compose y dos instancias

### Mensaje de la sesion

Docker no cambia Spring, pero cambia el entorno: nombres de host, puertos, orden de arranque y red.

### 0-10 min: abrir con el problema de `localhost`

Preguntas:

- "Que significa `localhost` dentro de un contenedor?"
- "Por que `http://localhost:8889` no sirve para hablar con otro contenedor?"
- "Que pasa si Bills arranca antes de que Config Server este listo?"

Respuesta que debe quedar:

```text
Dentro de Docker, localhost es el propio contenedor.
Para hablar con otro servicio se usa su nombre de servicio: config-server, registry, admin-server.
```

### 10-30 min: Dockerfile

Muestra:

```text
docker/Dockerfile.springboot
```

Lee el Dockerfile por bloques:

| Bloque | Explicacion |
|---|---|
| `FROM eclipse-temurin:21-jdk AS build` | Imagen para compilar |
| `ARG MODULE` | Permite construir modulos distintos |
| `RUN sed -i 's/\r$//' ./mvnw ...` | Arreglo practico para wrapper Maven en Windows/Linux |
| `FROM eclipse-temurin:21-jre` | Imagen final mas pequena |
| `ENTRYPOINT ["java", "-jar", "/app/app.jar"]` | Contrato de ejecucion |

Demo:

```powershell
docker build -f docker/Dockerfile.springboot --build-arg MODULE=microservices/registry -t petclinic-registry-demo .
docker run --rm -p 8761:8761 petclinic-registry-demo
```

Si el build tarda:

- Explica capas.
- Explica cache.
- Explica por que en una clase conviene tener imagenes preparadas.

### 30-50 min: Docker Compose como mapa del entorno

Muestra:

```text
docker-compose.yml
```

Lectura guiada:

| Elemento | Explicacion |
|---|---|
| `services` | Procesos que forman el entorno |
| `ports` | Puerto host -> puerto contenedor |
| `environment` | Configuracion externa |
| `healthcheck` | Como sabe Compose que el servicio esta listo |
| `depends_on.condition: service_healthy` | Espera readiness, no solo arranque |
| `profiles` | Servicios opcionales, como la segunda instancia |

Punto importante:

> `depends_on` sin healthcheck solo ordena arranque. Con `service_healthy`, la demo se vuelve repetible.

### 50-60 min: descanso

Deja el terminal preparado en la raiz del repo.

### 60-78 min: levantar la pila completa

Comando:

```powershell
docker compose up --build -d
docker compose ps
```

Espera hasta ver `healthy` en:

- `registry`
- `config-server`
- `admin-server`
- `bills-microservice`

Si alguien pregunta por MySQL:

> Esta en el compose porque el proyecto ya lo tenia como infraestructura. En esta demo concreta Bills usa H2; MySQL no es el foco.

### 78-94 min: validar el circuito

Config Server:

```powershell
curl http://localhost:8889/bills-microservice/development
```

Bills:

```powershell
curl http://localhost:8040/api/v1/bills/whoami/alumno
```

Eureka:

```powershell
curl -H "Accept: application/json" http://localhost:8761/eureka/apps
```

Admin:

```powershell
curl http://localhost:9090/instances
```

Resultados esperados:

- Config Server devuelve `user.role=Developer from Spring Cloud Config`.
- Bills responde con `Developer from Spring Cloud Config`.
- Eureka lista `CONFIG-SERVER`, `ADMIN-SERVER` y `BILLS-MICROSERVICE`.
- Admin lista las aplicaciones descubiertas.

Si Bills responde `admin`:

```powershell
docker compose logs bills-microservice
```

Explicacion:

> Es casi seguro que Bills arranco sin leer Config Server. La demo nos esta ensenando por que importan readiness y healthchecks.

### 94-104 min: dos instancias de Bills

Activa el perfil opcional:

```powershell
docker compose --profile scale-demo up -d bills-microservice-2
docker compose --profile scale-demo ps
```

Comprueba Eureka:

```powershell
$r = Invoke-RestMethod -Headers @{Accept='application/json'} http://localhost:8761/eureka/apps/BILLS-MICROSERVICE
@($r.application.instance) | Select-Object instanceId,hostName,status
@($r.application.instance).Count
```

Resultado esperado:

```text
Count = 2
```

Explicacion didactica:

- Eureka agrupa por `spring.application.name`.
- Las dos instancias son la misma aplicacion: `BILLS-MICROSERVICE`.
- Cada instancia tiene su propio `instanceId`, IP interna y health URL.
- La segunda instancia no publica puerto al host para evitar conflicto con `8040`.

Comprueba Admin:

```powershell
$instances = @(((Invoke-WebRequest -UseBasicParsing http://localhost:9090/instances).Content | ConvertFrom-Json))
$instances | Where-Object { $_.registration.name -eq 'BILLS-MICROSERVICE' } | Select-Object @{Name='name';Expression={$_.registration.name}},@{Name='serviceUrl';Expression={$_.registration.serviceUrl}},@{Name='status';Expression={$_.statusInfo.status}}
```

Resultado esperado:

```text
Dos entradas BILLS-MICROSERVICE
```

### 104-108 min: lectura critica

Ideas clave:

- Docker Compose es muy bueno para explicar entorno local, pero no es Kubernetes.
- Config Server opcional es comodo, pero puede esconder fallos.
- Eureka no sustituye a un gateway ni a un balanceador completo.
- Admin Server es muy util para inspeccion, no sustituye Prometheus, Grafana o tracing.
- La parte dificil no es arrancar servicios: es hacer que arranquen de forma repetible.

### 108-110 min: cierre

Apagado:

```powershell
docker compose --profile scale-demo down --remove-orphans
```

Frase final:

> Configurabilidad es conseguir que la misma aplicacion se adapte al entorno sin recompilar, sin perseguir IPs y sin mirar servicio por servicio.

## Alternativa completa sin Docker

Usa esta ruta si los alumnos no tienen Docker. Cubre Config Server, Eureka, Admin y dos instancias de Bills usando procesos Java locales.

### Paso 1: compilar

```powershell
.\mvnw.cmd -pl microservices/registry,microservices/configuration-server,microservices/admin-server,microservices/billsmicroservice -am package -DskipTests
```

### Paso 2: abrir cinco terminales

Terminal 1: Eureka.

```powershell
java -jar microservices/registry/target/registry-0.0.1-SNAPSHOT.jar
```

Terminal 2: Config Server.

```powershell
java -jar microservices/configuration-server/target/config-server-0.0.1-SNAPSHOT.jar
```

Terminal 3: Admin Server.

```powershell
java -jar microservices/admin-server/target/admin-server-0.0.1-SNAPSHOT.jar
```

Terminal 4: Bills 1.

```powershell
java -jar microservices/billsmicroservice/target/billsmicroservice-0.0.1-SNAPSHOT.jar
```

Terminal 5: Bills 2 en otro puerto.

```powershell
java -jar microservices/billsmicroservice/target/billsmicroservice-0.0.1-SNAPSHOT.jar --server.port=8041 --eureka.instance.instance-id=bills-microservice-8041
```

### Paso 3: comprobar

Config Server:

```powershell
curl http://localhost:8889/bills-microservice/development
```

Bills 1:

```powershell
curl http://localhost:8040/api/v1/bills/whoami/alumno
```

Bills 2:

```powershell
curl http://localhost:8041/api/v1/bills/whoami/alumno
```

Eureka:

```powershell
curl -H "Accept: application/json" http://localhost:8761/eureka/apps/BILLS-MICROSERVICE
```

Admin:

```powershell
curl http://localhost:9090/instances
```

Resultado esperado:

- Bills 1 responde con `Developer from Spring Cloud Config`.
- Bills 2 responde con `Developer from Spring Cloud Config`.
- Eureka muestra dos instancias de `BILLS-MICROSERVICE`.
- Admin muestra dos entradas de Bills.

### Diferencia didactica frente a Docker

| Con Docker | Sin Docker |
|---|---|
| Los servicios se hablan por nombre de servicio: `config-server`, `registry` | Los servicios usan `localhost` |
| La segunda instancia vive en la red interna y no publica puerto | La segunda instancia necesita `--server.port=8041` |
| Se ve el problema real de readiness con contenedores | Se ve mejor el arranque paso a paso |
| Hay healthchecks en Compose | La comprobacion la hace el profesor manualmente |

## Checklist final del profesor

Antes de dar la clase, comprueba:

- `docker compose config` es valido.
- `docker compose up --build -d` arranca.
- `docker compose ps` muestra servicios `healthy`.
- `curl http://localhost:8889/bills-microservice/development` devuelve `user.role`.
- `curl http://localhost:8040/api/v1/bills/whoami/alumno` usa el valor remoto.
- `http://localhost:8761` muestra Eureka.
- `http://localhost:9090` muestra Spring Boot Admin.
- `docker compose --profile scale-demo up -d bills-microservice-2` crea la segunda instancia.
- Eureka muestra dos instancias de `BILLS-MICROSERVICE`.

## Solucion de problemas rapida

### El puerto 8889 esta ocupado

Cambia temporalmente el puerto del Config Server:

```powershell
java -jar microservices/configuration-server/target/config-server-0.0.1-SNAPSHOT.jar --server.port=8890
```

Y arranca Bills con:

```powershell
java -jar microservices/billsmicroservice/target/billsmicroservice-0.0.1-SNAPSHOT.jar --spring.cloud.config.uri=http://localhost:8890
```

### Bills responde `admin`

Significa que no ha tomado Config Server.

Comprueba:

```powershell
curl http://localhost:8889/bills-microservice/development
docker compose logs bills-microservice
```

### Eureka no muestra dos instancias

Espera 30 segundos y comprueba:

```powershell
curl -H "Accept: application/json" http://localhost:8761/eureka/apps/BILLS-MICROSERVICE
```

Si estas en local sin Docker, asegurate de que la segunda instancia tiene otro puerto:

```powershell
--server.port=8041 --eureka.instance.instance-id=bills-microservice-8041
```

### Admin no muestra una app que Eureka si muestra

Admin consulta Eureka periodicamente. Espera unos segundos y refresca. Si sigue sin aparecer:

```powershell
curl http://localhost:9090/instances
curl -H "Accept: application/json" http://localhost:8761/eureka/apps
```
