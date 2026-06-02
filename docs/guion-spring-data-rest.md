# Guion docente: Spring Data REST en Petclinic

Este guion prepara una demo corta sobre Spring Data REST usando el monolito `spring-petclinic-monolith`.

## Objetivo

- Entender que Spring Data REST genera endpoints HTTP a partir de repositorios Spring Data.
- Ver el formato HAL: `_links`, recursos, colecciones y relaciones por URI.
- Practicar busqueda derivada desde un metodo de repositorio.
- Discutir cuando tiene sentido y cuando no exponer repositorios directamente.

## Entorno

- Proyecto: `spring-petclinic-monolith`
- Endpoint base de la demo: `http://localhost:8080/datarest`
- Repositorios expuestos:
  - `diseases`: CRUD sobre `Disease`
  - `pet-types`: lectura de `PetType`

Arranque recomendado:

```powershell
.\mvnw.cmd -pl spring-petclinic-monolith spring-boot:run
```

> Nota: en PowerShell usa `curl.exe` si quieres ejecutar comandos curl reales. `curl` puede resolver al alias de `Invoke-WebRequest`.

## 1. Punto de partida

### Mensaje inicial

Spring Data REST no es otro controlador escrito a mano. Es una capa que mira los repositorios Spring Data y publica recursos HTTP siguiendo convenciones.

### Codigo clave

Dependencia:

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-rest</artifactId>
</dependency>
```

Configuracion didactica:

```java
@Configuration
public class SpringDataRestConfiguration implements RepositoryRestConfigurer {

	@Override
	public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
		config.setBasePath("/datarest");
		config.disableDefaultExposure();
		config.exposeIdsFor(Disease.class, PetType.class);
	}

}
```

Mensaje clave:

> La parte importante de la demo no es solo anadir el starter. Es decidir que repositorios se publican y bajo que ruta.

## 2. Descubrir la API

### Paso

```powershell
curl.exe http://localhost:8080/datarest
```

### Lectura esperada

La respuesta no es una lista de enfermedades. Es un documento de entrada con enlaces:

```json
{
  "_links": {
    "diseases": { "href": "http://localhost:8080/datarest/diseases" },
    "petTypes": { "href": "http://localhost:8080/datarest/pet-types" }
  }
}
```

### Pregunta de clase

- "Por que la API empieza devolviendo enlaces en vez de datos?"

### Mensaje clave

> En HAL el cliente puede navegar por enlaces. El endpoint raiz actua como indice de recursos disponibles.

## 3. Leer colecciones y recursos

### Pasos

```powershell
curl.exe http://localhost:8080/datarest/diseases
curl.exe http://localhost:8080/datarest/diseases/1
curl.exe http://localhost:8080/datarest/pet-types
```

### Lectura esperada

En `/datarest/diseases` aparece una coleccion HAL:

```json
{
  "_embedded": {
    "diseases": [
      {
        "id": 1,
        "name": "COVID-19",
        "description": "...",
        "_links": {
          "self": { "href": "http://localhost:8080/datarest/diseases/1" },
          "disease": { "href": "http://localhost:8080/datarest/diseases/1" },
          "petTypeswithPrevalence": {
            "href": "http://localhost:8080/datarest/diseases/1/petTypeswithPrevalence"
          }
        }
      }
    ]
  }
}
```

### Preguntas de clase

- "Que diferencia hay entre los datos del recurso y sus relaciones?"
- "Por que `petTypeswithPrevalence` aparece como enlace y no necesariamente embebido?"

### Mensaje clave

> Spring Data REST representa relaciones como recursos navegables. Eso reduce acoplamiento, pero no sustituye automaticamente a un DTO disenado para una pantalla concreta.

## 4. Busquedas derivadas

### Codigo

```java
@RepositoryRestResource(path = "diseases", collectionResourceRel = "diseases")
public interface DiseaseRepository extends CrudRepository<Disease, Integer> {

	@RestResource(path = "by-name", rel = "by-name")
	Collection<Disease> findByNameContainingIgnoreCase(@Param("name") String name);

}
```

### Paso

```powershell
curl.exe "http://localhost:8080/datarest/diseases/search/by-name?name=diab"
```

### Lectura esperada

Debe devolver `Diabetes`.

### Pregunta de clase

- "Donde esta implementado el endpoint `/search/by-name`?"

### Mensaje clave

> La URL de busqueda sale de un metodo del repositorio. Spring Data REST convierte la firma del metodo en un recurso HTTP.

## 5. Crear un recurso con una relacion

### Paso

En PowerShell:

```powershell
$body = @'
{
  "name": "Otitis externa",
  "description": "Inflamacion persistente del conducto auditivo externo en mascotas.",
  "petTypeswithPrevalence": [
    "http://localhost:8080/datarest/pet-types/2"
  ]
}
'@

Invoke-WebRequest `
  -Uri http://localhost:8080/datarest/diseases `
  -Method POST `
  -ContentType "application/json" `
  -Body $body
```

### Lectura esperada

- HTTP `201 Created`.
- Cabecera `Location` con la URL de la nueva enfermedad.
- En el log SQL se ve un `insert` en `diseases` y otro en la tabla de relacion.

### Comprobacion

```powershell
curl.exe "http://localhost:8080/datarest/diseases/search/by-name?name=otitis"
```

### Mensaje clave

> Para relacionar entidades, el cliente envia URIs de recursos existentes. No envia un objeto `PetType` completo.

## 6. Seguridad y exposicion

### Codigo clave

```java
config.disableDefaultExposure();
```

Y en el repositorio:

```java
@RestResource(exported = true)
Collection<Disease> findAll();

@RestResource(exported = true)
<S extends Disease> S save(S disease);
```

### Discusion

Sin `disableDefaultExposure()`, Spring Data REST puede publicar repositorios que no queriamos ensenar como API publica. En esta demo se exponen solo los repositorios anotados y solo los metodos marcados.

### Preguntas de clase

- "Que pasaria si publicaramos `OwnerRepository` completo?"
- "Debe un usuario externo poder invocar directamente `save` sobre cualquier agregado?"
- "Que reglas de negocio nos saltamos si no pasamos por un servicio?"

### Mensaje clave

> Spring Data REST es muy productivo, pero la decision de exposicion es una decision de arquitectura y seguridad.

## 7. Comparacion con controladores REST propios

En este proyecto tambien hay controladores REST escritos a mano, por ejemplo `/api/v1/visits`.

### Spring Data REST encaja bien cuando

- El recurso es simple.
- La API se parece mucho al modelo persistente.
- Queremos una demo, backoffice interno o prototipo rapido.
- La navegacion por enlaces aporta valor.

### Un controlador propio encaja mejor cuando

- Hay un caso de uso con reglas de negocio.
- La entrada y salida no coinciden con la entidad JPA.
- Necesitamos DTOs estables para clientes externos.
- Hay autorizacion fina por usuario, rol, propietario del dato o estado del recurso.
- Queremos controlar errores, validacion y transacciones desde la capa de aplicacion.

### Mensaje clave

> Spring Data REST expone repositorios. Un controlador de aplicacion expone casos de uso. No son la misma frontera.

## 8. Ejercicio de cierre

### Propuesta

1. Quitar temporalmente `@RestResource(exported = true)` de `findAll`.
2. Arrancar la aplicacion.
3. Probar:

```powershell
curl.exe http://localhost:8080/datarest/diseases
curl.exe "http://localhost:8080/datarest/diseases/search/by-name?name=diab"
```

### Resultado esperado

- La coleccion deja de estar disponible.
- La busqueda puede seguir disponible si el metodo de busqueda esta expuesto.

### Pregunta de clase

- "Que nos ensena esto sobre `disableDefaultExposure()`?"

## Cierre

### Ideas que deben quedar

- `spring-boot-starter-data-rest` puede convertir repositorios en una API HAL.
- Los enlaces son parte central del contrato, no decoracion.
- Las busquedas derivadas se publican desde metodos de repositorio.
- Exponer repositorios directamente puede saltarse servicios y casos de uso.
- En una aplicacion real conviene empezar por una politica restrictiva de exposicion.

### Recursos

- `spring-petclinic-monolith/src/main/java/org/springframework/samples/petclinic/configuration/SpringDataRestConfiguration.java`
- `spring-petclinic-monolith/src/main/java/org/springframework/samples/petclinic/repository/DiseaseRepository.java`
- `spring-petclinic-monolith/src/main/java/org/springframework/samples/petclinic/repository/PetTypeRestRepository.java`
- `spring-petclinic-monolith/src/test/java/org/springframework/samples/petclinic/web/api/SpringDataRestIntegrationTests.java`
