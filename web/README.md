# Mercado UMSS — App web de análisis de titulados

App en Java que lee encuestas de titulados y empleadores de Ingeniería en
Sistemas (UMSS), las guarda en una base de datos SQLite y muestra
indicadores, cruces estadísticos y pronósticos en el navegador.

## Requisitos (instalar antes)

| Qué | Versión | Por qué / nota |
|---|---|---|
| JDK 21 | 21.x (Temurin u Oracle) | El código es Java 17, pero **JDK 25 NO sirve**: la herramienta visual de Vaadin 24 falla con él (`Unsupported class file major version 69`). Cualquier JDK 17–21 vale. |
| Maven | 3.9+ | Compila y descarga librerías. |
| Internet | Solo la **primera vez** | Descarga librerías (~300 MB) y Node.js para lo visual. Después trabaja offline. |
| Puerto 8080 | Libre | Si está ocupado, la app no arranca (ver problemas). |
| Navegador | Cualquiera moderno | Chrome, Edge, Firefox. |

Comprobar versiones:
```
java -version     -> debe decir 17, 18, 19, 20 o 21 (NO 25 para Maven)
mvn -version
```

## Puesta en marcha (3 pasos, ~10 min la primera vez)

Desde la carpeta `web/`:

**1. Decirle a Maven qué Java usar** (solo si tu Java por defecto NO es 17–21):
```
set JAVA_HOME=C:\tools\jdk21
```

**2. Compilar todo:**
```
mvn install -DskipTests
```

**3. Arrancar el servidor:**
```
cd servidor
C:\tools\jdk21\bin\java.exe -jar target\mercado-servidor-1.0.0.jar
```
(o `mvn spring-boot:run` desde `web/servidor/` para modo desarrollo).

Abrir **http://localhost:8080**. Listo: la base `mercado.db` se crea sola
junto al programa y, si está vacía, importa los CSV de ejemplo
(61 titulados + 20 empleadores).

## Qué hay en cada pantalla

| Ruta | Contenido |
|---|---|
| `/` | 4 KPIs + recomendaciones automáticas |
| `/titulados` | Tabla con filtro + antigüedad + satisfacción + top áreas |
| `/empleadores` | Tabla + barras de acuerdo + competencias + cobertura |
| `/cruces` | Mapas de calor + veredicto χ² en frase simple |
| `/simulacion` | Botón Monte Carlo + pronósticos + historial guardado |
| `/registro` | Formularios para cargar encuestas a mano |
| `/importar` | Subir CSV (no duplica id ya guardados) |

> Cada pantalla trae un panel plegable **"Cómo leer"**. Y si quieres la
> teoría completa (qué es cada número, fórmulas y en qué archivo está
> cada cálculo), lee **`GUIA_TEORIA.md`**.

## Dónde está cada cosa (mapa para novatos)

```
web/
├── pom.xml                 Raíz: ordena módulos y fija versiones.
├── README.md               Este archivo.
├── GUIA_TEORIA.md          Teoría de cero a experto + código de cada dato.
├── nucleo/                 Lógica pura (SIN Spring ni Vaadin).
│   └── .../nucleo/
│       ├── csv/        LectorCsv         CSV <-> filas.
│       ├── limpieza/   LimpiezaDatos     Data Wrangling.
│       ├── stats/      Numeros, Frecuencias, PruebaChiCuadrada,
│       │               IntervalosConfianza (+ records de resultado).
│       ├── likert/     EscalaLikert      Texto -> 1..4 + etiquetas.
│       └── guia/       GuiasLectura      Textos "CÓMO LEER".
└── servidor/               App web (Spring Boot + Vaadin + SQLite).
    ├── src/main/resources/
    │   ├── application.properties  Puerto, ruta mercado.db, dialecto.
    │   └── datos-iniciales/        titulados.csv + empleadores.csv semilla.
    └── .../servidor/
        ├── Aplicacion.java     Main de 10 líneas.
        ├── datos/              Titulado, Empleador, Simulacion (@Entity =
        │                       las 3 tablas) + 3 Repositorios (interfaces,
        │                       cero SQL: Spring los implementa solo).
        ├── servicio/           Carga, Analisis, Cruces, Simulacion,
        │                       Recomendaciones, CargadorInicial.
        └── vista/              MainLayout + 7 pantallas (Java puro).
```

## Reglas del proyecto (no negociables)

- Sin generadores congruenciales ni métodos de rechazo/composición.
- Único azar permitido: sorteos Monte Carlo (`Random`) en simulación.
- Fuente de datos: solo encuestas (CSV semilla, formulario o subida).
- Código simple antes que óptimo: métodos cortos, sin abreviaturas.

## Problemas típicos

| Síntoma | Causa y arreglo |
|---|---|
| `Unsupported class file major version 69` | Estás compilando con JDK 25. Usa JDK 17–21 (`set JAVA_HOME=...`). |
| `Port 8080 already in use` | Otra app usa el puerto. Ciérrala o cambia `server.port` en `application.properties`. |
| Pantalla en blanco la 1ª vez | Vaadin compila lo visual (1–3 min). Espera y recarga. |
| Quiero empezar de cero | Detén la app, **borra `servidor/mercado.db`** y arranca: se recrea con los CSV semilla. |
| Importé mal un CSV | Borra `mercado.db` y vuelve a importar (o importa el corregido: los id repetidos se saltan). |
| Sin internet en la 1ª compilación | Imposible: Maven debe descargar librerías. Conéctate una vez. |

## Detener el servidor
`Ctrl + C` en la terminal donde corre (o cierra esa ventana).
