# MEGA-CONTEXTO: Mercado UMSS — App web de análisis de titulados

> **Última actualización:** 2026-11-11  
> **Commit de referencia:** `5a4348a`  
> **Repositorio:** https://github.com/poko233/actividad2_tss  
> **App desplegada:** https://actividad2-tss.onrender.com  

---

## 1. Resumen ejecutivo

**Qué es:** Aplicación web en Java que lee encuestas de titulados y empleadores de
Ingeniería en Sistemas (UMSS), las guarda en SQLite y muestra indicadores, cruces
estadísticos (χ²), gráficos y pronósticos Monte Carlo en el navegador.

**Para qué sirve:** Alimentar el Plan de Desarrollo Estratégico y la acreditación
ARCUSUR con evidencia empírica sobre formación continua, empleabilidad y brechas
de competencias.

**Quién la usa:** La Dirección de Carrera de Ingeniería en Sistemas (UMSS).

---

## 2. Stack tecnológico (versiones exactas)

| Componente | Versión | Para qué |
|------------|---------|----------|
| JDK | 21 (Temurin) | Compilar y correr (el código es Java 17, pero Vaadin 24 no soporta JDK 25) |
| Maven | 3.9.9 | Compilar y empaquetar |
| Spring Boot | 3.3.5 | Servidor web + inyección de dependencias |
| Vaadin | 24.6.13 | Frontend 100 % Java (sin HTML/JS manual) |
| SQLite | 3.53.4.0 (driver) | Base de datos en un archivo (`mercado.db`) |
| Hibernate | 6.5.3.Final | ORM: crea tablas desde clases `@Entity` |
| Apache Commons CSV | 1.14.1 | Leer/escribir CSV |
| Apache Commons Math | 3.6.1 | Estadística (media, χ², t-Student) |

---

## 3. Estructura del monorepo (tree actualizado)

```
web/
├── pom.xml                                    # Raíz del monorepo: módulos y versiones
├── Dockerfile                                 # Multi-stage: compila con Maven, ejecuta con JRE 21
├── .dockerignore                               # Excluye target/, node_modules/, etc.
├── README.md                                  # Documentación general
├── GUIA_TEORIA.md                             # Teoría de estadística + dónde está cada cálculo
├── nucleo/                                    # Librería Java pura (sin Spring ni Vaadin)
│   ├── pom.xml
│   └── src/main/java/bo/umss/mercado/nucleo/
│       ├── csv/
│       │   └── LectorCsv.java                 # Leer/escribir CSV (Apache Commons CSV)
│       ├── limpieza/
│       │   ├── LimpiezaDatos.java             # Data Wrangling: nulos, SI/NO, números, acentos
│       │   └── LimpiezaCruda.java             # Convierte CSV crudo de MS Forms → formato normalizado
│       ├── stats/
│       │   ├── Frecuencias.java               # Conteos por categoría, moda, histogramas
│       │   ├── IntervaloHistograma.java       # Record: una barra del histograma
│       │   ├── IntervalosConfianza.java       # IC 95% (medias, proporciones, diferencias)
│       │   ├── Numeros.java                   # Media, mediana, desv, cuartiles (Commons Math)
│       │   ├── PruebaChiCuadrada.java         # χ² + p-valor (Commons Math)
│       │   ├── RangoConfianza.java            # Record del IC
│       │   ├── ResultadoChiCuadrada.java      # Record del resultado de χ²
│       │   └── ResumenNumerico.java           # Record de estadísticos numéricos
│       ├── likert/
│       │   └── EscalaLikert.java              # Escala 1-4: texto → número + etiquetas de lectura
│       └── guia/
│           └── GuiasLectura.java              # Textos "CÓMO LEER" de cada pantalla
├── servidor/                                  # App web (Spring Boot + Vaadin + SQLite)
│   ├── pom.xml
│   ├── src/main/java/bo/umss/mercado/servidor/
│   │   ├── Aplicacion.java                    # Main: 10 líneas
│   │   ├── ControladorExportacion.java        # REST: GET /exportar/{nombre}.csv
│   │   ├── datos/
│   │   │   ├── Titulado.java                 # @Entity: tabla titulado (35 columnas)
│   │   │   ├── Empleador.java                # @Entity: tabla empleador (24 columnas)
│   │   │   ├── Simulacion.java               # @Entity: tabla simulacion (historial Monte Carlo)
│   │   │   ├── RepositorioTitulados.java      # Interface: cero SQL
│   │   │   ├── RepositorioEmpleadores.java    # Interface: cero SQL
│   │   │   └── RepositorioSimulaciones.java   # Interface: cero SQL + orden por fecha
│   │   ├── servicio/
│   │   │   ├── CargadorInicial.java           # Al arrancar con BD vacía, importa CSV
│   │   │   ├── FichaCruce.java               # Record: resultado de un cruce
│   │   │   ├── FichaEscenario.java           # Record: resultado de un escenario
│   │   │   ├── FichaSegmento.java            # Record: resultado de un segmento
│   │   │   ├── ServicioAnalisis.java         # Indicadores, resúmenes, columnas derivadas
│   │   │   ├── ServicioCarga.java            # Importa CSV normalizado a la BD
│   │   │   ├── ServicioCruces.java           # Arma los 7 cruces χ²
│   │   │   ├── ServicioExportacion.java      # Genera los 17 CSV descargables
│   │   │   ├── ServicioImportacionCruda.java  # Convierte CSV crudo → normalizado
│   │   │   ├── ServicioRecomendaciones.java  # Reglas if-then → recomendaciones
│   │   │   └── ServicioSimulacion.java       # Monte Carlo + historial
│   │   └── vista/
│   │       ├── MainLayout.java               # Menú lateral con 8 pantallas
│   │       ├── GraficoCanvas.java            # Motor de gráficos SVG (barras, torta, heatmap, radar)
│   │       ├── VistaPanel.java               # / — KPIs + recomendaciones
│   │       ├── VistaTitulados.java           # /titulados — HU1 + HU2 con gráficos
│   │       ├── VistaEmpleadores.java         # /empleadores — HU4 con gráficos
│   │       ├── VistaCruces.java              # /cruces — mapas de calor χ²
│   │       ├── VistaSimulacion.java          # /simulacion — Monte Carlo + historial
│   │       ├── VistaRegistro.java            # /registro — formularios de recolección
│   │       ├── VistaImportar.java            # /importar — carga masiva con modal Duplicar/Reemplazar
│   │       └── VistaExportar.java            # /exportar — botones para bajar CSV
│   └── src/main/resources/
│       ├── application.properties            # Puerto, ruta BD, dialecto SQLite
│       └── datos-iniciales/
│           ├── TITULADOS CRUDO.csv          # 148 filas, 57 columnas (exportado de MS Forms)
│           ├── TITULADOS CRUDO.xlsx          # Excel original de la encuesta
│           ├── empleadores.csv               # 53 filas, 24 columnas (normalizado)
│           ├── titulados.csv                 # 148 filas, 35 columnas (normalizado)
│           └── EMPLEADORES CRUDO.csv         # 80 filas, 32 columnas (exportado de MS Forms)
│           └── EMPLEADORES CRUDO.xlsx        # Excel original de la encuesta
├── nucleo/pom.xml                            # Dependencias: Commons CSV, Commons Math
└── servidor/pom.xml                          # Dependencias: Spring Boot, Vaadin, SQLite
```

---

## 4. Arquitectura y flujo de datos

```
┌─────────────────────────────────────────────────────────────────────────┐
│  DATOS DE ENTRADA                                                       │
│  datos-iniciales/TITULADOS CRUDO.csv  (57 cols, MS Forms)              │
│  datos-iniciales/EMPLEADORES CRUDO.csv (32 cols, MS Forms)              │
│  datos-iniciales/titulados.csv          (35 cols, normalizado)          │
│  datos-iniciales/empleadores.csv         (24 cols, normalizado)          │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  CARGA (CargadorInicial → ServicioCarga)                               │
│  - Lee CSV con LectorCsv (Apache Commons CSV)                          │
│  - Aplica LimpiezaDatos.limpiarFila() (Data Wrangling)                 │
│  - Guarda en SQLite (mercado.db) vía RepositorioTitulados/Empleadores  │
│  - Si la BD está vacía, importa los CSV de datos-iniciales/            │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  ANÁLISIS (ServicioAnalisis, ServicioCruces, ServicioSimulacion)        │
│  - Lee filas de la BD como Map<String, String> (comoMapa())            │
│  - Delega cálculos a nucleo/stats/ (Commons Math)                      │
│  - Genera fichas (FichaCruce, FichaEscenario, FichaSegmento)            │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    ▼               ▼               ▼
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│  VISTAS (Vaadin) │  │  EXPORTACIÓN     │  │  GRÁFICOS       │
│  8 pantallas     │  │  Controlador     │  │  GraficoCanvas   │
│  en Java puro    │  │  Exportacion     │  │  (SVG inline)    │
│  (sin HTML/JS)   │  │  (17 CSV)        │  │                  │
└──────────────────┘  └──────────────────┘  └──────────────────┘
```

---

## 5. Backend (servidor/)

### 5.1. Entidades (datos/)

| Clase | Tabla | Columnas | Descripción |
|-------|-------|----------|-------------|
| `Titulado` | `titulado` | 35 | Una fila = un encuestado titulado |
| `Empleador` | `empleador` | 24 | Una fila = una empresa encuestada |
| `Simulacion` | `simulacion` | 9 | Historial de corridas Monte Carlo |

**Patrón clave:** Cada entidad tiene:
- `comoMapa()` → devuelve `Map<String, String>` con las mismas claves del CSV (para que el núcleo las procese).
- `desdeMapa(Map)` → construye la entidad desde una fila de CSV.

### 5.2. Repositorios (datos/)

Son **interfaces vacías** que extienden `JpaRepository`. Spring Data JPA genera automáticamente el código SQL:
- `findAll()`, `save()`, `deleteAll()`, `count()`
- `RepositorioSimulaciones` tiene `findAllByOrderByFechaHoraDesc()` (Spring traduce el nombre del método a `ORDER BY fechaHora DESC`).

### 5.3. Servicios (servicio/)

| Servicio | Responsabilidad |
|----------|-----------------|
| `ServicioAnalisis` | Lee la BD, calcula indicadores (tasa inserción, intención posgrado, beca), resúmenes numéricos, frecuencias, segmentación, recomendaciones |
| `ServicioCarga` | Importa CSV normalizado a la BD (salta id repetidos) |
| `ServicioCruces` | Arma los 7 cruces χ² con `PruebaChiCuadrada` |
| `ServicioExportacion` | Genera 17 CSV (indicadores, distribuciones, matrices, Likert, segmentos, histogramas, competencias) |
| `ServicioImportacionCruda` | Convierte CSV crudo de MS Forms → normalizado, con detección automática de tipo |
| `ServicioSimulacion` | Monte Carlo: 5 escenarios, 1000 repeticiones, historial en BD |
| `ServicioRecomendaciones` | Reglas if-then sobre los datos |
| `CargadorInicial` | Al arrancar con BD vacía, importa los CSV de `datos-iniciales/` |

### 5.4. Controladores (raíz del paquete servidor)

| Controlador | Endpoint | Descripción |
|-------------|----------|-------------|
| `ControladorExportacion` | `GET /exportar/{nombre}.csv` | Sirve los CSV como descarga (con `Content-Disposition: attachment`) |

### 5.5. Configuración

**`application.properties`:**
```properties
server.port=${PORT:8080}                                    # Lee PORT de Render, default 8080
spring.datasource.url=jdbc:sqlite:mercado.db                  # BD en archivo
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=update                         # Crea/actualiza tablas solo
spring.jpa.show-sql=false
vaadin.launch-browser=false
```

---

## 6. Frontend (Vaadin 24 — Java puro)

### 6.1. Pantallas (vista/)

| Pantalla | Ruta | HU | Descripción |
|----------|------|----|-------------|
| `VistaPanel` | `/` | — | KPIs + recomendaciones automáticas |
| `VistaTitulados` | `/titulados` | HU1, HU2 | Tabla con filtro + histograma + barras + torta |
| `VistaEmpleadores` | `/empleadores` | HU4 | Tabla + barras Likert + radar + torta |
| `VistaCruces` | `/cruces` | HU3, HU5 | Mapas de calor + χ² en frase |
| `VistaSimulacion` | `/simulacion` | Experimentación | Botón Monte Carlo + historial |
| `VistaRegistro` | `/registro` | Recolección | Formularios con listas cerradas |
| `VistaImportar` | `/importar` | Recolección | Subir CSV crudo o normalizado |
| `VistaExportar` | `/exportar` | HU5 | Botones para bajar los 17 CSV |

### 6.2. GraficoCanvas.java

Motor de gráficos SVG (sin librerías externas). Usa `Base64` + `<img src="data:image/svg+xml;base64,...">` para que el SVG viaje dentro del HTML del servidor.

Métodos disponibles:
- `barrasVerticales(id, datos, etiquetas, color, titulo)` → histogramas y frecuencias
- `torta(id, datos, etiquetas, colores, titulo)` → proporciones (pie chart)
- `mapaCalor(id, obs, filas, cols, titulo)` → tablas de contingencia coloreadas
- `radar(id, valores, etiquetas, titulo)` → diagrama de radar (escala 1-4)
- `enTarjeta(contenido, tituloTarjeta)` → envuelve un gráfico en una tarjeta blanca

---

## 7. Núcleo (nucleo/)

Librería Java pura con la lógica de análisis. **Sin Spring ni Vaadin** para que se pueda probar y entender sin servidor.

### 7.1. csv/
- `LectorCsv.java` — Lee y escribe CSV (Apache Commons CSV). Cada fila es `Map<String, String>`.

### 7.2. limpieza/
- `LimpiezaDatos.java` — Data Wrangling: recorta espacios, vacía nulos, unifica SI/NO, depura numéricos, normaliza texto (mayúsculas, sin tildes, espacios → `_`).
- `LimpiezaCruda.java` — Convierte CSV crudo de MS Forms (57/32 columnas) al formato normalizado (35/24 columnas) con todas las correcciones de valores.

### 7.3. stats/
- `Numeros.java` — Media, mediana, desv, cuartiles, min, max (Apache Commons Math `DescriptiveStatistics`)
- `Frecuencias.java` — Conteos por categoría (`contar`), moda, histogramas (`histograma`)
- `PruebaChiCuadrada.java` — χ² + p-valor (Apache Commons Math `ChiSquareTest`)
- `IntervalosConfianza.java` — IC 95% para medias (t Student), proporciones (Z), diferencias de medias (varianzas agrupadas)
- `ResumenNumerico.java`, `RangoConfianza.java`, `ResultadoChiCuadrada.java`, `IntervaloHistograma.java` — Records (cajitas inmutables)

### 7.4. likert/
- `EscalaLikert.java` — Traduce texto a 1..4 + etiquetas de lectura ("tibio", "de acuerdo", etc.)

### 7.5. guia/
- `GuiasLectura.java` — Textos "CÓMO LEER" de cada pantalla (χ², IC, Monte Carlo, Likert, segmentos)

---

## 8. Base de datos (SQLite)

**Archivo:** `mercado.db` (junto al programa, se crea sola si no existe)

**Tablas:**

| Tabla | Columnas | Descripción |
|-------|----------|-------------|
| `titulado` | 35 | Una fila = un encuestado titulado (espejo de titulados.csv) |
| `empleador` | 24 | Una fila = una empresa encuestada (espejo de empleadores.csv) |
| `simulacion` | 9 | Historial de corridas Monte Carlo |

**Patrón:** Todo se guarda como texto (igual que el CSV); los números se convierten solo al momento de calcular. Así nada se pierde.

---

## 9. Convenciones de código

### 9.1. Nombres
- **Español completo**, sin abreviaturas (`servicioAnalisis`, no `svcA`).
- **Clases:** PascalCase (`ServicioAnalisis`, `GraficoCanvas`).
- **Métodos:** camelCase (`calcularProporcion`, `leerColumnaTexto`).
- **Constantes:** MAYÚSCULAS con `_` (`ITEMS_LIKERT`, `COLOR_PRINCIPAL`).
- **Records:** nombres descriptivos (`ResumenNumerico`, `FichaCruce`, `RangoConfianza`).

### 9.2. Estructura de métodos
- Cada método tiene un comentario de 3-5 líneas explicando QUÉ calcula, de QUÉ HU viene y CÓMO leer el resultado.
- Métodos cortos (< 30 líneas). Si es largo, se extrae en helpers.
- Sin `String.format()` con `%` en strings que contienen código JavaScript (causa `FormatFlagsConversionMismatchException`). Usar concatenación `+` o `String.valueOf()`.

### 9.3. Patrones usados
- **Records** para resultados inmutables (no se mutan, se crean y se leen).
- **Interfaces vacías** para repositorios (Spring Data genera el SQL).
- **Inyección por constructor** (no por campo) para que las dependencias sean explícitas.
- **Delegación a librerías** (Apache Commons Math, Commons CSV) en lugar de reinventar fórmulas.
- **`Map<String, String>`** para filas de CSV (legible: `fila.get("edad")` en vez de `fila[2]`).

### 9.4. Imports
- `java.util.*` agrupado.
- Sin imports estáticos salvo constantes de clase.
- `Locale.US` en todo `String.format()` para evitar coma decimal en Windows.

---

## 10. Cómo compilar y ejecutar

### Requisitos
- JDK 21 (Temurin u Oracle) — **NO JDK 25** (Vaadin 24 no lo soporta).
- Maven 3.9+
- Internet solo la primera vez.

### Compilar
```powershell
set JAVA_HOME=C:\tools\jdk21
cd web
mvn install -DskipTests
```

### Ejecutar (local)
```powershell
cd servidor
C:\tools\jdk21\bin\java.exe -jar target\mercado-servidor-1.0.0.jar
```
Abrir http://localhost:8080

### Desplegar (Render)
1. Runtime: Docker
2. Root Directory: `web/`
3. Docker Build Context: `web/`
4. Dockerfile Path: `Dockerfile`
5. Variables: `JAVA_OPTS=-Xmx256m -Xss512k`, `PORT=8080`

---

## 11. Cómo agregar una nueva pantalla

1. Crear `vista/VistaNueva.java` con:
```java
@Route(value = "nueva", layout = MainLayout.class)
@PageTitle("Nueva | Mercado UMSS")
public class VistaNueva extends VerticalLayout {
    public VistaNueva(ServicioAnalisis analisis) {
        add(new H1("Nueva pantalla"));
    }
}
```
2. Agregar al menú en `MainLayout.java`:
```java
pestañaPara("Nueva", VistaNueva.class)
```
3. Si necesita un servicio, inyectarlo en el constructor (Spring lo entrega solo).

---

## 12. Cómo agregar un nuevo campo a la encuesta

1. `datos/Titulado.java` (o `Empleador.java`):
   - Agregar campo + getter/setter
   - Agregar línea en `comoMapa()`: `fila.put("nuevo_campo", nuevoCampo);`
   - Agregar `case "nuevo_campo" -> titulado.setNuevoCampo(valor);` en `desdeMapa()`
2. Al arrancar, Hibernate agrega la columna sola (`ddl-auto: update`).
3. Usar en servicios: `analisis.columnaDe(mapas, "nuevo_campo")`.
4. Usar en vistas: `tabla.addColumn(Titulado::getNuevoCampo).setHeader("Nuevo");`

**Sin tocar SQL jamás.**

---

## 13. Cómo agregar un nuevo gráfico

1. Si el gráfico es SVG simple (barras, torta, heatmap, radar), usar `GraficoCanvas.enTarjeta(...)`:
```java
add(GraficoCanvas.enTarjeta(
    GraficoCanvas.barrasVerticales("idUnico", datos, etiquetas, color, "Título"),
    "Gráfico [N]: descripción"));
```
2. Si necesitas un gráfico nuevo (ej. línea, dona), agregar el método en `GraficoCanvas.java` siguiendo el patrón: construir SVG con `StringBuilder`, usar `contenedorConSvg()`, insertar con `executeJs("this.innerHTML = $0", svg)`.

---

## 14. Cómo agregar un nuevo CSV exportable

1. `ServicioExportacion.java`:
   - Agregar el nombre a `nombresDisponibles()`
   - Agregar `case "nuevo_nombre": return nuevoMetodo();`
   - Implementar `nuevoMetodo()` que devuelva un `String` CSV
2. `VistaExportar.java`:
   - Agregar el nombre a la lista correspondiente en el constructor

---

## 15. Cómo agregar un nuevo cruce χ²

1. `ServicioCruces.java` → agregar en `crucesTitulados()` o `crucesEmpleadores()`:
```java
cruces.add(armarCruce("Etiqueta descriptiva",
        "columnaA", "columnaB", mapas));
```
2. Si alguna columna es derivada (como `segmento_antiguedad`), agregarla en `columnaDerivada()`.

---

## 16. Cómo agregar un nuevo escenario de simulación

1. `ServicioSimulacion.java` → agregar a `ESCENARIOS`:
```java
new String[]{"Abrir maestría en X", "AREA_TEMATICA"},
```
2. El área debe coincidir con un valor de `area_interes_posgrado` (IA, ROBOTICA, ING_SOFTWARE, BASE_DATOS, REDES_SEGURIDAD).

---

## 17. Cómo hacer deploy en Render

1. Subir código a GitHub: `git add`, `git commit`, `git push`
2. En Render: **New + → Web Service**, conecta tu repositorio
3. Configuración:
   - **Runtime:** Docker
   - **Root Directory:** `web/`
   - **Docker Build Context:** `web/`
   - **Dockerfile Path:** `Dockerfile`
   - **Plan:** Free
4. Variables de entorno:
   - `JAVA_OPTS` = `-Xmx256m -Xss512k`
   - `PORT` = `8080`
5. **Deploy web service**

El `Dockerfile` es multi-stage: compila con Maven 3.9 + JDK 21, ejecuta con JRE 21 Alpine.

---

## 18. Glosario de archivos clave

| Archivo | Responsabilidad | Cuándo tocarlo |
|---------|-----------------|----------------|
| `Aplicacion.java` | Main: 10 líneas | Nunca (solo arranca Spring) |
| `MainLayout.java` | Menú lateral | Al agregar una nueva pantalla |
| `ServicioAnalisis.java` | Cerebro estadístico | Al agregar un nuevo indicador o análisis |
| `GraficoCanvas.java` | Motor de gráficos SVG | Al agregar un gráfico nuevo |
| `ServicioExportacion.java` | Genera los 17 CSV | Al agregar un nuevo exportable |
| `ControladorExportacion.java` | Sirve descargas HTTP | Nunca (ya maneja todo) |
| `ServicioImportacionCruda.java` | Convierte CSV crudo → normalizado | Al agregar un campo nuevo a la encuesta |
| `LimpiezaCruda.java` | Lógica de conversión de valores | Al agregar un campo nuevo a la encuesta |
| `ServicioCruces.java` | Arma los 7 cruces | Al agregar un nuevo cruce χ² |
| `ServicioSimulacion.java` | Monte Carlo | Al agregar un escenario nuevo |
| `ServicioCarga.java` | Importa CSV normalizado | Nunca (ya maneja todo) |
| `CargadorInicial.java` | Carga CSV al arrancar con BD vacía | Nunca (automático) |
| `Titulado.java` / `Empleador.java` | Entidades (@Entity) | Al agregar un campo nuevo a la encuesta |
| `application.properties` | Configuración del servidor | Al cambiar puerto, BD o dialecto |

---

## 19. Tree actualizado (para copiar y pegar en el futuro)

```
web/
├── pom.xml
├── Dockerfile
├── .dockerignore
├── .gitignore
├── README.md
├── GUIA_TEORIA.md
├── nucleo/
│   ├── pom.xml
│   └── src/main/java/bo/umss/mercado/nucleo/
│       ├── csv/LectorCsv.java
│       ├── guia/GuiasLectura.java
│       ├── likert/EscalaLikert.java
│       ├── limpieza/LimpiezaDatos.java
│       ├── limpieza/LimpiezaCruda.java
│       └── stats/
│           ├── Frecuencias.java
│           ├── IntervaloHistograma.java
│           ├── IntervalosConfianza.java
│           ├── Numeros.java
│           ├── PruebaChiCuadrada.java
│           ├── RangoConfianza.java
│           ├── ResultadoChiCuadrada.java
│           └── ResumenNumerico.java
├── servidor/
│   ├── pom.xml
│   └── src/main/
│       ├── java/bo/umss/mercado/servidor/
│       │   ├── Aplicacion.java
│       │   ├── ControladorExportacion.java
│       │   ├── datos/
│       │   │   ├── Empleador.java
│       │   │   ├── RepositorioEmpleadores.java
│       │   │   ├── RepositorioSimulaciones.java
│       │   │   ├── RepositorioTitulados.java
│       │   │   ├── Simulacion.java
│       │   │   └── Titulado.java
│       │   ├── servicio/
│       │   │   ├── CargadorInicial.java
│       │   │   ├── FichaCruce.java
│       │   │   ├── FichaEscenario.java
│       │   │   ├── FichaSegmento.java
│       │   │   ├── ServicioAnalisis.java
│       │   │   ├── ServicioCarga.java
│       │   │   ├── ServicioCruces.java
│       │   │   ├── ServicioExportacion.java
│       │   │   ├── ServicioImportacionCruda.java
│       │   │   ├── ServicioRecomendaciones.java
│       │   │   └── ServicioSimulacion.java
│       │   └── vista/
│       │       ├── GraficoCanvas.java
│       │       ├── MainLayout.java
│       │       ├── VistaCruces.java
│       │       ├── VistaEmpleadores.java
│       │       ├── VistaExportar.java
│       │       ├── VistaImportar.java
│       │       ├── VistaPanel.java
│       │       ├── VistaRegistro.java
│       │       ├── VistaSimulacion.java
│       │       └── VistaTitulados.java
│       └── resources/
│           ├── application.properties
│           └── datos-iniciales/
│               ├── TITULADOS CRUDO.csv
│               ├── TITULADOS CRUDO.xlsx
│               ├── EMPLEADORES CRUDO.csv
│               ├── EMPLEADORES CRUDO.xlsx
│               ├── titulados.csv
│               └── empleadores.csv
```

---

## 20. Notas para ampliación

### Agregar un campo nuevo a la encuesta
1. `Titulado.java`: campo + getter/setter + línea en `comoMapa()` y `desdeMapa()`
2. `LimpiezaCruda.java`: agregar el mapeo y la transformación
3. `VistaRegistro.java`: agregar el campo al formulario (opcional)
4. `VistaTitulados.java`: agregar columna a la tabla (opcional)
5. Hibernate agrega la columna sola al arrancar (`ddl-auto: update`)

### Agregar una nueva pantalla
1. Crear `vista/VistaNueva.java` con `@Route(value = "nueva", layout = MainLayout.class)`
2. Agregar en `MainLayout.java`: `pestañaPara("Nueva", VistaNueva.class)`
3. Inyectar servicios en el constructor (Spring los entrega solo)

### Agregar un nuevo gráfico
1. Usar `GraficoCanvas.enTarjeta(GraficoCanvas.barrasVerticales(...), "Título")`
2. Si es un gráfico nuevo, agregar método en `GraficoCanvas.java` siguiendo el patrón: construir SVG con `StringBuilder`, usar `contenedorConSvg()`

### Agregar un nuevo CSV exportable
1. `ServicioExportacion.java`: agregar nombre a `nombresDisponibles()` y `case "nombre": return nuevoMetodo();`
2. `VistaExportar.java`: agregar el nombre a la lista correspondiente

### Agregar un nuevo cruce χ²
1. `ServicioCruces.java`: agregar `cruces.add(armarCruce("Etiqueta", "colA", "colB", mapas));`

### Agregar un escenario de simulación
1. `ServicioSimulacion.java`: agregar `new String[]{"Título", "AREA_TEMATICA"}` a `ESCENARIOS`

---

## 21. Problemas típicos y soluciones

| Síntoma | Causa y solución |
|---------|-----------------|
| `Unsupported class file major version 69` | Compilaste con JDK 25. Usa JDK 17–21: `set JAVA_HOME=C:\tools\jdk21` |
| `Port 8080 already in use` | Otra app usa el puerto. Ciérrala o cambia `server.port` |
| Gráficos en blanco | Usar SVG puro (ya implementado). No usar canvas con `getContext` en un div |
| `FormatFlagsConversionMismatchException` | No usar `String.format()` con `%` en strings de JavaScript. Usar concatenación `+` |
| BOM en archivos Java | PowerShell `Set-Content -Encoding UTF8` agrega BOM. Usar `write` tool de OpenCode o eliminar BOM con Python |
| Datos se pierden al importar | Verificar que `LimpiezaCruda` reconozca todas las variantes del texto del Excel |
| CSV con columnas desplazadas | El CSV tiene comas sin comillas. Usar `quoting=csv.QUOTE_ALL` en Python o parser robusto |

---

## 22. Notas para ampliación

- **Cambiar a PostgreSQL:** Cambiar `application.properties` (URL, dialecto) y `pom.xml` (de SQLite a PostgreSQL). Los repositorios Spring Data JPA no cambian.
- **Agregar autenticación:** Spring Security + Vaadin Security. Crear vistas con `@AnonymousAllowed` o `@PermitAll`.
- **Agregar gráficos interactivos (zoom, tooltips avanzados):** Integrar Chart.js o D3.js mediante `executeJs` en un div.
- **Agregar exportación a PDF:** Usar iText o Apache PDFBox para generar el informe como PDF desde Java.
- **Agregar notificaciones por email:** Spring Mail + JavaMailSender para enviar resultados a la Dirección de Carrera.

---

## 23. Historial de cambios

| Fecha | Commit | Descripción |
|-------|--------|-------------|
| 2026-10-04 | `6a64cba` | Commit inicial (app de consola Java) |
| 2026-10-05 | `9e08ec4` | Fix cositas |
| 2026-10-05 | `dda55f6` | Refactor (comentarios, IC diferencia, sensibilidad) |
| 2026-10-05 | `7aab2e4` | Subir carpeta web completa (monorepo) |
| 2026-10-05 | `b627e1c` | Dockerfile y .dockerignore para Render |
| 2026-10-05 | `1e63d35` | Mover Dockerfile a web/ para monorepo |
| 2026-10-05 | `2d2c046` | Agregar gráficos canvas a todas las vistas |
| 2026-10-05 | `5f0b35e` | Eliminar FormatFlagsConversionMismatchException en gráficos |
| 2026-10-05 | `f0ea22e` | Reescribir gráficos en SVG puro (se ven sin JavaScript) |
| 2026-10-05 | `20f03c5` | Insertar SVG como HTML inline en vez de Data URI |
| 2026-10-05 | `6d42fce` | Exportar CSV desde la app (HU5) + fix de conversión de datos |
| 2026-10-29 | `5a4348a` | Subir CSV crudo directamente al software (modal Duplicar/Reemplazar) |

---

**Repositorio GitHub:** https://github.com/poko233/actividad2_tss  
**App desplegada:** https://actividad2-tss.onrender.com  
**Última actualización:** 2026-11-11  
