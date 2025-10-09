# API Pagos Referenciados V1.3

Crear una API RESTful desde cero utilizando Spring Boot, implementando los principales
endpoints de la API de Pagos Referenciados descrita en la documentación proporcionada.
El candidato deberá demostrar su capacidad para construir un backend robusto, escalable
y bien documentado, asegurando la calidad del código, buenas prácticas de desarrollo y
manejo de escenarios complejos como autenticación, callbacks, restricciones de IP y
optimización de consultas

# Requerimientos

- Spring Boot v3.4.10
- JDK v17
- Apache Maven v3.9.11

# Instrucciones

El API está disponible en http://localhost:5800.

Cuenta con rutas públicas y rutas privadas:

Rutas públicas:

/api/v1/authenticate → Permite generar un token Bearer para acceder a las rutas privadas.

Rutas privadas (requieren token Bearer):

/api/v1/payment → Crear un nuevo pago referenciado.

/api/v1/payment/callback → Registrar pagos confirmados mediante callback.

/api/v1/payment/{reference}/{paymentId} → Consultar un pago específico.

/api/v1/payments/search → Buscar pagos por rango de fechas y estado (startCreationDate, endCreationDate, startPaymentDate, endPaymentDate, status).

/api/v1/payment/cancel → Cancelar un pago previamente generado.

Todas las rutas privadas requieren incluir el token Bearer en el encabezado Authorization. Si no se envía o el token es inválido, el API responderá con HTTP 401 (Unauthorized).

## Paso 1) Compila el proyecto 

```shell
mvn clean install
```

## Paso 2) Ejecuta el proyecto 

```shell
mvn spring-boot:run
```

## Paso 3) Generar y actualizar token

Genera un token nuevo desde la ruta o endpoint `http://localhost:5800/api/v1/authenticate` usando Postman, con un cuerpo JSON como:

```json
{
 "username":"usuario_test",
 "password":"password_secreto"
}
```

Actualiza el token de `jwt.app.token` desde el archivo `src\main\resources\application.properties` y `src\test\resources\application-test.properties`

Vuelve a correr el proyecto con el token actualizado

```shell
mvn spring-boot:run
```

### NOTA

El token (`jwt.app.token`) se usa para realizar CallBack y realizar las pruebas unitarias

## Paso 4) Ejecuta las pruebas unitarias

```shell
mvn clean test
```

# Vista previas

![test_01.jpg](/screenshot/test_01.jpg)

![test_02.jpg](/screenshot/test_02.jpg)

![test_03.jpg](/screenshot/test_03.jpg)

![test_04.jpg](/screenshot/test_04.jpg)

![test_05.jpg](/screenshot/test_05.jpg)

![test_06.jpg](/screenshot/test_06.jpg)
